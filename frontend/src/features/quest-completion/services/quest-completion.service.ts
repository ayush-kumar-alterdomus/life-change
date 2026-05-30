import { Injectable, inject, signal, computed } from '@angular/core';
import { Observable, Subject, EMPTY } from 'rxjs';
import { filter, tap, catchError } from 'rxjs/operators';
import { Quest } from '@shared/models/quest.model';
import { QuestService, QuestCompletionResponse } from '../../quests/services/quest.service';
import { HapticService } from '../../../core/services/haptic.service';
import { DashboardStore } from '../../dashboard/state/dashboard.store';
import { CompletionFlowState, QuestCompletionResult } from '../models/quest-completion.models';

/**
 * Singleton orchestration service for the quest completion flow.
 * Manages the multi-step reward experience using signal-based state.
 *
 * This skeleton provides:
 * - Signal-based state machine (CompletionFlowState)
 * - Preloaded Lottie animation data
 * - Completion event stream for subscribers
 *
 * The completeQuest() method and lifecycle methods are added in tasks 3.2 and 3.4.
 */
@Injectable({ providedIn: 'root' })
export class QuestCompletionService {
  private readonly questService = inject(QuestService);
  private readonly hapticService = inject(HapticService);
  private readonly dashboardStore = inject(DashboardStore);

  // ─── Signal-Based State ────────────────────────────────────────────────────

  /** Internal mutable flow state signal */
  private readonly _flowState = signal<CompletionFlowState>({
    status: 'idle',
    quest: null,
    error: null,
  });

  /** Public read-only flow state for UI binding */
  readonly flowState = this._flowState.asReadonly();

  /** Whether a completion flow is currently in progress */
  readonly isFlowActive = computed(() => this._flowState().status !== 'idle');

  // ─── Completion Event Stream ───────────────────────────────────────────────

  /** Internal subject for emitting completion results */
  private readonly _completionEvent$ = new Subject<QuestCompletionResult>();

  /** Public observable for subscribers to react to quest completions */
  readonly completionEvent$ = this._completionEvent$.asObservable();

  // ─── Preloaded Animation Data ──────────────────────────────────────────────

  /** Lottie JSON data for the particle burst animation */
  private particleBurstData: unknown = null;

  /** Lottie JSON data for the confetti animation */
  private confettiData: unknown = null;

  // ─── Constructor ───────────────────────────────────────────────────────────

  constructor() {
    this.preloadAnimations();
  }

  // ─── Public API ────────────────────────────────────────────────────────────

  /**
   * Initiate the quest completion flow.
   * Returns an Observable that emits the result for this specific quest,
   * or EMPTY if a flow is already in progress (concurrent guard).
   */
  completeQuest(quest: Quest): Observable<QuestCompletionResult> {
    if (this.isFlowActive()) {
      return EMPTY;
    }

    this._flowState.set({ status: 'confirming', quest, error: null });

    return this._completionEvent$.pipe(filter((result) => result.questId === quest.id));
  }

  // ─── Flow Lifecycle Methods ─────────────────────────────────────────────────

  /**
   * User confirmed quest completion.
   * Triggers light haptic, transitions to submitting, and calls the API.
   */
  onConfirm(): void {
    const quest = this._flowState().quest;
    if (!quest) return;
    this.hapticService.impact('light');
    this._flowState.set({ status: 'submitting', quest, error: null });
    this.questService
      .completeQuest(quest.id)
      .pipe(
        tap((response) => this.handleSuccess(quest, response)),
        catchError((error) => this.handleError(quest, error)),
      )
      .subscribe();
  }

  /**
   * User cancelled the confirmation sheet.
   * Resets state to idle.
   */
  onCancel(): void {
    this._flowState.set({ status: 'idle', quest: null, error: null });
  }

  /**
   * Called when the Perfect Day overlay is dismissed.
   * Resets state to idle.
   */
  onPerfectDayDismiss(): void {
    this._flowState.set({ status: 'idle', quest: null, error: null });
  }

  /**
   * Called when the success animation completes.
   * Checks if all daily quests are done → triggers Perfect Day or resets to idle.
   */
  onAnimationComplete(): void {
    const quest = this._flowState().quest;
    if (!quest) return;
    if (this.isDailyQuestSetComplete()) {
      this.hapticService.impact('heavy');
      this._flowState.set({ status: 'perfect-day', quest, error: null });
    } else {
      this._flowState.set({ status: 'idle', quest: null, error: null });
    }
  }

  /**
   * Returns stats for the Perfect Day overlay.
   */
  getPerfectDayStats(): { totalXp: number; questsCompleted: number } {
    const quest = this._flowState().quest;
    return {
      totalXp: quest?.xpReward ?? 0,
      questsCompleted:
        this.dashboardStore.todayQuests().status === 'loaded'
          ? this.dashboardStore.todayQuests().data.length
          : 0,
    };
  }

  // ─── Private Methods ───────────────────────────────────────────────────────

  /**
   * Handle successful quest completion API response.
   * Triggers success haptic, updates store, transitions to animating, emits event.
   */
  private handleSuccess(quest: Quest, response: QuestCompletionResponse): void {
    this.hapticService.notification('success');
    this.dashboardStore.completeQuest(quest.id, response.xpEarned);
    this._flowState.set({ status: 'animating', quest, error: null });
    const result: QuestCompletionResult = {
      questId: response.questId,
      questTitle: response.questTitle,
      xpEarned: response.xpEarned,
      completedAt: response.completedAt,
      message: response.message,
      isPerfectDay: this.isDailyQuestSetComplete(),
    };
    this._completionEvent$.next(result);
  }

  /**
   * Handle quest completion API error.
   * 409 → silent dismiss (quest already completed); other → show error, keep sheet open.
   */
  private handleError(quest: Quest, error: any): Observable<never> {
    const status = error?.status;
    if (status === 409) {
      this.dashboardStore.completeQuest(quest.id, quest.xpReward);
      const result: QuestCompletionResult = {
        questId: quest.id,
        questTitle: quest.title,
        xpEarned: quest.xpReward,
        completedAt: new Date().toISOString(),
        message: 'Quest already completed',
        isPerfectDay: this.isDailyQuestSetComplete(),
      };
      this._completionEvent$.next(result);
      this._flowState.set({ status: 'idle', quest: null, error: null });
    } else {
      this._flowState.set({
        status: 'confirming',
        quest,
        error: 'Could not complete quest. Try again.',
      });
    }
    return EMPTY;
  }

  /**
   * Check if all daily quests have been completed.
   * Returns true when the todayQuests list is loaded and empty.
   */
  private isDailyQuestSetComplete(): boolean {
    const questsState = this.dashboardStore.todayQuests();
    if (questsState.status !== 'loaded') return false;
    return questsState.data.length === 0;
  }

  /**
   * Preload Lottie animation JSON at service initialization.
   * Uses dynamic import to avoid blocking the main bundle and
   * ensures animations are ready for instant playback when needed.
   */
  private preloadAnimations(): void {
    import('../../../assets/animations/particle-burst.json')
      .then((data) => (this.particleBurstData = data.default ?? data))
      .catch(() => {
        /* Graceful fallback — animation won't play if asset is missing */
      });

    import('../../../assets/animations/confetti.json')
      .then((data) => (this.confettiData = data.default ?? data))
      .catch(() => {
        /* Graceful fallback — confetti won't play if asset is missing */
      });
  }
}
