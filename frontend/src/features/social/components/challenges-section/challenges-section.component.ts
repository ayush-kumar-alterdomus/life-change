import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChallengeCardComponent } from '../challenge-card/challenge-card.component';
import { ChallengeInfo } from '../../models';

@Component({
  standalone: true,
  selector: 'app-challenges-section',
  imports: [CommonModule, ChallengeCardComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (loading()) {
      <div class="skeleton">Loading challenges...</div>
    } @else if (error()) {
      <div class="error"><p>{{ error() }}</p><button (click)="retry.emit()">Retry</button></div>
    } @else {
      <div class="challenges-list">
        @for (challenge of challenges(); track challenge.id) {
          <app-challenge-card [challenge]="challenge" />
        } @empty {
          <div class="empty">No active challenges. Challenge a friend!</div>
        }
      </div>
      <button class="fab" (click)="createChallenge.emit()">⚔️ Challenge a Friend</button>
    }
  `,
  styles: [`
    .challenges-list { padding: 16px; }
    .empty { text-align: center; padding: 48px; color: #888; }
    .fab { display: block; margin: 16px auto; padding: 12px 24px; background: #FF9800; border: none; border-radius: 12px; color: #fff; font-weight: 600; cursor: pointer; }
    .skeleton, .error { text-align: center; padding: 48px; color: #888; }
    .error button { margin-top: 12px; padding: 8px 16px; background: #FF9800; border: none; border-radius: 8px; color: #fff; }
  `],
})
export class ChallengesSectionComponent {
  challenges = input.required<ChallengeInfo[]>();
  loading = input<boolean>(false);
  error = input<string | null>(null);
  createChallenge = output<void>();
  retry = output<void>();
}
