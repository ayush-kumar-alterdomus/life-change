import {
  Component,
  ChangeDetectionStrategy,
  input,
  output,
  signal,
  computed,
  OnInit,
  OnDestroy,
  NgZone,
  inject,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { XpFillAnimationConfig } from '../../models/quest-completion.models';
import { interpolateXp, formatXpDisplay } from '../../utils/xp-calculations';

/**
 * Wrapper component that orchestrates the XP fill animation sequence
 * on top of the shared app-xp-progress-bar component.
 *
 * Handles:
 * - Smooth fill from previousXp to newXp (600ms ease-out)
 * - Level-up sequence: fill to 100% → pause 200ms → reset to 0% → fill to overflow
 * - Counting-up XP text display synced with bar fill via requestAnimationFrame
 * - will-change: width hint during animation, removed after completion
 *
 * Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 9.3
 */
@Component({
  standalone: true,
  selector: 'app-xp-fill-animation',
  templateUrl: './xp-fill-animation.component.html',
  styleUrls: ['./xp-fill-animation.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
})
export class XpFillAnimationComponent implements OnInit, OnDestroy {
  private readonly ngZone = inject(NgZone);

  /** Animation configuration from calculateXpFillConfig */
  config = input.required<XpFillAnimationConfig>();

  /** Duration of the fill animation in ms (default 600ms per spec) */
  animateDuration = input<number>(600);

  /** Emitted when the entire fill animation sequence completes */
  animationComplete = output<void>();

  /** Current fill percentage for the progress bar (driven by animation) */
  readonly fillPercentage = signal<number>(0);

  /** Whether the animation is currently running (controls will-change) */
  readonly isAnimating = signal<boolean>(false);

  /** The animated XP display text (counting-up effect) */
  readonly displayText = signal<string>('');

  /** The current animated XP value (for the progress bar's currentXp input) */
  readonly animatedCurrentXp = signal<number>(0);

  /** Required XP for the progress bar */
  readonly requiredXp = computed(() => this.config().levelThreshold);

  private animationFrameId: number | null = null;
  private timeoutIds: ReturnType<typeof setTimeout>[] = [];
  private destroyed = false;

  ngOnInit(): void {
    const cfg = this.config();
    // Initialize display to previous state
    this.animatedCurrentXp.set(cfg.previousXp);
    this.fillPercentage.set((cfg.previousXp / cfg.levelThreshold) * 100);
    this.displayText.set(formatXpDisplay(cfg.previousXp, cfg.levelThreshold));

    // Start animation sequence on next frame to allow initial render
    this.scheduleTimeout(() => this.startAnimation(), 16);
  }

  ngOnDestroy(): void {
    this.destroyed = true;
    this.cleanup();
  }

  private startAnimation(): void {
    if (this.destroyed) return;

    const cfg = this.config();
    this.isAnimating.set(true);

    if (cfg.crossesLevel) {
      this.runLevelUpSequence(cfg);
    } else {
      this.runSimpleFill(cfg.previousXp, cfg.newXp, cfg.levelThreshold);
    }
  }

  /**
   * Simple fill: animate from previousXp to newXp within the same level.
   */
  private runSimpleFill(fromXp: number, toXp: number, threshold: number): void {
    const duration = this.animateDuration();
    const endPercentage = (toXp / threshold) * 100;

    // Set target percentage (CSS transition handles the visual animation)
    this.fillPercentage.set(endPercentage);

    // Animate the counting-up text in sync
    this.animateText(fromXp, toXp, threshold, duration, () => {
      this.onSequenceComplete();
    });
  }

  /**
   * Level-up sequence:
   * 1. Fill to 100% (600ms)
   * 2. Pause 200ms
   * 3. Reset to 0% (instant)
   * 4. Fill to overflow amount (600ms)
   */
  private runLevelUpSequence(cfg: XpFillAnimationConfig): void {
    const duration = this.animateDuration();
    const levelUpPause = 200;

    // Phase 1: Fill to 100%
    this.fillPercentage.set(100);
    this.animateText(cfg.previousXp, cfg.levelThreshold, cfg.levelThreshold, duration, () => {
      if (this.destroyed) return;

      // Phase 2: Pause at 100%
      this.scheduleTimeout(() => {
        if (this.destroyed) return;

        // Phase 3: Reset to 0% instantly (disable transition temporarily)
        this.isAnimating.set(false); // Remove will-change and transition
        this.fillPercentage.set(0);
        this.animatedCurrentXp.set(0);
        this.displayText.set(formatXpDisplay(0, cfg.levelThreshold));

        // Re-enable animation on next frame
        this.scheduleTimeout(() => {
          if (this.destroyed) return;
          this.isAnimating.set(true);

          // Phase 4: Fill to overflow amount
          const overflowPercentage = (cfg.overflowXp / cfg.levelThreshold) * 100;
          this.fillPercentage.set(overflowPercentage);
          this.animateText(0, cfg.overflowXp, cfg.levelThreshold, duration, () => {
            this.onSequenceComplete();
          });
        }, 32); // Allow a frame for the reset to render
      }, levelUpPause);
    });
  }

  /**
   * Animate the XP text counting up using requestAnimationFrame.
   * Syncs with the bar fill animation duration.
   */
  private animateText(
    fromXp: number,
    toXp: number,
    threshold: number,
    duration: number,
    onComplete: () => void
  ): void {
    const startTime = performance.now();

    const tick = (now: number): void => {
      if (this.destroyed) return;

      const elapsed = now - startTime;
      const progress = Math.min(elapsed / duration, 1);

      // Use ease-out curve to match CSS transition
      const easedProgress = 1 - Math.pow(1 - progress, 2);

      const currentValue = interpolateXp(fromXp, toXp, easedProgress);
      this.animatedCurrentXp.set(currentValue);
      this.displayText.set(formatXpDisplay(currentValue, threshold));

      if (progress < 1) {
        this.animationFrameId = requestAnimationFrame(tick);
      } else {
        // Ensure final value is exact
        this.animatedCurrentXp.set(toXp);
        this.displayText.set(formatXpDisplay(toXp, threshold));
        onComplete();
      }
    };

    this.ngZone.runOutsideAngular(() => {
      this.animationFrameId = requestAnimationFrame(tick);
    });
  }

  /**
   * Called when the entire animation sequence is done.
   */
  private onSequenceComplete(): void {
    if (this.destroyed) return;
    this.isAnimating.set(false);
    this.animationComplete.emit();
  }

  private scheduleTimeout(fn: () => void, delay: number): void {
    const id = setTimeout(fn, delay);
    this.timeoutIds.push(id);
  }

  private cleanup(): void {
    if (this.animationFrameId !== null) {
      cancelAnimationFrame(this.animationFrameId);
      this.animationFrameId = null;
    }
    for (const id of this.timeoutIds) {
      clearTimeout(id);
    }
    this.timeoutIds = [];
  }
}
