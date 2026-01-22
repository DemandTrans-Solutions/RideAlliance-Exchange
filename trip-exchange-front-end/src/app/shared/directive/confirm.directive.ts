import { Directive, ElementRef, EventEmitter, HostListener, Input, Output } from '@angular/core';
import { ConfirmationService } from 'primeng/api';
import { ConfirmPopupEmitterService } from '../service/confirm-popup-emitter.service';

/**
 * A directive that shows a confirmation dialog when an element is clicked outside
 */
@Directive({
  selector: '[clickOutside]',
})
export class ConfirmClickDirective {
  @Input() confirmMessage = 'Are you sure that you want to perform this action?';
  @Input() confirmTitle = 'Confirmation';

  @Output() clickOutside = new EventEmitter<MouseEvent>();

  constructor(
    private _elementRef: ElementRef,
    private _confirmationService: ConfirmationService,
    private _confirmPopupEmitterService: ConfirmPopupEmitterService
  ) {}

  @HostListener('document:click', ['$event', '$event.target'])
  public onClick(event: MouseEvent, targetElement: HTMLElement): void {
    if (!targetElement) {
      return;
    }

    // Check if the clicked element is not part of this element
    const clickedInside = this._elementRef.nativeElement.contains(targetElement);
    if (!clickedInside) {
      this.showConfirmation(event);
    }
  }

  private showConfirmation(event: MouseEvent): void {
    this._confirmationService.confirm({
      message: this.confirmMessage,
      header: this.confirmTitle,
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.clickOutside.emit(event);
        this._confirmPopupEmitterService.confirmResult(true);
      },
      reject: () => {
        this._confirmPopupEmitterService.confirmResult(false);
      },
    });
  }
}
