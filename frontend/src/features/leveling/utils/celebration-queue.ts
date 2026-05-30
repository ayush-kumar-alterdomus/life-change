import { CelebrationItem, LevelUpEvent } from '../models/level-up.models';
import { getMilestoneConfig } from './milestone-config';

export function decomposeLevelJump(event: LevelUpEvent): CelebrationItem[] {
  const levels = event.newLevel - event.previousLevel;
  const items: CelebrationItem[] = [];

  for (let i = 1; i <= levels; i++) {
    const level = event.previousLevel + i;
    const milestoneConfig = getMilestoneConfig(level);
    items.push({
      level,
      rewards: i === levels ? event.rewards : [],
      isMilestone: milestoneConfig !== null,
      milestoneConfig,
      queuePosition: i,
      queueTotal: levels,
    });
  }

  return items;
}
