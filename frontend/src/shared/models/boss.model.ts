export interface Boss {
  id: string;
  name: string;
  level: number;
  healthPercentage: number;
  defeated: boolean;
  arcId: string;
  rewards?: string[];
}
