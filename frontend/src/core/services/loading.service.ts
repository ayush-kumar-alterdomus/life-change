import { Injectable, signal, Signal, WritableSignal } from '@angular/core';

const DEBOUNCE_MS = 300;

/**
 * Global loading state service with 300ms debounce logic.
 * The loading indicator only shows after 300ms of continuous loading,
 * but hides immediately when the counter returns to zero.
 */
@Injectable({ providedIn: 'root' })
export class LoadingService {
  private readonly _isLoading: WritableSignal<boolean> = signal(false);
  private counter = 0;
  private debounceTimer: ReturnType<typeof setTimeout> | null = null;

  /** Reactive signal indicating whether the loading indicator should be visible. */
  readonly isLoading: Signal<boolean> = this._isLoading.asReadonly();

  /**
   * Increment the loading counter. If this is the first active request,
   * start the 300ms debounce timer before showing the indicator.
   */
  increment(): void {
    this.counter++;

    if (this.counter === 1) {
      // First request — start debounce timer
      this.debounceTimer = setTimeout(() => {
        if (this.counter > 0) {
          this._isLoading.set(true);
        }
        this.debounceTimer = null;
      }, DEBOUNCE_MS);
    }
  }

  /**
   * Decrement the loading counter (never below zero).
   * If the counter reaches zero, hide the indicator immediately
   * and cancel any pending debounce timer.
   */
  decrement(): void {
    if (this.counter > 0) {
      this.counter--;
    }

    if (this.counter === 0) {
      // Cancel pending debounce if request completed quickly
      if (this.debounceTimer !== null) {
        clearTimeout(this.debounceTimer);
        this.debounceTimer = null;
      }
      this._isLoading.set(false);
    }
  }
}
