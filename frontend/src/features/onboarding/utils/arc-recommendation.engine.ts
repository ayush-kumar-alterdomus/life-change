import { AVAILABLE_ARCS } from '../constants/available-arcs';

/**
 * Goal-to-Arc affinity mappings.
 * Each goal contributes weighted scores to specific Arcs.
 */
const GOAL_AFFINITIES: Record<string, Record<string, number>> = {
  fitness: { warrior: 3, beast: 3, monk: 1, scholar: 0, creator: 0 },
  career: { scholar: 3, creator: 2, warrior: 1, monk: 1, beast: 1 },
  mindfulness: { monk: 4, scholar: 1, creator: 1, warrior: 0, beast: 0 },
  relationships: { creator: 2, monk: 2, scholar: 1, warrior: 1, beast: 0 },
  finance: { scholar: 3, warrior: 1, monk: 1, creator: 1, beast: 1 },
  learning: { scholar: 4, creator: 2, monk: 1, warrior: 0, beast: 0 },
};

/**
 * Difficulty-to-Arc multipliers.
 * The difficulty tier boosts certain Arcs.
 */
const DIFFICULTY_MULTIPLIERS: Record<string, Record<string, number>> = {
  casual: { monk: 1.3, scholar: 1.2, creator: 1.1, warrior: 0.9, beast: 0.7 },
  balanced: { monk: 1.0, warrior: 1.0, scholar: 1.0, creator: 1.0, beast: 1.0 },
  beast_mode: { beast: 1.5, warrior: 1.3, monk: 0.8, scholar: 0.9, creator: 0.9 },
};

/**
 * Personality-to-Arc bias scores.
 * The personality type adds a final bias toward compatible Arcs.
 */
const PERSONALITY_BIAS: Record<string, Record<string, number>> = {
  disciplined: { monk: 2, warrior: 1, scholar: 1, creator: 0, beast: 1 },
  spontaneous: { creator: 2, beast: 1, warrior: 1, monk: 0, scholar: 0 },
  competitive: { warrior: 2, beast: 2, scholar: 0, monk: 0, creator: 0 },
  collaborative: { creator: 2, monk: 1, scholar: 1, warrior: 0, beast: 0 },
  analytical: { scholar: 3, monk: 1, creator: 0, warrior: 0, beast: 0 },
  creative: { creator: 3, scholar: 1, monk: 1, warrior: 0, beast: 0 },
};

/**
 * Fixed priority order for breaking ties in Arc recommendation.
 * When two or more Arcs have the same combined score,
 * the Arc appearing earlier in this array wins.
 */
const ARC_PRIORITY: readonly string[] = [
  'monk',
  'warrior',
  'scholar',
  'creator',
  'beast',
] as const;

/**
 * Computes the recommended Arc based on user inputs.
 *
 * Algorithm:
 * 1. Each goal maps to a set of Arc affinities (weighted scores) — sum across all goals.
 * 2. The difficulty tier applies a multiplier to each Arc's accumulated score.
 * 3. The personality type adds a final bias toward compatible Arcs.
 * 4. The Arc with the highest combined score wins. Ties broken by ARC_PRIORITY order.
 *
 * This function is pure and deterministic: same inputs always produce the same output.
 * Always returns a valid Arc id from AVAILABLE_ARCS.
 */
export function computeRecommendedArc(
  goals: string[],
  difficulty: string,
  personalityType: string
): string {
  const arcIds = AVAILABLE_ARCS.map((arc) => arc.id);
  const scores: Record<string, number> = {};

  // Initialize scores to 0
  for (const arcId of arcIds) {
    scores[arcId] = 0;
  }

  // Step 1: Accumulate goal affinities
  for (const goal of goals) {
    const affinities = GOAL_AFFINITIES[goal];
    if (affinities) {
      for (const arcId of arcIds) {
        scores[arcId] += affinities[arcId] ?? 0;
      }
    }
  }

  // Step 2: Apply difficulty multiplier
  const multipliers = DIFFICULTY_MULTIPLIERS[difficulty] ?? DIFFICULTY_MULTIPLIERS['balanced'];
  for (const arcId of arcIds) {
    scores[arcId] *= multipliers[arcId] ?? 1.0;
  }

  // Step 3: Add personality bias
  const bias = PERSONALITY_BIAS[personalityType];
  if (bias) {
    for (const arcId of arcIds) {
      scores[arcId] += bias[arcId] ?? 0;
    }
  }

  // Step 4: Find the Arc with the highest score, using priority order for ties
  let bestArc: string = ARC_PRIORITY[0];
  let bestScore = -Infinity;

  for (const arcId of ARC_PRIORITY) {
    const score = scores[arcId] ?? 0;
    if (score > bestScore) {
      bestScore = score;
      bestArc = arcId;
    }
  }

  return bestArc;
}
