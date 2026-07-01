import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';
import { User } from './user';
import { UserInfo } from './userInfo';
import { Logger } from '../shared/service/default-log.service';
import { SharedHttpClientService } from '../shared/service/shared-http-client.service';
import { ConstantService } from '../shared/service/constant-service';
import { TokenService } from '../shared/service/token.service';
import { NotificationEmitterService } from '../shared/service/notification-emitter.service';
import { CookieService } from 'ngx-cookie-service';
import { LocalStorageService } from '../shared/service/local-storage.service';
import { HeaderEmitterService } from '../shared/service/header-emmiter.service';
import { FirebaseAuthService, AuthenticationResult, FirebaseUser } from '../shared/service/firebase-auth.service';
import { LoginService } from './login.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent implements OnInit, OnDestroy {
  public user: User = new User();
  public userInfo: UserInfo = new UserInfo();
  public ngForm: FormGroup;
  public firebaseLoginForm: FormGroup;
  public successfull = false;
  public userName = false;
  public loading$ = false;

  // Firebase 2FA Properties
  public authStep: 'email-password' | 'mfa-verification' = 'email-password';
  public verificationId: string = '';
  public smsVerificationCode: string = '';
  public errorMessage: string = '';
  public successMessage: string = '';
  public showAccessRequestMessage: boolean = false;
  public emailVerificationRequired: boolean = false;

  // New Mandatory MFA Properties
  public phase: 'initial' | 'mfa-enrollment' | 'mfa-verification' | 'processing' = 'initial';
  public currentAuthResult: AuthenticationResult | null = null;
  public countryCode: string = '+1';
  public phoneNumber: string = '';
  public verificationCode: string = '';

  // Debug logging properties
  public debugLogs: string[] = [];
  public showDebugLogs: boolean = false;
  // Manual flag to show/hide the debug panel (default false)
  // This can be toggled from code for debugging purposes.
  public showDebugPanel: boolean = false;

  // Authentication flow control flags
  private isUserInitiatedAuth: boolean = false;
  private isComponentInitializing: boolean = true;
  private pendingRedirect: boolean = false; // set when a Google redirect was initiated
  private redirectProcessed: boolean = false; // set after we handle redirect result
  private static readonly REDIRECT_FLAG_KEY = 'firebase-auth-redirect-pending';

  // Subscriptions
  private authSubscription?: Subscription;

  constructor(
    public _router: Router,
    public _loginService: LoginService,
    public fb: FormBuilder,
    public _logger: Logger,
    public _sharedHttpClientService: SharedHttpClientService,
    public _tokenService: TokenService,
    public _notificationService: NotificationEmitterService,
    public _constantService: ConstantService,
    public _cookieService: CookieService,
    public _localStorage: LocalStorageService,
    public _headerEmitter: HeaderEmitterService,
    private _firebaseAuthService: FirebaseAuthService
  ) {
    // Original form for backward compatibility
    this.ngForm = this.fb.group({
      Email: [null, Validators.required],
      Password: [
        null,
        Validators.compose([
          Validators.required,
          Validators.minLength(5),
        ]),
      ],
    });

    // Firebase authentication form
    this.firebaseLoginForm = this.fb.group({
      email: [null, [Validators.required, Validators.email]],
      password: [
        null,
        Validators.compose([
          Validators.required,
          Validators.minLength(6),
        ]),
      ],
    });
  }

  ngOnInit() {
    // Initialize debug logging
    this.initializeDebugLogging();
    this.addDebugLog('🔄 LoginComponent initialized');

    if (navigator.cookieEnabled === false) {
      this._notificationService.error('Error Message', 'Cookies are not enable');
    } else {
      // Preserve redirect flag & debug logs while clearing auth/session data
      const redirectFlag = localStorage.getItem(LoginComponent.REDIRECT_FLAG_KEY);
      const savedDebug = localStorage.getItem('debug-auth-logs');
      const savedTheme = localStorage.getItem('tx-theme') || localStorage.getItem('theme');
      // Also preserve durable per-user table preferences (and theme), which clearAll() below would otherwise wipe.
      const keep: Record<string, string> = this._localStorage.getLocalStorageToKeep();

      this._tokenService.clearAll();
      // DO NOT clear entire local storage because we need redirect flag & debug logs
      // this._localStorage.clearAll();  (commented out)
      Object.entries(keep).forEach(([k, v]) => localStorage.setItem(k, v));
      if (redirectFlag) {
        localStorage.setItem(LoginComponent.REDIRECT_FLAG_KEY, redirectFlag);
        this.pendingRedirect = true;
        this.isUserInitiatedAuth = true; // treat as user initiated
        this.addDebugLog('↩️ Detected pending Google redirect flag');
      }
      if (savedDebug) {
        localStorage.setItem('debug-auth-logs', savedDebug);
      }
      if (savedTheme) {
        localStorage.setItem('tx-theme', savedTheme);
      }
    }

    // Reset authentication state
    this.authStep = 'email-password';
    this.clearMessages();
    // Begin authentication flow (no forced sign out – we need redirect context intact)
    this.initializeAuthenticationFlow();
  }

  /**
   * Initialize authentication flow after ensuring clean state
   */
  private initializeAuthenticationFlow(): void {
    this.addDebugLog('� Initializing authentication flow...');

    // Process redirect result FIRST (needed before listener reacts to existing sessions)
    this.addDebugLog('🔍 Checking for Google redirect result (priority)...');
    this._firebaseAuthService.checkGoogleRedirectResult().subscribe({
      next: (authResult) => {
        this.addDebugLog('📥 Google redirect result received');
        if (authResult) {
          this.addDebugLog('✅ Google redirect authentication successful');
          this.addDebugLog('👤 Firebase user: ' + authResult.user.uid + ' - ' + authResult.user.email);
          this.addDebugLog('🔐 MFA status: ' + authResult.mfaStatus);
          this._logger.log('Google redirect authentication successful, checking MFA status');

          // This is a user-initiated redirect authentication
          this.isUserInitiatedAuth = true;
          this.pendingRedirect = false;
          this.redirectProcessed = true;
          localStorage.removeItem(LoginComponent.REDIRECT_FLAG_KEY);

          if (authResult.mfaStatus === 'not-enrolled') {
            // User must enroll in MFA before proceeding
            this.addDebugLog('📱 Google user has no MFA - forcing enrollment');
            this._logger.log('Google redirect user has no MFA - forcing enrollment');
            this.currentAuthResult = authResult;
            this.phase = 'mfa-enrollment';
          } else if (authResult.mfaStatus === 'enrolled') {
            // User has MFA enrolled - must verify before backend validation
            this.addDebugLog('🔒 Google user has MFA enrolled - forcing verification');
            this._logger.log('Google redirect user has MFA - forcing verification');
            this.currentAuthResult = authResult;
            this.startForcedMfaVerification();
          } else {
            this.addDebugLog('❓ Unknown MFA status: ' + authResult.mfaStatus);
            this.addDebugLog('🚨 This should not happen - defaulting to MFA enrollment');
            // Fallback to MFA enrollment if status is unclear
            this.currentAuthResult = authResult;
            this.phase = 'mfa-enrollment';
          }
        } else {
          this.addDebugLog('ℹ️ No Google redirect result (user did not just complete Google sign-in)');
          if (this.pendingRedirect) {
            this.addDebugLog('⚠️ Redirect flag set but no auth result – clearing flag');
            localStorage.removeItem(LoginComponent.REDIRECT_FLAG_KEY);
            this.pendingRedirect = false;
          }
        }

        // After redirect attempt is processed, start backend connectivity test & auth listener
        this.setupAuthStateListener();
        //this.testBackendConnectivity();
      },
      error: (error) => {
        this.addDebugLog('❌ Google redirect result error: ' + (error.message || error));
        this._logger.log('Google redirect result error:', error);
        this.handleFirebaseError(error);
        // Even on error continue with listener & backend test
        this.setupAuthStateListener();
        this.testBackendConnectivity();
      }
    });

    // Mark component initialization as complete after a brief delay
    setTimeout(() => {
      this.isComponentInitializing = false;
      this.addDebugLog('✅ Component initialization complete - ready for user interactions');
    }, 1000);
  }

  /**
   * Handle Firebase auth state changes with proper flow control
   */
  private handleFirebaseAuthStateChange(user: FirebaseUser | null): void {
    if (user) {
      this.addDebugLog('🔥 Firebase auth state change - user authenticated');
      this.addDebugLog('👤 Firebase user: ' + user.uid + ' - ' + user.email);
      this.addDebugLog('📧 Email verified: ' + user.emailVerified);

      // Check if this is during component initialization (automatic browser state)
      if (this.isComponentInitializing) {
        this.addDebugLog('⏳ Auth state change during component initialization - ignoring automatic browser state');
        this.addDebugLog('🚫 This prevents automatic login from cached Google browser authentication');
        return;
      }

      // Check if user came through redirect (which already handles MFA)
      if (this.currentAuthResult) {
        this.addDebugLog('📋 User already processed through redirect flow - skipping duplicate processing');
        return;
      }
      // If redirect just processed we don't need to force sign out
      if (this.redirectProcessed) {
        this.addDebugLog('✅ Redirect already processed; ignoring additional auth state change');
        return;
      }

      // Only process if this was a user-initiated authentication
      if (!this.isUserInitiatedAuth) {
        this.addDebugLog('🚫 Auth state change was not user-initiated - user must explicitly choose to sign in');
        this.addDebugLog('💡 This ensures users see the login screen and choose their authentication method');

        // Sign out the automatically authenticated user to force explicit login choice
        this.addDebugLog('🚪 Signing out automatically authenticated user...');
        this._firebaseAuthService.signOut().subscribe({
          next: () => {
            this.addDebugLog('✅ Automatic authentication cleared - user can now choose login method');
          },
          error: (signOutError) => {
            this.addDebugLog('❌ Error clearing automatic authentication: ' + signOutError.message);
          }
        });
        return;
      }

      // This is a legitimate user-initiated authentication
      this.addDebugLog('🚨 User-initiated authentication detected - enforcing mandatory MFA');
      this.addDebugLog('🔍 Determining MFA requirement for authenticated user...');

      // Get current user from Firebase auth service
      const currentUser = this._firebaseAuthService.getCurrentUser();
      if (currentUser) {
        // For user-initiated sign-in, we'll assume MFA is not enrolled and force enrollment
        // This ensures security by requiring all users to have MFA
        this.addDebugLog('📱 User-initiated sign-in detected - defaulting to MFA enrollment requirement');

        const authResult: AuthenticationResult = {
          user: user,
          mfaStatus: 'not-enrolled' // Force MFA enrollment for all user-initiated sign-ins
        };

        this.addDebugLog('📱 Forcing MFA enrollment for user-initiated authentication');
        this.currentAuthResult = authResult;
        this.phase = 'mfa-enrollment';
        this.loading$ = false;
      } else {
        this.addDebugLog('❌ Could not get current Firebase user');
        // Still default to MFA enrollment for security
        const authResult: AuthenticationResult = {
          user: user,
          mfaStatus: 'not-enrolled'
        };
        this.currentAuthResult = authResult;
        this.phase = 'mfa-enrollment';
        this.loading$ = false;
      }
    } else {
      this.addDebugLog('👤 No Firebase user authenticated');
    }
  }

  /**
   * Initialize debug logging and load previous logs from localStorage
   */
  private initializeDebugLogging(): void {
    // Load previous debug logs from localStorage
    const savedLogs = localStorage.getItem('debug-auth-logs');
    if (savedLogs) {
      try {
        this.debugLogs = JSON.parse(savedLogs);
        this.debugLogs.push(`📅 ${new Date().toLocaleString()} - Component reinitialized`);
      } catch (e) {
        this.debugLogs = [];
      }
    } else {
      this.debugLogs = [];
    }

    // Limit log size to prevent memory issues
    if (this.debugLogs.length > 100) {
      this.debugLogs = this.debugLogs.slice(-50);
    }
  }

  /**
   * Add a debug log entry and save to localStorage
   */
  private addDebugLog(message: string): void {
    const timestamp = new Date().toLocaleTimeString();
    const logEntry = `${timestamp} - ${message}`;
    this.debugLogs.push(logEntry);

    // Save to localStorage so it persists through page refreshes
    try {
      localStorage.setItem('debug-auth-logs', JSON.stringify(this.debugLogs));
    } catch (e) {
      console.warn('Could not save debug logs to localStorage:', e);
    }

    // Also log to console for immediate visibility
    console.log(`🐛 AUTH DEBUG: ${logEntry}`);
    this._logger.log(`DEBUG: ${message}`);
  }

  /**
   * Toggle debug logs visibility
   */
  public toggleDebugLogs(): void {
    this.showDebugLogs = !this.showDebugLogs;
  }

  /**
   * Test backend connectivity to show clear error if backend is not running
   */
  private testBackendConnectivity(): void {
    this.addDebugLog('🌐 Testing backend connectivity...');

    // Create a simple test request to check if backend is responding
    // We'll use a lightweight endpoint or catch connection errors
    const testPayload = { email: 'test@connectivity.check' }; // Dummy payload

    this._loginService.validateUser(testPayload as any).subscribe({
      next: (_response) => {
        // Even if this fails validation, it means backend is running
        this.addDebugLog('✅ Backend is running and responsive');
      },
      error: (error) => {
        this.addDebugLog('🔗 Backend connectivity test result:');
        this.addDebugLog('   Status: ' + (error.status || 'unknown'));
        this.addDebugLog('   Message: ' + (error.message || 'unknown'));

        if (error.status === 0 || error.name === 'HttpErrorResponse' && error.status === 0) {
          // Network error - backend is likely not running
          this.addDebugLog('🚨 BACKEND NOT RUNNING - Network connection failed');
          this.addDebugLog('🚨 This will cause authentication to fail at backend validation step');
          this.errorMessage = 'Backend server is not running. Please start the backend service and refresh the page.';
        } else if (error.status >= 400 && error.status < 600) {
          // Backend is running but returned an error (expected for invalid test data)
          this.addDebugLog('✅ Backend is running (returned HTTP ' + error.status + ' as expected)');
        } else {
          this.addDebugLog('❓ Unexpected backend response: ' + error.status);
        }
      }
    });
  }

  /**
   * Test bypass - complete login without backend validation (for debugging only)
   * This is useful when backend is not running but we want to test Firebase 2FA flow
   */
  private debugBypassBackendValidation(firebaseUser: FirebaseUser): void {
    this.addDebugLog('⚠️ DEBUG BYPASS: Skipping backend validation');
    this.addDebugLog('🔥 Firebase User: ' + firebaseUser.uid + ' - ' + firebaseUser.email);

    // Simulate successful authentication without backend call
    this._notificationService.success('Debug Mode', 'Firebase authentication successful (backend bypassed)');
    this.loading$ = false;

    // Show success but don't navigate (since we don't have valid backend session)
    this.successMessage = 'Firebase 2FA completed successfully! (Backend validation bypassed for testing)';
    this.addDebugLog('✅ Firebase authentication flow completed successfully');
  }

  /**
   * Clear debug logs
   */
  public clearDebugLogs(): void {
    this.debugLogs = [];
    localStorage.removeItem('debug-auth-logs');
  }

  ngOnDestroy(): void {
    if (this.authSubscription) {
      this.authSubscription.unsubscribe();
    }
    // Clear any reCAPTCHA when component is destroyed
    this._firebaseAuthService.clearRecaptcha();
  }

  /**
   * Sends a verification email to the currently signed-in user.
   */
  public sendVerificationEmail(): void {
    this.loading$ = true;
    this.clearMessages();
    this.addDebugLog('📧 Sending verification email...');
    this._firebaseAuthService.sendVerificationEmail().subscribe({
      next: () => {
        this.loading$ = false;
        this.successMessage = 'A new verification email has been sent. Please check your inbox.';
        this.emailVerificationRequired = true; // Keep message visible
        this.addDebugLog('✅ Verification email sent successfully.');
      },
      error: (error) => {
        this.loading$ = false;
        this.addDebugLog('❌ Error sending verification email: ' + (error.message || error.code));
        this.handleFirebaseError(error);
      }
    });
  }

  /**
   * Handle Google Sign-In - MANDATORY MFA ENFORCEMENT
   */
  signInWithGoogle(): void {
    this.loading$ = true;
    this.clearMessages();
    this.addDebugLog('🌟 User clicked Google Sign-In button - marking as user-initiated');

    // Mark this as user-initiated authentication
    this.isUserInitiatedAuth = true;
    this.pendingRedirect = true;
    localStorage.setItem(LoginComponent.REDIRECT_FLAG_KEY, '1');

    // Initiate Google redirect - result will be handled on page reload in ngOnInit
    this._firebaseAuthService.signInWithGoogle();
  }

  /**
   * Set up the Firebase auth state listener (called after redirect result processing)
   */
  private setupAuthStateListener(): void {
    if (this.authSubscription) {
      return; // already set
    }
    this.addDebugLog('👂 Setting up Firebase auth state listener...');
    this.authSubscription = this._firebaseAuthService.currentUser$.subscribe({
      next: (user: FirebaseUser | null) => this.handleFirebaseAuthStateChange(user),
      error: (authError: any) => {
        this.addDebugLog('❌ Firebase auth state error: ' + (authError.message || authError));
        this._logger.error('Firebase auth state error:', authError);
      }
    });

    // Mark component initialization as complete after a brief delay
    setTimeout(() => {
      this.isComponentInitializing = false;
      this.addDebugLog('✅ Component initialization complete - ready for user interactions');
    }, 800);
  }

  /**
   * Handle first factor authentication (email/password)
   */
  onFirstFactorSubmit(): void {
    if (!this.firebaseLoginForm.valid) {
      return;
    }

    this.loading$ = true;
    this.clearMessages();

    // Mark this as user-initiated authentication
    this.isUserInitiatedAuth = true;

    const email = this.firebaseLoginForm.get('email')?.value;
    const password = this.firebaseLoginForm.get('password')?.value;

    this._firebaseAuthService.signInWithEmailAndPassword(email, password).subscribe({
      next: (result) => {
        if ('requiresMfa' in result && result.requiresMfa) {
          // Firebase detected MFA is required, proceed to MFA step
          this._logger.log('Firebase MFA required, proceeding to verification step');
          this.authStep = 'mfa-verification';
          this.loading$ = false;
        } else {
          // Check the AuthenticationResult for MFA status
          const authResult = result as AuthenticationResult;
          this._logger.log('Email/password authentication successful, checking MFA status');

          if (authResult.mfaStatus === 'not-enrolled') {
            // User must enroll in MFA before proceeding
            this._logger.log('Email/password user has no MFA - forcing enrollment');
            this.currentAuthResult = authResult;
            this.phase = 'mfa-enrollment';
            this.loading$ = false;
          } else if (authResult.mfaStatus === 'enrolled') {
            // User has MFA enrolled - must verify before backend validation
            this._logger.log('Email/password user has MFA - forcing verification');
            this.currentAuthResult = authResult;
            this.startForcedMfaVerification();
          } else {
            // Direct to backend validation (shouldn't happen in new flow)
            this.validateUserWithBackend(authResult.user);
          }
        }
      },
      error: (error) => {
        this.loading$ = false;
        this.handleFirebaseError(error);
      }
    });
  }

  /**
   * Send MFA verification code via SMS
   */
  sendMfaCode(): void {
    this.loading$ = true;
    this.clearMessages();

    this._firebaseAuthService.sendMfaVerificationCode('recaptcha-container-verification').subscribe({
      next: (verificationId) => {
        this.verificationId = verificationId;
        this.successMessage = 'Verification code sent to your phone';
        this.loading$ = false;
        this._logger.log('SMS verification code sent');
      },
      error: (error) => {
        this.loading$ = false;
        this.errorMessage = 'Failed to send verification code. Please try again.';
        this._logger.log('Error sending MFA code:', error);
      }
    });
  }

  /**
   * Handle MFA verification code submission
   */
  onMfaVerificationSubmit(): void {
    if (!this.verificationId || !this.smsVerificationCode || this.smsVerificationCode.length !== 6) {
      this.errorMessage = 'Please enter a valid 6-digit verification code';
      return;
    }

    this.loading$ = true;
    this.clearMessages();

    this._firebaseAuthService.completeMfaSignIn(this.smsVerificationCode, this.verificationId).subscribe({
      next: (firebaseUser) => {
        this._logger.log('MFA authentication completed');
        this.validateUserWithBackend(firebaseUser);
      },
      error: (error) => {
        this.loading$ = false;
        this.errorMessage = 'Invalid verification code. Please try again.';
        this._logger.log('Error completing MFA:', error);
        this.smsVerificationCode = ''; // Clear the code for retry
      }
    });
  }

  /**
   * Validate Firebase user with backend service
   */
  private validateUserWithBackend(firebaseUser: FirebaseUser): void {
    this.addDebugLog('🔥 Starting backend validation for user: ' + firebaseUser.uid + ' - ' + firebaseUser.email);
    this._logger.log('🔥 Starting backend validation for user:', firebaseUser.uid, firebaseUser.email);

    this._loginService.validateUser(firebaseUser).subscribe({
      next: (response) => {
        this.addDebugLog('✅ Backend validation successful for user ID: ' + response.id);
        this._logger.log('✅ Backend validation successful:', response);

        // Check if we have an XSRF token, which indicates successful authentication
        const xsrfToken = this._cookieService.get('XSRF-TOKEN');
        this.addDebugLog('🍪 XSRF Token: ' + (xsrfToken ? 'Found' : 'Not found'));
        this._logger.log('🍪 XSRF Token:', xsrfToken ? 'Found' : 'Not found');

        if (!xsrfToken) {
          this.addDebugLog('❌ Authentication failed - no XSRF token received');
          this._logger.error('❌ Authentication failed - no XSRF token received');
          this._notificationService.error(
            'Error Message',
            'Authentication failed - security token not received'
          );
          this.loading$ = false;
          return;
        }

        this.addDebugLog('💾 Storing authentication data in localStorage');
        this._logger.log('💾 Storing authentication data in localStorage');

        // Store authentication data (same as original login flow)
        if (response.JWTToken) {
          this._tokenService.set(response.JWTToken);
          this.addDebugLog('🔑 JWT Token stored');
          this._logger.log('🔑 JWT Token stored');
        }

        this._localStorage.set('xsrfToken', xsrfToken);
        this._localStorage.set('isPasswordExpired', response.isPasswordExpired);
        this._localStorage.set('name', response.name);
        this._localStorage.set('Role', response.authorities[0].authority);
        this._localStorage.set('title', response.jobTitle);
        this._localStorage.set('username', response.username);
        this._localStorage.set('userId', response.id);

        // Defensive: normalize providerId coming from backend. Some backend responses may place
        // the provider id in different fields or omit it; log and normalize here to avoid
        // 'undefined' being written into localStorage which then propagates into API URLs.
        let providerIdValue: any = null;
        try {
          providerIdValue = response.responseDataForUI ?? response.providerId ?? response.responseData?.providerId ?? null;
        } catch (e) {
          providerIdValue = null;
        }
        this.addDebugLog('🔎 Backend response providerId check: responseDataForUI=' + response.responseDataForUI + ', providerId=' + response.providerId);
        this.addDebugLog('🔎 Backend response providerId (normalized): ' + String(providerIdValue));

        // Ensure we only store valid providerId values (not null, undefined, or empty strings)
        if (providerIdValue !== null && providerIdValue !== undefined && String(providerIdValue).trim() !== '' && String(providerIdValue) !== 'null') {
          this._localStorage.set('providerId', providerIdValue);
          this.addDebugLog('✅ ProviderId stored: ' + providerIdValue);
        } else {
          // If no valid providerId is found, log an error but continue login
          this.addDebugLog('⚠️ Warning: No valid providerId found in backend response. User may have limited functionality.');
          this.addDebugLog('⚠️ Full backend response for debugging: ' + JSON.stringify(response));
        }

        // Store Firebase-specific data
        this._localStorage.set('firebaseUid', firebaseUser.uid);

        this.addDebugLog('✅ User info stored in local storage');
        this._logger.log('✅ User info stored in local storage');
        const userRole = response.authorities[0].authority;
        this.addDebugLog('👤 User role: ' + userRole);
        this._logger.log('👤 User role:', userRole);

        // Handle password expiration (unlikely with Firebase, but keeping for compatibility)
        if (response.isPasswordExpired === true) {
          this.addDebugLog('🔐 Password expired - redirecting to change password');
          this._logger.log('🔐 Password expired - redirecting to change password');
          this._router.navigate(['/changePasswordAfterLogin']);
        } else {
          if (userRole === 'ROLE_PROVIDERADMIN') {
            this.addDebugLog('🏢 Provider admin detected - fetching provider details');
            this._logger.log('🏢 Provider admin detected - fetching provider details');
            this._loginService.getProvider(this._localStorage.get('providerId')).subscribe({
              next: (providerResponse) => {
                this.addDebugLog('🏢 Provider details fetched: ' + providerResponse.providerName);
                this._logger.log('🏢 Provider details fetched:', providerResponse.providerName);
                this._localStorage.set('providerName', providerResponse.providerName);
                this.completeLogin();
              },
              error: (providerError) => {
                this.addDebugLog('⚠️ Provider details fetch failed, but continuing login');
                this._logger.log('⚠️ Provider details fetch failed, but continuing login:', providerError);
                // Still navigate even if provider details couldn't be fetched
                this.completeLogin();
              },
            });
          } else {
            this.addDebugLog('👤 Standard user - proceeding to complete login');
            this._logger.log('👤 Standard user - proceeding to complete login');
            this.completeLogin();
          }
        }
      },
      error: (error) => {
        this.addDebugLog('❌ Backend validation error: ' + (error.message || error.status || 'Unknown error'));
        this._logger.error('❌ Backend validation error:', error);

        // Check for specific error types and reset the state with a clear message.
        if (error.status === 0 || (error.name === 'HttpErrorResponse' && error.status === 0)) {
          const msg = 'Backend server is not responding. Please ensure the server is running and try again.';
          this.addDebugLog(`🚨 ${msg}`);
          this.resetAuthState(msg);
        } else if (error.status === 401) {
          // More user-friendly message for unauthorized users
          const msg = 'You are not authorized to use Ride Alliance. Please contact support.';
          this.addDebugLog(`🚫 401 Unauthorized: ${msg}`);
          this.resetAuthState(msg);
          // Special handling for access request message if needed, though resetAuthState is cleaner
          // this.showAccessRequestMessage = true;
        } else {
          const msg = `An unexpected error occurred during backend validation (HTTP ${error.status}). Please try again.`;
          this.addDebugLog(`💥 ${msg}`);
          this.resetAuthState(msg);
        }
      }
    });
  }

  /**
   * Complete the login process
   */
  private completeLogin(): void {
    this.addDebugLog('🎉 Completing login process...');
    this._logger.log('🎉 Completing login process...');
    this.addDebugLog('📍 Current route before navigation: ' + this._router.url);
    this._logger.log('📍 Current route before navigation:', this._router.url);

    try {
      this._notificationService.success('Success Message', 'Login successful');
      this.addDebugLog('✅ Success notification shown');
      this._logger.log('✅ Success notification shown');

      // Keep loading true while we navigate so the login UI does not briefly flash
      this.loading$ = true;
      this.addDebugLog('⏳ Loading state maintained true while navigating');
      this._logger.log('⏳ Loading state maintained true while navigating');

      this._headerEmitter.header.emit(true);
      this.addDebugLog('📡 Header emit signal sent');
      this._logger.log('📡 Header emit signal sent');

      this.addDebugLog('Navigating to /home...');
      this._logger.log('Navigating to /home...');
      // Navigate immediately and clear loading once navigation completes (or fails)
      this.addDebugLog('🚀 Attempting navigation now...');
      this._router.navigate(['/home']).then(
        (success) => {
          if (success) {
            this.addDebugLog('Navigation to /home successful');
            this._logger.log('Navigation to /home successful');
          } else {
            this.addDebugLog('Navigation to /home failed - router returned false');
            this._logger.error('Navigation to /home failed');
          }
          // In case the component is still present, clear loading state
          try {
            this.loading$ = false;
          } catch (e) {
            // Component may have been destroyed after navigation; ignore
          }
        }
      ).catch((navError) => {
        this.addDebugLog('💥 Navigation error: ' + navError.message);
        this._logger.error('💥 Navigation error:', navError);
        try {
          this.loading$ = false;
        } catch (e) {
          // ignore
        }
      });

      this.addDebugLog('🎯 Login completion process finished');
      this._logger.log('🎯 Login completion process finished');
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Unknown error';
      this.addDebugLog('💥 Error in completeLogin: ' + errorMessage);
      this._logger.error('💥 Error in completeLogin:', error);
    }
  }

  /**
   * Handle Firebase authentication errors
   */
  private handleFirebaseError(error: any): void {
    this._logger.log('Firebase auth error:', error);
    const code = error?.code;

    // Special handling for App Check / configuration errors
    if (code === 'auth/configuration-error' || code === 'auth/invalid-app-credential') {
      this.errorMessage = '⚠️ Authentication System Configuration Error\n\n' +
        'The authentication system is not properly configured for this domain. ' +
        'This is a server-side configuration issue that prevents 2-factor authentication enrollment.\n\n' +
        'Please contact technical support with error code: ' + code + '\n\n' +
        'For administrators: The reCAPTCHA v3 site key must be registered for domain: ' + window.location.hostname;
      // Clear reCAPTCHA to allow retry if configuration is fixed
      this._firebaseAuthService.clearRecaptcha();
      return;
    }

    // Provide richer context for internal errors during MFA flows
    if (code === 'auth/internal-error') {
      // Frequently due to: invalid / unapproved domain, recaptcha mismatch, missing SMS region config, or invalid phone format
      this.errorMessage = 'Internal authentication error while sending or verifying SMS. Please verify: (1) Phone number format with full country code, (2) reCAPTCHA visible and solved, (3) App domain is authorized in Firebase console, (4) SMS multi-factor is enabled and test phone numbers configured.';
      if (error?.message) {
        this.errorMessage += `\nDetails: ${error.message}`;
      }
    } else {
      switch (code) {
        case 'auth/unverified-email':
          this.errorMessage = 'Your email address is not verified. Please check your inbox for a verification link, or send a new one.';
          this.emailVerificationRequired = true;
          // Clear all reCAPTCHA containers to prevent "already rendered" error on retry
          this._firebaseAuthService.clearRecaptcha();
          break;
        case 'auth/user-not-found':
        case 'auth/wrong-password':
        case 'auth/invalid-credential':
          this.errorMessage = 'Invalid email or password. Please check your credentials.';
          break;
        case 'auth/too-many-requests':
          this.errorMessage = 'Too many failed attempts. Please try again later.';
          break;
        case 'auth/user-disabled':
          this.errorMessage = 'This account has been disabled. Please contact support.';
          break;
        case 'auth/invalid-phone-number':
          this.errorMessage = 'Invalid phone number format. Use only digits with country code (e.g., +11234567890).';
          break;
        case 'auth/missing-phone-number':
          this.errorMessage = 'Phone number is required.';
          break;
        case 'auth/too-many-requests/mfa':
          this.errorMessage = 'Too many SMS attempts. Please wait a few minutes and try again.';
          break;
        case 'auth/captcha-check-failed':
          this.errorMessage = 'reCAPTCHA verification failed. Please try again.';
          // Clear reCAPTCHA on captcha failure
          this._firebaseAuthService.clearRecaptcha();
          break;
        default:
          if (error.message && error.message.includes('reCAPTCHA has already been rendered')) {
            this.errorMessage = 'reCAPTCHA error occurred. Please refresh the page and try again.';
            // Clear reCAPTCHA on this specific error
            this._firebaseAuthService.clearRecaptcha();
          } else {
            this.errorMessage = error?.message || 'Authentication failed. Please try again.';
          }
          break;
      }
    }
  }

  /**
   * Go back to initial login step
   */
  goBackToLogin(): void {
    this.authStep = 'email-password';
    this.verificationId = '';
    this.smsVerificationCode = '';
    this.clearMessages();
    this._firebaseAuthService.clearRecaptcha();
  }

  /**
   * Clear all messages
   */
  private clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
    this.showAccessRequestMessage = false;
    this.emailVerificationRequired = false;
  }

  /**
   * Resets the entire authentication state, signs out the user, and displays an error.
   * This is used to gracefully handle failures, especially during backend validation.
   */
  private resetAuthState(errorMessage: string): void {
    this.addDebugLog(`🔄 Resetting authentication state. Reason: ${errorMessage}`);
    this._logger.log(`🔄 Resetting authentication state. Reason: ${errorMessage}`);

    // 1. Set the error message to be displayed
    this.errorMessage = errorMessage;

    // 2. Reset all state flags and properties
    this.phase = 'initial';
    this.authStep = 'email-password';
    this.currentAuthResult = null;
    this.verificationId = '';
    this.verificationCode = '';
    this.smsVerificationCode = '';
    this.phoneNumber = '';
    this.loading$ = false;
    this.emailVerificationRequired = false;
    this.showAccessRequestMessage = false;

    // 3. Clear any visible reCAPTCHA verifiers
    this._firebaseAuthService.clearRecaptcha();

    // 4. Sign out from Firebase to ensure a clean slate for the next attempt
    this.addDebugLog('🚪 Signing out from Firebase due to state reset.');
    this._firebaseAuthService.signOut().subscribe({
      next: () => {
        this.addDebugLog('✅ Firebase sign-out complete after state reset.');
        this._logger.log('✅ Firebase sign-out complete after state reset.');
      },
      error: (signOutError) => {
        this.addDebugLog(`❌ Error during sign-out in state reset: ${signOutError.message}`);
        this._logger.error('❌ Error during sign-out in state reset:', signOutError);
      }
    });
  }

  /**
   * Start forced MFA verification for users who already have MFA enrolled
   */
  public startForcedMfaVerification(): void {
    this.addDebugLog('🔒 Starting forced MFA verification...');
    this.addDebugLog('📱 Changing phase to mfa-verification');
    this.phase = 'mfa-verification';

    this.addDebugLog('📞 Attempting to send verification SMS...');
    this._firebaseAuthService.forceMfaVerification('recaptcha-container-forced').subscribe({
      next: (verificationId) => {
        this.addDebugLog('✅ SMS sent successfully - verification ID: ' + verificationId.substring(0, 10) + '...');
        this.verificationId = verificationId;
        this.loading$ = false;
        this.successMessage = 'SMS sent! Please enter the verification code.';
        this._logger.log('Forced MFA verification SMS sent');
      },
      error: (error) => {
        this.addDebugLog('❌ Error sending forced MFA verification SMS: ' + (error.message || error.code || error));
        this.loading$ = false;
        this.handleFirebaseError(error);
        this._logger.error('Error starting forced MFA verification:', error);
      }
    });
  }

  /**
   * Start MFA enrollment for users who don't have MFA configured
   */
  public startMfaEnrollment(): void {
    if (!this.phoneNumber) {
      this.errorMessage = 'Phone number is required for MFA enrollment';
      return;
    }

    // Basic sanitation: remove spaces / dashes
    this.phoneNumber = this.phoneNumber.replace(/[^0-9]/g, '');
    if (this.phoneNumber.length < 8) {
      this.errorMessage = 'Phone number seems too short. Include area code.';
      return;
    }

    // Combine country code with phone number for E.164 format
    const fullPhoneNumber = this.countryCode + this.phoneNumber;
    this._logger.log('Sending MFA enrollment request for phone number:', fullPhoneNumber);

    this.loading$ = true;
    this.clearMessages();

    this._firebaseAuthService.startMfaEnrollment(fullPhoneNumber, 'recaptcha-container-enrollment').subscribe({
      next: (verificationId) => {
        this.verificationId = verificationId;
        this.loading$ = false;
        this.successMessage = 'SMS sent! Please enter the verification code to complete MFA enrollment.';
        this._logger.log('MFA enrollment SMS sent');
      },
      error: (error) => {
        this.loading$ = false;
        this.handleFirebaseError(error);
        this._logger.error('Error starting MFA enrollment:', error);
      }
    });
  }

  /**
   * Complete MFA enrollment with verification code
   */
  public completeMfaEnrollment(): void {
    if (!this.verificationCode || !this.verificationId) {
      this.errorMessage = 'Verification code is required';
      this.addDebugLog('❌ MFA enrollment failed - missing verification code or ID');
      this._logger.log('❌ MFA enrollment failed - missing verification code or ID');
      return;
    }

    this.addDebugLog('📱 Starting MFA enrollment completion...');
    this.addDebugLog('🔐 Verification ID: ' + this.verificationId.substring(0, 10) + '...');
    this.addDebugLog('📝 Verification Code: ' + this.verificationCode);
    this._logger.log('📱 Starting MFA enrollment completion...');
    this.loading$ = true;
    this.clearMessages();

    this._firebaseAuthService.completeMfaEnrollment(this.verificationId, this.verificationCode, 'Phone').subscribe({
      next: () => {
        this.addDebugLog('✅ MFA enrollment completed successfully - proceeding to backend validation');
        this._logger.log('✅ MFA enrollment completed successfully - proceeding to backend validation');
        // Now that MFA is enrolled, proceed to backend validation
        if (this.currentAuthResult) {
          this.addDebugLog('👤 Current auth result available - validating with backend');
          this._logger.log('👤 Current auth result available - validating with backend');
          this.validateUserWithBackend(this.currentAuthResult.user);
        } else {
          this.addDebugLog('❌ No current auth result available after MFA enrollment');
          this._logger.error('❌ No current auth result available after MFA enrollment');
          this.loading$ = false;
          this.errorMessage = 'Authentication error - please try signing in again';
        }
      },
      error: (error) => {
        this.loading$ = false;
        this.addDebugLog('❌ Error completing MFA enrollment: ' + (error.message || error.code));
        this._logger.error('❌ Error completing MFA enrollment:', error);
        this.handleFirebaseError(error);
      }
    });
  }

  /**
   * Complete forced MFA verification with verification code
   */
  public completeForcedMfaVerification(): void {
    if (!this.verificationCode || !this.verificationId) {
      this.errorMessage = 'Verification code is required';
      this.addDebugLog('❌ Forced MFA verification failed - missing verification code or ID');
      this._logger.log('❌ Forced MFA verification failed - missing verification code or ID');
      return;
    }

    this.addDebugLog('🔐 Starting forced MFA verification completion...');
    this.addDebugLog('🔐 Verification ID: ' + this.verificationId.substring(0, 10) + '...');
    this.addDebugLog('📝 Verification Code: ' + this.verificationCode);
    this._logger.log('🔐 Starting forced MFA verification completion...');
    this.loading$ = true;
    this.clearMessages();

    this._firebaseAuthService.completeForcedMfaVerification(this.verificationId, this.verificationCode).subscribe({
      next: (firebaseUser) => {
        this.addDebugLog('✅ Forced MFA verification completed successfully - proceeding to backend validation');
        this.addDebugLog('👤 Firebase user from forced MFA: ' + firebaseUser.uid + ' - ' + firebaseUser.email);
        this._logger.log('✅ Forced MFA verification completed successfully - proceeding to backend validation');
        this._logger.log('👤 Firebase user from forced MFA:', firebaseUser.uid, firebaseUser.email);
        this.validateUserWithBackend(firebaseUser);
      },
      error: (error) => {
        this.loading$ = false;
        this.addDebugLog('❌ Error completing forced MFA verification: ' + (error.message || error.code));
        this._logger.error('❌ Error completing forced MFA verification:', error);
        this.handleFirebaseError(error);
      }
    });
  }

  /**
   * Skip back to login options (for testing only - remove in production)
   */
  public skipToLogin(): void {
    this.phase = 'initial';
    this.currentAuthResult = null;
    this.clearMessages();
    this.loading$ = false;
  }

  /**
   * Legacy method for backward compatibility (not used in new flow)
   */
  onSubmit(_form: FormGroup) {
    // This method is kept for backward compatibility but not used in Firebase flow
    this._logger.log('Legacy onSubmit called - this should not happen in Firebase flow');
  }

  displayName() {
    this.userName = true;
    // Get name from local storage (using underscore prefix to avoid linting error)
    const _userName = this._localStorage.get('name');
  }
}
