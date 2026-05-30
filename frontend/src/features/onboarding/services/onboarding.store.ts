import { Injectable, Signal, computed, signal } from '@angular/core';
import { OnboardingState, QuizAnswer } from '../models';
import { ONBOARDING_CONSTANTS } from '../constants';

@Injectable({ providedIn: 'root' })
export class OnboardingStore {
  // --- Writable Signals (internal) ---
  private readonly _currentStep = signal<number>(0);
  private readonly _selectedGoals = signal<string[]>([]);
  private readonly _selectedDifficulty = signal<string | null>(null);
  private readonly _quizAnswers = signal<QuizAnswer[]>([]);
  private readonly _personalityType = signal<string | null>(null);
  private readonly _selectedArc = signal<string | null>(null);
  private readonly _selectedAvatar = signal<string | null>(null);

  // --- Public Readonly Signals ---
  readonly currentStep: Signal<number> = this._currentStep.asReadonly();
  readonly selectedGoals: Signal<string[]> = this._selectedGoals.asReadonly();
  readonly selectedDifficulty: Signal<string | null> = this._selectedDifficulty.asReadonly();
  readonly quizAnswers: Signal<QuizAnswer[]> = this._quizAnswers.asReadonly();
  readonly personalityType: Signal<string | null> = this._personalityType.asReadonly();
  readonly selectedArc: Signal<string | null> = this._selectedArc.asReadonly();
  readonly selectedAvatar: Signal<string | null> = this._selectedAvatar.asReadonly();

  // --- Computed Signals ---
  readonly isStepValid: Signal<boolean> = computed(() => {
    switch (this._currentStep()) {
      case 0: return this._selectedGoals().length >= 1 && this._selectedGoals().length <= 3;
      case 1: return this._selectedDifficulty() !== null;
      case 2: return this._quizAnswers().length === ONBOARDING_CONSTANTS.QUIZ_QUESTION_COUNT;
      case 3: return this._selectedArc() !== null;
      case 4: return this._selectedAvatar() !== null;
      default: return false;
    }
  });

  // --- Mutators ---
  setCurrentStep(step: number): void { this._currentStep.set(step); }
  setGoals(goals: string[]): void { this._selectedGoals.set(goals); }
  setDifficulty(tier: string | null): void { this._selectedDifficulty.set(tier); }
  setQuizAnswers(answers: QuizAnswer[]): void { this._quizAnswers.set(answers); }
  setPersonalityType(type: string | null): void { this._personalityType.set(type); }
  setArc(arcId: string | null): void { this._selectedArc.set(arcId); }
  setAvatar(avatarId: string | null): void { this._selectedAvatar.set(avatarId); }

  /** Returns a plain object snapshot for serialization. */
  getSnapshot(): OnboardingState {
    return {
      currentStep: this._currentStep(),
      selectedGoals: this._selectedGoals(),
      selectedDifficulty: this._selectedDifficulty(),
      quizAnswers: this._quizAnswers(),
      personalityType: this._personalityType(),
      selectedArc: this._selectedArc(),
      selectedAvatar: this._selectedAvatar(),
    };
  }

  /** Restores state from a validated snapshot. */
  restore(state: OnboardingState): void {
    this._currentStep.set(state.currentStep);
    this._selectedGoals.set(state.selectedGoals);
    this._selectedDifficulty.set(state.selectedDifficulty);
    this._quizAnswers.set(state.quizAnswers);
    this._personalityType.set(state.personalityType);
    this._selectedArc.set(state.selectedArc);
    this._selectedAvatar.set(state.selectedAvatar);
  }

  /** Resets all signals to initial state. */
  reset(): void {
    this._currentStep.set(0);
    this._selectedGoals.set([]);
    this._selectedDifficulty.set(null);
    this._quizAnswers.set([]);
    this._personalityType.set(null);
    this._selectedArc.set(null);
    this._selectedAvatar.set(null);
  }
}
