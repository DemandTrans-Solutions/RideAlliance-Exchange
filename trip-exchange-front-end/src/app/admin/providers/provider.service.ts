import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { ConstantService } from '../../shared/service/constant-service';
import { SharedHttpClientService } from '../../shared/service/shared-http-client.service';
import { LocalStorageService } from '../../shared/service/local-storage.service';
import { ensureBreakpointWorks } from '../../shared/debug/debugHelper';

@Injectable({
  providedIn: 'root',
})
export class ProviderService {
  private webServiceURL: string;

  constructor(
    private _constantService: ConstantService,
    private _httpClient: SharedHttpClientService,
    private _localStorage: LocalStorageService
  ) {
    this.webServiceURL = this._constantService.WEBSERVICE_URL + 'provider/';
    this.initializeServiceUrl();
  }

  private initializeServiceUrl(): void {
    const role = this._localStorage.get('Role');
    if (role === 'ROLE_ADMIN') {
      this.webServiceURL = this._constantService.WEBSERVICE_URL + 'provider/';
    } else {
      this.webServiceURL =
        this._constantService.WEBSERVICE_URL + 'provider/' + this._localStorage.get('providerId');
    }
  }

  public get(id: string): Observable<any> {
    // Use the debug helper to ensure breakpoints can bind
    ensureBreakpointWorks('provider.service - get');
    return this._httpClient
      .get<any>(this.webServiceURL)
      .pipe(catchError(this._constantService.handleError));
  }

  public query(): Observable<any> {
    // Use the debug helper to ensure breakpoints can bind
    ensureBreakpointWorks('provider.service - query');
    return this._httpClient
      .get<any>(this.webServiceURL)
      .pipe(catchError(this._constantService.handleError));
  }

  public save(requestBody: any): Observable<any> {
    return this._httpClient
      .post<any>(this.webServiceURL, requestBody)
      .pipe(catchError(this._constantService.handleError));
  }

  public update(id: string, requestBody: any): Observable<any> {
    return this._httpClient
      .put<any>(this.webServiceURL, requestBody)
      .pipe(catchError(this._constantService.handleError));
  }

  public remove(id: string): Observable<any> {
    return this._httpClient
      .delete<any>(this.webServiceURL)
      .pipe(catchError(this._constantService.handleError));
  }

  public statusUpdate(id: string, requestBody: any, status: string): Observable<any> {
    return this._httpClient
      .put<any>(this.webServiceURL + "/" + status, requestBody)
      .pipe(catchError(this._constantService.handleError));
  }
}
