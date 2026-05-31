import { Injectable, inject, computed, signal } from '@angular/core';
import { DashboardService } from '../services/dashboard.service';
import { StorageService } from '../../../core/services/storage.service';
import {
  SectionState,
  DashboardUserSummary,
  DashboardXpProgress,
  DashboardDailyStats,
  DashboardActiveArc,
  DashboardLeaderboardPreview,
} from '../models/dashboard.models';
import { Quest } from '../../../shared/models/quest.model';

// Cache keys for StorageService persistence
const CACHE_KEYS = {
  userSummary: 'dashboard_user_summary',
  xpProgress: 'dashboard_xp_progress',
  dailyStats: 'dashboard_daily_stats',
  activeArc: 'dashboard_active_arc',
  todayQuests: 'dashboard_today_quests',
  leaderboardPreview: 'dashboard_leaderboard_preview',
} as const;

/**
 * Signal-based store for all dashboard state.
 * Each section has an independent SectionState<T> signal enabling
 * granular skeleton rendering and per-section error recovery.
 */
@Injectable({ providedIn: 'root' })
export class DashboardStore {
  private readonly dashboardService = inject(DashboardService);
  private readonly storageService = inject(StorageService);

  // ─── Section Signals ───────────────────────────────────────────────────────

  readonly userSummary = signal<SectionState<DashboardUserSummary>>({ status: 'idle' });
  readonly xpProgress = signal<SectionState<DashboardXpProgress>>({ status: 'idle' });
  readonly dailyStats = signal<SectionState<DashboardDailyStats>>({ status: 'idle' });
  readonly activeArc = signal<SectionState<DashboardActiveArc | null>>({ status: 'idle' });
  readonly todayQuests = signal<SectionState<Quest[]>>({ status: 'idle' });
  readonly leaderboardPreview = signal<SectionState<DashboardLeaderboardPreview>>({
    status: 'idle',
  });

  private userLeague = 'BRONZE';

  // ─── Computed: Global Loading ──────────────────────────────────────────────

  readonly isLoading = computed(
    () =>
      this.userSummary().status === 'loading' ||
      this.xpProgress().status === 'loading' ||
      this.dailyStats().status === 'loading' ||
      this.activeArc().status === 'loading' ||
      this.todayQuests().status === 'loading' ||
      this.leaderboardPreview().status === 'loading',
  );

  // ─── Computed: Per-Section Loading ─────────────────────────────────────────

  readonly userSummaryLoading = computed(() => this.userSummary().status === 'loading');
  readonly xpProgressLoading = computed(() => this.xpProgress().status === 'loading');
  readonly dailyStatsLoading = computed(() => this.dailyStats().status === 'loading');
  readonly activeArcLoading = computed(() => this.activeArc().status === 'loading');
  readonly questsLoading = computed(() => this.todayQuests().status === 'loading');
  readonly leaderboardLoading = computed(() => this.leaderboardPreview().status === 'loading');

  // ─── Computed: Per-Section Error ───────────────────────────────────────────

  readonly userSummaryError = computed(() => {
    const state = this.userSummary();
    return state.status === 'error' ? state.message : null;
  });
  readonly xpProgressError = computed(() => {
    const state = this.xpProgress();
    return state.status === 'error' ? state.message : null;
  });
  readonly dailyStatsError = computed(() => {
    const state = this.dailyStats();
    return state.status === 'error' ? state.message : null;
  });
  readonly activeArcError = computed(() => {
    const state = this.activeArc();
    return state.status === 'error' ? state.message : null;
  });
  readonly questsError = computed(() => {
    const state = this.todayQuests();
    return state.status === 'error' ? state.message : null;
  });
  readonly leaderboardError = computed(() => {
    const state = this.leaderboardPreview();
    return state.status === 'error' ? state.message : null;
  });

  // ─── Public Methods ────────────────────────────────────────────────────────

  /**
   * Load all dashboard data in parallel.
   * Restores cached data first (avoids skeletons), then fetches fresh data.
   */
  loadDashboard(): void {
    this.restoreCachedData();
    this.setAllLoading();
    this.fetchAllSections();
  }

  /**
   * Refresh all sections (pull-to-refresh).
   * Does NOT set loading state — keeps existing data visible while fetching.
   */
  refreshDashboard(): void {
    this.fetchAllSections();
  }

  /**
   * Atomically update store after quest completion.
   * Updates todayQuests, dailyStats, and xpProgress in a single synchronous tick.
   */
  completeQuest(questId: string, xpEarned: number): void {
    // Remove quest from list
    const currentQuests = this.todayQuests();
    if (currentQuests.status === 'loaded') {
      this.todayQuests.set({
        status: 'loaded',
        data: currentQuests.data.filter((q) => q.id !== questId),
      });
    }

    // Increment completed count
    const currentStats = this.dailyStats();
    if (currentStats.status === 'loaded') {
      this.dailyStats.set({
        status: 'loaded',
        data: {
          ...currentStats.data,
          questsCompleted: currentStats.data.questsCompleted + 1,
        },
      });
    }

    // Add XP
    const currentXp = this.xpProgress();
    if (currentXp.status === 'loaded') {
      this.xpProgress.set({
        status: 'loaded',
        data: {
          ...currentXp.data,
          currentXp: currentXp.data.currentXp + xpEarned,
        },
      });
    }
  }

  /**
   * Re-fetch only sections that are currently in an error state.
   * Used for connectivity recovery and manual retry.
   */
  retryErroredSections(): void {
    if (this.userSummary().status === 'error') {
      this.userSummary.set({ status: 'loading' });
      this.fetchUserSummary();
    }

    if (this.xpProgress().status === 'error') {
      this.xpProgress.set({ status: 'loading' });
      this.fetchXpProgress();
    }

    if (this.dailyStats().status === 'error') {
      this.dailyStats.set({ status: 'loading' });
      this.fetchDailyStats();
    }

    if (this.activeArc().status === 'error') {
      this.activeArc.set({ status: 'loading' });
      this.fetchActiveArc();
    }

    if (this.todayQuests().status === 'error') {
      this.todayQuests.set({ status: 'loading' });
      this.fetchTodayQuests();
    }

    if (this.leaderboardPreview().status === 'error') {
      this.leaderboardPreview.set({ status: 'loading' });
      this.fetchLeaderboardPreview();
    }
  }

  /**
   * Lazy-load leaderboard preview data independently.
   * Called after above-the-fold content has rendered.
   */
  loadLeaderboardPreview(): void {
    this.leaderboardPreview.set({ status: 'loading' });
    this.fetchLeaderboardPreview();
  }

  // ─── Private Methods ───────────────────────────────────────────────────────

  /**
   * Set all section signals to loading state.
   * Only called during initial load (not refresh).
   */
  private setAllLoading(): void {
    // Only set to loading if not already loaded (cache may have restored data)
    if (this.userSummary().status !== 'loaded') {
      this.userSummary.set({ status: 'loading' });
    }
    if (this.xpProgress().status !== 'loaded') {
      this.xpProgress.set({ status: 'loading' });
    }
    if (this.dailyStats().status !== 'loaded') {
      this.dailyStats.set({ status: 'loading' });
    }
    if (this.activeArc().status !== 'loaded') {
      this.activeArc.set({ status: 'loading' });
    }
    if (this.todayQuests().status !== 'loaded') {
      this.todayQuests.set({ status: 'loading' });
    }
    if (this.leaderboardPreview().status !== 'loaded') {
      this.leaderboardPreview.set({ status: 'loading' });
    }
  }

  /**
   * Fetch all dashboard data via the aggregated endpoint.
   * Falls back to individual fetches if the aggregated call fails.
   */
  private fetchAllSections(): void {
    this.dashboardService.getDashboard().subscribe({
      next: (response) => {
        const d = response.data;
        if (d.user) {
          this.userLeague = d.user.league ?? 'BRONZE';
          this.userSummary.set({
            status: 'loaded',
            data: {
              displayName: d.user.displayName,
              level: d.user.level,
              currentStreak: d.streak?.currentStreak ?? 0,
            },
          });
        }
        if (d.xp) {
          this.xpProgress.set({
            status: 'loaded',
            data: {
              currentLevel: d.xp.level,
              currentXp: d.xp.totalXp,
              requiredXp: d.xp.xpToNextLevel,
            },
          });
        } else {
          this.xpProgress.set({ status: 'error', message: 'XP data unavailable' });
        }
        if (d.dailyStats) {
          this.dailyStats.set({
            status: 'loaded',
            data: {
              questsCompleted: d.dailyStats.questsCompleted,
              questsTotal: d.dailyStats.questsTotal,
              currentStreak: d.streak?.currentStreak ?? 0,
              focusScore: 0,
              lifeScore: 0,
            },
          });
        } else {
          this.dailyStats.set({ status: 'error', message: 'Daily stats unavailable' });
        }
        this.activeArc.set({ status: 'loaded', data: d.activeArc ?? null });
        if (d.quests) {
          this.todayQuests.set({ status: 'loaded', data: d.quests as unknown as Quest[] });
        } else {
          this.todayQuests.set({ status: 'error', message: 'Quests unavailable' });
        }

        // Persist to cache
        const userSummaryData = {
          displayName: d.user.displayName,
          level: d.user.level,
          currentStreak: d.streak?.currentStreak ?? 0,
        };
        this.storageService.set(CACHE_KEYS.userSummary, userSummaryData);
        if (d.xp)
          this.storageService.set(CACHE_KEYS.xpProgress, {
            currentLevel: d.xp.level,
            currentXp: d.xp.totalXp,
            requiredXp: d.xp.xpToNextLevel,
          });
        if (d.dailyStats)
          this.storageService.set(CACHE_KEYS.dailyStats, {
            questsCompleted: d.dailyStats.questsCompleted,
            questsTotal: d.dailyStats.questsTotal,
            currentStreak: d.streak?.currentStreak ?? 0,
            focusScore: 0,
            lifeScore: 0,
          });
        this.storageService.set(CACHE_KEYS.activeArc, d.activeArc);
        if (d.quests) this.storageService.set(CACHE_KEYS.todayQuests, d.quests);
      },
      error: () => {
        // Aggregated call failed — fall back to individual fetches
        this.fetchUserSummary();
        this.fetchXpProgress();
        this.fetchDailyStats();
        this.fetchActiveArc();
        this.fetchTodayQuests();
      },
    });
  }

  private fetchUserSummary(): void {
    this.dashboardService.getUserSummary().subscribe({
      next: (data) => {
        this.userSummary.set({ status: 'loaded', data });
        this.storageService.set(CACHE_KEYS.userSummary, data);
      },
      error: (err) => {
        this.userSummary.set({
          status: 'error',
          message: err?.message ?? 'Failed to load user summary',
        });
      },
    });
  }

  private fetchXpProgress(): void {
    this.dashboardService.getXpProgress().subscribe({
      next: (data) => {
        this.xpProgress.set({ status: 'loaded', data });
        this.storageService.set(CACHE_KEYS.xpProgress, data);
      },
      error: (err) => {
        this.xpProgress.set({
          status: 'error',
          message: err?.message ?? 'Failed to load XP progress',
        });
      },
    });
  }

  private fetchDailyStats(): void {
    // Daily stats are now served by the aggregated endpoint.
    // This fallback re-derives stats from the quests endpoint.
    this.dashboardService.getTodayQuests().subscribe({
      next: (data) => {
        const quests = data as unknown as { completed?: boolean }[];
        const total = quests.length;
        const completed = quests.filter((q) => q.completed).length;
        this.dailyStats.set({
          status: 'loaded',
          data: {
            questsCompleted: completed,
            questsTotal: total,
            currentStreak: 0,
            focusScore: 0,
            lifeScore: 0,
          },
        });
      },
      error: (err) => {
        this.dailyStats.set({
          status: 'error',
          message: err?.message ?? 'Failed to load daily stats',
        });
      },
    });
  }

  private fetchActiveArc(): void {
    this.dashboardService.getActiveArc().subscribe({
      next: (data) => {
        this.activeArc.set({ status: 'loaded', data });
        this.storageService.set(CACHE_KEYS.activeArc, data);
      },
      error: (err) => {
        this.activeArc.set({
          status: 'error',
          message: err?.message ?? 'Failed to load active arc',
        });
      },
    });
  }

  private fetchTodayQuests(): void {
    this.dashboardService.getTodayQuests().subscribe({
      next: (data) => {
        this.todayQuests.set({ status: 'loaded', data });
        this.storageService.set(CACHE_KEYS.todayQuests, data);
      },
      error: (err) => {
        this.todayQuests.set({ status: 'error', message: err?.message ?? 'Failed to load quests' });
      },
    });
  }

  private fetchLeaderboardPreview(): void {
    this.dashboardService.getLeaderboardPreview(this.userLeague).subscribe({
      next: (data) => {
        this.leaderboardPreview.set({ status: 'loaded', data });
        this.storageService.set(CACHE_KEYS.leaderboardPreview, data);
      },
      error: (err) => {
        this.leaderboardPreview.set({
          status: 'error',
          message: err?.message ?? 'Failed to load leaderboard',
        });
      },
    });
  }

  /**
   * Restore cached data from StorageService.
   * Sets sections to 'loaded' with cached data so skeletons are avoided.
   */
  private restoreCachedData(): void {
    this.storageService.get<DashboardUserSummary>(CACHE_KEYS.userSummary).then((data) => {
      if (data && this.userSummary().status === 'idle') {
        this.userSummary.set({ status: 'loaded', data });
      }
    });

    this.storageService.get<DashboardXpProgress>(CACHE_KEYS.xpProgress).then((data) => {
      if (data && this.xpProgress().status === 'idle') {
        this.xpProgress.set({ status: 'loaded', data });
      }
    });

    this.storageService.get<DashboardDailyStats>(CACHE_KEYS.dailyStats).then((data) => {
      if (data && this.dailyStats().status === 'idle') {
        this.dailyStats.set({ status: 'loaded', data });
      }
    });

    this.storageService.get<DashboardActiveArc | null>(CACHE_KEYS.activeArc).then((data) => {
      if (data !== null && this.activeArc().status === 'idle') {
        this.activeArc.set({ status: 'loaded', data });
      }
    });

    this.storageService.get<Quest[]>(CACHE_KEYS.todayQuests).then((data) => {
      if (data && this.todayQuests().status === 'idle') {
        this.todayQuests.set({ status: 'loaded', data });
      }
    });

    this.storageService
      .get<DashboardLeaderboardPreview>(CACHE_KEYS.leaderboardPreview)
      .then((data) => {
        if (data && this.leaderboardPreview().status === 'idle') {
          this.leaderboardPreview.set({ status: 'loaded', data });
        }
      });
  }
}
