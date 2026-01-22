import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { ApplicationSettingService } from './application-settings.service';
import { TokenService } from '../shared/service/token.service';
import { NotificationEmitterService } from '../shared/service/notification-emitter.service';
import { HeaderEmitterService } from '../shared/service/header-emmiter.service';

interface ApplicationSettings {
  applicationSettingId: number;
  Email: string;
  Password: string;
  passwrodExpiredAfterDays: number;
  claimApprovalTimeInHours: number;
}

@Component({
  selector: 'app-application-settings',
  templateUrl: 'application-settings.component.html',
  styleUrls: ['application-settings.component.scss'],
  providers: [ApplicationSettingService],
})
export class ApplicationSettingsComponent implements OnInit {
  public applications: ApplicationSettings;
  public userName = false;
  public daysZero = false;
  public settingsForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private applicationSettingService: ApplicationSettingService,
    private notificationService: NotificationEmitterService,
    private router: Router,
    private headerEmitter: HeaderEmitterService,
    private tokenService: TokenService
  ) {
    this.settingsForm = this.fb.group({
      Email: ['', [Validators.required, Validators.email]],
      Password: ['', [Validators.required, Validators.minLength(4)]],
      passwrodExpiredAfterDays: ['', [Validators.required, Validators.min(0)]],
      claimApprovalTimeInHours: ['', [Validators.required, Validators.min(0)]],
    });
  }

  ngOnInit(): void {
    if (!this.tokenService.get()) {
      this.router.navigate(['/login']);
      return;
    }

    this.headerEmitter.header.emit((this.userName = true));
    this.getApplicationSettings();
  }

  getApplicationSettings(): void {
    this.applicationSettingService.query().subscribe({
      next: (response: ApplicationSettings) => {
        this.applications = response;
        this.settingsForm.patchValue({
          Email: response.Email,
          Password: response.Password,
          passwrodExpiredAfterDays: response.passwrodExpiredAfterDays,
          claimApprovalTimeInHours: response.claimApprovalTimeInHours,
        });
      },
      error: error => {
        this.notificationService.error('Error', error);
      },
    });
  }

  addEditSettings(): void {
    if (this.settingsForm.invalid) {
      return;
    }

    const formValue = this.settingsForm.value;
    const updatedSettings = {
      ...this.applications,
      ...formValue,
    };

    this.applicationSettingService
      .update(updatedSettings.applicationSettingId, updatedSettings)
      .subscribe({
        next: () => {
          this.notificationService.success('Success Message', 'Settings updated successfully.');
        },
        error: error => {
          this.notificationService.error('Error', error);
        },
      });
  }

  cancelEdit(): void {
    this.router.navigate(['/tripTicket']);
  }

  changeClaimApprovalTimeInHours(hours: number): void {
    this.daysZero = hours === 0;
  }

  // Helper methods for template
  get emailControl() {
    return this.settingsForm.get('Email');
  }
  get passwordControl() {
    return this.settingsForm.get('Password');
  }
  get daysControl() {
    return this.settingsForm.get('passwrodExpiredAfterDays');
  }
  get hoursControl() {
    return this.settingsForm.get('claimApprovalTimeInHours');
  }
}
