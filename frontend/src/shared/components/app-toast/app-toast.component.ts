import {
  Component,
  ChangeDetectionStrategy,
  input,
  output,
  computed,
  OnInit,
  OnDestroy,
  signal,
  inject,
  DestroyRef,
  HostBinding,
} from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  selector: 'app-toast',
  imports: [CommonModule],
  templateUrl: './app-toast.component.html',
  styleUrls: ['./app-toast.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppToastComponent implements OnInit, OnDestroy {
  private readonly destroyRef = inject(DestroyRef);

  @HostBinding('attr.role') readonly hostRole = 'alert';
  @HostBinding('attr.aria-live') readonly hostAriaLive = 'polite';
  @HostBinding('class.app-toast--success') get hostSuccess() { return this.type() === 'success'; }
  @HostBinding('class.app-toast--error') get hostError() { return this.type() === 'error'; }
  @HostBinding('class.app-toast--warning') get hostWarning() { return this.type() === 'warning'; }
  @HostBinding('class.app-toast--info') get hostInfo() { return this.type() === 'info'; }
  @HostBinding('class.app-toast--dismissing') get hostDismissing() { return this.isDismissing(); }
  @HostBinding('style.--toast-offset') get hostOffset() { return this.offsetPx(); }

  /** Toast type determines icon and color accent. */
  type = input<'success' | 'error' | 'warning' | 'info'>('info');

  /** The message to display in the toast. */
  message = input.required<string>();

  /** Auto-dismiss duration in milliseconds. */
  duration = input<number>(3000);

  /** Optional action button label. Null hides the button. */
  actionLabel = input<string | null>(null);

  /** Vertical offset in pixels for stacking multiple toasts. */
  offset = input<number>(0);

  /** Emitted when the toast is dismissed (auto or manual). */
  dismissed = output<void>();

  /** Emitted when the action button is clicked. */
  actionClicked = output<void>();

  /** Internal state tracking the dismiss animation. */
  isDismissing = signal(false);

  /** Computed pixel offset for stacking. */
  offsetPx = computed(() => `${this.offset()}px`);

  /** Icon character/symbol per toast type. */
  icon = computed(() => {
    switch (this.type()) {
      case 'success':
        return '✓';
      case 'error':
        return '✕';
      case 'warning':
        return '⚠';
      case 'info':
      default:
        return 'ℹ';
    }
  });

  private autoDismissTimer: ReturnType<typeof setTimeout> | null = null;

  ngOnInit(): void {
    this.startAutoDismiss();
  }

  ngOnDestroy(): void {
    this.clearAutoDismissTimer();
  }

  /** Start the auto-dismiss countdown based on duration input. */
  private startAutoDismiss(): void {
    const ms = this.duration() > 0 ? this.duration() : 3000;
    this.autoDismissTimer = setTimeout(() => {
      this.dismissToast();
    }, ms);
  }

  /** Clear any pending auto-dismiss timer. */
  private clearAutoDismissTimer(): void {
    if (this.autoDismissTimer !== null) {
      clearTimeout(this.autoDismissTimer);
      this.autoDismissTimer = null;
    }
  }

  /** Trigger the dismiss animation and emit the dismissed event. */
  dismissToast(): void {
    this.isDismissing.set(true);
    // Allow the slide-out animation to complete before emitting
    setTimeout(() => {
      this.dismissed.emit();
    }, 300);
  }

  /** Handle action button click. */
  onActionClick(): void {
    this.clearAutoDismissTimer();
    this.actionClicked.emit();
    this.dismissToast();
  }

  /** Handle manual close button click. */
  onCloseClick(): void {
    this.clearAutoDismissTimer();
    this.dismissToast();
  }
}
