import { Injectable, EventEmitter } from '@angular/core';
import { Subject } from 'rxjs';

export interface Notification {
  type: 'success' | 'error' | 'warn' | 'info';
  title: string;
  message: string;
}

@Injectable({
  providedIn: 'root',
})
export class NotificationEmitterService {
  // Using only Subject (modern approach)
  private notificationSubject = new Subject<Notification>();

  public notifications$ = this.notificationSubject.asObservable();

  constructor() {
    // No need to initialize EventEmitter anymore
  }

  public success(title: string, msg: string): void {
    this.add('success', title, msg);
  }

  public error(title: string, msg: string): void {
    this.add('error', title, msg);
  }

  public warn(title: string, msg: string): void {
    this.add('warn', title, msg);
  }

  public info(title: string, msg: string): void {
    this.add('info', title, msg);
  }

  public add(type: 'success' | 'error' | 'warn' | 'info', title: string, msg: string): void {
    const notification: Notification = {
      type,
      title,
      message: msg,
    };

    // Emit using only the modern Subject approach
    this.notificationSubject.next(notification);
  }
}
