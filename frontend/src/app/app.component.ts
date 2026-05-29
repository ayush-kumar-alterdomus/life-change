import { Component } from '@angular/core';
import { IonApp, IonRouterOutlet } from '@ionic/angular/standalone';
import { OfflineBannerComponent } from '../shared/components/offline-banner/offline-banner.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [IonApp, IonRouterOutlet, OfflineBannerComponent],
  template: `
    <ion-app>
      <app-offline-banner />
      <ion-router-outlet></ion-router-outlet>
    </ion-app>
  `,
  styles: [],
})
export class AppComponent {
  title = 'Ascend';
}
