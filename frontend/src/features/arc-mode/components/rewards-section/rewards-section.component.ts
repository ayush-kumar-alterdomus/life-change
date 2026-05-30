import { Component, ChangeDetectionStrategy, computed, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ArcReward } from '../../models';

@Component({
  standalone: true,
  selector: 'app-rewards-section',
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="rewards-section">
      <h2>Rewards</h2>
      @for (entry of groupedEntries(); track entry.phase) {
        <div class="rewards-section__group">
          <h3>{{ entry.phase }}</h3>
          @for (reward of entry.rewards; track reward.id) {
            <div
              class="rewards-section__item"
              [class.rewards-section__item--locked]="!reward.earned"
              [attr.aria-label]="getRewardAriaLabel(reward)"
            >
              <span>{{ reward.earned ? '✓' : '🔒' }}</span>
              <span>{{ reward.name }}</span>
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [
    `
      .rewards-section {
        padding: 16px;
      }
      .rewards-section h2 {
        color: #fff;
        font-size: 1.1rem;
        margin: 0 0 12px;
      }
      .rewards-section h3 {
        color: #aaa;
        font-size: 0.85rem;
        margin: 12px 0 4px;
      }
      .rewards-section__item {
        display: flex;
        gap: 8px;
        padding: 6px 0;
        color: #e0e0e0;
      }
      .rewards-section__item--locked {
        opacity: 0.5;
      }
    `,
  ],
})
export class RewardsSectionComponent {
  rewards = input.required<ArcReward[]>();

  groupedEntries = computed(() => {
    const groups = new Map<string, ArcReward[]>();
    for (const reward of this.rewards()) {
      const phase = reward.unlocksAtPhase;
      if (!groups.has(phase)) groups.set(phase, []);
      groups.get(phase)!.push(reward);
    }
    return Array.from(groups.entries()).map(([phase, rewards]) => ({ phase, rewards }));
  });

  getRewardAriaLabel(reward: ArcReward): string {
    const status = reward.earned ? 'earned' : `locked, unlocks at ${reward.unlocksAtPhase} phase`;
    return `Reward: ${reward.name}, ${status}`;
  }
}
