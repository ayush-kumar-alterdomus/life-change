# Design Document: Quest Completion UI Flow

## Overview

This design document describes the architecture for the Quest Completion UI flow — a shared orchestration layer that delivers an immersive, multi-step reward experience when a user completes a quest. The flow includes a confirmation bottom-sheet, celebratory animations (Lottie particle burst + Angular Animations glow), XP progress bar transitions, haptic feedback, silent duplicate handling, and a cinematic Perfect Day bonus overlay.

## Architecture

The Quest Completion UI flow is implemented as a **shared orchestration service** (`QuestCompletionService`) that coordinates a multi-step reward experience. Any screen (Dashboard, Quest Board, etc.) can invoke it to deliver a consistent, immersive completion flow without duplicating logic.

The architecture follows the existing feature-first, signal-based pattern:

```
Calling Component (Dashboard/Quest Board)
    ↓ completeQuest(quest)
QuestCompletionService (orchestrator)
    ↓ opens
ConfirmationSheetComponent (bottom-sheet)
    ↓ user confirms
QuestService.completeQuest(id) (API call)
    ↓ success response
RewardAnimationLayerComponent (overlay)
    ↓ simultaneously
XP Progress Bar Fill Animation
    ↓ all daily quests done?
PerfectDayOverlayComponent (cinematic overlay)
```

## Components and Interfaces

### Component Tree

```
QuestCompletionService (root singleton)
├── ConfirmationSheetComponent (standalone, OnPush)
├── RewardAnimationLayerComponent (standalone, OnPush)
│   └── Lottie Player (particle burst)
│   └── XP Float Text (Angular Animation)
└── PerfectDayOverlayComponent (standalone, OnPush)
    └── Lottie Player (confetti loop)
    └── Stat Counter Animations
```

### File Structure

```
frontend/src/features/quest-completion/
├── services/
│   └── quest-completion.service.ts
├── components/
│   ├── confirmation-sheet/
│   │   ├── confirmation-sheet.component.ts
│   │   ├── confirmation-sheet.component.html
│   │   └── confirmation-sheet.component.scss
│   ├── reward-animation-layer/
│   │   ├── reward-animation-layer.component.ts
│   │   ├── reward-animation-layer.component.html
│   │   └── reward-animation-layer.component.scss
│   └── perfect-day-overlay/
│       ├── perfect-day-overlay.component.ts
│       ├── perfect-day-overlay.component.html
│       └── perfect-day-overlay.component.scss
├── animations/
│   ├── reward.animations.ts
│   └── perfect-day.animations.ts
├── models/
│   └── quest-completion.models.ts
└── utils/
    └── xp-calculations.ts
```

## Data Models

```typescript
// quest-completion.models.ts

import { Quest } from '@shared/models/quest.model';

/** Result emitted after a successful quest completion flow */
export interface QuestCompletionResult {
  questId: string;
  questTitle: string;
  xpEarned: number;
  completedAt: string;
  message: string;
  isPerfectDay: boolean;
}

/** Internal state tracked by the orchestration service */
export interface CompletionFlowState {
  status: 'idle' | 'confirming' | 'submitting' | 'animating' | 'perfect-day' | 'complete';
  quest: Quest | null;
  error: string | null;
}

/** Configuration for the XP fill animation */
export interface XpFillAnimationConfig {
  previousXp: number;
  newXp: number;
  levelThreshold: number;
  crossesLevel: boolean;
  overflowXp: number;
}

/** Stats displayed in the Perfect Day overlay */
export interface PerfectDayStats {
  totalQuestsCompleted: number;
  totalXpEarnedToday: number;
  currentStreak: number;
}


## Service Design

### QuestCompletionService

```typescript
// quest-completion.service.ts

import { Injectable, inject, signal, computed } from '@angular/core';
import { Observable, Subject, EMPTY, of } from 'rxjs';
import { switchMap, tap, catchError, finalize, filter } from 'rxjs/operators';
import { Quest } from '@shared/models/quest.model';
import { QuestService, QuestCompletionResponse } from '../../quests/services/quest.service';
import { HapticService } from '../../../core/services/haptic.service';
import { DashboardStore } from '../../dashboard/state/dashboard.store';
import { CompletionFlowState, QuestCompletionResult, PerfectDayStats, XpFillAnimationConfig } from '../models/quest-completion.models';

@Injectable({ providedIn: 'root' })
export class QuestCompletionService {
  private readonly questService = inject(QuestService);
  private readonly hapticService = inject(HapticService);
  private readonly dashboardStore = inject(DashboardStore);

  // ─── State ─────────────────────────────────────────────────────────────────

  private readonly _flowState = signal<CompletionFlowState>({
    status: 'idle',
    quest: null,
    error: null,
  });

  /** Public read-only flow state for UI binding */
  readonly flowState = this._flowState.asReadonly();

  /** Whether a completion flow is currently in progress */
  readonly isFlowActive = computed(() => this._flowState().status !== 'idle');

  /** Emits completion results for subscribers */
  private readonly _completionEvent$ = new Subject<QuestCompletionResult>();
  readonly completionEvent$ = this._completionEvent$.asObservable();

  /** Lottie animation data, preloaded on service init */
  private particleBurstData: unknown = null;
  private confettiData: unknown = null;

  constructor() {
    this.preloadAnimations();
  }

  // ─── Public API ────────────────────────────────────────────────────────────

  /**
   * Initiate the quest completion flow.
   * Returns an Observable that emits the result or EMPTY if flow is already active.
   */
  completeQuest(quest: Quest): Observable<QuestCompletionResult> {
    // Guard: ignore if flow already in progress
    if (this.isFlowActive()) {
      return EMPTY;
    }

    this._flowState.set({ status: 'confirming', quest, error: null });
    // The confirmation sheet will be opened by the component layer
    // listening to flowState changes.
    // Actual API call happens when user confirms (see onConfirm).
    return this._completionEvent$.pipe(
      filter(result => result.questId === quest.id)
    );
  }

  /**
   * Called when user confirms completion in the sheet.
   * Triggers haptic, calls API, orchestrates animations.
   */
  onConfirm(): void {
    const quest = this._flowState().quest;
    if (!quest) return;

    // Light haptic on tap
    this.hapticService.impact('light');

    this._flowState.set({ status: 'submitting', quest, error: null });

    this.questService.completeQuest(quest.id).pipe(
      tap((response) => this.handleSuccess(quest, response)),
      catchError((error) => this.handleError(quest, error))
    ).subscribe();
  }

  /**
   * Called when user cancels (cancel button, swipe down, escape key).
   */
  onCancel(): void {
    this._flowState.set({ status: 'idle', quest: null, error: null });
  }

  /**
   * Called when Perfect Day overlay is dismissed.
   */
  onPerfectDayDismiss(): void {
    this._flowState.set({ status: 'idle', quest: null, error: null });
  }

  /**
   * Called when reward animation completes.
   */
  onAnimationComplete(): void {
    const quest = this._flowState().quest;
    if (!quest) return;

    if (this.isDailyQuestSetComplete()) {
      this.hapticService.impact('heavy');
      this._flowState.set({ status: 'perfect-day', quest, error: null });
    } else {
      this._flowState.set({ status: 'idle', quest: null, error: null });
    }
  }

  // ─── XP Calculation Utilities ──────────────────────────────────────────────

  /**
   * Calculate XP fill animation configuration.
   * Determines if the fill crosses a level boundary and computes overflow.
   */
  calculateXpFillConfig(currentXp: number, xpEarned: number, levelThreshold: number): XpFillAnimationConfig {
    const newXp = currentXp + xpEarned;
    const crossesLevel = newXp >= levelThreshold;
    const overflowXp = crossesLevel ? newXp - levelThreshold : 0;

    return {
      previousXp: currentXp,
      newXp: crossesLevel ? levelThreshold : newXp,
      levelThreshold,
      crossesLevel,
      overflowXp,
    };
  }

  /**
   * Get Perfect Day stats from the dashboard store.
   */
  getPerfectDayStats(): PerfectDayStats {
    const stats = this.dashboardStore.dailyStats();
    const xpProgress = this.dashboardStore.xpProgress();

    return {
      totalQuestsCompleted: stats.status === 'loaded' ? stats.data.questsCompleted + 1 : 0,
      totalXpEarnedToday: xpProgress.status === 'loaded' ? xpProgress.data.currentXp : 0,
      currentStreak: stats.status === 'loaded' ? (stats.data as any).currentStreak ?? 0 : 0,
    };
  }

  // ─── Private Methods ───────────────────────────────────────────────────────

  private handleSuccess(quest: Quest, response: QuestCompletionResponse): void {
    // Success haptic
    this.hapticService.notification('success');

    // Update dashboard store
    this.dashboardStore.completeQuest(quest.id, response.xpEarned);

    // Transition to animating state
    this._flowState.set({ status: 'animating', quest, error: null });

    // Emit completion event
    const result: QuestCompletionResult = {
      questId: response.questId,
      questTitle: response.questTitle,
      xpEarned: response.xpEarned,
      completedAt: response.completedAt,
      message: response.message,
      isPerfectDay: this.isDailyQuestSetComplete(),
    };
    this._completionEvent$.next(result);
  }

  private handleError(quest: Quest, error: any): Observable<never> {
    const status = error?.status;

    if (status === 409) {
      // Duplicate completion — treat as success silently
      this.dashboardStore.completeQuest(quest.id, quest.xpReward);

      const result: QuestCompletionResult = {
        questId: quest.id,
        questTitle: quest.title,
        xpEarned: quest.xpReward,
        completedAt: new Date().toISOString(),
        message: 'Quest already completed',
        isPerfectDay: this.isDailyQuestSetComplete(),
      };
      this._completionEvent$.next(result);
      this._flowState.set({ status: 'idle', quest: null, error: null });
    } else {
      // Real error — show toast, keep sheet open for retry
      this._flowState.set({
        status: 'confirming',
        quest,
        error: 'Could not complete quest. Try again.',
      });
    }

    return EMPTY;
  }

  private isDailyQuestSetComplete(): boolean {
    const questsState = this.dashboardStore.todayQuests();
    if (questsState.status !== 'loaded') return false;
    // After completeQuest removes the quest, an empty list means all done
    return questsState.data.length === 0;
  }

  private preloadAnimations(): void {
    // Preload Lottie JSON data at service initialization
    // Implementation will use dynamic import or fetch for the animation JSON files
    import('../../../assets/animations/particle-burst.json')
      .then(data => this.particleBurstData = data)
      .catch(() => { /* graceful fallback — animation won't play */ });

    import('../../../assets/animations/confetti.json')
      .then(data => this.confettiData = data)
      .catch(() => { /* graceful fallback */ });
  }
}
```

## Component Designs

### ConfirmationSheetComponent

```typescript
// confirmation-sheet.component.ts

@Component({
  standalone: true,
  selector: 'app-confirmation-sheet',
  templateUrl: './confirmation-sheet.component.html',
  styleUrls: ['./confirmation-sheet.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, IonicModule],
  animations: [slideUpAnimation],
  host: {
    'role': 'dialog',
    'aria-label': 'Quest completion confirmation',
    'aria-modal': 'true',
    '(keydown.escape)': 'onEscapeKey()',
  },
})
export class ConfirmationSheetComponent {
  quest = input.required<Quest>();
  isSubmitting = input<boolean>(false);
  errorMessage = input<string | null>(null);

  confirm = output<void>();
  cancel = output<void>();

  /** Dynamic aria-label for the complete button */
  completeButtonAriaLabel = computed(() =>
    `Complete quest: ${this.quest().title}`
  );

  onConfirm(): void {
    this.confirm.emit();
  }

  onCancel(): void {
    this.cancel.emit();
  }

  onEscapeKey(): void {
    this.cancel.emit();
  }

  onSwipeDown(): void {
    this.cancel.emit();
  }
}
```

**Template structure:**
```html
<!-- confirmation-sheet.component.html -->
<div class="sheet-backdrop" (click)="onCancel()">
  <div class="sheet-container"
       (click)="$event.stopPropagation()"
       (swipedown)="onSwipeDown()"
       cdkTrapFocus
       cdkTrapFocusAutoCapture>

    <!-- Drag handle -->
    <div class="drag-handle" aria-hidden="true"></div>

    <!-- Quest details -->
    <div class="quest-details">
      <h2 class="quest-title">{{ quest().title }}</h2>
      <p class="quest-description">{{ quest().description }}</p>

      <div class="quest-meta">
        <span class="difficulty-badge" [attr.data-difficulty]="quest().difficulty">
          {{ quest().difficulty }}
        </span>
        <span class="stat-type">
          <ion-icon [name]="getStatIcon(quest().statType)"></ion-icon>
          {{ quest().statType }}
        </span>
      </div>

      <div class="xp-preview">
        <span class="xp-value">+{{ quest().xpReward }} XP</span>
      </div>
    </div>

    <!-- Error message (if any) -->
    @if (errorMessage()) {
      <div class="error-banner" role="alert">
        {{ errorMessage() }}
      </div>
    }

    <!-- Actions -->
    <button class="btn-complete"
            [attr.aria-label]="completeButtonAriaLabel()"
            [disabled]="isSubmitting()"
            (click)="onConfirm()">
      @if (isSubmitting()) {
        <ion-spinner name="crescent"></ion-spinner>
      } @else {
        Complete Quest
      }
    </button>

    <button class="btn-cancel"
            (click)="onCancel()"
            [disabled]="isSubmitting()">
      Cancel
    </button>
  </div>
</div>
```

### RewardAnimationLayerComponent

```typescript
// reward-animation-layer.component.ts

@Component({
  standalone: true,
  selector: 'app-reward-animation-layer',
  templateUrl: './reward-animation-layer.component.html',
  styleUrls: ['./reward-animation-layer.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
  animations: [xpFloatAnimation, glowAnimation],
})
export class RewardAnimationLayerComponent implements OnInit, OnDestroy {
  xpEarned = input.required<number>();
  animationData = input<unknown>(null);

  animationComplete = output<void>();

  /** Aria-live region for screen reader announcement */
  xpAnnouncement = computed(() => `Earned ${this.xpEarned()} experience points`);

  private animationTimer: ReturnType<typeof setTimeout> | null = null;

  ngOnInit(): void {
    // Auto-dismiss after particle burst duration (300ms) + buffer
    this.animationTimer = setTimeout(() => {
      this.animationComplete.emit();
    }, 800); // 300ms burst + 500ms float text
  }

  ngOnDestroy(): void {
    if (this.animationTimer) {
      clearTimeout(this.animationTimer);
    }
  }
}
```

### PerfectDayOverlayComponent

```typescript
// perfect-day-overlay.component.ts

@Component({
  standalone: true,
  selector: 'app-perfect-day-overlay',
  templateUrl: './perfect-day-overlay.component.html',
  styleUrls: ['./perfect-day-overlay.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, IonicModule],
  animations: [scaleInAnimation, fadeOutAnimation, counterAnimation],
  host: {
    'role': 'dialog',
    'aria-label': 'Perfect Day achievement',
    'aria-modal': 'true',
  },
})
export class PerfectDayOverlayComponent implements AfterViewInit {
  stats = input.required<PerfectDayStats>();
  confettiData = input<unknown>(null);

  dismiss = output<void>();

  @ViewChild('continueBtn') continueBtn!: ElementRef<HTMLButtonElement>;

  ngAfterViewInit(): void {
    // Move focus to continue button for accessibility
    setTimeout(() => this.continueBtn.nativeElement.focus(), 100);
  }

  onDismiss(): void {
    this.dismiss.emit();
  }
}
```

## Animation Definitions

```typescript
// reward.animations.ts

import { trigger, transition, style, animate, keyframes } from '@angular/animations';

export const xpFloatAnimation = trigger('xpFloat', [
  transition(':enter', [
    style({ opacity: 1, transform: 'translateY(0) scale(1)' }),
    animate('500ms ease-out', style({ opacity: 0, transform: 'translateY(-60px) scale(1.2)' })),
  ]),
]);

export const glowAnimation = trigger('glow', [
  transition(':enter', [
    animate('600ms ease-out', keyframes([
      style({ boxShadow: '0 0 0px #FF9800', offset: 0 }),
      style({ boxShadow: '0 0 20px #FF9800', offset: 0.5 }),
      style({ boxShadow: '0 0 0px #FF9800', offset: 1 }),
    ])),
  ]),
]);

export const slideUpAnimation = trigger('slideUp', [
  transition(':enter', [
    style({ transform: 'translateY(100%)' }),
    animate('300ms ease-out', style({ transform: 'translateY(0)' })),
  ]),
  transition(':leave', [
    animate('200ms ease-in', style({ transform: 'translateY(100%)' })),
  ]),
]);
```

```typescript
// perfect-day.animations.ts

import { trigger, transition, style, animate } from '@angular/animations';

export const scaleInAnimation = trigger('scaleIn', [
  transition(':enter', [
    style({ opacity: 0, transform: 'scale(0.5)' }),
    animate('400ms cubic-bezier(0.34, 1.56, 0.64, 1)', style({ opacity: 1, transform: 'scale(1)' })),
  ]),
]);

export const fadeOutAnimation = trigger('fadeOut', [
  transition(':leave', [
    animate('300ms ease-out', style({ opacity: 0 })),
  ]),
]);
```

## XP Calculation Utilities

```typescript
// xp-calculations.ts

import { XpFillAnimationConfig } from '../models/quest-completion.models';

/**
 * Pure function: Calculate XP fill animation configuration.
 * Determines if the fill crosses a level boundary and computes overflow.
 */
export function calculateXpFillConfig(
  currentXp: number,
  xpEarned: number,
  levelThreshold: number
): XpFillAnimationConfig {
  const newXp = currentXp + xpEarned;
  const crossesLevel = newXp >= levelThreshold;
  const overflowXp = crossesLevel ? newXp - levelThreshold : 0;

  return {
    previousXp: currentXp,
    newXp: crossesLevel ? levelThreshold : newXp,
    levelThreshold,
    crossesLevel,
    overflowXp,
  };
}

/**
 * Pure function: Interpolate between two XP values for counting animation.
 * Returns the interpolated value at a given progress (0-1).
 */
export function interpolateXp(startXp: number, endXp: number, progress: number): number {
  const clamped = Math.max(0, Math.min(1, progress));
  return Math.round(startXp + (endXp - startXp) * clamped);
}

/**
 * Pure function: Format XP display text.
 */
export function formatXpDisplay(currentXp: number, requiredXp: number): string {
  return `${currentXp.toLocaleString()} / ${requiredXp.toLocaleString()} XP`;
}

/**
 * Pure function: Determine if all daily quests are complete.
 */
export function isDailySetComplete(quests: { completed: boolean }[]): boolean {
  return quests.length > 0 && quests.every(q => q.completed);
}
```

## Error Handling

| Scenario | HTTP Status | Behavior |
|----------|-------------|----------|
| Success | 200 | Dismiss sheet → play reward animation → update XP bar → check Perfect Day |
| Duplicate | 409 | Dismiss sheet silently → mark completed locally → emit event |
| Network error | 0 / timeout | Show error toast → keep sheet open for retry |
| Server error | 5xx | Show error toast → keep sheet open for retry |
| Auth expired | 401 | Handled by global interceptor (auto-logout) |

## Integration Points

### Calling from Dashboard

```typescript
// In dashboard.page.ts
onQuestComplete(quest: Quest): void {
  this.questCompletionService.completeQuest(quest).subscribe({
    next: (result) => {
      // Quest already removed from store by the service
      // Additional dashboard-specific logic if needed
    },
  });
}
```

### Calling from Quest Board

```typescript
// In quest-board.page.ts
onQuestComplete(quest: Quest): void {
  this.questCompletionService.completeQuest(quest).subscribe({
    next: (result) => {
      // Update local quest list state
      this.quests.update(list => list.filter(q => q.id !== result.questId));
    },
  });
}
```

## Styling Tokens

```scss
// Confirmation Sheet
$sheet-bg: #161616;
$sheet-border-radius: 16px;
$sheet-drag-handle-color: rgba(255, 255, 255, 0.3);

// Buttons
$btn-complete-bg: #4CAF50;
$btn-complete-min-size: 44px;
$btn-cancel-color: rgba(255, 255, 255, 0.7);

// Reward Animation
$particle-burst-duration: 300ms;
$xp-float-duration: 500ms;
$glow-color: #FF9800;
$accent-secondary: #A855F7;

// Progress Bar Fill
$fill-duration: 600ms;
$fill-timing: ease-out;
$fill-gradient: linear-gradient(90deg, #FF9800, #A855F7);
$level-up-pause: 200ms;

// Perfect Day Overlay
$overlay-bg: #0A0A0A;
$overlay-gradient: radial-gradient(circle, rgba(255, 152, 0, 0.2), rgba(168, 85, 247, 0.1));
$dismiss-duration: 300ms;
```

## Accessibility Design

| Component | ARIA Pattern | Details |
|-----------|-------------|---------|
| Confirmation Sheet | Dialog | `role="dialog"`, `aria-modal="true"`, `aria-label="Quest completion confirmation"`, focus trap, Escape dismissal |
| Complete Button | Button | Dynamic `aria-label="Complete quest: {title}"` |
| Reward Layer | Live Region | `aria-live="polite"` announces XP earned |
| Perfect Day Overlay | Dialog | `role="dialog"`, focus moves to Continue button, `aria-live` announces "Perfect Day achieved" |
| Continue Button | Button | Auto-focused on overlay appearance, min 44x44px touch target |

## Performance Considerations

1. **Lottie Preloading**: Animation JSON is loaded at service initialization via dynamic import, not at animation trigger time
2. **GPU Acceleration**: All animations use `transform` and `opacity` exclusively — no layout-triggering properties
3. **DOM Cleanup**: `RewardAnimationLayerComponent` removes itself from DOM after animation completes via `*ngIf` bound to flow state
4. **will-change Hints**: Progress bar fill uses `will-change: width` during animation, removed after completion
5. **OnPush**: All components use `ChangeDetectionStrategy.OnPush` to minimize change detection cycles
6. **Signal-based State**: Flow state is signal-based, enabling fine-grained reactivity without zone.js overhead


## Testing Strategy

### Unit Tests (Example-Based)
- Confirmation sheet renders all quest fields correctly
- Cancel/Escape/swipe-down dismiss the sheet without side effects
- Loading spinner appears during API call
- Haptic methods are called at correct trigger points (fire-and-forget)
- Lottie animations are preloaded on service initialization
- Focus trap and ARIA attributes are correctly applied
- Animation layer removes itself from DOM after completion

### Property-Based Tests
- XP fill calculation (Properties 6, 7, 8) — pure functions with large input space
- Concurrent call guard (Property 2) — service-level state machine
- Completion event emission (Property 3) — event correctness across all quests
- 409 vs non-409 error handling (Properties 9, 10) — error classification logic
- Perfect Day detection (Property 11) — boundary condition across quest set sizes

### Integration Tests
- Full flow from completeQuest() call through animation to idle state
- Dashboard store updates correctly after completion
- Toast service receives error messages for non-409 failures

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system — essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Sequential Flow Orchestration

*For any* quest passed to `completeQuest`, the service SHALL first open the confirmation sheet, then (upon user confirmation) call the API, then trigger the reward animation, then update the progress bar, and finally check for Perfect Day — always in this exact order, never skipping or reordering steps.

**Validates: Requirements 1.3, 1.4**

### Property 2: Concurrent Call Guard

*For any* two calls to `completeQuest` where the first has not yet resolved, the second call SHALL return EMPTY and produce no side effects — the flow state SHALL remain unchanged by the second call.

**Validates: Requirements 1.5, 6.4**

### Property 3: Completion Event Emission

*For any* quest that completes successfully (including 409 treated as success), the service SHALL emit exactly one `QuestCompletionResult` event containing the correct `questId`, `questTitle`, and `xpEarned` values matching the quest and API response.

**Validates: Requirements 1.6**

### Property 4: Confirmation Sheet Displays Quest Data

*For any* quest object with title T, description D, difficulty Diff, statType S, and xpReward X, the rendered confirmation sheet SHALL contain all five values, and the complete button's `aria-label` SHALL contain the string T.

**Validates: Requirements 2.2, 8.4**

### Property 5: XP Floating Number Correctness

*For any* successful completion response with `xpEarned = N`, the reward animation layer SHALL display the text `+N XP` where N is the exact numeric value from the response.

**Validates: Requirements 3.4**

### Property 6: XP Fill Transition Calculation

*For any* `(currentXp, xpEarned, levelThreshold)` triple where `currentXp + xpEarned < levelThreshold`, `calculateXpFillConfig` SHALL return `newXp = currentXp + xpEarned`, `crossesLevel = false`, and `overflowXp = 0`.

**Validates: Requirements 4.1**

### Property 7: Level-Up Overflow Calculation

*For any* `(currentXp, xpEarned, levelThreshold)` triple where `currentXp + xpEarned >= levelThreshold`, `calculateXpFillConfig` SHALL return `crossesLevel = true` and `overflowXp = (currentXp + xpEarned) - levelThreshold`.

**Validates: Requirements 4.4**

### Property 8: XP Interpolation Correctness

*For any* `(startXp, endXp)` pair and progress value `p` in [0, 1], `interpolateXp(startXp, endXp, p)` SHALL return a value equal to `round(startXp + (endXp - startXp) * p)`, and at `p = 0` it SHALL equal `startXp`, and at `p = 1` it SHALL equal `endXp`.

**Validates: Requirements 4.5**

### Property 9: 409 Conflict Graceful Handling

*For any* quest where the API returns a 409 status, the service SHALL dismiss the confirmation sheet without showing an error toast, mark the quest as completed in local state, and emit a completion event with `xpEarned` equal to the quest's `xpReward`.

**Validates: Requirements 6.1, 6.2**

### Property 10: Non-409 Error Handling

*For any* HTTP error with status code other than 409 (e.g., 500, 503, 0), the service SHALL set the error message to "Could not complete quest. Try again.", keep the confirmation sheet open (status = 'confirming'), and not emit a completion event.

**Validates: Requirements 6.3**

### Property 11: Perfect Day Detection

*For any* daily quest set of size N where N > 0, completing the last remaining quest (leaving 0 quests in the loaded list) SHALL trigger the Perfect Day overlay after the reward animation finishes.

**Validates: Requirements 7.1**

### Property 12: Perfect Day Stats Display

*For any* `PerfectDayStats` with `totalQuestsCompleted = Q`, `totalXpEarnedToday = X`, and `currentStreak = S`, the Perfect Day overlay SHALL render all three values Q, X, and S in its stat counters.

**Validates: Requirements 7.4**
