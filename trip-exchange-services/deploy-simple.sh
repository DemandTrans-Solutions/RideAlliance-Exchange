#!/bin/bash
set -e

# Build the JAR file locally without using build-image
echo "Building JAR file locally..."
./mvnw clean package -DskipTests -Dspring-boot.build-image.skip=true

# Get the commit ID
COMMIT_ID=$(git rev-parse HEAD)

# Login to ECR
echo "Logging in to ECR..."
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 750446117464.dkr.ecr.us-east-1.amazonaws.com

# Make sure we're using the correct buildx builder
echo "Setting up Docker buildx..."
if ! docker buildx inspect multiarch > /dev/null 2>&1; then
  docker buildx create --name multiarch --driver docker-container --bootstrap --use
else
  docker buildx use multiarch
fi

# Build and push the image with Docker
echo "Building and pushing Docker image for linux/amd64 platform..."
docker buildx build \
  --platform linux/amd64 \
  --tag 750446117464.dkr.ecr.us-east-1.amazonaws.com/clearinghouse:latest \
  --tag 750446117464.dkr.ecr.us-east-1.amazonaws.com/clearinghouse:$COMMIT_ID \
  --push \
  .

echo "Image successfully built and pushed to ECR"
echo "Latest tag: 750446117464.dkr.ecr.us-east-1.amazonaws.com/clearinghouse:latest"
echo "Commit tag: 750446117464.dkr.ecr.us-east-1.amazonaws.com/clearinghouse:$COMMIT_ID" 