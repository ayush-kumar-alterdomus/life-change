import { Component, ChangeDetectionStrategy, input, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { XpProgressBarComponent } from '../../../../shared/ui/xp-progress-bar/xp-progress-bar.component';
import { formatXpDisplay } from '../../utils/xp-format.util';

@Component({
  standalone: true,
  selector: 'app-xp-progress-card',
  templateUrl: './xp-progress-card.component.html',
  styleUrls: ['./xp-progress-card.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, XpProgressBarComponent],
})
export class XpProgressCardComponent {
  /** The user's current level */
  currentLevel = input.required<number>();

  /** Current XP earned toward the next level */
  currentXp = input.required<number>();

  /** Total XP required to reach the next level */
  requiredXp = input.required<number>();

  /** Formatted XP display string (e.g., "2,400 / 3,000 XP") */
  formattedXp = computed(() => formatXpDisplay(this.currentXp(), this.requiredXp()));
}
