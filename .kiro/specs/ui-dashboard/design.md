# Design Document: UI Dashboard

## Overview

The Dashboard is the primary home screen of the Ascend app — a single scrollable view aggregating user progress, daily quests, active arc status, motivation content, and leaderboard data. It replaces the existing placeholder and serves as the central hub users interact with after authentication and onboarding. The implementation uses Angular standalone components with signal-based reactive state, OnPush change detection, and parallel API fetching for optimal performance.

## Architecture

The Dashboard feature follows the established feature-first architecture with a smart page component orchestrating dumb child components. The `DashboardPage` acts as the single smart component that injects the `DashboardStore` (signal-based state) and delegates rendering to shared UI components and local presentational components.

**Data flow:**
```
DashboardPage (smart) → DashboardStore (signals) → DashboardService (API) → ApiService (HTTP) → Spring Boot
```

The store aggregates data from multiple backend endpoints fetched in parallel via `forkJoin`. Each section has independent loading/error/data signals enabling granular skeleton rendering and per-section error recovery.

---

## Components and Interfaces

### Component Tree

```
DashboardPage (smart, standalone, OnPush)
├── ion-refresher
├── HeaderSectionComponent (dumb)
│   ├── LevelBadgeComponent (shared)
│   └── StreakFlameComponent (shared)
├── XpProgressCardComponent (dumb)
│   └── XpProgressBarComponent (shared)
├── DailySummaryComponent (dumb)
│   └── MiniCardComponent × 4 (dumb)
├── ActiveArcSectionComponent (dumb)
│   └── ArcCardComponent (shared)
├── QuestListComponent (dumb)
│   ├── ion-item-sliding × N
│   │   └── QuestCardComponent (shared)
│   └── EmptyStateComponent (conditional)
├── MotivationWidgetComponent (dumb)
└── LeaderboardPreviewComponent (dumb)
    └── LeaderboardCardComponent × 3 (shared)
```

### DashboardPage (Smart Component)

```typescript
@Component({
  standalone: true,
  selector: 'app-dashboard',
  templateUrl: './dashboard.page.html',
  styleUrls: ['./dashboard.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IonicModule,
    CommonModule,
    HeaderSectionComponent,
    XpProgressCardComponent,
    DailySummaryComponent,
    ActiveArcSectionComponent,
    QuestListComponent,
    MotivationWidgetComponent,
    LeaderboardPreviewComponent,
  ],
})
export class DashboardPage implements OnInit {
  private readonly store = inject(DashboardStore);
  private readonly connectivity = inject(ConnectivityService);
  private readonly router = inject(Router);
  private readonly haptic = inject(HapticService);

  // Expose store signals to template
  readonly userSummary = this.store.userSummary;
  readonly xpProgress = this.store.xpProgress;
  readonly dailyStats = this.store.dailyStats;
  readonly activeArc = this.store.activeArc;
  readonly todayQuests = this.store.todayQuests;
  readonly leaderboardPreview = this.store.leaderboardPreview;
  readonly isLoading = this.store.isLoading;

  ngOnInit(): void {
    this.store.loadDashboard();
    this.setupConnectivityWatcher();
  }
}
```

### HeaderSectionComponent (Dumb)

```typescript
@Component({
  standalone: true,
  selector: 'app-header-section',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, LevelBadgeComponent, StreakFlameComponent],
})
export class HeaderSectionComponent {
  displayName = input<string | null>(null);
  level = input.required<number>();
  streakDays = input.required<number>();

  greeting = computed(() => getTimeBasedGreeting(this.displayName()));
}
```

### QuestListComponent (Dumb)

```typescript
@Component({
  standalone: true,
  selector: 'app-quest-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonicModule, CommonModule, QuestCardComponent],
})
export class QuestListComponent {
  quests = input.required<Quest[]>();
  maxDisplay = input<number>(8);

  completeQuest = output<string>(); // quest ID
  skipQuest = output<string>();
  editQuest = output<string>();
  viewAll = output<void>();

  displayedQuests = computed(() => this.quests().slice(0, this.maxDisplay()));
  hasOverflow = computed(() => this.quests().length > this.maxDisplay());
}
```

---

## Data Models

### Dashboard-Specific Interfaces

```typescript
/** Aggregated XP progress data for the dashboard card */
export interface DashboardXpProgress {
  currentLevel: number;
  currentXp: number;
  requiredXp: number;
}

/** Daily summary statistics */
export interface DashboardDailyStats {
  questsCompleted: number;
  questsTotal: number;
  currentStreak: number;
  focusScore: number;
  lifeScore: number;
}

/** Active arc summary for dashboard display */
export interface DashboardActiveArc {
  id: string;
  name: string;
  arcType: ArcType;
  progressPercentage: number;
  currentPhase: string;
}

/** Leaderboard preview data */
export interface DashboardLeaderboardPreview {
  userRank: number;
  userXpTotal: number;
  leagueName: string;
  topThree: LeaderboardEntry[];
}

export interface LeaderboardEntry {
  rank: number;
  username: string;
  level: number;
  xpTotal: number;
  avatarUrl: string;
  isCurrentUser: boolean;
}

/** User summary for header */
export interface DashboardUserSummary {
  displayName: string;
  level: number;
  currentStreak: number;
}

/** Section load state */
export type SectionState<T> =
  | { status: 'idle' }
  | { status: 'loading' }
  | { status: 'loaded'; data: T }
  | { status: 'error'; message: string };
```

---

## Dashboard Store

```typescript
@Injectable({ providedIn: 'root' })
export class DashboardStore {
  private readonly dashboardService = inject(DashboardService);
  private readonly storageService = inject(StorageService);

  // Section signals
  readonly userSummary = signal<SectionState<DashboardUserSummary>>({ status: 'idle' });
  readonly xpProgress = signal<SectionState<DashboardXpProgress>>({ status: 'idle' });
  readonly dailyStats = signal<SectionState<DashboardDailyStats>>({ status: 'idle' });
  readonly activeArc = signal<SectionState<DashboardActiveArc | null>>({ status: 'idle' });
  readonly todayQuests = signal<SectionState<Quest[]>>({ status: 'idle' });
  readonly leaderboardPreview = signal<SectionState<DashboardLeaderboardPreview>>({ status: 'idle' });

  // Computed loading signals
  readonly isLoading = computed(() =>
    this.userSummary().status === 'loading' ||
    this.xpProgress().status === 'loading' ||
    this.dailyStats().status === 'loading' ||
    this.activeArc().status === 'loading' ||
    this.todayQuests().status === 'loading' ||
    this.leaderboardPreview().status === 'loading'
  );

  // Per-section loading signals
  readonly userSummaryLoading = computed(() => this.userSummary().status === 'loading');
  readonly xpProgressLoading = computed(() => this.xpProgress().status === 'loading');
  readonly dailyStatsLoading = computed(() => this.dailyStats().status === 'loading');
  readonly activeArcLoading = computed(() => this.activeArc().status === 'loading');
  readonly questsLoading = computed(() => this.todayQuests().status === 'loading');
  readonly leaderboardLoading = computed(() => this.leaderboardPreview().status === 'loading');

  // Per-section error signals
  readonly userSummaryError = computed(() =>
    this.userSummary().status === 'error' ? this.userSummary() : null
  );
  readonly xpProgressError = computed(() =>
    this.xpProgress().status === 'error' ? this.xpProgress() : null
  );
  // ... similar for other sections

  /**
   * Load all dashboard data in parallel.
   * Restores cached data first if available, then fetches fresh data.
   */
  loadDashboard(): void {
    this.restoreCachedData();
    this.fetchAllSections();
  }

  /**
   * Refresh all sections (pull-to-refresh).
   * Does NOT set loading state — keeps existing data visible.
   */
  refreshDashboard(): void {
    this.fetchAllSections();
  }

  /**
   * Atomically update store after quest completion.
   * Updates todayQuests, dailyStats, and xpProgress in a single tick.
   */
  completeQuest(questId: string, xpEarned: number): void {
    // Remove quest from list
    const currentQuests = this.todayQuests();
    if (currentQuests.status === 'loaded') {
      this.todayQuests.set({
        status: 'loaded',
        data: currentQuests.data.filter(q => q.id !== questId),
      });
    }

    // Increment completed count
    const currentStats = this.dailyStats();
    if (currentStats.status === 'loaded') {
      this.dailyStats.set({
        status: 'loaded',
        data: { ...currentStats.data, questsCompleted: currentStats.data.questsCompleted + 1 },
      });
    }

    // Add XP
    const currentXp = this.xpProgress();
    if (currentXp.status === 'loaded') {
      this.xpProgress.set({
        status: 'loaded',
        data: { ...currentXp.data, currentXp: currentXp.data.currentXp + xpEarned },
      });
    }
  }

  private fetchAllSections(): void {
    // Uses forkJoin for parallel execution
    // Each section updates independently on success/error
  }

  private restoreCachedData(): void {
    // Reads last-known data from StorageService
    // Sets section state to 'loaded' with cached data if available
  }
}
```

---

## Dashboard Service

```typescript
@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly api = inject(ApiService);
  private readonly skipLoadingContext = new HttpContext().set(SKIP_LOADING, true);

  getUserSummary(): Observable<DashboardUserSummary> {
    return this.api.get<DashboardUserSummary>('/users/me/summary');
  }

  getXpProgress(): Observable<DashboardXpProgress> {
    return this.api.get<DashboardXpProgress>('/xp/progress');
  }

  getDailyStats(): Observable<DashboardDailyStats> {
    return this.api.get<DashboardDailyStats>('/stats/daily');
  }

  getActiveArc(): Observable<DashboardActiveArc | null> {
    return this.api.get<DashboardActiveArc | null>('/arcs/active');
  }

  getTodayQuests(): Observable<Quest[]> {
    return this.api.get<Quest[]>('/quests/today');
  }

  getLeaderboardPreview(): Observable<DashboardLeaderboardPreview> {
    return this.api.get<DashboardLeaderboardPreview>('/leagues/preview');
  }

  completeQuest(questId: string): Observable<{ xpEarned: number }> {
    return this.api.post<{ xpEarned: number }>('/quests/complete', { questId });
  }

  skipQuest(questId: string): Observable<void> {
    return this.api.post<void>('/quests/skip', { questId });
  }
}
```

---

## Utility Functions

### Time-Based Greeting

```typescript
/**
 * Returns a greeting string based on the current hour.
 * 5:00–11:59 → "Good Morning"
 * 12:00–16:59 → "Good Afternoon"
 * 17:00–4:59 → "Good Evening"
 */
export function getTimeBasedGreeting(displayName: string | null, hour?: number): string {
  const h = hour ?? new Date().getHours();
  let greeting: string;

  if (h >= 5 && h < 12) {
    greeting = 'Good Morning';
  } else if (h >= 12 && h < 17) {
    greeting = 'Good Afternoon';
  } else {
    greeting = 'Good Evening';
  }

  return displayName ? `${greeting}, ${displayName}` : greeting;
}
```

### Motivation Rotation

```typescript
const MOTIVATION_MESSAGES: string[] = [/* 20+ static messages */];
const LAST_MOTIVATION_INDEX_KEY = 'motivation_last_index';

/**
 * Selects the next motivation message, avoiding consecutive repeats.
 * Persists the selected index to storage for cross-session rotation.
 */
export function selectMotivationMessage(
  lastIndex: number | null,
  poolSize: number
): number {
  if (poolSize <= 1) return 0;

  let nextIndex: number;
  do {
    nextIndex = Math.floor(Math.random() * poolSize);
  } while (nextIndex === lastIndex);

  return nextIndex;
}
```

### XP Formatting

```typescript
/**
 * Formats XP values as "current / required XP" with locale number formatting.
 * Example: formatXpDisplay(2400, 3000) → "2,400 / 3,000 XP"
 */
export function formatXpDisplay(currentXp: number, requiredXp: number): string {
  return `${currentXp.toLocaleString()} / ${requiredXp.toLocaleString()} XP`;
}
```

---

## Error Handling

Each section operates independently with its own `SectionState<T>` signal:

1. **Initial load failure**: Section shows skeleton → transitions to error state with retry button
2. **Partial failure**: Only the failed section shows error; other sections render normally
3. **Total failure**: All sections in error → full-page error overlay with "Try Again"
4. **Connectivity recovery**: `ConnectivityService.isOnline` signal triggers automatic retry of errored sections via `effect()`

```typescript
// In DashboardPage
private setupConnectivityWatcher(): void {
  effect(() => {
    if (this.connectivity.isOnline()) {
      this.store.retryErroredSections();
    }
  });
}
```

---

## Caching Strategy

- On successful data load, each section's data is persisted to `StorageService` under namespaced keys (e.g., `dashboard_xp_progress`)
- On next dashboard init, cached data is restored immediately (avoiding skeletons) while fresh data is fetched in background
- When fresh data arrives, signals update reactively — no full re-render needed due to OnPush + signals

---

## Pull-to-Refresh Flow

1. User pulls down → `ion-refresher` fires `ionRefresh` event
2. `DashboardPage` calls `store.refreshDashboard()` — does NOT set loading states (existing data stays visible)
3. `forkJoin` fetches all endpoints in parallel
4. On completion (success or error), calls `event.target.complete()` to dismiss refresher
5. Store signals update → template re-renders affected sections

---

## Leaderboard Lazy Loading

The leaderboard preview data is fetched separately from the above-the-fold content:

```typescript
// In DashboardPage
private readonly viewportLoaded = signal(false);

ngAfterViewInit(): void {
  // Delay leaderboard fetch until above-the-fold content renders
  setTimeout(() => {
    this.store.loadLeaderboardPreview();
    this.viewportLoaded.set(true);
  }, 0);
}
```

---

## File Structure

```
features/dashboard/
├── dashboard.page.ts
├── dashboard.page.html
├── dashboard.page.scss
├── dashboard.page.spec.ts
├── components/
│   ├── header-section/
│   │   ├── header-section.component.ts
│   │   ├── header-section.component.html
│   │   └── header-section.component.scss
│   ├── xp-progress-card/
│   │   ├── xp-progress-card.component.ts
│   │   ├── xp-progress-card.component.html
│   │   └── xp-progress-card.component.scss
│   ├── daily-summary/
│   │   ├── daily-summary.component.ts
│   │   ├── daily-summary.component.html
│   │   └── daily-summary.component.scss
│   ├── mini-card/
│   │   ├── mini-card.component.ts
│   │   ├── mini-card.component.html
│   │   └── mini-card.component.scss
│   ├── active-arc-section/
│   │   ├── active-arc-section.component.ts
│   │   ├── active-arc-section.component.html
│   │   └── active-arc-section.component.scss
│   ├── quest-list/
│   │   ├── quest-list.component.ts
│   │   ├── quest-list.component.html
│   │   └── quest-list.component.scss
│   ├── motivation-widget/
│   │   ├── motivation-widget.component.ts
│   │   ├── motivation-widget.component.html
│   │   └── motivation-widget.component.scss
│   └── leaderboard-preview/
│       ├── leaderboard-preview.component.ts
│       ├── leaderboard-preview.component.html
│       └── leaderboard-preview.component.scss
├── models/
│   └── dashboard.models.ts
├── services/
│   └── dashboard.service.ts
├── state/
│   └── dashboard.store.ts
└── utils/
    ├── greeting.util.ts
    ├── motivation.util.ts
    └── xp-format.util.ts
```

---

## Testing Strategy

- **Property-based tests**: Cover pure utility functions (greeting, XP formatting, motivation rotation) and store logic (isLoading computation, quest completion atomicity, section error isolation). Use fast-check with 100+ iterations per property.
- **Unit tests**: Verify component rendering (correct shared components receive correct inputs), swipe action wiring, navigation calls, and haptic triggers.
- **Integration tests**: Verify parallel API fetching on init, pull-to-refresh flow, connectivity recovery auto-retry, and cache restore behavior.
- **Edge case tests**: Empty quest list, no active arc, all sections failing, null user name during loading.

---

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system — essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Time-based greeting correctness

*For any* hour value in the range [0, 23], `getTimeBasedGreeting` SHALL return "Good Morning" for hours 5–11, "Good Afternoon" for hours 12–16, and "Good Evening" for hours 17–4. When a non-null display name is provided, the greeting SHALL be suffixed with ", {displayName}".

**Validates: Requirements 2.1, 2.5**

### Property 2: XP display formatting

*For any* pair of non-negative integers (currentXp, requiredXp), `formatXpDisplay` SHALL produce a string containing both values formatted with locale-appropriate thousand separators, separated by " / ", and suffixed with " XP".

**Validates: Requirements 3.3**

### Property 3: Mini-card data completeness

*For any* `DashboardDailyStats` object with valid numeric fields, the daily summary rendering SHALL produce exactly four mini-cards, each containing an icon identifier, a numeric value, and a descriptive label string.

**Validates: Requirements 4.1, 4.2, 4.3**

### Property 4: Quest list capping

*For any* list of quests with length N, the displayed quest list SHALL contain `min(N, 8)` items. When N > 8, a "View All Quests" link SHALL be present; when N ≤ 8, it SHALL be absent.

**Validates: Requirements 6.8**

### Property 5: Motivation rotation avoids consecutive repeats

*For any* last-displayed index and a pool of size ≥ 2, `selectMotivationMessage` SHALL return an index different from the last-displayed index, and the returned index SHALL be within bounds [0, poolSize).

**Validates: Requirements 7.2, 7.5**

### Property 6: Skeleton visibility depends on cache and loading state

*For any* section, IF the section state is 'loading' AND no cached data exists, THEN the skeleton placeholder SHALL be visible. IF cached data exists (state is 'loaded' from cache), THEN the skeleton SHALL NOT be visible regardless of whether a background refresh is in progress.

**Validates: Requirements 9.4, 10.1, 10.7**

### Property 7: Independent section error isolation

*For any* combination of section states where at least one section has status 'error' and at least one has status 'loaded', the loaded sections SHALL render their data normally and only the errored sections SHALL display an error state with a retry action.

**Validates: Requirements 11.1, 11.2**

### Property 8: isLoading computed signal correctness

*For any* combination of section loading states, the `isLoading` computed signal SHALL return `true` if and only if at least one section signal has status 'loading'.

**Validates: Requirements 12.2**

### Property 9: Quest completion atomic multi-signal update

*For any* quest completion event with a given questId and xpEarned value, after `completeQuest` is called: (1) the todayQuests signal SHALL no longer contain a quest with that ID, (2) the dailyStats.questsCompleted SHALL be incremented by exactly 1, and (3) the xpProgress.currentXp SHALL be incremented by exactly xpEarned.

**Validates: Requirements 12.5**

### Property 10: Leaderboard preview data completeness

*For any* valid `DashboardLeaderboardPreview` object, the rendered leaderboard preview SHALL display the user's rank position, XP total, and league name, and SHALL render exactly 3 top entries.

**Validates: Requirements 8.1, 8.2**

### Property 11: Quest card renders all required fields

*For any* quest in the displayed list, the rendered quest item SHALL include the quest title, XP reward value, difficulty badge, and stat type icon.

**Validates: Requirements 6.2**
