import {
  Directive,
  ElementRef,
  OnDestroy,
  inject,
  input,
  output,
} from '@angular/core';

@Directive({
  standalone: true,
  selector: '[appLongPress]',
})
export class LongPressDirective implements OnDestroy {
  private readonly el = inject(ElementRef);

  /** Duration in ms before the long-press event fires (default 500ms). */
  duration = input<number>(500, { alias: 'appLongPressDuration' });

  /** Emitted when a long press is detected. */
  longPress = output<void>({ alias: 'appLongPress' });

  private timer: ReturnType<typeof setTimeout> | null = null;
  private startX = 0;
  private startY = 0;
  private readonly MOVE_THRESHOLD = 10;

  private readonly onTouchStart = (event: TouchEvent): void => {
    const touch = event.touches[0];
    this.startX = touch.clientX;
    this.startY = touch.clientY;
    this.startTimer();
  };

  private readonly onMouseDown = (event: MouseEvent): void => {
    this.startX = event.clientX;
    this.startY = event.clientY;
    this.startTimer();
  };

  private readonly onTouchMove = (event: TouchEvent): void => {
    if (this.timer === null) {
      return;
    }
    const touch = event.touches[0];
    const deltaX = Math.abs(touch.clientX - this.startX);
    const deltaY = Math.abs(touch.clientY - this.startY);
    if (deltaX > this.MOVE_THRESHOLD || deltaY > this.MOVE_THRESHOLD) {
      this.cancelTimer();
    }
  };

  private readonly onTouchEnd = (): void => {
    this.cancelTimer();
  };

  private readonly onMouseUp = (): void => {
    this.cancelTimer();
  };

  constructor() {
    const element = this.el.nativeElement as HTMLElement;

    element.addEventListener('touchstart', this.onTouchStart, { passive: true });
    element.addEventListener('touchmove', this.onTouchMove, { passive: true });
    element.addEventListener('touchend', this.onTouchEnd);
    element.addEventListener('mousedown', this.onMouseDown);
    element.addEventListener('mouseup', this.onMouseUp);
  }

  ngOnDestroy(): void {
    this.cancelTimer();

    const element = this.el.nativeElement as HTMLElement;
    element.removeEventListener('touchstart', this.onTouchStart);
    element.removeEventListener('touchmove', this.onTouchMove);
    element.removeEventListener('touchend', this.onTouchEnd);
    element.removeEventListener('mousedown', this.onMouseDown);
    element.removeEventListener('mouseup', this.onMouseUp);
  }

  private startTimer(): void {
    this.cancelTimer();
    const dur = this.duration() > 0 ? this.duration() : 500;
    this.timer = setTimeout(() => {
      this.timer = null;
      this.longPress.emit();
    }, dur);
  }

  private cancelTimer(): void {
    if (this.timer !== null) {
      clearTimeout(this.timer);
      this.timer = null;
    }
  }
}
