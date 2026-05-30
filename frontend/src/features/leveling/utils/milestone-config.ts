import { MilestoneConfig } from '../models/level-up.models';

export const MILESTONE_CONFIGS: MilestoneConfig[] = [
  {
    level: 10,
    featureName: 'Leagues Unlocked',
    tagline: 'Compete with players at your level',
    icon: 'trophy-outline',
  },
  {
    level: 25,
    featureName: 'Guilds Unlocked',
    tagline: 'Join forces with other players',
    icon: 'people-outline',
  },
  {
    level: 50,
    featureName: 'Elite Cosmetics Unlocked',
    tagline: 'Exclusive visual upgrades await',
    icon: 'diamond-outline',
  },
  {
    level: 100,
    featureName: 'Prestige System Unlocked',
    tagline: 'Reset and ascend to legendary status',
    icon: 'star-outline',
  },
];

export function getMilestoneConfig(level: number): MilestoneConfig | null {
  return MILESTONE_CONFIGS.find((m) => m.level === level) ?? null;
}

export function isMilestoneLevel(level: number): boolean {
  return MILESTONE_CONFIGS.some((m) => m.level === level);
}
