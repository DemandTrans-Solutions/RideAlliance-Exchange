import { NgModule } from '@angular/core';
import { ToastModule } from 'primeng/toast';
import { FormsModule } from '@angular/forms';

// Import SharedModule
import { SharedModule } from '../shared/shared.module';

// Import routing module
import { AuthRoutingModule } from './auth-routing.module';

// Import auth components
import { LoginComponent } from '../login/login.component';
import { ForgotPasswordComponent } from '../forgot-password/forgot-password.component';
import { ChangePasswordComponent } from '../change-password/change-password.component';
import { ChangePasswordAfterLoginComponent } from '../change-password-after-login/change-password-after-login.component';
import { ActivateAccountComponent } from '../activate-account/activate-account.component';
import { SetPasswordComponent } from '../activate-account/set-password/set-password.component';
import { ResetPasswordComponent } from '../reset-password/reset-password.component';

// Import auth services
import { LoginService } from '../login/login.service';
import { PasswordService } from '../shared/service/password.service';
import { FirebaseAuthService } from '../shared/service/firebase-auth.service';

@NgModule({
  imports: [SharedModule, AuthRoutingModule, ToastModule, FormsModule],
  declarations: [
    LoginComponent,
    ForgotPasswordComponent,
    ResetPasswordComponent,
    ChangePasswordComponent,
    ChangePasswordAfterLoginComponent,
    ActivateAccountComponent,
    SetPasswordComponent,
  ],
  providers: [LoginService, PasswordService, FirebaseAuthService],
})
export class AuthModule {}
