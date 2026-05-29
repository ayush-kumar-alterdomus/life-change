import {
  Component,
  ChangeDetectionStrategy,
  input,
  computed,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { XpFormatPipe } from '../../pipes/xp-format.pipe';

@Component({
  standalone: true,
  selector: 'game-leaderboard-card',
  templateUrl: './leaderboard-card.component.html',
  styleUrls: ['./leaderboard-card.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, XpFormatPipe],
  host: {
    'class': 'leaderboard-card',
    '[class.leaderboard-card--current-user]': 'isCurrentUser()',
    '[class.leaderboard-card--gold]': 'rank() === 1',
    '[class.leaderboard-card--silver]': 'rank() === 2',
    '[class.leaderboard-card--bronze]': 'rank() === 3',
    '[attr.aria-label]': 'ariaLabel()',
  },
})
export class LeaderboardCardComponent {
  /** Position on the leaderboard */
  rank = input.required<number>();

  /** Player username */
  username = input.required<string>();

  /** Player level */
  level = input.required<number>();

  /** Total XP earned */
  xpTotal = input.required<number>();

  /** URL for the player avatar */
  avatarUrl = input<string>('');

  /** Whether this entry represents the current user */
  isCurrentUser = input<boolean>(false);

  /** Computed rank display class for medal styling */
  rankClass = computed(() => {
    const r = this.rank();
    if (r === 1) return 'gold';
    if (r === 2) return 'silver';
    if (r === 3) return 'bronze';
    return 'default';
  });

  /** Computed aria label for accessibility */
  ariaLabel = computed(
    () =>
      `Rank ${this.rank()}, ${this.username()}, Level ${this.level()}, ${this.xpTotal()} XP`
  );
}
