import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { User } from '../login/user';
import { Router } from '@angular/router';
import { ConstantService } from '../shared/service/constant-service';
import { SharedHttpClientService } from '../shared/service/shared-http-client.service';

@Injectable({
  providedIn: 'root',
})
export class ChangePasswordAfterLoginService {
  constructor(
    private _router: Router,
    private _http: HttpClient,
    private _constantService: ConstantService,
    private _sharedHttpClientService: SharedHttpClientService
  ) {}

  private webServiceUrl =
    this._constantService.WEBSERVICE_URL + 'forgotCrendential/resetForgotPassword';

  resetPassword(user: User, username): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json');

    return this._http
      .post(
        this.webServiceUrl,
        {
          email: ' ',
          newPassword: user.new_password,
          oldPassword: user.old_password,
          username: username,
          tempPassword: ' ',
        },
        { headers, withCredentials: true }
      )
      .pipe(map(this.extractData), catchError(this._sharedHttpClientService.handleError));
  }

  public extractData = function (res: any) {
    if (res.status === 204) {
      // Empty response
      return {};
    } else {
      return res;
    }
  }.bind(this);
}
