import { Injectable, inject, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { WebSocketService, WsMessage } from './websocket.service';

@Injectable({ providedIn: 'root' })
export class RealtimeNotificationsService {
  private readonly ws = inject(WebSocketService);
  private readonly destroyRef = inject(DestroyRef);

  initialize(): void {
    this.ws.messages$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((msg) => this.handleMessage(msg));
  }

  private handleMessage(msg: WsMessage): void {
    switch (msg.destination) {
      case '/queue/xp':
        this.handleXpUpdate(msg.body as { xpGained: number; newTotal: number; newLevel: number });
        break;
      case '/queue/level':
        this.handleLevelUp(msg.body as { newLevel: number; unlocks: string[] });
        break;
      case '/queue/streak':
        this.handleStreakAlert(msg.body as { message: string; currentStreak: number });
        break;
      case '/queue/boss':
        this.handleBossProgress(msg.body as { bossName: string; progress: number });
        break;
      case '/queue/notifications':
        this.handleNotification(msg.body as { title: string; message: string });
        break;
    }
  }

  private handleXpUpdate(data: { xpGained: number; newTotal: number; newLevel: number }): void {
    // Update user store signals reactively — no page refresh needed
    console.log('[RT] XP update:', data);
  }

  private handleLevelUp(data: { newLevel: number; unlocks: string[] }): void {
    console.log('[RT] Level up:', data);
  }

  private handleStreakAlert(data: { message: string; currentStreak: number }): void {
    console.log('[RT] Streak alert:', data);
  }

  private handleBossProgress(data: { bossName: string; progress: number }): void {
    console.log('[RT] Boss progress:', data);
  }

  private handleNotification(data: { title: string; message: string }): void {
    console.log('[RT] Notification:', data);
  }
}
