import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { TripAcceptanceCriteria } from './trip-acceptance.model';

@Injectable()
export class TripAcceptanceService {
  private apiUrl = environment.apiUrl + 'trip-acceptance';

  constructor(private http: HttpClient) {}

  /**
   * Get all funding sources
   */
  getFundingSources(): Observable<any[]> {
    return this.http
      .get<any[]>(`${environment.apiUrl}funding-sources`)
      .pipe(map((response: any) => response.data || []));
  }

  /**
   * Get all trip acceptance criteria
   */
  getTripAcceptanceCriteria(): Observable<TripAcceptanceCriteria[]> {
    return this.http.get<any>(`${this.apiUrl}`).pipe(map((response: any) => response.data || []));
  }

  /**
   * Create new trip acceptance criteria
   */
  createTripAcceptanceCriteria(criteria: TripAcceptanceCriteria): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}`, criteria);
  }

  /**
   * Update existing trip acceptance criteria
   */
  updateTripAcceptanceCriteria(criteria: TripAcceptanceCriteria): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${criteria.id}`, criteria);
  }

  /**
   * Delete trip acceptance criteria
   */
  deleteTripAcceptanceCriteria(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }
}
