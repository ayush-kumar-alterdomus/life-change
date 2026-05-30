import {
  Component,
  ChangeDetectionStrategy,
  AfterViewInit,
  ViewChild,
  ElementRef,
  input,
  output,
} from '@angular/core';
import { CommonModule } from '@angular/common';

import { PerfectDayStats } from '../../models/quest-completion.models';
import { scaleInAnimation, fadeOutAnimation } from '../../animations/perfect-day.animations';

@Component({
  standalone: true,
  selector: 'app-perfect-day-overlay',
  templateUrl: './perfect-day-overlay.component.html',
  styleUrls: ['./perfect-day-overlay.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
  animations: [scaleInAnimation, fadeOutAnimation],
  host: {
    'role': 'dialog',
    'aria-label': 'Perfect Day achievement',
    'aria-modal': 'true',
  },
})
export class PerfectDayOverlayComponent implements AfterViewInit {
  /** Stats to display in the overlay (quests completed, XP earned, streak) */
  stats = input.required<PerfectDayStats>();

  /** Preloaded Lottie confetti animation data */
  confettiData = input<unknown>(null);

  /** Emitted when the user dismisses the overlay */
  dismiss = output<void>();

  @ViewChild('continueBtn') continueBtn!: ElementRef<HTMLButtonElement>;

  ngAfterViewInit(): void {
    // Move focus to continue button for accessibility (Requirement 8.6)
    setTimeout(() => this.continueBtn.nativeElement.focus(), 100);
  }

  onDismiss(): void {
    this.dismiss.emit();
  }
}
