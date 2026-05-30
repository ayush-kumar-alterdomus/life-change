import { Component, ChangeDetectionStrategy, computed, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LevelBadgeComponent } from '../../../../shared/ui/level-badge/level-badge.component';
import { StreakFlameComponent } from '../../../../shared/ui/streak-flame/streak-flame.component';
import { getTimeBasedGreeting } from '../../utils/greeting.util';

@Component({
  standalone: true,
  selector: 'app-header-section',
  templateUrl: './header-section.component.html',
  styleUrls: ['./header-section.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, LevelBadgeComponent, StreakFlameComponent],
})
export class HeaderSectionComponent {
  /** The user's display name, or null if not yet loaded */
  displayName = input<string | null>(null);

  /** The user's current level */
  level = input.required<number>();

  /** The user's current streak in days */
  streakDays = input.required<number>();

  /** Computed greeting based on time of day and display name */
  greeting = computed(() => getTimeBasedGreeting(this.displayName()));
}
