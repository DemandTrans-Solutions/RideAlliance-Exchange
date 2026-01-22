import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PasswordService } from '../shared/service/password.service';
import { NotificationEmitterService } from '../shared/service/notification-emitter.service';
import { Router, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-change-password',
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.css'],
})
export class ChangePasswordComponent implements OnInit {
  changePasswordForm: FormGroup;
  loading = false;
  username: string;

  constructor(
    private fb: FormBuilder,
    private passwordService: PasswordService,
    private notificationService: NotificationEmitterService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    // Get the username from route parameters if available
    this.route.queryParams.subscribe(params => {
      this.username = params['username'] || '';
    });

    this.changePasswordForm = this.fb.group(
      {
        password: ['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', Validators.required],
      },
      { validator: this.passwordMatchValidator }
    );
  }

  passwordMatchValidator(g: FormGroup) {
    return g.get('password')?.value === g.get('confirmPassword')?.value ? null : { mismatch: true };
  }

  onSubmit() {
    if (this.changePasswordForm.valid) {
      this.loading = true;
      this.passwordService
        .changePassword(this.username, this.changePasswordForm.get('password')?.value)
        .subscribe({
          next: () => {
            this.notificationService.success('Success', 'Password changed successfully');
            this.router.navigate(['/login']);
          },
          error: error => {
            this.notificationService.error('Error', error);
            this.loading = false;
          },
        });
    }
  }
}
