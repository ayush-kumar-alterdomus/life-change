export interface Streak {
  id: string;
  userId: string;
  currentDays: number;
  longestDays: number;
  comboMultiplier: number;
  shieldsRemaining: number;
  lastActiveDate: Date;
}
