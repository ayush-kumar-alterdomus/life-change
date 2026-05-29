import { Component, ChangeDetectionStrategy, input, output, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ArcType } from '../../enums/arc-type.enum';
import { AppProgressComponent } from '../../components/app-progress/app-progress.component';

@Component({
  standalone: true,
  selector: 'game-arc-card',
  templateUrl: './arc-card.component.html',
  styleUrls: ['./arc-card.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, AppProgressComponent],
  host: {
    class: 'arc-card',
    '[class.arc-card--monk]': 'arcType() === ArcType.Monk',
    '[class.arc-card--warrior]': 'arcType() === ArcType.Warrior',
    '[class.arc-card--scholar]': 'arcType() === ArcType.Scholar',
    '[class.arc-card--creator]': 'arcType() === ArcType.Creator',
    '[class.arc-card--athlete]': 'arcType() === ArcType.Athlete',
    '(click)': 'onCardTap()',
    role: 'button',
    tabindex: '0',
    '(keydown.enter)': 'onCardTap()',
    '(keydown.space)': 'onCardTap()',
    '[attr.aria-label]': 'ariaLabel()',
  },
})
export class ArcCardComponent {
  protected readonly ArcType = ArcType;

  /** Name of the arc */
  arcName = input.required<string>();

  /** Progress percentage (0-100) */
  progressPercentage = input.required<number>();

  /** Current phase name */
  currentPhase = input.required<string>();

  /** Type of arc determining the themed gradient */
  arcType = input.required<ArcType>();

  /** Emitted when the card is tapped */
  navigate = output<void>();

  /** Computed aria label for accessibility */
  ariaLabel = computed(
    () =>
      `${this.arcName()} arc, ${this.progressPercentage()}% complete, current phase: ${this.currentPhase()}`,
  );

  onCardTap(): void {
    this.navigate.emit();
  }
}
