import { Injectable, inject, effect, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ApiService } from './api.service';
import { ConnectivityService } from './connectivity.service';
import { OfflineQueueService } from './offline-queue.service';

export interface SyncActionResult {
  actionId: string;
  status: 'ACCEPTED' | 'REJECTED';
  reason?: string;
}

export interface SyncBatchResponse {
  results: SyncActionResult[];
}

@Injectable({ providedIn: 'root' })
export class SyncService {
  private readonly api = inject(ApiService);
  private readonly connectivity = inject(ConnectivityService);
  private readonly offlineQueue = inject(OfflineQueueService);

  /** Number of actions currently being synced. */
  readonly syncingCount = signal(0);

  /** Whether a sync is in progress. */
  readonly isSyncing = signal(false);

  /** Actions that were rejected on last sync. */
  readonly rejectedActions = signal<SyncActionResult[]>([]);

  private previousOnline = this.connectivity.isOnline();

  constructor() {
    effect(() => {
      const isOnline = this.connectivity.isOnline();
      if (isOnline && !this.previousOnline) {
        this.syncPendingActions();
      }
      this.previousOnline = isOnline;
    });
  }

  async syncPendingActions(): Promise<void> {
    if (this.isSyncing() || !this.connectivity.isOnline()) return;

    const pending = await this.offlineQueue.getPendingActions();
    if (pending.length === 0) return;

    this.isSyncing.set(true);
    this.syncingCount.set(pending.length);
    this.rejectedActions.set([]);

    try {
      const response = await firstValueFrom(
        this.api.post<SyncBatchResponse>('/sync/batch', { actions: pending }),
      );

      const rejected: SyncActionResult[] = [];

      for (const result of response.results) {
        if (result.status === 'ACCEPTED') {
          await this.offlineQueue.markSynced(result.actionId);
          await this.offlineQueue.removeAction(result.actionId);
        } else {
          rejected.push(result);
          await this.offlineQueue.removeAction(result.actionId);
        }
      }

      this.rejectedActions.set(rejected);
      await this.offlineQueue.clearSynced();
    } catch (error) {
      console.warn('[SyncService] Batch sync failed, will retry later:', error);
    } finally {
      this.isSyncing.set(false);
      this.syncingCount.set(0);
    }
  }
}
