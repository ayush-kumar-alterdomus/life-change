import { Component, ChangeDetectionStrategy, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProfileTitle } from '../../services/profile.service';

@Component({
  standalone: true,
  selector: 'app-titles-section',
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="titles">
      <h2 class="titles__title">Identity Titles</h2>
      @if (titles().length === 0) {
        <p class="titles__empty">Reach stat milestones to earn titles</p>
      } @else {
        <div class="titles__list">
          @for (t of titles(); track t.name; let first = $first) {
            <div class="titles__item" [class.titles__item--active]="first">
              <span class="titles__name">{{ t.name }}</span>
              <span class="titles__stat">{{ t.statType }} · {{ t.threshold }}+</span>
            </div>
          }
        </div>
      }
    </section>
  `,
  styles: [
    `
      .titles {
        padding: 16px;
      }
      .titles__title {
        color: #fff;
        font-size: 1.1rem;
        margin: 0 0 12px;
      }
      .titles__empty {
        color: #666;
        text-align: center;
        padding: 24px;
        font-size: 0.85rem;
      }
      .titles__list {
        display: flex;
        flex-direction: column;
        gap: 8px;
      }
      .titles__item {
        background: #1a1a1a;
        border-radius: 10px;
        padding: 12px 16px;
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
      .titles__item--active {
        border: 1px solid #ff9800;
        box-shadow: 0 0 8px rgba(255, 152, 0, 0.3);
      }
      .titles__name {
        color: #fff;
        font-weight: 600;
        font-size: 0.9rem;
      }
      .titles__stat {
        color: #888;
        font-size: 0.75rem;
        text-transform: capitalize;
      }
    `,
  ],
})
export class TitlesSectionComponent {
  titles = input.required<ProfileTitle[]>();
}
