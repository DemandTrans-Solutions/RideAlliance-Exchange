import { Component } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { ActivateAccountService } from './activate-account.service';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { ActivateAccount } from './activate-account';
import { TokenService } from '../shared/service/token.service';
import { NotificationEmitterService } from '../shared/service/notification-emitter.service';
import { Subscription } from 'rxjs';
import { LocalStorageService } from '../shared/service/local-storage.service';

@Component({
  selector: 'app-login',
  templateUrl: './activate-account.component.html',
  styleUrls: ['./activate-account.component.scss'],
  providers: [ActivateAccountService],
})
export class ActivateAccountComponent {
  public activateAccount: ActivateAccount = new ActivateAccount();
  public username: any;
  public ngForm: FormGroup;
  public subscription: Subscription;

  constructor(
    public _router: Router,
    public _activateAccountService: ActivateAccountService,
    public fb: FormBuilder,
    public _activatedRoute: ActivatedRoute,
    public _tokenService: TokenService,
    public _notificationService: NotificationEmitterService,
    public _localStorage: LocalStorageService
  ) {
    this.ngForm = fb.group({
      Text: [null, Validators.compose([Validators.required])],
      Password: [null, Validators.compose([Validators.required])],
    });
  }

  onSubmit(form: FormGroup) {
    this._tokenService.clearAll();
    event.preventDefault();
    console.log('activateAccount', this.activateAccount);
    this._activateAccountService.activateAccount(this.activateAccount).subscribe(
      response => {
        if (response.value == 'invalidUsernameOrPassword') {
          this._notificationService.error(
            'Error Message',
            'Please enter correct username or password'
          );
        } else if (response.value == 'userAlredyActivatedWithoutSetPassword') {
          this._notificationService.error(
            'Error Message',
            'Your account is already activated please set the password'
          );
          this._router.navigate(['/setPasswordComponent']);
        } else if (response.value == 'userAlredyActivatedWithSetPassword') {
          this._notificationService.error('Error Message', 'Your account is already activated');
          this._router.navigate(['/login']);
        } else if (response.value == 'accountEnabled') {
          this._localStorage.set('addUserUsername', this.activateAccount.username);
          this._notificationService.success('Success Message', 'Your Account has been Activated');
          this._router.navigate(['/setPasswordComponent']);
        }
      },
      error => {
        this._notificationService.error('Error Message', error);
        this.activateAccount.temporaryPassword = '';
        this.activateAccount.new_password = '';
      }
    );
  }
}
