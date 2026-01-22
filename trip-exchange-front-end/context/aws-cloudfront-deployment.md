# AWS CloudFront Deployment Documentation

This document explains how to use the `deploy-to-aws-cloudfront.sh` script to deploy the Angular application to AWS S3 with CloudFront distribution.

## Prerequisites

1. AWS CLI installed and configured with appropriate credentials
2. A valid ACM certificate for `*.demandtrans.com` in the `us-east-1` region
3. Yarn package manager installed
4. Bash shell environment

## Script Overview

The script `deploy-to-aws-cloudfront.sh` provides two main commands:

1. `initial-setup`: Creates and configures all necessary AWS resources
2. `update`: Updates the site content and invalidates the CloudFront cache

## Configuration

At the top of the script, you'll find the following configuration variables that can be modified:

```bash
S3_BUCKET="trip-exchange-demos.demandtrans.com"
DOMAIN_NAME="trip-exchange-demos.demandtrans.com"
AWS_REGION="us-east-1"
BUILD_DIR="dist/"
```

- `S3_BUCKET`: The name of the S3 bucket to create/use
- `DOMAIN_NAME`: The domain name where the site will be accessible
- `AWS_REGION`: The AWS region to create resources in
- `BUILD_DIR`: The directory containing the built Angular application

## Usage

### Initial Setup

To perform the initial setup, run:

```bash
./deploy-to-aws-cloudfront.sh initial-setup
```

This command will:
1. Create an S3 bucket if it doesn't exist
2. Configure the bucket for CloudFront access using Origin Access Identity (OAI)
3. Create a CloudFront distribution with best practices:
   - HTTPS only (redirect HTTP to HTTPS)
   - SNI-enabled SSL
   - Proper cache settings
   - SPA support (404 -> index.html)
4. Build and deploy the initial version of the site

### Updating the Site

To update the site content, run:

```bash
./deploy-to-aws-cloudfront.sh update
```

This command will:
1. Build the Angular application
2. Sync the new build to S3
3. Invalidate the CloudFront cache

## Security Features

The script implements several security best practices:

1. Uses Origin Access Identity (OAI) to restrict S3 bucket access
2. Blocks all public access to the S3 bucket
3. Forces HTTPS using TLS 1.2 or higher
4. Uses SNI for SSL certificate handling
5. Restricts allowed HTTP methods to GET and HEAD only

## File Storage

The script maintains two local files to store important IDs:

- `.oai_id`: Stores the Origin Access Identity ID
- `.cloudfront_dist_id`: Stores the CloudFront distribution ID

These files are created during the initial setup and are used for subsequent updates.

## Troubleshooting

1. If you see "Access Denied" errors:
   - Verify the S3 bucket policy is correctly set
   - Check that the OAI is properly configured
   - Ensure CloudFront distribution is fully deployed

2. If SSL certificate issues occur:
   - Verify the ACM certificate exists in the us-east-1 region
   - Check that the certificate covers *.demandtrans.com
   - Ensure the certificate is validated and active

3. If updates aren't visible:
   - Wait a few minutes for CloudFront cache invalidation to complete
   - Verify the files were successfully uploaded to S3
   - Check the CloudFront distribution status

## Best Practices

1. Always run `initial-setup` when deploying to a new environment
2. Keep the configuration variables at the top of the script updated
3. Don't delete the `.oai_id` and `.cloudfront_dist_id` files
4. Use the `update` command for all content updates
5. Monitor AWS CloudWatch for any distribution errors

## Notes

- Initial setup can take 15-30 minutes due to CloudFront distribution deployment
- Cache invalidation typically takes 5-15 minutes to fully propagate
- The script assumes the Angular application is built to the `dist/` directory 
