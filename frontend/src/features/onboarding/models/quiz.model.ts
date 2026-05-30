export interface QuizAnswer {
  readonly questionId: string;
  readonly selectedOptionId: string;
}

export interface QuizOption {
  readonly id: string;
  readonly text: string;
  readonly weight: Record<string, number>;
}

export interface QuizQuestion {
  readonly id: string;
  readonly text: string;
  readonly options: QuizOption[];
  readonly dimension: PersonalityDimension;
}

export type PersonalityDimension =
  | 'discipline_style'
  | 'motivation_triggers'
  | 'challenge_type'
  | 'time_availability';
