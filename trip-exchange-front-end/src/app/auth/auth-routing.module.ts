import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { LoginComponent } from '../login/login.component';
import { ForgotPasswordComponent } from '../forgot-password/forgot-password.component';
import { ChangePasswordComponent } from '../change-password/change-password.component';
import { ChangePasswordAfterLoginComponent } from '../change-password-after-login/change-password-after-login.component';
import { ActivateAccountComponent } from '../activate-account/activate-account.component';
import { SetPasswordComponent } from '../activate-account/set-password/set-password.component';
import { ResetPasswordComponent } from '../reset-password/reset-password.component';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'forgotPassword', component: ForgotPasswordComponent },
  { path: 'reset-password', component: ResetPasswordComponent },
  { path: 'changePassword', component: ChangePasswordComponent },
  { path: 'changePasswordAfterLogin', component: ChangePasswordAfterLoginComponent },
  { path: 'activateAccount', component: ActivateAccountComponent },
  { path: 'setPasswordComponent', component: SetPasswordComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AuthRoutingModule {}
