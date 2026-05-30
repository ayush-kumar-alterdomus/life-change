import { Directive, effect, ElementRef, inject, input, OnDestroy, Renderer2 } from '@angular/core';

@Directive({
  standalone: true,
  selector: '[appGlowCard]',
})
export class GlowCardDirective implements OnDestroy {
  private readonly el = inject(ElementRef);
  private readonly renderer = inject(Renderer2);

  /** Whether the glow effect is active. */
  appGlowCard = input<boolean>(false);

  /** Glow color (default: primary accent). */
  glowColor = input<string>('#FF9800');

  /** Glow spread radius. */
  glowRadius = input<string>('12px');

  private pressed = false;

  private readonly onTouchStart = (): void => {
    this.applyPress();
  };

  private readonly onTouchEnd = (): void => {
    this.releasePress();
  };

  private readonly onMouseDown = (): void => {
    this.applyPress();
  };

  private readonly onMouseUp = (): void => {
    this.releasePress();
  };

  private readonly onMouseLeave = (): void => {
    if (this.pressed) {
      this.releasePress();
    }
  };

  constructor() {
    const nativeEl = this.el.nativeElement as HTMLElement;

    // Apply base transition styles
    this.renderer.setStyle(
      nativeEl,
      'transition',
      'box-shadow 200ms ease-in, border-color 200ms ease-in, transform 100ms ease',
    );

    // Register press event listeners
    nativeEl.addEventListener('touchstart', this.onTouchStart, { passive: true });
    nativeEl.addEventListener('touchend', this.onTouchEnd);
    nativeEl.addEventListener('mousedown', this.onMouseDown);
    nativeEl.addEventListener('mouseup', this.onMouseUp);
    nativeEl.addEventListener('mouseleave', this.onMouseLeave);

    // React to input changes via effect
    effect(() => {
      const active = this.appGlowCard();
      const color = this.glowColor();
      const radius = this.glowRadius();

      if (active) {
        this.renderer.setStyle(nativeEl, 'box-shadow', `0 0 ${radius} ${color}99`);
        this.renderer.setStyle(nativeEl, 'border-color', color);
      } else {
        this.renderer.removeStyle(nativeEl, 'box-shadow');
        this.renderer.removeStyle(nativeEl, 'border-color');
      }
    });
  }

  ngOnDestroy(): void {
    const nativeEl = this.el.nativeElement as HTMLElement;
    nativeEl.removeEventListener('touchstart', this.onTouchStart);
    nativeEl.removeEventListener('touchend', this.onTouchEnd);
    nativeEl.removeEventListener('mousedown', this.onMouseDown);
    nativeEl.removeEventListener('mouseup', this.onMouseUp);
    nativeEl.removeEventListener('mouseleave', this.onMouseLeave);
  }

  private applyPress(): void {
    this.pressed = true;
    this.renderer.setStyle(this.el.nativeElement, 'transform', 'scale(0.97)');
  }

  private releasePress(): void {
    this.pressed = false;
    this.renderer.removeStyle(this.el.nativeElement, 'transform');
  }
}
