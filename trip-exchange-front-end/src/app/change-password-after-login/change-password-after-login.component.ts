import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TokenService } from '../shared/service/token.service';
import { PasswordService } from '../shared/service/password.service';
import { NotificationEmitterService } from '../shared/service/notification-emitter.service';
import { HeaderEmitterService } from '../shared/service/header-emmiter.service';
import { SharedHttpClientService } from '../shared/service/shared-http-client.service';

@Component({
  selector: 'app-change-password-after-login',
  templateUrl: './change-password-after-login.component.html',
  styleUrls: ['./change-password-after-login.component.scss'],
})
export class ChangePasswordAfterLoginComponent implements OnInit {
  public passwordForm: FormGroup;
  public userName = false;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private passwordService: PasswordService,
    private tokenService: TokenService,
    private notificationService: NotificationEmitterService,
    private headerEmitter: HeaderEmitterService,
    private sharedHttpClientService: SharedHttpClientService
  ) {
    this.passwordForm = this.fb.group(
      {
        currentPassword: ['', [Validators.required, Validators.minLength(4)]],
        newPassword: ['', [Validators.required, Validators.minLength(4)]],
        confirmPassword: ['', [Validators.required]],
      },
      { validator: this.passwordMatchValidator }
    );
  }

  ngOnInit() {
    if (!this.tokenService.get()) {
      this.router.navigate(['/login']);
      return;
    }
    this.headerEmitter.header.emit((this.userName = true));
  }

  private passwordMatchValidator(g: FormGroup) {
    return g.get('newPassword').value === g.get('confirmPassword').value
      ? null
      : { mismatch: true };
  }

  onSubmit() {
    if (this.passwordForm.invalid) {
      return;
    }

    const formValue = this.passwordForm.value;

    // For debugging - log token information
    console.log('Debug token information:');
    const tokenInfo = this.tokenService.debug();

    // Try to get username or use fallback
    let username = '';
    try {
      username = this.tokenService.getUsername();
      console.log('Extracted username:', username);
    } catch (error) {
      console.error('Error extracting username:', error);
      this.notificationService.error('Error', 'Could not extract username from token');
      return;
    }

    // Add a fallback if username extraction fails
    if (!username) {
      // You might want to get the username from another source or ask the user
      console.warn('No username found in token, trying to get it from another source');
      // Option 1: Ask the user for their username (uncomment if needed)
      // Implement a dialog or form field for username input

      // Option 2: Extract from JWT without using the getUsername method
      try {
        const token = this.tokenService.get();
        if (token) {
          // Just for this emergency fix, get username from JWT manually
          const parts = token.split('.');
          if (parts.length === 3) {
            // Try to pull out username from JWT payload using a simpler approach
            const base64Payload = parts[1].replace(/-/g, '+').replace(/_/g, '/');
            const paddedPayload = base64Payload.padEnd(
              base64Payload.length + ((4 - (base64Payload.length % 4)) % 4),
              '='
            );
            const jsonStr = atob(paddedPayload);
            const payload = JSON.parse(jsonStr);
            username = payload.sub || payload.username || payload.email || '';
            console.log('Username extracted via fallback:', username);
          }
        }
      } catch (err) {
        console.error('Fallback username extraction failed:', err);
      }
    }

    // Final safety check - if no username is available, alert the user
    if (!username) {
      this.notificationService.error(
        'Error',
        'Could not determine your username. Please log out and try again.'
      );
      return;
    }

    // Create request payload
    const payload = {
      username: username,
      oldPassword: formValue.currentPassword,
      newPassword: formValue.newPassword,
    };

    // Use sharedHttpClientService instead of passwordService
    const changePasswordUrl = this.passwordService.getChangePasswordUrl();
    this.sharedHttpClientService.post(changePasswordUrl, payload).subscribe({
      next: () => {
        this.notificationService.success('Success', 'Password changed successfully');
        this.tokenService.remove();
        this.router.navigate(['/login']);
      },
      error: error => {
        console.error('Error changing password:', error);
        this.notificationService.error('Error', error.message || 'Failed to change password');
      },
    });
  }

  // Helper methods for template binding
  get currentPasswordControl() {
    return this.passwordForm.get('currentPassword');
  }
  get newPasswordControl() {
    return this.passwordForm.get('newPassword');
  }
  get confirmPasswordControl() {
    return this.passwordForm.get('confirmPassword');
  }
}
