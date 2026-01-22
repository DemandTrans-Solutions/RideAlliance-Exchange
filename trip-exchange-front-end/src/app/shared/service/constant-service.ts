import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { Logger } from './default-log.service';
import { TokenService } from './token.service';
import { NotificationEmitterService } from './notification-emitter.service';
import { ConfirmationService } from 'primeng/api';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class ConstantService {
  constructor(
    private router: Router,
    private _logger: Logger,
    private _tokenService: TokenService,
    private _notificationService: NotificationEmitterService,
    private _confirmationService: ConfirmationService
  ) {}


  // API URL configuration from environment
  public WEBSERVICE_URL: string = environment.apiUrl;

  // OAuth client credentials
  public OAUTH2_CLIENT_USERNAME: string = 'springbootproj-trusted-client';
  public OAUTH2_CLIENT_PASSWORD: string = 'secret';

  // Error messages
  public MESSAGE_ACCESSTOKEN_EXPIRED: string =
    'Looks like your access token has been expired. Please login.';
  public MESSAGE_INTERNAL_SERVER_ERROR: string =
    'Some internal server error has occurred. Please try again. If problem persists then please contact Administrator.';
  public MESSAGE_NO_RECORDFOUND_ERROR: string = 'No records to display.';
  public MESSAGE_DUBLICATESFOUND_ERROR_FOR_PROVIDER: string = 'Email id already exist.';
  public MESSAGE_DUBLICATESFOUND_ERROR_FOR_USER: string = 'Username/Email already exist.';

  public handleError = (error: HttpErrorResponse) => {
    if (error.status === 0) {
      this._notificationService.error('Error Message', this.MESSAGE_ACCESSTOKEN_EXPIRED);
      this.router.navigate(['/login']);
    } else if (error.status === 500) {
      this._notificationService.error('Error Message', this.MESSAGE_INTERNAL_SERVER_ERROR);
    } else if (error.status === 409) {
      this._notificationService.error(
        'Error Message',
        this.MESSAGE_DUBLICATESFOUND_ERROR_FOR_PROVIDER
      );
      return throwError(() => new Error(this.MESSAGE_DUBLICATESFOUND_ERROR_FOR_PROVIDER));
    } else if (error.status === 410) {
      this._confirmationService.confirm({
        message:
          'You cannot create the claim as your organization is not associated with the service area that the trip is in. Do you want to add this service area for your organization?"',
        accept: () => {
          this.router.navigate(['/admin/serviceArea']);
        },
        reject: () => {
          // Do nothing on reject
        },
      });
    } else if (error.status === 411) {
      this._notificationService.error('Error Message', this.MESSAGE_DUBLICATESFOUND_ERROR_FOR_USER);
      return throwError(() => new Error(this.MESSAGE_DUBLICATESFOUND_ERROR_FOR_USER));
    }

    // For other status codes
    const errorMessage = error.error?.error || 'Server error';
    return throwError(() => new Error(errorMessage));
  };

  public extractData = (res: any) => {
    if (!res) {
      return {};
    }
    return res || {};
  };
}
