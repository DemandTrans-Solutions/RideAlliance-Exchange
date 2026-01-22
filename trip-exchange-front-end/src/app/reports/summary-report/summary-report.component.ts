import { Component, OnInit, EventEmitter, Input } from '@angular/core';
import { Router } from '@angular/router';
import { Message } from 'primeng/api';
import { ConfirmationService } from 'primeng/api';
import { SummaryReportService } from './summary-report.service';
import { ListService } from '../../shared/service/list.service';
import { NotificationEmitterService } from '../../shared/service/notification-emitter.service';
import { LocalStorageService } from '../../shared/service/local-storage.service';
import { TokenService } from '../../shared/service/token.service';
import { Range } from './summary-report';
import { SummaryReport } from './summary-report';
import moment from 'moment';

@Component({
  selector: 'summary-report',
  templateUrl: 'summary-report.component.html',
  styleUrls: ['summary-report.component.scss'],
  providers: [SummaryReportService, ListService],
})
export class SummaryReportComponent implements OnInit {
  @Input() range: Range = new Range();
  @Input() summaryReport: SummaryReport = new SummaryReport();
  public minDateTime: any;
  public providersList: any;
  public fromDate: any;
  public toDate: any;
  public providerPartnersId: any;
  public PleaseSelect: any = 'pleaseSelect';
  public minRange: boolean = false;
  public maxRange: boolean = false;
  public displayRecords: boolean = false;
  public loggerRole = this._localStorage.get('Role');
  public noTickets: boolean = false;
  public loading: boolean = false;

  constructor(
    public _summaryReportService: SummaryReportService,
    public _listService: ListService,
    public _notificationService: NotificationEmitterService,
    public _localStorage: LocalStorageService,
    public _confirmationService: ConfirmationService,
    public _router: Router,
    public _tokenService: TokenService
  ) {}

  ngOnInit() {
    if (typeof this._tokenService.get() == 'undefined') {
      this._router.navigate(['/login']);
    }
    this._listService.queryProvider().subscribe(
      response => {
        this.providersList = response;
      },
      error => {}
    );
    if (this.loggerRole == 'ROLE_PROVIDERADMIN') {
      this.getMinimumDate(this._localStorage.get('providerId'));
    }
  }

  getMinimumDate(providerId) {
    this._summaryReportService.get(providerId).subscribe(
      response => {
        this.range.toDate = null;
        if (response.date == 'null') {
          this.fromDate = new Date();
          this.noTickets = true;
        } else {
          this.noTickets = false;
          this.range.fromDate = new Date(moment(response.date.slice(0, 17) + '00').format('llll'));
          this.fromDate = new Date(moment(response.date.slice(0, 17) + '00').format('LLLL'));
        }
      },
      error => {}
    );
  }

  maxDate() {
    if (this.noTickets === false) {
      this.maxRange = true;
    } else {
      this._notificationService.error('Error Message', 'No tickets available for this provider');
    }
  }
  // get the date
  getDate(providerPartnersId) {
    if (typeof providerPartnersId === 'undefined') {
      this.range.fromDate = null;
      this.range.toDate = null;
    } else {
      this.maxRange = false;
      this.getMinimumDate(providerPartnersId);
    }
  }

  // get the list
  filter(toDate, providerPartnersId) {
    this.range.toDate = toDate;
    if (new Date(this.range.fromDate).getTime() <= new Date(this.range.toDate).getTime()) {
      this.loading = true;
      if (typeof providerPartnersId === 'undefined') {
        this.range.providerId = this._localStorage.get('providerId');
      } else {
        this.range.providerId = providerPartnersId;
      }
      const dateTo = moment(this.range.toDate).format().split('+');
      this.range.toDate = new Date(dateTo[0]);
      const dateFrom = moment(this.fromDate).format().split('+');
      this.range.fromDate = new Date(dateFrom[0]);
      this.range.reportTicketFilterStatus = [''];
      const temp = {
        fromDate: dateFrom[0],
        toDate: dateTo[0],
        reportTicketFilterStatus: this.range.reportTicketFilterStatus,
        providerId: this.range.providerId,
      };
      this._summaryReportService.save(temp).subscribe(
        response => {
          this.minRange = false;
          this.displayRecords = true;
          this.summaryReport = response;
          this.loading = false;
        },
        error => {
          this._notificationService.error('Error Message', error);
          this.loading = false;
        }
      );
    } else {
      this.minRange = true;
    }
  }
}
