# AWS Deployment Task

## Background

Review [project-history.md](../project-history.md) for the full background of this application.

## TODO

I would like to deploy this application to AWS in S3 with a Cloudfront distribution that serves content from http://trip-exhchange-demo.demandtrans.com. This document should be used to track progress.

- 1 Review the Application and all project dependencies
- 2 Check the required AWS CLI libraries are installed. This is MacOS running on Apple Silicon
- 3 Create a .sh file that will perform the deployment. This script should also have a command to quickly deploy code changes with a single command
- 4 Test that the script has no errors and works on either MacOS or Linux
- 5 Run the script

## Deployment Script Usage

### Script Functions and Commands

The `deploy-to-aws.sh` script provides the following commands:

- `create-s3` — Creates and configures the S3 bucket for static website hosting, sets up a CloudFront Origin Access Identity (OAI), and applies the correct bucket policy for secure access.
- `create-cf` — Creates a CloudFront distribution for the S3 bucket. This command will automatically:
  - Check for an existing ACM SSL certificate for `*.demandtrans.com` (in us-east-1), request one if not found, automate DNS validation in Route53, and wait for issuance.
  - Use the ACM certificate for SSL in CloudFront.
- `create-route53` — Creates or updates a Route53 DNS record (A/ALIAS) to point your custom domain (e.g., trip-exhchange-demo.demandtrans.com) to the CloudFront distribution.
- `build` — Installs dependencies and builds the Angular/Nx app.
- `deploy` — Syncs the built app to S3 and invalidates the CloudFront cache.
- `all` (or no argument) — Runs `build`, `deploy`, and invalidates CloudFront in sequence.
- `initial-deploy` - Performs the entire initial deployment in one step. This command will automatically:
  - Create and configure the S3 bucket
  - Create the CloudFront distribution (with SSL, ACM certificate, and automated DNS validation)
  - Create the Route53 DNS record
  - Build and deploy the app to S3
  - Invalidate the CloudFront cache

### First-Time Deployment Flow

To perform the entire initial deployment in one step, run:

```sh
./deploy-to-aws.sh initial-deploy
```

This will:
1. Create and configure the S3 bucket
2. Create the CloudFront distribution (with SSL, ACM certificate, and automated DNS validation)
3. Create the Route53 DNS record
4. Build and deploy the app to S3
5. Invalidate the CloudFront cache

---

If you prefer to run each step manually, you can still use the individual commands as described above.

### Deploying Updates (After Initial Setup)

1. **Build and deploy the app, and invalidate CloudFront**:

   ```sh
   ./deploy-to-aws.sh all
   ```

   - Or, to only deploy and invalidate (without rebuilding):

   ```sh
   ./deploy-to-aws.sh deploy
   ```

---

Refer to the script comments for more details on each function.
