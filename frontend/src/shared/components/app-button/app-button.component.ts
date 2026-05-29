import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  selector: 'app-button',
  templateUrl: './app-button.component.html',
  styleUrls: ['./app-button.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
  host: {
    '[class]': '"app-button--" + variant() + " app-button--" + size()',
  },
})
export class AppButtonComponent {
  /** Accessible label for the button (use when content is icon-only) */
  ariaLabel = input<string>('');

  /** Visual variant of the button */
  variant = input<'primary' | 'secondary' | 'outline' | 'ghost' | 'danger'>('primary');

  /** Size of the button */
  size = input<'small' | 'medium' | 'large'>('medium');

  /** Whether the button is disabled */
  disabled = input<boolean>(false);

  /** Whether the button is in a loading state */
  loading = input<boolean>(false);

  /** Emits when the button is clicked (only when not disabled and not loading) */
  clicked = output<MouseEvent>();

  handleClick(event: MouseEvent): void {
    if (!this.disabled() && !this.loading()) {
      this.clicked.emit(event);
    }
  }

  handleKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      if (!this.disabled() && !this.loading()) {
        this.clicked.emit(new MouseEvent('click'));
      }
    }
  }
}
