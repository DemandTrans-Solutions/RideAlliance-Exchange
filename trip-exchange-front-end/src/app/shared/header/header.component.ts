import { Component, OnInit, OnDestroy, HostListener, Renderer2 } from '@angular/core';
import { Router, NavigationEnd, Event } from '@angular/router';
import { TokenService } from '../service/token.service';
import { LocalStorageService } from '../service/local-storage.service';
import { HeaderEmitterService } from '../service/header-emmiter.service';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
})
export class HeaderComponent implements OnInit, OnDestroy {
  // Class properties
  public logo: string;
  public provider = false;
  public user: boolean;
  public showGrid: boolean;
  public isPasswordExpired: any;
  public role: string;
  public userName: string;
  public providerName: string;
  public isLoginPage = false;
  public isAuthenticated = false;
  public isDropdownOpen = false; // New property to track dropdown state
  public isDarkMode = false; // New property to track dark mode state

  // Store subscriptions to unsubscribe on component destruction
  public subscriptions: Subscription[] = [];

  // Add click outside listener to close dropdown when clicking elsewhere
  @HostListener('document:click', ['$event'])
  clickOutside(event: MouseEvent): void {
    if (!(event.target as HTMLElement).closest('.user-dropdown')) {
      this.isDropdownOpen = false;
    }
  }

  constructor(
    public _tokenService: TokenService,
    public _router: Router,
    public _localStorage: LocalStorageService,
    public _headerEmitter: HeaderEmitterService,
    private renderer: Renderer2 // Add renderer to manipulate DOM elements
  ) {
    // Initialize properties from localStorage
    this.role = this._localStorage.get('Role');
    this.userName = this._localStorage.get('name');
    this.providerName = this._localStorage.get('providerName');

    // Check initial route and authentication status
    this.checkIfLoginPage(this._router.url);
    this.checkAuthStatus();

    // Initialize theme from localStorage or default to system preference
    this.initializeTheme();
  }

  ngOnInit(): void {
    // Subscribe to header updates
    this.subscriptions.push(this._headerEmitter.header.subscribe(msg => this.header(msg)));

    // Also subscribe to the more modern userName$ observable
    this.subscriptions.push(
      this._headerEmitter.userName$.subscribe(isVisible => {
        if (isVisible) {
          this.userName = this._localStorage.get('name');
        }
      })
    );
  }

  ngOnDestroy(): void {
    // Clean up subscriptions to prevent memory leaks
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  /**
   * Initialize theme based on localStorage setting or system preferences
   */
  private initializeTheme(): void {
    // Check if user has a saved theme preference
    const savedTheme = this._localStorage.get('theme');

    if (savedTheme === 'dark') {
      this.isDarkMode = true;
      this.applyDarkMode();
    } else if (savedTheme === 'light') {
      this.isDarkMode = false;
      this.applyLightMode();
    } else {
      // No saved preference, check if we can detect system preference
      // This keeps the default behavior when no manual choice is made
      this.isDarkMode =
        window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;

      // Set initial state based on system preference
      if (this.isDarkMode) {
        this.applyDarkMode();
      } else {
        this.applyLightMode();
      }
    }
  }

  /**
   * Toggle between light and dark mode
   */
  public toggleDarkMode(): void {
    console.log('Toggle dark mode clicked. Current state:', this.isDarkMode);
    this.isDarkMode = !this.isDarkMode;

    if (this.isDarkMode) {
      console.log('Applying dark mode');
      this.applyDarkMode();
      this._localStorage.set('theme', 'dark');
    } else {
      console.log('Applying light mode');
      this.applyLightMode();
      this._localStorage.set('theme', 'light');
    }
  }

  /**
   * Apply dark mode styles
   */
  private applyDarkMode(): void {
    // First remove light-theme class if it exists
    document.body.classList.remove('light-theme');
    // Then add dark-theme class
    document.body.classList.add('dark-theme');
    console.log('Body classes after dark mode applied:', document.body.className);
  }

  /**
   * Apply light mode styles
   */
  private applyLightMode(): void {
    // First remove dark-theme class if it exists
    document.body.classList.remove('dark-theme');
    // Then add light-theme class
    document.body.classList.add('light-theme');
    console.log('Body classes after light mode applied:', document.body.className);
  }

  onLogout(): void {

    this._tokenService.clearAll();
    this._localStorage.clearAll();
    this._router.navigate(['/login']);
    this._headerEmitter.header.emit(true);
  }

  routeAccordingly(): void {
    const role = this._localStorage.get('Role');
    if (role === 'ROLE_PROVIDERADMIN') {
      this._router.navigate(['/admin/users']);
    } else {
      this._router.navigate(['/admin/providers']);
    }
  }

  checkRole(): void {
    this.role = this._localStorage.get('Role');
  }

  displayName(): void {
    this.userName = this._localStorage.get('name');
  }

  header(msg: any): void {


    if (!this._tokenService.get()) {
      // User is logged out - clear all user data
      this.userName = '';
      this.providerName = '';
      this.role = '';
      this.isAuthenticated = false;
    } else {
      // User is logged in - update all user data from localStorage
      this.userName = this._localStorage.get('name');
      this.providerName = this._localStorage.get('providerName');
      this.role = this._localStorage.get('Role');
      this.isAuthenticated = true;
    }


  }

  // Navigation event handlers
  public checkPasswordAndNavigate(route: string[]): void {
    this.isPasswordExpired = this._localStorage.get('isPasswordExpired');
    if (this.isPasswordExpired !== 'true') {
      this._router.navigate(route);
    } else {
      this._router.navigate(['/changePasswordAfterLogin']);
    }
  }

  ticketEvent(): void {
    this.isPasswordExpired = this._localStorage.get('isPasswordExpired');
    if (this.isPasswordExpired !== 'true') {
      // Check if we're already on the trip ticket page
      if (this._router.url.startsWith('/tripTicket')) {
        // Store a flag in localStorage to force grid view refresh
        this._localStorage.set('forceGridView', 'true');
        // Navigate to the same route to trigger a refresh
        this._router.navigateByUrl('/', { skipLocationChange: true }).then(() => {
          this._router.navigate(['/tripTicket']);
        });
      } else {
        // Normal navigation if not already on trip ticket page
        this._router.navigate(['/tripTicket']);
      }
    } else {
      this._router.navigate(['/changePasswordAfterLogin']);
    }
  }

  providerEvent(): void {
    this.checkPasswordAndNavigate(['/admin/providers']);
  }

  bulkOperationEvent(): void {
    this.checkPasswordAndNavigate(['/tripTicket']);
  }

  reportsEvent(): void {
    this.checkPasswordAndNavigate(['/reports/summaryReport']);
  }

  // Toggle dropdown menu
  toggleDropdown(event: MouseEvent): void {
    event.stopPropagation(); // Prevent document click from immediately closing it
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  // Check if current route is login page or authentication pages
  private checkIfLoginPage(url: string): void {
    // Consider login, forgot password, activate account, etc. as unauthenticated pages
    // Remove the root path '/' from being considered as a login page
    const authPages = ['/login', '/forgotPassword', '/activateAccount', '/setPassword'];

    // Check if the URL exactly matches root path or starts with any of the auth pages
    if (url === '/') {
      // Special case for root path: only consider it a login page if user is not authenticated
      this.isLoginPage = !this._tokenService.get();
    } else {
      this.isLoginPage = authPages.some(page => url.startsWith(page));
    }


  }

  // Check user authentication status
  private checkAuthStatus(): void {
    const token = this._tokenService.get();
    const userId = this._localStorage.get('userId');
    this.isAuthenticated = !!token && !!userId;

    if (this.isAuthenticated) {
      // Refresh role and user info when authenticated
      this.role = this._localStorage.get('Role');
      this.userName = this._localStorage.get('name');
      this.providerName = this._localStorage.get('providerName');
    }
  }

  // Check if user has required role
  public hasRole(requiredRoles: string[]): boolean {
    if (!this.isAuthenticated) return false;

    const userRole = this._localStorage.get('Role');
    return requiredRoles.includes(userRole);
  }
}
