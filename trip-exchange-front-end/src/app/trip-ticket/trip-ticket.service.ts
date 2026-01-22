import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { map, catchError, timeout, retry } from 'rxjs/operators';
import { ConstantService } from '../shared/service/constant-service';
import { SharedHttpClientService } from '../shared/service/shared-http-client.service';
import { LocalStorageService } from '../shared/service/local-storage.service';
import { UberBookingResponse, UberCancellationResponse, UberRequest, UberResponse, UberRideRequest } from './uber-request';

export interface TripResultRequestDTO {
  id?: number;
  tripTicketId: number;
  trip_claim_id?: number;
  tripDate: string;
  actualPickupArriveTime?: string;
  actualPickupDepartTime?: string;
  actualDropOffArriveTime?: string;
  actualDropOffDepartTime?: string;
  pickUpLatitude?: number;
  pickupLongitude?: number;
  dropOffLatitude?: number;
  dropOffLongitude?: number;
  vehicleId?: string;
  driverId?: string;
  claimantProvider?: string;
  pickUpAddress?: string;
  dropOffAddress?: string;
  numberOfGuests?: number;
  numberOfAttendants?: number;
  numberOfPassengers?: number;
  fareCollected?: number;
  noShowFlag?: boolean;
  cancellationReason?: string;
  noShowReason?: string;
}

export interface TripResultDTO {
  id: number;
  tripTicketId: number;
  tripDate: string;
  actualPickupArriveTime?: string;
  actualPickupDepartTime?: string;
  actualDropOffArriveTime?: string;
  actualDropOffDepartTime?: string;
  pickUpLatitude?: number;
  pickupLongitude?: number;
  dropOffLatitude?: number;
  dropOffLongitude?: number;
  vehicleId?: string;
  driverId?: string;
  claimantProvider?: string;
  pickUpAddress?: string;
  dropOffAddress?: string;
  scheduledPickupTime?: string;
  scheduledDropOffTime?: string;
  numberOfGuests?: number;
  numberOfAttendants?: number;
  numberOfPassengers?: number;
  orgProviderId?: number;
  rate?: number;
  fare?: number;
  notes?: string;
  outcome?: string;
  version?: number;
  fareCollected?: number;
  noShowFlag?: boolean;
  trip_claim_id?: number;
  driver_name?: string;
  rate_type?: string;
  vehicle_type?: string;
  vehicle_name?: string;
  fare_type?: string;
  base_fare?: number;
  miles_traveled?: number;
  odometer_start?: number;
  odometer_end?: number;
  billable_mileage?: number;
  extra_securement_count?: number;
  created_at?: string;
  updated_at?: string;
}

@Injectable({
  providedIn: 'root'
})
export class TripTicketService {
  private webServiceURL: string;
  private webServiceURLForSave: string;
  private webServiceURLForSearchFilter: string;
  private webServiceURLForSaveFilter: string;
  private webServiceURLForShowFilter: string;
  private webServiceURLToUpdateFilter: string;
  private webServiceURLForAddress: string;
  private webServiceURLForActivity: string;
  private webServiceURLForClaims: string;
  private webServiceURLTOGetTickets: string;
  private webServiceURLForMinimumDate: string;
  private webServiceURLForSummaryReport: string;
  private webServiceURLForUpdate: string;
  private webServiceURLForTripTicketCost: string;
  private webServiceURLForSaveTripTicketCost: string;
  private webServiceURLForCustomerEligibility: string;
  private webServiceURLForHospitalityAreaList: string;
  private webServiceURLForApprovedProviderPartner: string;
  private webServiceURLForFilterList: string;
  private webServiceURLForFilters: string;
  private webServiceURLToUploadTickets: string;
  private webServiceURLForUber: string;
  private webServiceURLForTripResultCompleted: string;

  constructor(
    private _constantService: ConstantService,
    private _httpClient: SharedHttpClientService,
    private _localStorage: LocalStorageService
  ) {
    this.webServiceURL = this._constantService.WEBSERVICE_URL + 'trip_tickets/syncForUI';
    this.webServiceURLForSave = this._constantService.WEBSERVICE_URL + 'trip_tickets';
    this.webServiceURLForSearchFilter = this._constantService.WEBSERVICE_URL + 'ticket_filters/filterByObject';
    this.webServiceURLForSaveFilter = this._constantService.WEBSERVICE_URL + 'ticket_filters';
    this.webServiceURLForShowFilter = this._constantService.WEBSERVICE_URL + 'ticket_filters/';
    this.webServiceURLToUpdateFilter = this._constantService.WEBSERVICE_URL + 'ticket_filters/';
    this.webServiceURLForAddress = this._constantService.WEBSERVICE_URL + 'list/address/';
    this.webServiceURLForActivity = this._constantService.WEBSERVICE_URL + 'activity/tripTicket/';
    this.webServiceURLTOGetTickets = this._constantService.WEBSERVICE_URL + 'trip_tickets/syncForUI';
    this.webServiceURLToUploadTickets = this._constantService.WEBSERVICE_URL + 'trip_tickets/uploadTripTickets';
    this.webServiceURLForMinimumDate = this._constantService.WEBSERVICE_URL + 'reports/oldestCreatedDate/';
    this.webServiceURLForSummaryReport = this._constantService.WEBSERVICE_URL + 'reports/summaryReport';
    this.webServiceURLForUpdate = this._constantService.WEBSERVICE_URL + 'trip_tickets/';
    this.webServiceURLForTripTicketCost = this._constantService.WEBSERVICE_URL + 'trip_tickets/getTripticketCostForProvider/';
    this.webServiceURLForSaveTripTicketCost = this._constantService.WEBSERVICE_URL + 'tripticketcost/createUpdateTripTicketCost';
    this.webServiceURLForCustomerEligibility = this._constantService.WEBSERVICE_URL + 'trip_tickets/listOfCustomerEligibility';
    this.webServiceURLForHospitalityAreaList = this._constantService.WEBSERVICE_URL + 'servicearea/hospitalityarealist';
    this.webServiceURLForApprovedProviderPartner = this._constantService.WEBSERVICE_URL + 'providerPartners/approvedProviderPartners/';
    this.webServiceURLForClaims = this._constantService.WEBSERVICE_URL + 'trip_tickets/trip_claims/';
    this.webServiceURLForFilterList = this._constantService.WEBSERVICE_URL + 'ticket_filters';
    this.webServiceURLForFilters = this._constantService.WEBSERVICE_URL + 'ticket_filters';
    this.webServiceURLForUber = this._constantService.WEBSERVICE_URL + 'uber';
    this.webServiceURLForTripResultCompleted = this._constantService.WEBSERVICE_URL + 'trip_tickets/trip_result/completed';

  }

  /**
   * Get list of approved provider partners
   */
  public getApprovedProviderPartner(id: string): Observable<any> {
    return this._httpClient.get(this.webServiceURLForApprovedProviderPartner + id).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Get cost by trip ticket ID
   */
  public getCostByTripTicketId(tripTicketId: string): Observable<any> {
    return this._httpClient.get(this._constantService.WEBSERVICE_URL + 'tripticketcost/' + tripTicketId).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Get cost for trip ticket
   */
  public getCostForTripTicket(ticketId: string, claimentId: string): Observable<any> {
    return this._httpClient.get(this.webServiceURLForTripTicketCost + ticketId + '/claimantProviderId/' + claimentId).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Save cost for trip ticket
   */
  public saveCostForTripTicket(requestBody: any): Observable<any> {
    return this._httpClient.post(this.webServiceURLForSaveTripTicketCost, requestBody).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Get hospital service list
   */
  public getHospitalServiceList(): Observable<any> {
    return this._httpClient.get(this.webServiceURLForHospitalityAreaList).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Update trip ticket
   */
  public updateTripTicket(id: string, requestBody: any): Observable<any> {
    const url = this._constantService.WEBSERVICE_URL + 'trip_tickets/partialUpdate/' + id;
    return this._httpClient.put(url, requestBody).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Get tickets by provider ID and role
   */
  public get(id: string, role: string): Observable<any> {
    if (role === 'ROLE_PROVIDERADMIN' || role === 'ROLE_PROVIDERUSER' || role === 'ROLE_READONLY') {
      this.webServiceURLTOGetTickets = this._constantService.WEBSERVICE_URL + 'trip_tickets/providerIdWiseTickets/' + id;
    }
    return this._httpClient.get(this.webServiceURLTOGetTickets).pipe(
      map((response) => {
        const result = this._constantService.extractData(response);
        this.webServiceURL = '';
        return result;
      }),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Query for tickets
   */
  public query(): Observable<any> {
    return this._httpClient.get(this.webServiceURL).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Get customer eligibility options
   */
  public customerEligibility(): Observable<any> {
    return this._httpClient.get(this.webServiceURLForCustomerEligibility).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Save a ticket
   */
  public save(requestBody: any): Observable<any> {
    return this._httpClient.post(this.webServiceURLForSave, requestBody).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Update a filter
   */
  public update(id: string, requestBody: any): Observable<any> {
    return this._httpClient.put(this.webServiceURLToUpdateFilter + id, requestBody).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Remove a ticket
   */
  public remove(id: string): Observable<any> {
    return this._httpClient.delete(this.webServiceURL + id).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Search for tickets using filter
   */
  public searchFilter(requestBody: any): Observable<any> {
    return this._httpClient.post(this.webServiceURLForSearchFilter, requestBody).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Save a filter
   */
  public saveFilter(requestBody: any): Observable<any> {
    return this._httpClient.post(this.webServiceURLForSaveFilter, requestBody).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }




  /**
   * Get filter by ID
   */
  public showFilter(filterId: string): Observable<any> {
    const url = this.webServiceURLForShowFilter + filterId;
    return this._httpClient.get(url).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Get address by ID
   */
  public getaddress(id: string): Observable<any> {
    return this._httpClient.get(this.webServiceURLForAddress + id).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Save a comment for a ticket
   */
  public saveComment(id: string, comment: any): Observable<any> {
    const url = this._constantService.WEBSERVICE_URL + 'trip_tickets/' + id + '/trip_ticket_comments';
    return this._httpClient.post(url, comment).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * View activity for a ticket
   */
  public viewActivity(id: string): Observable<any> {
    return this._httpClient.get(this.webServiceURLForActivity + id).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Create a claim for a ticket
   */
  public createClaim(claim: any): Observable<any> {
    const url = this._constantService.WEBSERVICE_URL + 'trip_tickets/' + claim.trip_ticket_id + '/trip_claims';
    return this._httpClient.post(url, claim).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Edit a claim
   */
  public editClaim(tripId: string, claim: any): Observable<any> {
    const url = this._constantService.WEBSERVICE_URL + 'trip_tickets/' + tripId + '/trip_claims/' + claim.id;
    return this._httpClient.put(url, claim).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Change status of a claim to approved
   */
  public changeStatusToApproved(data: any): Observable<any> {
    const url = this._constantService.WEBSERVICE_URL + 'trip_tickets/' + data.ticketId + '/trip_claims/' + data.claimId + '/approve';
    return this._httpClient.put(url, data).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Change status of a claim to decline
   */
  public changeStatusToDecline(data: any): Observable<any> {
    const url = this._constantService.WEBSERVICE_URL + 'trip_tickets/' + data.ticketId + '/trip_claims/' + data.claimId + '/decline';
    return this._httpClient.put(url, data).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }


  public changeStatusToRescind(tripId: number, claimId: number): Observable<any> {
    return this.statusRescind(tripId, claimId, {});
  }

  /**
   * Change status of a claim to rescind
   */
  public statusRescind(tripId: number, claimId: number, requestBody: any): Observable<any> {
    this.webServiceURL =
      this._constantService.WEBSERVICE_URL +
      'trip_tickets/' +
      tripId +
      '/trip_claims/' +
      claimId +
      '/rescind';
    return this._httpClient
      .put(this.webServiceURL, requestBody)
      .pipe(map(this._constantService.extractData), catchError(this.handleHttpError));
  }

  /**
   * Get details of a ticket
   */
  public getTicketDetails(ticketId: string, providerId: string): Observable<any> {
    const url = this._constantService.WEBSERVICE_URL + 'trip_tickets/' + ticketId + '?providerId=' + providerId;
    return this._httpClient.get(url).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Get claims for a ticket
   */
  public getClaims(id: any): Observable<any> {
    const url = this._constantService.WEBSERVICE_URL + 'trip_tickets/' + id + '/trip_claims';
    return this._httpClient.get(url).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Get comments for a ticket
   */
  public getComments(id: any): Observable<any> {
    const url = this._constantService.WEBSERVICE_URL + 'trip_tickets/' + id + '/trip_ticket_comments';
    return this._httpClient.get(url).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Get a claim by ID
   */
  public getClaimById(ticketId: string, claimId: string): Observable<any> {
    const url = this._constantService.WEBSERVICE_URL + 'trip_tickets/' + ticketId + '/trip_claims/' + claimId;
    return this._httpClient.get(url).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Check for service area
   */
  public checkForServiceArea(requestBody: any): Observable<any> {
    const url = this._constantService.WEBSERVICE_URL + 'servicearea/serviceareaCheck';
    return this._httpClient.post(url, requestBody).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Check for working hours
   */
  public checkForWorkingHours(requestBody: any): Observable<any> {
    const url = this._constantService.WEBSERVICE_URL + 'workinghours/workingHoursCheck';
    return this._httpClient.post(url, requestBody).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Get user ID for provider
   */
  public userIdForProvider(providerId: string): Observable<any> {
    const url = this._constantService.WEBSERVICE_URL + 'users/userIdByProviderId/' + providerId;
    return this._httpClient.get(url).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Get minimum date
   */
  public getMinimunDate(id: string): Observable<any> {
    // Defensive check: ensure id is not null/undefined/empty/"null"
    if (id === null || id === undefined || String(id).trim() === '' || String(id) === 'null') {
      console.warn('getMinimunDate: providerId missing or invalid ("' + id + '"); cannot fetch minimum date');
      // Return an observable that emits a default response rather than failing
      return new Observable(observer => {
        observer.next({ date: null });
        observer.complete();
      });
    }

    return this._httpClient.get(this.webServiceURLForMinimumDate + id).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Utility function to format dates into ISO 8601 format without timezone (YYYY-MM-DDTHH:mm:ss)
   */
  private formatDateToISOWithoutTimezone(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
  }

  /**
   * Get summary reports
   */
  public getSummaryReports(requestBody: any): Observable<any> {
    // Format dates in the request body
    if (requestBody.fromDate) {
      requestBody.fromDate = this.formatDateToISOWithoutTimezone(new Date(requestBody.fromDate));
    }
    if (requestBody.toDate) {
      requestBody.toDate = this.formatDateToISOWithoutTimezone(new Date(requestBody.toDate));
    }

    return this._httpClient.post(this.webServiceURLForSummaryReport, requestBody).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Get provider by ID from localStorage
   */
  public getProvider(providerId?: string): Observable<any> {
    const id = providerId || this._localStorage.get('providerId');
    const url = this._constantService.WEBSERVICE_URL + 'provider/' + id;
    return this._httpClient.get(url).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Get all providers
   */
  public getAllProvider(): Observable<any> {
    const url = this._constantService.WEBSERVICE_URL + 'provider/';
    return this._httpClient.get(url).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Get tickets list with pagination
   */
  public getTicketsList(first: number, rows: number, sortField?: string, sortOrder?: number): Observable<any> {
    //let url = this._constantService.WEBSERVICE_URL + 'trip_tickets?first=' + first + '&rows=' + rows;
    //if (sortField && sortOrder) {
     // url += '&sortField=' + sortField + '&sortOrder=' + sortOrder;
   //}

    console.debug(`Fetching tickets list: pageNumber=${first}, pageSize=${rows}, sortField=${sortField}, sortOrder=${sortOrder}`);
    let url = this._constantService.WEBSERVICE_URL + 'trip_tickets/pagination?pagenumber=' + first + '&pagesize=' + rows;
    if (sortField && sortOrder) {
      url += '&sortField=' + sortField + '&sortOrder=' + sortOrder;
    }

    return this._httpClient.get(url).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Get all tickets list with pagination
   */
  public getAllTicketsList(first: number, rows: number, sortField?: string, sortOrder?: number): Observable<any> {
    //let url = this._constantService.WEBSERVICE_URL + 'trip_tickets/all?first=' + first + '&rows=' + rows;
    let url = this._constantService.WEBSERVICE_URL + 'trip_tickets/pagination?pagenumber=' + first + '&pagesize=' + rows;
    if (sortField && sortOrder) {
      url += '&sortField=' + sortField + '&sortOrder=' + sortOrder;
    }
    console.debug('Fetching all tickets with URL:', url);
    return this._httpClient.get(url).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }



  /**
   * Get filter list
   */
  public getFilterList(): Observable<any> {
    return this._httpClient.get(this.webServiceURLForFilters).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Get filter by ID
   */
  public getFilterById(id: string): Observable<any> {
    return this._httpClient.get(this.webServiceURLForFilters + '/' + id).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Get my tickets
   */
  public getMyTickets(providerId: string, first: number, rows: number, sortField?: string, sortOrder?: number): Observable<any> {
    let url = this._constantService.WEBSERVICE_URL + 'trip_tickets/my?providerId=' + providerId + '&first=' + first + '&rows=' + rows;
    if (sortField && sortOrder) {
      url += '&sortField=' + sortField + '&sortOrder=' + sortOrder;
    }
    return this._httpClient.get(url).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Get claim by provider name
   */
  public getClaimByProviderName(ticketId: any, _providerName: string): Observable<any> {
    //const url = this._constantService.WEBSERVICE_URL + 'trip_tickets/' + ticketId + '/trip_claims/provider/' + providerName;
    const url = this._constantService.WEBSERVICE_URL + 'trip_tickets/' + ticketId + '/trip_claims';
    return this._httpClient.get(url).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Get claim for edit
   */
  public getClaimForEdit(ticketId: string, claimantName: string): Observable<any> {
    const url = this._constantService.WEBSERVICE_URL + 'trip_tickets/' + ticketId + '/trip_claims/edit/' + claimantName;
    return this._httpClient.get(url).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }



  /**
   * Request Uber options
   */
  public requestUberOptions(requestBody: UberRequest): Observable<UberResponse> {
    const url = this.webServiceURLForUber + '/options';
    return this._httpClient.post(url, requestBody).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }



  /**
   * Book Uber option
   */
  public bookUberOption(requestBody: UberRideRequest): Observable<UberBookingResponse> {
    const url = this.webServiceURLForUber + '/book';
    return this._httpClient.post(url, requestBody).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }


  /**
   * Book Uber option
   */
  public cancelUberTrip(tripTicketId: string): Observable<UberCancellationResponse> {
    const url = this.webServiceURLForUber + '/cancelUberTrip/' + tripTicketId;
    return this._httpClient.put(url, {}).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }




  /**
  * Get current ride status for a trip ticket
  */
  public currentRideStatus(tripTicketId: string): Observable<any> {
    const url = this._constantService.WEBSERVICE_URL + 'trip_tickets/' + tripTicketId + '/rideStatus';
    return this._httpClient.get(url).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }



  /**
   * Cancel / Rescind a trip ticket
   */
  public cancelTripTicket(requestPayload: any): Observable<any> {
    const url = this._constantService.WEBSERVICE_URL + 'trip_tickets/' + requestPayload.ticketId + '/cancel';
    return this._httpClient.put(url, requestPayload).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }


  public rescindTripTicket(ticketId: string): Observable<any> {
    const url = this._constantService.WEBSERVICE_URL + 'trip_tickets/' + ticketId + '/rescind';
    return this._httpClient.put(url, {}).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Custom error handler for upload-specific errors
   */
  private handleUploadError(error: any): Observable<never> {
    let errorMessage = 'Failed to upload tickets';
    console.error('Upload error details:', error);
    if (error.error) {
      if (error.error.message) {
        errorMessage = error.error.message;
      } else if (error.error.errors) {
        errorMessage = error.error.errors.join(', ');
      } else if (typeof error.error === 'string') {
        errorMessage = error.error;
      }
    }

    return throwError(() => new Error(errorMessage));
  }

  /**
   * Upload trip tickets from a file
   * @param ticketCollection The collection of trip ticket DTOs to upload
   * @returns Observable with the response from the server
   */
  public uploadTickets(ticketCollection: any): Observable<any> {
    // Validate input
    if (!Array.isArray(ticketCollection) || ticketCollection.length === 0) {
      return throwError(() => new Error('No valid tickets to upload'));
    }
    console.log(`Uploading ${ticketCollection.length} tickets...`);

    return this._httpClient.post(this.webServiceURLToUploadTickets, ticketCollection).pipe(
      map(this._constantService.extractData),
      catchError(error => this.handleUploadError(error)),
      timeout(300000), // 5-minute timeout for large uploads
      retry(3) // Retry failed uploads up to 3 times
    );
  }

  /**
   * Handle HTTP errors - delegates to ConstantService for consistent error handling
   */
  private handleHttpError = (error: any): Observable<never> => {
    return this._constantService.handleError(error);
  };

  /**
   * Export selected tickets to Excel format
   * @param ticketIds Array of ticket IDs to export
   * @returns Observable with the trip data for export
   */
  public exportTickets(ticketIds: number[]): Observable<any> {
    const url = this._constantService.WEBSERVICE_URL + 'trip_tickets/export';
    const requestBody = { ticketIds };

    return this._httpClient.post(url, requestBody).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

  /**
   * Create trip completed result
   * @param requestBody Array of TripResultRequestDTO to submit
   * @returns Observable with array of TripResultDTO
   */
  public createTripCompletedResult(requestBody: TripResultRequestDTO[]): Observable<TripResultDTO[]> {
    return this._httpClient.post(this.webServiceURLForTripResultCompleted, requestBody).pipe(
      map(this._constantService.extractData),
      catchError(this._constantService.handleError)
    );
  }

}
