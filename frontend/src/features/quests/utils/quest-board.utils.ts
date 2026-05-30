import { Quest } from '../../../shared/models/quest.model';
import { QuestFrequency } from '../../../shared/enums/quest-frequency.enum';
import { Difficulty } from '../../../shared/enums/difficulty.enum';
import { StatType } from '../../../shared/enums/stat-type.enum';

// ─── Interfaces ────────────────────────────────────────────────────────────────

export interface ProgressSummary {
  completed: number;
  total: number;
  ratio: number;
}

export interface ValidationResult {
  valid: boolean;
  errors: Record<string, string>;
}

export interface CreateQuestPayload {
  title: string;
  description?: string;
  difficulty: Difficulty;
  statType: StatType;
  frequency: QuestFrequency;
  timeEstimate?: string;
}

// ─── Pure Logic Functions ──────────────────────────────────────────────────────

/**
 * Filters quests by the given frequency tab.
 * Returns only quests whose frequency matches the selected tab.
 */
export function filterQuestsByFrequency(quests: Quest[], frequency: QuestFrequency): Quest[] {
  return quests.filter((q) => q.frequency === frequency);
}

/**
 * Computes progress summary for daily quests.
 * Returns completed count, total count, and ratio (0 when no daily quests exist).
 */
export function computeProgress(quests: Quest[]): ProgressSummary {
  const dailyQuests = quests.filter((q) => q.frequency === QuestFrequency.Daily);
  const completed = dailyQuests.filter((q) => q.completed).length;
  const total = dailyQuests.length;
  return { completed, total, ratio: total > 0 ? completed / total : 0 };
}

/**
 * Calculates XP reward based on difficulty level.
 * Returns a deterministic positive integer for valid difficulties, 0 for null/invalid.
 */
export function calculateXpFromDifficulty(difficulty: Difficulty | null | undefined): number {
  switch (difficulty) {
    case Difficulty.Easy:
      return 25;
    case Difficulty.Medium:
      return 50;
    case Difficulty.Hard:
      return 100;
    case Difficulty.Legendary:
      return 200;
    default:
      return 0;
  }
}

/**
 * Determines whether a user can create a new custom quest.
 * Premium users always can; free users are limited to 5 active custom quests.
 */
export function canUserCreateQuest(isPremium: boolean, activeCustomQuestCount: number): boolean {
  if (isPremium) return true;
  return activeCustomQuestCount < 5;
}

/**
 * Counts the number of active (non-completed) custom quests.
 */
export function countActiveCustomQuests(quests: Quest[]): number {
  return quests.filter((q) => q.frequency === QuestFrequency.OneTime && !q.completed).length;
}

/**
 * Validates a create quest form payload.
 * Returns valid: true when all required fields are present and valid,
 * otherwise returns valid: false with error messages for each invalid field.
 */
export function validateCreateQuestForm(payload: Partial<CreateQuestPayload>): ValidationResult {
  const errors: Record<string, string> = {};

  if (!payload.title || payload.title.trim().length < 3) {
    errors['title'] = 'Title is required and must be at least 3 characters';
  }
  if (!payload.difficulty) {
    errors['difficulty'] = 'Difficulty is required';
  }
  if (!payload.statType) {
    errors['statType'] = 'Stat type is required';
  }
  if (!payload.frequency) {
    errors['frequency'] = 'Frequency is required';
  }

  return { valid: Object.keys(errors).length === 0, errors };
}
