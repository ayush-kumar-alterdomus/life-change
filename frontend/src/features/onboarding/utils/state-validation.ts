import { OnboardingState } from '../models';

/**
 * Validates a deserialized onboarding state structure.
 *
 * Returns the validated OnboardingState object if all checks pass,
 * or null if the input is invalid or corrupted.
 *
 * Validation rules:
 * - raw must be a non-null object
 * - currentStep must be a number between 0 and 4 inclusive
 * - selectedGoals must be an array of 0–3 strings
 * - selectedDifficulty must be a string or null
 * - quizAnswers must be an array where each element has questionId (string) and selectedOptionId (string)
 * - personalityType must be a string or null
 * - selectedArc must be a string or null
 * - selectedAvatar must be a string or null
 */
export function validateOnboardingState(raw: unknown): OnboardingState | null {
  if (raw === null || raw === undefined || typeof raw !== 'object') {
    return null;
  }

  const obj = raw as Record<string, unknown>;

  // Validate currentStep: number between 0 and 4 inclusive
  if (
    typeof obj['currentStep'] !== 'number' ||
    !Number.isInteger(obj['currentStep']) ||
    obj['currentStep'] < 0 ||
    obj['currentStep'] > 4
  ) {
    return null;
  }

  // Validate selectedGoals: array of 0–3 strings
  if (!Array.isArray(obj['selectedGoals'])) {
    return null;
  }
  if (obj['selectedGoals'].length > 3) {
    return null;
  }
  for (const goal of obj['selectedGoals']) {
    if (typeof goal !== 'string') {
      return null;
    }
  }

  // Validate selectedDifficulty: string or null
  if (obj['selectedDifficulty'] !== null && typeof obj['selectedDifficulty'] !== 'string') {
    return null;
  }

  // Validate quizAnswers: array of objects with questionId (string) and selectedOptionId (string)
  if (!Array.isArray(obj['quizAnswers'])) {
    return null;
  }
  for (const answer of obj['quizAnswers']) {
    if (answer === null || typeof answer !== 'object') {
      return null;
    }
    const answerObj = answer as Record<string, unknown>;
    if (typeof answerObj['questionId'] !== 'string') {
      return null;
    }
    if (typeof answerObj['selectedOptionId'] !== 'string') {
      return null;
    }
  }

  // Validate personalityType: string or null
  if (obj['personalityType'] !== null && typeof obj['personalityType'] !== 'string') {
    return null;
  }

  // Validate selectedArc: string or null
  if (obj['selectedArc'] !== null && typeof obj['selectedArc'] !== 'string') {
    return null;
  }

  // Validate selectedAvatar: string or null
  if (obj['selectedAvatar'] !== null && typeof obj['selectedAvatar'] !== 'string') {
    return null;
  }

  // All checks passed — return the validated state
  return {
    currentStep: obj['currentStep'] as number,
    selectedGoals: obj['selectedGoals'] as string[],
    selectedDifficulty: obj['selectedDifficulty'] as string | null,
    quizAnswers: obj['quizAnswers'] as OnboardingState['quizAnswers'],
    personalityType: obj['personalityType'] as string | null,
    selectedArc: obj['selectedArc'] as string | null,
    selectedAvatar: obj['selectedAvatar'] as string | null,
  };
}
