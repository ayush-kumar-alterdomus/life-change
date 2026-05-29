import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  selector: 'app-card',
  templateUrl: './app-card.component.html',
  styleUrls: ['./app-card.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
  host: {
    '[class.app-card--elevated]': 'elevated()',
    '[class.app-card--clickable]': 'clickable()',
    '[attr.role]': 'clickable() ? "button" : null',
    '[attr.tabindex]': 'clickable() ? 0 : null',
    '(click)': 'handleClick()',
    '(keydown.enter)': 'handleClick()',
    '(keydown.space)': 'handleClick()',
  },
})
export class AppCardComponent {
  /** Whether the card has elevated shadow styling */
  elevated = input<boolean>(false);

  /** Whether the card is interactive/clickable */
  clickable = input<boolean>(false);

  /** Emits when the card is clicked (only when clickable is true) */
  cardClick = output<void>();

  handleClick(): void {
    if (this.clickable()) {
      this.cardClick.emit();
    }
  }
}
