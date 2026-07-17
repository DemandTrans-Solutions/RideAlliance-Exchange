#!/bin/bash
set -e

# Login to ECR first
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 750446117464.dkr.ecr.us-east-1.amazonaws.com

# Get the commit ID
COMMIT_ID=$(git rev-parse HEAD)

# Set Docker host environment variable to match the one in pom.xml
export DOCKER_HOST=unix:///Users/tvoir/.docker/run/docker.sock

# Explicitly pull the builder image with the correct platform
echo "Pulling the builder image with platform linux/amd64..."
docker pull --platform linux/amd64 paketobuildpacks/builder-jammy-base:latest

# Build and tag with Spring Boot's buildpack support
./mvnw clean package spring-boot:build-image \
  -DskipTests \
  -Dspring-boot.build-image.imageName=750446117464.dkr.ecr.us-east-1.amazonaws.com/clearinghouse:latest

# Also tag with commit ID
docker tag 750446117464.dkr.ecr.us-east-1.amazonaws.com/clearinghouse:latest 750446117464.dkr.ecr.us-east-1.amazonaws.com/clearinghouse:$COMMIT_ID

# Push all tags for the clearinghouse image
docker push 750446117464.dkr.ecr.us-east-1.amazonaws.com/clearinghouse:latest
docker push 750446117464.dkr.ecr.us-east-1.amazonaws.com/clearinghouse:$COMMIT_ID

echo "Successfully pushed images with tags:"
aws ecr list-images --repository-name clearinghouse --region us-east-1