import {
  Component,
  ChangeDetectionStrategy,
  OnInit,
  OnDestroy,
  computed,
  input,
  output,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { xpFloatAnimation, glowAnimation } from '../../animations/reward.animations';

@Component({
  standalone: true,
  selector: 'app-reward-animation-layer',
  templateUrl: './reward-animation-layer.component.html',
  styleUrls: ['./reward-animation-layer.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
  animations: [xpFloatAnimation, glowAnimation],
})
export class RewardAnimationLayerComponent implements OnInit, OnDestroy {
  /** XP earned from quest completion */
  xpEarned = input.required<number>();

  /** Lottie animation data for particle burst */
  animationData = input<unknown>(null);

  /** Emits when the full animation sequence completes */
  animationComplete = output<void>();

  /** Aria-live announcement for screen readers */
  xpAnnouncement = computed(() => `Earned ${this.xpEarned()} experience points`);

  private animationTimer: ReturnType<typeof setTimeout> | null = null;

  ngOnInit(): void {
    // Auto-dismiss after 800ms (300ms particle burst + 500ms XP float)
    this.animationTimer = setTimeout(() => {
      this.animationComplete.emit();
    }, 800);
  }

  ngOnDestroy(): void {
    if (this.animationTimer) {
      clearTimeout(this.animationTimer);
    }
  }
}
