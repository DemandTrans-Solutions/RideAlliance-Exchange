import { Directive, ElementRef, Input, OnInit, Renderer2 } from '@angular/core';

@Directive({
  selector: '[dataLabel]',
})
export class DataLabelDirective implements OnInit {
  @Input() dataLabel: string;

  constructor(private el: ElementRef, private renderer: Renderer2) {}

  ngOnInit() {
    if (this.dataLabel) {
      this.renderer.setAttribute(this.el.nativeElement, 'data-label', this.dataLabel);
    }
  }
}
