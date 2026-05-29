import { Difficulty, QuestFrequency, StatType } from '../enums';

export interface Quest {
  id: string;
  title: string;
  description?: string;
  xpReward: number;
  difficulty: Difficulty;
  statType: StatType;
  frequency: QuestFrequency;
  timeEstimate?: string;
  completed: boolean;
  completedAt?: Date;
  arcId?: string;
}
