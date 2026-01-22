#!/bin/bash
# deploy-to-aws.sh
# Deploy Angular/Nx app to AWS S3 and invalidate CloudFront
# Usage: ./deploy-to-aws.sh [build|deploy|all|create-s3|create-cf|create-route53|initial-deploy]

set -e

# CONFIGURATION - EDIT THESE VALUES
S3_BUCKET="s3://trip-exchange-demo.demandtrans.com"
CLOUDFRONT_DIST_ID="E3LB7OLLAWQ9C4"
BUILD_DIR="dist/"
ROUTE53_ZONE_ID="Z1Y3EX40BAJ1XV"
DOMAIN_NAME="exchange-demo.demandtrans.com"

# Check for AWS CLI
if ! command -v aws &> /dev/null; then
  echo "AWS CLI not found. Please install it: https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html"
  exit 1
fi

# Build the Angular/Nx app
build_app() {
  echo "Building Angular/Nx app..."
  yarn install
  yarn build
}

# Deploy to S3
sync_s3() {
  echo "Syncing $BUILD_DIR to $S3_BUCKET ..."
  aws s3 sync "$BUILD_DIR" "$S3_BUCKET" --delete
}

# Invalidate CloudFront
invalidate_cloudfront() {
  if [ "$CLOUDFRONT_DIST_ID" = "YOUR_CLOUDFRONT_DISTRIBUTION_ID" ]; then
    echo "Please set your CloudFront distribution ID in the script."
    exit 1
  fi
  echo "Invalidating CloudFront distribution..."
  aws cloudfront create-invalidation --distribution-id "$CLOUDFRONT_DIST_ID" --paths "/*"
}

# Get or request ACM certificate for *.demandtrans.com
get_or_request_acm_cert() {
  DOMAIN="*.demandtrans.com"
  echo "Checking for existing ACM certificate for $DOMAIN ..."
  # DEBUG: Show all ARNs returned
  aws acm list-certificates --region us-east-1 --query "CertificateSummaryList[?DomainName=='$DOMAIN'].CertificateArn" --output text
  # Only use the first ARN
  CERT_ARN=$(aws acm list-certificates --region us-east-1 --query "CertificateSummaryList[?DomainName=='$DOMAIN'].CertificateArn" --output text | awk '{print $1}')
  echo "DEBUG: Using ACM ARN: $CERT_ARN"
  if [ -n "$CERT_ARN" ]; then
    echo "Found existing ACM certificate: $CERT_ARN"
  else
    echo "No ACM certificate found for $DOMAIN. Requesting new certificate..."
    CERT_ARN=$(aws acm request-certificate --domain-name "$DOMAIN" --validation-method DNS --region us-east-1 --query CertificateArn --output text)
    echo "Requested new ACM certificate: $CERT_ARN"
    echo "Automating DNS validation in Route53..."
    sleep 5
    VALIDATION_JSON=$(aws acm describe-certificate --certificate-arn "$CERT_ARN" --region us-east-1 --query 'Certificate.DomainValidationOptions[0].ResourceRecord' --output json)
    DNS_NAME=$(echo $VALIDATION_JSON | grep -o '"Name": *"[^"]*"' | head -1 | cut -d '"' -f4)
    DNS_VALUE=$(echo $VALIDATION_JSON | grep -o '"Value": *"[^"]*"' | head -1 | cut -d '"' -f4)
    if [ -z "$ROUTE53_ZONE_ID" ]; then
      echo "ROUTE53_ZONE_ID is not set. Cannot automate DNS validation."
      exit 1
    fi
    cat > acm-validation.json <<EOF
{
  "Comment": "ACM DNS validation for $DOMAIN",
  "Changes": [
    {
      "Action": "UPSERT",
      "ResourceRecordSet": {
        "Name": "$DNS_NAME",
        "Type": "CNAME",
        "TTL": 300,
        "ResourceRecords": [{"Value": "$DNS_VALUE"}]
      }
    }
  ]
}
EOF
    aws route53 change-resource-record-sets --hosted-zone-id "$ROUTE53_ZONE_ID" --change-batch file://acm-validation.json
    rm -f acm-validation.json
    echo "Waiting for ACM certificate to be issued..."
    for i in {1..30}; do
      STATUS=$(aws acm describe-certificate --certificate-arn "$CERT_ARN" --region us-east-1 --query 'Certificate.Status' --output text)
      echo "ACM certificate status: $STATUS"
      if [ "$STATUS" = "ISSUED" ]; then
        echo "Certificate issued!"
        break
      fi
      sleep 10
    done
    if [ "$STATUS" != "ISSUED" ]; then
      echo "Certificate not issued after waiting. Please check ACM console."
      exit 1
    fi
  fi
  export ACM_CERT_ARN="$CERT_ARN"
}

# Path to persist the OAI ID
OAI_ID_FILE=".oai_id"

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

# Create S3 bucket and configure for CloudFront with SSL
create_s3_bucket() {
  BUCKET_NAME=$(echo "$S3_BUCKET" | sed 's|s3://||')
  AWS_REGION="us-east-1" # Set your region here or make configurable
  echo "Checking if S3 bucket $BUCKET_NAME exists..."
  if aws s3api head-bucket --bucket "$BUCKET_NAME" 2>/dev/null; then
    echo "Bucket $BUCKET_NAME already exists."
  else
    echo "Creating S3 bucket $BUCKET_NAME ..."
    if [ "$AWS_REGION" = "us-east-1" ]; then
      aws s3api create-bucket --bucket "$BUCKET_NAME" --region "$AWS_REGION"
    else
      aws s3api create-bucket --bucket "$BUCKET_NAME" --region "$AWS_REGION" --create-bucket-configuration LocationConstraint="$AWS_REGION"
    fi
  fi

  echo "Enabling static website hosting on $BUCKET_NAME ..."
  aws s3 website s3://$BUCKET_NAME/ --index-document index.html --error-document index.html

  # Create or load OAI
  load_oai_id
  if [ -z "$OAI_ID" ]; then
    echo "Creating CloudFront Origin Access Identity (OAI)..."
    OAI_ID=$(aws cloudfront create-cloud-front-origin-access-identity --cloud-front-origin-access-identity-config CallerReference="deploy-$(date +%s)",Comment="OAI for $BUCKET_NAME" | grep -o '"Id": *"[^"]*"' | head -1 | cut -d '"' -f4)
    echo "$OAI_ID" > "$OAI_ID_FILE"
    echo "OAI created: $OAI_ID (saved to $OAI_ID_FILE)"
  else
    echo "Using existing OAI: $OAI_ID"
  fi
  OAI_S3_CANONICAL_USER_ID=$(aws cloudfront get-cloud-front-origin-access-identity --id "$OAI_ID" --query 'CloudFrontOriginAccessIdentity.S3CanonicalUserId' --output text)

  echo "Setting bucket policy for OAI..."
  cat > s3-policy.json <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": { "CanonicalUser": "$OAI_S3_CANONICAL_USER_ID" },
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::$BUCKET_NAME/*"
    }
  ]
}
EOF
  aws s3api put-bucket-policy --bucket "$BUCKET_NAME" --policy file://s3-policy.json
  rm -f s3-policy.json

  echo "S3 bucket $BUCKET_NAME is ready for CloudFront with OAI $OAI_ID."
  echo "Use this OAI ID when creating your CloudFront distribution."
  echo "DEBUG: OAI_ID used for S3 policy: $OAI_ID"
}

# Create CloudFront Distribution
# Ensure the correct S3 website endpoint is used for CloudFront origin
create_cloudfront_distribution() {
  echo "Creating a new CloudFront distribution..."
  if [ -z "$S3_BUCKET" ]; then
    echo "S3_BUCKET is not set. Please set it in the script."
    exit 1
  fi
  # Remove s3:// prefix for origin domain name
  BUCKET_NAME=$(echo "$S3_BUCKET" | sed 's|s3://||')
  AWS_REGION=$(aws s3api get-bucket-location --bucket "$BUCKET_NAME" --query "LocationConstraint" --output text)
  if [ "$AWS_REGION" = "None" ]; then
    AWS_REGION="us-east-1"
  fi
  # Use REST endpoint for OAI best practice
  ORIGIN_DOMAIN="${BUCKET_NAME}.s3.amazonaws.com"
  echo "DEBUG: Using ORIGIN_DOMAIN: $ORIGIN_DOMAIN (REST endpoint for OAI)"
  # Get or request ACM cert
  get_or_request_acm_cert
  if [ -z "$ACM_CERT_ARN" ]; then
    echo "No ACM certificate ARN found. Cannot proceed."
    exit 1
  fi
  # Load OAI_ID from file
  load_oai_id
  if [ -z "$OAI_ID" ]; then
    echo "No OAI_ID found. Please run './deploy-to-aws.sh create-s3' first."
    exit 1
  fi
  # Create distribution config JSON
  cat > cf-dist-config.json <<EOF
{
  "CallerReference": "deploy-$(date +%s)",
  "Comment": "Created by deploy-to-aws.sh",
  "Enabled": true,
  "Origins": {
    "Quantity": 1,
    "Items": [
      {
        "Id": "S3Origin",
        "DomainName": "${ORIGIN_DOMAIN}",
        "S3OriginConfig": { "OriginAccessIdentity": "origin-access-identity/cloudfront/${OAI_ID}" }
      }
    ]
  },
  "DefaultCacheBehavior": {
    "TargetOriginId": "S3Origin",
    "ViewerProtocolPolicy": "redirect-to-https",
    "AllowedMethods": { "Quantity": 2, "Items": ["GET", "HEAD"], "CachedMethods": { "Quantity": 2, "Items": ["GET", "HEAD"] } },
    "ForwardedValues": { "QueryString": false, "Cookies": { "Forward": "none" } },
    "TrustedSigners": { "Enabled": false, "Quantity": 0 },
    "Compress": true,
    "DefaultTTL": 86400,
    "MinTTL": 0
  },
  "ViewerCertificate": {
    "ACMCertificateArn": "$ACM_CERT_ARN",
    "SSLSupportMethod": "sni-only",
    "MinimumProtocolVersion": "TLSv1.2_2021",
    "Certificate": "$ACM_CERT_ARN",
    "CertificateSource": "acm"
  },
  "PriceClass": "PriceClass_100"
}
EOF
  # Create the distribution
  aws cloudfront create-distribution --distribution-config file://cf-dist-config.json > cf-dist-output.json
  DIST_ID=$(cat cf-dist-output.json | grep -o '"Id": *"[^"]*"' | head -1 | cut -d '"' -f4)
  CLOUDFRONT_DIST_ID="$DIST_ID"
  export CLOUDFRONT_DIST_ID
  echo "CloudFront distribution created with ID: $DIST_ID"
  echo "Set CLOUDFRONT_DIST_ID=\"$DIST_ID\" in this script for future deployments."
  rm -f cf-dist-config.json cf-dist-output.json
  # Wait for distribution to be deployed
  echo "Waiting for CloudFront distribution $DIST_ID to be deployed..."
  aws cloudfront wait distribution-deployed --id "$DIST_ID"
  echo "CloudFront distribution $DIST_ID is now deployed."
}

# Create Route53 DNS record for CloudFront
create_route53_record() {
  if [ -z "$ROUTE53_ZONE_ID" ] || [ -z "$DOMAIN_NAME" ]; then
    echo "Please set ROUTE53_ZONE_ID and DOMAIN_NAME in the script."
    exit 1
  fi
  if [ -z "$CLOUDFRONT_DIST_ID" ] || [ "$CLOUDFRONT_DIST_ID" = "YOUR_CLOUDFRONT_DISTRIBUTION_ID" ]; then
    echo "Please set CLOUDFRONT_DIST_ID in the script."
    exit 1
  fi
  echo "Fetching CloudFront domain name..."
  CF_DOMAIN=$(aws cloudfront get-distribution --id "$CLOUDFRONT_DIST_ID" --query 'Distribution.DomainName' --output text)
  if [ -z "$CF_DOMAIN" ]; then
    echo "Could not fetch CloudFront domain name."
    exit 1
  fi
  # Check if the Route53 record already exists
  echo "Checking if Route53 record for $DOMAIN_NAME exists..."
  EXISTING_RECORD=$(aws route53 list-resource-record-sets --hosted-zone-id "$ROUTE53_ZONE_ID" --query "ResourceRecordSets[?Name == '$DOMAIN_NAME.']" --output json)
  if echo "$EXISTING_RECORD" | grep -q 'AliasTarget'; then
    echo "Route53 record for $DOMAIN_NAME already exists. Skipping creation."
    return
  fi
  echo "Creating Route53 record for $DOMAIN_NAME -> $CF_DOMAIN ..."
  cat > route53-change-batch.json <<EOF
{
  "Comment": "Create alias for CloudFront",
  "Changes": [
    {
      "Action": "UPSERT",
      "ResourceRecordSet": {
        "Name": "$DOMAIN_NAME",
        "Type": "A",
        "AliasTarget": {
          "HostedZoneId": "Z2FDTNDATAQYW2",
          "DNSName": "$CF_DOMAIN",
          "EvaluateTargetHealth": false
        }
      }
    }
  ]
}
EOF
  aws route53 change-resource-record-sets --hosted-zone-id "$ROUTE53_ZONE_ID" --change-batch file://route53-change-batch.json
  echo "Route53 record created."
  rm -f route53-change-batch.json
}

# Main
case "$1" in
  build)
    build_app
    ;;
  deploy)
    sync_s3
    invalidate_cloudfront
    ;;
  create-s3)
    create_s3_bucket
    ;;
  create-cf)
    create_cloudfront_distribution
    ;;
  create-route53)
    create_route53_record
    ;;
  initial-deploy)
    create_s3_bucket
    create_cloudfront_distribution
    create_route53_record
    build_app
    sync_s3
    invalidate_cloudfront
    ;;
  all|"")
    build_app
    sync_s3
    invalidate_cloudfront
    ;;
  *)
    echo "Usage: $0 [build|deploy|all|create-s3|create-cf|create-route53|initial-deploy]"
    exit 1
    ;;
esac

echo "Done."
