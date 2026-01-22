import { Component, OnInit, EventEmitter, Input, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { Message } from 'primeng/api';
import { ConfirmationService } from 'primeng/api';
import { Table } from 'primeng/table';
import { NewTripTicketReportService } from './new-trip-ticket-report.service';
import { ListService } from '../../shared/service/list.service';
import { NotificationEmitterService } from '../../shared/service/notification-emitter.service';
import { LocalStorageService } from '../../shared/service/local-storage.service';
import { TokenService } from '../../shared/service/token.service';
import { Range } from './new-trip-ticket-report';
import { SummaryReport } from './new-trip-ticket-report';
import moment from 'moment';
import * as XLSX from 'xlsx';

interface TicketData {
  submittedDateTime: string;
  _customerName: string;
  claimantProviders: string;
  pickupDateTime: string;
  dropoffDateTime: string;
  pickup: string;
  dropoff: string;
  status: {
    type: string;
  };
  tripDetails: string;
  customer_first_name: string;
  customer_last_name: string;
  trip_Claims: Array<{
    claimant_provider_name: string;
  }>;
  pickup_address: {
    street1: string;
    city: string;
    state: string;
    zipcode: string;
  };
  drop_off_address: {
    street1: string;
    city: string;
    state: string;
    zipcode: string;
  };
  requested_pickup_date: string | null;
  requested_pickup_time: string;
  requested_dropoff_date: string | null;
  requested_dropoff_time: string;
  created_at: string | null;
  _requested_pickup_time?: string;
  _requested_dropOff_time?: string;
  attendant_mobility_factors: string | null;
  guest_mobility_factors: string | null;
  guests: number | string;
  customer_service_animals: string | null;
  customer_seats_required: string | null;
}

@Component({
  selector: 'app-new-trip-ticket-report',
  templateUrl: 'new-trip-ticket-report.component.html',
  styleUrls: ['new-trip-ticket-report.component.scss'],
  providers: [NewTripTicketReportService, ListService],
})
export class NewTripTicketReportComponent implements OnInit {
  @ViewChild('dt') dt!: Table;
  public providerPartnersId: any;
  public pleaseSelect: any = null;
  public toDate: Date = new Date();
  public minDateTime: any;
  @Input() range: Range = new Range();
  public ticketList: TicketData[] = [];
  public fromDate: any;
  public TicketStatus: any;
  public claimantProviderList: any;
  public minRange: boolean = false;
  public maxRange: boolean = false;
  public loading: boolean = false;
  public arrayLength: any;
  public providersList: any;
  public reportTicketFilterStatus: any;
  public date = new Date();
  public noTickets: boolean = false;
  public loggerRole = this._localStorage.get('Role');

  constructor(
    public _newTripTicketReportService: NewTripTicketReportService,
    public _listService: ListService,
    public _notificationService: NotificationEmitterService,
    public _localStorage: LocalStorageService,
    public _confirmationService: ConfirmationService,
    public _tokenService: TokenService,
    public _router: Router
  ) {
    this.TicketStatus = [];
    this.claimantProviderList = [];
    this.TicketStatus = [{ value: '0', label: 'Aech' }];

    this._listService.queryStatus().subscribe({
      next: response => {
        const statusList: any = [];
        for (let i = 0, iLen = response.length; i < iLen; i++) {
          const value = response[i].statusId.toString();
          const label = response[i].type;
          statusList.push({
            value: value,
            label: label,
          });
        }
        this.TicketStatus = statusList;
      },
      error: error => {
        // Error handling
      },
    });
  }

  ngOnInit() {
    if (typeof this._tokenService.get() === 'undefined') {
      this._router.navigate(['/login']);
    }
    this._listService.queryProvider().subscribe({
      next: response => {
        this.providersList = response;
      },
      error: error => {
        // Error handling
      },
    });
    if (this.loggerRole === 'ROLE_PROVIDERADMIN') {
      this.getTicketList(this._localStorage.get('providerId'));
    }
  }

  // get from date
  getDate(providerPartnersId: any) {
    if (typeof providerPartnersId === 'undefined') {
      this.range.fromDate = null;
      this.range.toDate = null;
    } else {
      this.maxRange = false;
      this.reportTicketFilterStatus = [];
      this.getTicketList(providerPartnersId);
    }
  }

  // get list
  getTicketList(providerId: any) {
    this.loading = true;
    this._newTripTicketReportService.get(providerId).subscribe({
      next: response => {
        if (response.date === 'null') {
          this.fromDate = new Date();
          this.range.fromDate = moment(new Date()).format('YYYY-MM-DD T hh:mm:ss');
          this.noTickets = true;
        } else {
          this.fromDate = new Date(moment(response.date.slice(0, 17) + '00').format('LLLL'));
          this.range.fromDate = response.date.slice(0, 17) + '00';
        }
        this.range.toDate = moment(new Date()).format().slice(0, 19);
        this.range.reportTicketFilterStatus = [''];
        this.range.providerId = providerId;
        this._newTripTicketReportService.save(this.range).subscribe({
          next: data => {
            this.getList(data);
            this.range.toDate = null;
          },
          error: error => {
            this._notificationService.error('Error Message', error);
          },
        });
      },
      error: error => {
        this._notificationService.error('Error Message', error);
      },
    });
  }

  maxDate() {
    if (this.noTickets == false) {
      this.maxRange = true;
    } else {
      this._notificationService.error('Error Message', 'No tickets available for this provider');
    }
  }

  filter(toDate: any, reportTicketFilterStatus: any, providerPartnersId: any) {
    if (this.range.fromDate == null) {
      alert();
    } else {
      this.range.toDate = toDate;
      if (new Date(this.range.fromDate).getTime() <= new Date(this.range.toDate).getTime()) {
        if (typeof providerPartnersId === 'undefined') {
          this.range.providerId = this._localStorage.get('providerId');
        } else {
          this.range.providerId = providerPartnersId;
        }
        if (typeof reportTicketFilterStatus == 'undefined') {
          this.range.reportTicketFilterStatus = [''];
        } else {
          this.range.reportTicketFilterStatus = reportTicketFilterStatus;
        }
        const dateFrom = moment(this.fromDate).format().split('+');
        const dateTo = moment(this.range.toDate).format().split('+');
        this.range.fromDate = dateFrom[0];
        this.range.toDate = dateTo[0];
        this._newTripTicketReportService.filter(this.range).subscribe({
          next: response => {
            this.getList(response);
            this.minRange = false;
          },
          error: error => {
            // Error handling
          },
        });
      } else {
        this.minRange = true;
      }
    }
  }

  getList(data: any) {
    this.ticketList = data;
    if (typeof this.ticketList == 'undefined') {
      return;
    }

    this.arrayLength = this.ticketList.length;
    for (let i = 0; i < this.ticketList.length; i++) {
      this.ticketList[i]._customerName =
        this.ticketList[i].customer_first_name + ' ' + this.ticketList[i].customer_last_name;

      // Process trip claims
      this.claimantProviderList = [];
      for (let j = 0; j < this.ticketList[i].trip_Claims.length; j++) {
        if (this.ticketList[i].trip_Claims[j].claimant_provider_name) {
          this.claimantProviderList.push(this.ticketList[i].trip_Claims[j].claimant_provider_name);
        }
      }

      // Join claimant providers
      this.ticketList[i].claimantProviders = ' ';
      for (let j = 0; j < this.claimantProviderList.length; j++) {
        this.ticketList[i].claimantProviders =
          this.ticketList[i].claimantProviders + ', ' + this.claimantProviderList[j];
      }

      if (this.ticketList[i].claimantProviders == ' ') {
        this.ticketList[i].claimantProviders = ' No Claimants';
      } else {
        this.ticketList[i].claimantProviders = this.ticketList[i].claimantProviders.slice(
          2,
          this.ticketList[i].claimantProviders.length
        );
      }

      // Process addresses
      const pickup = this.ticketList[i].pickup_address;
      const dropoff = this.ticketList[i].drop_off_address;

      this.ticketList[i].pickup = pickup ?
        `${pickup.street1}, ${pickup.city}, ${pickup.state}, ${pickup.zipcode}` : '';

      this.ticketList[i].dropoff = dropoff ?
        `${dropoff.street1}, ${dropoff.city}, ${dropoff.state}, ${dropoff.zipcode}` : '';

      // Process dates and times
      if (this.ticketList[i].requested_pickup_date) {
        const pickupDate = moment(this.ticketList[i].requested_pickup_date);
        const pickupTime = moment(this.ticketList[i].requested_pickup_time, 'HH:mm:ss');

        this.ticketList[i].pickupDateTime = pickupDate.isValid() && pickupTime.isValid() ?
          `${pickupDate.format('MM/DD/YYYY')} ${pickupTime.format('hh:mm A')}` : '-';
      } else {
        this.ticketList[i].pickupDateTime = '-';

      }

      if (this.ticketList[i].requested_dropoff_date) {
        const dropoffDate = moment(this.ticketList[i].requested_dropoff_date);
        const dropoffTime = moment(this.ticketList[i].requested_dropoff_time, 'HH:mm:ss');
        this.ticketList[i].dropoffDateTime = dropoffDate.isValid() && dropoffTime.isValid() ?
          `${dropoffDate.format('MM/DD/YYYY')} ${dropoffTime.format('hh:mm A')}` : '-';
      } else {
        this.ticketList[i].dropoffDateTime = '-';
      }

      if (this.ticketList[i].created_at) {
        const createdAt = moment(this.ticketList[i].created_at);
        this.ticketList[i].submittedDateTime = createdAt.isValid() ?
          createdAt.format('MM/DD/YYYY hh:mm A') : '-';
      } else {
          this.ticketList[i].submittedDateTime = '-';
      }

      // Process trip details
      const details = [];

      if (this.ticketList[i].attendant_mobility_factors) {
        details.push(`Attendant Mobility Factors: ${this.ticketList[i].attendant_mobility_factors}`);
      }

      if (this.ticketList[i].guest_mobility_factors) {
        details.push(`Guest Mobility Factors: ${this.ticketList[i].guest_mobility_factors}`);
      }

      if (this.ticketList[i].guests !== null && this.ticketList[i].guests !== 0) {
        details.push(`Guests: ${this.ticketList[i].guests}`);
      }

      if (this.ticketList[i].customer_service_animals) {
        details.push(`Service Animals: ${this.ticketList[i].customer_service_animals}`);
      }

      if (this.ticketList[i].customer_seats_required) {
        details.push(`Seats Required: ${this.ticketList[i].customer_seats_required}`);
      }

      this.ticketList[i].tripDetails = details.join(', ') || ' ';
    }
    this.loading = false;
  }

  onFilter(event: Event, field: string) {
    const element = event.target as HTMLInputElement;
    this.dt.filter(element.value, field, 'contains');
  }

  lookupRowStyleClass(ticket: TicketData) {
    return {
      'row-expired': ticket.status.type === 'Expired',
      'row-completed': ticket.status.type === 'Completed',
      'row-pending': ticket.status.type === 'Pending',
    };
  }

  exportToExcel() {
    if (!this.ticketList || this.ticketList.length === 0) {
      this._notificationService.error('Error', 'No data available to export');
      return;
    }

    const exportData = this.ticketList.map(ticket => ({
      'Submitted Date': ticket.submittedDateTime,
      'Customer Name': ticket._customerName,
      'Claimant Provider': ticket.claimantProviders ? ticket.claimantProviders : '' ,
      'Pickup Date/Time': ticket.pickupDateTime,
      'Dropoff Date/Time': ticket.dropoffDateTime,
      'Pickup Address': ticket.pickup,
      'Dropoff Address': ticket.dropoff,
      'Status': ticket.status.type,
      'Trip Details': ticket.tripDetails
    }));

    const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(exportData);
    const workbook: XLSX.WorkBook = { Sheets: { 'data': worksheet }, SheetNames: ['data'] };
    const excelBuffer: any = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });

    this.saveAsExcelFile(excelBuffer, 'trip_tickets_report');
  }

  exportToCSV() {
    if (!this.ticketList || this.ticketList.length === 0) {
      this._notificationService.error('Error', 'No data available to export');
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
      'Status': string;
      'Trip Details': string;
    };

    const exportData: ExportRow[] = this.ticketList.map(ticket => ({
      'Submitted Date': ticket.submittedDateTime,
      'Customer Name': ticket._customerName,
      'Claimant Provider':  ticket.claimantProviders ? ticket.claimantProviders : '' ,
      'Pickup Date/Time': ticket.pickupDateTime,
      'Dropoff Date/Time': ticket.dropoffDateTime,
      'Pickup Address': ticket.pickup,
      'Dropoff Address': ticket.dropoff,
      'Status': ticket.status.type,
      'Trip Details': ticket.tripDetails
    }));

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
    link.setAttribute('download', 'trip_tickets_report.csv');
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

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
