import { Component, ChangeDetectionStrategy, inject, computed } from '@angular/core';
import { QuestCompletionService } from '../../services/quest-completion.service';
import { ConfirmationSheetComponent } from '../confirmation-sheet/confirmation-sheet.component';
import { RewardAnimationLayerComponent } from '../reward-animation-layer/reward-animation-layer.component';
import { PerfectDayOverlayComponent } from '../perfect-day-overlay/perfect-day-overlay.component';

/**
 * Host component that listens to the QuestCompletionService flowState signal
 * and conditionally renders the appropriate overlay component based on the
 * current status of the quest completion flow.
 *
 * This component is placed at the app level (after router-outlet) so that
 * overlays render above all routed content.
 *
 * Requirements: 1.3, 1.4
 */
@Component({
  standalone: true,
  selector: 'app-quest-completion-host',
  template: `
    @if (flowState().status === 'confirming' || flowState().status === 'submitting') {
      <app-confirmation-sheet
        [quest]="flowState().quest!"
        [isSubmitting]="flowState().status === 'submitting'"
        [errorMessage]="flowState().error"
        (confirm)="service.onConfirm()"
        (cancel)="service.onCancel()"
      />
    }

    @if (flowState().status === 'animating') {
      <app-reward-animation-layer
        [xpEarned]="lastXpEarned()"
        [animationData]="null"
        (animationComplete)="service.onAnimationComplete()"
      />
    }

    @if (flowState().status === 'perfect-day') {
      <app-perfect-day-overlay
        [stats]="perfectDayStats()"
        [confettiData]="null"
        (dismiss)="service.onPerfectDayDismiss()"
      />
    }
  `,
  imports: [ConfirmationSheetComponent, RewardAnimationLayerComponent, PerfectDayOverlayComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class QuestCompletionHostComponent {
  readonly service = inject(QuestCompletionService);
  readonly flowState = this.service.flowState;

  /** XP earned from the current quest (uses quest's xpReward as the display value) */
  readonly lastXpEarned = computed(() => this.flowState().quest?.xpReward ?? 0);

  /** Perfect Day stats computed from the service */
  readonly perfectDayStats = computed(() => this.service.getPerfectDayStats());
}
