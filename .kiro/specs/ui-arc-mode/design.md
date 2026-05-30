# Design Document

## Overview

This document defines the technical architecture for the Arc Mode UI feature — a set of Angular 20+ standalone components implementing the Arc browsing, detail, and creation experience. The design follows the project's feature-first architecture with smart pages + dumb components, signals-based state management, and Ionic UI primitives.

## Architecture

The feature is structured as a lazy-loaded Angular feature module under `frontend/src/features/arc-mode/` with three smart page components orchestrating data flow through an `ArcStore` (signals) backed by an `ArcService` (HTTP). Dumb components handle presentation logic for the milestone timeline, phase progress, rewards, skill tree preview, and identity title.

```
features/arc-mode/
├── arc-mode.routes.ts              # Lazy-loaded route definitions
├── pages/
│   ├── arc-list/                   # Smart page: tabbed arc browsing
│   ├── arc-detail/                 # Smart page: full arc view
│   └── arc-create/                 # Smart page: custom arc form
├── components/
│   ├── cinematic-banner/           # Dumb: hero banner with parallax
│   ├── phase-progress/             # Dumb: 4-phase stepper visualization
│   ├── milestone-timeline/         # Dumb: phase-grouped accordion
│   ├── boss-section/               # Dumb: boss encounter display
│   ├── rewards-section/            # Dumb: rewards list by phase
│   ├── skill-tree-preview/         # Dumb: horizontal node preview
│   └── identity-title/             # Dumb: evolving title display
├── services/
│   └── arc.service.ts              # HTTP communication layer
└── store/
    └── arc.store.ts                # Signals-based reactive state
```

## Components and Interfaces

### Smart Pages

#### ArcListPage

Orchestrates the tabbed arc browsing experience. Reads from `ArcStore` computed signals and delegates rendering to `app-arc-card`.

```typescript
@Component({
  standalone: true,
  selector: 'app-arc-list',
  templateUrl: './arc-list.page.html',
  styleUrls: ['./arc-list.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule, IonicModule, ArcCardComponent,
    SkeletonLoaderComponent, ErrorStateComponent,
  ],
})
export class ArcListPage implements OnInit {
  private readonly arcStore = inject(ArcStore);
  private readonly router = inject(Router);

  readonly selectedTab = signal<'explore' | 'my-arcs' | 'completed'>('explore');
  readonly prebuiltArcs = this.arcStore.prebuiltArcs;
  readonly activeArcs = this.arcStore.activeArcs;
  readonly completedArcs = this.arcStore.completedArcs;
  readonly loading = this.arcStore.loadingList;
  readonly error = this.arcStore.listError;

  ngOnInit(): void {
    this.arcStore.loadArcsIfEmpty();
    this.selectedTab.set(this.arcStore.activeArcs().length > 0 ? 'my-arcs' : 'explore');
  }

  onArcTap(arcId: string): void {
    this.router.navigate(['/arc-mode', arcId]);
  }

  onCreateArc(): void {
    this.router.navigate(['/arc-mode', 'create']);
  }

  onRetry(): void {
    this.arcStore.loadArcs();
  }
}
```

#### ArcDetailPage

Fetches arc detail by route param, orchestrates all detail sub-components.

```typescript
@Component({
  standalone: true,
  selector: 'app-arc-detail',
  templateUrl: './arc-detail.page.html',
  styleUrls: ['./arc-detail.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule, IonicModule, CinematicBannerComponent,
    PhaseProgressComponent, MilestoneTimelineComponent,
    BossSectionComponent, RewardsSectionComponent,
    SkillTreePreviewComponent, IdentityTitleComponent,
    SkeletonLoaderComponent, ErrorStateComponent,
  ],
})
export class ArcDetailPage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly arcStore = inject(ArcStore);
  private readonly router = inject(Router);

  readonly arcDetail = this.arcStore.selectedArcDetail;
  readonly loading = this.arcStore.loadingDetail;
  readonly error = this.arcStore.detailError;

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.arcStore.loadArcDetail(id);
  }

  onBack(): void {
    this.router.navigate(['/arc-mode']);
  }

  onRetry(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.arcStore.loadArcDetail(id);
  }

  onSkillTreeTap(): void {
    const arcId = this.arcDetail()?.id;
    this.router.navigate(['/skill-tree'], { queryParams: { arcId } });
  }
}
```

#### ArcCreatePage

Single-page reactive form for custom arc creation with inline validation.

```typescript
@Component({
  standalone: true,
  selector: 'app-arc-create',
  templateUrl: './arc-create.page.html',
  styleUrls: ['./arc-create.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, IonicModule, ReactiveFormsModule],
})
export class ArcCreatePage {
  private readonly arcStore = inject(ArcStore);
  private readonly router = inject(Router);
  private readonly toastCtrl = inject(ToastController);

  readonly submitting = this.arcStore.loadingCreate;
  readonly serverError = this.arcStore.createError;

  readonly form = new FormGroup({
    title: new FormControl('', [Validators.required, Validators.maxLength(100)]),
    goal: new FormControl('', [Validators.required, Validators.maxLength(500)]),
    durationDays: new FormControl<number | null>(null, [
      Validators.required, Validators.min(30), Validators.max(90),
    ]),
    milestones: new FormArray([new FormControl('', Validators.required)]),
    questFrequency: new FormControl('', Validators.required),
  });

  get milestones(): FormArray {
    return this.form.get('milestones') as FormArray;
  }

  addMilestone(): void {
    this.milestones.push(new FormControl('', Validators.required));
  }

  removeMilestone(index: number): void {
    if (this.milestones.length > 1) {
      this.milestones.removeAt(index);
    }
  }

  async onSubmit(): Promise<void> {
    if (this.form.invalid) return;
    const result = await this.arcStore.createArc(this.form.getRawValue());
    if (result) {
      const toast = await this.toastCtrl.create({
        message: 'Arc created!', duration: 2000, color: 'success',
      });
      await toast.present();
      this.router.navigate(['/arc-mode', result.id]);
    }
  }
}
```

### Dumb Components

#### CinematicBannerComponent

Full-width hero banner with parallax scroll effect.

```typescript
@Component({
  standalone: true,
  selector: 'app-cinematic-banner',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CinematicBannerComponent {
  arcName = input.required<string>();
  arcType = input.required<ArcType>();
  currentPhase = input.required<string>();
  progressPercentage = input.required<number>();
  scrollOffset = input<number>(0);

  ariaLabel = computed(() =>
    `${this.arcName()}, ${this.currentPhase()} phase, ${this.progressPercentage()}% complete`
  );

  parallaxTransform = computed(() =>
    `translateY(${this.scrollOffset() * 0.5}px)`
  );
}
```

#### PhaseProgressComponent

Wraps the shared `app-stepper` to display the 4-phase arc progression.

```typescript
@Component({
  standalone: true,
  selector: 'app-phase-progress',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [StepperComponent],
})
export class PhaseProgressComponent {
  static readonly PHASES = ['Beginner', 'Intermediate', 'Elite', 'Master'];

  currentPhase = input.required<string>();

  currentStepIndex = computed(() => {
    const idx = PhaseProgressComponent.PHASES.indexOf(this.currentPhase());
    return idx >= 0 ? idx : 0;
  });

  completedCount = computed(() => this.currentStepIndex());

  ariaLabel = computed(() =>
    `Phase progress: currently in ${this.currentPhase()}, ${this.completedCount()} of 4 phases completed`
  );
}
```

#### MilestoneTimelineComponent

Phase-grouped accordion with expand/collapse behavior.

```typescript
@Component({
  standalone: true,
  selector: 'app-milestone-timeline',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MilestoneTimelineComponent {
  phases = input.required<ArcPhaseWithMilestones[]>();
  currentPhase = input.required<string>();

  expandedPhases = signal<Set<string>>(new Set());

  constructor() {
    effect(() => {
      this.expandedPhases.set(new Set([this.currentPhase()]));
    });
  }

  togglePhase(phaseName: string): void {
    this.expandedPhases.update(set => {
      const next = new Set(set);
      next.has(phaseName) ? next.delete(phaseName) : next.add(phaseName);
      return next;
    });
  }

  isExpanded(phaseName: string): boolean {
    return this.expandedPhases().has(phaseName);
  }

  getCompletionSummary(phase: ArcPhaseWithMilestones): string {
    const completed = phase.milestones.filter(m => m.completed).length;
    return `${completed}/${phase.milestones.length} milestones`;
  }
}
```

#### BossSectionComponent

Wraps the shared `app-boss-card` and `app-xp-progress-bar`.

```typescript
@Component({
  standalone: true,
  selector: 'app-boss-section',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [BossCardComponent, XpProgressBarComponent],
})
export class BossSectionComponent {
  boss = input.required<Boss | null>();

  hasBoss = computed(() => this.boss() !== null);
  isDefeated = computed(() => this.boss()?.healthPercentage === 0);
}
```

#### RewardsSectionComponent

Displays rewards grouped by phase with earned/locked visual states.

```typescript
@Component({
  standalone: true,
  selector: 'app-rewards-section',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RewardsSectionComponent {
  rewards = input.required<ArcReward[]>();

  groupedRewards = computed(() => {
    const groups = new Map<string, ArcReward[]>();
    for (const reward of this.rewards()) {
      const phase = reward.unlocksAtPhase;
      if (!groups.has(phase)) groups.set(phase, []);
      groups.get(phase)!.push(reward);
    }
    return groups;
  });

  getRewardAriaLabel(reward: ArcReward): string {
    const status = reward.earned ? 'earned' : `locked, unlocks at ${reward.unlocksAtPhase} phase`;
    return `Reward: ${reward.name}, ${status}`;
  }
}
```

#### SkillTreePreviewComponent

Horizontal scrollable preview limited to 6 nodes.

```typescript
@Component({
  standalone: true,
  selector: 'app-skill-tree-preview',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SkillTreePreviewComponent {
  nodes = input.required<SkillNode[]>();
  navigate = output<void>();

  displayNodes = computed(() => this.nodes().slice(0, 6));
  hasMore = computed(() => this.nodes().length > 6);
  unlockedCount = computed(() => this.nodes().filter(n => n.unlocked).length);

  ariaLabel = computed(() =>
    `Skill tree preview, ${this.unlockedCount()} of ${this.nodes().length} skills unlocked, tap to view full tree`
  );

  onTap(): void {
    this.navigate.emit();
  }
}
```

#### IdentityTitleComponent

Displays the evolving identity title with phase-based mapping.

```typescript
@Component({
  standalone: true,
  selector: 'app-identity-title',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class IdentityTitleComponent {
  currentPhase = input.required<string>();
  titles = input.required<IdentityTitleMap>();
  arcType = input.required<ArcType>();

  currentTitle = computed(() => {
    const map = this.titles();
    const phase = this.currentPhase();
    return map[phase] ?? map['Beginner'];
  });

  ariaLabel = computed(() => `Your arc identity: ${this.currentTitle()}`);
}
```

## Data Models

### Frontend Interfaces

```typescript
// Extended arc detail model for the detail page
export interface ArcDetail extends Arc {
  milestones: ArcMilestoneDetail[];
  boss: Boss | null;
  rewards: ArcReward[];
  skillTreeNodes: SkillNode[];
  identityTitles: IdentityTitleMap;
  questFrequency: string;
}

export interface ArcMilestoneDetail {
  id: string;
  title: string;
  description: string;
  completed: boolean;
  xpReward: number;
  phase: string;
  orderIndex: number;
}

export interface ArcPhaseWithMilestones {
  name: string;
  order: number;
  milestones: ArcMilestoneDetail[];
}
```

```typescript
export interface ArcReward {
  id: string;
  name: string;
  type: 'xp' | 'title' | 'cosmetic' | 'coins';
  earned: boolean;
  unlocksAtPhase: string;
}

export interface SkillNode {
  id: string;
  name: string;
  unlocked: boolean;
  order: number;
}

export interface IdentityTitleMap {
  [phase: string]: string;
  Beginner: string;
  Intermediate: string;
  Elite: string;
  Master: string;
}

export interface CreateArcPayload {
  title: string;
  goal: string;
  durationDays: number;
  milestones: string[];
  questFrequency: string;
}
```

## Services

### ArcService

HTTP communication layer. Never called directly from components.

```typescript
@Injectable({ providedIn: 'root' })
export class ArcService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/arcs`;

  getAvailableArcs(): Observable<ApiResponse<ArcResponse[]>> {
    return this.http.get<ApiResponse<ArcResponse[]>>(this.baseUrl);
  }

  getActiveArcs(): Observable<ApiResponse<ArcProgressResponse>> {
    return this.http.get<ApiResponse<ArcProgressResponse>>(`${this.baseUrl}/active`);
  }

  getArcDetail(id: string): Observable<ApiResponse<ArcDetailResponse>> {
    return this.http.get<ApiResponse<ArcDetailResponse>>(`${this.baseUrl}/${id}`);
  }

  getProgress(arcId: string): Observable<ApiResponse<ArcProgressResponse>> {
    return this.http.get<ApiResponse<ArcProgressResponse>>(
      `${this.baseUrl}/progress`, { params: { arcId } }
    );
  }
```

```typescript
  completeMilestone(arcId: string, milestoneId: string): Observable<ApiResponse<ArcProgressResponse>> {
    return this.http.patch<ApiResponse<ArcProgressResponse>>(
      `${this.baseUrl}/progress`, { arcId, milestoneId }
    );
  }

  createArc(payload: CreateArcPayload): Observable<ApiResponse<ArcDetailResponse>> {
    return this.http.post<ApiResponse<ArcDetailResponse>>(this.baseUrl, payload);
  }

  startArc(arcId: string): Observable<ApiResponse<ArcProgressResponse>> {
    return this.http.post<ApiResponse<ArcProgressResponse>>(
      `${this.baseUrl}/start`, { arcId }
    );
  }
}
```

## Store

### ArcStore

Signals-based reactive state management with cache-first strategy and optimistic updates.

```typescript
@Injectable({ providedIn: 'root' })
export class ArcStore {
  private readonly arcService = inject(ArcService);

  // --- Core state signals ---
  private readonly _arcs = signal<Arc[]>([]);
  private readonly _activeProgress = signal<ArcProgressResponse[]>([]);
  private readonly _selectedDetail = signal<ArcDetail | null>(null);

  // --- Loading signals (per operation) ---
  readonly loadingList = signal(false);
  readonly loadingDetail = signal(false);
  readonly loadingCreate = signal(false);

  // --- Error signals ---
  readonly listError = signal<string | null>(null);
  readonly detailError = signal<string | null>(null);
  readonly createError = signal<string | null>(null);

  // --- Computed filtered lists ---
  readonly prebuiltArcs = computed(() =>
    this._arcs().filter(arc => arc.isPrebuilt)
  );

  readonly activeArcs = computed(() =>
    this._arcs().filter(arc => arc.startedAt && !arc.completedAt)
  );

  readonly completedArcs = computed(() =>
    this._arcs().filter(arc => arc.completedAt !== undefined && arc.completedAt !== null)
  );

  readonly selectedArcDetail = this._selectedDetail.asReadonly();
```

```typescript
  // --- Actions ---

  /** Cache-first: only fetches if arcs signal is empty */
  loadArcsIfEmpty(): void {
    if (this._arcs().length > 0) return;
    this.loadArcs();
  }

  loadArcs(): void {
    this.loadingList.set(true);
    this.listError.set(null);
    this.arcService.getAvailableArcs().subscribe({
      next: (res) => {
        this._arcs.set(res.data);
        this.loadingList.set(false);
      },
      error: (err) => {
        this.listError.set(err.message ?? 'Failed to load arcs');
        this.loadingList.set(false);
      },
    });
  }

  loadArcDetail(id: string): void {
    this.loadingDetail.set(true);
    this.detailError.set(null);
    this._selectedDetail.set(null);
    this.arcService.getArcDetail(id).subscribe({
      next: (res) => {
        this._selectedDetail.set(res.data);
        this.loadingDetail.set(false);
      },
      error: (err) => {
        this.detailError.set(err.message ?? 'Failed to load arc detail');
        this.loadingDetail.set(false);
      },
    });
  }

  /** Optimistic milestone completion with rollback on failure */
  completeMilestone(arcId: string, milestoneId: string): void {
    const previousDetail = this._selectedDetail();
    if (!previousDetail) return;

    // Optimistic update
    const updatedDetail = this.applyMilestoneCompletion(previousDetail, milestoneId);
    this._selectedDetail.set(updatedDetail);

    this.arcService.completeMilestone(arcId, milestoneId).subscribe({
      next: (res) => {
        // Reconcile with server response
        this._selectedDetail.update(detail =>
          detail ? { ...detail, progressPercentage: res.data.progressPercent } : detail
        );
      },
      error: () => {
        // Revert optimistic update
        this._selectedDetail.set(previousDetail);
      },
    });
  }
```

```typescript
  async createArc(payload: CreateArcPayload): Promise<ArcDetail | null> {
    this.loadingCreate.set(true);
    this.createError.set(null);
    try {
      const res = await firstValueFrom(this.arcService.createArc(payload));
      this._arcs.update(arcs => [...arcs, res.data]);
      this.loadingCreate.set(false);
      return res.data;
    } catch (err: any) {
      this.createError.set(err.error?.message ?? 'Failed to create arc');
      this.loadingCreate.set(false);
      return null;
    }
  }

  private applyMilestoneCompletion(detail: ArcDetail, milestoneId: string): ArcDetail {
    return {
      ...detail,
      milestones: detail.milestones.map(m =>
        m.id === milestoneId ? { ...m, completed: true } : m
      ),
    };
  }
}
```

## Routing

```typescript
// arc-mode.routes.ts
import { Routes } from '@angular/router';

export const ARC_MODE_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/arc-list/arc-list.page').then(m => m.ArcListPage),
  },
  {
    path: 'create',
    loadComponent: () =>
      import('./pages/arc-create/arc-create.page').then(m => m.ArcCreatePage),
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./pages/arc-detail/arc-detail.page').then(m => m.ArcDetailPage),
  },
];
```

## Error Handling

| Scenario | Behavior |
|----------|----------|
| Arc list fetch fails | Show `error-state` with retry button; `listError` signal populated |
| Arc detail fetch fails | Show `error-state` with retry button; `detailError` signal populated |
| Milestone completion fails | Revert optimistic update; show toast with error message |
| Arc creation fails (validation) | Display server error below relevant form field |
| Arc creation fails (limit) | Display premium upgrade prompt |
| Network offline | Handled by global interceptor; offline banner shown |

## Accessibility

- **Tablist pattern**: `ion-segment` uses `role="tablist"`, each segment button has `role="tab"` and `aria-selected`
- **Accordion pattern**: Milestone timeline headers use `aria-expanded` and `aria-controls` linking to panel IDs
- **Form associations**: Every form input has a `<label>` with `for` attribute; validation errors use `aria-describedby`
- **Semantic headings**: h1 for arc name, h2 for section titles (Milestones, Boss, Rewards, Skill Tree)
- **Touch targets**: All interactive elements meet 44×44px minimum
- **Screen reader labels**: Cinematic banner, phase progress, boss section, rewards, skill tree preview, and identity title all expose computed `aria-label` attributes

## Performance Considerations

- **Lazy loading**: All three pages are lazy-loaded via `loadComponent`
- **OnPush**: All components use `ChangeDetectionStrategy.OnPush`
- **Cache-first**: `loadArcsIfEmpty()` prevents redundant API calls on back navigation
- **Skeleton loading**: Immediate visual feedback while data loads
- **Signal-based reactivity**: Fine-grained updates without zone.js overhead
- **Parallax**: Uses CSS `transform: translateY()` for GPU-accelerated scrolling

## Testing Strategy

- **Property-based tests**: Validate universal properties of the ArcStore filtering logic, form validation, phase classification, milestone grouping, and optimistic update/revert behavior using generated inputs (minimum 100 iterations per property)
- **Unit tests**: Verify specific component rendering (skeleton loaders shown during loading, error-state shown on failure, navigation on card tap), form submission flow, and API service method calls
- **Integration tests**: Verify end-to-end page behavior with mocked HTTP (list page loads and displays arcs, detail page fetches by route param, create form submits and navigates)

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system — essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Arc store computed signals correctly filter arcs by category

*For any* list of arcs in the store, the `prebuiltArcs` computed signal SHALL contain only arcs where `isPrebuilt` is true, the `activeArcs` computed signal SHALL contain only arcs with a `startedAt` date and no `completedAt` date, and the `completedArcs` computed signal SHALL contain only arcs with a non-null `completedAt` date. The union of these filtered lists SHALL NOT contain any arc that does not belong to its respective category.

**Validates: Requirements 1.2, 1.4, 11.2**

### Property 2: Default tab selection follows active arc presence

*For any* state of the arc store, when the Arc List Page initializes, the default selected tab SHALL be "my-arcs" if `activeArcs` has at least one element, and "explore" if `activeArcs` is empty.

**Validates: Requirements 1.5**

### Property 3: Empty state message matches selected tab context

*For any* tab selection where the corresponding arc list is empty, the Arc List Page SHALL display an empty state message. The message content SHALL be contextually appropriate to the selected tab (explore, my-arcs, or completed).

**Validates: Requirements 1.7**

### Property 4: Phase progress correctly classifies phases based on current phase

*For any* valid current phase value (Beginner, Intermediate, Elite, or Master), the Phase Progress component SHALL classify all phases with a lower order index as "completed", the phase matching the current phase as "active", and all phases with a higher order index as "upcoming". The aria-label SHALL accurately report the current phase name and the count of completed phases.

**Validates: Requirements 4.3, 4.5**

### Property 5: Milestone timeline groups milestones by phase with active phase expanded

*For any* set of milestones with phase assignments and a given current phase, the Milestone Timeline SHALL produce exactly one accordion section per phase, each section containing only milestones belonging to that phase. The section matching the current phase SHALL be expanded by default, and all other sections SHALL be collapsed.

**Validates: Requirements 5.1, 5.2**

### Property 6: Milestone items display correct completion state and summary counts

*For any* milestone in the timeline, the completion indicator SHALL be a checkmark (✓) if `completed` is true and a circle (○) if `completed` is false. For any phase section header, the completion summary SHALL display the exact count of completed milestones over the total milestone count for that phase.

**Validates: Requirements 5.4, 5.5, 5.6**

### Property 7: Boss section visibility follows boss presence

*For any* arc detail, the Boss Section SHALL be rendered if and only if the arc has a non-null boss. When rendered, the boss name, level, and health percentage SHALL match the boss data.

**Validates: Requirements 6.1, 6.5**

### Property 8: Rewards display correct earned/locked state grouped by phase

*For any* set of arc rewards, the Rewards Section SHALL group rewards by their `unlocksAtPhase` value. Each reward SHALL display at full opacity with a success checkmark if `earned` is true, and at 50% opacity with a lock icon if `earned` is false. The aria-label for each reward SHALL contain the reward name and its earned/locked status.

**Validates: Requirements 7.1, 7.2, 7.3, 7.5**

### Property 9: Skill tree preview limits displayed nodes and reports correct counts

*For any* list of skill tree nodes, the Skill Tree Preview SHALL display at most 6 nodes. The aria-label SHALL report the correct count of unlocked nodes out of the total node count (not just the displayed count). A "View Full Tree" indicator SHALL appear if and only if the total node count exceeds 6.

**Validates: Requirements 8.2, 8.5, 8.6**

### Property 10: Form validation rejects invalid inputs and controls button state

*For any* form state in the Arc Creation Form: the title field SHALL reject empty strings and strings exceeding 100 characters; the goal field SHALL reject empty strings and strings exceeding 500 characters; the duration field SHALL reject values below 30 or above 90; the milestones list SHALL reject removal when only one milestone remains. The "Create Arc" submit button SHALL be disabled if and only if the form is invalid OR a submission is in progress.

**Validates: Requirements 9.2, 9.3, 9.4, 9.5, 9.12**

### Property 11: Identity title maps correctly to current phase

*For any* arc with an identity title map and a current phase, the Identity Title component SHALL display the title corresponding to the current phase key. The aria-label SHALL follow the format "Your arc identity: {title}" where {title} is the resolved title for the current phase.

**Validates: Requirements 10.2, 10.6**

### Property 12: Cache-first strategy prevents redundant fetches

*For any* state where the arc store's arcs signal is non-empty, calling `loadArcsIfEmpty()` SHALL NOT trigger an HTTP request. Only when the arcs signal is empty SHALL the method initiate a fetch.

**Validates: Requirements 11.3, 12.5**

### Property 13: Optimistic milestone update reverts on API failure

*For any* arc detail state and milestone completion attempt, the store SHALL immediately mark the milestone as completed (optimistic update). If the API request fails, the store SHALL revert the arc detail to its exact previous state, with the milestone marked as not completed.

**Validates: Requirements 11.4, 11.6**

### Property 14: Cinematic banner aria-label contains arc name, phase, and progress

*For any* arc detail data, the Cinematic Banner's aria-label SHALL contain the arc name, the current phase name, and the progress percentage formatted as "{name}, {phase} phase, {progress}% complete".

**Validates: Requirements 3.4, 14.4**
