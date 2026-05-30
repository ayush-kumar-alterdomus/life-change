import { Component, ChangeDetectionStrategy, input, computed, HostBinding } from '@angular/core';
import { CommonModule } from '@angular/common';
import { XpFormatPipe } from '../../pipes/xp-format.pipe';

@Component({
  standalone: true,
  selector: 'app-leaderboard-card',
  templateUrl: './leaderboard-card.component.html',
  styleUrls: ['./leaderboard-card.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, XpFormatPipe],
})
export class LeaderboardCardComponent {
  @HostBinding('class') readonly hostClass = 'leaderboard-card';
  @HostBinding('class.leaderboard-card--current-user') get hostCurrentUser() { return this.isCurrentUser(); }
  @HostBinding('class.leaderboard-card--gold') get hostGold() { return this.rank() === 1; }
  @HostBinding('class.leaderboard-card--silver') get hostSilver() { return this.rank() === 2; }
  @HostBinding('class.leaderboard-card--bronze') get hostBronze() { return this.rank() === 3; }
  @HostBinding('attr.aria-label') get hostAriaLabel() { return this.ariaLabel(); }
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
    () => `Rank ${this.rank()}, ${this.username()}, Level ${this.level()}, ${this.xpTotal()} XP`,
  );
}
