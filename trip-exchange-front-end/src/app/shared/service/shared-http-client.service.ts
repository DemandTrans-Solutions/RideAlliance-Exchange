import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { Router } from '@angular/router';
// ...existing code... (ConstantService intentionally unused in this module)
import { Logger } from './default-log.service';
import { TokenService } from './token.service';
import { LocalStorageService } from './local-storage.service';

@Injectable({
  providedIn: 'root',
})
export class SharedHttpClientService {
  constructor(
    private router: Router,
    private http: HttpClient,
    private _logger: Logger,
    private _tokenService: TokenService,
    private _localStorage: LocalStorageService
  ) {}

  private getHttpOptions() {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
    });

    const httpOptions: any = {
      headers: headers,
      withCredentials: true,
    };

    // Get XSRF token from localStorage instead of tokenService
    const xsrfToken = this._localStorage.get('xsrfToken');
    if (xsrfToken) {
      httpOptions.headers = httpOptions.headers.set('X-AUTH-TOKEN', xsrfToken);
    }

    return httpOptions;
  }

  /**
   * Remove trailing slash from URL if present
   * @param url The URL to normalize
   * @returns URL without trailing slash
   */
  private normalizeUrl(url: string): string {
    return url.endsWith('/') ? url.slice(0, -1) : url;
  }

  public post<T>(url: string, requestData: any): Observable<T> {
    const options = this.getHttpOptions();
    const normalizedUrl = this.normalizeUrl(url);
    return this.http.post<T>(normalizedUrl, requestData, options).pipe(
      map((response: any) => response as T),
      catchError(this.handleError)
    );
  }


  public patch<T>(url: string, requestData: any): Observable<T> {
    const options = this.getHttpOptions();
    const normalizedUrl = this.normalizeUrl(url);
    return this.http.patch<T>(normalizedUrl, requestData, options).pipe(
      map((response: any) => response as T),
      catchError(this.handleError)
    );
  }

  public get<T>(url: string): Observable<T> {
    const options = this.getHttpOptions();
    const normalizedUrl = this.normalizeUrl(url);
    return this.http.get<T>(normalizedUrl, options).pipe(
      map((response: any) => response as T),
      catchError(this.handleError)
    );
  }

  public put<T>(url: string, requestData: any): Observable<T> {
    const options = this.getHttpOptions();
    const normalizedUrl = this.normalizeUrl(url);
    return this.http.put<T>(normalizedUrl, requestData, options).pipe(
      map((response: any) => response as T),
      catchError(this.handleError)
    );
  }

  public delete<T>(url: string): Observable<T> {
    const options = this.getHttpOptions();
    const normalizedUrl = this.normalizeUrl(url);
    return this.http.delete<T>(normalizedUrl, options).pipe(
      map((response: any) => response as T),
      catchError(this.handleError)
    );
  }

  public extractData(res: Response) {
    const body = res;
    return body || {};
  }

  public handleError = (error: any) => {
    // Check for authentication errors (401, 403)
    if (error.status === 403 || error.status === 401) {
      console.error('Authentication error:', error.status);

      // Clear any auth-related tokens from localStorage
      this._localStorage.clear('authToken');
      this._localStorage.clear('xsrfToken');

      // Redirect to login page (give calling code a chance to handle first)
      setTimeout(() => {
        this.router.navigate(['/login']);
      }, 100);

      // Re-throw the original error (preserve status and other fields)
      return throwError(() => error);
    }

    // For other errors, return the generic error handling
    const errMsg = error.message
      ? error.message
      : error.status
      ? `${error.status} - ${error.statusText}`
      : 'Server error';
    console.error(errMsg);
    return throwError(() => new Error(errMsg));
  };
}
