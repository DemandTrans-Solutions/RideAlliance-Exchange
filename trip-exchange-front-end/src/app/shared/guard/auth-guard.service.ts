import { Injectable } from '@angular/core';
import {
  CanActivate,
  Router,
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
  UrlTree,
} from '@angular/router';
import { Observable } from 'rxjs';
import { LoginService } from '../../login/login.service';
import { Logger } from '../service/default-log.service';
import { LocalStorageService } from '../service/local-storage.service';
import { NotificationEmitterService } from '../service/notification-emitter.service';
import { FirebaseAuthService } from '../service/firebase-auth.service';

@Injectable({
  providedIn: 'root',
})
export class AuthGuard implements CanActivate {
  constructor(
    private _authService: LoginService,
    private _router: Router,
    private _logger: Logger,
    private _localStorage: LocalStorageService,
    private _notificationService: NotificationEmitterService,
    private _firebaseAuthService: FirebaseAuthService
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    const url: string = state.url;

    if (!this.checkLogin()) {
      // Navigate to the login page if user is not authenticated
      this._router.navigate(['/login']);
      return false;
    }

    // Check role-based access
    const role = this._localStorage.get('Role');

    // If it's an admin route, verify admin role
    if (url.includes('/admin')) {
      if (role === 'ROLE_ADMIN' || role === 'ROLE_PROVIDERADMIN') {
        return true;
      } else {
        this._notificationService.error(
          'Error Message',
          'Sorry, you are not authorized for that page'
        );
        this._router.navigate(['/tripTicket']);
        return false;
      }
    }

    // For provider admin routes
    if (
      url.includes('/providerPartners') &&
      role !== 'ROLE_ADMIN' &&
      role !== 'ROLE_PROVIDERADMIN'
    ) {
      this._notificationService.error(
        'Error Message',
        'Sorry, you are not authorized for that page'
      );
      this._router.navigate(['/tripTicket']);
      return false;
    }

    // For all other authenticated routes
    return true;
  }

  // Check if the user is logged in
  private checkLogin(): boolean {
    // First check Firebase authentication
    const isFirebaseAuthenticated = this._firebaseAuthService.isAuthenticated();

    // Then check traditional authentication (for backward compatibility)
    const isTraditionalAuthenticated = this._authService.isLoggedIn();

    // Also check if we have user data in localStorage (indicates successful backend validation)
    const userId = this._localStorage.get('userId');
    const hasUserData = userId !== undefined && userId !== null && userId !== '';

    this._logger.log('Auth check:', {
      firebase: isFirebaseAuthenticated,
      traditional: isTraditionalAuthenticated,
      hasUserData: hasUserData
    });

    // User is considered logged in if:
    // 1. They are authenticated with Firebase AND have valid user data from backend
    // 2. OR they are authenticated traditionally (for backward compatibility)
    return (isFirebaseAuthenticated && hasUserData) || isTraditionalAuthenticated;
  }
}
