import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { ConstantService } from '../../shared/service/constant-service';
import { SharedHttpClientService } from '../../shared/service/shared-http-client.service';
import { LocalStorageService } from '../../shared/service/local-storage.service';

@Injectable({
  providedIn: 'root',
})
export class NewTripTicketReportService {
  private webServiceURL: string;
  private webServiceURLforSave: string;
  private webServiceURLforGet: string;
  private webServiceURLforFilter: string;
  role = this._localStorage.get('Role');
  providerId = this._localStorage.get('providerId');

  constructor(
    private _constantService: ConstantService,
    private _httpClient: SharedHttpClientService,
    private _localStorage: LocalStorageService
  ) {
    this.webServiceURL = this._constantService.WEBSERVICE_URL + 'reports/oldestCreatedDate/';
  }

  public get(id): Observable<any> {
    this.webServiceURLforGet = this._constantService.WEBSERVICE_URL + 'reports/oldestCreatedDate/';
    return this._httpClient
      .get(this.webServiceURLforGet + id)
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }

  public query(): Observable<any> {
    return this._httpClient
      .get(this.webServiceURL)
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }

  public save(requestBody): Observable<any> {
    this.webServiceURLforSave =
      this._constantService.WEBSERVICE_URL + 'reports/currentTicketsReportInit';
    return this._httpClient
      .post(this.webServiceURLforSave, requestBody)
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

  public filter(requestBody): Observable<any> {
    this.webServiceURLforFilter =
      this._constantService.WEBSERVICE_URL + 'reports/currentTicketsReport';
    return this._httpClient
      .post(this.webServiceURLforFilter, requestBody)
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }
}
