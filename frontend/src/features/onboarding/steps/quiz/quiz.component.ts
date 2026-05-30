import { Component, inject, signal, computed } from '@angular/core';

import { HapticService } from '@core/services/haptic.service';
import { OnboardingService } from '../../services/onboarding.service';
import { OnboardingStore } from '../../services/onboarding.store';
import { QUIZ_QUESTIONS, ONBOARDING_CONSTANTS } from '../../constants';
import { QuizQuestion, QuizOption } from '../../models';

@Component({
  standalone: true,
  selector: 'app-quiz',
  templateUrl: './quiz.component.html',
  styleUrls: ['./quiz.component.scss'],
})
export class QuizComponent {
  private readonly onboardingService = inject(OnboardingService);
  private readonly store = inject(OnboardingStore);
  private readonly hapticService = inject(HapticService);

  readonly questions: QuizQuestion[] = QUIZ_QUESTIONS;
  readonly totalQuestions = QUIZ_QUESTIONS.length;

  /** Current question index (local state). */
  readonly currentQuestionIndex = signal<number>(0);

  /** Currently selected option ID for visual highlight before auto-advance. */
  readonly selectedOptionId = signal<string | null>(null);

  /** Whether auto-advance is in progress (prevents double-tap). */
  readonly isAdvancing = signal<boolean>(false);

  /** The current question based on index. */
  readonly currentQuestion = computed<QuizQuestion>(
    () => this.questions[this.currentQuestionIndex()],
  );

  /** Progress label: "Question X of Y". */
  readonly progressLabel = computed<string>(
    () => `Question ${this.currentQuestionIndex() + 1} of ${this.totalQuestions}`,
  );

  /** Whether the back button should be shown. */
  readonly canGoBack = computed<boolean>(() => this.currentQuestionIndex() > 0);

  /**
   * Returns the previously selected option ID for the current question,
   * used to pre-select when navigating back.
   */
  readonly preSelectedOptionId = computed<string | null>(() => {
    const question = this.currentQuestion();
    const answers = this.store.quizAnswers();
    const existing = answers.find((a) => a.questionId === question.id);
    return existing ? existing.selectedOptionId : null;
  });

  /**
   * Checks if a given option is currently highlighted (either freshly selected
   * or pre-selected from a previous answer).
   */
  isOptionHighlighted(optionId: string): boolean {
    return this.selectedOptionId() === optionId || this.preSelectedOptionId() === optionId;
  }

  /**
   * Handles answer option tap:
   * 1. Highlight selected option
   * 2. Trigger light haptic
   * 3. Store the answer
   * 4. Auto-advance after delay
   */
  selectOption(option: QuizOption): void {
    if (this.isAdvancing()) {
      return;
    }

    const question = this.currentQuestion();

    // Highlight selected option
    this.selectedOptionId.set(option.id);
    this.isAdvancing.set(true);

    // Trigger light haptic
    this.hapticService.impact('light');

    // Store the answer
    this.onboardingService.addQuizAnswer({
      questionId: question.id,
      selectedOptionId: option.id,
    });

    // Auto-advance after delay
    setTimeout(() => {
      if (this.currentQuestionIndex() < this.totalQuestions - 1) {
        // Move to next question
        this.currentQuestionIndex.set(this.currentQuestionIndex() + 1);
        this.selectedOptionId.set(null);
        this.isAdvancing.set(false);
      } else {
        // Final question answered — personality type computed by service
        // Advance to Arc Recommendation step
        this.onboardingService.advanceStep();
        this.isAdvancing.set(false);
      }
    }, ONBOARDING_CONSTANTS.QUIZ_AUTO_ADVANCE_DELAY_MS);
  }

  /** Navigate back to the previous question. */
  goBack(): void {
    if (!this.canGoBack()) {
      return;
    }

    this.currentQuestionIndex.set(this.currentQuestionIndex() - 1);
    this.selectedOptionId.set(null);
    this.isAdvancing.set(false);
  }
}
