import { Component, ChangeDetectionStrategy, input, computed } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import {
  StatRadarComponent,
  StatRadarPoint,
} from '../../../../shared/ui/stat-radar/stat-radar.component';
import { ProfileStats } from '../../services/profile.service';

@Component({
  standalone: true,
  selector: 'app-stats-section',
  imports: [CommonModule, DecimalPipe, StatRadarComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="stats-section">
      <h2 class="stats-section__title">Character Stats</h2>
      <app-stat-radar [stats]="radarPoints()" [size]="220" />
      <div class="stats-section__life-score">
        <span class="stats-section__life-score-label">Life Score</span>
        <span class="stats-section__life-score-value">{{
          stats().lifeScore | number: '1.1-1'
        }}</span>
      </div>
    </section>
  `,
  styles: [
    `
      .stats-section {
        padding: 16px;
        display: flex;
        flex-direction: column;
        align-items: center;
      }
      .stats-section__title {
        color: #fff;
        font-size: 1.1rem;
        margin: 0 0 12px;
        align-self: flex-start;
      }
      .stats-section__life-score {
        display: flex;
        justify-content: center;
        align-items: baseline;
        gap: 8px;
        margin-top: 12px;
      }
      .stats-section__life-score-label {
        color: #888;
        font-size: 0.85rem;
      }
      .stats-section__life-score-value {
        color: #ff9800;
        font-size: 1.8rem;
        font-weight: 700;
      }
    `,
  ],
})
export class StatsSectionComponent {
  stats = input.required<ProfileStats>();

  radarPoints = computed<StatRadarPoint[]>(() => {
    const s = this.stats();
    return [
      { name: 'Strength', value: s.strength },
      { name: 'Wisdom', value: s.wisdom },
      { name: 'Focus', value: s.focus },
      { name: 'Discipline', value: s.discipline },
      { name: 'Vitality', value: s.vitality },
      { name: 'Charisma', value: s.charisma },
    ];
  });
}
