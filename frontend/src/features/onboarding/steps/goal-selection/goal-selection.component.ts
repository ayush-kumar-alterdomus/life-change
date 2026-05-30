import { Component, inject } from '@angular/core';
import { IonIcon } from '@ionic/angular/standalone';
import { ToastController } from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import {
  barbellOutline,
  briefcaseOutline,
  leafOutline,
  peopleOutline,
  walletOutline,
  bookOutline,
} from 'ionicons/icons';

import { HapticService } from '@core/services/haptic.service';
import { GlowCardDirective } from '@shared/directives';
import { OnboardingService } from '../../services/onboarding.service';
import { OnboardingStore } from '../../services/onboarding.store';
import { GOAL_CATEGORIES, ONBOARDING_CONSTANTS } from '../../constants';
import { GoalCategory } from '../../models';

@Component({
  standalone: true,
  selector: 'app-goal-selection',
  templateUrl: './goal-selection.component.html',
  styleUrls: ['./goal-selection.component.scss'],
  imports: [IonIcon, GlowCardDirective],
})
export class GoalSelectionComponent {
  private readonly onboardingService = inject(OnboardingService);
  private readonly store = inject(OnboardingStore);
  private readonly hapticService = inject(HapticService);
  private readonly toastCtrl = inject(ToastController);

  readonly goals: GoalCategory[] = GOAL_CATEGORIES;

  constructor() {
    addIcons({
      'barbell-outline': barbellOutline,
      'briefcase-outline': briefcaseOutline,
      'leaf-outline': leafOutline,
      'people-outline': peopleOutline,
      'wallet-outline': walletOutline,
      'book-outline': bookOutline,
    });
  }

  isSelected(goalId: string): boolean {
    return this.store.selectedGoals().includes(goalId);
  }

  async toggleGoal(goal: GoalCategory): Promise<void> {
    const current = this.store.selectedGoals();

    this.hapticService.impact('light');

    if (current.includes(goal.id)) {
      // Deselect
      const updated = current.filter((id) => id !== goal.id);
      this.onboardingService.setGoals(updated);
    } else {
      // Check max limit
      if (current.length >= ONBOARDING_CONSTANTS.MAX_GOALS) {
        const toast = await this.toastCtrl.create({
          message: 'Maximum 3 goals allowed',
          duration: 2000,
          position: 'bottom',
          color: 'warning',
        });
        await toast.present();
        return;
      }

      // Select
      const updated = [...current, goal.id];
      this.onboardingService.setGoals(updated);
    }
  }
}
