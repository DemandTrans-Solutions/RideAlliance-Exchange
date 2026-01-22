# Clearinghouse Server

A Spring Boot application for managing trip exchange and coordination between service providers.

## Table of Contents
- [Project Overview](#project-overview)
- [System Requirements](#system-requirements)
- [Architecture](#architecture)
- [Installation](#installation)
- [Development Setup](#development-setup)
- [Key Dependencies](#key-dependencies)
- [Features](#features)
- [API Endpoints](#api-endpoints)
- [Building and Running](#building-and-running)
- [Docker Support](#docker-support)
- [Configuration](#configuration)
- [Project Structure](#project-structure)
- [Documentation](#documentation)
- [Troubleshooting](#troubleshooting)
- [Deployment](#deployment)
- [License](#license)
- [Contact](#contact)

## Project Overview

- **Group ID**: com.clearingHouse
- **Artifact ID**: clearingHouse
- **Version**: 0.0.1-SNAPSHOT
- **Packaging**: jar

The TripExchange Server facilitates trip exchange and coordination between transportation service providers, enabling efficient resource sharing and improved service delivery.

## System Requirements

- Java 21
- MySQL 8+
- Maven 3.x

## Architecture

The Clearinghouse Server follows a multi-tier architecture:

- **API Layer**: REST controllers handling HTTP requests
- **Service Layer**: Business logic implementation
- **Repository Layer**: Data access using Spring Data JPA
- **Security Layer**: JWT authentication and role-based access control

## Installation

1. Clone the repository
   ```bash
   git clone https://github.com/your-org/clearinghouse_server.git
   cd clearinghouse_server
   ```

2. Configure the database
   ```bash
   # Create MySQL database
   mysql -u root -p
   CREATE DATABASE clearinghouse;
   CREATE USER 'clearinghouse'@'localhost' IDENTIFIED BY 'your_password';
   GRANT ALL PRIVILEGES ON clearinghouse.* TO 'clearinghouse'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. Configure application properties
   - Update database credentials in `src/main/resources/application-local.yaml`

4. Build and run the application (see [Building and Running](#building-and-running))

## Development Setup

1. IDE Setup
   - Import as Maven project in IntelliJ IDEA or Eclipse
   - Enable annotation processing for Lombok

2. Local environment configuration
   - Copy `application-example.yaml` to `application-local.yaml`
   - Customize settings for your development environment

3. Code style
   - The project uses Google Java Style Guide
   - Configure your IDE with the provided code style XML

## Key Dependencies

### Core Spring Boot (v3.4.4)

- spring-boot-starter-web
- spring-boot-starter-security
- spring-boot-starter-data-jpa
- spring-boot-starter-mail
- spring-boot-starter-freemarker
- spring-security-config

### Database

- MySQL Connector/J (v9.2.0)
- HikariCP (v5.0.1)
- Atomikos JTA/JDBC (v5.0.8)

### Development Tools

- Lombok (v1.18.30)
- ModelMapper (v3.2.2)
- Jakarta Validation API (v3.0.2)
- Jakarta Mail (v2.0.1)

### Testing

- JUnit Jupiter (v5.10.0)
- Mockito JUnit Jupiter (v5.5.0)
- Mockito Core (v5.14.2)

### API Documentation

- SpringDoc OpenAPI UI (v2.8.5)

### Utilities

- JTS Core (v1.19.0) - For geospatial operations
- GeoJSON Jackson (v1.0)
- Jackson Databind
- Commons Codec (v1.15)

## Features

- JWT-based authentication
- Role-based access control
- Email notifications with templates
- Geospatial data handling
- REST API with Swagger documentation
- Trip management and coordination
- Provider partnerships

## API Endpoints

The application exposes the following main API endpoints:

- **Authentication**
  - `POST /api/auth/login` - Authenticate user and get JWT token
  - `POST /api/auth/refresh` - Refresh JWT token

- **Providers**
  - `GET /api/providers` - List all providers
  - `GET /api/providers/{id}` - Get provider details
  - `POST /api/providers` - Register new provider

- **Trips**
  - `GET /api/trips` - List available trips
  - `POST /api/trips` - Create new trip
  - `PUT /api/trips/{id}` - Update trip details
  - `DELETE /api/trips/{id}` - Cancel trip

- **Partnerships**
  - `GET /api/partnerships` - List provider partnerships
  - `POST /api/partnerships/request` - Request new partnership
  - `PUT /api/partnerships/{id}/accept` - Accept partnership request

- **MCP (Model Control Protocol)**
  - `POST /api/mcp/invoke` - Invoke a foundation model via AWS Bedrock

For complete API documentation, see the Swagger UI at `/swagger-ui.html`

## Building and Running

### Building the Project

```bash
mvn clean install
```

### Running the Application

```bash
mvn spring-boot:run
```

### Running with Specific Profile

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## Docker Support

The project includes Docker support through Spring Boot's build-image goal:

```bash
mvn spring-boot:build-image
```

The Docker image will be tagged with:

- latest
- git commit ID

Running with Docker:

```bash
docker run -p 8080:8080 --name clearinghouse com.clearinghouse/clearinghouse:latest
```

## Configuration

- Application properties can be configured in:
  - `src/main/resources/application-local.yaml`
  - `src/main/resources/application-qa.yaml`

### Environment Variables

The following environment variables can be used to override configuration:

- `DB_URL` - Database connection URL
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password
- `JWT_SECRET` - Secret key for JWT token signing
- `MAIL_HOST` - SMTP server hostname
- `MAIL_PORT` - SMTP server port
- `MAIL_USERNAME` - SMTP server username
- `MAIL_PASSWORD` - SMTP server password

### AWS Bedrock and MCP Configuration

Add the following to your `application.yaml` (see example in the repo):

```yaml
aws:
  bedrock:
    region: us-east-1
    accessKey: YOUR_AWS_ACCESS_KEY
    secretKey: YOUR_AWS_SECRET_KEY
    allowedModels:
      - modelId: "amazon.titan-text-express-v1"
      - modelId: "anthropic.claude-v2"
    usagePolicy:
      maxRequestsPerMinute: 60
      maxTokensPerRequest: 2048
      logRequests: true
mcp:
  enabled: true
  quota:
    maxRequestsPerMinute: 60
    maxTokensPerRequest: 2048
```

## Project Structure

```
clearinghouse_server/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── clearinghouse/
│   │   │           ├── config/       # Application configuration
│   │   │           ├── controller/   # REST controllers
│   │   │           ├── dto/          # Data transfer objects
│   │   │           ├── entity/       # JPA entities
│   │   │           ├── exception/    # Custom exceptions
│   │   │           ├── repository/   # Data repositories
│   │   │           ├── security/     # Security configuration
│   │   │           ├── service/      # Business services
│   │   │           └── util/         # Utility classes
│   │   └── resources/
│   │       ├── application.yaml      # Common configuration
│   │       ├── application-local.yaml # Local environment config
│   │       └── templates/            # Email templates
│   └── test/                         # Test classes
├── pom.xml                           # Maven configuration
└── README.md                         # This file
```

## Documentation

- Swagger UI: `/swagger-ui.html`
- OpenAPI docs: `/v3/api-docs`

## Troubleshooting

### Common Issues

1. **Database Connection Issues**
   - Verify database credentials in application config
   - Ensure MySQL service is running

2. **JWT Token Issues**
   - Check that JWT_SECRET is properly set
   - Verify token expiration settings

3. **Email Sending Failures**
   - Verify SMTP server settings
   - Check email templates in resources

4. **MCP Server Issues**
   - Ensure AWS credentials are correct
   - Check that the requested model is allowed

### Logs

- Application logs are available in `logs/clearinghouse.log`
- Enable debug logging by setting `logging.level.com.clearinghouse=DEBUG` in your application-local.yaml

## Deployment

The application is deployed to AWS AppRunner using a Docker image built for the AMD64 architecture. This section describes the deployment process.

### Cross-Platform Build Process

Since the development environment uses Apple Silicon (ARM64) but the production environment requires AMD64 architecture, we use a cross-platform build approach:

1. **Local JAR Build**: The application is compiled and packaged locally using Maven
2. **Cross-Platform Docker Build**: Docker buildx is used to create an AMD64-compatible image
3. **ECR Push**: The image is pushed to Amazon ECR
4. **AWS AppRunner**: AppRunner pulls the latest image and deploys it

### Deployment Scripts

The project includes several deployment scripts:

##### `deploy-simple.sh` (Recommended)

This is the recommended deployment script that handles cross-platform builds correctly:

```bash
./deploy-simple.sh
```

This script:
1. Builds the JAR file locally using Maven
2. Uses Docker buildx to create an AMD64-compatible image
3. Tags the image with both 'latest' and the git commit ID
4. Pushes the image to ECR
5. AWS AppRunner automatically deploys the new image

##### `deploy-to-ecr-apprunner.sh` (Alternative)

An alternative deployment method using Spring Boot's built-in image building:

```bash
./deploy-to-ecr-apprunner.sh
```

### Docker Configuration

The Dockerfile is configured to use the AMD64 platform explicitly:

```dockerfile
FROM --platform=linux/amd64 eclipse-temurin:21-jre
WORKDIR /app
COPY target/clearinghouse.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Troubleshooting Deployment

Common deployment issues:

1. **Architecture Mismatch**: If you see "exec format error" in the logs, it indicates an architecture mismatch. Ensure the Docker image is built for linux/amd64.

2. **ECR Authentication**: If you can't push to ECR, ensure your AWS credentials are properly configured:
   ```bash
   aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin .dkr.ecr.us-east-1.amazonaws.com
   ```

3. **Docker Buildx**: If buildx commands fail, ensure Docker Desktop is properly configured for multi-architecture builds:
   ```bash
   docker buildx create --name multiarch --driver docker-container --bootstrap --use
   ```

### Continuous Deployment

AWS AppRunner is configured to automatically deploy new versions when a new image is pushed to the ECR repository with the 'latest' tag.

## MCP Server & AWS Bedrock Integration (Spring AI)

The MCP server integrates with AWS Bedrock using [Spring AI](https://docs.spring.io/spring-ai/reference/), which provides a unified and simplified API for invoking foundation models. This integration allows you to:
- Easily configure Bedrock credentials and models via `application.yaml`.
- Invoke Bedrock models using a REST endpoint with robust error handling and usage controls.
- Leverage Spring AI's abstractions for future extensibility and maintainability.

### Configuration
Add the following to your `application.yaml`:
```yaml
spring:
  ai:
    bedrock:
      region: ${aws.bedrock.region}
      access-key: ${aws.bedrock.accessKey}
      secret-key: ${aws.bedrock.secretKey}
      model: "anthropic.claude-v2" # Default model
```

### MCP Model Invocation Endpoint
- **POST** `/api/mcp/invoke`
- **Description:** Invokes a foundation model via AWS Bedrock using Spring AI.

#### Request Example
```json
{
  "modelId": "anthropic.claude-v2",
  "inputPayload": "{\"prompt\":\"Hello, world!\"}"
}
```

#### Response Example (Success)
```json
{
  "output": "Hello! How can I help you today?",
  "status": "success",
  "error": null
}
```

#### Response Example (Error)
```json
{
  "output": null,
  "status": "error",
  "error": "Model not allowed"
}
```

### Reset Data

```sql
delete from tripticketdistance;
delete from tripticketcomment;
delete from tripresult;
delete from claimanttripticket;
update tripticket set ApprovedTripClaimId = null;
delete from tripclaim;
delete from activity;
delete from tripticket;
```


### Usage Controls & Error Handling
- Only models listed in `aws.bedrock.allowedModels` are permitted.
- Rate limiting and input size quotas are enforced (see `mcp.quota` in config).
- All requests and errors are logged for auditing.
- Standardized error responses for invalid requests, quota violations, and Bedrock errors.

For more details, see the [mcp-server-bedrock-plan.md](.github/plans/mcp-server-bedrock-plan.md).


## Federal Compliance & Data Management

This project complies with U.S. Department of Transportation (USDOT) data management requirements and the DRCOG Data Management Plan. 

### DCAT-US Metadata

The project includes a [data.json](data.json) file that conforms to the **DCAT-US v1.1 (Project Open Data Metadata Schema)** standard, which is the federal requirement for data search and discovery across government data catalogs including [data.gov](https://data.gov) and [transportation.data.gov](https://transportation.data.gov).

**What is DCAT-US?**
- DCAT-US is the federal standard metadata schema for describing datasets and APIs
- Enables automatic discovery and cataloging in government data repositories
- Provides standardized information about data access, licensing, formats, and contacts
- Required for federal grant-funded projects per USDOT Public Access Plan

**What's in the data.json file?**
The metadata file describes:
- Dataset title, description, and keywords for discovery
- Access information including API endpoints and downloadable resources
- Contact information and publisher details
- Licensing terms (Public Domain)
- Distribution formats (REST API, OpenAPI spec, source code, documentation)
- Compliance with federal metadata standards

**Data Preservation:**
All project data will be preserved in the National Transportation Library (NTL), a repository managed by the USDOT, to ensure long-term access and adherence to federal data management standards.

**Learn More:**
- [DCAT-US v1.1 Standard](https://resources.data.gov/resources/dcat-us/)
- [USDOT Public Access Plan](https://www.transportation.gov/public-access)
- [Project Open Data](https://project-open-data.cio.gov/)

## Contact

- **Project Maintainer**: [Todd Voirol](mailto:todd.voirol@demandtrans.com)


Version: 0.0.1-SNAPSHOT (alpha)