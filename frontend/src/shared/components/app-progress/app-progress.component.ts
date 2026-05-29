import {
  Component,
  ChangeDetectionStrategy,
  input,
  computed,
} from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  selector: 'app-progress',
  templateUrl: './app-progress.component.html',
  styleUrls: ['./app-progress.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
  host: {
    'class': 'app-progress',
    '[class.app-progress--striped]': 'striped()',
    '[class.app-progress--animated]': 'animated()',
    'role': 'progressbar',
    '[attr.aria-valuenow]': 'clampedValue()',
    '[attr.aria-valuemin]': '0',
    '[attr.aria-valuemax]': '100',
  },
})
export class AppProgressComponent {
  /** Progress value from 0 to 100 */
  value = input<number>(0);

  /** Color variant for the progress fill */
  color = input<'primary' | 'secondary' | 'success' | 'danger'>('primary');

  /** Whether to apply smooth transition animation */
  animated = input<boolean>(true);

  /** Whether to show the percentage label */
  showLabel = input<boolean>(false);

  /** Position of the percentage label */
  labelPosition = input<'inside' | 'above'>('above');

  /** Whether to apply a striped pattern to the fill */
  striped = input<boolean>(false);

  /** Clamped value between 0 and 100 */
  clampedValue = computed(() => Math.min(Math.max(this.value(), 0), 100));
}
