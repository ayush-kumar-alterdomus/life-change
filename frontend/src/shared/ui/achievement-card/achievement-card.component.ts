import {
  Component,
  ChangeDetectionStrategy,
  input,
  output,
  computed,
  HostBinding,
  HostListener,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { TimeAgoPipe } from '../../pipes/time-ago.pipe';

@Component({
  standalone: true,
  selector: 'app-achievement-card',
  templateUrl: './achievement-card.component.html',
  styleUrls: ['./achievement-card.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, TimeAgoPipe],
})
export class AchievementCardComponent {
  @HostBinding('class') readonly hostClass = 'achievement-card';
  @HostBinding('class.achievement-card--locked') get hostLocked() {
    return this.locked();
  }
  @HostBinding('class.achievement-card--unlocked') get hostUnlocked() {
    return !this.locked();
  }
  @HostBinding('attr.role') readonly hostRole = 'button';
  @HostBinding('attr.tabindex') readonly hostTabindex = '0';
  @HostBinding('attr.aria-label') get hostAriaLabel() {
    return this.ariaLabel();
  }

  @HostListener('click') onClick() {
    this.onCardTap();
  }
  @HostListener('keydown.enter') onEnter() {
    this.onCardTap();
  }
  @HostListener('keydown.space') onSpace() {
    this.onCardTap();
  }
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
