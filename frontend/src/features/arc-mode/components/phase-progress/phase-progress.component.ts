import { Component, ChangeDetectionStrategy, computed, input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  selector: 'app-phase-progress',
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="phase-progress" [attr.aria-label]="ariaLabel()">
      @for (phase of PHASES; track phase; let i = $index) {
        <div class="phase-progress__step"
          [class.phase-progress__step--completed]="i < currentStepIndex()"
          [class.phase-progress__step--active]="i === currentStepIndex()"
          [class.phase-progress__step--upcoming]="i > currentStepIndex()">
          <span class="phase-progress__label">{{ phase }}</span>
        </div>
      }
    </div>
  `,
  styles: [`
    .phase-progress { display: flex; gap: 8px; justify-content: center; padding: 16px; }
    .phase-progress__step { text-align: center; flex: 1; padding: 8px; border-radius: 8px; font-size: 0.75rem; }
    .phase-progress__step--completed { background: #FF9800; color: #fff; }
    .phase-progress__step--active { background: #FF9800; color: #fff; box-shadow: 0 0 8px #FF9800; }
    .phase-progress__step--upcoming { background: #333; color: #B0B0B0; }
    .phase-progress__label { font-weight: 600; }
  `],
})
export class PhaseProgressComponent {
  static readonly PHASES = ['Beginner', 'Intermediate', 'Elite', 'Master'];
  readonly PHASES = PhaseProgressComponent.PHASES;

  currentPhase = input.required<string>();

  currentStepIndex = computed(() => {
    const idx = PhaseProgressComponent.PHASES.indexOf(this.currentPhase());
    return idx >= 0 ? idx : 0;
  });

  completedCount = computed(() => this.currentStepIndex());

  ariaLabel = computed(() =>
    `Phase progress: currently in ${this.currentPhase()}, ${this.completedCount()} of 4 phases completed`,
  );
}
