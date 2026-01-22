import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { ConstantService } from '../shared/service/constant-service';
import { SharedHttpClientService } from '../shared/service/shared-http-client.service';

@Injectable({
  providedIn: 'root',
})
export class ApplicationSettingService {
  private webServiceURL: string;

  constructor(
    private _constantService: ConstantService,
    private _httpClient: SharedHttpClientService
  ) {
    this.webServiceURL = this._constantService.WEBSERVICE_URL + 'applicationSettings/';
  }

  public get(id): Observable<any> {
    return this._httpClient
      .get(this.webServiceURL + id)
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }

  public query(): Observable<any> {
    return this._httpClient
      .get(this.webServiceURL)
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
}
