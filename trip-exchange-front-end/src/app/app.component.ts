import { Component, OnInit, OnDestroy } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { ConfirmationService, MessageService } from 'primeng/api';
import { NotificationEmitterService } from './shared/service/notification-emitter.service';
import { ConfirmPopupEmitterService } from './shared/service/confirm-popup-emitter.service';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';

// Routes that render full-screen without the app header/footer chrome.
const AUTH_ROUTE_PREFIXES = [
  '/login',
  '/forgotPassword',
  '/reset-password',
  '/changePassword',
  '/changePasswordAfterLogin',
  '/activateAccount',
  '/setPasswordComponent',
];

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
})
export class AppComponent implements OnInit, OnDestroy {
  public subscriptions: Subscription[] = [];
  public isAuthRoute = false;

  constructor(
    public translate: TranslateService,
    public _notificationEmitterService: NotificationEmitterService,
    public _confirmPopupEmitterService: ConfirmPopupEmitterService,
    public _confirmationService: ConfirmationService,
    private messageService: MessageService,
    private router: Router
  ) {
    // Set default language
    translate.setDefaultLang('en');
    translate.use('en');
    this.isAuthRoute = this.matchesAuthRoute(this.router.url);
  }

  private matchesAuthRoute(url: string): boolean {
    const path = (url || '/').split('?')[0];
    if (path === '/' || path === '') return true;
    return AUTH_ROUTE_PREFIXES.some(p => path.startsWith(p));
  }

  ngOnInit(): void {
    this.subscriptions.push(
      this.router.events
        .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
        .subscribe(e => (this.isAuthRoute = this.matchesAuthRoute(e.urlAfterRedirects)))
    );

    // Subscribe to notifications using the new pattern
    this.subscriptions.push(
      this._notificationEmitterService.notifications$.subscribe(notification =>
        this.addNotification({
          notifType: notification.type,
          notifyTitle: notification.title,
          message: notification.message,
        })
      )
    );

    // Subscribe to confirm popup events
    this.subscriptions.push(
      this._confirmPopupEmitterService.invokePopup.subscribe(msg => this.confirmPopUp(msg))
    );
  }

  ngOnDestroy(): void {
    // Clean up subscriptions to prevent memory leaks
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  public confirmPopUp(msg: string): void {
    this._confirmationService.confirm({
      message: msg || 'Are you sure that you want to perform this action?',
      accept: () => {
        this._confirmPopupEmitterService.confirmResult(true);
      },
      reject: () => {
        this._confirmPopupEmitterService.confirmResult(false);
      },
    });
  }

  public addNotification(msg: any): void {
    this.messageService.add({
      severity: msg.notifType,
      summary: msg.notifyTitle,
      detail: msg.message,
      life: 3000,
    });
  }
}
