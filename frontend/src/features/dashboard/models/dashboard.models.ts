import { ArcType } from '../../../shared/enums';

/** Section load state — each dashboard section tracks its own status independently */
export type SectionState<T> =
  | { status: 'idle' }
  | { status: 'loading' }
  | { status: 'loaded'; data: T }
  | { status: 'error'; message: string };

/** Aggregated XP progress data for the dashboard card */
export interface DashboardXpProgress {
  currentLevel: number;
  currentXp: number;
  requiredXp: number;
}

/** Daily summary statistics */
export interface DashboardDailyStats {
  questsCompleted: number;
  questsTotal: number;
  currentStreak: number;
  focusScore: number;
  lifeScore: number;
}

/** Active arc summary for dashboard display */
export interface DashboardActiveArc {
  id: string;
  name: string;
  arcType: ArcType;
  progressPercentage: number;
  currentPhase: string;
}

/** Leaderboard preview data */
export interface DashboardLeaderboardPreview {
  userRank: number;
  userXpTotal: number;
  leagueName: string;
  topThree: LeaderboardEntry[];
}

/** Individual leaderboard entry */
export interface LeaderboardEntry {
  rank: number;
  username: string;
  level: number;
  xpTotal: number;
  avatarUrl: string;
  isCurrentUser: boolean;
}

/** User summary for header */
export interface DashboardUserSummary {
  displayName: string;
  level: number;
  currentStreak: number;
}
