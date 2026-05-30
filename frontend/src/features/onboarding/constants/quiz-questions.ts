import { QuizQuestion } from '../models';

export const QUIZ_QUESTIONS: QuizQuestion[] = [
  {
    id: 'q1',
    text: 'How do you prefer to structure your day?',
    dimension: 'discipline_style',
    options: [
      { id: 'q1_a', text: 'Strict schedule with clear time blocks', weight: { disciplined: 3, analytical: 1 } },
      { id: 'q1_b', text: 'Flexible flow based on energy levels', weight: { spontaneous: 3, creative: 1 } },
      { id: 'q1_c', text: 'Mix of planned priorities and open time', weight: { disciplined: 1, spontaneous: 1, collaborative: 1 } },
    ],
  },
  {
    id: 'q2',
    text: 'When you fall off track, what gets you back on?',
    dimension: 'discipline_style',
    options: [
      { id: 'q2_a', text: 'A clear system or checklist to follow', weight: { disciplined: 3, analytical: 2 } },
      { id: 'q2_b', text: 'A burst of inspiration or new idea', weight: { spontaneous: 2, creative: 3 } },
      { id: 'q2_c', text: 'Accountability from someone I respect', weight: { collaborative: 3, competitive: 1 } },
    ],
  },
  {
    id: 'q3',
    text: 'What motivates you most to push harder?',
    dimension: 'motivation_triggers',
    options: [
      { id: 'q3_a', text: 'Competing against others and winning', weight: { competitive: 3, disciplined: 1 } },
      { id: 'q3_b', text: 'Working with a team toward a shared goal', weight: { collaborative: 3, spontaneous: 1 } },
      { id: 'q3_c', text: 'Tracking personal progress and data', weight: { analytical: 3, disciplined: 1 } },
      { id: 'q3_d', text: 'Exploring new creative possibilities', weight: { creative: 3, spontaneous: 1 } },
    ],
  },
  {
    id: 'q4',
    text: 'What type of challenge excites you most?',
    dimension: 'challenge_type',
    options: [
      { id: 'q4_a', text: 'Physical endurance and strength tests', weight: { competitive: 2, disciplined: 2 } },
      { id: 'q4_b', text: 'Complex puzzles and strategic thinking', weight: { analytical: 3, creative: 1 } },
      { id: 'q4_c', text: 'Creative projects with open-ended outcomes', weight: { creative: 3, spontaneous: 1 } },
      { id: 'q4_d', text: 'Social challenges and leadership tasks', weight: { collaborative: 3, competitive: 1 } },
    ],
  },
  {
    id: 'q5',
    text: 'How much time can you dedicate to self-improvement daily?',
    dimension: 'time_availability',
    options: [
      { id: 'q5_a', text: '15–30 minutes', weight: { spontaneous: 2, creative: 1 } },
      { id: 'q5_b', text: '30–60 minutes', weight: { disciplined: 1, collaborative: 1, analytical: 1 } },
      { id: 'q5_c', text: '1–2 hours', weight: { disciplined: 2, competitive: 1 } },
      { id: 'q5_d', text: '2+ hours — I go all in', weight: { competitive: 2, disciplined: 2 } },
    ],
  },
  {
    id: 'q6',
    text: 'When is your peak performance time?',
    dimension: 'time_availability',
    options: [
      { id: 'q6_a', text: 'Early morning — I rise before the world', weight: { disciplined: 3, competitive: 1 } },
      { id: 'q6_b', text: 'Midday — I hit my stride after warming up', weight: { collaborative: 2, analytical: 1 } },
      { id: 'q6_c', text: 'Evening — I come alive at night', weight: { creative: 2, spontaneous: 2 } },
      { id: 'q6_d', text: 'It varies — I adapt to the day', weight: { spontaneous: 2, collaborative: 1 } },
    ],
  },
];
