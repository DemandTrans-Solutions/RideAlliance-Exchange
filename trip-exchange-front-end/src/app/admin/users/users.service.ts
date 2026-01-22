import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { ConstantService } from '../../shared/service/constant-service';
import { SharedHttpClientService } from '../../shared/service/shared-http-client.service';
import { LocalStorageService } from '../../shared/service/local-storage.service';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private webServiceURL: string;
  private webServiceURLs: string;
  role = this._localStorage.get('Role');
  providerId = this._localStorage.get('providerId');

  constructor(
    private _constantService: ConstantService,
    private _httpClient: SharedHttpClientService,
    private _localStorage: LocalStorageService
  ) {
    this.webServiceURL = this._constantService.WEBSERVICE_URL + 'users/';
    if (this.role == 'ROLE_ADMIN') {
      this.webServiceURLs = this._constantService.WEBSERVICE_URL + 'users/';
    } else {
      this.webServiceURLs =
        this._constantService.WEBSERVICE_URL + 'users/usersByProviderId/' + this.providerId;
    }
  }

  public get(id): Observable<any> {
    return this._httpClient
      .get(this.webServiceURL + id)
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }

  public query(): Observable<any> {
    return this._httpClient
      .get(this.webServiceURLs)
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }

  public save(requestBody): Observable<any> {
    return this._httpClient
      .post(this.webServiceURL, requestBody)
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }

  public update(id, requestBody): Observable<any> {
    return this._httpClient
      .put(this.webServiceURL + id, requestBody)
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }

  public remove(id): Observable<any> {
    return this._httpClient
      .delete(this.webServiceURL + id)
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }

  public statusUpdate(id, requestBody, status): Observable<any> {
    return this._httpClient
      .put(this.webServiceURL + id + status, requestBody)
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }
}
