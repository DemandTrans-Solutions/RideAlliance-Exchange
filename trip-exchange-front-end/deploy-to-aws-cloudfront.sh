#!/bin/bash

# deploy-to-aws-cloudfront.sh
# Deploy Angular/Nx app to AWS S3 with CloudFront distribution
#
# Usage:
#   ./deploy-to-aws-cloudfront.sh [--ride-alliance] [initial-setup|update]
#
# Default target: exchange.demandtrans-apis.com
# Use --ride-alliance flag to target ride-alliance.demandtrans-apis.com instead.

set -e

# ---------------------------------------------------------------------------
# Parse flags — strip --ride-alliance before positional args
# ---------------------------------------------------------------------------
TARGET="exchange"
POSITIONAL_ARGS=()
for arg in "$@"; do
  case "$arg" in
    --ride-alliance)
      TARGET="ride-alliance"
      ;;
    *)
      POSITIONAL_ARGS+=("$arg")
      ;;
  esac
done
set -- "${POSITIONAL_ARGS[@]}"

# ---------------------------------------------------------------------------
# Ensure nvm is available and switch to Node v20
# ---------------------------------------------------------------------------
export NVM_DIR="${NVM_DIR:-$HOME/.nvm}"
if [ -s "$NVM_DIR/nvm.sh" ]; then
  # shellcheck source=/dev/null
  . "$NVM_DIR/nvm.sh"
fi

if command -v nvm >/dev/null 2>&1; then
  echo "Switching to Node v20 using nvm..."
  nvm use 20 || {
    echo "Failed to switch to Node v20. Please install it with 'nvm install 20' and try again." >&2
    exit 1
  }
else
  echo "nvm not found. Please install nvm (https://github.com/nvm-sh/nvm) and ensure it's available in this shell." >&2
  exit 1
fi

# ---------------------------------------------------------------------------
# CONFIGURATION — set per deployment target
# ---------------------------------------------------------------------------
AWS_REGION="us-east-1"
BUILD_DIR="dist/"

if [ "$TARGET" = "ride-alliance" ]; then
  S3_BUCKET="ride-alliance.demandtrans-apis.com"
  DOMAIN_NAME="ride-alliance.demandtrans-apis.com"
  OAI_ID_FILE=".oai_id_ride_alliance"
  DIST_ID_FILE=".cloudfront_dist_id_ride_alliance"
  # Change BUILD_CONFIG to a ride-alliance Angular configuration once one is added
  # e.g. "ride-alliance" → `yarn build --configuration ride-alliance`
  BUILD_CONFIG="production"
else
  S3_BUCKET="trip-exchange-demo.demandtrans.com"
  DOMAIN_NAME="exchange.demandtrans-apis.com"
  OAI_ID_FILE=".oai_id"
  DIST_ID_FILE=".cloudfront_dist_id"
  BUILD_CONFIG="production"
fi

AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)

echo "Deployment target : $TARGET"
echo "Domain            : $DOMAIN_NAME"
echo "S3 bucket         : $S3_BUCKET"
echo "Build config      : $BUILD_CONFIG"
echo ""

# ---------------------------------------------------------------------------
# Check for AWS CLI
# ---------------------------------------------------------------------------
if ! command -v aws &> /dev/null; then
  echo "AWS CLI not found. Please install it: https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html"
  exit 1
fi

# ---------------------------------------------------------------------------
# Helpers to persist/load OAI and CloudFront distribution IDs
# ---------------------------------------------------------------------------
load_oai_id() {
  if [ -f "$OAI_ID_FILE" ]; then
    OAI_ID=$(cat "$OAI_ID_FILE")
    export OAI_ID
    echo "Loaded OAI_ID from $OAI_ID_FILE: $OAI_ID"
  else
    OAI_ID=""
  fi
}

load_dist_id() {
  if [ -f "$DIST_ID_FILE" ]; then
    CLOUDFRONT_DIST_ID=$(cat "$DIST_ID_FILE")
    export CLOUDFRONT_DIST_ID
    echo "Loaded CloudFront Distribution ID from $DIST_ID_FILE: $CLOUDFRONT_DIST_ID"
  else
    CLOUDFRONT_DIST_ID=""
  fi
}

# ---------------------------------------------------------------------------
# Get (or error on missing) ACM wildcard cert for *.demandtrans-apis.com
# ---------------------------------------------------------------------------
get_or_request_acm_cert() {
  local DOMAIN="*.demandtrans-apis.com"
  echo "Checking for existing ACM certificate for $DOMAIN ..."
  CERT_ARN=$(aws acm list-certificates --region us-east-1 \
    --query "CertificateSummaryList[?DomainName=='$DOMAIN'].CertificateArn" \
    --output text | awk '{print $1}')

  if [ -n "$CERT_ARN" ]; then
    echo "Found existing ACM certificate: $CERT_ARN"
  else
    echo "No ACM certificate found for $DOMAIN."
    echo "Please request one in AWS Certificate Manager (us-east-1) and try again."
    exit 1
  fi
  export ACM_CERT_ARN="$CERT_ARN"
}

# ---------------------------------------------------------------------------
# Build the Angular/Nx app
# ---------------------------------------------------------------------------
build_app() {
  echo "Building Angular/Nx app (configuration: $BUILD_CONFIG)..."
  yarn install
  yarn build --configuration "$BUILD_CONFIG"
}

# ---------------------------------------------------------------------------
# Create or verify S3 bucket configured for CloudFront with OAI
# ---------------------------------------------------------------------------
create_s3_bucket() {
  echo "Creating/Configuring S3 bucket: $S3_BUCKET ..."

  if ! aws s3api head-bucket --bucket "$S3_BUCKET" 2>/dev/null; then
    echo "Bucket does not exist — creating..."
    if [ "$AWS_REGION" = "us-east-1" ]; then
      aws s3api create-bucket --bucket "$S3_BUCKET" --region "$AWS_REGION"
    else
      aws s3api create-bucket --bucket "$S3_BUCKET" --region "$AWS_REGION" \
        --create-bucket-configuration LocationConstraint="$AWS_REGION"
    fi
    echo "Bucket $S3_BUCKET created."
  else
    echo "Bucket $S3_BUCKET already exists."
  fi

  # Create or reuse OAI
  load_oai_id
  if [ -z "$OAI_ID" ]; then
    echo "Creating CloudFront Origin Access Identity (OAI)..."
    OAI_ID=$(aws cloudfront create-cloud-front-origin-access-identity \
      --cloud-front-origin-access-identity-config \
        CallerReference="deploy-${TARGET}-$(date +%s)",Comment="OAI for $S3_BUCKET" \
      --query 'CloudFrontOriginAccessIdentity.Id' --output text)
    echo "$OAI_ID" > "$OAI_ID_FILE"
    echo "OAI created: $OAI_ID (saved to $OAI_ID_FILE)"
  fi

  # Block all public access
  aws s3api put-public-access-block \
    --bucket "$S3_BUCKET" \
    --public-access-block-configuration \
      "BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true"

  echo "S3 bucket $S3_BUCKET is configured for CloudFront with OAI $OAI_ID"
}

# ---------------------------------------------------------------------------
# Create CloudFront distribution and Route 53 record (idempotent)
# ---------------------------------------------------------------------------
create_cloudfront_distribution() {
  get_or_request_acm_cert

  load_oai_id
  if [ -z "$OAI_ID" ]; then
    echo "No OAI_ID found. Please run initial-setup first."
    exit 1
  fi

  # ------------------------------------------------------------------
  # Determine whether a distribution already exists for this target.
  # Check the local ID file first; verify the ID is still live in AWS.
  # ------------------------------------------------------------------
  DIST_ID=""
  load_dist_id
  if [ -n "$CLOUDFRONT_DIST_ID" ]; then
    echo "Found saved distribution ID $CLOUDFRONT_DIST_ID — verifying it exists in AWS..."
    if aws cloudfront get-distribution --id "$CLOUDFRONT_DIST_ID" \
         --query 'Distribution.Id' --output text 2>/dev/null | grep -q "$CLOUDFRONT_DIST_ID"; then
      DIST_ID="$CLOUDFRONT_DIST_ID"
      echo "Distribution $DIST_ID already exists — skipping creation."
    else
      echo "Saved distribution ID $CLOUDFRONT_DIST_ID no longer exists in AWS; will create a new one."
    fi
  fi

  if [ -z "$DIST_ID" ]; then
    echo "Creating CloudFront distribution for $DOMAIN_NAME ..."

    cat > cf-dist-config.json <<EOF
{
  "CallerReference": "deploy-${TARGET}-$(date +%s)",
  "Comment": "Distribution for $DOMAIN_NAME",
  "Origins": {
    "Quantity": 1,
    "Items": [
      {
        "Id": "S3Origin",
        "DomainName": "${S3_BUCKET}.s3.amazonaws.com",
        "S3OriginConfig": {
          "OriginAccessIdentity": "origin-access-identity/cloudfront/${OAI_ID}"
        }
      }
    ]
  },
  "DefaultRootObject": "index.html",
  "Aliases": {
    "Quantity": 1,
    "Items": ["$DOMAIN_NAME"]
  },
  "DefaultCacheBehavior": {
    "TargetOriginId": "S3Origin",
    "ViewerProtocolPolicy": "redirect-to-https",
    "AllowedMethods": {
      "Quantity": 2,
      "Items": ["GET", "HEAD"],
      "CachedMethods": { "Quantity": 2, "Items": ["GET", "HEAD"] }
    },
    "ForwardedValues": {
      "QueryString": false,
      "Cookies": { "Forward": "none" }
    },
    "TrustedSigners": { "Enabled": false, "Quantity": 0 },
    "Compress": true,
    "DefaultTTL": 86400,
    "MinTTL": 0,
    "MaxTTL": 31536000
  },
  "CustomErrorResponses": {
    "Quantity": 1,
    "Items": [
      {
        "ErrorCode": 404,
        "ResponsePagePath": "/index.html",
        "ResponseCode": "200",
        "ErrorCachingMinTTL": 300
      }
    ]
  },
  "ViewerCertificate": {
    "ACMCertificateArn": "$ACM_CERT_ARN",
    "SSLSupportMethod": "sni-only",
    "MinimumProtocolVersion": "TLSv1.2_2021"
  },
  "Enabled": true,
  "PriceClass": "PriceClass_100"
}
EOF

    DIST_ID=$(aws cloudfront create-distribution \
      --distribution-config file://cf-dist-config.json \
      --query 'Distribution.Id' --output text)

    rm -f cf-dist-config.json
    echo "$DIST_ID" > "$DIST_ID_FILE"
    echo "CloudFront distribution created: $DIST_ID (saved to $DIST_ID_FILE)"
  fi

  # ------------------------------------------------------------------
  # Apply bucket policy — always re-applied so re-runs are safe
  # ------------------------------------------------------------------
  echo "Applying bucket policy for $S3_BUCKET ..."
  cat > cloudfront-bucket-policy.json <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "AllowCloudFrontOAI",
            "Effect": "Allow",
            "Principal": {
                "AWS": "arn:aws:iam::cloudfront:user/CloudFront Origin Access Identity ${OAI_ID}"
            },
            "Action": "s3:GetObject",
            "Resource": "arn:aws:s3:::${S3_BUCKET}/*"
        }
    ]
}
EOF

  aws s3api put-bucket-policy --bucket "$S3_BUCKET" --policy file://cloudfront-bucket-policy.json
  rm -f cloudfront-bucket-policy.json
  echo "Bucket policy applied."

  # ------------------------------------------------------------------
  # Wait for distribution to reach Deployed state
  # ------------------------------------------------------------------
  STATUS=$(aws cloudfront get-distribution --id "$DIST_ID" \
    --query 'Distribution.Status' --output text)
  if [ "$STATUS" != "Deployed" ]; then
    echo "Waiting for CloudFront distribution to reach Deployed state (15-30 min)..."
    aws cloudfront wait distribution-deployed --id "$DIST_ID"
  fi
  echo "CloudFront distribution $DIST_ID is Deployed."

  # ------------------------------------------------------------------
  # Create/update Route 53 alias — UPSERT is always safe to re-run
  # ------------------------------------------------------------------
  echo "Upserting Route 53 alias record for $DOMAIN_NAME ..."
  ZONE_ID=$(aws route53 list-hosted-zones-by-name --dns-name demandtrans-apis.com \
    --query 'HostedZones[0].Id' --output text | sed 's/\/hostedzone\///')

  if [ -z "$ZONE_ID" ] || [ "$ZONE_ID" = "None" ]; then
    echo "ERROR: Hosted zone for demandtrans-apis.com not found in Route 53."
    echo "Please create it manually, then re-run."
    exit 1
  fi

  CF_DOMAIN=$(aws cloudfront get-distribution --id "$DIST_ID" \
    --query 'Distribution.DomainName' --output text)

  aws route53 change-resource-record-sets \
    --hosted-zone-id "$ZONE_ID" \
    --change-batch '{
        "Changes": [{
            "Action": "UPSERT",
            "ResourceRecordSet": {
                "Name": "'"$DOMAIN_NAME"'",
                "Type": "A",
                "AliasTarget": {
                    "HostedZoneId": "Z2FDTNDATAQYW2",
                    "DNSName": "'"$CF_DOMAIN"'",
                    "EvaluateTargetHealth": false
                }
            }
        }]
    }'

  echo "Route 53 record upserted: $DOMAIN_NAME -> $CF_DOMAIN"
}

# ---------------------------------------------------------------------------
# Sync built files to S3 and invalidate CloudFront cache
# ---------------------------------------------------------------------------
update_site() {
  echo "Updating site content for $DOMAIN_NAME ..."

  build_app

  echo "Syncing $BUILD_DIR to s3://$S3_BUCKET ..."
  aws s3 sync "$BUILD_DIR" "s3://$S3_BUCKET" --delete

  load_dist_id
  if [ -z "$CLOUDFRONT_DIST_ID" ]; then
    echo "No CloudFront Distribution ID found. Please run initial-setup first."
    exit 1
  fi

  echo "Invalidating CloudFront cache..."
  aws cloudfront create-invalidation \
    --distribution-id "$CLOUDFRONT_DIST_ID" \
    --paths "/*"

  echo "Update complete! Changes may take a few minutes to propagate through CloudFront."
}

# ---------------------------------------------------------------------------
# initial-setup: provision everything then deploy
# ---------------------------------------------------------------------------
initial_setup() {
  echo "Starting initial setup for $DOMAIN_NAME ..."
  create_s3_bucket
  create_cloudfront_distribution
  update_site
  echo ""
  echo "Initial setup complete! Your site will be available at https://$DOMAIN_NAME"
  echo "  S3 bucket         : $S3_BUCKET"
  echo "  OAI ID file       : $OAI_ID_FILE"
  echo "  Distribution file : $DIST_ID_FILE"
}

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------
case "${1:-}" in
  "initial-setup")
    initial_setup
    ;;
  "update")
    update_site
    ;;
  *)
    echo "Usage: $0 [--ride-alliance] <command>"
    echo ""
    echo "Commands:"
    echo "  initial-setup   Provision S3, CloudFront, Route 53, then deploy"
    echo "  update          Build, sync to S3, and invalidate CloudFront cache"
    echo ""
    echo "Flags:"
    echo "  --ride-alliance  Target ride-alliance.demandtrans-apis.com"
    echo "                   (default: exchange.demandtrans-apis.com)"
    echo ""
    echo "Examples:"
    echo "  $0 initial-setup                    # provision & deploy exchange"
    echo "  $0 update                           # redeploy exchange"
    echo "  $0 --ride-alliance initial-setup    # provision & deploy ride-alliance"
    echo "  $0 --ride-alliance update           # redeploy ride-alliance"
    exit 1
    ;;
esac
