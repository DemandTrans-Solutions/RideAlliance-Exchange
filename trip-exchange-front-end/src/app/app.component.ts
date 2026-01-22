import { Component, OnInit, OnDestroy } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { ConfirmationService, MessageService } from 'primeng/api';
import { NotificationEmitterService } from './shared/service/notification-emitter.service';
import { ConfirmPopupEmitterService } from './shared/service/confirm-popup-emitter.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
})
export class AppComponent implements OnInit, OnDestroy {
  public subscriptions: Subscription[] = [];

  constructor(
    public translate: TranslateService,
    public _notificationEmitterService: NotificationEmitterService,
    public _confirmPopupEmitterService: ConfirmPopupEmitterService,
    public _confirmationService: ConfirmationService,
    private messageService: MessageService
  ) {
    // Set default language
    translate.setDefaultLang('en');
    translate.use('en');
  }

  ngOnInit(): void {
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
