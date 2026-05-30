/** Key used to persist the last displayed motivation message index in storage. */
export const LAST_MOTIVATION_INDEX_KEY = 'motivation_last_index';

/** Static pool of motivational messages displayed on the dashboard. */
export const MOTIVATION_MESSAGES: string[] = [
  'Small steps every day lead to massive transformation.',
  "You don't have to be perfect — just consistent.",
  'Progress is progress, no matter how small.',
  "The only bad workout is the one that didn't happen.",
  'Your future self will thank you for starting today.',
  'Discipline is choosing between what you want now and what you want most.',
  'Every expert was once a beginner.',
  'Fall seven times, stand up eight.',
  'The secret of getting ahead is getting started.',
  "Believe you can and you're halfway there.",
  'Success is the sum of small efforts repeated day in and day out.',
  "Don't watch the clock; do what it does — keep going.",
  'You are stronger than you think.',
  'A journey of a thousand miles begins with a single step.',
  'What you do today can improve all your tomorrows.',
  "It's not about being the best. It's about being better than you were yesterday.",
  'Push yourself, because no one else is going to do it for you.',
  'Great things never come from comfort zones.',
  'Dream it. Wish it. Do it.',
  'Wake up with determination. Go to bed with satisfaction.',
  "The harder you work for something, the greater you'll feel when you achieve it.",
  "Don't stop when you're tired. Stop when you're done.",
];

/**
 * Selects the next motivation message index, avoiding consecutive repeats.
 * The returned index is guaranteed to differ from `lastIndex` (when poolSize >= 2)
 * and to be within bounds [0, poolSize).
 *
 * @param lastIndex - The index of the previously displayed message, or null if none.
 * @param poolSize - The total number of messages in the pool.
 * @returns The selected index for the next message.
 */
export function selectMotivationMessage(lastIndex: number | null, poolSize: number): number {
  if (poolSize <= 1) {
    return 0;
  }

  let nextIndex: number;
  do {
    nextIndex = Math.floor(Math.random() * poolSize);
  } while (nextIndex === lastIndex);

  return nextIndex;
}
