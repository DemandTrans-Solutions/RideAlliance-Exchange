import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { TripCompletedReportService } from './trips-completed-report.service';
import { ListService } from '../../shared/service/list.service';
import { NotificationEmitterService } from '../../shared/service/notification-emitter.service';
import { LocalStorageService } from '../../shared/service/local-storage.service';
import { ConstantService } from '../../shared/service/constant-service';
import { TranslateService } from '@ngx-translate/core';
import {
  CompletedRange,
  ShowTicketOption,
  TripCompletedReport,
} from './trip-completed-report.model';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import moment from 'moment';
import { Table } from 'primeng/table';
import * as XLSX from 'xlsx';

@Component({
  selector: 'app-trip-completed-report',
  templateUrl: './trips-completed-report.component.html',
  styleUrls: ['./trips-completed-report.component.scss'],
})
export class TripCompletedReportComponent implements OnInit, OnDestroy {
  @ViewChild('dt') dt: Table | undefined;

  // Destroy subject for managing subscriptions
  private destroy$ = new Subject<void>();

  // Filter parameters
  public completed: CompletedRange = {
    fromDate: '',
    toDate: '',
    providerId: 0,
    reportTicketFilterStatus: [],
    partnerProviderTicket: false,
    myTicket: false,
  };

  // Form fields
  public fromDate: string = '';
  public toDate: string = '';
  public minRange: boolean = false;
  public maxRange: boolean = false;
  public loading: boolean = false;
  public isFilter: boolean = false;
  public providerPartnersId: number | null = null;

  // Data
  public ticketList: TripCompletedReport[] = [];
  public claimantProviderList: string[] = [];
  public providersList: any[] = [];
  public arrayShowTicket: number[] = [];
  public showTicket: number[] = [];
  public ShowTickets: ShowTicketOption[] = [
    { value: 1, label: 'My Tickets' },
    { value: 2, label: "Partner's Ticket" },
  ];

  // User info
  public loggerRole: string = this._localStorage.get('Role');
  public pageTitle: string = 'Competed Trips Report';

  // Table properties for PrimeNG
  public gb: any; // Global filter value
  public hourFormat: string = '12';

  constructor(
    private _listService: ListService,
    private _tripCompletedReportService: TripCompletedReportService,
    private _notificationService: NotificationEmitterService,
    private _localStorage: LocalStorageService,
    private _constantService: ConstantService,
    private translateService: TranslateService
  ) {}

  ngOnInit(): void {
    this.loadProviders();
  }

  ngOnDestroy(): void {
    // Complete the destroy subject to clean up subscriptions
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Load the list of providers for the admin dropdown
   */
  private loadProviders(): void {
    this._listService
      .queryProvider()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: response => {
          this.providersList = response;
        },
        error: error => {
          console.error('Error loading providers:', error);
        },
      });
  }

  /**
   * Check if the maximum date range is valid
   */
  public maxDate(): void {
    this.maxRange = true;
    if (new Date(this.fromDate) <= new Date(this.toDate)) {
      this.minRange = false;
    } else {
      this.minRange = true;
    }
  }

  /**
   * Handle checkbox selection for ticket types (My Tickets/Partner Tickets)
   */
  public checkShowTicket(showTicket: number[]): void {
    this.arrayShowTicket = showTicket;
  }

  /**
   * Reset date selection when from date changes
   */
  public dateChange(): void {
    this.toDate = '';
    this.maxRange = false;
  }

  /**
   * Filter trip completed reports based on selected criteria
   */
  public filter(): void {
    this.isFilter = true;

    // Set provider ID based on user role
    if (this.loggerRole === 'ROLE_ADMIN') {
      this.completed.providerId = this.providerPartnersId as number;
    } else {
      this.completed.providerId = Number(this._localStorage.get('providerId'));
    }

    // Format dates for API using moment
    const dateFrom = moment(this.fromDate).format();
    const dateTo = moment(this.toDate).format();

    // Split off timezone part
    this.completed.fromDate = dateFrom.split('+')[0];
    this.completed.toDate = dateTo.split('+')[0];

    // Set ticket type filters (My Tickets/Partner Tickets)
    this.completed.myTicket = false;
    this.completed.partnerProviderTicket = false;

    if (this.arrayShowTicket.length === 0) {
      // If no checkboxes selected, show all tickets
      this.completed.partnerProviderTicket = true;
      this.completed.myTicket = true;
    } else {
      // Set filter based on selected options
      this.arrayShowTicket.forEach(element => {

        if (element === 1) {
          this.completed.myTicket = true;
        } else if (element === 2) {
          this.completed.partnerProviderTicket = true;
        }
      });
    }

    // Set API request parameters
    this.completed.reportTicketFilterStatus = ['7']; // Status code for completed tickets

    // Fetch the completed ticket list
    this.loading = true;
    this._tripCompletedReportService
      .getCompletedTicketList(this.completed)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: response => {
          this.loading = false;
          this.ticketList = response;

          if (!this.ticketList || this.ticketList.length === 0) {
            this._notificationService.error(
              this.translateService.instant('COMMON.ERROR'),
              this.translateService.instant('REPORTS.NO_RESULTS_FOUND')
            );
            return;
          }

          // Process data for display
          this.processTicketList();
        },
        error: error => {
          this.loading = false;
          console.error('Error filtering trip completed reports:', error);
          this._notificationService.error(
            this.translateService.instant('COMMON.ERROR'),
            this.translateService.instant('COMMON.SERVER_ERROR')
          );
        },
      });
  }

  /**
   * Process ticket list data to add formatted display properties
   */
  private processTicketList(): void {
    for (const ticket of this.ticketList) {
      // Format customer name
      ticket._customerName = `${ticket.customer_first_name} ${ticket.customer_last_name}`;

      // Format claimant providers
      if (ticket.trip_Claims && ticket.trip_Claims.length > 0) {
        const claimantProviders: string[] = [];

        for (const claim of ticket.trip_Claims) {
          if (claim.claimant_provider_name) {
            claimantProviders.push(claim.claimant_provider_name);
          }
        }

        ticket.claimantProviders =
          claimantProviders.length > 0 ? claimantProviders.join(', ') : 'No Claimants';
      } else {
        ticket.claimantProviders = 'No Claimants';
      }

      // Format addresses
      ticket.pickup = this.formatAddress(ticket.pickup_address);
      ticket.dropoff = this.formatAddress(ticket.drop_off_address);

      // Format pickup date/time
      if (ticket.requested_pickup_date) {
        const pickupDate = this.formatDate(ticket.requested_pickup_date);
        const pickupTime = this.formatTime(ticket.requested_pickup_time || '');
        ticket.pickupDateTime = `${pickupDate} ${pickupTime}`;
      } else {
        ticket.pickupDateTime = '-';
      }

      // Format dropoff date/time
      if (ticket.requested_dropoff_date) {
        const dropoffDate = this.formatDate(ticket.requested_dropoff_date);
        const dropoffTime = this.formatTime(ticket.requested_dropoff_time || '');
        ticket.dropoffDateTime = `${dropoffDate} ${dropoffTime}`;
      } else {
        ticket.dropoffDateTime = '-';
      }

      // Format submitted date/time
      if (ticket.created_at) {
        ticket.submittedDateTime = this.formatDateTime(ticket.created_at);
      } else {
        ticket.submittedDateTime = '-';
      }

      // Format trip details
      this.formatTripDetails(ticket);
    }
  }

  /**
   * Format address object into a string
   */
  private formatAddress(address: any): string {
    if (!address) return '-';
    return `${address.street1}, ${address.city}, ${address.state}, ${address.zipcode}`;
  }

  /**
   * Format date string (YYYY-MM-DD to MM/DD/YYYY)
   */
  private formatDate(dateStr: string): string {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return `${date.getMonth() + 1}/${date.getDate()}/${date.getFullYear()}`;
  }

  /**
   * Format time string to 12-hour format with AM/PM
   */
  private formatTime(timeStr: string): string {
    if (!timeStr) return '';

    const [hours, minutes] = timeStr.split(':');
    const hoursNum = parseInt(hours, 10);

    if (hoursNum === 0) {
      return `12:${minutes} AM`;
    } else if (hoursNum === 12) {
      return `12:${minutes} PM`;
    } else if (hoursNum > 12) {
      return `${hoursNum - 12}:${minutes} PM`;
    } else {
      return `${hoursNum}:${minutes} AM`;
    }
  }

  /**
   * Format datetime string (ISO format to MM/DD/YYYY HH:MM AM/PM)
   */
  private formatDateTime(dateTimeStr: string): string {
    if (!dateTimeStr) return '-';

    const date = new Date(dateTimeStr);
    const month = date.getMonth() + 1;
    const day = date.getDate();
    const year = date.getFullYear();

    let hours = date.getHours();
    const minutes = date.getMinutes().toString().padStart(2, '0');
    const amPm = hours >= 12 ? 'PM' : 'AM';

    hours = hours % 12;
    hours = hours ? hours : 12; // Convert 0 to 12

    return `${month}/${day}/${year} ${hours}:${minutes} ${amPm}`;
  }

  /**
   * Format trip details including mobility factors, guests, etc.
   */
  private formatTripDetails(ticket: TripCompletedReport): void {
    const details: string[] = [];

    if (ticket.attendant_mobility_factors) {
      details.push(`Attendant Mobility Factors: ${ticket.attendant_mobility_factors}`);
    }

    if (ticket.guest_mobility_factors) {
      details.push(`Guest Mobility Factors: ${ticket.guest_mobility_factors}`);
    }

    if (ticket.guests && ticket.guests > 0) {
      details.push(`Guests: ${ticket.guests}`);
    }

    if (ticket.customer_service_animals) {
      details.push(`Service Animals: ${ticket.customer_service_animals}`);
    }

    if (ticket.customer_seats_required) {
      details.push(`Seats Required: ${ticket.customer_seats_required}`);
    }

    ticket.tripDetails = details.join(', ');
  }

  /**
   * Global filter handler for p-table
   */
  public onGlobalFilter(event: Event): void {
    if (this.dt) {
      this.dt.filterGlobal((event.target as HTMLInputElement).value, 'contains');
    }
  }

  /**
   * Export the current ticket list to Excel
   */
  public exportToExcel(): void {
    if (!this.ticketList || this.ticketList.length === 0) {
      this._notificationService.warn(
        this.translateService.instant('COMMON.WARNING'),
        this.translateService.instant('REPORTS.NO_DATA_TO_EXPORT')
      );
      return;
    }

    const exportData = this.ticketList.map(ticket => ({
      'Submitted Date': ticket.submittedDateTime || '',
      'Customer Name': ticket._customerName || '',
      'Claimant Provider': ticket.claimantProviders || '',
      'Pickup Date/Time': ticket.pickupDateTime || '',
      'Dropoff Date/Time': ticket.dropoffDateTime || '',
      'Pickup Address': ticket.pickup || '',
      'Dropoff Address': ticket.dropoff || '',
      'Trip Details': ticket.tripDetails || ''
    }));

    const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(exportData);
    const workbook: XLSX.WorkBook = { Sheets: { 'data': worksheet }, SheetNames: ['data'] };
    const excelBuffer: any = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });

    this.saveAsExcelFile(excelBuffer, 'completed_trips_report');
  }

  /**
   * Export the current ticket list to CSV
   */
  public exportToCSV(): void {
    if (!this.ticketList || this.ticketList.length === 0) {
      this._notificationService.warn(
        this.translateService.instant('COMMON.WARNING'),
        this.translateService.instant('REPORTS.NO_DATA_TO_EXPORT')
      );
      return;
    }

    type ExportRow = {
      'Submitted Date': string;
      'Customer Name': string;
      'Claimant Provider': string;
      'Pickup Date/Time': string;
      'Dropoff Date/Time': string;
      'Pickup Address': string;
      'Dropoff Address': string;
      'Trip Details': string;
    };

    const exportData = this.ticketList.map(ticket => ({
      'Submitted Date': ticket.submittedDateTime || '',
      'Customer Name': ticket._customerName || '',
      'Claimant Provider': ticket.claimantProviders || '',
      'Pickup Date/Time': ticket.pickupDateTime || '',
      'Dropoff Date/Time': ticket.dropoffDateTime || '',
      'Pickup Address': ticket.pickup || '',
      'Dropoff Address': ticket.dropoff || '',

      'Trip Details': ticket.tripDetails || ''
    })) as ExportRow[];

    const replacer = (_key: string, value: any) => value === null ? '' : value;
    const header = Object.keys(exportData[0]);
    const csv = exportData.map(row =>
      header.map(fieldName => JSON.stringify(row[fieldName as keyof ExportRow], replacer)).join(',')
    );
    csv.unshift(header.join(','));
    const csvArray = csv.join('\r\n');

    const blob = new Blob([csvArray], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', 'completed_trips_report.csv');
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

  /**
   * Save Excel file to disk
   */
  private saveAsExcelFile(buffer: any, fileName: string): void {
    const data: Blob = new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    const link = document.createElement('a');
    const url = window.URL.createObjectURL(data);
    link.setAttribute('href', url);
    link.setAttribute('download', `${fileName}.xlsx`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
}
