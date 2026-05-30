import { Component, ChangeDetectionStrategy, input, output, HostBinding, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  selector: 'app-card',
  templateUrl: './app-card.component.html',
  styleUrls: ['./app-card.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
})
export class AppCardComponent {
  /** Whether the card has elevated shadow styling */
  elevated = input<boolean>(false);

  /** Whether the card is interactive/clickable */
  clickable = input<boolean>(false);

  /** Emits when the card is clicked (only when clickable is true) */
  cardClick = output<void>();

  @HostBinding('class.app-card--elevated') get hostElevated() { return this.elevated(); }
  @HostBinding('class.app-card--clickable') get hostClickable() { return this.clickable(); }
  @HostBinding('attr.role') get hostRole() { return this.clickable() ? 'button' : null; }
  @HostBinding('attr.tabindex') get hostTabindex() { return this.clickable() ? 0 : null; }

  @HostListener('click') onClick() { this.handleClick(); }
  @HostListener('keydown.enter') onEnter() { this.handleClick(); }
  @HostListener('keydown.space') onSpace() { this.handleClick(); }

  handleClick(): void {
    if (this.clickable()) {
      this.cardClick.emit();
    }
  }
}
