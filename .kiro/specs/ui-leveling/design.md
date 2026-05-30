# Design Document: UI Leveling

## Overview

The Level-Up Celebration UI is a self-contained feature module that delivers an immersive, sequential celebration experience when a user levels up in Ascend. It operates independently of the quest completion flow, listening for level-up events from any source (API responses, WebSocket) and orchestrating a full-screen overlay with Lottie animations, Angular Animations, rewards display, feature unlock announcements, and a prestige system at Level 100.

## Architecture

The Level-Up Celebration UI is a self-contained feature module under `frontend/src/features/leveling/` following the project's feature-first architecture. It consists of a singleton orchestration service (`LevelUpService`), a full-screen overlay component tree, an HTTP interceptor for API-based level detection, and a WebSocket subscription for server-pushed level events.

The system operates as a **sequential celebration state machine** — events arrive from two sources (API interceptor, WebSocket), are deduplicated and queued, then processed one-at-a-time through a multi-step animation pipeline rendered in a full-screen overlay.

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Event Sources                                 │
│  ┌──────────────────┐          ┌──────────────────────────────┐     │
│  │ HTTP Interceptor │          │ WebSocket /user/{id}/queue/level │  │
│  │ (newLevel field) │          │ (LEVEL_UP message)               │  │
│  └────────┬─────────┘          └──────────────┬──────────────────┘  │
│           │                                    │                     │
│           └──────────────┬─────────────────────┘                    │
│                          ▼                                          │
│              ┌───────────────────────┐                              │
│              │   LevelUpService      │                              │
│              │   (Singleton, Root)   │                              │
│              │                       │                              │
│              │  • Deduplication      │                              │
│              │  • Queue Management   │                              │
│              │  • Multi-level Split  │                              │
│              │  • State Machine      │                              │
│              └───────────┬───────────┘                              │
│                          ▼                                          │
│              ┌───────────────────────┐                              │
│              │ CelebrationOverlay    │                              │
│              │ (Full-screen, CDK)    │                              │
│              │                       │                              │
│              │  ├─ GlowExplosion     │                              │
│              │  ├─ LevelTitle        │                              │
│              │  ├─ XpFlyUpNumber     │                              │
│              │  ├─ RewardsCard       │                              │
│              │  ├─ FeatureUnlock     │                              │
│              │  ├─ ContinueButton    │                              │
│              │  └─ PrestigeScreen    │                              │
│              └───────────────────────┘                              │
└─────────────────────────────────────────────────────────────────────┘
```

## Components and Interfaces

All components are **standalone Angular components** using signals for reactive state binding. They follow the existing project pattern (see `quest-completion` feature).

### File Structure

```
frontend/src/features/leveling/
├── services/
│   └── level-up.service.ts
├── interceptors/
│   └── level-up.interceptor.ts
├── components/
│   ├── celebration-overlay/
│   │   ├── celebration-overlay.component.ts
│   │   ├── celebration-overlay.component.html
│   │   ├── celebration-overlay.component.scss
│   │   └── celebration-overlay.animations.ts
│   ├── glow-explosion/
│   │   ├── glow-explosion.component.ts
│   │   └── glow-explosion.component.html
│   ├── xp-fly-up-number/
│   │   ├── xp-fly-up-number.component.ts
│   │   └── xp-fly-up-number.component.scss
│   ├── rewards-card/
│   │   ├── rewards-card.component.ts
│   │   ├── rewards-card.component.html
│   │   └── rewards-card.component.scss
│   ├── feature-unlock-announcement/
│   │   ├── feature-unlock-announcement.component.ts
│   │   └── feature-unlock-announcement.component.scss
│   └── prestige-screen/
│       ├── prestige-screen.component.ts
│       ├── prestige-screen.component.html
│       └── prestige-screen.component.scss
├── models/
│   └── level-up.models.ts
├── utils/
│   ├── milestone-config.ts
│   └── celebration-queue.ts
└── index.ts
```

## Data Models

```typescript
// models/level-up.models.ts

/** Raw event from WebSocket or API response */
export interface LevelUpEvent {
  userId: string;
  previousLevel: number;
  newLevel: number;
  rewards: LevelReward[];
  unlockedFeatures: string[];
}

/** Individual reward item */
export interface LevelReward {
  type: 'coins' | 'title' | 'cosmetic' | 'xp_multiplier';
  name: string;
  amount?: number;
  value?: string;
}

/** Milestone feature unlock configuration */
export interface MilestoneConfig {
  level: number;
  featureName: string;
  tagline: string;
  icon: string;
}

/** Single celebration in the queue (one per level gained) */
export interface CelebrationItem {
  level: number;
  rewards: LevelReward[];
  isMilestone: boolean;
  milestoneConfig: MilestoneConfig | null;
  queuePosition: number;
  queueTotal: number;
}

/** State machine states for the celebration flow */
export type CelebrationStep =
  | 'idle'
  | 'waiting-for-quest-animation'
  | 'glow-explosion'
  | 'level-title'
  | 'xp-fly-up'
  | 'rewards-card'
  | 'feature-unlock'
  | 'prestige-prompt'
  | 'prestige-screen'
  | 'continue-ready'
  | 'dismissing';

/** Full celebration flow state */
export interface CelebrationFlowState {
  step: CelebrationStep;
  currentItem: CelebrationItem | null;
  queue: CelebrationItem[];
  error: string | null;
}

/** Prestige activation data */
export interface PrestigeData {
  previousPrestigeLevel: number;
  newPrestigeLevel: number;
  badgeId: string;
  badgeName: string;
}
```

## LevelUpService Design

The `LevelUpService` is a singleton (`providedIn: 'root'`) that mirrors the `QuestCompletionService` pattern — signal-based state, preloaded animation assets, and a public read-only API.

```typescript
// services/level-up.service.ts

@Injectable({ providedIn: 'root' })
export class LevelUpService implements OnDestroy {
  private readonly storageService = inject(StorageService);
  private readonly questCompletionService = inject(QuestCompletionService);

  // ─── Signal-Based State ─────────────────────────────────────────
  private readonly _flowState = signal<CelebrationFlowState>({
    step: 'idle',
    currentItem: null,
    queue: [],
    error: null,
  });

  /** Public read-only flow state */
  readonly flowState = this._flowState.asReadonly();

  /** Whether a celebration is currently active */
  readonly celebrationActive = computed(() => this._flowState().step !== 'idle');

  /** Current celebration step for UI binding */
  readonly currentStep = computed(() => this._flowState().step);

  /** Current celebration item for UI binding */
  readonly currentItem = computed(() => this._flowState().currentItem);

  /** Queue indicator (e.g., "1 of 3") */
  readonly queueIndicator = computed(() => {
    const item = this._flowState().currentItem;
    if (!item || item.queueTotal <= 1) return null;
    return `${item.queuePosition} of ${item.queueTotal}`;
  });

  // ─── Deduplication State ────────────────────────────────────────
  private lastProcessedLevel: number;

  // ─── Preloaded Assets ───────────────────────────────────────────
  private glowExplosionData: unknown = null;
  private prestigeAnimationData: unknown = null;

  // ─── WebSocket Subscription ─────────────────────────────────────
  private wsSubscription: Subscription | null = null;

  constructor() {
    this.lastProcessedLevel = this.loadLastProcessedLevel();
    this.preloadGlowAnimation();
  }

  // ─── Public API ─────────────────────────────────────────────────

  /** Called by the interceptor or WebSocket handler when a level-up is detected */
  triggerLevelUp(event: LevelUpEvent): void { /* ... */ }

  /** Advance to the next celebration step (called by overlay component) */
  advanceStep(): void { /* ... */ }

  /** Dismiss current celebration and process next in queue */
  dismiss(): void { /* ... */ }

  /** Activate prestige system (Level 100 only) */
  activatePrestige(): void { /* ... */ }

  /** Connect to WebSocket level channel */
  connectWebSocket(userId: string): void { /* ... */ }

  /** Disconnect from WebSocket level channel */
  disconnectWebSocket(): void { /* ... */ }

  ngOnDestroy(): void {
    this.disconnectWebSocket();
  }
}
```

### State Machine Transitions

```
idle
  │── triggerLevelUp(event) ──→ waiting-for-quest-animation (if quest flow active)
  │                          ──→ glow-explosion (if no quest flow)
  │
waiting-for-quest-animation
  │── quest animation completes ──→ glow-explosion
  │
glow-explosion
  │── 750ms (50% progress) ──→ level-title (parallel, title appears)
  │── 1125ms (75% progress) ──→ xp-fly-up (parallel, number appears)
  │── 1500ms (complete) ──→ rewards-card
  │
rewards-card
  │── slide-up complete ──→ feature-unlock (if milestone)
  │                      ──→ continue-ready (if not milestone)
  │
feature-unlock
  │── displayed ──→ prestige-prompt (if level 100)
  │             ──→ continue-ready (if not level 100)
  │
prestige-prompt
  │── user activates prestige ──→ prestige-screen
  │── user declines ──→ continue-ready
  │
prestige-screen
  │── "Begin New Journey" tapped ──→ idle (navigate to dashboard)
  │
continue-ready
  │── "Continue Journey" tapped ──→ dismissing
  │
dismissing
  │── fade-out complete (300ms) ──→ idle (after 500ms delay, process next queue item)
```

### Deduplication Logic

```typescript
/** Returns true if the event should be processed, false if duplicate */
shouldProcessEvent(event: LevelUpEvent): boolean {
  return event.newLevel > this.lastProcessedLevel;
}

/** Persist last processed level to survive page refreshes */
private persistLastProcessedLevel(level: number): void {
  this.lastProcessedLevel = level;
  localStorage.setItem('ascend_last_level', String(level));
}

private loadLastProcessedLevel(): number {
  const stored = localStorage.getItem('ascend_last_level');
  return stored ? parseInt(stored, 10) : 0;
}
```

### Multi-Level Jump Decomposition

```typescript
/** Decompose a multi-level jump into individual celebration items */
decomposeLevelJump(event: LevelUpEvent): CelebrationItem[] {
  const levels = event.newLevel - event.previousLevel;
  const items: CelebrationItem[] = [];

  for (let i = 1; i <= levels; i++) {
    const level = event.previousLevel + i;
    const milestoneConfig = getMilestoneConfig(level);
    items.push({
      level,
      rewards: getRewardsForLevel(level, event.rewards),
      isMilestone: milestoneConfig !== null,
      milestoneConfig,
      queuePosition: i,
      queueTotal: levels,
    });
  }

  return items;
}
```

## HTTP Interceptor for Level Detection

```typescript
// interceptors/level-up.interceptor.ts

export const levelUpInterceptor: HttpInterceptorFn = (req, next) => {
  const levelUpService = inject(LevelUpService);

  return next(req).pipe(
    tap((event) => {
      if (event instanceof HttpResponse && event.body) {
        const body = event.body as Record<string, unknown>;
        if ('newLevel' in body && typeof body['newLevel'] === 'number') {
          const levelUpEvent: LevelUpEvent = {
            userId: '', // extracted from auth context
            previousLevel: (body['previousLevel'] as number) ?? 0,
            newLevel: body['newLevel'] as number,
            rewards: (body['rewards'] as LevelReward[]) ?? [],
            unlockedFeatures: (body['unlockedFeatures'] as string[]) ?? [],
          };
          levelUpService.triggerLevelUp(levelUpEvent);
        }
      }
    })
  );
};
```

## WebSocket Integration

The service subscribes to `/user/{userId}/queue/level` using the existing RxStomp infrastructure (STOMP over SockJS). The subscription is established on user authentication and torn down on logout.

```typescript
// Inside LevelUpService

connectWebSocket(userId: string): void {
  this.disconnectWebSocket();
  this.wsSubscription = this.rxStomp
    .watch(`/user/${userId}/queue/level`)
    .pipe(
      map((message) => JSON.parse(message.body) as { type: string; data: LevelUpEvent }),
      filter((msg) => msg.type === 'LEVEL_UP'),
      map((msg) => msg.data)
    )
    .subscribe((event) => this.triggerLevelUp(event));
}

disconnectWebSocket(): void {
  this.wsSubscription?.unsubscribe();
  this.wsSubscription = null;
}
```

## Animation Pipeline

### Lottie Integration (Glow Explosion + Prestige)

Lottie animations are rendered via `ngx-lottie` with preloaded JSON data to eliminate loading delay.

```typescript
// glow-explosion.component.ts

@Component({
  selector: 'app-glow-explosion',
  standalone: true,
  imports: [LottieComponent],
  template: `
    <ng-lottie
      [options]="lottieOptions()"
      (animationCreated)="onAnimationCreated($event)"
    />
  `,
})
export class GlowExplosionComponent {
  private readonly levelUpService = inject(LevelUpService);

  lottieOptions = computed(() => ({
    animationData: this.levelUpService.glowExplosionData,
    loop: false,
    autoplay: true,
  }));

  /** Emit progress events for coordinating other animations */
  @Output() progress = new EventEmitter<number>();
  @Output() complete = new EventEmitter<void>();

  onAnimationCreated(animation: AnimationItem): void {
    animation.addEventListener('enterFrame', () => {
      const progress = animation.currentFrame / animation.totalFrames;
      this.progress.emit(progress);
    });
    animation.addEventListener('complete', () => this.complete.emit());
  }
}
```

### Angular Animations (XP Fly-Up, Rewards Card, Feature Unlock)

All Angular Animations use only `transform` and `opacity` for GPU-accelerated compositing.

```typescript
// celebration-overlay.animations.ts

export const flyUpAnimation = trigger('flyUp', [
  transition(':enter', [
    style({ transform: 'translateY(40px)', opacity: 0 }),
    animate('200ms ease-out', style({ transform: 'translateY(0)', opacity: 1 })),
  ]),
]);

export const slideUpAnimation = trigger('slideUp', [
  transition(':enter', [
    style({ transform: 'translateY(100%)', opacity: 0 }),
    animate('400ms ease-out', style({ transform: 'translateY(0)', opacity: 1 })),
  ]),
]);

export const scaleInAnimation = trigger('scaleIn', [
  transition(':enter', [
    style({ transform: 'scale(0)', opacity: 0 }),
    animate('300ms ease-out', style({ transform: 'scale(1)', opacity: 1 })),
  ]),
]);

export const fadeInAnimation = trigger('fadeIn', [
  transition(':enter', [
    style({ opacity: 0 }),
    animate('200ms ease-out', style({ opacity: 1 })),
  ]),
]);

export const fadeOutAnimation = trigger('fadeOut', [
  transition(':leave', [
    animate('300ms ease-out', style({ opacity: 0 })),
  ]),
]);

export const staggeredFadeIn = trigger('staggeredFadeIn', [
  transition(':enter', [
    query(':enter', [
      style({ opacity: 0, transform: 'translateY(10px)' }),
      stagger('100ms', [
        animate('200ms ease-out', style({ opacity: 1, transform: 'translateY(0)' })),
      ]),
    ], { optional: true }),
  ]),
]);
```

## Celebration Overlay Component

```typescript
// celebration-overlay.component.ts

@Component({
  selector: 'app-celebration-overlay',
  standalone: true,
  imports: [
    GlowExplosionComponent,
    XpFlyUpNumberComponent,
    RewardsCardComponent,
    FeatureUnlockAnnouncementComponent,
    PrestigeScreenComponent,
  ],
  animations: [fadeInAnimation, fadeOutAnimation],
  host: {
    'role': 'dialog',
    'aria-modal': 'true',
    'aria-label': 'Level up celebration',
    '(keydown.escape)': 'onEscapeKey()',
    '[class.will-change-active]': 'animating()',
  },
})
export class CelebrationOverlayComponent implements OnInit, OnDestroy {
  private readonly levelUpService = inject(LevelUpService);

  readonly step = this.levelUpService.currentStep;
  readonly currentItem = this.levelUpService.currentItem;
  readonly queueIndicator = this.levelUpService.queueIndicator;

  /** Track whether animations are running for will-change optimization */
  readonly animating = computed(() => {
    const s = this.step();
    return s !== 'idle' && s !== 'continue-ready';
  });

  private previouslyFocusedElement: HTMLElement | null = null;

  ngOnInit(): void {
    this.previouslyFocusedElement = document.activeElement as HTMLElement;
    this.announceLevel();
  }

  ngOnDestroy(): void {
    this.previouslyFocusedElement?.focus();
  }

  onGlowProgress(progress: number): void {
    if (progress >= 0.5) this.levelUpService.advanceStep(); // show title
    if (progress >= 0.75) this.levelUpService.advanceStep(); // show fly-up
  }

  onGlowComplete(): void {
    this.levelUpService.advanceStep(); // transition to rewards-card
  }

  onContinue(): void {
    this.levelUpService.dismiss();
  }

  onEscapeKey(): void {
    if (this.step() === 'continue-ready') {
      this.levelUpService.dismiss();
    }
  }

  private announceLevel(): void {
    const item = this.currentItem();
    if (item) {
      // aria-live="assertive" region updated in template
    }
  }
}
```

## Prestige Screen Flow

The prestige screen is a separate full-screen component that replaces the celebration overlay when the user activates prestige at Level 100.

```typescript
// prestige-screen.component.ts

@Component({
  selector: 'app-prestige-screen',
  standalone: true,
  imports: [LottieComponent],
  animations: [fadeInAnimation],
})
export class PrestigeScreenComponent {
  private readonly levelUpService = inject(LevelUpService);
  private readonly router = inject(Router);

  readonly prestigeData = this.levelUpService.prestigeData;

  /** Lottie options loaded on-demand (not preloaded) */
  lottieOptions = signal<AnimationOptions | null>(null);

  constructor() {
    this.loadPrestigeAnimation();
  }

  onBeginNewJourney(): void {
    this.levelUpService.dismiss();
    this.router.navigate(['/dashboard']);
  }

  private async loadPrestigeAnimation(): Promise<void> {
    const data = await import('../../../../assets/animations/prestige-ascend.json');
    this.lottieOptions.set({
      animationData: data.default ?? data,
      loop: false,
      autoplay: true,
    });
  }
}
```

## Milestone Configuration

```typescript
// utils/milestone-config.ts

export const MILESTONE_CONFIGS: MilestoneConfig[] = [
  {
    level: 10,
    featureName: 'Leagues Unlocked',
    tagline: 'Compete with players at your level',
    icon: 'trophy-outline',
  },
  {
    level: 25,
    featureName: 'Guilds Unlocked',
    tagline: 'Join forces with other players',
    icon: 'people-outline',
  },
  {
    level: 50,
    featureName: 'Elite Cosmetics Unlocked',
    tagline: 'Exclusive visual upgrades await',
    icon: 'diamond-outline',
  },
  {
    level: 100,
    featureName: 'Prestige System Unlocked',
    tagline: 'Reset and ascend to legendary status',
    icon: 'star-outline',
  },
];

/** Returns milestone config for a level, or null if not a milestone */
export function getMilestoneConfig(level: number): MilestoneConfig | null {
  return MILESTONE_CONFIGS.find((m) => m.level === level) ?? null;
}

/** Check if a level is a milestone level */
export function isMilestoneLevel(level: number): boolean {
  return MILESTONE_CONFIGS.some((m) => m.level === level);
}
```

## Performance Considerations

### Preloading Strategy

| Asset | Load Timing | Rationale |
|-------|-------------|-----------|
| Glow Explosion Lottie | Service initialization | Used for every level-up; must be instant |
| Prestige Lottie | On-demand at Level 100 | Rare event; saves memory for 99% of users |
| Angular Animation metadata | Compile-time | Zero runtime cost |

### GPU Acceleration

- All animations use **only** `transform` and `opacity` — these are compositor-only properties that avoid layout/paint
- `will-change: transform, opacity` is applied to animated elements **during** the celebration and removed after to avoid permanent GPU memory allocation
- Lottie renders to a `<canvas>` element which is inherently GPU-composited

### DOM Cleanup

- The `CelebrationOverlayComponent` is conditionally rendered with `@if (celebrationActive())` — when dismissed, it is fully removed from the DOM
- No persistent DOM nodes exist between celebrations
- Lottie animation instances are destroyed on component teardown

### Memory Management

- Preloaded Lottie JSON is held as a single reference in the service (shared across all celebrations)
- Queue items are lightweight data objects (no DOM references)
- WebSocket subscription uses a single RxStomp watch — no per-event subscriptions

## Error Handling

| Scenario | Handling |
|----------|----------|
| Lottie asset fails to load | Skip glow animation, proceed directly to rewards card |
| WebSocket disconnects | Rely on API interceptor as primary source; reconnect via RxStomp auto-reconnect |
| Invalid LevelUpEvent data | Validate `newLevel > 0` and `newLevel > previousLevel`; discard invalid events |
| localStorage unavailable | Fall back to in-memory deduplication (loses cross-refresh protection) |
| Quest completion service unavailable | Skip wait, proceed with celebration immediately |

## Testing Strategy

### Unit Tests (Example-Based)
- Service initialization and singleton behavior
- WebSocket subscription/unsubscription lifecycle
- Individual animation timing configurations (200ms, 300ms, 400ms, 1500ms)
- Specific milestone content (Level 10 → "Leagues Unlocked", etc.)
- Prestige screen activation and navigation
- Accessibility attributes (aria-labels, aria-live regions)
- Escape key dismissal behavior
- GPU-only animation properties (transform + opacity)

### Property-Based Tests
- Event deduplication across random event sequences
- Multi-level jump decomposition for arbitrary level ranges
- State machine transition ordering for all event configurations
- Queue processing order preservation
- Milestone detection bidirectionality across all levels 1–100
- Rewards rendering completeness for arbitrary reward lists
- LocalStorage round-trip for last processed level

### Integration Tests
- HTTP interceptor detecting `newLevel` in real API response shapes
- WebSocket message parsing and celebration triggering
- Coordination with QuestCompletionService (wait for animation)
- Full celebration flow end-to-end with mocked timers

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system — essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: API response level detection triggers celebration

*For any* HTTP response body containing a `newLevel` field with a numeric value greater than the last processed level, the `LevelUpService` SHALL trigger exactly one celebration flow.

**Validates: Requirements 1.3**

### Property 2: Event queue preserves sequential processing order

*For any* sequence of N LevelUp events arriving while a celebration is in progress, all N events SHALL be queued and processed sequentially in arrival order, with exactly one celebration active at any time.

**Validates: Requirements 1.5, 8.5**

### Property 3: celebrationActive signal reflects overlay state

*For any* celebration flow lifecycle, the `celebrationActive` signal SHALL be `true` from the moment the celebration begins until the overlay is fully dismissed, and `false` at all other times.

**Validates: Requirements 1.6**

### Property 4: Celebration state machine executes steps in correct order

*For any* LevelUp event (milestone or non-milestone), the celebration state machine SHALL transition through steps in the defined sequential order: glow-explosion → rewards-card → feature-unlock (if milestone) → continue-ready, never skipping or reordering mandatory steps.

**Validates: Requirements 2.4, 6.1, 6.8**

### Property 5: Level number display format

*For any* valid level number N (1 ≤ N ≤ 100), the XP fly-up component SHALL render the text exactly as "Level {N}".

**Validates: Requirements 4.2**

### Property 6: Rewards card displays all reward items

*For any* non-empty list of rewards in a CelebrationItem, the Rewards Card SHALL render every reward with its type icon, name, and amount/value — the count of rendered items SHALL equal the count of rewards in the event data.

**Validates: Requirements 5.2**

### Property 7: Staggered reward animation timing

*For any* list of N rewards, the Nth reward item SHALL appear with a delay of (N-1) × 100ms relative to the first item's appearance.

**Validates: Requirements 5.5**

### Property 8: Milestone level detection is bidirectional

*For any* level number, the Feature Unlock Announcement step SHALL appear if and only if the level equals one of the milestone values (10, 25, 50, 100). For all other levels, the step SHALL be skipped.

**Validates: Requirements 6.1, 6.8**

### Property 9: Milestone content correctness

*For any* milestone level, the Feature Unlock Announcement SHALL display the feature name and tagline that correspond to that specific milestone level in the configuration map.

**Validates: Requirements 6.2**

### Property 10: Prestige text format

*For any* prestige level number P (P ≥ 1), the Prestige Screen SHALL render the text exactly as "Prestige {P}".

**Validates: Requirements 7.4**

### Property 11: Continue button appears only after all steps complete

*For any* celebration flow configuration (with or without milestone, with or without prestige), the "Continue Journey" button SHALL appear only after all applicable preceding steps have completed.

**Validates: Requirements 8.1**

### Property 12: Focus trap and restoration

*For any* element that held focus before the celebration overlay appeared, focus SHALL be trapped within the overlay during display and restored to that original element after dismissal.

**Validates: Requirements 9.1**

### Property 13: Aria-live level announcement format

*For any* level number N, the aria-live assertive region SHALL contain the text "Level up! You reached level {N}" when the overlay appears.

**Validates: Requirements 9.3**

### Property 14: Rewards aria-label completeness

*For any* list of rewards, the Rewards Card aria-label SHALL contain a description of every reward in the list.

**Validates: Requirements 9.4**

### Property 15: DOM cleanup after dismissal

*For any* celebration dismissal, the overlay component SHALL be fully removed from the DOM, leaving zero celebration-related elements in the document.

**Validates: Requirements 10.4**

### Property 16: will-change lifecycle management

*For any* celebration flow, the `will-change` CSS hint SHALL be present on animated elements during animation and removed after all animations complete.

**Validates: Requirements 10.6**

### Property 17: Deduplication discards stale events

*For any* LevelUp event where `newLevel` is less than or equal to the last processed level, the service SHALL discard the event without triggering a celebration or modifying the queue.

**Validates: Requirements 11.2**

### Property 18: Last processed level persistence round-trip

*For any* level that is successfully processed, persisting it to localStorage and then reading it back SHALL yield the same level value.

**Validates: Requirements 11.5**

### Property 19: Multi-level jump decomposition count

*For any* LevelUp event where `newLevel - previousLevel = N` (N > 1), the service SHALL generate exactly N individual CelebrationItems, one for each level from `previousLevel + 1` to `newLevel` inclusive.

**Validates: Requirements 12.1**

### Property 20: Multi-level celebrations process in ascending order

*For any* multi-level jump, the generated CelebrationItems SHALL be ordered by level in strictly ascending order, and processed in that order.

**Validates: Requirements 12.2**

### Property 21: Multi-level milestone inclusion

*For any* multi-level jump from level A to level B that crosses a milestone level M (A < M ≤ B), the CelebrationItem for level M SHALL have `isMilestone = true` and include the correct MilestoneConfig.

**Validates: Requirements 12.4**

### Property 22: Queue position indicator correctness

*For any* multi-level jump of N levels, each CelebrationItem at position K SHALL display the indicator "K of N" where 1 ≤ K ≤ N.

**Validates: Requirements 12.5**
