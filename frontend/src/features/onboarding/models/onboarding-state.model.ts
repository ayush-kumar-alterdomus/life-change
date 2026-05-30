import { QuizAnswer } from './quiz.model';

export interface OnboardingState {
  readonly currentStep: number;
  readonly selectedGoals: string[];
  readonly selectedDifficulty: string | null;
  readonly quizAnswers: QuizAnswer[];
  readonly personalityType: string | null;
  readonly selectedArc: string | null;
  readonly selectedAvatar: string | null;
}
