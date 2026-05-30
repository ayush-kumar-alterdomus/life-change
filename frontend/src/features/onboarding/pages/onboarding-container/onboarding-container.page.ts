import { Component, inject, signal, OnInit } from '@angular/core';
import {
  IonContent,
  IonHeader,
  IonToolbar,
  IonButton,
  IonButtons,
  IonIcon,
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { arrowBack } from 'ionicons/icons';

import { StepperComponent } from '@shared/components';
import { OnboardingService } from '../../services/onboarding.service';
import { OnboardingStore } from '../../services/onboarding.store';
import { GoalSelectionStep } from '../../steps/goal-selection/goal-selection.step';
import { DifficultySelectionStep } from '../../steps/difficulty-selection/difficulty-selection.step';
import { QuizStep } from '../../steps/quiz/quiz.step';
import { ArcRecommendationStep } from '../../steps/arc-recommendation/arc-recommendation.step';
import { AvatarSelectionStep } from '../../steps/avatar-selection/avatar-selection.step';
import { ONBOARDING_CONSTANTS } from '../../constants';

@Component({
  standalone: true,
  selector: 'app-onboarding',
  templateUrl: './onboarding-container.page.html',
  styleUrls: ['./onboarding-container.page.scss'],
  imports: [
    IonContent,
    IonHeader,
    IonToolbar,
    IonButton,
    IonButtons,
    IonIcon,
    StepperComponent,
    GoalSelectionStep,
    DifficultySelectionStep,
    QuizStep,
    ArcRecommendationStep,
    AvatarSelectionStep,
  ],
})
export class OnboardingContainerComponent implements OnInit {
  protected readonly onboardingService = inject(OnboardingService);
  protected readonly store = inject(OnboardingStore);

  /** Direction of slide transition: 'left' for advance, 'right' for back */
  protected slideDirection = signal<'left' | 'right' | 'none'>('none');

  /** Tracks whether a transition animation is active */
  protected isAnimating = signal(false);

  readonly totalSteps = ONBOARDING_CONSTANTS.TOTAL_STEPS;

  constructor() {
    addIcons({ 'arrow-back': arrowBack });
  }

  async ngOnInit(): Promise<void> {
    await this.onboardingService.initialize();
  }

  /** Whether the back button should be visible (hidden on step 0) */
  get showBackButton(): boolean {
    return this.store.currentStep() > 0;
  }

  /** Whether the Continue button should be visible (hidden on quiz step 2 and avatar step 4) */
  get showContinueButton(): boolean {
    const step = this.store.currentStep();
    return step === 0 || step === 1 || step === 3;
  }

  /** Advance to the next step with slide-left animation */
  onContinue(): void {
    if (!this.store.isStepValid() || this.isAnimating()) {
      return;
    }

    this.slideDirection.set('left');
    this.isAnimating.set(true);

    setTimeout(() => {
      this.onboardingService.advanceStep();
      this.isAnimating.set(false);
    }, ONBOARDING_CONSTANTS.TRANSITION_DURATION_MS);
  }

  /** Go back to the previous step with slide-right animation */
  onBack(): void {
    if (this.store.currentStep() <= 0 || this.isAnimating()) {
      return;
    }

    this.slideDirection.set('right');
    this.isAnimating.set(true);

    setTimeout(() => {
      this.onboardingService.goBack();
      this.isAnimating.set(false);
    }, ONBOARDING_CONSTANTS.TRANSITION_DURATION_MS);
  }
}
