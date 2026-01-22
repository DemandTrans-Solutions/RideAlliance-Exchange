import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ConstantService } from '../../shared/service/constant-service';
import { SharedHttpClientService } from '../../shared/service/shared-http-client.service';
import { LocalStorageService } from '../../shared/service/local-storage.service';
import { Fund } from './fund';

@Injectable({
  providedIn: 'root',
})
export class FundSourceService {
  private webServiceURL: string;
  private role = this._localStorage.getUserRoles();

  constructor(
    private _constantService: ConstantService,
    private _httpClient: SharedHttpClientService,
    private _localStorage: LocalStorageService
  ) {
    this.webServiceURL = this._constantService.WEBSERVICE_URL + 'fundingSource/';
  }

  public get(): Observable<Fund[]> {
    console.log('Fetching all fund sources');
    const url = this.webServiceURL + 'list';
    return this._httpClient
      .get(url)
      .pipe(
        map((response: any) => {
          return response;
        }),
        catchError((error: any) => {
          console.error('Error fetching fund sources', error);
          return throwError(error);
        })
      );
  }

  public save(requestBody: Fund): Observable<Fund> {
    return this._httpClient.post(this.webServiceURL, requestBody).pipe(
      map(res => res as Fund),
      catchError(error => throwError(() => error))
    );
  }

  public update(id: number, requestBody: Fund): Observable<Fund> {
    return this._httpClient.put(`${this.webServiceURL}${id}`, requestBody).pipe(
      map(res => res as Fund),
      catchError(error => throwError(() => error))
    );
  }

  public remove(id: number): Observable<any> {
    return this._httpClient.delete(`${this.webServiceURL}${id}`).pipe(
      map(res => res),
      catchError(error => throwError(() => error))
    );
  }

  public statusUpdate(id: number, requestBody: Fund, status: string): Observable<any> {
    return this._httpClient.put(`${this.webServiceURL}${id}${status}`, requestBody).pipe(
      map(res => res),
      catchError(error => throwError(() => error))
    );
  }

  // New method for Angular 18 migration
  public getAllFundSources(): Observable<any> {
    console.log('Fetching all fund sources');
    const url = this.webServiceURL + 'list';
    return this._httpClient
      .get(url)
      .pipe(
        map((response: any) => {
          return response;
        }),
        catchError((error: any) => {
          console.error('Error fetching fund sources', error);
          return throwError(error);
        })
      );
  }

  // Original method maintained for backward compatibility
  public getFundSourcesWithStatus(): Observable<any> {
    return this.getAllFundSources();
  }
}
