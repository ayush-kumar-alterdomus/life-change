import { Component, ChangeDetectionStrategy, input, computed, HostBinding } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MiniCardComponent } from '../mini-card/mini-card.component';
import { DashboardDailyStats } from '../../models';

export interface MiniCardData {
  icon: string;
  value: string;
  label: string;
}

@Component({
  standalone: true,
  selector: 'app-daily-summary',
  templateUrl: './daily-summary.component.html',
  styleUrls: ['./daily-summary.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, MiniCardComponent],
})
export class DailySummaryComponent {
  @HostBinding('class') readonly hostClass = 'daily-summary';

  /** Daily statistics data from the dashboard store */
  stats = input.required<DashboardDailyStats>();

  /** Computed mini-card data derived from stats */
  cards = computed<MiniCardData[]>(() => {
    const s = this.stats();
    return [
      {
        icon: 'checkmark-circle-outline',
        value: `${s.questsCompleted}/${s.questsTotal}`,
        label: 'Quests',
      },
      {
        icon: 'flame-outline',
        value: `${s.currentStreak}`,
        label: 'Streak',
      },
      {
        icon: 'eye-outline',
        value: `${s.focusScore}`,
        label: 'Focus Score',
      },
      {
        icon: 'heart-outline',
        value: `${s.lifeScore}`,
        label: 'Life Score',
      },
    ];
  });
}
