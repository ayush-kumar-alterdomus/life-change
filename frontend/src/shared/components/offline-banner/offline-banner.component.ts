import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  effect,
  OnDestroy,
  computed,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConnectivityService } from '../../../core/services/connectivity.service';

/**
 * Banner state:
 * - 'hidden': no banner shown
 * - 'offline': amber banner indicating offline mode
 * - 'back-online': green banner confirming reconnection (auto-dismisses after 3s)
 */
type BannerState = 'hidden' | 'offline' | 'back-online';

@Component({
  standalone: true,
  selector: 'app-offline-banner',
  imports: [CommonModule],
  template: `
    <div
      class="offline-banner"
      [class.offline-banner--offline]="bannerState() === 'offline'"
      [class.offline-banner--back-online]="bannerState() === 'back-online'"
      [class.offline-banner--visible]="bannerState() !== 'hidden'"
      role="status"
      aria-live="polite"
      [attr.aria-hidden]="bannerState() === 'hidden'">
      <span class="offline-banner__message">{{ bannerMessage() }}</span>
    </div>
  `,
  styleUrls: ['./offline-banner.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OfflineBannerComponent implements OnDestroy {
  private readonly connectivity = inject(ConnectivityService);

  /** Current banner display state. */
  readonly bannerState = signal<BannerState>(
    this.connectivity.isOnline() ? 'hidden' : 'offline'
  );

  /** Message displayed in the banner based on current state. */
  readonly bannerMessage = computed(() => {
    switch (this.bannerState()) {
      case 'offline':
        return "You're in Offline Mode. Progress will sync later.";
      case 'back-online':
        return 'Back online';
      default:
        return '';
    }
  });

  private autoDismissTimer: ReturnType<typeof setTimeout> | null = null;
  private previousOnline: boolean = this.connectivity.isOnline();

  constructor() {
    effect(() => {
      const isOnline = this.connectivity.isOnline();

      if (!isOnline && this.previousOnline) {
        // Went offline
        this.clearAutoDismissTimer();
        this.bannerState.set('offline');
      } else if (isOnline && !this.previousOnline) {
        // Came back online
        this.bannerState.set('back-online');
        this.startAutoDismiss();
      }

      this.previousOnline = isOnline;
    });
  }

  ngOnDestroy(): void {
    this.clearAutoDismissTimer();
  }

  private startAutoDismiss(): void {
    this.clearAutoDismissTimer();
    this.autoDismissTimer = setTimeout(() => {
      this.bannerState.set('hidden');
    }, 3000);
  }

  private clearAutoDismissTimer(): void {
    if (this.autoDismissTimer !== null) {
      clearTimeout(this.autoDismissTimer);
      this.autoDismissTimer = null;
    }
  }
}
