#!/bin/bash
set -e

# Set Docker host environment variable
export DOCKER_HOST=unix:///Users/tvoir/.docker/run/docker.sock

# Login to ECR first
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin dkr.ecr.us-east-1.amazonaws.com

# Get the commit ID
COMMIT_ID=$(git rev-parse HEAD)

# Create a new builder instance with multi-architecture support if it doesn't exist
if ! docker buildx inspect multiarch > /dev/null 2>&1; then
  echo "Creating new Docker buildx builder with multi-architecture support..."
  docker buildx create --name multiarch --driver docker-container --bootstrap --use
else
  docker buildx use multiarch
fi

# Build and push the image directly with buildx
echo "Building and pushing Docker image for linux/amd64 platform..."
docker buildx build \
  --platform linux/amd64 \
  --tag .dkr.ecr.us-east-1.amazonaws.com/clearinghouse:latest \
  --tag .dkr.ecr.us-east-1.amazonaws.com/clearinghouse:$COMMIT_ID \
  --push \
  .

echo "Image successfully built and pushed to ECR"
echo "Latest tag: .dkr.ecr.us-east-1.amazonaws.com/clearinghouse:latest"
echo "Commit tag: .dkr.ecr.us-east-1.amazonaws.com/clearinghouse:$COMMIT_ID" 