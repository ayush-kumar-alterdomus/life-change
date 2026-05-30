import { XpFillAnimationConfig } from '../models/quest-completion.models';

/**
 * Pure function: Calculate XP fill animation configuration.
 * Determines if the fill crosses a level boundary and computes overflow.
 *
 * @param currentXp - The user's current XP before quest completion
 * @param xpEarned - The XP earned from completing the quest
 * @param levelThreshold - The XP required to reach the next level
 * @returns Configuration object for animating the XP progress bar fill
 */
export function calculateXpFillConfig(
  currentXp: number,
  xpEarned: number,
  levelThreshold: number
): XpFillAnimationConfig {
  const newXp = currentXp + xpEarned;
  const crossesLevel = newXp >= levelThreshold;
  const overflowXp = crossesLevel ? newXp - levelThreshold : 0;

  return {
    previousXp: currentXp,
    newXp: crossesLevel ? levelThreshold : newXp,
    levelThreshold,
    crossesLevel,
    overflowXp,
  };
}

/**
 * Pure function: Interpolate between two XP values for counting animation.
 * Progress is clamped to [0, 1] to ensure safe interpolation.
 *
 * @param startXp - The starting XP value
 * @param endXp - The ending XP value
 * @param progress - Animation progress from 0 to 1 (clamped)
 * @returns The interpolated XP value, rounded to the nearest integer
 */
export function interpolateXp(startXp: number, endXp: number, progress: number): number {
  const clamped = Math.max(0, Math.min(1, progress));
  return Math.round(startXp + (endXp - startXp) * clamped);
}

/**
 * Pure function: Format XP display text with locale-formatted numbers.
 *
 * @param currentXp - The current XP value to display
 * @param requiredXp - The required XP for the next level
 * @returns Formatted string like "2,400 / 3,000 XP"
 */
export function formatXpDisplay(currentXp: number, requiredXp: number): string {
  return `${currentXp.toLocaleString()} / ${requiredXp.toLocaleString()} XP`;
}

/**
 * Pure function: Determine if all daily quests are complete.
 * Returns false for empty quest arrays (no quests means no "perfect day").
 *
 * @param quests - Array of quest objects with a completed flag
 * @returns true if there is at least one quest and all are completed
 */
export function isDailySetComplete(quests: { completed: boolean }[]): boolean {
  return quests.length > 0 && quests.every(q => q.completed);
}
