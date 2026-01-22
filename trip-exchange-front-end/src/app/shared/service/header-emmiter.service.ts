import { Injectable, EventEmitter } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class HeaderEmitterService {
  // Keep EventEmitter for backward compatibility
  public header: EventEmitter<any>;

  // Modern approach using BehaviorSubject
  private userNameSubject = new BehaviorSubject<boolean>(false);
  public userName$ = this.userNameSubject.asObservable();

  // Property for backward compatibility
  public get userName(): boolean {
    return this.userNameSubject.value;
  }

  public set userName(value: boolean) {
    this.userNameSubject.next(value);
  }

  constructor() {
    this.header = new EventEmitter();
  }

  // Method to update header state
  updateHeader(data: any): void {
    this.header.emit(data);
  }

  // Method to update username state
  updateUserName(isVisible: boolean): void {
    this.userNameSubject.next(isVisible);
  }
}
