import { DifficultyTier } from '../models';

export const DIFFICULTY_TIERS: DifficultyTier[] = [
  { id: 'casual', name: 'Casual', subtitle: 'Easy pace, gentle reminders', flameCount: 1, recommended: false, glowColor: '#FF9800' },
  { id: 'balanced', name: 'Balanced', subtitle: 'Moderate challenge, steady growth', flameCount: 2, recommended: true, glowColor: '#FF9800' },
  { id: 'beast_mode', name: 'Beast Mode', subtitle: 'Intense discipline, maximum results', flameCount: 3, recommended: false, glowColor: '#F44336' },
];
