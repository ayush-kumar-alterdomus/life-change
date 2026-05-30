import { Component, Input, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LevelReward } from '../../models/level-up.models';
import {
  slideUpAnimation,
  staggeredFadeIn,
} from '../celebration-overlay/celebration-overlay.animations';

@Component({
  standalone: true,
  selector: 'app-rewards-card',
  imports: [CommonModule],
  template: `
    <div class="rewards-card" @slideUp [attr.aria-label]="ariaLabel()">
      @if (rewards.length > 0) {
        <h3 class="rewards-card__title">Rewards</h3>
        <ul class="rewards-card__list" @staggeredFadeIn>
          @for (reward of rewards; track reward.name) {
            <li class="rewards-card__item">
              <span class="rewards-card__name">{{ reward.name }}</span>
              @if (reward.amount) {
                <span class="rewards-card__amount">×{{ reward.amount }}</span>
              }
            </li>
          }
        </ul>
      } @else {
        <h3 class="rewards-card__title">Level {{ level }} Achieved</h3>
      }
    </div>
  `,
  styles: [
    `
      .rewards-card {
        background: #161616;
        border-radius: 16px;
        padding: 24px;
        border: 1px solid rgba(255, 152, 0, 0.3);
        max-width: 320px;
        margin: 0 auto;
      }
      .rewards-card__title {
        color: #fff;
        font-size: 1.2rem;
        margin: 0 0 12px;
        text-align: center;
      }
      .rewards-card__list {
        list-style: none;
        padding: 0;
        margin: 0;
      }
      .rewards-card__item {
        display: flex;
        justify-content: space-between;
        padding: 8px 0;
        color: #e0e0e0;
      }
      .rewards-card__amount {
        color: #ff9800;
        font-weight: 600;
      }
    `,
  ],
  animations: [slideUpAnimation, staggeredFadeIn],
})
export class RewardsCardComponent {
  @Input({ required: true }) rewards!: LevelReward[];
  @Input({ required: true }) level!: number;

  readonly ariaLabel = computed(() => {
    if (this.rewards.length === 0) return `Level ${this.level} Achieved`;
    const items = this.rewards.map((r) => `${r.amount ?? ''} ${r.name}`.trim()).join(', ');
    return `Rewards: ${items}`;
  });
}
