import { Component, inject, signal, computed } from '@angular/core';
import { IonButton, IonSpinner, ToastController } from '@ionic/angular/standalone';
import { GlowCardDirective } from '@shared/directives';
import { HapticService } from '@core/services/haptic.service';
import { OnboardingService } from '../../services/onboarding.service';
import { OnboardingStore } from '../../services/onboarding.store';
import { AVATAR_OPTIONS } from '../../constants';
import { AvatarOption } from '../../models';

@Component({
  standalone: true,
  selector: 'app-avatar-selection',
  templateUrl: './avatar-selection.component.html',
  styleUrls: ['./avatar-selection.component.scss'],
  imports: [IonButton, IonSpinner, GlowCardDirective],
})
export class AvatarSelectionComponent {
  private readonly onboardingService = inject(OnboardingService);
  protected readonly store = inject(OnboardingStore);
  private readonly hapticService = inject(HapticService);
  private readonly toastCtrl = inject(ToastController);

  readonly avatars: AvatarOption[] = AVATAR_OPTIONS;
  readonly isSubmitting = signal<boolean>(false);
  readonly errorMessage = signal<string | null>(null);

  readonly selectedAvatar = computed<AvatarOption | null>(() => {
    const id = this.store.selectedAvatar();
    return this.avatars.find((a) => a.id === id) ?? null;
  });

  readonly canComplete = computed<boolean>(() => {
    return this.store.selectedAvatar() !== null && !this.isSubmitting();
  });

  isSelected(avatarId: string): boolean {
    return this.store.selectedAvatar() === avatarId;
  }

  selectAvatar(avatar: AvatarOption): void {
    this.onboardingService.setAvatar(avatar.id);
    this.hapticService.impact('light');
    this.errorMessage.set(null);
  }

  complete(): void {
    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    this.onboardingService.completeOnboarding().subscribe({
      next: async () => {
        const toast = await this.toastCtrl.create({
          message: 'Welcome to Ascend! Your Arc begins now.',
          duration: 3000,
          position: 'bottom',
          color: 'success',
        });
        await toast.present();
        this.isSubmitting.set(false);
      },
      error: () => {
        this.isSubmitting.set(false);
        this.errorMessage.set('Could not save your profile. Please try again.');
      },
    });
  }
}
