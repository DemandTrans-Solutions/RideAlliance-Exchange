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
  { path: 'login', component: LoginComponent, title: 'Login' },
  { path: 'forgotPassword', component: ForgotPasswordComponent, title: 'Forgot Password' },
  { path: 'reset-password', component: ResetPasswordComponent, title: 'Reset Password' },
  { path: 'changePassword', component: ChangePasswordComponent, title: 'Change Password' },
  {
    path: 'changePasswordAfterLogin',
    component: ChangePasswordAfterLoginComponent,
    title: 'Change Password After Login',
  },
  { path: 'activateAccount', component: ActivateAccountComponent, title: 'Activate Account' },
  { path: 'setPasswordComponent', component: SetPasswordComponent, title: 'Set Password' },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AuthRoutingModule {}
