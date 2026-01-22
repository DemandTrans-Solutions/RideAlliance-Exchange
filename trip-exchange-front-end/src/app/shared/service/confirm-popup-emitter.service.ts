import { Injectable, EventEmitter } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ConfirmPopupEmitterService {
  // Keep EventEmitters for backward compatibility
  public invokePopup: EventEmitter<any>;
  public confirmCallback: EventEmitter<any>;

  // Modern approach using Subjects
  private popupSubject = new Subject<string>();
  private confirmSubject = new Subject<boolean>();

  public popup$ = this.popupSubject.asObservable();
  public confirm$ = this.confirmSubject.asObservable();

  constructor() {
    this.invokePopup = new EventEmitter();
    this.confirmCallback = new EventEmitter();
  }

  public invoke(msg: string): void {
    // Emit for backward compatibility
    this.invokePopup.emit(msg);

    // Emit using modern approach
    this.popupSubject.next(msg);
  }

  public confirmResult(result: boolean): void {
    // Emit for backward compatibility
    this.confirmCallback.emit(result);

    // Emit using modern approach
    this.confirmSubject.next(result);
  }
}
