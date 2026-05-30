export interface DifficultyTier {
  readonly id: string;
  readonly name: string;
  readonly subtitle: string;
  readonly flameCount: number;
  readonly recommended: boolean;
  readonly glowColor: string;
}
