# API Authentication Guide

This application supports dual authentication methods optimized for different client types:

## Browser Clients (Web Applications)

**Authentication Method:** Cookie-based JWT + CSRF Token

### Flow:
1. **Login:** `POST /api/login` with JSON body
   ```json
   {
     "username": "your_username", 
     "password": "your_password"
   }
   ```

2. **Response:** JWT token set as httpOnly cookie + CSRF token
   ```json
   {
     "id": 73,
     "username": "user@example.com",
     "JWTToken": "eyJhbGciOiJIUzUxMiJ9...",
     "csrfToken": "794e2328-463c-4a41-9d27-4ce255293bb4",
     "authorities": [{"authority": "ROLE_PROVIDERADMIN"}]
   }
   ```

3. **Subsequent Requests:** Automatic cookie handling + `X-AUTH-TOKEN` header
   ```
   GET /api/some-endpoint
   X-AUTH-TOKEN: 794e2328-463c-4a41-9d27-4ce255293bb4
   Cookie: JWT-TOKEN=eyJhbGciOiJIUzUxMiJ9...
   ```

## API Clients (Non-Browser)

**Authentication Method:** HMAC Key Per Request

### Flow:
1. **Optional Login Verification:** `GET /api/login?key=YOUR_HMAC_KEY`
   ```json
   Response:
   {
     "id": 73,
     "username": "flexride@demandtrans.com",
     "name": "FlexRide Admin", 
     "email": "flexride@demandtrans.com",
     "authType": "HMAC",
     "message": "Authentication successful. Use your HMAC key as '?key=YOUR_KEY' parameter for all subsequent API requests.",
     "authorities": [{"authority": "ROLE_PROVIDERADMIN"}]
   }
   ```

2. **All Subsequent Requests:** Use HMAC key as query parameter
   ```
   GET /api/trip_tickets?key=YOUR_HMAC_KEY
   POST /api/trip_tickets?key=YOUR_HMAC_KEY
   PUT /api/trip_tickets/123?key=YOUR_HMAC_KEY
   DELETE /api/trip_tickets/123?key=YOUR_HMAC_KEY
   ```

### Benefits of HMAC-Per-Request:
- ✅ **More Secure:** Each request is independently authenticated
- ✅ **No Token Expiration:** HMAC keys don't expire
- ✅ **No Token Storage:** No need to store or refresh tokens
- ✅ **Stateless:** No session management required

## Authentication Requirements

### For HMAC Authentication:
- User must have `isAuthanticationTypeIsAdapter = true` in database
- User account must be active (`isActive = true`)
- Valid HMAC key required for each request

### For JWT Authentication:
- Standard username/password credentials
- User account must be active
- CSRF protection enabled for security

## Error Responses

### HMAC Authentication Errors:
- **401 Unauthorized:** Invalid HMAC key
- **403 Forbidden:** User not authorized for HMAC authentication
- **503 Service Unavailable:** User account disabled

### JWT Authentication Errors:
- **502 Bad Gateway:** Bad credentials (username/password)
- **504 Gateway Timeout:** Account locked
- **503 Service Unavailable:** User disabled
- **505 HTTP Version Not Supported:** Account expired
- **501 Not Implemented:** Credentials expired

## Implementation Notes

- Both authentication methods can coexist without interference
- Browser clients automatically handle cookies and CSRF tokens
- API clients should use HMAC keys for simplicity and security
- All endpoints support both authentication methods transparently