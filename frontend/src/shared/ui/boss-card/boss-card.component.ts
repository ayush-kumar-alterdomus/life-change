import { Component, ChangeDetectionStrategy, input, output, computed, HostBinding } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AppProgressComponent } from '../../components/app-progress/app-progress.component';

@Component({
  standalone: true,
  selector: 'game-boss-card',
  templateUrl: './boss-card.component.html',
  styleUrls: ['./boss-card.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, AppProgressComponent],
})
export class BossCardComponent {
  @HostBinding('class') readonly hostClass = 'boss-card';
  @HostBinding('class.boss-card--defeated') get hostDefeated() { return this.defeated(); }
  @HostBinding('attr.role') readonly hostRole = 'article';
  @HostBinding('attr.aria-label') get hostAriaLabel() { return this.ariaLabel(); }
  /** Name of the boss */
  bossName = input.required<string>();

  /** Boss level */
  bossLevel = input.required<number>();

  /** Boss remaining health percentage (0-100) */
  healthPercentage = input.required<number>();

  /** Whether the boss has been defeated */
  defeated = input<boolean>(false);

  /** Emitted when the challenge button is tapped */
  challenge = output<void>();

  /** Computed aria label for accessibility */
  ariaLabel = computed(
    () =>
      `Boss: ${this.bossName()}, Level ${this.bossLevel()}, ${this.healthPercentage()}% health remaining${this.defeated() ? ', defeated' : ''}`,
  );

  onChallenge(): void {
    if (!this.defeated()) {
      this.challenge.emit();
    }
  }
}
