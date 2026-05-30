import { Component, ChangeDetectionStrategy, OnInit, inject } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { IonContent } from '@ionic/angular/standalone';
import { NavController } from '@ionic/angular/standalone';
import { filter, first } from 'rxjs/operators';
import { firstValueFrom } from 'rxjs';

import { AuthService } from '../../../../core/services/auth.service';
import { UserStore } from '../../../../core/services/user-store.service';

@Component({
  standalone: true,
  selector: 'app-splash',
  templateUrl: './splash.page.html',
  styleUrls: ['./splash.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonContent],
})
export class SplashPage implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly userStore = inject(UserStore);
  private readonly navCtrl = inject(NavController);

  async ngOnInit(): Promise<void> {
    const minDisplayTime = new Promise<void>((resolve) => setTimeout(resolve, 1500));

    // Wait for auth to resolve (AuthService handles the 5s timeout internally)
    await firstValueFrom(
      toObservable(this.authService.authReady).pipe(
        filter((ready) => ready),
        first(),
      ),
    );

    // Ensure minimum 1.5s display time
    await minDisplayTime;

    const user = this.authService.currentUser();
    if (user) {
      const profile = this.userStore.user();
      if (profile?.onboardingComplete) {
        this.navCtrl.navigateRoot('/tabs/home');
      } else {
        this.navCtrl.navigateRoot('/onboarding');
      }
    } else {
      this.navCtrl.navigateRoot('/auth/welcome');
    }
  }
}
