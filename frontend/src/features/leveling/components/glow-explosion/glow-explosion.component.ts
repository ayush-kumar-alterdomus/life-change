import { Component, inject, computed, Output, EventEmitter } from '@angular/core';
import { LevelUpService } from '../../services/level-up.service';

@Component({
  standalone: true,
  selector: 'app-glow-explosion',
  template: `<div class="glow-explosion" aria-hidden="true"></div>`,
  styles: [
    `
      .glow-explosion {
        width: 100%;
        height: 100%;
        position: absolute;
        inset: 0;
      }
    `,
  ],
})
export class GlowExplosionComponent {
  private readonly levelUpService = inject(LevelUpService);
  readonly animationData = computed(() => this.levelUpService.glowExplosionData);

  @Output() glowProgress = new EventEmitter<number>();
  @Output() glowComplete = new EventEmitter<void>();

  constructor() {
    // Simulate glow progress timeline
    setTimeout(() => this.glowProgress.emit(0.5), 750);
    setTimeout(() => this.glowProgress.emit(0.75), 1125);
    setTimeout(() => {
      this.glowProgress.emit(1);
      this.glowComplete.emit();
    }, 1500);
  }
}
