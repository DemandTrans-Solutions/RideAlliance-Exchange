import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { ActivateAccount } from '../activate-account';
import { Router } from '@angular/router';
import { ConstantService } from '../../shared/service/constant-service';
import { SharedHttpClientService } from '../../shared/service/shared-http-client.service';

@Injectable({
  providedIn: 'root',
})
export class SetPasswordService {
  constructor(
    private _router: Router,
    private _http: HttpClient,
    private _constantService: ConstantService,
    private _sharedHttpClientService: SharedHttpClientService
  ) {}

  private webServiceUrl = this._constantService.WEBSERVICE_URL + 'changePassword';

  resetPassword(activateAccount: ActivateAccount, username): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    return this._http
      .post(
        this.webServiceUrl,
        { password: activateAccount.new_password, username: username },
        { headers, withCredentials: true }
      )
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }
}
