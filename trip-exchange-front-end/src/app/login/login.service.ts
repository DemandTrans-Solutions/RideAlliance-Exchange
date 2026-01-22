import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, from } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { User } from './user';
import { Router } from '@angular/router';
import { ConstantService } from '../shared/service/constant-service';
import { TokenService } from '../shared/service/token.service';
import { Logger } from '../shared/service/default-log.service';
import { SharedHttpClientService } from '../shared/service/shared-http-client.service';
import { LocalStorageService } from '../shared/service/local-storage.service';
import { FirebaseAuthService, FirebaseUser } from '../shared/service/firebase-auth.service';

@Injectable({
  providedIn: 'root',
})
export class LoginService {
  webServiceURL: any;

  constructor(
    private _router: Router,
    private _http: HttpClient,
    private _constantService: ConstantService,
    private _tokenService: TokenService,
    private _logger: Logger,
    private _localStorage: LocalStorageService,
    private _sharedHttpClientService: SharedHttpClientService,
    private _firebaseAuthService: FirebaseAuthService
  ) {}

  private apiUrl = this._constantService.WEBSERVICE_URL + 'login';

  login(user: User): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'text/plain');
    const options = { headers: headers, withCredentials: true };

    return this._http
      .post(
        this.apiUrl,
        {
          username: user.username,
          password: user.password,
        },
        options
      )
      .pipe(
        map(this._constantService.extractData),
        catchError(this._sharedHttpClientService.handleError)
      );
  }

  /**
   * Validate Firebase authenticated user with backend
   * This is the new method that replaces the traditional login flow
   */
  validateUser(firebaseUser: FirebaseUser): Observable<any> {
    const validateUrl = this._constantService.WEBSERVICE_URL + 'login/validateUser';
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    const options = { headers: headers, withCredentials: true };
    this._logger.log('Validating Firebase user with backend:', {
      firebaseUid: firebaseUser.uid,
    });

    // Encrypt the email (or base64-encode when shared key not configured) then POST
    return from(this.encryptEmail(firebaseUser.email)).pipe(
      switchMap((encodedEmail: string) => {
        const requestBody = {
          encodedEmailAddress: encodedEmail,
        };
        return this._http.post(validateUrl, requestBody, options);
      }),
      map(this._constantService.extractData),
      catchError(this._sharedHttpClientService.handleError)
    );
  }

  /**
   * Encrypts an email address so the backend can decrypt it using the
   * SharedEncryptionService.decryptToString logic. If a base64 shared key
   * is not configured in the environment, falls back to Base64(plaintext)
   * to match the server-side fallback behavior.
   *
   * Format (when key present): Base64( IV (12 bytes) || ciphertext || tag )
   */
  private async encryptEmail(email: string): Promise<string> {
    if (!email) {
      throw new Error('email is required for encryption');
    }

    const sharedKeyBase64 = (environment as any).loginValidateSharedKey || '';
    // If no shared key configured, fallback to Base64 of plaintext (server mirrors this behavior)
    if (!sharedKeyBase64) {
      return btoa(email);
    }

    // Helper: convert base64 string to Uint8Array
    const base64ToUint8 = (b64: string) => {
      const binary = atob(b64);
      const len = binary.length;
      const bytes = new Uint8Array(len);
      for (let i = 0; i < len; i++) {
        bytes[i] = binary.charCodeAt(i);
      }
      return bytes;
    };

    // Helper: convert Uint8Array to base64
    const uint8ToBase64 = (u8: Uint8Array) => {
      let binary = '';
      const len = u8.byteLength;
      for (let i = 0; i < len; i++) {
        binary += String.fromCharCode(u8[i]);
      }
      return btoa(binary);
    };

    const keyBytes = base64ToUint8(sharedKeyBase64);

    // Import key for AES-GCM
    const cryptoKey = await crypto.subtle.importKey(
      'raw',
      keyBytes.buffer,
      { name: 'AES-GCM' },
      false,
      ['encrypt']
    );

    // 12-byte IV
    const iv = crypto.getRandomValues(new Uint8Array(12));
    const encoder = new TextEncoder();
    const plain = encoder.encode(email);

    // Web Crypto returns ciphertext||tag
    const cipherBuffer = await crypto.subtle.encrypt(
      { name: 'AES-GCM', iv: iv, tagLength: 128 },
      cryptoKey,
      plain
    );

    const cipherBytes = new Uint8Array(cipherBuffer);
    const combined = new Uint8Array(iv.length + cipherBytes.length);
    combined.set(iv, 0);
    combined.set(cipherBytes, iv.length);

    return uint8ToBase64(combined);
  }

  isLoggedIn(): boolean {
    // Check for the actual token instead of hardcoded true
    const accessKey = this._localStorage.get('userId');
    if (accessKey === undefined || accessKey === null || accessKey === '') {
      return false;
    } else {
      return true;
    }
  }

  logout(): boolean {
    this._logger.log('logout service called');
    this._tokenService.clearAll();
    this._router.navigate(['/login']);
    return true;
  }

  public getProvider(_id: any): Observable<any> {
    this.webServiceURL =
      this._constantService.WEBSERVICE_URL + 'provider/' + this._localStorage.get('providerId');
    return this._sharedHttpClientService.get(this.webServiceURL);
  }
}
