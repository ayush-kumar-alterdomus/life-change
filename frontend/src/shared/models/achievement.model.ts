export interface Achievement {
  id: string;
  title: string;
  description: string;
  iconUrl: string;
  category: string;
  locked: boolean;
  unlockedAt?: Date;
}
