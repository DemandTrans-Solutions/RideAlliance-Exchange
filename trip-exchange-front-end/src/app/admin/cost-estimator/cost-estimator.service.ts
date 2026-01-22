import { Injectable } from '@angular/core';
import { Observable, catchError, map, throwError } from 'rxjs';
import { HttpClient } from '@angular/common/http';

import { ConstantService } from '../../shared/service/constant-service';
import { LocalStorageService } from '../../shared/service/local-storage.service';
import { Cost, CostResponse } from './cost.model';

@Injectable({
  providedIn: 'root',
})
export class CostEstimatorService {
  private webServiceURL: string;
  private role: string;

  constructor(
    private constantService: ConstantService,
    private http: HttpClient,
    private localStorage: LocalStorageService
  ) {
    this.webServiceURL = this.constantService.WEBSERVICE_URL + 'providercost/';
    this.role = this.localStorage.get('Role');
  }

  /**
   * Gets cost estimation data for a provider
   * @param id Provider ID
   * @returns Observable with provider cost data
   */
  public getCostByProviderId(id: string | number): Observable<CostResponse> {
    return this.http.get<CostResponse>(`${this.webServiceURL}${id}`).pipe(
      map(response => response as CostResponse),
      catchError(error => {
        console.error('Error fetching cost data:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Saves or updates provider cost data
   * @param costData Cost data to save
   * @returns Observable with saved provider cost data
   */
  public saveCostEstimation(costData: Cost): Observable<CostResponse> {
    return this.http
      .post<CostResponse>(`${this.webServiceURL}createUpdateProviderCost`, costData)
      .pipe(
        map(response => response as CostResponse),
        catchError(error => {
          console.error('Error saving cost data:', error);
          return throwError(() => error);
        })
      );
  }
}
