import { Component, ChangeDetectionStrategy, input, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChallengeInfo } from '../../models';

@Component({
  standalone: true,
  selector: 'app-challenge-card',
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="challenge-card" [class.challenge-card--completed]="isCompleted()">
      <h4>{{ challenge().title }}</h4>
      <p class="opponent">vs {{ challenge().opponentName }}</p>
      <div class="progress-bars">
        <div class="bar">
          <span>You</span>
          <div class="bar__track"><div class="bar__fill bar__fill--me" [style.width.%]="myPercent()" [attr.aria-valuenow]="challenge().myProgress" aria-valuemin="0" [attr.aria-valuemax]="challenge().target" role="progressbar"></div></div>
          <span>{{ challenge().myProgress }}/{{ challenge().target }}</span>
        </div>
        <div class="bar">
          <span>{{ challenge().opponentName }}</span>
          <div class="bar__track"><div class="bar__fill bar__fill--opp" [style.width.%]="oppPercent()" [attr.aria-valuenow]="challenge().opponentProgress" aria-valuemin="0" [attr.aria-valuemax]="challenge().target" role="progressbar"></div></div>
          <span>{{ challenge().opponentProgress }}/{{ challenge().target }}</span>
        </div>
      </div>
      @if (isCompleted()) {
        <div class="result">{{ isWinner() ? '🏆 Won' : '💔 Lost' }}</div>
      }
    </div>
  `,
  styles: [`
    .challenge-card { background: #1a1a1a; border-radius: 12px; padding: 16px; margin-bottom: 12px; }
    .challenge-card--completed { opacity: 0.7; }
    .challenge-card h4 { color: #fff; margin: 0 0 4px; }
    .opponent { color: #aaa; font-size: 0.8rem; margin: 0 0 12px; }
    .progress-bars { display: flex; flex-direction: column; gap: 8px; }
    .bar { display: flex; align-items: center; gap: 8px; font-size: 0.75rem; color: #ccc; }
    .bar span { min-width: 60px; }
    .bar__track { flex: 1; height: 8px; background: #333; border-radius: 4px; overflow: hidden; }
    .bar__fill { height: 100%; border-radius: 4px; transition: width 300ms; }
    .bar__fill--me { background: #FF9800; }
    .bar__fill--opp { background: #A855F7; }
    .result { text-align: center; margin-top: 12px; font-weight: 600; color: #fff; }
  `],
})
export class ChallengeCardComponent {
  challenge = input.required<ChallengeInfo>();

  myPercent = computed(() => Math.min(100, (this.challenge().myProgress / this.challenge().target) * 100));
  oppPercent = computed(() => Math.min(100, (this.challenge().opponentProgress / this.challenge().target) * 100));
  isCompleted = computed(() => this.challenge().status === 'COMPLETED');
  isWinner = computed(() => this.challenge().winner === 'me');
}
