import {
  Component,
  ChangeDetectionStrategy,
  computed,
  input,
  CUSTOM_ELEMENTS_SCHEMA,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { IonProgressBar, IonText, IonCard, IonCardContent } from '@ionic/angular/standalone';

@Component({
  standalone: true,
  selector: 'app-progress-summary',
  templateUrl: './progress-summary.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  imports: [CommonModule, IonProgressBar, IonText, IonCard, IonCardContent],
})
export class ProgressSummaryComponent {
  /** Number of completed quests */
  completed = input.required<number>();

  /** Total number of quests */
  total = input.required<number>();

  /** Computed ratio of completed to total (0 when total is 0) */
  ratio = computed(() => (this.total() > 0 ? this.completed() / this.total() : 0));
}
