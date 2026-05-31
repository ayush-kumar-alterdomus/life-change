import { Component, ChangeDetectionStrategy, input } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ProfileStreak } from '../../services/profile.service';

@Component({
  standalone: true,
  selector: 'app-activity-summary',
  imports: [CommonModule, DatePipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="activity">
      <h2 class="activity__title">Activity</h2>
      <div class="activity__grid">
        <div class="activity__card">
          <span class="activity__icon">🔥</span>
          <span class="activity__value">{{ streak().currentStreak }}</span>
          <span class="activity__label">Current Streak</span>
        </div>
        <div class="activity__card">
          <span class="activity__icon">⚡</span>
          <span class="activity__value">{{ streak().longestStreak }}</span>
          <span class="activity__label">Longest Streak</span>
        </div>
        <div class="activity__card">
          <span class="activity__icon">📅</span>
          <span class="activity__value">{{ memberSince() | date: 'MMM yyyy' }}</span>
          <span class="activity__label">Member Since</span>
        </div>
        <div class="activity__card">
          <span class="activity__icon">✕</span>
          <span class="activity__value">{{ streak().comboMultiplier | number: '1.2-2' }}x</span>
          <span class="activity__label">Combo</span>
        </div>
      </div>
    </section>
  `,
  styles: [
    `
      .activity {
        padding: 16px;
      }
      .activity__title {
        color: #fff;
        font-size: 1.1rem;
        margin: 0 0 12px;
      }
      .activity__grid {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 10px;
      }
      .activity__card {
        background: #1a1a1a;
        border-radius: 12px;
        padding: 14px 12px;
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 4px;
      }
      .activity__icon {
        font-size: 1.2rem;
      }
      .activity__value {
        color: #fff;
        font-size: 1.1rem;
        font-weight: 700;
      }
      .activity__label {
        color: #888;
        font-size: 0.7rem;
      }
    `,
  ],
})
export class ActivitySummaryComponent {
  streak = input.required<ProfileStreak>();
  memberSince = input.required<string>();
}
