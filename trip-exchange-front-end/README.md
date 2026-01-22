# Clearinghouse

This project was originally generated with Angular 2.4 and has been upgraded to Angular 18. The upgrade process and details are documented in [upgrade-guide.md](upgrade-guide.md).

## Prerequisites

- Node.js (LTS version recommended)
- npm (comes with Node.js)
- Angular CLI version 18.0.0 or higher

## Getting Started

1. Install dependencies:
```bash
npm install --legacy-peer-deps
```

> **Note:** The `--legacy-peer-deps` flag is required to handle backward compatibility with some dependencies during the Angular 2.4 to Angular 18 upgrade process. This allows npm to bypass peer dependency validation, which is necessary due to some packages having conflicting peer dependency requirements.

2. Choose an environment to run the application:

### Development Environment
```bashf
npm run start:dev
```
- Uses development API endpoints
- Includes source maps for debugging
- Enables hot module replacement
- Available at http://localhost:4200

### Staging Environment
```bash
npm run start:staging
```
- Uses staging API endpoints
- Includes minimal debugging tools
- Mimics production build
- Available at http://localhost:4200

### Production Environment
```bash
npm run start:prod
```
- Uses production API endpoints
- Optimized build
- No debug tools
- Available at http://localhost:4200

## Building the Application

Build the application for different environments:

### Development Build
```bash
npm run build:dev
```

### Staging Build
```bash
npm run build:staging
```

### Production Build
```bash
npm run build:prod
```

Build artifacts will be stored in the `dist/` directory.

## Debugging in VS Code

The project is configured for debugging Angular applications directly within VS Code.

### Quick Start Debugging

1. Start the application in debug mode:
   ```bash
   npm run debug
   ```
   This runs the application in development mode with source maps enabled.

2. Launch VS Code debugger:
   - Press `F5` or click the "Run and Debug" icon in the sidebar
   - Select "Debug Angular in Chrome"
   - VS Code will launch Chrome and attach the debugger

### Setting Breakpoints

- Open any TypeScript file (for example, a component or service)
- Click in the left gutter next to line numbers to add breakpoints
- When execution reaches that line, the debugger will pause execution

### Using Watches

1. While debugging, open the "WATCH" panel in the debug sidebar
2. Click the + button to add expressions to watch
3. Enter expressions to monitor variables, for example:
   - `this.user` (to watch a component property)
   - `localStorage.getItem('auth_token')` (to inspect stored tokens)

### Debugging JWT and Authentication

For JWT token-related issues:
1. Set breakpoints in `token.service.ts` methods
2. Use the TokenService's built-in debug helper:
   - In the debug console, run `this.debug()` when paused in TokenService methods
   - Check the console for detailed token information

### Attaching to Running Chrome

If you prefer to start Chrome yourself:
1. Launch Chrome with remote debugging enabled:
   ```bash
   google-chrome --remote-debugging-port=9222
   ```
2. In VS Code, select the "Attach to Chrome" debug configuration
3. VS Code will connect to the running Chrome instance

### Debug Configurations

Two debug configurations are included:
- **Debug Angular in Chrome**: Launches a new Chrome instance and attaches debugger
- **Attach to Chrome**: Connects to an already running Chrome instance with remote debugging enabled

See the `.vscode/launch.json` file for configuration details.

## Key Features

- Updated to Angular 18 with modern architecture
- Modular design with lazy-loaded features
- Enhanced security with modern authentication
- Improved performance with optimized builds
- PrimeNG integration for UI components
- Comprehensive environment configuration

## Testing

### Unit Tests
```bash
npm run test
```
Executes unit tests via Karma.

### End-to-End Tests
```bash
npm run e2e
```
Executes end-to-end tests via Protractor.

## Project Structure

- `src/app/` - Application source code
- `src/environments/` - Environment-specific configuration
- `src/assets/` - Static assets
- `src/styles/` - Global styles

## Environment Configuration

The application uses three environment configurations:

1. **Development** (`environment.ts`)
   - Local development settings
   - Debug tools enabled
   - Detailed error messages
   - Example: 
     ```typescript
     export const environment = {
       production: false,
       apiUrl: 'http://localhost:8080/api/', // Note: URL must end with "/"
       // ...other settings
     };
     ```

2. **Staging** (`environment.staging.ts`)
   - Staging server API endpoints
   - Limited debug capabilities
   - Production-like settings
   - Example:
     ```typescript
     export const environment = {
       production: false,
       apiUrl: 'https://staging-api.example.com/api/', // Note: URL must end with "/"
       // ...other settings
     };
     ```

3. **Production** (`environment.prod.ts`)
   - Production API endpoints
   - Optimized for performance
   - Minimal debugging
   - Example:
     ```typescript
     export const environment = {
       production: true,
       apiUrl: 'https://api.example.com/api/', // Note: URL must end with "/"
       // ...other settings
     };
     ```

> **IMPORTANT**: The `apiUrl` in all environment files must end with a forward slash (`/`). Missing the trailing slash will cause API requests to fail.

## Shared AES key for login/validateUser

This project uses an AES-GCM shared secret to encrypt the user's email address before posting to the backend `login/validateUser` endpoint. The backend's `SharedEncryptionService` expects a Base64-encoded AES key.

Quick summary:
- Frontend environment key: `environment.loginValidateSharedKey` (Base64 string)
- Backend property: `security.login-validate.sharedKey` (Base64 string)

Local helper script

- A helper script is included at `scripts/generate_shared_key.sh` to generate a Base64 AES key (128/192/256-bit), copy to clipboard and verify decoded length.

Example: generate an AES-256 key and copy it to clipboard (macOS):

```bash
./scripts/generate_shared_key.sh -k 256 -c
```

Where to put the key

- Frontend (development only): temporarily set the key in `src/environments/environment.ts` like:

```ts
export const environment = {
   // ... other fields ...
   loginValidateSharedKey: '<BASE64_KEY_HERE>'
}
```

- Production / CI: do NOT commit the production key into source control. Instead inject the Base64 key at build time from your CI / secret manager. For example set an environment variable in your pipeline and use your build tooling to replace the value in `environment.prod.ts` or provide it to the runtime config used by the app.

- Backend: set the same Base64 key in your Spring Boot config (application.properties or secret store):

```properties
security.login-validate.sharedKey=<BASE64_KEY_HERE>
```

Fallback behavior and testing

- If `loginValidateSharedKey` is empty on either side, the server falls back to treating the encoded value as Base64(plaintext). The frontend mirrors that behavior by sending `btoa(email)` when the key is not present — useful for local testing without shared secrets.

Security notes

- Treat the Base64 key as a secret. Do not commit it to git or place it in public locations.
- Use a secret manager (AWS Secrets Manager, Vault, Azure Key Vault, etc.) or CI-provided secrets to inject the key into the backend and the frontend build.
- Rotate the shared key periodically and update both backend and frontend at the same time to avoid decrypt failures.

## Recent Updates

This project has been upgraded from Angular 2.4 to Angular 18, including

- Modern dependency versions
- Updated coding patterns
- Enhanced type checking
- Improved build system
- Modern component architecture

For detailed information about the upgrade process and changes, please refer to [upgrade-guide.md](upgrade-guide.md).

## Additional Documentation

- [Angular Documentation](https://angular.dev/)
- [PrimeNG Documentation](https://primeng.org/documentation)
- [RxJS Documentation](https://rxjs.dev/guide/overview)
- **Data Management Plan & DCAT-US metadata**
   - [data.json](data.json): DCAT-US v1.1 metadata record for this project, satisfying DOT Recipient Reporting Guidance Part 3 (Data Format and Metadata Standards). It documents the dataset, contact, access level, open non-proprietary formats, and planned preservation in the National Transportation Library.
   - Statement (per guidance): The final data will have a DCAT-US v1.1 (https://resources.data.gov/resources/dcat-us/) .JSON metadata file, the federal standard for data search and discovery to be compliant with the USDOT Public Access Plan. No additional metadata schemas are planned at this time; if domain-specific standards become relevant, they will be added alongside DCAT-US.

## MCP Server Setup

This project uses Model Context Protocol (MCP) servers to enhance the development experience and provide better support for working with large files in the codebase.

### What are MCP Servers?

MCP (Model Context Protocol) servers provide specialized capabilities to VS Code and other tools to improve how they handle code generation, analysis, and editing. This is particularly valuable when working with large files (3000+ lines) like those in the trip-ticket module.

### Required MCP Servers

The project is configured to use the following MCP servers:

1. **Docker-based Servers**
   - **mcp/filesystem**: Handles file system operations with better performance for large files
   - **mcp/git**: Provides Git integration and history operations
   - **mcp/fetch**: Facilitates fetching external resources

2. **Non-Docker Servers**
   - **mcp-server-time**: Handles time-related operations
   - **nx-mcp**: Provides Nx workspace-specific functionality

### Server Requirements

- Docker installed and running for the Docker-based servers
- Python 3.10+ with the mcp_server_time package installed
- Node.js 18+ for the nx-mcp server
- Nx tools installed in the workspace

### Configuration Files

- `/.vscode/settings.json`: Contains MCP server definitions
- `/mcp.json`: Root-level MCP configuration
- `/nx.json`: Nx workspace configuration

### Starting the MCP Servers

To start all MCP servers:

```bash
./start-mcp-servers.sh
```

This script will start all required MCP servers in the background. The output will confirm which servers have been started successfully.

### Verifying MCP Servers

To verify that the MCP servers are running:

```bash
# For Docker-based servers
docker ps | grep mcp

# For the nx-mcp server
cat nx-mcp.log
```

### Troubleshooting

If you encounter issues with the MCP servers:

1. **Docker containers not running**:  
   - Ensure Docker is running
   - Check the Docker container logs: `docker logs <container_id>`

2. **nx-mcp server issues**:  
   - Ensure `nx.json` exists at the workspace root
   - Verify `mcp.json` has the correct workspace path
   - Try running the nx-mcp server directly: `npx nx-mcp@latest . --verbose`

3. **I/O errors in Python-based servers**:  
   - Check Python version compatibility
   - Ensure proper stdin/stdout handling in the start script

### Benefits of MCP Servers

- Improved performance when editing large files
- Better code intelligence for large Angular modules
- Enhanced navigation in complex components
- More reliable refactoring operations
- Reduced editor freezing and crashes when working with large files like trip-ticket.service.ts

### MCP Server Logs

Each server writes logs to their respective log files in the project root:

- `fetch.log`
- `filesystem.log`
- `git.log`
- `mcp-server-time.log`
- `nx-mcp.log`

These logs are useful for troubleshooting if you encounter any issues with the MCP servers.

## Authentication flow (Firebase + backend validation)

This application uses Firebase for user authentication (first factor and MFA) and relies on the backend to perform application-level authorization and session setup.

Frontend files to inspect:

- `src/app/shared/service/firebase-auth.service.ts` — wrapper around the Firebase SDK used for signing in, MFA enrollment, forced MFA verification, and retrieving tokens/idToken.
- `src/app/login/login.component.ts` — orchestrates the sign-in flow (email/password and Google), enforces mandatory MFA, and after Firebase success calls backend validation.
- `src/app/login/login.service.ts` — contains `validateUser(firebaseUser: FirebaseUser)` which encrypts (or base64-encodes) the user email and POSTs `{ encodedEmailAddress }` to `login/validateUser` for backend validation.

High-level flow:

1. User authenticates with Firebase using email/password or Google sign-in.
2. The app enforces MFA: users will enroll if not already enrolled, or verify via SMS if enrolled.
3. After successful Firebase authentication and MFA, the frontend calls `LoginService.validateUser(firebaseUser)`.
4. `validateUser` sends `{ encodedEmailAddress }` to the backend. If `environment.loginValidateSharedKey` is set the email is AES-GCM encrypted (IV(12) || ciphertext || tag, then Base64), otherwise the frontend sends Base64(plaintext) for local-testing compatibility.
5. The backend decrypts or decodes the email and performs authorization. On success it returns session and user metadata (JWT token, user id, authorities, etc.) and sets cookies like `XSRF-TOKEN` used by the frontend.
6. The frontend stores returned tokens and metadata (via `TokenService` and `LocalStorageService`) and proceeds to navigate into the app.

Where data is stored after successful validation:

- JWT token (if provided) -> stored with `TokenService.set(...)`.
- XSRF token -> cookie `XSRF-TOKEN` and local storage key `xsrfToken`.
- User details -> `LocalStorageService` keys such as `userId`, `username`, `name`, `Role`, `providerId`.

Security note:

- The backend is authoritative for authorization and session issuance. Firebase provides identity verification and MFA; the backend must validate and create a session before the app treats the user as authenticated.
