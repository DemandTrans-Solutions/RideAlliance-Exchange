import { Directive, ElementRef, Input, OnChanges, SimpleChanges } from '@angular/core';

/**
 * Legacy directive to maintain backwards compatibility
 */
@Directive({
  selector: '[appAccessibility]',
})
export class AccessibilityDirective {
  constructor() {}
}

/**
 * Directive to handle aria-label attribute
 */
@Directive({
  selector: '[aria-label]',
})
export class AriaLabelDirective implements OnChanges {
  @Input('aria-label') ariaLabelValue: string;

  constructor(private el: ElementRef) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes['ariaLabelValue'] && this.ariaLabelValue) {
      this.el.nativeElement.setAttribute('aria-label', this.ariaLabelValue);
    }
  }
}

/**
 * Directive to handle data-label attribute
 */
@Directive({
  selector: '[dataLabel]',
})
export class DataLabelDirective implements OnChanges {
  @Input('dataLabel') dataLabelValue: string;

  constructor(private el: ElementRef) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes['dataLabelValue'] && this.dataLabelValue) {
      this.el.nativeElement.setAttribute('data-label', this.dataLabelValue);
    }
  }
}
