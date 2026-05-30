import { Component, ChangeDetectionStrategy, input, computed } from '@angular/core';

@Component({
  standalone: true,
  selector: 'app-stepper',
  templateUrl: './stepper.component.html',
  styleUrls: ['./stepper.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StepperComponent {
  /** Total number of steps in the flow */
  totalSteps = input.required<number>();

  /** Current active step index (0-based) */
  currentStep = input.required<number>();

  /** Array of step indices for template iteration */
  steps = computed(() => Array.from({ length: this.totalSteps() }, (_, i) => i));
}
