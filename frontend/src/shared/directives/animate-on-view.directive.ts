import {
  Directive,
  ElementRef,
  OnInit,
  OnDestroy,
  input,
  inject,
  PLATFORM_ID,
} from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Directive({
  standalone: true,
  selector: '[appAnimateOnView]',
})
export class AnimateOnViewDirective implements OnInit, OnDestroy {
  animationType = input<'fade-in' | 'slide-up' | 'slide-left' | 'scale-in'>('fade-in');
  delay = input<number>(0);
  once = input<boolean>(true);

  private readonly el = inject(ElementRef);
  private readonly platformId = inject(PLATFORM_ID);
  private observer: IntersectionObserver | null = null;
  private timeoutId: ReturnType<typeof setTimeout> | null = null;

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    // If IntersectionObserver is not supported, skip animation — element remains visible
    if (typeof IntersectionObserver === 'undefined') {
      return;
    }

    // Hide element initially before animation triggers
    this.el.nativeElement.classList.add('animate-on-view--hidden');

    this.observer = new IntersectionObserver((entries) => this.onIntersection(entries), {
      threshold: 0.1,
    });

    this.observer.observe(this.el.nativeElement);
  }

  ngOnDestroy(): void {
    this.disconnectObserver();
    this.clearTimeout();
  }

  private onIntersection(entries: IntersectionObserverEntry[]): void {
    for (const entry of entries) {
      if (entry.isIntersecting) {
        this.triggerAnimation();

        if (this.once()) {
          this.disconnectObserver();
        }
      } else {
        // If not "once" mode, reset animation when element leaves viewport
        if (!this.once()) {
          this.resetAnimation();
        }
      }
    }
  }

  private triggerAnimation(): void {
    const delayMs = this.delay();

    if (delayMs > 0) {
      this.timeoutId = setTimeout(() => {
        this.applyAnimationClass();
      }, delayMs);
    } else {
      this.applyAnimationClass();
    }
  }

  private applyAnimationClass(): void {
    const element = this.el.nativeElement;
    element.classList.remove('animate-on-view--hidden');
    element.classList.add(`animate-on-view--${this.animationType()}`);
  }

  private resetAnimation(): void {
    this.clearTimeout();
    const element = this.el.nativeElement;
    element.classList.remove(`animate-on-view--${this.animationType()}`);
    element.classList.add('animate-on-view--hidden');
  }

  private disconnectObserver(): void {
    if (this.observer) {
      this.observer.disconnect();
      this.observer = null;
    }
  }

  private clearTimeout(): void {
    if (this.timeoutId !== null) {
      clearTimeout(this.timeoutId);
      this.timeoutId = null;
    }
  }
}
