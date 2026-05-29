import { League } from '../enums';

export interface User {
  readonly id: string;
  readonly firebaseUid: string;
  readonly username: string;
  readonly email: string;
  readonly avatarUrl?: string;
  readonly level: number;
  readonly totalXp: number;
  readonly currentStreak: number;
  readonly league: League;
  readonly premiumStatus: boolean;
  readonly onboardingComplete: boolean;
  readonly createdAt: string;
}
