import { Component, ChangeDetectionStrategy, input, HostBinding } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  selector: 'app-level-badge',
  templateUrl: './level-badge.component.html',
  styleUrls: ['./level-badge.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
})
export class LevelBadgeComponent {
  @HostBinding('class') readonly hostClass = 'level-badge';
  @HostBinding('class.level-badge--small') get hostSmall() { return this.size() === 'small'; }
  @HostBinding('class.level-badge--large') get hostLarge() { return this.size() === 'large'; }
  @HostBinding('attr.aria-label') get hostAriaLabel() { return `Level ${this.level()}`; }
  /** The user's current level */
  level = input.required<number>();

  /** Size variant: small for inline use, large for profile/header */
  size = input<'small' | 'large'>('small');
}
