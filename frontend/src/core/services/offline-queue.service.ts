import { Injectable, inject } from '@angular/core';
import { StorageService } from './storage.service';

export interface QueuedAction {
  id: string;
  type: string;
  payload: unknown;
  timestamp: number;
  synced: boolean;
}

const QUEUE_KEY = 'offline_queue';

@Injectable({ providedIn: 'root' })
export class OfflineQueueService {
  private readonly storage = inject(StorageService);

  async queueAction(type: string, payload: unknown): Promise<QueuedAction> {
    const action: QueuedAction = {
      id: crypto.randomUUID(),
      type,
      payload,
      timestamp: Date.now(),
      synced: false,
    };

    const queue = await this.getQueue();
    queue.push(action);
    await this.storage.set(QUEUE_KEY, queue);
    return action;
  }

  async getPendingActions(): Promise<QueuedAction[]> {
    const queue = await this.getQueue();
    return queue.filter((a) => !a.synced);
  }

  async markSynced(actionId: string): Promise<void> {
    const queue = await this.getQueue();
    const action = queue.find((a) => a.id === actionId);
    if (action) {
      action.synced = true;
      await this.storage.set(QUEUE_KEY, queue);
    }
  }

  async removeAction(actionId: string): Promise<void> {
    const queue = await this.getQueue();
    const filtered = queue.filter((a) => a.id !== actionId);
    await this.storage.set(QUEUE_KEY, filtered);
  }

  async clearSynced(): Promise<void> {
    const queue = await this.getQueue();
    const pending = queue.filter((a) => !a.synced);
    await this.storage.set(QUEUE_KEY, pending);
  }

  private async getQueue(): Promise<QueuedAction[]> {
    return (await this.storage.get<QueuedAction[]>(QUEUE_KEY)) ?? [];
  }
}
