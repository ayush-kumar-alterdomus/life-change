import { Quest } from '@shared/models/quest.model';

/** Result emitted after a successful quest completion flow */
export interface QuestCompletionResult {
  questId: string;
  questTitle: string;
  xpEarned: number;
  completedAt: string;
  message: string;
  isPerfectDay: boolean;
}

/** Internal state tracked by the orchestration service */
export interface CompletionFlowState {
  status: 'idle' | 'confirming' | 'submitting' | 'animating' | 'perfect-day' | 'complete';
  quest: Quest | null;
  error: string | null;
}

/** Configuration for the XP fill animation */
export interface XpFillAnimationConfig {
  previousXp: number;
  newXp: number;
  levelThreshold: number;
  crossesLevel: boolean;
  overflowXp: number;
}

/** Stats displayed in the Perfect Day overlay */
export interface PerfectDayStats {
  totalQuestsCompleted: number;
  totalXpEarnedToday: number;
  currentStreak: number;
}
