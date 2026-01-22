# Code / Scripts / Supporting Materials

This file provides a high-level inventory of all code and script locations in this repository. The RideAlliance Exchange project consists of two main components: a **front-end Angular application** and a **backend Spring Boot service**.

---

## Repository Structure

This repository is organized into two primary directories:

- **[trip-exchange-front-end/](trip-exchange-front-end/)** — Angular 18 front-end application (upgraded from Angular 2.4)
- **[trip-exchange-services/](trip-exchange-services/)** — Spring Boot 3.4.4 backend service (Java 21)

---

## Front-End: trip-exchange-front-end/

### Documentation

- **[README.md](trip-exchange-front-end/README.md)** — Comprehensive front-end documentation including:
  - Prerequisites (Node.js, npm, Angular CLI 18+)
  - Getting started guide and installation instructions
  - Environment configurations (development, staging, production)
  - Build and deployment instructions
  - Debugging in VS Code with breakpoints and watches
  - MCP (Model Context Protocol) server setup for enhanced development
  - Authentication flow (Firebase + backend validation)
  - Shared AES key configuration for login/validateUser endpoint
  - Testing procedures (unit and e2e)
  - Project structure and environment configuration details
  - Angular 18 upgrade notes and recent updates

### Context Directory

**[context/](trip-exchange-front-end/context/)** — Project documentation, guides, and planning materials:

- **aws-cloudfront-deployment.md** — CloudFront deployment configuration and procedures
- **aws-deployment.md** — AWS deployment strategies and setup
- **CSS_USAGE.md** — CSS architecture, patterns, and styling guidelines
- **DARK_MODE_GLOBAL_SUMMARY.md** — Dark mode implementation and theming documentation
- **DEBUG_GUIDE.md** — Debugging procedures, troubleshooting, and development tools
- **edit-trip-ticket-plan.md** — Planning documentation for trip ticket editing features
- **project-history.md** — Project evolution, major milestones, and version history
- **README.md** — Context directory overview and index
- **TRIP_EXCHANGE_OVERVIEW.md** — High-level project overview and business logic
- **update-guide.md** — Angular upgrade guide and migration notes

### Scripts Directory

**[scripts/](trip-exchange-front-end/scripts/)** — Utility scripts for development and operations:

- **aws-translate-json.js** — Node.js script for translating i18n JSON files using AWS Translate
  - Translates JSON locale files from source language to target languages
  - Preserves existing translations unless `--force` flag is used
  - Usage: `AWS_ACCESS_KEY_ID=... AWS_SECRET_ACCESS_KEY=... node scripts/aws-translate-json.js --source en --dir src/assets/i18n`

- **generate_shared_key.sh** — Shell script to generate Base64-encoded AES keys (128/192/256-bit) for login/validateUser encryption
  - Generates cryptographic keys for shared encryption between frontend and backend
  - Supports clipboard copy (macOS) and file output with secure permissions
  - Usage: `./scripts/generate_shared_key.sh -k 256 -c` (generates AES-256 key and copies to clipboard)

- **trip-exchange-logging-monitoring-diagram.py** — Python script for generating logging and monitoring architecture diagrams
  - Creates visual diagrams of system monitoring architecture

### Source Code Structure (src/)

**[src/](trip-exchange-front-end/src/)** — Main application source code:

#### Core Application Files
- **index.html** — Main HTML entry point
- **main.ts** — Application bootstrap and initialization
- **polyfills.ts** — Browser compatibility polyfills
- **styles.css** — Global stylesheet
- **test.ts** — Test configuration
- **typings.d.ts** — TypeScript type definitions

#### Application Code (src/app/)

The application follows Angular's modular architecture with feature modules:

**Core Modules:**
- **app.component.*** — Root application component (HTML, CSS, TypeScript)
- **app.module.ts** — Main application module with dependency declarations
- **app.routes.ts** — Application routing configuration
- **standalone-config.ts** — Standalone component configuration for Angular 18
- **core/** — Core services, guards, and application-wide functionality
- **shared/** — Shared components, services, directives, and pipes used across features
- **models/** — TypeScript interfaces and data models

**Feature Modules:**
- **login/** — User authentication (Firebase + backend validation)
- **home/** — Main dashboard and landing page
- **trip-ticket/** — Trip ticket management (create, edit, view, search)
- **admin/** — Administrative functions and user management
- **profile/** — User profile management
- **reports/** — Reporting and analytics
- **chatbot/** — AI chatbot integration
- **auth/** — Authentication guards and interceptors
- **forgot-password/** — Password recovery flow
- **reset-password/** — Password reset functionality
- **change-password/** — Password change for authenticated users
- **change-password-after-login/** — Forced password change on first login
- **activate-account/** — Account activation workflow
- **application-settings/** — Application configuration management

#### Assets and Styling
- **assets/** — Static assets (images, icons, templates, i18n files)
- **styles/** — Modular stylesheets and theme files
- **environments/** — Environment-specific configuration files (dev, staging, prod)

#### Type Definitions
- **types/** — Additional TypeScript type definitions for third-party libraries

### Deployment Scripts

The front-end includes comprehensive AWS deployment automation:

#### [deploy-to-aws.sh](trip-exchange-front-end/deploy-to-aws.sh)
Full-featured deployment script with multiple modes:
- **Features:**
  - S3 bucket creation and configuration
  - CloudFront distribution setup with Origin Access Identity (OAI)
  - ACM certificate management for SSL/TLS (*.demandtrans.com)
  - Route53 DNS record creation and management
  - Automated DNS validation for ACM certificates
  - Build, sync, and cache invalidation
- **Usage Modes:**
  - `build` — Build Angular app only
  - `deploy` — Sync to S3 and invalidate CloudFront
  - `create-s3` — Create and configure S3 bucket with OAI
  - `create-cf` — Create CloudFront distribution
  - `create-route53` — Create Route53 DNS records
  - `initial-deploy` — Complete end-to-end setup and deployment
  - `all` — Build, deploy, and invalidate (default)
- **Configuration:** S3 bucket, CloudFront distribution ID, Route53 zone ID

#### [deploy-to-aws-cloudfront.sh](trip-exchange-front-end/deploy-to-aws-cloudfront.sh)
Streamlined CloudFront deployment with NVM integration:
- **Features:**
  - Automatic Node.js version management (switches to Node v20 via nvm)
  - S3 bucket creation with public access blocking
  - CloudFront distribution with custom domain and SSL
  - OAI (Origin Access Identity) creation and management
  - Bucket policy configuration for CloudFront access
  - Custom error responses (404 → index.html for SPA routing)
  - Route53 alias record automation
- **Usage Modes:**
  - `initial-setup` — Full infrastructure provisioning and first deployment
  - `update` — Update content, sync to S3, and invalidate CloudFront cache
- **Configuration:** Domain (exchange.demandtrans-apis.com), S3 bucket, AWS region

---

## Back-End: trip-exchange-services/

### Documentation

- **[README.md](trip-exchange-services/README.md)** — Comprehensive backend documentation including:
  - Project overview and system requirements (Java 21, MySQL 8+, Maven 3.x)
  - Multi-tier architecture (API, Service, Repository, Security layers)
  - Installation and development setup instructions
  - Key dependencies (Spring Boot 3.4.4, MySQL, JWT, OpenAPI)
  - Features (JWT authentication, RBAC, email notifications, geospatial data)
  - API endpoints documentation (auth, providers, trips, partnerships, MCP)
  - Building and running instructions with specific profiles
  - Docker support with Spring Boot build-image
  - Configuration guide (environment variables, AWS Bedrock, MCP)
  - Project structure details
  - Troubleshooting common issues
  - Deployment procedures (AWS AppRunner with ECR)
  - Cross-platform build process (ARM64 → AMD64)
  - MCP Server & AWS Bedrock integration using Spring AI
  - Federal compliance & Data Management (DCAT-US metadata)
  - Database reset scripts

### Source Code Structure (src/)

**[src/main/](trip-exchange-services/src/main/)** — Main application source code:

#### Java Source Code (src/main/java/com/clearinghouse/)

The backend follows Spring Boot's layered architecture:

**Core Application:**
- **ClearingHouseApplication.java** — Spring Boot application entry point

**Architectural Layers:**

- **controller/** — REST API controllers handling HTTP requests
  - Authentication endpoints
  - Provider management
  - Trip ticket operations
  - Partnership coordination
  - MCP (Model Context Protocol) endpoints

- **service/** — Business logic implementation layer
  - User authentication and authorization services
  - Trip management services
  - Email notification services
  - Geospatial services
  - Shared encryption services for login validation

- **dao/** / **repository/** — Data access layer using Spring Data JPA
  - Database query methods
  - Custom repository implementations

- **entity/** — JPA entity classes mapping to database tables
  - User, Provider, Trip, Partnership entities
  - Audit and tracking entities

- **dto/** — Data Transfer Objects for API requests/responses
  - Request/response models
  - Validation annotations

- **config/** / **configuration/** — Application configuration classes
  - Spring Security configuration
  - Database configuration
  - AWS Bedrock configuration
  - CORS and web configuration

- **security/** — Security implementation
  - JWT token generation and validation
  - Authentication filters
  - Password encoding
  - Role-based access control

- **mcp/** — Model Context Protocol integration
  - AWS Bedrock model invocation
  - Spring AI integration
  - Usage controls and error handling

- **exceptions/** / **exceptionentity/** — Custom exception handling
  - Global exception handler
  - Custom exception classes

- **util/** — Utility classes and helper methods
  - Date/time utilities
  - String manipulation
  - Geospatial utilities

**Supporting Packages:**
- **converter/** — Entity-DTO converters
- **enumentity/** — Enumeration types
- **events/** — Application event handling
- **filter/** — Custom servlet filters
- **listener/** — Application event listeners
- **listresponseentity/** — List response wrapper entities
- **support/** — Support utilities
- **tds/** — TDS (Transportation Data Standard) integration
- **vector/** — Vector/geospatial operations
- **web/** — Web layer utilities

#### Resources (src/main/resources/)
- **application.yaml** — Common application configuration
- **application-local.yaml** — Local development configuration
- **application-qa.yaml** — QA environment configuration
- **templates/** — Freemarker email templates

#### Web Assets (src/main/webapp/)
- Static web resources if applicable

#### Test Code (src/test/)
- **JUnit 5** and **Mockito** test cases
- Integration tests
- Unit tests for services and controllers

### Deployment Scripts

The backend includes automated Docker-based deployment to AWS:

#### [deploy-simple.sh](trip-exchange-services/deploy-simple.sh) (Recommended)
Primary deployment script with cross-platform Docker build:
- **Process:**
  1. Builds JAR file locally using Maven (skips Spring Boot image generation)
  2. Gets git commit ID for versioning
  3. Authenticates with AWS ECR
  4. Sets up Docker buildx for multi-architecture builds
  5. Creates builder instance if needed (multiarch/docker-container)
  6. Builds Docker image for linux/amd64 platform (compatible with AWS AppRunner)
  7. Tags image with 'latest' and commit ID
  8. Pushes to ECR repository
- **Key Features:**
  - Cross-platform build (ARM64 dev → AMD64 prod)
  - ECR authentication and multi-tag push
  - Docker buildx for platform-specific builds
- **Usage:** `./deploy-simple.sh`

#### [deploy-docker-direct.sh](trip-exchange-services/deploy-docker-direct.sh)
Alternative deployment with explicit Docker host configuration:
- **Process:**
  - Sets explicit Docker socket path for macOS
  - ECR authentication
  - Creates/uses multiarch buildx builder
  - Platform-specific build (linux/amd64)
  - Multi-tag push (latest + commit ID)
- **Differences:**
  - Includes explicit DOCKER_HOST environment variable
  - Uses absolute path to Docker socket
- **Usage:** `./deploy-docker-direct.sh`
- **Use Case:** When default Docker socket detection fails

**Docker Configuration:**
Both scripts rely on `Dockerfile` configured for linux/amd64 platform with Eclipse Temurin JRE 21.

---

## Root-Level Scripts and Tools

### Utility Scripts

- **[tools/read-xlsx-header.js](tools/read-xlsx-header.js)** — Extracts header rows from Excel files for template validation

### Related Repository Content

- **[data.json](data.json)** — DCAT-US v1.1 metadata record for federal compliance (DOT data management requirements)
- **[trip-exchange-front-end/import-export/](trip-exchange-front-end/import-export/)** — Import/export templates including global-trip-template.xlsx
- **[trip-exchange-services/dbscript/](trip-exchange-services/dbscript/)** — Database initialization and test data generation scripts

---

## Additional Documentation

### Project-Level Documentation
- **[README.md](README.md)** — Main project README with overview and setup
- **[CODEBOOK.md](CODEBOOK.md)** — Data dictionary and code definitions
- **[DATA_DICTIONARY.md](DATA_DICTIONARY.md)** — Comprehensive data structure documentation
- **[LICENSE](LICENSE)** — Project license terms

### API Documentation
- **[trip-exchange-front-end/api-docs.json](trip-exchange-front-end/api-docs.json)** — OpenAPI specification for front-end APIs
- **[trip-exchange-services/api-docs.json](trip-exchange-services/api-docs.json)** — OpenAPI specification for backend REST APIs
- Backend Swagger UI available at: `/swagger-ui.html` when service is running

---
