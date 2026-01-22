import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { WorkingHour, WorkingHourModel } from './working-hour';
import { WorkingHourService } from './working-hour.service';
import { NotificationEmitterService } from '../../shared/service/notification-emitter.service';
import { LocalStorageService } from '../../shared/service/local-storage.service';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-working-hour',
  templateUrl: './working-hour.component.html',
  styleUrls: ['./working-hour.component.scss'],
})
export class WorkingHourComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  workingHourForm: FormGroup;
  workingHours: WorkingHour[] = []; // Initialize workingHours as an empty array to prevent null or undefined errors
  currentWorkingHour: WorkingHour;
  providerId: number;
  providerName: string;
  isEditMode = false;
  daysOfWeek = [
    { value: 'Monday', label: 'WORKING_HOURS.DAYS.MONDAY' },
    { value: 'Tuesday', label: 'WORKING_HOURS.DAYS.TUESDAY' },
    { value: 'Wednesday', label: 'WORKING_HOURS.DAYS.WEDNESDAY' },
    { value: 'Thursday', label: 'WORKING_HOURS.DAYS.THURSDAY' },
    { value: 'Friday', label: 'WORKING_HOURS.DAYS.FRIDAY' },
    { value: 'Saturday', label: 'WORKING_HOURS.DAYS.SATURDAY' },
    { value: 'Sunday', label: 'WORKING_HOURS.DAYS.SUNDAY' },
  ];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private formBuilder: FormBuilder,
    private workingHourService: WorkingHourService,
    private notificationService: NotificationEmitterService,
    private localStorage: LocalStorageService,
    private translateService: TranslateService
  ) {}

  ngOnInit(): void {
    this.initForm();

    this.route.paramMap.pipe(takeUntil(this.destroy$)).subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.providerId = +id;
        this.loadProviderName();
        this.loadWorkingHours();
      } else {
        // No provider ID provided, redirect to providers list with a message
        this.notificationService.info(
          this.translateService.instant('COMMON.INFO'),
          this.translateService.instant('WORKING_HOURS.SELECT_PROVIDER_MESSAGE')
        );
        this.router.navigate(['/admin/working-hour']);
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Load working hours for the selected provider
   */
  private loadWorkingHours(): void {
    this.workingHourService
      .getByProvider(this.providerId)
      .pipe(takeUntil(this.destroy$))
      .subscribe(
        hours => {
          this.workingHours = hours;
        },
        error => {
          console.error('Error loading working hours:', error);
          this.notificationService.error(
            this.translateService.instant('COMMON.ERROR'),
            this.translateService.instant('WORKING_HOURS.LOAD_ERROR')
          );
        }
      );
  }

  /**
   * Get provider name from localStorage based on user role
   */
  private loadProviderName(): void {
    const userRole = this.localStorage.get('Role');

    if (userRole === 'ROLE_ADMIN') {
      // For admin, check if this provider is the selected one
      const adminChoiceProvider = this.localStorage.get('adminChoiceProvider');

      if (adminChoiceProvider && +adminChoiceProvider === this.providerId) {
        this.providerName = this.localStorage.get('adminChoiceProviderName');
      }
    } else if (userRole === 'ROLE_PROVIDERADMIN') {
      // For provider admin, use the current provider info
      this.providerName = this.localStorage.get('providerName');
    }

    // If still no provider name, use a generic one
    if (!this.providerName) {
      this.providerName = this.translateService.instant('WORKING_HOURS.PROVIDER');
    }
  }

  /**
   * Initialize the form
   */
  private initForm(): void {
    this.workingHourForm = this.formBuilder.group({
      workingHoursId: [0],
      providerId: [this.providerId],
      day: ['', Validators.required],
      startTime: [
        '',
        [Validators.required, Validators.pattern(/^([01]?[0-9]|2[0-3]):[0-5][0-9]$/)],
      ],
      endTime: ['', [Validators.required, Validators.pattern(/^([01]?[0-9]|2[0-3]):[0-5][0-9]$/)]],
      isHoliday: [false],
      isActive: [true],
    });
  }

  /**
   * Add new working hour
   */
  addWorkingHour(): void {
    console.log('addWorkingHour called');
    this.isEditMode = true; // Ensure isEditMode is set to true to display the form
    console.log('isEditMode set to:', this.isEditMode);
    this.currentWorkingHour = new WorkingHourModel();
    this.workingHourForm.reset({
      workingHoursId: 0,
      providerId: this.providerId,
      isHoliday: false,
      isActive: true,
    });
    console.log('Form reset with default values:', this.workingHourForm.value);
  }

  /**
   * Edit existing working hour
   */
  editWorkingHour(workingHour: WorkingHour): void {
    this.isEditMode = true;
    this.currentWorkingHour = { ...workingHour };
    this.workingHourForm.patchValue(workingHour);
  }

  /**
   * Save working hour (create or update)
   */
  saveWorkingHour(): void {
    if (this.workingHourForm.invalid) {
      Object.keys(this.workingHourForm.controls).forEach(key => {
        this.workingHourForm.get(key)?.markAsTouched();
      });
      return;
    }

    const workingHourData = this.workingHourForm.value;
    workingHourData.providerId = this.providerId;

    // Ensure startTime and endTime are in HH:MM:SS format
    ['startTime', 'endTime'].forEach(field => {
      if (/^\d{2}:\d{2}$/.test(workingHourData[field])) {
        workingHourData[field] = `${workingHourData[field]}:00`;
      }
    });

    if (workingHourData.workingHoursId && workingHourData.workingHoursId !== 0) {
      // Update existing working hour
      this.workingHourService
        .update(this.providerId, workingHourData)
        .pipe(takeUntil(this.destroy$))
        .subscribe(
          updatedHour => {
            this.notificationService.success(
              this.translateService.instant('COMMON.SUCCESS'),
              this.translateService.instant('WORKING_HOURS.UPDATE_SUCCESS')
            );
            this.loadWorkingHours();
            this.cancelEdit();
          },
          error => {
            console.error('Error updating working hour:', error);
            this.notificationService.error(
              this.translateService.instant('COMMON.ERROR'),
              this.translateService.instant('WORKING_HOURS.UPDATE_ERROR')
            );
          }
        );
    } else {
      // Create new working hour
      this.workingHourService
        .create(workingHourData)
        .pipe(takeUntil(this.destroy$))
        .subscribe(
          newHour => {
            this.notificationService.success(
              this.translateService.instant('COMMON.SUCCESS'),
              this.translateService.instant('WORKING_HOURS.CREATE_SUCCESS')
            );
            this.loadWorkingHours();
            this.cancelEdit();
          },
          error => {
            console.error('Error creating working hour:', error);
            this.notificationService.error(
              this.translateService.instant('COMMON.ERROR'),
              this.translateService.instant('WORKING_HOURS.CREATE_ERROR')
            );
          }
        );
    }
  }

  /**
   * Toggle active status of a working hour
   */
  toggleActive(workingHour: WorkingHour): void {
    const newStatus = !workingHour.isActive;

    this.workingHourService
      .setActive(workingHour.workingHoursId, newStatus)
      .pipe(takeUntil(this.destroy$))
      .subscribe(
        () => {
          workingHour.isActive = newStatus;
          const messageKey = newStatus ? 'WORKING_HOURS.ACTIVATED' : 'WORKING_HOURS.DEACTIVATED';
          this.notificationService.success(
            this.translateService.instant('COMMON.SUCCESS'),
            this.translateService.instant(messageKey)
          );
        },
        error => {
          console.error('Error toggling working hour status:', error);
          this.notificationService.error(
            this.translateService.instant('COMMON.ERROR'),
            this.translateService.instant('WORKING_HOURS.STATUS_ERROR')
          );
        }
      );
  }

  /**
   * Delete a working hour
   */
  deleteWorkingHour(workingHour: WorkingHour): void {
    if (confirm(this.translateService.instant('WORKING_HOURS.DELETE_CONFIRM'))) {
      this.workingHourService
        .delete(workingHour.workingHoursId)
        .pipe(takeUntil(this.destroy$))
        .subscribe(
          () => {
            this.notificationService.success(
              this.translateService.instant('COMMON.SUCCESS'),
              this.translateService.instant('WORKING_HOURS.DELETE_SUCCESS')
            );
            this.loadWorkingHours();
          },
          error => {
            console.error('Error deleting working hour:', error);
            this.notificationService.error(
              this.translateService.instant('COMMON.ERROR'),
              this.translateService.instant('WORKING_HOURS.DELETE_ERROR')
            );
          }
        );
    }
  }

  /**
   * Cancel form and return to list view
   */
  cancelEdit(): void {
    this.isEditMode = false;
    this.workingHourForm.reset({
      workingHoursId: 0,
      providerId: this.providerId,
      isHoliday: false,
      isActive: true,
    });
  }

  /**
   * Navigate back to providers list
   */
  goBack(): void {
    this.router.navigate(['/admin/providers']);
  }

  /**
   * Format time string to HH:MM format
   */
  formatTime(timeString: string): string {
    if (!timeString) return '';

    // If time is already in HH:MM format, return it
    if (/^([01]?[0-9]|2[0-3]):[0-5][0-9]$/.test(timeString)) {
      return timeString;
    }

    // Otherwise try to parse and format
    try {
      const [hours, minutes] = timeString.split(':');
      return `${hours.padStart(2, '0')}:${minutes.padStart(2, '0')}`;
    } catch (e) {
      return timeString;
    }
  }

  /**
   * Get translated day name
   */
  getDayName(dayKey: string): string {
    const day = this.daysOfWeek.find(d => d.value === dayKey);
    return day ? this.translateService.instant(day.label) : dayKey;
  }
}
