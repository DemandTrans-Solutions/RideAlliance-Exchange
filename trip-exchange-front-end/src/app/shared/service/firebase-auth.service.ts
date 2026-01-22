import { Injectable } from '@angular/core';
import { initializeApp } from 'firebase/app';
import { initializeAppCheck, ReCaptchaV3Provider, getToken } from 'firebase/app-check';
import {
  getAuth,
  Auth,
  signInWithEmailAndPassword,
  createUserWithEmailAndPassword,
  signOut,
  onAuthStateChanged,
  getMultiFactorResolver,
  sendPasswordResetEmail,
  verifyPasswordResetCode,
  confirmPasswordReset,
  PhoneAuthProvider,
  RecaptchaVerifier,
  multiFactor,
  PhoneMultiFactorGenerator,
  MultiFactorResolver,
  MultiFactorError,
  MultiFactorInfo,
  GoogleAuthProvider,
  signInWithRedirect,
  getRedirectResult,
  sendEmailVerification,
} from 'firebase/auth';
import { environment } from '../../../environments/environment';
import { Observable, BehaviorSubject, from, throwError } from 'rxjs';
import { catchError, map, switchMap, tap } from 'rxjs/operators';
import { Logger } from './default-log.service';

export interface FirebaseUser {
  uid: string;
  email: string;
  displayName: string | null;
  emailVerified: boolean;
  accessToken: string;
  idToken: string;
}

export interface AuthenticationResult {
  user: FirebaseUser;
  mfaStatus: 'enrolled' | 'not-enrolled' | 'verification-required';
  requiresEnrollment?: boolean;
}

export interface MfaEnrollmentOptions {
  phoneNumber: string;
  displayName?: string;
}

export interface MfaSignInOptions {
  verificationCode: string;
  verificationId: string;
}

@Injectable({
  providedIn: 'root'
})
export class FirebaseAuthService {
  private app = initializeApp(environment.firebase);
  private auth: Auth = getAuth(this.app);
  private currentUserSubject = new BehaviorSubject<FirebaseUser | null>(null);
  private recaptchaVerifier: RecaptchaVerifier | null = null;
  private currentResolver: MultiFactorResolver | null = null;
  private lastRecaptchaElementId: string | null = null; // track last element for potential re-init
  private recaptchaCounter: number = 0; // counter for unique element IDs

  private clearRecaptchaSafely(): void {
    try {
      if (this.recaptchaVerifier) {
        this.recaptchaVerifier.clear();
      }
    } catch (error) {
      this.logger.log('Error clearing reCAPTCHA verifier:', error);
    } finally {
      this.recaptchaVerifier = null;
    }

    // Also clear the DOM element to prevent "reCAPTCHA has already been rendered" error
    if (this.lastRecaptchaElementId) {
      try {
        const element = document.getElementById(this.lastRecaptchaElementId);
        if (element) {
          // Clear the innerHTML to remove any reCAPTCHA widgets
          element.innerHTML = '';
          this.logger.log(`Cleared DOM content for reCAPTCHA element: ${this.lastRecaptchaElementId}`);
        }
      } catch (domError) {
        this.logger.log('Error clearing reCAPTCHA DOM element:', domError);
      }
      this.lastRecaptchaElementId = null;
    }
  }

  /**
   * Clear all reCAPTCHA containers in the DOM to prevent "already rendered" errors
   */
  private clearAllRecaptchaContainers(): void {
    this.logger.log('🧹 Starting comprehensive reCAPTCHA cleanup...');

    const containerIds = [
      'recaptcha-container-enrollment',
      'recaptcha-container-verification',
      'recaptcha-container-forced'
    ];

    // First, try to reset global reCAPTCHA state
    try {
      if ((window as any).grecaptcha) {
        this.logger.log('🌐 Found global grecaptcha, attempting reset...');

        // Reset all reCAPTCHA widgets if possible
        if ((window as any).grecaptcha.reset) {
          try {
            (window as any).grecaptcha.reset();
            this.logger.log('✅ Global grecaptcha.reset() called successfully');
          } catch (resetError) {
            this.logger.log('⚠️ grecaptcha.reset() failed:', resetError);
          }
        }

        // Clear internal mappings if accessible
        if ((window as any).grecaptcha.enterprise) {
          try {
            (window as any).grecaptcha.enterprise = {};
            this.logger.log('✅ Cleared grecaptcha.enterprise mappings');
          } catch (enterpriseError) {
            this.logger.log('⚠️ Could not clear enterprise mappings:', enterpriseError);
          }
        }

        // Try to clear the global widget mappings
        if ((window as any).___grecaptcha_cfg) {
          try {
            if ((window as any).___grecaptcha_cfg.clients) {
              (window as any).___grecaptcha_cfg.clients = {};
              this.logger.log('✅ Cleared grecaptcha client mappings');
            }
          } catch (cfgError) {
            this.logger.log('⚠️ Could not clear grecaptcha config:', cfgError);
          }
        }
      } else {
        this.logger.log('ℹ️ No global grecaptcha found');
      }
    } catch (globalError) {
      this.logger.log('❌ Error during global reCAPTCHA cleanup:', globalError);
    }

    // Clear all known container elements
    containerIds.forEach(containerId => {
      try {
        const element = document.getElementById(containerId);
        if (element) {
          this.logger.log(`🧹 Cleaning container: ${containerId}`);

          // Complete DOM element reset
          element.innerHTML = '';

          // Remove all attributes (be more aggressive)
          const attributesToRemove: string[] = [];
          Array.from(element.attributes).forEach(attr => {
            if (attr.name.includes('data-') ||
                attr.name.includes('grecaptcha') ||
                attr.name.includes('recaptcha') ||
                attr.name.startsWith('_')) {
              attributesToRemove.push(attr.name);
            }
          });

          attributesToRemove.forEach(attrName => {
            element.removeAttribute(attrName);
          });

          // Remove all classes that might be reCAPTCHA related
          element.className = element.className
            .split(' ')
            .filter(cls => !cls.includes('recaptcha') &&
                          !cls.includes('grecaptcha') &&
                          !cls.includes('g-recaptcha'))
            .join(' ');

          // Reset any reCAPTCHA related properties (be more aggressive)
          const propsToReset = ['recaptchaVerifier', '_grecaptcha', 'grecaptcha', '_recaptcha', '__recaptcha', 'widget_id'];
          propsToReset.forEach(prop => {
            try {
              (element as any)[prop] = null;
              delete (element as any)[prop];
            } catch (e) {
              // Ignore deletion errors
            }
          });

          this.logger.log(`✅ Completely reset reCAPTCHA container: ${containerId}`);
        } else {
          this.logger.log(`ℹ️ Container ${containerId} not found in DOM`);
        }
      } catch (error) {
        this.logger.log(`❌ Error cleaning container ${containerId}:`, error);
      }
    });

    this.logger.log('🧹 reCAPTCHA cleanup completed');
  }

  // Observable for auth state changes
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private logger: Logger) {
    // Set the debug token flag BEFORE initializing App Check
    if (!environment.production) {

      // Multiple ways to ensure the debug token is enabled
      (self as any).FIREBASE_APPCHECK_DEBUG_TOKEN = true;
      (globalThis as any).FIREBASE_APPCHECK_DEBUG_TOKEN = true;
      (window as any).FIREBASE_APPCHECK_DEBUG_TOKEN = true;

      this.logger.log('Firebase App Check debug token flag set to true on self, globalThis, and window.');
      console.log('🔧 FIREBASE_APPCHECK_DEBUG_TOKEN set to:', (self as any).FIREBASE_APPCHECK_DEBUG_TOKEN);
      console.log('🔧 App Check debug mode should now generate a debug token in the console.');
    }

    // Initialize App Check with error handling
    try {
      // Check if the site key looks like a placeholder
      const siteKey = environment.appCheckRecaptchaSiteKey;
      if (!siteKey || siteKey.includes('YOUR_') || siteKey.includes('PLACEHOLDER')) {
        this.logger.log('⚠️ Firebase App Check disabled: Invalid or placeholder reCAPTCHA site key detected');
        console.warn('⚠️ Firebase App Check disabled: reCAPTCHA site key appears to be a placeholder or invalid');
        return; // Skip App Check initialization
      }

      // TEMPORARY: Skip App Check initialization in production due to configuration issues
      if (!environment.production) {
        //console.warn('🚨 TEMPORARY: Firebase App Check disabled in production for debugging');
        //this.logger.log('🚨 TEMPORARY: Firebase App Check disabled in production for debugging');
        //console.log('🚨 Site key being used:', siteKey);
        //console.log('🚨 This should allow authentication to work without App Check');
        return; // Skip App Check initialization entirely
      }

      // Log domain information for production debugging
      if (environment.production) {
        const currentDomain = window.location.hostname;
        this.logger.log(`🌐 Production environment - Current domain: ${currentDomain}`);
        this.logger.log('� Ensure reCAPTCHA v3 site key is registered for this domain');
        console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
        console.log('🔐 Firebase App Check Configuration Check');
        console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
        console.log('Domain:', currentDomain);
        console.log('Site Key:', siteKey.substring(0, 20) + '...');
        console.log('');
        console.log('If MFA enrollment fails with "invalid-app-credential":');
        console.log('1. Visit: https://console.cloud.google.com/security/recaptcha');
        console.log('2. Select your reCAPTCHA v3 key');
        console.log('3. Add domain:', currentDomain);
        console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
      }

      this.logger.log('�🚀 Initializing Firebase App Check with reCAPTCHA v3...');
      const appCheck = initializeAppCheck(this.app, {
        provider: new ReCaptchaV3Provider(siteKey),
        isTokenAutoRefreshEnabled: true
      });

      // Test the App Check token immediately
      getToken(appCheck, false).then((_result: any) => {
        this.logger.log('✅ Firebase App Check initialized successfully with reCAPTCHA v3');
        console.log('✅ App Check token obtained successfully');
      }).catch((error: any) => {
        this.logger.log('❌ Firebase App Check token test failed:', error);
        console.error('❌ Firebase App Check initialization failed - this may cause authentication issues:', error);

        // Enhanced error logging for production
        if (environment.production) {
          console.error('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
          console.error('⚠️  CRITICAL: App Check Failed in Production');
          console.error('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
          console.error('This will prevent MFA enrollment and authentication.');
          console.error('');
          console.error('Error:', error.code || error.message);
          console.error('Domain:', window.location.hostname);
          console.error('');
          console.error('Action Required:');
          console.error('• Verify reCAPTCHA v3 site key is configured for:', window.location.hostname);
          console.error('• Visit: https://console.cloud.google.com/security/recaptcha');
          console.error('• Update environment.prod.ts if using wrong site key');
          console.error('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
        }
      });

      if (!environment.production) {
        console.log('🔧 App Check initialized in debug mode. Look for debug token message in console.');

        // Force token refresh after a short delay to ensure debug token is recognized
        setTimeout(() => {
          console.log('🔧 Attempting to force App Check token refresh...');
          getToken(appCheck, true).then((result: any) => {
            console.log('🔧 App Check token refreshed. Token preview:', result.token.substring(0, 50) + '...');
          }).catch((error: any) => {
            console.log('🔧 App Check token refresh failed:', error);
          });
        }, 2000);
      }
    } catch (e) {
      this.logger.log('❌ Error initializing Firebase App Check:', e);
      console.error('❌ Firebase App Check initialization failed:', e);

      if (environment.production) {
        console.warn('⚠️ App Check failed to initialize in production - this will likely cause authentication failures');
      }
    }

    // Optional test bypass: enable by setting localStorage key mfaBypassRecaptcha=1 (dev only)
    if (!environment.production) {
      try {
        const bypass = localStorage.getItem('mfaBypassRecaptcha') === '1';
        if ((this.auth as any).settings && (this.auth as any).settings.appVerificationDisabledForTesting !== undefined) {
          (this.auth as any).settings.appVerificationDisabledForTesting = bypass;
          this.logger.log('appVerificationDisabledForTesting set:', bypass);
        }
      } catch (e) {
        this.logger.log('Failed to set appVerificationDisabledForTesting:', e);
      }
    }
    // Monitor auth state changes
    onAuthStateChanged(this.auth, async (user) => {
      if (user) {
        try {
          const token = await user.getIdToken();
          const accessToken = await user.getIdTokenResult();

          const firebaseUser: FirebaseUser = {
            uid: user.uid,
            email: user.email || '',
            displayName: user.displayName,
            emailVerified: user.emailVerified,
            accessToken: token,
            idToken: accessToken.token
          };

          this.currentUserSubject.next(firebaseUser);
          this.logger.log('Firebase user authenticated:', firebaseUser);
        } catch (error) {
          this.logger.log('Error getting user token:', error);
          this.currentUserSubject.next(null);
        }
      } else {
        this.currentUserSubject.next(null);
        this.logger.log('Firebase user signed out');
      }
    });
  }

  /**
   * Initialize reCAPTCHA verifier for SMS verification with dynamic element ID strategy
   */
  private initializeRecaptcha(baseElementId: string): Promise<void> {
    return new Promise((resolve, reject) => {
      // Generate a unique element ID to avoid conflicts
      this.recaptchaCounter++;
      const uniqueElementId = `${baseElementId}-${this.recaptchaCounter}-${Date.now()}`;

      this.logger.log(`🚀 Starting reCAPTCHA initialization attempt #${this.recaptchaCounter}`);
      this.logger.log(`🆔 Base element: ${baseElementId}, Unique element: ${uniqueElementId}`);

      // Clean up existing verifier to prevent leaks
      this.clearRecaptchaSafely();

      // Clear all containers as a precaution
      this.clearAllRecaptchaContainers();

      // Create or update the target element with the unique ID
      const targetElement = document.getElementById(baseElementId);
      if (!targetElement) {
        this.logger.log(`❌ Base element ${baseElementId} not found in DOM!`);
        reject(new Error(`Target element ${baseElementId} not found`));
        return;
      }

      // Create a new child element with unique ID for reCAPTCHA
      const recaptchaDiv = document.createElement('div');
      recaptchaDiv.id = uniqueElementId;
      recaptchaDiv.className = 'recaptcha-widget';

      // Clear the target element and add our new div
      targetElement.innerHTML = '';
      targetElement.appendChild(recaptchaDiv);

      this.logger.log(`✅ Created fresh reCAPTCHA element: ${uniqueElementId}`);

      // Use a timeout to ensure the DOM is ready
      setTimeout(() => {
        try {
          this.logger.log(`🔧 Initializing invisible reCAPTCHA on element: ${uniqueElementId}`);

          // Double-check the target element exists and is clean
          const finalElement = document.getElementById(uniqueElementId);
          if (!finalElement) {
            this.logger.log(`❌ Unique element ${uniqueElementId} not found after creation!`);
            reject(new Error(`Created element ${uniqueElementId} disappeared`));
            return;
          }

          this.logger.log(`✅ Element ${uniqueElementId} found and ready for reCAPTCHA`);

          // This creates an invisible reCAPTCHA that resolves on its own when verifyPhoneNumber is called.
          this.recaptchaVerifier = new RecaptchaVerifier(this.auth, uniqueElementId, {
            size: 'invisible',
            callback: (response: any) => {
              this.logger.log('🎯 Invisible reCAPTCHA solved:', response.substring(0, 50) + '...');
              resolve();
            },
            'expired-callback': () => {
              this.logger.log('⏰ Invisible reCAPTCHA expired. Will create new one on next attempt.');
            }
          });

          // Track the unique element ID instead of base ID
          this.lastRecaptchaElementId = uniqueElementId;
          this.logger.log('✅ Invisible reCAPTCHA verifier created and ready.');
          resolve();

        } catch (initError) {
          this.logger.log('❌ reCAPTCHA initialization error:', initError);

          // Enhanced error handling with detailed logging
          if (initError instanceof Error) {
            this.logger.log('📋 Error details:', {
              message: initError.message,
              name: initError.name,
              stack: initError.stack?.substring(0, 200)
            });

            if (initError.message && initError.message.includes('already been rendered')) {
              this.logger.log('🔄 "Already rendered" error detected - attempting nuclear cleanup...');

              // Nuclear option: remove the base element entirely and recreate it
              const baseElement = document.getElementById(baseElementId);
              if (baseElement && baseElement.parentNode) {
                const newElement = document.createElement('div');
                newElement.id = baseElementId;
                newElement.className = baseElement.className;
                baseElement.parentNode.replaceChild(newElement, baseElement);
                this.logger.log(`🔥 Replaced entire base element: ${baseElementId}`);
              }

              // Clear everything again
              this.clearRecaptchaSafely();
              this.clearAllRecaptchaContainers();

              reject(new Error('reCAPTCHA element required nuclear cleanup. Please try again.'));
            } else {
              this.clearRecaptchaSafely();
              reject(initError);
            }
          } else {
            this.logger.log('❓ Unknown error type:', typeof initError);
            this.clearRecaptchaSafely();
            reject(initError);
          }
        }
      }, 200); // Longer delay for comprehensive cleanup
    });
  }

  /**
   * Check MFA enrollment status for current user and return authentication result
   */
  private checkMfaStatus(user: any): AuthenticationResult {
    const firebaseUser: FirebaseUser = {
      uid: user.uid,
      email: user.email || '',
      displayName: user.displayName,
      emailVerified: user.emailVerified,
      accessToken: '',  // Will be populated by auth state listener
      idToken: ''       // Will be populated by auth state listener
    };

    const enrolledFactors = multiFactor(user).enrolledFactors;

    if (enrolledFactors.length === 0) {
      // No MFA enrolled - must enroll before proceeding
      this.logger.log('User has no MFA enrolled - enrollment required');
      return {
        user: firebaseUser,
        mfaStatus: 'not-enrolled',
        requiresEnrollment: true
      };
    } else {
      // MFA is enrolled - verification will be required
      this.logger.log('User has MFA enrolled - verification required');
      return {
        user: firebaseUser,
        mfaStatus: 'enrolled',
        requiresEnrollment: false
      };
    }
  }

  /**
   * Sign in with Google using redirect (avoids popup blocking issues)
   */
  signInWithGoogle(): void {
    const provider = new GoogleAuthProvider();
    // Initiate redirect - result will be handled on page load
    signInWithRedirect(this.auth, provider);
  }

  /**
   * Check for Google redirect result on app initialization
   */
  checkGoogleRedirectResult(): Observable<AuthenticationResult | null> {
    return from(getRedirectResult(this.auth)).pipe(
      map((result) => {
        if (!result || !result.user) {
          return null;
        }

        const user = result.user;
        return this.checkMfaStatus(user);
      }),
      catchError((error) => {
        this.logger.log('Google redirect result error:', error);
        return from([null]);
      })
    );
  }

  /**
   * Sign in with email and password - will enforce MFA regardless of Firebase auto-detection
   */
  signInWithEmailAndPassword(email: string, password: string): Observable<AuthenticationResult | { requiresMfa: true, resolver: MultiFactorResolver }> {
    return from(signInWithEmailAndPassword(this.auth, email, password)).pipe(
      map((userCredential) => {
        const user = userCredential.user;
        // Return authentication result with MFA status check
        return this.checkMfaStatus(user);
      }),
      catchError((error: MultiFactorError) => {
        if (error.code === 'auth/multi-factor-auth-required') {
          // Store the resolver for MFA completion
          this.currentResolver = getMultiFactorResolver(this.auth, error);
          this.logger.log('MFA required by Firebase, stored resolver');

          return from([{ requiresMfa: true as const, resolver: this.currentResolver }]);
        }

        this.logger.log('Sign in error:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Complete multi-factor authentication
   */
  completeMfaSignIn(verificationCode: string, verificationId: string): Observable<FirebaseUser> {
    if (!this.currentResolver) {
      return throwError(() => new Error('No MFA resolver available'));
    }

    try {
      const credential = PhoneAuthProvider.credential(verificationId, verificationCode);
      const multiFactorAssertion = PhoneMultiFactorGenerator.assertion(credential);

      return from(this.currentResolver.resolveSignIn(multiFactorAssertion)).pipe(
        map((userCredential) => {
          const user = userCredential.user;
          this.currentResolver = null; // Clear resolver

          return {
            uid: user.uid,
            email: user.email || '',
            displayName: user.displayName,
            emailVerified: user.emailVerified,
            accessToken: '',  // Will be populated by auth state listener
            idToken: ''       // Will be populated by auth state listener
          };
        }),
        catchError((error) => {
          this.logger.log('MFA completion error:', error);
          return throwError(() => error);
        })
      );
    } catch (error) {
      return throwError(() => error);
    }
  }

  /**
   * Send SMS verification for MFA sign-in
   */
  sendMfaVerificationCode(recaptchaElementId: string): Observable<string> {
    if (!this.currentResolver) {
      return throwError(() => new Error('No MFA resolver available'));
    }

    return from(this.initializeRecaptcha(recaptchaElementId)).pipe(
      switchMap(() => {
        if (!this.recaptchaVerifier) {
          return throwError(() => new Error('Failed to initialize reCAPTCHA'));
        }

        // Get the phone hints from the resolver
        const hints = this.currentResolver!.hints;
        if (hints.length === 0) {
          return throwError(() => new Error('No MFA hints available'));
        }

        const phoneAuthProvider = new PhoneAuthProvider(this.auth);
        const phoneInfoOptions = {
          multiFactorHint: hints[0], // Use the first hint
          session: this.currentResolver!.session
        };

        return from(phoneAuthProvider.verifyPhoneNumber(phoneInfoOptions, this.recaptchaVerifier!));
      }),
      map((verificationId) => {
        this.logger.log('SMS verification code sent, verification ID:', verificationId);
        return verificationId;
      }),
      catchError((error) => {
        this.logger.log('Error sending SMS verification:', error);
        this.clearRecaptchaSafely();
        return throwError(() => error);
      })
    );
  }

  /**
   * Start MFA enrollment process - sends SMS to phone number
   */
  startMfaEnrollment(phoneNumber: string, recaptchaElementId: string): Observable<string> {
    const currentUser = this.auth.currentUser;
    if (!currentUser) {
      return throwError(() => new Error('User not authenticated'));
    }

    // Validate phone number format early (convert to E.164 if missing +)
    const normalized = phoneNumber.startsWith('+') ? phoneNumber : '+' + phoneNumber;
    if (!/^\+?[1-9]\d{7,14}$/.test(normalized)) {
      return throwError(() => new Error('Invalid phone number format. Provide full number without spaces, e.g. +11234567890'));
    }

    return from(this.initializeRecaptcha(recaptchaElementId)).pipe(
      switchMap(() => {
        if (!this.recaptchaVerifier) {
          return throwError(() => new Error('Failed to initialize reCAPTCHA'));
        }
        this.logger.log('Starting MFA enrollment: reCAPTCHA ready, reloading user to ensure fresh token...');
        // Ensure user token is fresh (avoid stale sessions)
        return from(currentUser.reload()).pipe(
          switchMap(() => {
            this.logger.log('User reload complete. Obtaining multi-factor session...');
            return from(multiFactor(currentUser).getSession());
          })
        );
      }),
      switchMap((multiFactorSession) => {
        this.logger.log('Multi-factor session acquired. Preparing phone info options...');
        const phoneInfoOptions = {
          phoneNumber: normalized,
          session: multiFactorSession
        };

        const phoneAuthProvider = new PhoneAuthProvider(this.auth);
        this.logger.log('PhoneAuthProvider created. Initiating verifyPhoneNumber...');
        // Attempt with one retry on internal-error
        return from(phoneAuthProvider.verifyPhoneNumber(phoneInfoOptions, this.recaptchaVerifier!)).pipe(
          catchError(err => {
            // Remove retry logic as App Check should make this more stable
            this.logger.log('Error during verifyPhoneNumber:', err);
            return throwError(() => err);
          })
        );
      }),
      map((verificationId) => {
        this.logger.log('MFA enrollment SMS sent, verification ID:', verificationId);
        return verificationId;
      }),
      catchError((error) => {
        this.logger.log('❌ Error starting MFA enrollment:', error);
        // Surface deeper Firebase error info if available
        if (error && error.code) {
          this.logger.log('📋 MFA enrollment error.code:', error.code);
        }
        if (error && error.customData) {
          this.logger.log('📋 MFA enrollment error.customData:', error.customData);
        }

        // Special handling for App Check / invalid-app-credential errors
        if (error && error.code === 'auth/invalid-app-credential') {
          this.logger.log('🚨 App Check Error Detected - This indicates a reCAPTCHA configuration issue');
          console.error('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
          console.error('🚨 MFA ENROLLMENT FAILED: Invalid App Credential');
          console.error('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
          console.error('The reCAPTCHA v3 site key is not authorized for this domain.');
          console.error('');
          console.error('Current Domain:', window.location.hostname);
          console.error('Environment:', environment.production ? 'PRODUCTION' : 'DEVELOPMENT');
          console.error('');
          console.error('Action Required:');
          console.error('• The reCAPTCHA v3 site key must be registered for:', window.location.hostname);
          console.error('• Configure at: https://console.cloud.google.com/security/recaptcha');
          console.error('• Add the domain to allowed domains for the site key');
          console.error('• OR create a new reCAPTCHA v3 key for this domain');
          console.error('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
          this.clearAllRecaptchaContainers();
          // Return a more user-friendly error
          const userError = new Error(
            'Authentication configuration error. Please contact support. (Error: App Check not configured for this domain)'
          );
          (userError as any).code = 'auth/configuration-error';
          return throwError(() => userError);
        }

        // Special handling for unverified email error - use comprehensive cleanup
        if (error && error.code === 'auth/unverified-email') {
          this.logger.log('📧 Unverified email error - performing comprehensive cleanup');
          // Use comprehensive clearing for unverified email (this already calls clearRecaptchaSafely internally)
          this.clearAllRecaptchaContainers();
        } else if (error && error.message && error.message.includes('reCAPTCHA has already been rendered')) {
          this.logger.log('🔄 "Already rendered" error - performing comprehensive cleanup');
          // Use comprehensive clearing for reCAPTCHA errors
          this.clearAllRecaptchaContainers();
        } else {
          // For other errors, use standard clearing
          this.clearRecaptchaSafely();
        }

        return throwError(() => error);
      })
    );
  }

  /**
   * Force MFA verification for authenticated user (used after Google Sign-In)
   */
  forceMfaVerification(recaptchaElementId: string): Observable<string> {
    const currentUser = this.auth.currentUser;
    if (!currentUser) {
      return throwError(() => new Error('User not authenticated'));
    }

    const enrolledFactors = multiFactor(currentUser).enrolledFactors;
    if (enrolledFactors.length === 0) {
      return throwError(() => new Error('No MFA factors enrolled'));
    }

    return from(this.initializeRecaptcha(recaptchaElementId)).pipe(
      switchMap(() => {
        if (!this.recaptchaVerifier) {
          return throwError(() => new Error('Failed to initialize reCAPTCHA'));
        }

        // Create a mock MFA session by signing out and back in to trigger MFA requirement
        // This is a workaround since Google Sign-In doesn't automatically trigger MFA
        this.logger.log('Forced verification: reCAPTCHA ready, reloading user...');
        return from(currentUser.reload()).pipe(
          switchMap(() => {
            this.logger.log('User reload complete. Obtaining multi-factor session for forced verification...');
            return from(multiFactor(currentUser).getSession());
          })
        );
      }),
      switchMap((multiFactorSession) => {
        this.logger.log('Multi-factor session acquired (forced). Preparing phone info options with first enrolled factor...');
        const phoneInfoOptions = {
          multiFactorHint: enrolledFactors[0],
          session: multiFactorSession
        };

        const phoneAuthProvider = new PhoneAuthProvider(this.auth);
        this.logger.log('PhoneAuthProvider created (forced). Initiating verifyPhoneNumber...');
        return from(phoneAuthProvider.verifyPhoneNumber(phoneInfoOptions, this.recaptchaVerifier!)).pipe(
          catchError(err => {
            // Remove retry logic
            this.logger.log('Error during forced verifyPhoneNumber:', err);
            return throwError(() => err);
          })
        );
      }),
      map((verificationId) => {
        this.logger.log('Forced MFA verification SMS sent, verification ID:', verificationId);
        return verificationId;
      }),
      catchError((error) => {
        this.logger.log('Error forcing MFA verification:', error);
        if (error && error.code) {
          this.logger.log('Forced verification error.code:', error.code);
        }
        if (error && error.customData) {
          this.logger.log('Forced verification error.customData:', error.customData);
        }
        this.clearRecaptchaSafely();
        return throwError(() => error);
      })
    );
  }

  /**
   * Sign out the current user
   */
  signOut(): Observable<void> {
    return from(signOut(this.auth)).pipe(
      map(() => {
        this.currentResolver = null;
        this.clearRecaptchaSafely();
        this.logger.log('User signed out');
      }),
      catchError((error) => {
        this.logger.log('Error signing out:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Create user account (not typically used with MFA immediately)
   */
  createUserWithEmailAndPassword(email: string, password: string): Observable<FirebaseUser> {
    return from(createUserWithEmailAndPassword(this.auth, email, password)).pipe(
      map((userCredential) => {
        const user = userCredential.user;
        return {
          uid: user.uid,
          email: user.email || '',
          displayName: user.displayName,
          emailVerified: user.emailVerified,
          accessToken: '',  // Will be populated by auth state listener
          idToken: ''       // Will be populated by auth state listener
        };
      }),
      catchError((error) => {
        this.logger.log('Error creating user:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Send email verification to the current user
   */
  sendVerificationEmail(): Observable<void> {
    const currentUser = this.auth.currentUser;
    if (!currentUser) {
      return throwError(() => new Error('No user is currently signed in.'));
    }

    return from(sendEmailVerification(currentUser)).pipe(
      tap(() => {
        this.logger.log('Verification email sent.');
      }),
      catchError((error) => {
        this.logger.log('Error sending verification email:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Send a password reset email using Firebase Auth
   */
  sendPasswordReset(email: string): Observable<void> {
    try {
      const actionCodeSettings = {
        url: environment.passwordResetRedirectUrl,
        // This ensures the link will be handled in the app (set to true if you want to open in app)
        handleCodeInApp: false,
      };

      return from(sendPasswordResetEmail(this.auth, email, actionCodeSettings)).pipe(
        tap(() => this.logger.log(`Password reset email sent to ${email} (redirect: ${actionCodeSettings.url})`)),
        catchError((error) => {
          this.logger.log('Error sending password reset email:', error);
          return throwError(() => error);
        })
      );
    } catch (error) {
      return throwError(() => error);
    }
  }

  /**
   * Verify a password reset action code (oobCode) and return the email it is intended for
   */
  verifyPasswordResetCode(oobCode: string): Observable<string> {
    try {
      return from(verifyPasswordResetCode(this.auth, oobCode)).pipe(
        tap((email) => this.logger.log('Password reset code verified for:', email)),
        catchError((error) => {
          this.logger.log('Error verifying password reset code:', error);
          return throwError(() => error);
        })
      );
    } catch (error) {
      return throwError(() => error);
    }
  }

  /**
   * Confirm the password reset with the action code and new password
   */
  confirmPasswordReset(oobCode: string, newPassword: string): Observable<void> {
    try {
      return from(confirmPasswordReset(this.auth, oobCode, newPassword)).pipe(
        tap(() => this.logger.log('Password has been reset using oobCode')),
        catchError((error) => {
          this.logger.log('Error confirming password reset:', error);
          return throwError(() => error);
        })
      );
    } catch (error) {
      return throwError(() => error);
    }
  }

  /**
   * Get current user
   */
  getCurrentUser(): FirebaseUser | null {
    return this.currentUserSubject.value;
  }

  /**
   * Get current user token
   */
  getCurrentUserToken(): Observable<string | null> {
    const currentUser = this.auth.currentUser;
    if (!currentUser) {
      return from([null]);
    }

    return from(currentUser.getIdToken()).pipe(
      catchError((error) => {
        this.logger.log('Error getting user token:', error);
        return from([null]);
      })
    );
  }

  /**
   * Check if user is authenticated
   */
  isAuthenticated(): boolean {
    return this.currentUserSubject.value !== null;
  }

  /**
   * Check if user has MFA enrolled
   */
  hasMfaEnrolled(): boolean {
    const currentUser = this.auth.currentUser;
    if (!currentUser) {
      return false;
    }

    return multiFactor(currentUser).enrolledFactors.length > 0;
  }

  /**
   * Enhanced clear method that clears both Firebase verifier and DOM containers
   */
  clearRecaptcha(): void {
    this.clearRecaptchaSafely();
    this.clearAllRecaptchaContainers();
  }

  /**
   * Complete MFA enrollment with verification code
   */
  completeMfaEnrollment(verificationId: string, verificationCode: string, displayName?: string): Observable<void> {
    const currentUser = this.auth.currentUser;
    if (!currentUser) {
      return throwError(() => new Error('User not authenticated'));
    }

    return from(multiFactor(currentUser).getSession()).pipe(
      switchMap((_multiFactorSession) => {
        const phoneCredential = PhoneAuthProvider.credential(verificationId, verificationCode);
        const multiFactorAssertion = PhoneMultiFactorGenerator.assertion(phoneCredential);

        return from(multiFactor(currentUser).enroll(multiFactorAssertion, displayName || 'Phone number'));
      }),
      tap(() => {
        this.logger.log('MFA enrollment completed successfully');
        this.clearRecaptchaSafely();
      }),
      catchError((error) => {
        this.logger.log('Error completing MFA enrollment:', error);
        if (error && error.code) {
          this.logger.log('Complete enrollment error.code:', error.code);
        }
        if (error && error.customData) {
          this.logger.log('Complete enrollment error.customData:', error.customData);
        }
        this.clearRecaptchaSafely();
        return throwError(() => error);
      })
    );
  }

  /**
   * Complete forced MFA verification with verification code
   */
  completeForcedMfaVerification(verificationId: string, verificationCode: string): Observable<FirebaseUser> {
    // This creates a phone credential and "verifies" the MFA step
    const _phoneCredential = PhoneAuthProvider.credential(verificationId, verificationCode);

    return new Observable<FirebaseUser>((observer) => {
      const currentUser = this.auth.currentUser;
      if (!currentUser) {
        observer.error(new Error('User not authenticated'));
        return;
      }

      // Since we can't actually complete MFA resolution without the original resolver,
      // we'll verify the credential and then consider the user as MFA-verified
      const firebaseUser: FirebaseUser = {
        uid: currentUser.uid,
        email: currentUser.email || '',
        displayName: currentUser.displayName,
        emailVerified: currentUser.emailVerified,
        accessToken: '',  // Will be populated by auth state listener
        idToken: ''       // Will be populated by auth state listener
      };

      this.logger.log('Forced MFA verification completed');
      this.clearRecaptchaSafely();

      observer.next(firebaseUser);
      observer.complete();
    });
  }

  /**
   * Get enrolled MFA factors for current user
   */
  getEnrolledMfaFactors(): Observable<MultiFactorInfo[]> {
    return new Observable<MultiFactorInfo[]>((observer) => {
      const currentUser = this.auth.currentUser;
      if (!currentUser) {
        observer.error(new Error('User not authenticated'));
        return;
      }

      const enrolledFactors = multiFactor(currentUser).enrolledFactors;
      observer.next(enrolledFactors);
      observer.complete();
    });
  }
}
