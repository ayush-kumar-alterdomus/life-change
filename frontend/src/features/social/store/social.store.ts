import { Injectable, inject, signal } from '@angular/core';
import { LeagueService } from '../services/league.service';
import { GuildService } from '../services/guild.service';
import { SocialApiService } from '../services/social.service';
import { LeagueInfo, LeaderboardEntry, GuildInfo, FriendInfo, ChallengeInfo, CreateChallengePayload } from '../models';

@Injectable({ providedIn: 'root' })
export class SocialStore {
  private readonly leagueService = inject(LeagueService);
  private readonly guildService = inject(GuildService);
  private readonly socialService = inject(SocialApiService);

  readonly leagueInfo = signal<LeagueInfo | null>(null);
  readonly leaderboardEntries = signal<LeaderboardEntry[]>([]);
  readonly myGuild = signal<GuildInfo | null>(null);
  readonly browseGuilds = signal<GuildInfo[]>([]);
  readonly guildLeaderboard = signal<GuildInfo[]>([]);
  readonly friends = signal<FriendInfo[]>([]);
  readonly pendingRequests = signal<FriendInfo[]>([]);
  readonly challenges = signal<ChallengeInfo[]>([]);

  readonly loadingLeaderboard = signal(false);
  readonly loadingGuilds = signal(false);
  readonly loadingFriends = signal(false);
  readonly loadingChallenges = signal(false);

  readonly leaderboardError = signal<string | null>(null);
  readonly guildsError = signal<string | null>(null);
  readonly friendsError = signal<string | null>(null);
  readonly challengesError = signal<string | null>(null);

  private leaderboardPage = 0;

  loadLeaderboard(): void {
    this.loadingLeaderboard.set(true);
    this.leaderboardError.set(null);
    this.leaderboardPage = 0;

    this.leagueService.getLeagueInfo().subscribe({
      next: (info) => this.leagueInfo.set(info),
      error: () => {},
    });

    this.leagueService.getLeaderboard(this.leagueInfo()?.tier ?? 'BRONZE', 0).subscribe({
      next: (entries) => {
        this.leaderboardEntries.set(entries);
        this.loadingLeaderboard.set(false);
      },
      error: (err) => {
        this.leaderboardError.set(err.message ?? 'Failed to load leaderboard');
        this.loadingLeaderboard.set(false);
      },
    });
  }

  loadMoreLeaderboard(): void {
    this.leaderboardPage++;
    this.leagueService.getLeaderboard(this.leagueInfo()?.tier ?? 'BRONZE', this.leaderboardPage).subscribe({
      next: (entries) => this.leaderboardEntries.update((prev) => [...prev, ...entries]),
      error: () => {},
    });
  }

  loadGuilds(): void {
    this.loadingGuilds.set(true);
    this.guildsError.set(null);

    this.guildService.listGuilds().subscribe({
      next: (guilds) => {
        this.browseGuilds.set(guilds);
        const mine = guilds.find((g) => g.isMember);
        this.myGuild.set(mine ?? null);
        this.loadingGuilds.set(false);
      },
      error: (err) => {
        this.guildsError.set(err.message ?? 'Failed to load guilds');
        this.loadingGuilds.set(false);
      },
    });

    this.guildService.getGuildLeaderboard().subscribe({
      next: (guilds) => this.guildLeaderboard.set(guilds),
      error: () => {},
    });
  }

  joinGuild(guildId: string): void {
    this.guildService.joinGuild(guildId).subscribe({
      next: () => this.loadGuilds(),
      error: () => {},
    });
  }

  loadFriends(): void {
    this.loadingFriends.set(true);
    this.friendsError.set(null);

    this.socialService.getFriends().subscribe({
      next: (friends) => {
        this.friends.set(friends);
        this.loadingFriends.set(false);
      },
      error: (err) => {
        this.friendsError.set(err.message ?? 'Failed to load friends');
        this.loadingFriends.set(false);
      },
    });

    this.socialService.getPendingRequests().subscribe({
      next: (pending) => this.pendingRequests.set(pending),
      error: () => {},
    });
  }

  acceptFriendRequest(friendId: string): void {
    this.socialService.acceptFriendRequest(friendId).subscribe({
      next: () => this.loadFriends(),
      error: () => {},
    });
  }

  removeFriend(friendId: string): void {
    this.friends.update((list) => list.filter((f) => f.userId !== friendId));
    this.socialService.removeFriend(friendId).subscribe({ error: () => this.loadFriends() });
  }

  sendFriendRequest(userId: string): void {
    this.socialService.sendFriendRequest(userId).subscribe({ error: () => {} });
  }

  loadChallenges(): void {
    this.loadingChallenges.set(true);
    this.challengesError.set(null);

    this.socialService.getChallenges().subscribe({
      next: (challenges) => {
        this.challenges.set(challenges);
        this.loadingChallenges.set(false);
      },
      error: (err) => {
        this.challengesError.set(err.message ?? 'Failed to load challenges');
        this.loadingChallenges.set(false);
      },
    });
  }

  createChallenge(payload: CreateChallengePayload): void {
    this.socialService.createChallenge(payload).subscribe({
      next: (challenge) => this.challenges.update((list) => [challenge, ...list]),
      error: () => {},
    });
  }
}
