export interface Guild {
  id: string;
  name: string;
  description?: string;
  memberCount: number;
  guildLevel: number;
  guildRank?: number;
  createdAt: Date;
}
