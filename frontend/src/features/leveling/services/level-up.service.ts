import { Injectable, computed, signal, OnDestroy, inject } from '@angular/core';
import { Subscription } from 'rxjs';
import {
  CelebrationFlowState,
  CelebrationItem,
  CelebrationStep,
  LevelUpEvent,
  PrestigeData,
} from '../models/level-up.models';
import { decomposeLevelJump } from '../utils/celebration-queue';
import { WebSocketService } from '../../../core/services/websocket.service';

const STORAGE_KEY = 'ascend_last_level';

@Injectable({ providedIn: 'root' })
export class LevelUpService implements OnDestroy {
  private readonly ws = inject(WebSocketService);

  private readonly _flowState = signal<CelebrationFlowState>({
    step: 'idle',
    currentItem: null,
    queue: [],
    error: null,
  });

  readonly flowState = this._flowState.asReadonly();
  readonly celebrationActive = computed(() => this._flowState().step !== 'idle');
  readonly currentStep = computed(() => this._flowState().step);
  readonly currentItem = computed(() => this._flowState().currentItem);
  readonly queueIndicator = computed(() => {
    const item = this._flowState().currentItem;
    if (!item || item.queueTotal <= 1) return null;
    return `${item.queuePosition} of ${item.queueTotal}`;
  });

  readonly prestigeData = signal<PrestigeData | null>(null);
  glowExplosionData: unknown = null;

  private lastProcessedLevel: number;
  private wsSubscription: Subscription | null = null;

  constructor() {
    this.lastProcessedLevel = this.loadLastProcessedLevel();
    this.preloadGlowAnimation();
  }

  triggerLevelUp(event: LevelUpEvent): void {
    if (!this.shouldProcessEvent(event)) return;

    const items = decomposeLevelJump(event);
    if (items.length === 0) return;

    this.persistLastProcessedLevel(event.newLevel);

    const state = this._flowState();
    if (state.step !== 'idle') {
      this._flowState.set({ ...state, queue: [...state.queue, ...items] });
      return;
    }

    this.startCelebration(items[0], items.slice(1));
  }

  advanceStep(): void {
    const state = this._flowState();
    const next = this.getNextStep(state.step, state.currentItem);
    this._flowState.set({ ...state, step: next });
  }

  dismiss(): void {
    const state = this._flowState();
    this._flowState.set({ ...state, step: 'dismissing' });

    setTimeout(() => {
      const current = this._flowState();
      if (current.queue.length > 0) {
        const [next, ...rest] = current.queue;
        setTimeout(() => this.startCelebration(next, rest), 500);
      } else {
        this._flowState.set({ step: 'idle', currentItem: null, queue: [], error: null });
      }
    }, 300);
  }

  activatePrestige(): void {
    this.prestigeData.set({
      previousPrestigeLevel: 0,
      newPrestigeLevel: 1,
      badgeId: 'prestige-1',
      badgeName: 'Prestige I',
    });
    this._flowState.set({ ...this._flowState(), step: 'prestige-screen' });
  }

  connectWebSocket(userId: string): void {
    this.disconnectWebSocket();
    this.wsSubscription = this.ws.messages$.subscribe((msg) => {
      if (msg.destination === '/queue/level') {
        const data = msg.body as { newLevel?: number; previousLevel?: number };
        if (data.newLevel) {
          this.triggerLevelUp({
            userId,
            previousLevel: data.previousLevel ?? this.lastProcessedLevel,
            newLevel: data.newLevel,
            rewards: [],
            unlockedFeatures: [],
          });
        }
      }
    });
  }

  disconnectWebSocket(): void {
    this.wsSubscription?.unsubscribe();
    this.wsSubscription = null;
  }

  ngOnDestroy(): void {
    this.disconnectWebSocket();
  }

  shouldProcessEvent(event: LevelUpEvent): boolean {
    return event.newLevel > this.lastProcessedLevel && event.newLevel > event.previousLevel;
  }

  private startCelebration(item: CelebrationItem, remaining: CelebrationItem[]): void {
    this._flowState.set({
      step: 'glow-explosion',
      currentItem: item,
      queue: remaining,
      error: null,
    });
  }

  private getNextStep(current: CelebrationStep, item: CelebrationItem | null): CelebrationStep {
    switch (current) {
      case 'glow-explosion':
        return 'level-title';
      case 'level-title':
        return 'xp-fly-up';
      case 'xp-fly-up':
        return 'rewards-card';
      case 'rewards-card':
        return item?.isMilestone ? 'feature-unlock' : 'continue-ready';
      case 'feature-unlock':
        return item?.level === 100 ? 'prestige-prompt' : 'continue-ready';
      case 'prestige-prompt':
        return 'continue-ready';
      default:
        return 'continue-ready';
    }
  }

  private persistLastProcessedLevel(level: number): void {
    this.lastProcessedLevel = level;
    try {
      localStorage.setItem(STORAGE_KEY, String(level));
    } catch {
      // localStorage unavailable — in-memory only
    }
  }

  private loadLastProcessedLevel(): number {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      return stored ? parseInt(stored, 10) : 0;
    } catch {
      return 0;
    }
  }

  private async preloadGlowAnimation(): Promise<void> {
    try {
      const data = await import('../../../assets/animations/confetti.json');
      this.glowExplosionData = data.default ?? data;
    } catch {
      this.glowExplosionData = null;
    }
  }
}
