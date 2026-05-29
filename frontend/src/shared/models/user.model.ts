import { League } from '../enums';

export interface User {
  id: string;
  firebaseUid: string;
  username: string;
  email: string;
  avatarUrl?: string;
  level: number;
  totalXp: number;
  currentStreak: number;
  league: League;
  premiumStatus: boolean;
  createdAt: Date;
}
