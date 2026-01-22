import { Component } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { SetPasswordService } from './set-password.service';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { ActivateAccount } from '../activate-account';
import { NotificationEmitterService } from '../../shared/service/notification-emitter.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-login',
  templateUrl: './set-password.component.html',
  styleUrls: ['./set-password.component.scss'],
  providers: [SetPasswordService],
})
export class SetPasswordComponent {
  public activateAccount: ActivateAccount = new ActivateAccount();
  passwordMismatch: boolean = false;
  wrongPassword: boolean = false;
  maxlength: boolean = false;
  username = localStorage.getItem('addUserUsername');
  ngForm: FormGroup;
  public subscription: Subscription;

  constructor(
    public _router: Router,
    public _setPasswordService: SetPasswordService,
    fb: FormBuilder,
    public _activatedRoute: ActivatedRoute,
    public _notificationService: NotificationEmitterService
  ) {
    this.ngForm = fb.group({
      Password: [null, Validators.compose([Validators.required])],
    });
  }

  maxLength() {
    this.passwordMismatch = false;
    this.maxlength = true;
    this.wrongPassword = false;
  }

  removeText() {
    this.maxlength = false;
  }

  onSubmit(form: FormGroup) {
    event.preventDefault();
    if (this.activateAccount.new_password == this.activateAccount.confirm_password) {
      this._setPasswordService.resetPassword(this.activateAccount, this.username).subscribe(
        response => {
          this.passwordMismatch = false;
          this.wrongPassword = false;
          this.maxlength = false;
          this._notificationService.success(
            'Success Message',
            'Your Password has been changed successfully'
          );
          this._router.navigate(['/login']);
        },
        error => {
          this.passwordMismatch = false;
          this.maxlength = false;
          this.wrongPassword = true;
          this.activateAccount.new_password = '';
          this.activateAccount.confirm_password = '';
        }
      );
    } else {
      this.wrongPassword = false;
      this.maxlength = false;
      this.passwordMismatch = true;
    }
  }
}
