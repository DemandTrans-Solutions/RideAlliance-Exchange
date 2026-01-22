#!/bin/zsh

# Update script for DemandTrans Solutions Demo Website
# Created: May 28, 2023

echo "Starting update of DemandTrans Demo website..."

# Sync HTML and other files
echo "Uploading HTML and other files..."
aws s3 cp dts-mdr-demo.html s3://demandtrans-demos/
aws s3 cp dts-logo.svg s3://demandtrans-demos/
aws s3 cp error.html s3://demandtrans-demos/
aws s3 cp index.html s3://demandtrans-demos/
aws s3 cp README.md s3://demandtrans-demos/
aws s3 cp styles.css s3://demandtrans-demos/
aws s3 cp scripts.js s3://demandtrans-demos/
aws s3 cp favicon.png s3://demandtrans-demos/

# Sync video files with correct content type
echo "Uploading video files..."
aws s3 sync videos/ s3://demandtrans-demos/videos/ --content-type "video/mp4"

# Clear CloudFront cache
echo "Clearing CloudFront cache..."

# Get the CloudFront distribution ID for mdr-demos.demandtrans.com
# Using a more robust query that handles null Aliases.Items
DIST_ID=$(aws cloudfront list-distributions \
    --query "DistributionList.Items[?Aliases.Items && contains(Aliases.Items, 'mdr-demos.demandtrans.com')].Id" \
    --output text)

# If the above fails, try a broader search and filter manually
if [ -z "$DIST_ID" ]; then
    echo "Trying alternative lookup method..."
    # Get all distributions and filter using grep
    DIST_ID=$(aws cloudfront list-distributions \
        --query "DistributionList.Items[*].[Id,Aliases.Items]" \
        --output text | grep -B1 "mdr-demos.demandtrans.com" | head -1 | awk '{print $1}')
fi

if [ -n "$DIST_ID" ]; then
    echo "Found CloudFront distribution: $DIST_ID"
    echo "Creating cache invalidation..."
    
    # Create invalidation for all files
    INVALIDATION_ID=$(aws cloudfront create-invalidation \
        --distribution-id $DIST_ID \
        --paths "/*" \
        --query 'Invalidation.Id' \
        --output text)
    
    if [ -n "$INVALIDATION_ID" ]; then
        echo "Cache invalidation created: $INVALIDATION_ID"
        echo "Cache clearing initiated. It may take 5-15 minutes to complete."
        echo "You can check the status with: aws cloudfront get-invalidation --distribution-id $DIST_ID --id $INVALIDATION_ID"
    else
        echo "Warning: Failed to create cache invalidation"
    fi
else
    echo "Warning: CloudFront distribution for mdr-demos.demandtrans.com not found"
    echo "Skipping cache invalidation..."
fi

echo ""
echo "Website update complete!"
echo "Your website is accessible at:"
echo "  - S3 Direct: http://demandtrans-demos.s3-website-us-east-1.amazonaws.com/"
echo "  - CloudFront: https://mdr-demos.demandtrans.com"
echo ""
if [ -n "$DIST_ID" ] && [ -n "$INVALIDATION_ID" ]; then
    echo "Note: CloudFront cache is being cleared. Changes may take 5-15 minutes to appear."
fi
