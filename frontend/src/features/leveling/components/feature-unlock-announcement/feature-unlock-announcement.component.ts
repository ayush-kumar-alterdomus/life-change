import { Component, Input } from '@angular/core';
import { IonIcon } from '@ionic/angular/standalone';
import { MilestoneConfig } from '../../models/level-up.models';
import { scaleInAnimation } from '../celebration-overlay/celebration-overlay.animations';

@Component({
  standalone: true,
  selector: 'app-feature-unlock-announcement',
  imports: [IonIcon],
  template: `
    <div class="feature-unlock" @scaleIn aria-live="polite">
      <div class="feature-unlock__icon">
        <ion-icon [name]="milestoneConfig.icon"></ion-icon>
      </div>
      <h3 class="feature-unlock__name">{{ milestoneConfig.featureName }}</h3>
      <p class="feature-unlock__tagline">{{ milestoneConfig.tagline }}</p>
    </div>
  `,
  styles: [
    `
      .feature-unlock {
        text-align: center;
        padding: 24px;
      }
      .feature-unlock__icon {
        font-size: 3rem;
        color: #a855f7;
        filter: drop-shadow(0 0 12px #a855f7);
        margin-bottom: 12px;
      }
      .feature-unlock__name {
        color: #fff;
        font-size: 1.4rem;
        margin: 0 0 8px;
      }
      .feature-unlock__tagline {
        color: #aaa;
        font-size: 0.9rem;
        margin: 0;
      }
    `,
  ],
  animations: [scaleInAnimation],
})
export class FeatureUnlockAnnouncementComponent {
  @Input({ required: true }) milestoneConfig!: MilestoneConfig;
}
