import { Component, OnInit } from '@angular/core';
import { TripTicketService } from './trip-ticket.service';
import * as moment from 'moment';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-claims-debug',
  template: `
    <div class="container">
      <h2>Claims Debug Page</h2>
      <div class="row">
        <div class="col-md-12">
          <div class="form-group">
            <label>Ticket ID:</label>
            <input type="number" class="form-control" [(ngModel)]="ticketId" />
          </div>
          <button class="btn btn-primary" (click)="fetchClaims()">Fetch Claims</button>
        </div>
      </div>

      <div class="row mt-3" *ngIf="claims && claims.length > 0">
        <div class="col-md-12">
          <h4>Claims Found: {{claims.length}}</h4>

          <table class="table table-bordered">
            <thead>
              <tr>
                <th>ID</th>
                <th>Provider</th>
                <th>Pickup Time</th>
                <th>Formatted Time</th>
                <th>Acknowledged</th>
                <th>Fare</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let claim of claims">
                <td>{{claim.id}}</td>
                <td>{{claim.claimant_provider_name}}</td>
                <td>{{claim.proposed_pickup_time}}</td>
                <td>{{claim._proposed_pickup_time || 'Not formatted'}}</td>
                <td>{{claim.ackStatusString || (claim.ackStatus ? 'Yes' : 'No')}}</td>
                <td>{{claim.proposed_fare | currency:'USD':'symbol'}}</td>
                <td>{{claim.status?.type || 'Unknown'}}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="row mt-3" *ngIf="claims && claims.length === 0">
        <div class="col-md-12 alert alert-warning">
          No claims found for this ticket.
        </div>
      </div>

      <div class="row mt-3" *ngIf="error">
        <div class="col-md-12 alert alert-danger">
          {{error}}
        </div>
      </div>
    </div>
  `
})
export class ClaimsDebugComponent implements OnInit {
  ticketId: number = 0;
  claims: any[] = [];
  error: string = '';

  private destroy$ = new Subject<void>();

  constructor(private _tripTicketService: TripTicketService) {}

  ngOnInit() {}

  fetchClaims() {
    this.error = '';

    if (!this.ticketId || this.ticketId <= 0) {
      this.error = 'Please enter a valid ticket ID';
      return;
    }

    this._tripTicketService.getClaims(this.ticketId).pipe(
      takeUntil(this.destroy$)
    ).subscribe(
      response => {
        this.claims = response;
        console.log('Debug - Fetched claims:', this.claims);

        // Format claims just like in the main component
        if (this.claims) {
          for (let i = 0; i < this.claims.length; i++) {
            this.formatClaimTime(this.claims[i]);

            // Set ackStatusString
            if (this.claims[i].ackStatus === true) {
              this.claims[i].ackStatusString = 'Yes';
            } else {
              this.claims[i].ackStatusString = 'No';
            }
          }
        }
      },
      error => {
        console.error('Error fetching claims:', error);
        this.error = 'Error fetching claims: ' + (error.message || 'Unknown error');
      }
    );
  }

  // Helper method to format claim times - copy of the one in trip-ticket.component.ts
  private formatClaimTime(claim: any): void {
    if (!claim) return;

    try {
      if (claim.proposed_pickup_time !== null) {
        // Ensure we're not trying to split undefined values
        if (typeof claim.proposed_pickup_time !== 'string') {
          console.warn('Unexpected proposed_pickup_time format:', claim.proposed_pickup_time);
          claim._proposed_pickup_time = '-';
          return;
        }

        // Check if the proposed_pickup_time contains a T (ISO format)
        if (claim.proposed_pickup_time.includes('T')) {
          // Parse the full datetime and format it
          const dt = moment(claim.proposed_pickup_time);
          if (dt.isValid()) {
            // Format the date part
            claim._proposed_pickup_date = dt.format('MM/DD/YYYY');
            // Format the time part
            claim._proposed_pickup_time = dt.format('HH:mm');
            return;
          }
        }

        // Fall back to splitting based on colons
        const timeParts = claim.proposed_pickup_time.split(':');
        if (timeParts.length >= 2) {
          const hour = parseInt(timeParts[0], 10);
          const minute = timeParts[1];

          if (isNaN(hour)) {
            console.warn('Invalid hour in claim time:', timeParts[0]);
            claim._proposed_pickup_time = '-';
          } else {
            claim._proposed_pickup_time = hour.toString().padStart(2, '0') + ':' + minute;
          }
        } else {
          console.warn('Invalid time format in claim:', claim.proposed_pickup_time);
          claim._proposed_pickup_time = '-';
        }
      } else {
        claim._proposed_pickup_time = '-';
      }
    } catch (e) {
      console.error('Error formatting claim time:', e);
      claim._proposed_pickup_time = '-';
    }
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
