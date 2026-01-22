import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { ConstantService } from './constant-service';
import { SharedHttpClientService } from './shared-http-client.service';
import { LocalStorageService } from '../../shared/service/local-storage.service';

@Injectable({
  providedIn: 'root',
})
export class ListService {
  private webServiceURL!: string;

  constructor(
    private _constantService: ConstantService,
    private _httpClient: SharedHttpClientService,
    private _localStorage: LocalStorageService
  ) {}

  public queryProvider(): Observable<any> {
    let webService: string;
    const providerId = this._localStorage.get('providerId');
    const role = this._localStorage.get('Role');

    if (role == 'ROLE_ADMIN') {
      webService = 'list/providers';
    } else {
      // Enhanced defensive check: ensure providerId is not null/undefined/empty/"null"
      if (providerId === null || providerId === undefined || String(providerId).trim() === '' || String(providerId) === 'null') {
        console.warn('queryProvider: providerId missing or invalid ("' + providerId + '"); falling back to list/providers');
        webService = 'list/providers';
      } else {
        webService = 'list/providers/' + providerId;
      }
    }

    this.webServiceURL = this._constantService.WEBSERVICE_URL + webService;
    return this._httpClient
      .get(this.webServiceURL)
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }

  public queryRoles(): Observable<any> {
    this.webServiceURL = this._constantService.WEBSERVICE_URL + 'list/roles';
    return this._httpClient
      .get(this.webServiceURL)
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }

  public queryStatus(): Observable<any> {
    this.webServiceURL = this._constantService.WEBSERVICE_URL + 'list/status';
    return this._httpClient
      .get(this.webServiceURL)
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }

  public queryServiceArea(): Observable<any> {
    this.webServiceURL = this._constantService.WEBSERVICE_URL + 'list/serviceAreas';
    return this._httpClient
      .get(this.webServiceURL)
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }

  public queryProviderPartner(): Observable<any> {
    const role = this._localStorage.get('Role');
    let id: any;

    if (role == 'ROLE_PROVIDERADMIN') {
      id = this._localStorage.get('providerId');
    } else if (role == 'ROLE_ADMIN') {
      id = this._localStorage.get('adminChoiceProvider');
      console.log('adminChoiceProvider: ' + id);
    }
    console.log('queryProviderPartner id: ' + id);

    this.webServiceURL = this._constantService.WEBSERVICE_URL + 'list/providerPartners/' + id;
    return this._httpClient
      .get(this.webServiceURL)
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }

  public queryAddress(addressWord: any): Observable<any> {
    this.webServiceURL = this._constantService.WEBSERVICE_URL + 'list/address/' + addressWord;
    return this._httpClient
      .get(this.webServiceURL)
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }

  public filterList(): Observable<any> {
    const userId = this._localStorage.get('userId');
    this.webServiceURL = this._constantService.WEBSERVICE_URL + 'list/ticketFilters/' + userId;
    console.log('filterList calling endpoint ' + this.webServiceURL);
    return this._httpClient
      .get(this.webServiceURL)
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }

  public queryProviderFilter(): Observable<any> {
    let webService: string;
    const providerId = this._localStorage.get('providerId');
    const role = this._localStorage.get('Role');

    if (role == 'ROLE_ADMIN') {
      webService = 'list/providers';
    } else {
      // Enhanced defensive check: ensure providerId is not null/undefined/empty/"null"
      if (providerId === null || providerId === undefined || String(providerId).trim() === '' || String(providerId) === 'null') {
        console.warn('queryProviderFilter: providerId missing or invalid ("' + providerId + '"); falling back to list/providers');
        webService = 'list/providers';
      } else {
        webService = 'list/claimingProviderPartners/' + providerId;
      }
    }

    this.webServiceURL = this._constantService.WEBSERVICE_URL + webService;
    return this._httpClient
      .get(this.webServiceURL)
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }

  public queryOriginatingProviderFilter(): Observable<any> {
    let webService: string;
    const providerId = this._localStorage.get('providerId');
    const role = this._localStorage.get('Role');

    if (role == 'ROLE_ADMIN') {
      webService = 'list/providers';
    } else {
      // Enhanced defensive check: ensure providerId is not null/undefined/empty/"null"
      if (providerId === null || providerId === undefined || String(providerId).trim() === '' || String(providerId) === 'null') {
        console.warn('queryOriginatingProviderFilter: providerId missing or invalid ("' + providerId + '"); falling back to list/providers');
        webService = 'list/providers';
      } else {
        webService = 'list/originatingProviderPartners/' + providerId;
      }
    }

    this.webServiceURL = this._constantService.WEBSERVICE_URL + webService;
    return this._httpClient
      .get(this.webServiceURL)
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }

  public Provider(providerId: any): Observable<any> {
    const webServices = 'list/claimingProviderPartners/' + providerId;
    this.webServiceURL = this._constantService.WEBSERVICE_URL + webServices;
    return this._httpClient
      .get(this.webServiceURL)
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }

  public originatingProvider(providerId: any): Observable<any> {
    const webService = 'list/originatingProviderPartners/' + providerId;
    this.webServiceURL = this._constantService.WEBSERVICE_URL + webService;
    return this._httpClient
      .get(this.webServiceURL)
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }


}
