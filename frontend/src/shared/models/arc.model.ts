import { ArcType } from '../enums';

export interface Arc {
  id: string;
  name: string;
  description: string;
  arcType: ArcType;
  durationDays: number;
  phases: ArcPhase[];
  progressPercentage: number;
  currentPhase: string;
  startedAt?: Date;
  completedAt?: Date;
}

export interface ArcPhase {
  id: string;
  name: string;
  order: number;
  milestones: ArcMilestone[];
}

export interface ArcMilestone {
  id: string;
  title: string;
  completed: boolean;
}
