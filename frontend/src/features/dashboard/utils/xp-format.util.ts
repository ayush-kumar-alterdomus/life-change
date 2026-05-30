/**
 * Formats XP values as "current / required XP" with locale number formatting.
 * Example: formatXpDisplay(2400, 3000) → "2,400 / 3,000 XP"
 */
export function formatXpDisplay(currentXp: number, requiredXp: number): string {
  return `${currentXp.toLocaleString()} / ${requiredXp.toLocaleString()} XP`;
}
