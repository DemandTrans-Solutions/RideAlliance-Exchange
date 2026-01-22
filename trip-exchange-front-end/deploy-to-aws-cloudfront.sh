#!/bin/bash

# deploy-to-aws-cloudfront.sh
# Deploy Angular/Nx app to AWS S3 with CloudFront distribution
# Usage: ./deploy-to-aws-cloudfront.sh [initial-setup|update]

set -e

# Ensure nvm is available and switch to Node v20 for the rest of the script.
# This attempts to source nvm from the common install location if it's not already
# available in non-interactive shells.
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

# CONFIGURATION - EDIT THESE VALUES
S3_BUCKET="trip-exchange-demo.demandtrans.com"
DOMAIN_NAME="exchange.demandtrans-apis.com"
AWS_REGION="us-east-1"
BUILD_DIR="dist/"
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)

# Path to persist IDs
OAI_ID_FILE=".oai_id"
DIST_ID_FILE=".cloudfront_dist_id"

# Check for AWS CLI
if ! command -v aws &> /dev/null; then
  echo "AWS CLI not found. Please install it: https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html"
  exit 1
fi

# Helper to load OAI_ID from file if it exists
load_oai_id() {
  if [ -f "$OAI_ID_FILE" ]; then
    OAI_ID=$(cat "$OAI_ID_FILE")
    export OAI_ID
    echo "Loaded OAI_ID from $OAI_ID_FILE: $OAI_ID"
  else
    OAI_ID=""
  fi
}

# Helper to load CloudFront Distribution ID from file if it exists
load_dist_id() {
  if [ -f "$DIST_ID_FILE" ]; then
    CLOUDFRONT_DIST_ID=$(cat "$DIST_ID_FILE")
    export CLOUDFRONT_DIST_ID
    echo "Loaded CloudFront Distribution ID from $DIST_ID_FILE: $CLOUDFRONT_DIST_ID"
  else
    CLOUDFRONT_DIST_ID=""
  fi
}

# Get or request ACM certificate for *.demandtrans.com
get_or_request_acm_cert() {
  DOMAIN="*.demandtrans-apis.com"
  echo "Checking for existing ACM certificate for $DOMAIN ..."
  CERT_ARN=$(aws acm list-certificates --region us-east-1 --query "CertificateSummaryList[?DomainName=='$DOMAIN'].CertificateArn" --output text | awk '{print $1}')

  if [ -n "$CERT_ARN" ]; then
    echo "Found existing ACM certificate: $CERT_ARN"
  else
    echo "No ACM certificate found for $DOMAIN. Please request a certificate in the AWS Console and try again."
    exit 1
  fi
  export ACM_CERT_ARN="$CERT_ARN"
}

# Build the Angular/Nx app
build_app() {
  echo "Building Angular/Nx app..."
  yarn install
  #yarn build
  yarn build --configuration production
}

# Create S3 bucket and configure for CloudFront with OAI
create_s3_bucket() {
  echo "Creating/Configuring S3 bucket $S3_BUCKET..."

  # Create bucket if it doesn't exist
  if ! aws s3api head-bucket --bucket "$S3_BUCKET" 2>/dev/null; then
    if [ "$AWS_REGION" = "us-east-1" ]; then
      aws s3api create-bucket --bucket "$S3_BUCKET" --region "$AWS_REGION"
    else
      aws s3api create-bucket --bucket "$S3_BUCKET" --region "$AWS_REGION" --create-bucket-configuration LocationConstraint="$AWS_REGION"
    fi
  fi

  # Create or load OAI
  load_oai_id
  if [ -z "$OAI_ID" ]; then
    echo "Creating CloudFront Origin Access Identity (OAI)..."
    OAI_ID=$(aws cloudfront create-cloud-front-origin-access-identity \
      --cloud-front-origin-access-identity-config CallerReference="deploy-$(date +%s)",Comment="OAI for $S3_BUCKET" \
      --query 'CloudFrontOriginAccessIdentity.Id' --output text)
    echo "$OAI_ID" > "$OAI_ID_FILE"
    echo "OAI created: $OAI_ID (saved to $OAI_ID_FILE)"
  fi

  # Block public access
  aws s3api put-public-access-block \
    --bucket "$S3_BUCKET" \
    --public-access-block-configuration "BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true"

  echo "S3 bucket $S3_BUCKET is configured for CloudFront with OAI $OAI_ID"
}

# Create CloudFront Distribution
create_cloudfront_distribution() {
  echo "Creating CloudFront distribution..."

  # Get ACM certificate
  get_or_request_acm_cert

  # Load OAI
  load_oai_id
  if [ -z "$OAI_ID" ]; then
    echo "No OAI_ID found. Please run initial-setup first."
    exit 1
  fi

  # Create distribution config
  cat > cf-dist-config.json <<EOF
{
  "CallerReference": "deploy-$(date +%s)",
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

  # Create the distribution
  DIST_ID=$(aws cloudfront create-distribution \
    --distribution-config file://cf-dist-config.json \
    --query 'Distribution.Id' --output text)

  echo "$DIST_ID" > "$DIST_ID_FILE"
  echo "CloudFront distribution created with ID: $DIST_ID (saved to $DIST_ID_FILE)"

  # Clean up
  rm -f cf-dist-config.json

  # Update bucket policy with CloudFront permissions
  echo "Updating bucket policy with CloudFront permissions..."
  cat > cloudfront-bucket-policy.json <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "AllowCloudFrontServicePrincipal",
            "Effect": "Allow",
            "Principal": {
                "Service": "cloudfront.amazonaws.com"
            },
            "Action": "s3:GetObject",
            "Resource": "arn:aws:s3:::${S3_BUCKET}/*",
            "Condition": {
                "StringEquals": {
                    "AWS:SourceArn": "arn:aws:cloudfront::${AWS_ACCOUNT_ID}:distribution/${DIST_ID}"
                }
            }
        },
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

  # Wait for distribution to deploy
  echo "Waiting for CloudFront distribution to deploy (this may take 15-30 minutes)..."
  aws cloudfront wait distribution-deployed --id "$DIST_ID"
  echo "CloudFront distribution is now deployed"

  # Create Route 53 record
  echo "Creating Route 53 record..."
  ZONE_ID=$(aws route53 list-hosted-zones-by-name --dns-name demandtrans-apis.com \
    --query 'HostedZones[0].Id' --output text | sed 's/\/hostedzone\///')

  CF_DOMAIN=$(aws cloudfront get-distribution --id "$DIST_ID" \
    --query 'Distribution.DomainName' --output text)

  aws route53 change-resource-record-sets \
    --hosted-zone-id "$ZONE_ID" \
    --change-batch '{
        "Changes": [{
            "Action": "UPSERT",
            "ResourceRecordSet": {
                "Name": "'$DOMAIN_NAME'",
                "Type": "A",
                "AliasTarget": {
                    "HostedZoneId": "Z2FDTNDATAQYW2",
                    "DNSName": "'$CF_DOMAIN'",
                    "EvaluateTargetHealth": false
                }
            }
        }]
    }'
}

# Update site content and invalidate cache
update_site() {
  echo "Updating site content..."

  # Build the app
  build_app

  # Sync to S3
  echo "Syncing $BUILD_DIR to s3://$S3_BUCKET..."
  aws s3 sync "$BUILD_DIR" "s3://$S3_BUCKET" --delete

  # Load distribution ID
  load_dist_id
  if [ -z "$CLOUDFRONT_DIST_ID" ]; then
    echo "No CloudFront Distribution ID found. Please run initial-setup first."
    exit 1
  fi

  # Invalidate cache
  echo "Invalidating CloudFront cache..."
  aws cloudfront create-invalidation \
    --distribution-id "$CLOUDFRONT_DIST_ID" \
    --paths "/*"

  echo "Update complete! Changes may take a few minutes to propagate through CloudFront."
}

# Initial setup process
initial_setup() {
  echo "Starting initial setup..."
  create_s3_bucket
  create_cloudfront_distribution
  update_site
  echo "Initial setup complete! Your site will be available at https://$DOMAIN_NAME"
}

# Main script logic
case "$1" in
  "initial-setup")
    initial_setup
    ;;
  "update")
    update_site
    ;;
  *)
    echo "Usage: $0 [initial-setup|update]"
    echo "  initial-setup  - Create S3 bucket, CloudFront distribution, and deploy site"
    echo "  update        - Update site content and invalidate CloudFront cache"
    exit 1
    ;;
esac
