import { Component, ChangeDetectionStrategy, input, computed, HostBinding } from '@angular/core';
import { CommonModule } from '@angular/common';

export type StreakIntensity = 'inactive' | 'low' | 'active' | 'epic' | 'legendary';

@Component({
  standalone: true,
  selector: 'game-streak-flame',
  templateUrl: './streak-flame.component.html',
  styleUrls: ['./streak-flame.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
})
export class StreakFlameComponent {
  @HostBinding('class') readonly hostClass = 'streak-flame';
  @HostBinding('class.streak-flame--inactive') get hostInactive() { return this.intensity() === 'inactive'; }
  @HostBinding('class.streak-flame--low') get hostLow() { return this.intensity() === 'low'; }
  @HostBinding('class.streak-flame--active') get hostActive() { return this.intensity() === 'active'; }
  @HostBinding('class.streak-flame--epic') get hostEpic() { return this.intensity() === 'epic'; }
  @HostBinding('class.streak-flame--legendary') get hostLegendary() { return this.intensity() === 'legendary'; }
  @HostBinding('attr.aria-label') get hostAriaLabel() { return this.ariaLabel(); }
  /** Number of consecutive streak days */
  streakDays = input.required<number>();

  /** Computed intensity based on streak day thresholds */
  intensity = computed<StreakIntensity>(() => {
    const days = this.streakDays();
    if (days >= 100) return 'legendary';
    if (days >= 30) return 'epic';
    if (days >= 7) return 'active';
    if (days > 0) return 'low';
    return 'inactive';
  });

  /** Accessible label describing the streak state */
  ariaLabel = computed(() => {
    const days = this.streakDays();
    if (days === 0) return 'No active streak';
    return `${days} day streak - ${this.intensity()} intensity`;
  });
}
