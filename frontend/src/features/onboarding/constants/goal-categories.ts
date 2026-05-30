import { GoalCategory } from '../models';

export const GOAL_CATEGORIES: GoalCategory[] = [
  {
    id: 'fitness',
    name: 'Fitness',
    description: 'Build strength and endurance',
    icon: 'barbell-outline',
  },
  {
    id: 'career',
    name: 'Career',
    description: 'Level up professionally',
    icon: 'briefcase-outline',
  },
  {
    id: 'mindfulness',
    name: 'Mindfulness',
    description: 'Find inner peace and focus',
    icon: 'leaf-outline',
  },
  {
    id: 'relationships',
    name: 'Relationships',
    description: 'Deepen your connections',
    icon: 'people-outline',
  },
  { id: 'finance', name: 'Finance', description: 'Master your money', icon: 'wallet-outline' },
  { id: 'learning', name: 'Learning', description: 'Expand your knowledge', icon: 'book-outline' },
];
