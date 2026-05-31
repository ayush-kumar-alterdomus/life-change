import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LeaderboardCardComponent } from '../../../../shared/ui/leaderboard-card/leaderboard-card.component';
import { LeagueInfo, LeaderboardEntry } from '../../models';

@Component({
  standalone: true,
  selector: 'app-leaderboard-section',
  imports: [CommonModule, LeaderboardCardComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (loading()) {
      <div class="skeleton">Loading leaderboard...</div>
    } @else if (error()) {
      <div class="error"><p>{{ error() }}</p><button (click)="retry.emit()">Retry</button></div>
    } @else {
      @if (leagueInfo()) {
        <div class="league-header">
          <h3>{{ leagueInfo()!.tier }} League</h3>
          <p>Rank #{{ leagueInfo()!.rank }} · {{ leagueInfo()!.weeklyXp }} XP this week</p>
          @if (leagueInfo()!.inPromotionZone) {
            <span class="badge badge--promo">Promotion Zone</span>
          }
          @if (leagueInfo()!.inDemotionZone) {
            <span class="badge badge--demo">Demotion Zone</span>
          }
        </div>
      }
      <div class="entries">
        @for (entry of entries(); track entry.rank) {
          <app-leaderboard-card
            [rank]="entry.rank"
            [username]="entry.username"
            [level]="entry.level"
            [xpTotal]="entry.xpTotal"
            [avatarUrl]="entry.avatarUrl"
            [isCurrentUser]="entry.isCurrentUser"
          />
        }
      </div>
      <button class="load-more" (click)="loadMore.emit()">Load More</button>
    }
  `,
  styles: [`
    .league-header { padding: 16px; text-align: center; }
    .league-header h3 { color: #fff; margin: 0 0 4px; }
    .league-header p { color: #aaa; margin: 0 0 8px; font-size: 0.85rem; }
    .badge { padding: 4px 8px; border-radius: 4px; font-size: 0.7rem; font-weight: 600; }
    .badge--promo { background: #10b981; color: #fff; }
    .badge--demo { background: #ef4444; color: #fff; }
    .entries { padding: 0 16px; display: flex; flex-direction: column; gap: 8px; }
    .load-more { display: block; margin: 16px auto; padding: 8px 24px; background: #333; border: none; color: #aaa; border-radius: 8px; cursor: pointer; }
    .skeleton, .error { text-align: center; padding: 48px; color: #888; }
    .error button { margin-top: 12px; padding: 8px 16px; background: #FF9800; border: none; border-radius: 8px; color: #fff; }
  `],
})
export class LeaderboardSectionComponent {
  leagueInfo = input.required<LeagueInfo | null>();
  entries = input.required<LeaderboardEntry[]>();
  loading = input<boolean>(false);
  error = input<string | null>(null);
  loadMore = output<void>();
  retry = output<void>();
}
