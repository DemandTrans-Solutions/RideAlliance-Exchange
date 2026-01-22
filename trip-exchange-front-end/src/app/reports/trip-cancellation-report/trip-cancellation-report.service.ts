import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ConstantService } from '../../shared/service/constant-service';
import { SharedHttpClientService } from '../../shared/service/shared-http-client.service';
import { LocalStorageService } from '../../shared/service/local-storage.service';
import { CancellationRange, TripCancellationReport } from './trip-cancellation-report.model';

@Injectable({
  providedIn: 'root',
})
export class TripCancellationReportService {
  private webServiceURL: string;

  constructor(
    private _constantService: ConstantService,
    private _httpClient: SharedHttpClientService,
    private _localStorage: LocalStorageService
  ) {
    this.webServiceURL = this._constantService.WEBSERVICE_URL + 'reports/cancelTicketsReport';
  }

  /**
   * Get cancelled trip tickets based on the provided filter criteria
   * @param requestBody The filter criteria
   * @returns Observable of trip cancellation reports
   */
  public getCancelledTicketList(
    requestBody: CancellationRange
  ): Observable<TripCancellationReport[]> {
    return this._httpClient.post(this.webServiceURL, requestBody).pipe(
      map(response => response as TripCancellationReport[]),
      catchError(error => {
        console.error('Error fetching cancelled ticket list:', error);
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
   * @returns Observable of all trip cancellation reports
   */
  public query(): Observable<TripCancellationReport[]> {
    return this._httpClient.get(this.webServiceURL).pipe(
      map(response => response as TripCancellationReport[]),
      catchError(error => {
        console.error('Error querying trip cancellation reports:', error);
        return throwError(() => error);
      })
    );
  }
}
