import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { ConstantService } from '../../shared/service/constant-service';
import { SharedHttpClientService } from '../../shared/service/shared-http-client.service';

@Injectable({
  providedIn: 'root',
})
export class ServiceAreaService {
  private webServiceURL: string;

  constructor(
    private _constantService: ConstantService,
    private _httpClient: SharedHttpClientService
  ) {
    this.webServiceURL = this._constantService.WEBSERVICE_URL + 'servicearea';
  }

  public get(id: string | number): Observable<any> {
    const webServiceURLs = this.webServiceURL + '/providerIdWiseServicearea/';
    console.log('API URL:', webServiceURLs + id);

    return this._httpClient.get(webServiceURLs + id).pipe(
      map(response => {
        console.log('Service area API response:', response);
        return this._constantService.extractData(response);
      }),
      catchError(error => {
        console.error('Error fetching service areas:', error);
        return this._constantService.handleError(error);
      })
    );
  }

  public query(): Observable<any> {
    return this._httpClient
      .get(this.webServiceURL)
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }

  public save(requestBody: any): Observable<any> {
    return this._httpClient
      .post(this.webServiceURL, requestBody)
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }

  public update(requestBody: any): Observable<any> {
    const id = requestBody.serviceId;
    //console.log('Service update called with ID:', id);
    //console.log('Update URL:', this.webServiceURL + '/' + id);
    //console.log('Update request body:', JSON.stringify(requestBody, null, 2));

    return this._httpClient.put(this.webServiceURL + '/' + id, requestBody).pipe(
      map(response => {
        console.log('Update successful, response:', response);
        return this._constantService.extractData(response);
      }),
      catchError(error => {
        console.error('Error in update service area:', error);
        return this._constantService.handleError(error);
      })
    );
  }

  public remove(id: string | number): Observable<any> {
    return this._httpClient
      .delete(this.webServiceURL + id)
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }

  public checkServiceArea(requestBody: any): Observable<any> {
    const webService = this._constantService.WEBSERVICE_URL + 'servicearea/isLastActiveServicearea';
    return this._httpClient
      .post(webService, requestBody)
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }
}
