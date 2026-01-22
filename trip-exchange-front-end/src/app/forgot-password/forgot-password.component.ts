import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ForgotPasswordService } from './forgot-password.service';
import { NotificationEmitterService } from '../shared/service/notification-emitter.service';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.scss'],
  providers: [ForgotPasswordService],
})
export class ForgotPasswordComponent {
  public forgotPasswordForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private forgotPasswordService: ForgotPasswordService,
    private notificationService: NotificationEmitterService
  ) {
    this.forgotPasswordForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
    });
  }


  onSubmit() {
    if (this.forgotPasswordForm.invalid) {
      return;
    }

  const email = this.forgotPasswordForm.get('email')?.value || '';

    this.forgotPasswordService.sendEmail(email).subscribe({
      next: () => {
        this.notificationService.success(
          'Success',
          'Reset password link has been sent to your email address.'
        );
        this.router.navigate(['/login']);
      },
      error: error => {
        // Map common Firebase errors to friendlier messages
        let friendly = 'An error occurred while attempting to send a password reset email.';
        if (error && error.code) {
          switch (error.code) {
            case 'auth/user-not-found':
              friendly = 'There is no account registered with that email address.';
              break;
            case 'auth/invalid-email':
              friendly = 'The email address is invalid. Please enter a valid email.';
              break;
            case 'auth/too-many-requests':
              friendly = 'Too many requests. Please try again later.';
              break;
            default:
              // If Firebase provided a message, use it as a fallback
              if (error.message) {
                friendly = error.message;
              }
          }
        } else if (typeof error === 'string') {
          friendly = error;
        }

        this.notificationService.error('Error', friendly);
      },
    });
  }

  // Helper method for template
  get emailControl() {
    return this.forgotPasswordForm.get('email');
  }
}
