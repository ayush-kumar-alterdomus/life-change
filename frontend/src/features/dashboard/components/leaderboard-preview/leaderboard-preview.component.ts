import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonButton } from '@ionic/angular/standalone';
import { LeaderboardCardComponent } from '../../../../shared/ui/leaderboard-card/leaderboard-card.component';
import { DashboardLeaderboardPreview } from '../../models/dashboard.models';

@Component({
  standalone: true,
  selector: 'app-leaderboard-preview',
  templateUrl: './leaderboard-preview.component.html',
  styleUrls: ['./leaderboard-preview.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, IonButton, LeaderboardCardComponent],
})
export class LeaderboardPreviewComponent {
  /** Leaderboard preview data including user rank, XP, league, and top 3 entries */
  preview = input.required<DashboardLeaderboardPreview>();

  /** Emitted when the user clicks "View Full Rankings" */
  viewFullRankings = output<void>();
}
