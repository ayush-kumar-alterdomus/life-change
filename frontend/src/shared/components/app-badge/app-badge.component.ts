import { Component, ChangeDetectionStrategy, input, HostBinding } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  selector: 'app-badge',
  templateUrl: './app-badge.component.html',
  styleUrls: ['./app-badge.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
})
export class AppBadgeComponent {
  /** Color variant mapped to design tokens */
  color = input<'primary' | 'secondary' | 'success' | 'danger' | 'warning' | 'neutral'>('primary');

  /** Size variant */
  size = input<'small' | 'medium'>('medium');

  /** When true, renders only a colored dot without text content */
  dotOnly = input<boolean>(false);

  @HostBinding('class') get hostClass() {
    return `app-badge--${this.color()} app-badge--${this.size()}`;
  }

  @HostBinding('class.app-badge--dot-only') get hostDotOnly() {
    return this.dotOnly();
  }
}
