import { QuizAnswer, QuizQuestion } from '../models';

/**
 * Fixed priority order for breaking ties in personality type computation.
 * When two or more personality types have the same total score,
 * the type appearing earlier in this array wins.
 */
const PERSONALITY_PRIORITY: readonly string[] = [
  'disciplined',
  'competitive',
  'analytical',
  'creative',
  'collaborative',
  'spontaneous',
] as const;

/**
 * Computes the user's personality type from their quiz answers.
 *
 * Algorithm:
 * 1. For each answered question, find the selected option.
 * 2. Aggregate the weight values from all selected options across all questions.
 * 3. The personality type with the highest total score wins.
 * 4. Ties are broken by a fixed priority order (PERSONALITY_PRIORITY).
 *
 * This function is pure and deterministic: same inputs always produce the same output.
 */
export function computePersonalityType(
  answers: QuizAnswer[],
  questions: QuizQuestion[]
): string {
  const scores: Record<string, number> = {};

  for (const answer of answers) {
    const question = questions.find((q) => q.id === answer.questionId);
    if (!question) {
      continue;
    }

    const selectedOption = question.options.find(
      (opt) => opt.id === answer.selectedOptionId
    );
    if (!selectedOption) {
      continue;
    }

    for (const [trait, weight] of Object.entries(selectedOption.weight)) {
      scores[trait] = (scores[trait] ?? 0) + weight;
    }
  }

  // Find the personality type with the highest score, using priority order for ties
  let bestType: string = PERSONALITY_PRIORITY[0];
  let bestScore = -Infinity;

  for (const type of PERSONALITY_PRIORITY) {
    const score = scores[type] ?? 0;
    if (score > bestScore) {
      bestScore = score;
      bestType = type;
    }
  }

  return bestType;
}
