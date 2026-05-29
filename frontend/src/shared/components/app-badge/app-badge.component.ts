import { Component, ChangeDetectionStrategy, input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  selector: 'app-badge',
  templateUrl: './app-badge.component.html',
  styleUrls: ['./app-badge.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
  host: {
    '[class]': '"app-badge--" + color() + " app-badge--" + size()',
    '[class.app-badge--dot-only]': 'dotOnly()',
  },
})
export class AppBadgeComponent {
  /** Color variant mapped to design tokens */
  color = input<'primary' | 'secondary' | 'success' | 'danger' | 'warning' | 'neutral'>('primary');

  /** Size variant */
  size = input<'small' | 'medium'>('medium');

  /** When true, renders only a colored dot without text content */
  dotOnly = input<boolean>(false);
}
