export interface LevelUpEvent {
  userId: string;
  previousLevel: number;
  newLevel: number;
  rewards: LevelReward[];
  unlockedFeatures: string[];
}

export interface LevelReward {
  type: 'coins' | 'title' | 'cosmetic' | 'xp_multiplier';
  name: string;
  amount?: number;
  value?: string;
}

export interface MilestoneConfig {
  level: number;
  featureName: string;
  tagline: string;
  icon: string;
}

export interface CelebrationItem {
  level: number;
  rewards: LevelReward[];
  isMilestone: boolean;
  milestoneConfig: MilestoneConfig | null;
  queuePosition: number;
  queueTotal: number;
}

export type CelebrationStep =
  | 'idle'
  | 'waiting-for-quest-animation'
  | 'glow-explosion'
  | 'level-title'
  | 'xp-fly-up'
  | 'rewards-card'
  | 'feature-unlock'
  | 'prestige-prompt'
  | 'prestige-screen'
  | 'continue-ready'
  | 'dismissing';

export interface CelebrationFlowState {
  step: CelebrationStep;
  currentItem: CelebrationItem | null;
  queue: CelebrationItem[];
  error: string | null;
}

export interface PrestigeData {
  previousPrestigeLevel: number;
  newPrestigeLevel: number;
  badgeId: string;
  badgeName: string;
}
