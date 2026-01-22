import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { ConstantService } from '../../shared/service/constant-service';
import { SharedHttpClientService } from '../../shared/service/shared-http-client.service';
import { LocalStorageService } from '../../shared/service/local-storage.service';

@Injectable({
  providedIn: 'root',
})
export class ProviderPartnersService {
  private webServiceURL: string;
  private webServiceURls: any;
  role = this._localStorage.get('Role');

  constructor(
    private _constantService: ConstantService,
    private _httpClient: SharedHttpClientService,
    private _localStorage: LocalStorageService
  ) {
    if (this.role == 'ROLE_PROVIDERADMIN') {
      const url = 'providerPartners/requesterProvider/' + this._localStorage.get('providerId');
      const urls = 'providerPartners/';
      this.webServiceURL = this._constantService.WEBSERVICE_URL + url;
      this.webServiceURls = this._constantService.WEBSERVICE_URL + urls;
    } else if (this.role == 'ROLE_ADMIN') {
      const url =
        'providerPartners/requesterProvider/' + this._localStorage.get('adminChoiceProvider');
      const urls = 'providerPartners/';
      this.webServiceURL = this._constantService.WEBSERVICE_URL + url;
      this.webServiceURls = this._constantService.WEBSERVICE_URL + urls;
    }

  }

  public setUrls(): void {

    if (this.role == 'ROLE_PROVIDERADMIN') {
      const url = 'providerPartners/requesterProvider/' + this._localStorage.get('providerId');
      const urls = 'providerPartners/';
      this.webServiceURL = this._constantService.WEBSERVICE_URL + url;
      this.webServiceURls = this._constantService.WEBSERVICE_URL + urls;
    } else if (this.role == 'ROLE_ADMIN') {
      const url =
        'providerPartners/requesterProvider/' + this._localStorage.get('adminChoiceProvider');
      const urls = 'providerPartners/';
      this.webServiceURL = this._constantService.WEBSERVICE_URL + url;
      this.webServiceURls = this._constantService.WEBSERVICE_URL + urls;
    }
    console.log('Web Service URL:', this.webServiceURL);
    console.log('Web Service URLs:', this.webServiceURls);
  }

  public get(id): Observable<any> {
    this.setUrls();
    return this._httpClient
      .get(this.webServiceURL + id)
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }

  public query(): Observable<any> {
    this.setUrls();
    return this._httpClient
      .get(this.webServiceURL)
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }

  public save(requestBody): Observable<any> {
    this.setUrls();
    return this._httpClient
      .post(this.webServiceURls, requestBody)
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }

  public update(id, requestBody): Observable<any> {
    this.setUrls();
    return this._httpClient
      .put(this.webServiceURls + id, requestBody)
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }

  public remove(id): Observable<any> {
    this.setUrls();
    return this._httpClient
      .delete(this.webServiceURL + id)
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }

  // New method for Angular 18 migration
  public getProvider(providerId: number): Observable<any> {
    this.setUrls();
    const url = this._constantService.WEBSERVICE_URL + 'provider/' + providerId;
    return this._httpClient
      .get(url)
      .pipe(
        map((response: any) => {
          return response;
        }),
        catchError((error: any) => {
          console.error('Error fetching provider', error);
          return [];
        })
      );
  }

  // Original method maintained for backward compatibility
  public getProviderName(providerId: number): Observable<any> {
    return this.getProvider(providerId);
  }
}
