import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ConstantService } from '../shared/service/constant-service';
import { Logger } from '../shared/service/default-log.service';
import { ProfileService } from './profile.service';
import { TokenService } from '../shared/service/token.service';
import { LocalStorageService } from '../shared/service/local-storage.service';
import { NotificationEmitterService } from '../shared/service/notification-emitter.service';
import { HeaderEmitterService } from '../shared/service/header-emmiter.service';
declare let google: any;

@Component({
  selector: 'profile',
  templateUrl: 'profile.component.html',
  styleUrls: ['profile.component.scss'],
  providers: [ProfileService],
})
export class ProfileComponent implements OnInit {
  public name: any;
  public email: any;
  public role: any;
  public title: any;
  public myPolygon: any;
  public userName: boolean = false;
  public showApiKey: boolean;
  public token: any;
  public tokenButton: boolean;
  public api: any =
    'eyJpZCI6MTIsInVzZXJuYW1lIjoic25laGEua290YXdhZGVAemNvbnNvbHV0aW9ucy5jb20iLCJleHBpcmVzIjo1MDE2NiwiY3NyZlRva2VuIjoiNzhkODkwMjItYjZkOS00YTRjLTgwMzEtOTJhYjliMWMxNzRiIiwiYWNjb3VudExvY2tlZCI6ZmFsc2UsImFjY291bnREaXNhYmxlZCI6ZmFsc2UsImFjY291bnRFeHBpcmVkIjpmYWxzZSwiY3JlZGVudGlhbHNFeHBpcmVkIjpmYWxzZSwiYXV0aG9yaXRpZXMiOlt7ImF1dGhvcml0eSI6IlJPTEVfUFJPVklERVJBRE1JTiJ9XSwiYXV0aGFudGljYXRpb25UeXBlSXNBZGFwdGVyIjp0cnVlLCJyZXNwb25zZURhdGFGb3JVSSI6IjQiLCJqb2JUaXRsZSI6InVzZXIiLCJuYW1lIjoiU25laGEiLCJpc1Bhc3N3b3JkRXhwaXJlZCI6ZmFsc2UsImZhaWxlZEF0dGVtcHRzIjowLCJsYXN0RmFpbGVkQXR0ZW1wdERhdGUiOiIyMDE3LTA1LTI2In0';
  constructor(
    public _profileService: ProfileService,
    public _localStorage: LocalStorageService,
    public _notificationService: NotificationEmitterService,
    public _router: Router,
    public _headerEmitter: HeaderEmitterService,
    public _tokenService: TokenService
  ) {}

  public ngOnInit() {
    if (typeof this._tokenService.get() === 'undefined') {
      this._router.navigate(['/login']);
    }
    this._headerEmitter.header.emit((this.userName = true));
    this.name = this._localStorage.get('name');
    this.email = this._localStorage.get('username');
    this.role = this._localStorage.get('Role');
    this.title = this._localStorage.get('title');
    if (this.role === 'ROLE_PROVIDERADMIN') {
      this.role = 'Provider Admin';
      this.showApiKey = true;
    } else if (this.role === 'ROLE_ADMIN') {
      this.role = 'Site Admin';
      this.showApiKey = false;
    }
    this._profileService.get(this._localStorage.get('userId')).subscribe(
      response => {
        this.token = response.userToken;
        this.tokenButton = false;
      },
      error => {
        this.tokenButton = true;
      }
    );
  }

  public cancel() {
    const isPasswordExpired: any = this._localStorage.get('isPasswordExpired');
    if (isPasswordExpired === 'false') {
      this._router.navigate(['/tripTicket']);
    } else {
      this._router.navigate(['/changePasswordAfterLogin']);
    }
  }

  public getToken() {
    this._profileService.getToken(this._localStorage.get('userId')).subscribe(
      response => {
        this.token = response.value;
        this.tokenButton = false;
      },
      error => {}
    );
  }
}
