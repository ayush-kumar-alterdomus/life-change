import { Component, ChangeDetectionStrategy, computed, inject, signal } from '@angular/core';
import {
  IonContent,
  IonButton,
  IonIcon,
  IonRouterLink,
  ToastController,
} from '@ionic/angular/standalone';
import { NavController } from '@ionic/angular';
import { RouterLink } from '@angular/router';
import { addIcons } from 'ionicons';
import { logoGoogle, logoApple, mailOutline } from 'ionicons/icons';
import { getAuthPlatform } from '../../utils/platform-detection';
import { AuthService } from '../../../../core/services/auth.service';
import { UserStore } from '../../../../core/services/user-store.service';
import { HapticService } from '../../../../core/services/haptic.service';
import { AuthError, toAuthError } from '../../../../core/models/auth-error.model';

@Component({
  standalone: true,
  selector: 'app-welcome',
  templateUrl: './welcome.component.html',
  styleUrls: ['./welcome.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonContent, IonButton, IonIcon, IonRouterLink, RouterLink],
})
export class WelcomeComponent {
  private readonly authService = inject(AuthService);
  private readonly userStore = inject(UserStore);
  private readonly navCtrl = inject(NavController);
  private readonly toastCtrl = inject(ToastController);
  private readonly hapticService = inject(HapticService);

  /** Current platform detected at construction time */
  private readonly platform = signal(getAuthPlatform());

  /** Whether to show the Apple sign-in button (iOS and web only) */
  readonly showAppleButton = computed(() => {
    const p = this.platform();
    return p === 'ios' || p === 'web';
  });

  /** Loading state during sign-in */
  readonly loading = signal(false);

  constructor() {
    addIcons({ logoGoogle, logoApple, mailOutline });
  }

  /** Handles "Continue with Google" tap */
  async onGoogleSignIn(): Promise<void> {
    this.loading.set(true);
    try {
      await this.authService.loginWithGoogle();
      await this.hapticService.impact('light');
      this.navigateAfterSocialAuth();
    } catch (error: unknown) {
      this.handleSignInError(error, 'Sign-in failed. Please try again.');
    } finally {
      this.loading.set(false);
    }
  }

  /** Handles "Continue with Apple" tap */
  async onAppleSignIn(): Promise<void> {
    this.loading.set(true);
    try {
      await this.authService.loginWithApple();
      await this.hapticService.impact('light');
      this.navigateAfterSocialAuth();
    } catch (error: unknown) {
      this.handleSignInError(error, 'Sign-in failed. Please try again.');
    } finally {
      this.loading.set(false);
    }
  }

  /** Handles "Continue as Guest" tap */
  async onGuestSignIn(): Promise<void> {
    this.loading.set(true);
    try {
      await this.authService.loginAsGuest();
      await this.hapticService.impact('light');
      this.navCtrl.navigateRoot('/onboarding');
    } catch (error: unknown) {
      this.handleSignInError(error, 'Could not start guest session. Please try again.');
    } finally {
      this.loading.set(false);
    }
  }

  /**
   * Navigates after a successful social sign-in (Google/Apple).
   * Checks UserStore to determine if user has completed onboarding.
   */
  private navigateAfterSocialAuth(): void {
    const user = this.userStore.user();
    if (user?.onboardingComplete) {
      this.navCtrl.navigateRoot('/tabs/home');
    } else {
      this.navCtrl.navigateRoot('/onboarding');
    }
  }

  /**
   * Handles sign-in errors. If the error message is empty (user cancellation),
   * remains on the welcome screen silently. Otherwise, shows an error toast.
   */
  private handleSignInError(error: unknown, fallbackMessage: string): void {
    const authError = toAuthError(error);
    if (authError.message === '') return;
    this.showErrorToast(authError.message || fallbackMessage);
  }

  /** Displays an error toast notification */
  private async showErrorToast(message: string): Promise<void> {
    const toast = await this.toastCtrl.create({
      message,
      duration: 3000,
      position: 'bottom',
      color: 'danger',
    });
    await toast.present();
  }
}
