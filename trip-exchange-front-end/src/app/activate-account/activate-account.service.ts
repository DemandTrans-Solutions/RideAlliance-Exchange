import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { ActivateAccount } from './activate-account';
import { Router } from '@angular/router';
import { ConstantService } from '../shared/service/constant-service';
import { SharedHttpClientService } from '../shared/service/shared-http-client.service';

@Injectable({
  providedIn: 'root',
})
export class ActivateAccountService {
  constructor(
    private _router: Router,
    private _http: HttpClient,
    private _constantService: ConstantService,
    private _sharedHttpClientService: SharedHttpClientService
  ) {}
  private webServiceUrl = this._constantService.WEBSERVICE_URL + 'activateAccount';
  activateAccount(activateAccount: ActivateAccount): Observable<any> {
    const headers = new HttpHeaders().set('Content-Type', 'application/json');
    const user = {
      username: activateAccount.username,
      password: activateAccount.temporaryPassword,
      currentLoginIp: ' ',
      email: activateAccount.username,
      id: 0,
      isActive: true,
      isNotifyClaimedTicketExpired: true,
      isNotifyClaimedTicketRescinded: true,
      isNotifyNewTripClaimAutoApproved: true,
      isNotifyNewTripClaimAwaitingApproval: true,
      isNotifyPartnerCreatesTicket: true,
      isNotifyPartnerUpdateTicket: true,
      isNotifyTripClaimApproved: true,
      isNotifyTripClaimDeclined: true,
      isNotifyTripClaimRescinded: true,
      isNotifyTripCommentAdded: true,
      jobTitle: ' ',
      lastLoginIp: ' ',
      name: ' ',
      phoneNumber: ' ',
      providerId: 0,
      providerName: ' ',
      isNotifyTripResultSubmitted: true,
      userRole: ' ',
    };
    console.log('activateAccount', user);
    return this._http
      .post(this.webServiceUrl, user, { headers, withCredentials: true })
      .pipe(map(this._constantService.extractData), catchError(this._constantService.handleError));
  }
}
