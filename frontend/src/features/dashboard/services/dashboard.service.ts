import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpContext } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { SKIP_LOADING } from '../../../core/interceptors/http-context-tokens';
import {
  DashboardUserSummary,
  DashboardXpProgress,
  DashboardDailyStats,
  DashboardActiveArc,
  DashboardLeaderboardPreview,
} from '../models/dashboard.models';
import { Quest } from '../../../shared/models/quest.model';

/**
 * Service responsible for all dashboard-related API communication.
 * Uses SKIP_LOADING HttpContext token on all requests to prevent
 * the global loading overlay — the dashboard manages its own skeleton states.
 */
@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;
  private readonly skipLoadingContext = new HttpContext().set(SKIP_LOADING, true);

  getUserSummary(): Observable<DashboardUserSummary> {
    return this.http.get<DashboardUserSummary>(`${this.baseUrl}/users/me/summary`, {
      context: this.skipLoadingContext,
    });
  }

  getXpProgress(): Observable<DashboardXpProgress> {
    return this.http.get<DashboardXpProgress>(`${this.baseUrl}/xp/summary`, {
      context: this.skipLoadingContext,
    });
  }

  getDailyStats(): Observable<DashboardDailyStats> {
    return this.http.get<DashboardDailyStats>(`${this.baseUrl}/stats/daily`, {
      context: this.skipLoadingContext,
    });
  }

  getActiveArc(): Observable<DashboardActiveArc | null> {
    return this.http.get<DashboardActiveArc | null>(`${this.baseUrl}/arcs/active`, {
      context: this.skipLoadingContext,
    });
  }

  getTodayQuests(): Observable<Quest[]> {
    return this.http.get<Quest[]>(`${this.baseUrl}/quests/today`, {
      context: this.skipLoadingContext,
    });
  }

  getLeaderboardPreview(): Observable<DashboardLeaderboardPreview> {
    return this.http.get<DashboardLeaderboardPreview>(`${this.baseUrl}/league/leaderboard`, {
      context: this.skipLoadingContext,
    });
  }

  completeQuest(questId: string): Observable<{ xpEarned: number }> {
    return this.http.post<{ xpEarned: number }>(
      `${this.baseUrl}/quests/complete`,
      { questId },
      { context: this.skipLoadingContext },
    );
  }

  skipQuest(questId: string): Observable<void> {
    return this.http.post<void>(
      `${this.baseUrl}/quests/skip`,
      { questId },
      { context: this.skipLoadingContext },
    );
  }
}
