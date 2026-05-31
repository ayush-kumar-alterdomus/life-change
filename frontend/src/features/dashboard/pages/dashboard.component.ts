import {
  Component,
  OnInit,
  AfterViewInit,
  ChangeDetectionStrategy,
  inject,
  effect,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { IonicModule } from '@ionic/angular';

import { DashboardStore } from '../state/dashboard.store';
import { DashboardService } from '../services/dashboard.service';
import { ConnectivityService } from '../../../core/services/connectivity.service';
import { HapticService } from '../../../core/services/haptic.service';
import { QuestCompletionService } from '../../quest-completion/services/quest-completion.service';

import { HeaderSectionComponent } from '../components/header-section/header-section.component';
import { XpProgressCardComponent } from '../components/xp-progress-card/xp-progress-card.component';
import { DailySummaryComponent } from '../components/daily-summary/daily-summary.component';
import { ActiveArcSectionComponent } from '../components/active-arc-section/active-arc-section.component';
import { QuestListComponent } from '../components/quest-list/quest-list.component';
import { MotivationWidgetComponent } from '../components/motivation-widget/motivation-widget.component';
import { LeaderboardPreviewComponent } from '../components/leaderboard-preview/leaderboard-preview.component';

/**
 * DashboardPage — the primary smart component for the home tab.
 * Orchestrates data loading via DashboardStore, delegates rendering
 * to dumb child components, and handles user interactions (swipe actions,
 * pull-to-refresh, navigation).
 */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    IonicModule,
    HeaderSectionComponent,
    XpProgressCardComponent,
    DailySummaryComponent,
    ActiveArcSectionComponent,
    QuestListComponent,
    MotivationWidgetComponent,
    LeaderboardPreviewComponent,
  ],
})
export class DashboardComponent implements OnInit, AfterViewInit {
  private readonly store = inject(DashboardStore);
  private readonly dashboardService = inject(DashboardService);
  private readonly connectivity = inject(ConnectivityService);
  private readonly router = inject(Router);
  private readonly haptic = inject(HapticService);
  private readonly questCompletionService = inject(QuestCompletionService);

  // ─── Expose store signals to template ──────────────────────────────────────

  readonly userSummary = this.store.userSummary;
  readonly xpProgress = this.store.xpProgress;
  readonly dailyStats = this.store.dailyStats;
  readonly activeArc = this.store.activeArc;
  readonly todayQuests = this.store.todayQuests;
  readonly leaderboardPreview = this.store.leaderboardPreview;
  readonly isLoading = this.store.isLoading;

  // Per-section loading signals
  readonly userSummaryLoading = this.store.userSummaryLoading;
  readonly xpProgressLoading = this.store.xpProgressLoading;
  readonly dailyStatsLoading = this.store.dailyStatsLoading;
  readonly activeArcLoading = this.store.activeArcLoading;
  readonly questsLoading = this.store.questsLoading;
  readonly leaderboardLoading = this.store.leaderboardLoading;

  // Per-section error signals
  readonly userSummaryError = this.store.userSummaryError;
  readonly xpProgressError = this.store.xpProgressError;
  readonly dailyStatsError = this.store.dailyStatsError;
  readonly activeArcError = this.store.activeArcError;
  readonly questsError = this.store.questsError;
  readonly leaderboardError = this.store.leaderboardError;

  // Track whether leaderboard has been loaded
  private readonly viewportLoaded = signal(false);

  constructor() {
    this.setupConnectivityWatcher();
  }

  ngOnInit(): void {
    this.store.loadDashboard();
  }

  ngAfterViewInit(): void {
    // Lazy-load leaderboard after above-the-fold content renders
    setTimeout(() => {
      this.store.loadLeaderboardPreview();
      this.viewportLoaded.set(true);
    }, 0);
  }

  // ─── Pull-to-Refresh ──────────────────────────────────────────────────────

  onRefresh(event: { target: { complete: () => void } }): void {
    this.store.refreshDashboard();

    // Complete the refresher after a reasonable timeout
    // In production, this would be tied to the forkJoin completion
    setTimeout(() => {
      event.target.complete();
    }, 2000);
  }

  // ─── Quest Swipe Action Handlers ──────────────────────────────────────────

  onCompleteQuest(questId: string): void {
    const questsState = this.todayQuests();
    if (questsState.status !== 'loaded') return;
    const quest = questsState.data.find((q) => q.id === questId);
    if (!quest) return;
    this.questCompletionService.completeQuest(quest).subscribe();
  }

  onSkipQuest(questId: string): void {
    this.dashboardService.skipQuest(questId).subscribe({
      next: () => {
        this.haptic.impact('light');
      },
    });
  }

  onEditQuest(questId: string): void {
    this.router.navigate(['/tabs/quests/edit', questId]);
  }

  onViewAllQuests(): void {
    this.router.navigate(['/tabs/quests']);
  }

  // ─── Arc Navigation ────────────────────────────────────────────────────────

  onNavigateToArc(): void {
    const arc = this.activeArc();
    if (arc.status === 'loaded' && arc.data) {
      this.router.navigate(['/tabs/arc-mode', arc.data.id]);
    }
  }

  onNavigateToArcSelection(): void {
    this.router.navigate(['/tabs/arc-mode']);
  }

  // ─── Leaderboard Navigation ────────────────────────────────────────────────

  onViewFullRankings(): void {
    this.router.navigate(['/tabs/social/leaderboard']);
  }

  // ─── Section Retry ─────────────────────────────────────────────────────────

  onRetryAll(): void {
    this.store.loadDashboard();
  }

  onRetrySection(): void {
    this.store.retryErroredSections();
  }

  // ─── Connectivity Watcher ──────────────────────────────────────────────────

  private setupConnectivityWatcher(): void {
    effect(() => {
      if (this.connectivity.isOnline()) {
        this.store.retryErroredSections();
      }
    });
  }
}
