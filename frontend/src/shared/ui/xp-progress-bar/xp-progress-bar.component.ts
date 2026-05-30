import { Component, ChangeDetectionStrategy, input, computed, HostBinding } from '@angular/core';
import { CommonModule } from '@angular/common';
import { XpFormatPipe } from '../../pipes/xp-format.pipe';

@Component({
  standalone: true,
  selector: 'app-xp-progress-bar',
  templateUrl: './xp-progress-bar.component.html',
  styleUrls: ['./xp-progress-bar.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, XpFormatPipe],
})
export class XpProgressBarComponent {
  @HostBinding('class') readonly hostClass = 'xp-progress-bar';
  @HostBinding('attr.role') readonly hostRole = 'progressbar';
  @HostBinding('attr.aria-valuenow') get hostValueNow() { return this.currentXp(); }
  @HostBinding('attr.aria-valuemin') readonly hostValueMin = 0;
  @HostBinding('attr.aria-valuemax') get hostValueMax() { return this.requiredXp(); }
  @HostBinding('attr.aria-label') readonly hostAriaLabel = 'XP progress';
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
