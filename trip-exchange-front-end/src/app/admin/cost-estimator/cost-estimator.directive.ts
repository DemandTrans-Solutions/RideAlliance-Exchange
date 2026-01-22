import { Directive, ElementRef, HostListener } from '@angular/core';

/**
 * Directive to restrict input to numeric values with up to 2 decimal places
 * Used for cost input fields
 */
@Directive({
  selector: '[appCostEstimator]',
  standalone: true,
})
export class CostEstimatorDirective {
  // Pattern for numbers with up to 2 decimal places
  private readonly regex: RegExp = /^\d*\.?\d{0,2}$/;

  // Keys that should be allowed regardless of pattern matching
  private readonly specialKeys: Array<string> = [
    'Backspace',
    'Tab',
    'End',
    'Home',
    'ArrowLeft',
    'ArrowRight',
    'Delete',
  ];

  constructor(private el: ElementRef<HTMLInputElement>) {}

  /**
   * Handle keydown events to enforce decimal number format
   */
  @HostListener('keydown', ['$event'])
  onKeyDown(event: KeyboardEvent): void {
    // Allow special keys (backspace, delete, arrows, etc)
    if (this.specialKeys.includes(event.key)) {
      return;
    }

    // Get current input value and cursor position
    const current: string = this.el.nativeElement.value;
    const position: number = this.el.nativeElement.selectionStart || 0;

    // Build the next value if this key is added
    const next: string = [
      current.slice(0, position),
      event.key === 'Decimal' ? '.' : event.key,
      current.slice(position),
    ].join('');

    // Prevent the keypress if the resulting value doesn't match the pattern
    if (next && !this.regex.test(next)) {
      event.preventDefault();
    }
  }
}
