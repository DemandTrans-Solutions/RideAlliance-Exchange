import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { Hospitality } from './hospitality.model';
import { ConstantService } from '../../shared/service/constant-service';
import { SharedHttpClientService } from '../../shared/service/shared-http-client.service';
import { LocalStorageService } from '../../shared/service/local-storage.service';

@Injectable({
  providedIn: 'root',
})
export class HospitalityService {
  private webServiceURL: string;
  private webServiceURLGetHospitality: string;
  private webServiceURLSaveService: string;
  private role: string;

  constructor(
    private _constantService: ConstantService,
    private _httpClient: SharedHttpClientService,
    private _localStorage: LocalStorageService
  ) {
    this.role = this._localStorage.get('Role');
    if (this.role === 'ROLE_ADMIN') {
      this.webServiceURL = this._constantService.WEBSERVICE_URL + 'provider/';
    } else {
      this.webServiceURL =this._constantService.WEBSERVICE_URL + 'provider/' + this._localStorage.get('providerId');
    }
    this.webServiceURLGetHospitality = this._constantService.WEBSERVICE_URL + 'servicearea/hospitality';
    this.webServiceURLSaveService =this._constantService.WEBSERVICE_URL + 'servicearea/hospitality';
  }

  /**
   * Get providers
   */
  query(): Observable<any> {
    return this._httpClient
      .get(this.webServiceURL)
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }

  /**
   * Get all hospitality entries
   */
  getAll(): Observable<Hospitality[]> {
    return this._httpClient
      .get(this.webServiceURLGetHospitality)
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }


  /**
   * Update hospitality entry
   */
  update(requestBody: any): Observable<any> {
    return this._httpClient
      .put(this.webServiceURLSaveService, requestBody)
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }

}
