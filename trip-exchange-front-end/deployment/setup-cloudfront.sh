#!/bin/zsh

# Script to create CloudFront distribution and set up Route 53 for MDR Demos
echo "Creating CloudFront distribution for MDR Demos..."

# Get the ACM certificate ARN for *.demandtrans.com (use first matching cert)
CERT_ARN=$(aws acm list-certificates --region us-east-1 | jq -r '.CertificateSummaryList[] | select(.DomainName=="*.demandtrans.com") | .CertificateArn' | head -n 1)

if [ -z "$CERT_ARN" ]; then
    echo "Error: Certificate for *.demandtrans.com not found"
    exit 1
fi

# Create Origin Access Identity
echo "Creating Origin Access Identity..."
OAI_CONFIG=$(cat << EOF
{
    "CallerReference": "mdr-demos-oai-$(date +%s)",
    "Comment": "OAI for MDR Demos"
}
EOF
)

OAI_ID=$(aws cloudfront create-cloud-front-origin-access-identity --cloud-front-origin-access-identity-config "$OAI_CONFIG" \
    --query 'CloudFrontOriginAccessIdentity.Id' --output text)

if [ -z "$OAI_ID" ]; then
    echo "Error: Failed to create Origin Access Identity"
    exit 1
fi

echo "Origin Access Identity created: $OAI_ID"

# Create CloudFront distribution config
echo "Creating CloudFront distribution..."
DIST_CONFIG=$(cat << EOF
{
    "CallerReference": "mdr-demos-$(date +%s)",
    "Comment": "MDR Demos Distribution",
    "Origins": {
        "Quantity": 1,
        "Items": [
            {
                "Id": "S3-demandtrans-demos",
                "DomainName": "demandtrans-demos.s3.amazonaws.com",
                "S3OriginConfig": {
                    "OriginAccessIdentity": "origin-access-identity/cloudfront/$OAI_ID"
                }
            }
        ]
    },
    "DefaultRootObject": "index.html",
    "Aliases": {
        "Quantity": 1,
        "Items": ["mdr-demos.demandtrans.com"]
    },
    "DefaultCacheBehavior": {
        "TargetOriginId": "S3-demandtrans-demos",
        "ViewerProtocolPolicy": "redirect-to-https",
        "AllowedMethods": {
            "Quantity": 2,
            "Items": ["GET", "HEAD"]
        },
        "ForwardedValues": {
            "QueryString": false,
            "Cookies": {
                "Forward": "none"
            }
        },
        "MinTTL": 0,
        "DefaultTTL": 86400,
        "MaxTTL": 31536000,
        "Compress": true
    },
    "Enabled": true,
    "ViewerCertificate": {
        "ACMCertificateArn": "$CERT_ARN",
        "SSLSupportMethod": "sni-only",
        "MinimumProtocolVersion": "TLSv1.2_2021"
    }
}
EOF
)

# Create the distribution
DIST_ID=$(aws cloudfront create-distribution \
    --distribution-config "$DIST_CONFIG" \
    --query 'Distribution.Id' --output text)

if [ -z "$DIST_ID" ]; then
    echo "Error: Failed to create CloudFront distribution"
    exit 1
fi

echo "CloudFront Distribution ID: $DIST_ID"

# Wait for distribution to deploy
echo "Waiting for distribution to deploy (this may take 5-10 minutes)..."
aws cloudfront wait distribution-deployed --id $DIST_ID

# Get the CloudFront domain name
CF_DOMAIN=$(aws cloudfront get-distribution --id $DIST_ID \
    --query 'Distribution.DomainName' --output text)

# Create Route 53 record
echo "Creating Route 53 record..."
ZONE_ID=$(aws route53 list-hosted-zones-by-name --dns-name demandtrans.com \
    --query 'HostedZones[0].Id' --output text | sed 's/\/hostedzone\///')

aws route53 change-resource-record-sets \
    --hosted-zone-id $ZONE_ID \
    --change-batch '{
        "Changes": [{
            "Action": "UPSERT",
            "ResourceRecordSet": {
                "Name": "mdr-demos.demandtrans.com",
                "Type": "A",
                "AliasTarget": {
                    "HostedZoneId": "Z2FDTNDATAQYW2",
                    "DNSName": "'$CF_DOMAIN'",
                    "EvaluateTargetHealth": false
                }
            }
        }]
    }'

# Update S3 bucket policy
echo "Updating S3 bucket policy..."
aws s3api put-bucket-policy --bucket demandtrans-demos \
    --policy "$(cat cloudfront-policy.json | \
    sed "s/\[YOUR-ACCOUNT-ID\]/$(aws sts get-caller-identity --query Account --output text)/" | \
    sed "s/\[DISTRIBUTION-ID\]/$DIST_ID/" | \
    sed "s/\[OAI-ID\]/$OAI_ID/")"

echo "Setup complete! The site will be available at https://mdr-demos.demandtrans.com"
echo "Note: It may take a few minutes for DNS changes to propagate"
