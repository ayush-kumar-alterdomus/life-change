import { Component, ChangeDetectionStrategy, input } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ProfileAchievement } from '../../services/profile.service';

@Component({
  standalone: true,
  selector: 'app-achievements-section',
  imports: [CommonModule, DatePipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="achievements">
      <h2 class="achievements__title">Achievements</h2>
      @if (achievements().length === 0) {
        <p class="achievements__empty">Complete quests to unlock achievements</p>
      } @else {
        <div class="achievements__scroll">
          @for (a of achievements(); track a.id) {
            <div class="achievements__card">
              <span class="achievements__icon">🏆</span>
              <span class="achievements__name">{{ a.achievementName }}</span>
              <span class="achievements__date">{{ a.unlockedAt | date: 'mediumDate' }}</span>
            </div>
          }
        </div>
      }
    </section>
  `,
  styles: [
    `
      .achievements {
        padding: 16px;
      }
      .achievements__title {
        color: #fff;
        font-size: 1.1rem;
        margin: 0 0 12px;
      }
      .achievements__empty {
        color: #666;
        text-align: center;
        padding: 24px;
        font-size: 0.85rem;
      }
      .achievements__scroll {
        display: flex;
        gap: 12px;
        overflow-x: auto;
        padding-bottom: 8px;
        scrollbar-width: none;
      }
      .achievements__scroll::-webkit-scrollbar {
        display: none;
      }
      .achievements__card {
        min-width: 140px;
        background: #1a1a1a;
        border-radius: 12px;
        padding: 16px 12px;
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 6px;
      }
      .achievements__icon {
        font-size: 1.5rem;
      }
      .achievements__name {
        color: #fff;
        font-size: 0.75rem;
        font-weight: 600;
        text-align: center;
      }
      .achievements__date {
        color: #666;
        font-size: 0.65rem;
      }
    `,
  ],
})
export class AchievementsSectionComponent {
  achievements = input.required<ProfileAchievement[]>();
}
