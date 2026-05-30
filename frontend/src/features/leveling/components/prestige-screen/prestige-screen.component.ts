import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { LevelUpService } from '../../services/level-up.service';
import { fadeInAnimation } from '../celebration-overlay/celebration-overlay.animations';

@Component({
  standalone: true,
  selector: 'app-prestige-screen',
  animations: [fadeInAnimation],
  template: `
    <div class="prestige" @fadeIn>
      @if (prestigeData(); as data) {
        <h2 class="prestige__title">Prestige {{ data.newPrestigeLevel }}</h2>
        <p class="prestige__badge">{{ data.badgeName }}</p>
        <div class="prestige__summary">
          <p>Level reset to 1</p>
          <p>Prestige Badge Earned</p>
        </div>
        <button class="prestige__button" (click)="onBeginNewJourney()">Begin New Journey</button>
      }
    </div>
  `,
  styles: [
    `
      .prestige {
        text-align: center;
        padding: 32px;
      }
      .prestige__title {
        font-family: 'Orbitron', sans-serif;
        font-size: 2.5rem;
        margin: 0 0 16px;
        background: linear-gradient(135deg, #ffd700, #ff9800);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
      }
      .prestige__badge {
        color: #a855f7;
        font-size: 1.2rem;
        margin: 0 0 24px;
      }
      .prestige__summary {
        color: #ccc;
        margin-bottom: 32px;
      }
      .prestige__summary p {
        margin: 4px 0;
      }
      .prestige__button {
        background: #4caf50;
        color: #fff;
        border: none;
        border-radius: 12px;
        padding: 14px 32px;
        font-size: 1rem;
        font-weight: 600;
        cursor: pointer;
      }
    `,
  ],
})
export class PrestigeScreenComponent {
  private readonly levelUpService = inject(LevelUpService);
  private readonly router = inject(Router);

  readonly prestigeData = this.levelUpService.prestigeData;

  onBeginNewJourney(): void {
    this.levelUpService.dismiss();
    this.router.navigate(['/dashboard']);
  }
}
