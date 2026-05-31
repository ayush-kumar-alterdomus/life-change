import { Component, ChangeDetectionStrategy, computed, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Boss } from '@shared/models';

@Component({
  standalone: true,
  selector: 'app-boss-section',
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (hasBoss()) {
      <div class="boss-section">
        <h2>Boss Encounter</h2>
        <div class="boss-section__card">
          <span class="boss-section__name">{{ boss()!.name }}</span>
          @if (isDefeated()) {
            <span class="boss-section__defeated">Defeated ✓</span>
          } @else {
            <div class="boss-section__health">HP: {{ boss()!.healthPercentage }}%</div>
          }
        </div>
      </div>
    }
  `,
  styles: [
    `
      .boss-section {
        padding: 16px;
      }
      .boss-section h2 {
        color: #fff;
        font-size: 1.1rem;
        margin: 0 0 12px;
      }
      .boss-section__card {
        background: #1a1a1a;
        border-radius: 12px;
        padding: 16px;
      }
      .boss-section__name {
        color: #fff;
        font-weight: 600;
      }
      .boss-section__defeated {
        color: #4caf50;
        margin-left: 8px;
        text-decoration: line-through;
      }
      .boss-section__health {
        color: #ef4444;
        margin-top: 8px;
      }
    `,
  ],
})
export class BossSectionComponent {
  boss = input.required<Boss | null>();

  hasBoss = computed(() => !!this.boss());
  isDefeated = computed(() => this.boss()?.healthPercentage === 0);
}
