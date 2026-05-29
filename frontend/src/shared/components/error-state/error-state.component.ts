import {
  Component,
  ChangeDetectionStrategy,
  input,
  output,
  computed,
  signal,
  effect,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonIcon, IonButton } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { alertCircleOutline } from 'ionicons/icons';

import { SkeletonLoaderComponent } from '../skeleton-loader/skeleton-loader.component';

/**
 * ErrorStateComponent
 *
 * A reusable standalone error state component that displays an error message
 * with an icon and a "Try Again" button. Supports retry functionality with
 * a skeleton loader shown during retry attempts.
 *
 * Usage:
 * ```html
 * <app-error-state
 *   [message]="'Failed to load quests.'"
 *   [icon]="'cloud-offline-outline'"
 *   [retrying]="isRetrying"
 *   (retry)="onRetry()"
 * />
 * ```
 */
@Component({
  standalone: true,
  selector: 'app-error-state',
  templateUrl: './error-state.component.html',
  styleUrls: ['./error-state.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, IonIcon, IonButton, SkeletonLoaderComponent],
})
export class ErrorStateComponent {
  /** Error message to display. Truncated to 150 characters if longer. */
  message = input<string>('Something went wrong. Tap to try again.');

  /** Ionicon name to display above the message. */
  icon = input<string>('alert-circle-outline');

  /**
   * Whether a retry is currently in progress.
   * When true, the skeleton loader is shown instead of the error state.
   * Controlled by the parent component.
   */
  retrying = input<boolean>(false);

  /** Emitted when the user taps the "Try Again" button. */
  retry = output<void>();

  /** Truncated message — max 150 characters displayed */
  displayMessage = computed(() => {
    const msg = this.message();
    if (msg.length > 150) {
      return msg.slice(0, 150) + '…';
    }
    return msg;
  });

  /** Internal disabled state for the button after tap */
  buttonDisabled = signal(false);

  constructor() {
    addIcons({ alertCircleOutline });

    // Reset button disabled state when retrying transitions back to false
    // (i.e., retry completed — either success or failure)
    effect(() => {
      if (!this.retrying()) {
        this.buttonDisabled.set(false);
      }
    });
  }

  /** Handle retry button tap */
  onRetry(): void {
    this.buttonDisabled.set(true);
    this.retry.emit();
  }
}
