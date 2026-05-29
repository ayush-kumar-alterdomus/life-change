import {
  Component,
  ChangeDetectionStrategy,
  input,
  computed,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { XpFormatPipe } from '../../pipes/xp-format.pipe';

@Component({
  standalone: true,
  selector: 'game-xp-progress-bar',
  templateUrl: './xp-progress-bar.component.html',
  styleUrls: ['./xp-progress-bar.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, XpFormatPipe],
  host: {
    'class': 'xp-progress-bar',
    'role': 'progressbar',
    '[attr.aria-valuenow]': 'currentXp()',
    '[attr.aria-valuemin]': '0',
    '[attr.aria-valuemax]': 'requiredXp()',
    '[attr.aria-label]': '"XP progress"',
  },
})
export class XpProgressBarComponent {
  /** Current XP earned toward the next level */
  currentXp = input.required<number>();

  /** Total XP required to reach the next level */
  requiredXp = input.required<number>();

  /** Calculated fill percentage, capped at 100% */
  fillPercentage = computed(() => {
    const required = this.requiredXp();
    if (required === 0) {
      return 0;
    }
    return Math.min((this.currentXp() / required) * 100, 100);
  });
}
