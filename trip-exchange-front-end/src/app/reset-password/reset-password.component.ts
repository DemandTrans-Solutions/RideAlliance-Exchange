import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { FirebaseAuthService } from '../shared/service/firebase-auth.service';
import { NotificationEmitterService } from '../shared/service/notification-emitter.service';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.scss']
})
export class ResetPasswordComponent {
  public resetForm: FormGroup;
  public emailForReset: string | null = null;
  public oobCode: string | null = null;
  public loading = false;

  // complexity flags
  public hasMinLen = false;
  public hasUpper = false;
  public hasLower = false;
  public hasNumber = false;
  public hasSpecial = false;

  // custom validator
  private passwordComplexityValidator(control: AbstractControl): { [key: string]: any } | null {
    const val: string = control.value || '';
    const minLen = val.length >= 8;
    const upper = /[A-Z]/.test(val);
    const lower = /[a-z]/.test(val);
    const num = /[0-9]/.test(val);
    const special = /[^A-Za-z0-9]/.test(val);

    const valid = minLen && upper && lower && num && special;
    return valid ? null : { complexity: true };
  }

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private firebaseAuth: FirebaseAuthService,
    private notificationService: NotificationEmitterService
  ) {
    this.resetForm = this.fb.group({
      password: ['', [Validators.required, Validators.minLength(8), this.passwordComplexityValidator.bind(this)]],
      confirmPassword: ['', [Validators.required]]
    });

    // update strength on password changes
    this.resetForm.get('password')?.valueChanges.subscribe((val) => {
      this.updateStrength(val || '');
      this.resetForm.get('password')?.updateValueAndValidity({ onlySelf: true, emitEvent: false });
    });

    this.oobCode = this.route.snapshot.queryParamMap.get('oobCode');
    if (!this.oobCode) {
      this.notificationService.error('Invalid Link', 'Missing verification code.');
    } else {
      this.verifyCode();
    }
  }

  private verifyCode() {
    if (!this.oobCode) { return; }
    this.loading = true;
    this.firebaseAuth.verifyPasswordResetCode(this.oobCode).subscribe({
      next: (email) => {
        this.emailForReset = email;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        const friendly = this.mapFirebaseError(err, 'Unable to verify password reset link.');
        this.notificationService.error('Error', friendly);
      }
    });
  }

  submit() {
    if (this.resetForm.invalid || !this.oobCode) { return; }

    const pw = this.resetForm.get('password')?.value;
    const confirm = this.resetForm.get('confirmPassword')?.value;
    if (pw !== confirm) {
      this.notificationService.error('Error', 'Passwords do not match.');
      return;
    }

    this.loading = true;
    this.firebaseAuth.confirmPasswordReset(this.oobCode, pw).subscribe({
      next: () => {
        // After resetting the password, automatically sign in the user
        this.firebaseAuth.signInWithEmailAndPassword(this.emailForReset || '', pw).subscribe({
          next: (authResult: any) => {
            this.loading = false;
            // If MFA is required, inform user and redirect to login for MFA flow
            if (authResult && (authResult as any).requiresMfa) {
              this.notificationService.success('Password reset', 'Password reset successful. Please complete multi-factor authentication on login.');
              this.router.navigate(['/auth/login']);
              return;
            }

            this.notificationService.success('Success', 'Your password has been reset and you are now signed in.');
            // Navigate to app root
            this.router.navigate(['/']);
          },
          error: (signInErr) => {
            this.loading = false;
            // If sign-in fails, show success for reset but ask user to login manually
            const friendly = this.mapFirebaseError(signInErr, 'Password reset succeeded but automatic sign-in failed. Please login manually.');
            this.notificationService.warn('Reset succeeded', friendly);
            this.router.navigate(['/auth/login']);
          }
        });
      },
      error: (err) => {
        this.loading = false;
        const friendly = this.mapFirebaseError(err, 'Unable to reset password.');
        this.notificationService.error('Error', friendly);
      }
    });
  }

  // Password strength fields
  public strengthPercent = 0;
  public strengthLabel = 'Very weak';

  private updateStrength(pw: string) {
    let score = 0;
    if (pw.length >= 8) score += 1;
    if (/[A-Z]/.test(pw)) score += 1;
    if (/[a-z]/.test(pw)) score += 1;
    if (/[0-9]/.test(pw)) score += 1;
    if (/[^A-Za-z0-9]/.test(pw)) score += 1;

    this.strengthPercent = (score / 5) * 100;
    if (score <= 1) this.strengthLabel = 'Very weak';
    else if (score === 2) this.strengthLabel = 'Weak';
    else if (score === 3) this.strengthLabel = 'Fair';
    else if (score === 4) this.strengthLabel = 'Good';
    else this.strengthLabel = 'Strong';
  }

  private mapFirebaseError(err: any, fallback: string): string {
    if (!err) { return fallback; }
    if (err.code) {
      switch (err.code) {
        case 'auth/invalid-action-code':
          return 'The password reset link is invalid or has expired.';
        case 'auth/user-disabled':
          return 'This user account has been disabled.';
        case 'auth/user-not-found':
          return 'No user found for this email address.';
        case 'auth/weak-password':
          return 'The provided password is too weak. Please choose a stronger password.';
        default:
          return err.message || fallback;
      }
    }
    return typeof err === 'string' ? err : fallback;
  }
}
