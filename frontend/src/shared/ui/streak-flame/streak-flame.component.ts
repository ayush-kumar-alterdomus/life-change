import {
  Component,
  ChangeDetectionStrategy,
  input,
  computed,
} from '@angular/core';
import { CommonModule } from '@angular/common';

export type StreakIntensity = 'inactive' | 'low' | 'active' | 'epic' | 'legendary';

@Component({
  standalone: true,
  selector: 'game-streak-flame',
  templateUrl: './streak-flame.component.html',
  styleUrls: ['./streak-flame.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
  host: {
    'class': 'streak-flame',
    '[class.streak-flame--inactive]': 'intensity() === "inactive"',
    '[class.streak-flame--low]': 'intensity() === "low"',
    '[class.streak-flame--active]': 'intensity() === "active"',
    '[class.streak-flame--epic]': 'intensity() === "epic"',
    '[class.streak-flame--legendary]': 'intensity() === "legendary"',
    '[attr.aria-label]': 'ariaLabel()',
  },
})
export class StreakFlameComponent {
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
