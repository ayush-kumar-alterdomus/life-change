import { Component } from '@angular/core';
import { IonApp, IonRouterOutlet } from '@ionic/angular/standalone';
import { OfflineBannerComponent } from '../shared/components/offline-banner/offline-banner.component';
import { QuestCompletionHostComponent } from '../features/quest-completion/components/quest-completion-host/quest-completion-host.component';
import { CelebrationOverlayComponent } from '../features/leveling/components/celebration-overlay/celebration-overlay.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    IonApp,
    IonRouterOutlet,
    OfflineBannerComponent,
    QuestCompletionHostComponent,
    CelebrationOverlayComponent,
  ],
  template: `
    <ion-app>
      <app-offline-banner />
      <ion-router-outlet></ion-router-outlet>
      <app-quest-completion-host />
      <app-celebration-overlay />
    </ion-app>
  `,
  styles: [],
})
export class AppComponent {
  title = 'Ascend';
}
