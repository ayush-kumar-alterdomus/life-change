import { Component, ChangeDetectionStrategy, input, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ArcPhaseWithMilestones } from '../../models';

@Component({
  standalone: true,
  selector: 'app-milestone-timeline',
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @for (phase of phases(); track phase.name) {
      <div class="timeline-phase">
        <button class="timeline-phase__header"
          (click)="togglePhase(phase.name)"
          [attr.aria-expanded]="isExpanded(phase.name)"
          [attr.aria-controls]="'panel-' + phase.name">
          <span>{{ phase.name }}</span>
          <span class="timeline-phase__summary">{{ getCompletionSummary(phase) }}</span>
        </button>
        @if (isExpanded(phase.name)) {
          <ul class="timeline-phase__panel" [id]="'panel-' + phase.name">
            @for (m of phase.milestones; track m.id) {
              <li class="timeline-milestone" [class.timeline-milestone--completed]="m.completed">
                <span class="timeline-milestone__icon">{{ m.completed ? '✓' : '○' }}</span>
                <span>{{ m.title }}</span>
              </li>
            }
          </ul>
        }
      </div>
    }
  `,
  styles: [`
    .timeline-phase__header {
      width: 100%; display: flex; justify-content: space-between; padding: 12px 16px;
      background: #1a1a1a; border: none; color: #fff; font-size: 0.95rem; cursor: pointer; border-radius: 8px; margin-bottom: 4px;
    }
    .timeline-phase__summary { color: #888; font-size: 0.8rem; }
    .timeline-phase__panel { list-style: none; padding: 0 16px; margin: 0 0 12px; }
    .timeline-milestone { display: flex; gap: 8px; padding: 8px 0; color: #ccc; }
    .timeline-milestone--completed { color: #FF9800; }
    .timeline-milestone__icon { width: 20px; }
  `],
})
export class MilestoneTimelineComponent {
  phases = input.required<ArcPhaseWithMilestones[]>();
  currentPhase = input.required<string>();

  expandedPhases = signal<Set<string>>(new Set());

  constructor() {
    effect(() => {
      this.expandedPhases.set(new Set([this.currentPhase()]));
    });
  }

  togglePhase(phaseName: string): void {
    this.expandedPhases.update((set) => {
      const next = new Set(set);
      next.has(phaseName) ? next.delete(phaseName) : next.add(phaseName);
      return next;
    });
  }

  isExpanded(phaseName: string): boolean {
    return this.expandedPhases().has(phaseName);
  }

  getCompletionSummary(phase: ArcPhaseWithMilestones): string {
    const completed = phase.milestones.filter((m) => m.completed).length;
    return `${completed}/${phase.milestones.length} milestones`;
  }
}
