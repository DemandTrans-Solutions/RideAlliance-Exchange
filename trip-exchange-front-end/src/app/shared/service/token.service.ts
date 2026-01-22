import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class TokenService {
  private tokenKey = 'auth_token';

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  get(): string | null {
    return this.getToken();
  }

  set(token: string): void {
    localStorage.setItem(this.tokenKey, token);
  }

  remove(): void {
    localStorage.removeItem(this.tokenKey);
  }

  clearAll(): void {
    localStorage.clear();
  }

  /**
   * Debug helper method - can be called while debugging to inspect token parts
   */
  debug() {
    const token = this.get();
    if (!token) {
      console.warn('No token found');
      return null;
    }

    // Split the token into parts
    const parts = token.split('.');
    if (parts.length !== 3) {
      console.error('Token does not have the expected format (header.payload.signature)');
      return null;
    }

    try {
      // Get token parts with proper base64 conversion for each part
      const header = this.decodeBase64UrlPart(parts[0]);
      const payload = this.decodeBase64UrlPart(parts[1]);
      // Don't decode signature, just keep it for reference

      console.log('Token debug info:');
      console.log('Header:', header);
      console.log('Payload:', payload);
      console.log('Original token:', token);
      console.log('Parts:', parts);

      return { header, payload, parts, token };
    } catch (error) {
      console.error('Failed to decode token parts for debugging:', error);
      return null;
    }
  }

  /**
   * Helper method to properly decode base64url encoded JWT parts
   * @param part Base64url encoded string from JWT token
   * @returns Decoded object
   */
  private decodeBase64UrlPart(part: string): any {
    try {
      // Replace base64url characters with standard base64
      const base64 = part.replace(/-/g, '+').replace(/_/g, '/');

      // Add padding if needed
      const paddedBase64 = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');

      // Log each step to help with debugging
      console.log('Original part:', part);
      console.log('After character replacement:', base64);
      console.log('After padding:', paddedBase64);

      // Decode the base64 string
      const decoded = atob(paddedBase64);

      // Parse as JSON
      return JSON.parse(decoded);
    } catch (error) {
      console.error('Error in decodeBase64UrlPart:', error);
      console.log('Failed part:', part);
      throw error;
    }
  }

  getUsername(): string {
    const token = this.get();
    if (!token) return '';

    try {
      // JWT tokens are in the format: header.payload.signature
      const parts = token.split('.');
      if (parts.length !== 3) {
        throw new Error('Invalid token format');
      }

      // The payload is the second part
      const payload = parts[1];

      // Base64 URL decoding (replace chars and add padding)
      const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
      const paddedBase64 = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');

      // Decode the base64 payload
      const decodedPayload = JSON.parse(atob(paddedBase64));

      // Return the username from the decoded payload (might be sub, preferred_username, or email depending on your JWT structure)
      return (
        decodedPayload.sub ||
        decodedPayload.preferred_username ||
        decodedPayload.email ||
        decodedPayload.username ||
        ''
      );
    } catch (error) {
      console.error('Error parsing JWT token:', error);
      return '';
    }
  }
}
