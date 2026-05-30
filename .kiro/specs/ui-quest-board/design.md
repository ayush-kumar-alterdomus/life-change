# Design Document

## Overview

The Quest Board is rebuilt as a standalone Angular/Ionic smart page component following the feature-first architecture. It orchestrates tab-based quest filtering, a bottom-sheet detail modal, an enhanced create-quest form, and a free-user limit gate. The design leverages Angular signals for reactive state, the existing shared `app-quest-card` dumb component, and the existing `QuestService` for API communication.

## Architecture

### Component Hierarchy

```
QuestBoardPage (smart)
├── ProgressSummaryComponent (dumb)
├── IonSegment (Tab_Bar: Daily | Weekly | Custom)
├── QuestListComponent (dumb)
│   └── QuestCardComponent (shared dumb - app-quest-card)
├── QuestDetailSheetComponent (dumb - bottom sheet)
├── CreateQuestFormComponent (dumb - modal)
└── UpgradePromptComponent (dumb - modal)
```

## Components and Interfaces

### QuestBoardPage (Smart Component)

**Path:** `src/features/quests/pages/quest-board/quest-board.page.ts`

The orchestrating smart component. Manages state via signals, coordinates API calls through `QuestService`, and delegates rendering to dumb components.

```typescript
@Component({
  standalone: true,
  selector: 'app-quest-board',
  templateUrl: './quest-board.page.html',
  styleUrls: ['./quest-board.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    IonContent, IonHeader, IonToolbar, IonTitle, IonSegment, IonSegmentButton,
    IonLabel, IonFab, IonFabButton, IonIcon, IonRefresher, IonRefresherContent,
    IonSkeletonText, IonToast,
    ProgressSummaryComponent,
    QuestListComponent,
    QuestDetailSheetComponent,
    CreateQuestFormComponent,
    UpgradePromptComponent,
  ],
})
export class QuestBoardPage implements OnInit {
  private readonly questService = inject(QuestService);
  private readonly userStore = inject(UserStore);
  private readonly destroyRef = inject(DestroyRef);

  // State signals
  allQuests = signal<Quest[]>([]);
  activeTab = signal<QuestFrequency>(QuestFrequency.Daily);
  loading = signal(true);
  selectedQuest = signal<Quest | null>(null);
  showCreateForm = signal(false);
  showUpgradePrompt = signal(false);

  // Computed signals
  filteredQuests = computed(() =>
    filterQuestsByFrequency(this.allQuests(), this.activeTab())
  );

  progressSummary = computed(() =>
    computeProgress(this.allQuests())
  );

  canCreateQuest = computed(() =>
    canUserCreateQuest(this.userStore.isPremium(), this.activeCustomQuestCount())
  );

  activeCustomQuestCount = computed(() =>
    countActiveCustomQuests(this.allQuests())
  );
}
```

### ProgressSummaryComponent (Dumb)

**Path:** `src/features/quests/components/progress-summary/progress-summary.component.ts`

Displays daily completion progress with a progress bar.

```typescript
@Component({
  standalone: true,
  selector: 'app-progress-summary',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProgressSummaryComponent {
  completed = input.required<number>();
  total = input.required<number>();
  ratio = computed(() => this.total() > 0 ? this.completed() / this.total() : 0);
}
```

### QuestListComponent (Dumb)

**Path:** `src/features/quests/components/quest-list/quest-list.component.ts`

Renders a list of quest cards with empty state handling.

```typescript
@Component({
  standalone: true,
  selector: 'app-quest-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [QuestCardComponent],
})
export class QuestListComponent {
  quests = input.required<Quest[]>();
  emptyMessage = input<string>('No quests available');
  questTapped = output<Quest>();
  questCompleted = output<Quest>();
  questSkipped = output<Quest>();
}
```

### QuestDetailSheetComponent (Dumb)

**Path:** `src/features/quests/components/quest-detail-sheet/quest-detail-sheet.component.ts`

Bottom-sheet modal displaying full quest details and action bar.

```typescript
@Component({
  standalone: true,
  selector: 'app-quest-detail-sheet',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class QuestDetailSheetComponent {
  quest = input.required<Quest>();
  isOpen = input.required<boolean>();

  complete = output<void>();
  edit = output<void>();
  skip = output<void>();
  dismissed = output<void>();
}
```

### CreateQuestFormComponent (Dumb)

**Path:** `src/features/quests/components/create-quest-form/create-quest-form.component.ts`

Enhanced modal form for custom quest creation with validation and XP calculation.

```typescript
@Component({
  standalone: true,
  selector: 'app-create-quest-form',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule],
})
export class CreateQuestFormComponent {
  isOpen = input.required<boolean>();

  created = output<CreateQuestPayload>();
  dismissed = output<void>();

  form = new FormGroup({
    title: new FormControl('', [Validators.required, Validators.minLength(3)]),
    description: new FormControl(''),
    difficulty: new FormControl<Difficulty | null>(null, Validators.required),
    statType: new FormControl<StatType | null>(null, Validators.required),
    frequency: new FormControl<QuestFrequency | null>(null, Validators.required),
    timeEstimate: new FormControl(''),
  });

  calculatedXp = computed(() => calculateXpFromDifficulty(this.form.get('difficulty')?.value));
}
```

### UpgradePromptComponent (Dumb)

**Path:** `src/features/quests/components/upgrade-prompt/upgrade-prompt.component.ts`

Modal displayed when free users hit the 5-quest limit.

```typescript
@Component({
  standalone: true,
  selector: 'app-upgrade-prompt',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UpgradePromptComponent {
  isOpen = input.required<boolean>();
  activeQuestCount = input.required<number>();
  maxQuests = input<number>(5);

  upgrade = output<void>();
  dismissed = output<void>();
}
```

## Data Models

### Extended Quest Interface

The existing `Quest` model from `shared/models/quest.model.ts` is sufficient. No changes needed:

```typescript
interface Quest {
  id: string;
  title: string;
  description?: string;
  xpReward: number;
  difficulty: Difficulty;
  statType: StatType;
  frequency: QuestFrequency;
  timeEstimate?: string;
  completed: boolean;
  completedAt?: Date;
  arcId?: string;
}
```

### CreateQuestPayload

Enhanced payload for quest creation:

```typescript
interface CreateQuestPayload {
  title: string;
  description?: string;
  difficulty: Difficulty;
  statType: StatType;
  frequency: QuestFrequency;
  timeEstimate?: string;
}
```

### ProgressSummary

```typescript
interface ProgressSummary {
  completed: number;
  total: number;
  ratio: number;
}
```

## Pure Logic Functions

These functions encapsulate the core business logic, making them independently testable:

### filterQuestsByFrequency

```typescript
function filterQuestsByFrequency(quests: Quest[], frequency: QuestFrequency): Quest[] {
  return quests.filter(q => q.frequency === frequency);
}
```

### computeProgress

```typescript
function computeProgress(quests: Quest[]): ProgressSummary {
  const dailyQuests = quests.filter(q => q.frequency === QuestFrequency.Daily);
  const completed = dailyQuests.filter(q => q.completed).length;
  const total = dailyQuests.length;
  return { completed, total, ratio: total > 0 ? completed / total : 0 };
}
```

### calculateXpFromDifficulty

```typescript
function calculateXpFromDifficulty(difficulty: Difficulty | null): number {
  switch (difficulty) {
    case Difficulty.Easy: return 25;
    case Difficulty.Medium: return 50;
    case Difficulty.Hard: return 100;
    case Difficulty.Legendary: return 200;
    default: return 0;
  }
}
```

### canUserCreateQuest

```typescript
function canUserCreateQuest(isPremium: boolean, activeCustomQuestCount: number): boolean {
  if (isPremium) return true;
  return activeCustomQuestCount < 5;
}
```

### countActiveCustomQuests

```typescript
function countActiveCustomQuests(quests: Quest[]): number {
  return quests.filter(q => q.frequency === QuestFrequency.Custom && !q.completed).length;
}
```

### validateCreateQuestForm

```typescript
interface ValidationResult {
  valid: boolean;
  errors: Record<string, string>;
}

function validateCreateQuestForm(payload: Partial<CreateQuestPayload>): ValidationResult {
  const errors: Record<string, string> = {};

  if (!payload.title || payload.title.trim().length < 3) {
    errors['title'] = 'Title is required and must be at least 3 characters';
  }
  if (!payload.difficulty) {
    errors['difficulty'] = 'Difficulty is required';
  }
  if (!payload.statType) {
    errors['statType'] = 'Stat type is required';
  }
  if (!payload.frequency) {
    errors['frequency'] = 'Frequency is required';
  }

  return { valid: Object.keys(errors).length === 0, errors };
}
```

## QuestService Extensions

The existing `QuestService` needs additional methods to support the new tabs:

```typescript
@Injectable({ providedIn: 'root' })
export class QuestService {
  // Existing methods remain unchanged

  getWeeklyQuests(): Observable<Quest[]> {
    return this.api.get<ApiResponse<Quest[]>>('/quests/weekly').pipe(map(res => res.data));
  }

  getCustomQuests(): Observable<Quest[]> {
    return this.api.get<ApiResponse<Quest[]>>('/quests/custom').pipe(map(res => res.data));
  }

  getAllQuests(): Observable<Quest[]> {
    return this.api.get<ApiResponse<Quest[]>>('/quests').pipe(map(res => res.data));
  }
}
```

## State Management

The page uses Angular signals for local reactive state. No global store changes are needed since quest state is page-scoped.

### Signal Flow

```
QuestService.getAllQuests() → allQuests signal
                                    ↓
                        activeTab signal (user interaction)
                                    ↓
                        filteredQuests computed (auto-derived)
                        progressSummary computed (auto-derived)
                        activeCustomQuestCount computed (auto-derived)
                        canCreateQuest computed (auto-derived)
```

### Tab Change Flow

```
User taps tab segment → activeTab.set(newFrequency)
                              ↓
                    filteredQuests recomputes (signal dependency)
                              ↓
                    QuestListComponent re-renders (OnPush + signal)
```

### Quest Completion Flow

```
User taps Complete → QuestService.completeQuest(id)
                          ↓
                    Update allQuests signal (mark quest completed)
                          ↓
                    progressSummary recomputes automatically
                    filteredQuests recomputes automatically
```

## Error Handling

| Scenario | Behavior |
|----------|----------|
| Quest load fails | Show error toast, retain loading skeleton |
| Quest complete fails (409) | Show "Already completed" warning toast |
| Quest complete fails (other) | Show "Failed to complete" danger toast |
| Quest create fails | Show "Failed to create" danger toast, keep form open |
| Refresh fails | Show error toast, retain previously loaded data |

## Bottom Sheet Implementation

The Quest Detail Sheet uses Ionic's `ion-modal` with `breakpoints` and `initialBreakpoint` for bottom-sheet behavior:

```typescript
// In template
<ion-modal
  [isOpen]="selectedQuest() !== null"
  [breakpoints]="[0, 0.5, 0.75]"
  [initialBreakpoint]="0.5"
  (didDismiss)="onDetailDismissed()">
  <app-quest-detail-sheet
    [quest]="selectedQuest()!"
    [isOpen]="selectedQuest() !== null"
    (complete)="onCompleteQuest()"
    (edit)="onEditQuest()"
    (skip)="onSkipQuest()"
    (dismissed)="onDetailDismissed()" />
</ion-modal>
```

## Testing Strategy

### Unit Tests (Example-Based)
- Component rendering: verify tab bar renders 3 tabs, default tab is Daily, skeleton loading state, empty state messages
- UI interactions: FAB opens create form, quest card tap opens detail sheet, detail sheet dismiss behavior
- Action flows: complete quest updates state, create quest closes modal and shows toast
- Edge cases: empty quest list per tab, refresh failure retains data, 5-quest limit boundary

### Property-Based Tests
- Pure logic functions (`filterQuestsByFrequency`, `computeProgress`, `calculateXpFromDifficulty`, `canUserCreateQuest`, `countActiveCustomQuests`, `validateCreateQuestForm`) are tested with property-based tests using randomized inputs (minimum 100 iterations per property)
- Generators produce random Quest arrays with varied frequencies, completion states, and field values

### Integration Tests
- QuestService API calls return expected data shapes
- End-to-end tab switching with real service responses

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system — essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Tab frequency filter returns only matching quests

*For any* list of quests with mixed frequencies and *for any* selected frequency tab (Daily, Weekly, or Custom), the `filterQuestsByFrequency` function SHALL return only quests whose frequency matches the selected tab, and no quests with a different frequency SHALL be included in the result.

**Validates: Requirements 1.2, 1.3, 1.4**

### Property 2: Tab state preservation across refresh

*For any* selected tab state, after a pull-to-refresh operation completes (success or failure), the active tab signal SHALL remain equal to the tab that was selected before the refresh was triggered.

**Validates: Requirements 1.6**

### Property 3: Quest detail sheet displays all quest fields

*For any* valid Quest object, when displayed in the Quest Detail Sheet, the rendered output SHALL contain the quest's title, description, XP reward, difficulty level, stat type, time estimate, and frequency.

**Validates: Requirements 3.2**

### Property 4: XP reward calculation from difficulty

*For any* Difficulty enum value, the `calculateXpFromDifficulty` function SHALL return a positive integer XP value, and the mapping SHALL be deterministic (same difficulty always produces same XP).

**Validates: Requirements 4.3**

### Property 5: Form validation reports errors for exactly the missing required fields

*For any* partial CreateQuestPayload where one or more required fields (title, difficulty, statType, frequency) are missing or invalid, the `validateCreateQuestForm` function SHALL return `valid: false` and the `errors` object SHALL contain keys for exactly the fields that are missing or invalid, with no extra or missing error keys.

**Validates: Requirements 4.5**

### Property 6: Quest limit gate logic

*For any* boolean `isPremium` and *for any* non-negative integer `activeCustomQuestCount`, the `canUserCreateQuest` function SHALL return `true` when `isPremium` is true (regardless of count), and SHALL return `true` when `isPremium` is false AND `activeCustomQuestCount < 5`, and SHALL return `false` when `isPremium` is false AND `activeCustomQuestCount >= 5`.

**Validates: Requirements 5.2, 5.3, 5.5**

### Property 7: Progress computation correctness

*For any* list of quests, the `computeProgress` function SHALL return a `completed` count equal to the number of daily quests with `completed === true`, a `total` count equal to the total number of daily quests, and a `ratio` equal to `completed / total` (or 0 when total is 0).

**Validates: Requirements 6.1, 6.2**

### Property 8: Active custom quest count accuracy

*For any* list of quests, the `countActiveCustomQuests` function SHALL return a count equal to the number of quests where `frequency === QuestFrequency.Custom` AND `completed === false`.

**Validates: Requirements 5.2, 5.3**
