import { Component, ChangeDetectionStrategy, computed, input } from '@angular/core';
import { ArcType } from '@shared/enums';

@Component({
  standalone: true,
  selector: 'app-cinematic-banner',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="banner" [attr.aria-label]="ariaLabel()" [style.transform]="parallaxTransform()">
      <h1 class="banner__name">{{ arcName() }}</h1>
      <p class="banner__phase">{{ currentPhase() }} Phase</p>
      <p class="banner__progress">{{ progressPercentage() }}% Complete</p>
    </div>
  `,
  styles: [
    `
      .banner {
        width: 100%;
        min-height: 240px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 32px 16px;
        background: linear-gradient(135deg, #ff9800, #a855f7);
        border-radius: 0 0 24px 24px;
      }
      .banner__name {
        font-family: 'Orbitron', sans-serif;
        font-size: 1.8rem;
        color: #fff;
        margin: 0 0 8px;
      }
      .banner__phase {
        color: rgba(255, 255, 255, 0.8);
        font-size: 1rem;
        margin: 0 0 4px;
      }
      .banner__progress {
        color: rgba(255, 255, 255, 0.7);
        font-size: 0.9rem;
        margin: 0;
      }
    `,
  ],
})
export class CinematicBannerComponent {
  arcName = input.required<string>();
  arcType = input.required<ArcType>();
  currentPhase = input.required<string>();
  progressPercentage = input.required<number>();
  scrollOffset = input<number>(0);

  ariaLabel = computed(
    () => `${this.arcName()}, ${this.currentPhase()} phase, ${this.progressPercentage()}% complete`,
  );

  parallaxTransform = computed(() => `translateY(${this.scrollOffset() * 0.5}px)`);
}
