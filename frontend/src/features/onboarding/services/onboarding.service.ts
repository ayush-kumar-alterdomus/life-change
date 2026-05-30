import { Injectable, inject } from '@angular/core';
import { NavController } from '@ionic/angular/standalone';
import { Observable, tap } from 'rxjs';

import { StorageService } from '@core/services/storage.service';
import { HapticService } from '@core/services/haptic.service';
import { OnboardingPayload, QuizAnswer } from '../models';
import { ONBOARDING_CONSTANTS, QUIZ_QUESTIONS } from '../constants';
import { computePersonalityType, validateOnboardingState } from '../utils';
import { OnboardingStore } from './onboarding.store';
import { OnboardingApiService } from './onboarding-api.service';

const STORAGE_KEY = 'onboarding_state';
const COMPLETE_KEY = 'onboarding_complete';

@Injectable({ providedIn: 'root' })
export class OnboardingService {
  private readonly store = inject(OnboardingStore);
  private readonly storage = inject(StorageService);
  private readonly api = inject(OnboardingApiService);
  private readonly haptic = inject(HapticService);
  private readonly navCtrl = inject(NavController);

  /** Initializes onboarding: restores persisted state or starts fresh. */
  async initialize(): Promise<void> {
    const raw = await this.storage.get<unknown>(STORAGE_KEY);

    if (raw !== null) {
      const validated = validateOnboardingState(raw);
      if (validated) {
        this.store.restore(validated);
        return;
      }
    }

    this.store.reset();
  }

  /** Advances to the next step and persists state. */
  advanceStep(): void {
    if (this.store.currentStep() >= ONBOARDING_CONSTANTS.MAX_STEP) {
      return;
    }

    this.store.setCurrentStep(this.store.currentStep() + 1);
    this.persistState();
  }

  /** Goes back to the previous step and persists state. */
  goBack(): void {
    if (this.store.currentStep() <= ONBOARDING_CONSTANTS.MIN_STEP) {
      return;
    }

    this.store.setCurrentStep(this.store.currentStep() - 1);
    this.persistState();
  }

  /** Updates selected goals (max 3) and persists. */
  setGoals(goals: string[]): void {
    const bounded = goals.slice(0, ONBOARDING_CONSTANTS.MAX_GOALS);
    this.store.setGoals(bounded);
    this.persistState();
  }

  /** Updates selected difficulty tier and persists. */
  setDifficulty(tier: string): void {
    this.store.setDifficulty(tier);
    this.persistState();
  }

  /** Adds a quiz answer, computes personality type if final answer, persists. */
  addQuizAnswer(answer: QuizAnswer): void {
    const current = this.store.quizAnswers();
    const existingIndex = current.findIndex((a) => a.questionId === answer.questionId);

    let updated: QuizAnswer[];
    if (existingIndex >= 0) {
      updated = [...current];
      updated[existingIndex] = answer;
    } else {
      updated = [...current, answer];
    }

    this.store.setQuizAnswers(updated);

    if (updated.length === ONBOARDING_CONSTANTS.QUIZ_QUESTION_COUNT) {
      const personalityType = computePersonalityType(updated, QUIZ_QUESTIONS);
      this.store.setPersonalityType(personalityType);
    }

    this.persistState();
  }

  /** Sets the selected Arc and persists. */
  setArc(arcId: string): void {
    this.store.setArc(arcId);
    this.persistState();
  }

  /** Sets the selected avatar and persists. */
  setAvatar(avatarId: string): void {
    this.store.setAvatar(avatarId);
    this.persistState();
  }

  /** Submits onboarding payload to backend, handles success flow. */
  completeOnboarding(): Observable<void> {
    const snapshot = this.store.getSnapshot();

    const payload: OnboardingPayload = {
      selectedGoals: snapshot.selectedGoals,
      difficulty: snapshot.selectedDifficulty!,
      personalityType: snapshot.personalityType!,
      selectedArc: snapshot.selectedArc!,
      selectedAvatar: snapshot.selectedAvatar!,
    };

    return this.api.submitOnboarding(payload).pipe(
      tap(() => {
        this.storage.set(COMPLETE_KEY, true);
        this.storage.remove(STORAGE_KEY);
        this.haptic.impact('medium');
        this.navCtrl.navigateRoot('/tabs/home');
        this.store.reset();
      }),
    );
  }

  /** Resets store and clears persisted state. */
  reset(): void {
    this.store.reset();
    this.storage.remove(STORAGE_KEY);
  }

  /** Persists current store snapshot to Capacitor Preferences. */
  private persistState(): void {
    const snapshot = this.store.getSnapshot();
    this.storage.set(STORAGE_KEY, snapshot);
  }
}
