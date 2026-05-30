import { ArcDefinition } from '../models';

export const AVAILABLE_ARCS: ArcDefinition[] = [
  {
    id: 'monk',
    name: 'The Monk',
    description: 'Master discipline through mindfulness and routine',
    themeColor: '#4CAF50',
    sampleQuests: ['Morning meditation', 'Digital detox hour', 'Gratitude journal', 'Breathwork session'],
    icon: 'leaf-outline',
  },
  {
    id: 'warrior',
    name: 'The Warrior',
    description: 'Conquer challenges through physical and mental strength',
    themeColor: '#F44336',
    sampleQuests: ['Cold shower challenge', '100 pushups', 'Run 5K', 'No excuses day'],
    icon: 'shield-outline',
  },
  {
    id: 'scholar',
    name: 'The Scholar',
    description: 'Grow through knowledge and intellectual pursuit',
    themeColor: '#2196F3',
    sampleQuests: ['Read 30 pages', 'Learn a new skill', 'Teach someone', 'Deep work block'],
    icon: 'book-outline',
  },
  {
    id: 'creator',
    name: 'The Creator',
    description: 'Build and express through creative output',
    themeColor: '#9C27B0',
    sampleQuests: ['Create something new', 'Share your work', 'Brainstorm 10 ideas', 'Ship a project'],
    icon: 'color-palette-outline',
  },
  {
    id: 'beast',
    name: 'Beast Mode',
    description: 'Push every limit with relentless intensity',
    themeColor: '#FF5722',
    sampleQuests: ['5AM wake-up', 'Double workout', 'Zero cheat meals', 'Outwork everyone'],
    icon: 'flame-outline',
  },
];
