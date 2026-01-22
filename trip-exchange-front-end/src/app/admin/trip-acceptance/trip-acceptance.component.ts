import { Component, OnInit, OnDestroy } from '@angular/core';
import { NgForm } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { NotificationEmitterService } from '../../shared/service/notification-emitter.service';
import { LocalStorageService } from '../../shared/service/local-storage.service';
import { LoginService } from '../../login/login.service';
import { TripAcceptanceService } from './trip-acceptance.service';
import { FundSourceService } from '../fund-source/fund.service';
import { TripAcceptanceCriteria } from './trip-acceptance.model';

@Component({
  selector: 'app-trip-acceptance',
  templateUrl: './trip-acceptance.component.html',
  styleUrls: ['./trip-acceptance.component.scss'],
})
export class TripAcceptanceComponent implements OnInit, OnDestroy {
  // Available weekdays
  weekDays: string[] = [
    'Monday',
    'Tuesday',
    'Wednesday',
    'Thursday',
    'Friday',
    'Saturday',
    'Sunday',
  ];

  // Model for the form
  criteria: TripAcceptanceCriteria = {
    futureDays: 14,
    dayOfWeek: '',
    timeOfDay: '12:00',
    fundingSourceId: null,
  };

  // Data sources
  fundingSources: any[] = [];
  existingCriteria: TripAcceptanceCriteria[] = [];

  // User role
  isProviderAdmin: boolean = false;

  // Subject for unsubscribe notifications
  private destroy$ = new Subject<void>();

  constructor(
    private tripAcceptanceService: TripAcceptanceService,
    private fundSourceService: FundSourceService,
    private authService: LoginService,
    private notificationService: NotificationEmitterService,
    private translateService: TranslateService,
    private _localStorage: LocalStorageService
  ) {}

  ngOnInit(): void {
    this.checkUserRole();
    this.loadFundingSources();
    this.loadExistingCriteria();
  }

  ngOnDestroy(): void {
    // Clean up subscriptions
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Check if current user has provider admin role
   */
  checkUserRole(): void {
    const userRole = this._localStorage.get('Role');
    this.isProviderAdmin = userRole === 'ROLE_PROVIDERADMIN' || userRole === 'ROLE_ADMIN';
  }

  /**
   * Load funding sources from service
   */
  loadFundingSources(): void {
    this.fundSourceService
      .getAllFundSources()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: data => {
          this.fundingSources = data;
        },
        error: err => {
          console.error('Error loading funding sources', err);
          this.notificationService.error(
            this.translateService.instant('COMMON.ERROR'),
            this.translateService.instant('COMMON.ERROR_LOADING_DATA')
          );
        },
      });
  }

  /**
   * Load existing trip acceptance criteria
   */
  loadExistingCriteria(): void {
    this.tripAcceptanceService
      .getTripAcceptanceCriteria()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: data => {
          this.existingCriteria = data;
        },
        error: err => {
          console.error('Error loading existing criteria', err);
          this.notificationService.error(
            this.translateService.instant('COMMON.ERROR'),
            this.translateService.instant('COMMON.ERROR_LOADING_DATA')
          );
        },
      });
  }

  /**
   * Save trip acceptance criteria
   */
  saveTripAcceptanceCriteria(form: NgForm): void {
    if (form.invalid) {
      return;
    }

    this.tripAcceptanceService
      .createTripAcceptanceCriteria(this.criteria)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.notificationService.success(
            this.translateService.instant('COMMON.SUCCESS'),
            this.translateService.instant('ADMIN.TRIP_ACCEPTANCE.SAVED_SUCCESS')
          );
          // Reset form and reload data
          form.resetForm();
          this.criteria = {
            futureDays: 14,
            dayOfWeek: '',
            timeOfDay: '12:00',
            fundingSourceId: null,
          };
          this.loadExistingCriteria();
        },
        error: err => {
          console.error('Error saving criteria', err);
          this.notificationService.error(
            this.translateService.instant('COMMON.ERROR'),
            this.translateService.instant('ADMIN.TRIP_ACCEPTANCE.SAVE_ERROR')
          );
        },
      });
  }
}
