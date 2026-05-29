import { Directive, ElementRef, OnInit, OnDestroy, input, output } from '@angular/core';

@Directive({
  standalone: true,
  selector: '[appSwipe]'
})
export class SwipeDirective implements OnInit, OnDestroy {
  disabled = input<boolean>(false);
  minDistance = input<number>(50);

  swipeLeft = output<void>();
  swipeRight = output<void>();

  private startX = 0;
  private startTime = 0;
  private readonly MIN_VELOCITY = 0.3; // px/ms

  private touchStartHandler = this.onTouchStart.bind(this);
  private touchEndHandler = this.onTouchEnd.bind(this);

  constructor(private el: ElementRef<HTMLElement>) {}

  ngOnInit(): void {
    const element = this.el.nativeElement;
    element.addEventListener('touchstart', this.touchStartHandler, { passive: true });
    element.addEventListener('touchend', this.touchEndHandler, { passive: true });
  }

  ngOnDestroy(): void {
    const element = this.el.nativeElement;
    element.removeEventListener('touchstart', this.touchStartHandler);
    element.removeEventListener('touchend', this.touchEndHandler);
  }

  private onTouchStart(event: TouchEvent): void {
    if (this.disabled()) {
      return;
    }

    const touch = event.touches[0];
    this.startX = touch.clientX;
    this.startTime = Date.now();
  }

  private onTouchEnd(event: TouchEvent): void {
    if (this.disabled()) {
      return;
    }

    const touch = event.changedTouches[0];
    const deltaX = touch.clientX - this.startX;
    const elapsed = Date.now() - this.startTime;

    const distance = Math.abs(deltaX);
    const velocity = elapsed > 0 ? distance / elapsed : 0;

    if (distance < this.minDistance() || velocity < this.MIN_VELOCITY) {
      return;
    }

    if (deltaX < 0) {
      this.swipeLeft.emit();
    } else {
      this.swipeRight.emit();
    }
  }
}
