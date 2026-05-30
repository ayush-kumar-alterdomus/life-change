/**
 * Returns a greeting string based on the current hour.
 * 5:00–11:59 → "Good Morning"
 * 12:00–16:59 → "Good Afternoon"
 * 17:00–4:59 → "Good Evening"
 *
 * When a non-null display name is provided, the greeting is suffixed with ", {displayName}".
 *
 * @param displayName - The user's display name, or null if not yet loaded
 * @param hour - Optional hour override (0–23) for testing; defaults to current hour
 * @returns The formatted greeting string
 */
export function getTimeBasedGreeting(displayName: string | null, hour?: number): string {
  const h = hour ?? new Date().getHours();
  let greeting: string;

  if (h >= 5 && h < 12) {
    greeting = 'Good Morning';
  } else if (h >= 12 && h < 17) {
    greeting = 'Good Afternoon';
  } else {
    greeting = 'Good Evening';
  }

  return displayName ? `${greeting}, ${displayName}` : greeting;
}
