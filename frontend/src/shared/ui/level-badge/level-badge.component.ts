import { Component, ChangeDetectionStrategy, input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  selector: 'game-level-badge',
  templateUrl: './level-badge.component.html',
  styleUrls: ['./level-badge.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
  host: {
    class: 'level-badge',
    '[class.level-badge--small]': 'size() === "small"',
    '[class.level-badge--large]': 'size() === "large"',
    '[attr.aria-label]': '"Level " + level()',
  },
})
export class LevelBadgeComponent {
  /** The user's current level */
  level = input.required<number>();

  /** Size variant: small for inline use, large for profile/header */
  size = input<'small' | 'large'>('small');
}
