import { Arc } from '@shared/models';
import { Boss } from '@shared/models';

export interface ArcDetail extends Arc {
  milestones: ArcMilestoneDetail[];
  boss: Boss | null;
  rewards: ArcReward[];
  skillTreeNodes: SkillNode[];
  identityTitles: IdentityTitleMap;
  questFrequency: string;
  isPrebuilt?: boolean;
}

export interface ArcMilestoneDetail {
  id: string;
  title: string;
  description: string;
  completed: boolean;
  xpReward: number;
  phase: string;
  orderIndex: number;
}

export interface ArcPhaseWithMilestones {
  name: string;
  order: number;
  milestones: ArcMilestoneDetail[];
}

export interface ArcReward {
  id: string;
  name: string;
  type: 'xp' | 'title' | 'cosmetic' | 'coins';
  earned: boolean;
  unlocksAtPhase: string;
}

export interface SkillNode {
  id: string;
  name: string;
  unlocked: boolean;
  order: number;
}

export interface IdentityTitleMap {
  [phase: string]: string;
  Beginner: string;
  Intermediate: string;
  Elite: string;
  Master: string;
}

export interface CreateArcPayload {
  title: string;
  goal: string;
  durationDays: number;
  milestones: string[];
  questFrequency: string;
}
