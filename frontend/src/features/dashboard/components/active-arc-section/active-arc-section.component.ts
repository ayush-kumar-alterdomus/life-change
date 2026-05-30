import { Component, ChangeDetectionStrategy, input, output, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ArcCardComponent } from '../../../../shared/ui';
import { DashboardActiveArc } from '../../models';

@Component({
  standalone: true,
  selector: 'app-active-arc-section',
  templateUrl: './active-arc-section.component.html',
  styleUrls: ['./active-arc-section.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, ArcCardComponent],
})
export class ActiveArcSectionComponent {
  /** The user's currently active arc, or null if none */
  activeArc = input.required<DashboardActiveArc | null>();

  /** Emitted when the user taps the active arc card to navigate to arc detail */
  navigateToArc = output<void>();

  /** Emitted when the user taps the CTA to start their first arc */
  navigateToArcSelection = output<void>();

  /** Computed progress label (e.g., "43% Complete") */
  progressLabel = computed(() => {
    const arc = this.activeArc();
    return arc ? `${arc.progressPercentage}% Complete` : '';
  });
}
