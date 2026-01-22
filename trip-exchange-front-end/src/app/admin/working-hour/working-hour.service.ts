import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { WorkingHour } from './working-hour';

@Injectable({
  providedIn: 'root',
})
export class WorkingHourService {
  private apiUrl = environment.apiUrl + 'workinghours';

  constructor(private http: HttpClient) {}

  /**
   * Get working hours for a provider
   * @param providerId Provider ID
   * @returns Observable of WorkingHour array
   */
  getByProvider(providerId: number): Observable<WorkingHour[]> {
    return this.http.get<WorkingHour[]>(`${this.apiUrl}/providerIdWiseWorkingHours/${providerId}`);
  }

  /**
   * Get a specific working hour by ID
   * @param id Working hour ID
   * @returns Observable of WorkingHour
   */
  getById(id: number): Observable<WorkingHour> {
    return this.http.get<WorkingHour>(`${this.apiUrl}/${id}`);
  }

  /**
   * Create a new working hour
   * @param workingHour Working hour data
   * @returns Observable of created WorkingHour
   */
  create(workingHour: WorkingHour): Observable<WorkingHour> {
    return this.http.post<WorkingHour>(`${this.apiUrl}/create`, [workingHour]);
  }

  /**
   * Update an existing working hour
   * @param id Working hour ID
   * @param workingHour Updated working hour data
   * @returns Observable of updated WorkingHour
   */
  update(providerId: number, workingHour: WorkingHour): Observable<WorkingHour> {
    return this.http.put<WorkingHour>(`${this.apiUrl}/updateProviderIdWiseWorkingHours/${providerId}`, [workingHour]);
  }

  /**
   * Delete a working hour
   * @param id Working hour ID
   * @returns Observable of operation result
   */
  delete(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  /**
   * Set active status of a working hour
   * @param id Working hour ID
   * @param isActive Active status
   * @returns Observable of update result
   */
  setActive(id: number, isActive: boolean): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/status`, { isActive });
  }
}
