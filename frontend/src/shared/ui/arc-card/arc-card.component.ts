import { Component, ChangeDetectionStrategy, input, output, computed, HostBinding, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ArcType } from '../../enums/arc-type.enum';
import { AppProgressComponent } from '../../components/app-progress/app-progress.component';

@Component({
  standalone: true,
  selector: 'app-arc-card',
  templateUrl: './arc-card.component.html',
  styleUrls: ['./arc-card.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, AppProgressComponent],
})
export class ArcCardComponent {
  protected readonly ArcType = ArcType;

  @HostBinding('class') readonly hostClass = 'arc-card';
  @HostBinding('class.arc-card--monk') get hostMonk() { return this.arcType() === ArcType.Monk; }
  @HostBinding('class.arc-card--warrior') get hostWarrior() { return this.arcType() === ArcType.Warrior; }
  @HostBinding('class.arc-card--scholar') get hostScholar() { return this.arcType() === ArcType.Scholar; }
  @HostBinding('class.arc-card--creator') get hostCreator() { return this.arcType() === ArcType.Creator; }
  @HostBinding('class.arc-card--athlete') get hostAthlete() { return this.arcType() === ArcType.Athlete; }
  @HostBinding('attr.role') readonly hostRole = 'button';
  @HostBinding('attr.tabindex') readonly hostTabindex = '0';
  @HostBinding('attr.aria-label') get hostAriaLabel() { return this.ariaLabel(); }

  @HostListener('click') onClick() { this.onCardTap(); }
  @HostListener('keydown.enter') onEnter() { this.onCardTap(); }
  @HostListener('keydown.space') onSpace() { this.onCardTap(); }

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
