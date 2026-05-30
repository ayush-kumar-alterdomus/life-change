import { Component, inject } from '@angular/core';
import { IonIcon } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { flame } from 'ionicons/icons';

import { GlowCardDirective } from '@shared/directives';
import { HapticService } from '@core/services/haptic.service';
import { OnboardingService } from '../../services/onboarding.service';
import { OnboardingStore } from '../../services/onboarding.store';
import { DIFFICULTY_TIERS } from '../../constants';
import { DifficultyTier } from '../../models';

@Component({
  standalone: true,
  selector: 'app-difficulty-selection',
  templateUrl: './difficulty-selection.step.html',
  styleUrls: ['./difficulty-selection.step.scss'],
  imports: [IonIcon, GlowCardDirective],
})
export class DifficultySelectionComponent {
  private readonly onboardingService = inject(OnboardingService);
  private readonly hapticService = inject(HapticService);
  protected readonly store = inject(OnboardingStore);

  protected readonly tiers: DifficultyTier[] = DIFFICULTY_TIERS;

  constructor() {
    addIcons({ flame });
  }

  protected isSelected(tierId: string): boolean {
    return this.store.selectedDifficulty() === tierId;
  }

  protected selectTier(tier: DifficultyTier): void {
    this.onboardingService.setDifficulty(tier.id);
    this.hapticService.impact('light');
  }

  protected getFlameArray(count: number): number[] {
    return Array.from({ length: count }, (_, i) => i);
  }
}
