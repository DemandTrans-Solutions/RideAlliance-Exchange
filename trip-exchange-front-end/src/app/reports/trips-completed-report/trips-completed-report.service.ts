import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ConstantService } from '../../shared/service/constant-service';
import { SharedHttpClientService } from '../../shared/service/shared-http-client.service';
import { LocalStorageService } from '../../shared/service/local-storage.service';
import { CompletedRange, TripCompletedReport } from './trip-completed-report.model';

@Injectable({
  providedIn: 'root',
})
export class TripCompletedReportService {
  private webServiceURL: string;

  constructor(
    private _constantService: ConstantService,
    private _httpClient: SharedHttpClientService,
    private _localStorage: LocalStorageService
  ) {
    this.webServiceURL = this._constantService.WEBSERVICE_URL + 'reports/completedTripsReport';
  }

  /**
   * Get complted trip tickets based on the provided filter criteria
   * @param requestBody The filter criteria
   * @returns Observable of trip complted reports
   */
  public getCompletedTicketList(
    requestBody: CompletedRange
  ): Observable<TripCompletedReport[]> {
    return this._httpClient.post(this.webServiceURL, requestBody).pipe(
      map(response => response as TripCompletedReport[]),
      catchError(error => {
        console.error('Error fetching completed ticket list:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Get the minimum date for available reports
   * @param providerId The provider ID to get the minimum date for
   * @returns Observable of the minimum date
   */
  public getMinimumDate(providerId: number): Observable<any> {
    const url = `${this._constantService.WEBSERVICE_URL}reports/oldestCreatedDate/${providerId}`;
    return this._httpClient.get(url).pipe(
      map(response => response),
      catchError(error => {
        console.error('Error fetching minimum date:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Get all cancelled trip tickets
   * @returns Observable of all trip complted reports
   */
  public query(): Observable<TripCompletedReport[]> {
    return this._httpClient.get(this.webServiceURL).pipe(
      map(response => response as TripCompletedReport[]),
      catchError(error => {
        console.error('Error querying trip cancellation reports:', error);
        return throwError(() => error);
      })
    );
  }
}
