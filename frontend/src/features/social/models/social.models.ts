export interface LeagueInfo {
  tier: string;
  rank: number;
  weeklyXp: number;
  inPromotionZone: boolean;
  inDemotionZone: boolean;
  promotionThreshold: number;
  demotionThreshold: number;
}

export interface LeaderboardEntry {
  rank: number;
  username: string;
  level: number;
  xpTotal: number;
  avatarUrl: string;
  isCurrentUser: boolean;
}

export interface GuildInfo {
  id: string;
  name: string;
  description?: string;
  memberCount: number;
  guildLevel: number;
  guildRank?: number;
  isMember?: boolean;
}

export interface FriendInfo {
  userId: string;
  username: string;
  avatarUrl?: string;
  level: number;
  streak: number;
  status: 'ACCEPTED' | 'PENDING';
  online?: boolean;
}

export interface ChallengeInfo {
  id: string;
  title: string;
  opponentName: string;
  opponentAvatar?: string;
  target: number;
  myProgress: number;
  opponentProgress: number;
  status: 'ACTIVE' | 'COMPLETED' | 'EXPIRED';
  winner?: string;
  endsAt: string;
}

export interface CreateChallengePayload {
  friendId: string;
  title: string;
  target: number;
  endsAt: string;
}
