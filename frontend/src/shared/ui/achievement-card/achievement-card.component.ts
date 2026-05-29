import { Component, ChangeDetectionStrategy, input, output, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TimeAgoPipe } from '../../pipes/time-ago.pipe';

@Component({
  standalone: true,
  selector: 'game-achievement-card',
  templateUrl: './achievement-card.component.html',
  styleUrls: ['./achievement-card.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, TimeAgoPipe],
  host: {
    class: 'achievement-card',
    '[class.achievement-card--locked]': 'locked()',
    '[class.achievement-card--unlocked]': '!locked()',
    '(click)': 'onCardTap()',
    role: 'button',
    tabindex: '0',
    '(keydown.enter)': 'onCardTap()',
    '(keydown.space)': 'onCardTap()',
    '[attr.aria-label]': 'ariaLabel()',
  },
})
export class AchievementCardComponent {
  /** Achievement title */
  title = input.required<string>();

  /** Achievement description */
  description = input.required<string>();

  /** URL for the achievement icon */
  iconUrl = input.required<string>();

  /** Date when the achievement was unlocked, null if still locked */
  unlockedAt = input<Date | null>(null);

  /** Whether the achievement is locked */
  locked = input<boolean>(true);

  /** Emitted when the card is tapped */
  tap = output<void>();

  /** Computed aria label for accessibility */
  ariaLabel = computed(() => {
    const status = this.locked() ? 'locked' : 'unlocked';
    return `${this.title()} achievement, ${status}: ${this.description()}`;
  });

  onCardTap(): void {
    this.tap.emit();
  }
}
