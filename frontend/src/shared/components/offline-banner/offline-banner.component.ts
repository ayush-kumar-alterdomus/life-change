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
import { SyncService } from '../../../core/services/sync.service';

type BannerState = 'hidden' | 'offline' | 'syncing' | 'sync-error' | 'back-online';

@Component({
  standalone: true,
  selector: 'app-offline-banner',
  imports: [CommonModule],
  template: `
    <div
      class="offline-banner"
      [class.offline-banner--offline]="bannerState() === 'offline'"
      [class.offline-banner--syncing]="bannerState() === 'syncing'"
      [class.offline-banner--sync-error]="bannerState() === 'sync-error'"
      [class.offline-banner--back-online]="bannerState() === 'back-online'"
      [class.offline-banner--visible]="bannerState() !== 'hidden'"
      role="status"
      aria-live="polite"
      [attr.aria-hidden]="bannerState() === 'hidden'"
    >
      <span class="offline-banner__message">{{ bannerMessage() }}</span>
      @if (bannerState() === 'sync-error') {
        <button class="offline-banner__retry" (click)="retrySyncFn()">Retry</button>
      }
    </div>
  `,
  styleUrls: ['./offline-banner.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OfflineBannerComponent implements OnDestroy {
  private readonly connectivity = inject(ConnectivityService);
  private readonly syncService = inject(SyncService);

  readonly bannerState = signal<BannerState>(this.connectivity.isOnline() ? 'hidden' : 'offline');

  readonly bannerMessage = computed(() => {
    switch (this.bannerState()) {
      case 'offline':
        return "You're in Offline Mode. Progress will sync later.";
      case 'syncing':
        return `Syncing ${this.syncService.syncingCount()} actions...`;
      case 'sync-error':
        return "Some actions couldn't be synced.";
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
      const isSyncing = this.syncService.isSyncing();
      const rejected = this.syncService.rejectedActions();

      if (!isOnline) {
        this.clearAutoDismissTimer();
        this.bannerState.set('offline');
      } else if (isSyncing) {
        this.clearAutoDismissTimer();
        this.bannerState.set('syncing');
      } else if (rejected.length > 0) {
        this.clearAutoDismissTimer();
        this.bannerState.set('sync-error');
      } else if (isOnline && !this.previousOnline) {
        this.bannerState.set('back-online');
        this.startAutoDismiss();
      }

      this.previousOnline = isOnline;
    });
  }

  ngOnDestroy(): void {
    this.clearAutoDismissTimer();
  }

  retrySyncFn(): void {
    this.syncService.syncPendingActions();
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
