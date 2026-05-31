# Design Document: Social Tab UI

## Overview

The Social tab is a multi-section feature screen under `frontend/src/features/social/` implementing the Leaderboard, Guild, Friends, and Challenges experiences. It follows the project's smart pages + dumb components pattern with a signals-based store backed by HTTP services.

## Architecture

```
features/social/
├── social.routes.ts                    # Lazy-loaded route definitions
├── pages/
│   └── social-hub/                     # Smart page: tabbed social hub
├── components/
│   ├── leaderboard-section/            # Dumb: league info + ranked list
│   ├── guild-section/                  # Dumb: guild card + browse + leaderboard
│   ├── friends-section/                # Dumb: friend list + pending + search
│   ├── challenges-section/             # Dumb: active challenges + create
│   └── challenge-card/                 # Dumb: individual challenge display
├── services/
│   ├── social.service.ts               # HTTP: friends, challenges
│   ├── guild.service.ts                # HTTP: guilds
│   └── league.service.ts               # HTTP: leaderboard, league info
└── store/
    └── social.store.ts                 # Signals-based reactive state
```

## Components and Interfaces

### Smart Page: SocialHubComponent

Orchestrates the tabbed layout, injects the store, and delegates rendering to dumb section components.

```typescript
@Component({
  standalone: true,
  selector: 'app-social-hub',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IonicModule, CommonModule,
    LeaderboardSectionComponent, GuildSectionComponent,
    FriendsSectionComponent, ChallengesSectionComponent,
  ],
})
export class SocialHubComponent implements OnInit {
  private readonly store = inject(SocialStore);
  private readonly router = inject(Router);

  readonly selectedTab = signal<'leaderboard' | 'guild' | 'friends' | 'challenges'>('leaderboard');

  ngOnInit(): void {
    this.loadCurrentSection();
  }

  onTabChange(tab: string): void {
    this.selectedTab.set(tab as any);
    this.loadCurrentSection();
  }

  onRefresh(event: any): void {
    this.loadCurrentSection();
    setTimeout(() => event.target.complete(), 1500);
  }

  private loadCurrentSection(): void {
    switch (this.selectedTab()) {
      case 'leaderboard': this.store.loadLeaderboard(); break;
      case 'guild': this.store.loadGuilds(); break;
      case 'friends': this.store.loadFriends(); break;
      case 'challenges': this.store.loadChallenges(); break;
    }
  }
}
```

### Dumb Components

#### LeaderboardSectionComponent

```typescript
@Component({ standalone: true, selector: 'app-leaderboard-section', changeDetection: OnPush })
export class LeaderboardSectionComponent {
  leagueInfo = input.required<LeagueInfo | null>();
  entries = input.required<LeaderboardEntry[]>();
  loading = input<boolean>(false);
  error = input<string | null>(null);
  loadMore = output<void>();
  retry = output<void>();
}
```

#### GuildSectionComponent

```typescript
@Component({ standalone: true, selector: 'app-guild-section', changeDetection: OnPush })
export class GuildSectionComponent {
  myGuild = input.required<GuildInfo | null>();
  browseGuilds = input.required<GuildInfo[]>();
  guildLeaderboard = input.required<GuildInfo[]>();
  loading = input<boolean>(false);
  error = input<string | null>(null);
  joinGuild = output<string>(); // guildId
  viewGuild = output<string>(); // guildId
  createGuild = output<void>();
  retry = output<void>();
}
```

#### FriendsSectionComponent

```typescript
@Component({ standalone: true, selector: 'app-friends-section', changeDetection: OnPush })
export class FriendsSectionComponent {
  friends = input.required<FriendInfo[]>();
  pendingRequests = input.required<FriendInfo[]>();
  loading = input<boolean>(false);
  error = input<string | null>(null);
  acceptRequest = output<string>(); // friendId
  declineRequest = output<string>(); // friendId
  removeFriend = output<string>(); // friendId
  sendRequest = output<string>(); // userId
  retry = output<void>();

  searchQuery = signal('');
  searchResults = signal<FriendInfo[]>([]);
}
```

#### ChallengesSectionComponent

```typescript
@Component({ standalone: true, selector: 'app-challenges-section', changeDetection: OnPush })
export class ChallengesSectionComponent {
  challenges = input.required<ChallengeInfo[]>();
  loading = input<boolean>(false);
  error = input<string | null>(null);
  createChallenge = output<void>();
  retry = output<void>();
}
```

#### ChallengeCardComponent

```typescript
@Component({ standalone: true, selector: 'app-challenge-card', changeDetection: OnPush })
export class ChallengeCardComponent {
  challenge = input.required<ChallengeInfo>();

  myProgressPercent = computed(() => (this.challenge().myProgress / this.challenge().target) * 100);
  opponentProgressPercent = computed(() => (this.challenge().opponentProgress / this.challenge().target) * 100);
  isCompleted = computed(() => this.challenge().status === 'COMPLETED');
  isWinner = computed(() => this.challenge().winner === 'me');
}
```

## Data Models

```typescript
// models/social.models.ts

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
```

## Services

### SocialService

```typescript
@Injectable({ providedIn: 'root' })
export class SocialService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  getFriends(): Observable<ApiResponse<FriendInfo[]>> { ... }
  getPendingRequests(): Observable<ApiResponse<FriendInfo[]>> { ... }
  sendFriendRequest(friendId: string): Observable<ApiResponse<void>> { ... }
  acceptFriendRequest(friendId: string): Observable<ApiResponse<void>> { ... }
  removeFriend(friendId: string): Observable<ApiResponse<void>> { ... }
  getChallenges(): Observable<ApiResponse<ChallengeInfo[]>> { ... }
  createChallenge(payload: CreateChallengePayload): Observable<ApiResponse<ChallengeInfo>> { ... }
}
```

### GuildService

```typescript
@Injectable({ providedIn: 'root' })
export class GuildService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/guilds`;

  listGuilds(page?: number): Observable<ApiResponse<GuildInfo[]>> { ... }
  getGuildLeaderboard(page?: number): Observable<ApiResponse<GuildInfo[]>> { ... }
  joinGuild(guildId: string): Observable<ApiResponse<void>> { ... }
  leaveGuild(guildId: string): Observable<ApiResponse<void>> { ... }
}
```

### LeagueService

```typescript
@Injectable({ providedIn: 'root' })
export class LeagueService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/league`;

  getLeagueInfo(): Observable<ApiResponse<LeagueInfo>> { ... }
  getLeaderboard(tier: string, page: number): Observable<ApiResponse<{ entries: LeaderboardEntry[] }>> { ... }
}
```

## Store

### SocialStore

```typescript
@Injectable({ providedIn: 'root' })
export class SocialStore {
  // --- State signals ---
  readonly leagueInfo = signal<LeagueInfo | null>(null);
  readonly leaderboardEntries = signal<LeaderboardEntry[]>([]);
  readonly myGuild = signal<GuildInfo | null>(null);
  readonly browseGuilds = signal<GuildInfo[]>([]);
  readonly guildLeaderboard = signal<GuildInfo[]>([]);
  readonly friends = signal<FriendInfo[]>([]);
  readonly pendingRequests = signal<FriendInfo[]>([]);
  readonly challenges = signal<ChallengeInfo[]>([]);

  // --- Loading signals ---
  readonly loadingLeaderboard = signal(false);
  readonly loadingGuilds = signal(false);
  readonly loadingFriends = signal(false);
  readonly loadingChallenges = signal(false);

  // --- Error signals ---
  readonly leaderboardError = signal<string | null>(null);
  readonly guildsError = signal<string | null>(null);
  readonly friendsError = signal<string | null>(null);
  readonly challengesError = signal<string | null>(null);

  // --- Actions ---
  loadLeaderboard(): void { ... }
  loadMoreLeaderboard(): void { ... }
  loadGuilds(): void { ... }
  joinGuild(guildId: string): void { ... }
  loadFriends(): void { ... }
  acceptFriendRequest(friendId: string): void { ... }
  removeFriend(friendId: string): void { ... }
  sendFriendRequest(userId: string): void { ... }
  loadChallenges(): void { ... }
}
```

## Routing

```typescript
export const SOCIAL_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/social-hub/social-hub.component').then(m => m.SocialHubComponent),
  },
  {
    path: 'guild/:id',
    loadComponent: () => import('./pages/guild-detail/guild-detail.component').then(m => m.GuildDetailComponent),
  },
  {
    path: 'challenge/create',
    loadComponent: () => import('./pages/challenge-create/challenge-create.component').then(m => m.ChallengeCreateComponent),
  },
];
```

## Backend API Mapping

| Frontend Action | Backend Endpoint | Method |
|----------------|-----------------|--------|
| Load league info | `/api/v1/league/info` | GET |
| Load leaderboard | `/api/v1/league/leaderboard?tier={tier}&page={page}` | GET |
| Load friends | `/api/v1/friends` | GET |
| Load pending requests | `/api/v1/friends/pending` | GET |
| Send friend request | `/api/v1/friends/request` | POST |
| Accept friend request | `/api/v1/friends/accept` | POST |
| Remove friend | `/api/v1/friends/{friendId}` | DELETE |
| Block user | `/api/v1/friends/block` | POST |
| List guilds | `/api/v1/guilds` | GET |
| Guild leaderboard | `/api/v1/guilds/leaderboard` | GET |
| Join guild | `/api/v1/guilds/{id}/join` | POST |
| Leave guild | `/api/v1/guilds/{id}/leave` | POST |
| Guild detail | `/api/v1/guilds/{id}` | GET |
| Load challenges | `/api/v1/social/challenges` | GET |
| Create challenge | `/api/v1/social/challenges` | POST |

## Error Handling

| Scenario | Behavior |
|----------|----------|
| Leaderboard fetch fails | Show error state with retry in leaderboard section only |
| Guild fetch fails | Show error state with retry in guild section only |
| Friends fetch fails | Show error state with retry in friends section only |
| Join guild fails | Show toast with error message |
| Friend request fails | Show toast with error message |
| Network offline | Global offline banner shown (existing infrastructure) |

## Accessibility

- Segmented control uses `role="tablist"` / `role="tab"` / `role="tabpanel"` pattern
- All cards have descriptive `aria-label` attributes
- Progress bars use `aria-valuenow`, `aria-valuemin`, `aria-valuemax`
- Pending badge uses `aria-label="N pending friend requests"`
- Touch targets minimum 44×44px on all interactive elements
- Screen reader announces tab changes via `aria-live="polite"`

## Performance

- Only active section fetches data (lazy section loading)
- Leaderboard uses virtual scroll for 50+ entries
- All components use OnPush change detection
- Avatar images use lazy loading
- Cache-first strategy with background refresh
