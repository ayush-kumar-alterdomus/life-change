import { Component, ChangeDetectionStrategy, input, output, computed } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  selector: 'game-guild-card',
  templateUrl: './guild-card.component.html',
  styleUrls: ['./guild-card.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
  host: {
    class: 'guild-card',
    role: 'article',
    '[attr.aria-label]': 'ariaLabel()',
  },
})
export class GuildCardComponent {
  /** Name of the guild */
  guildName = input.required<string>();

  /** Number of members in the guild */
  memberCount = input.required<number>();

  /** Level of the guild */
  guildLevel = input.required<number>();

  /** Rank of the guild (optional) */
  guildRank = input<number | null>(null);

  /** Emitted when the user wants to join the guild */
  join = output<void>();

  /** Emitted when the user wants to view guild details */
  view = output<void>();

  /** Computed aria label for accessibility */
  ariaLabel = computed(() => {
    const rank = this.guildRank();
    const base = `${this.guildName()}, level ${this.guildLevel()}, ${this.memberCount()} members`;
    return rank !== null ? `${base}, rank ${rank}` : base;
  });

  onJoin(event: Event): void {
    event.stopPropagation();
    this.join.emit();
  }

  onView(): void {
    this.view.emit();
  }
}
