# Implementation Plan: Quest Completion UI Flow

## Overview

Implement the Quest Completion UI flow as a shared orchestration layer using Angular 17+ standalone components, signal-based state, and OnPush change detection. The flow includes a confirmation bottom-sheet, Lottie + Angular Animations reward sequence, XP progress bar transitions, haptic feedback, silent duplicate handling, and a cinematic Perfect Day overlay. All components live under `frontend/src/features/quest-completion/`.

## Tasks

- [ ] 1. Set up feature structure, models, and pure utilities
  - [ ] 1.1 Create data models and type definitions
    - Create `frontend/src/features/quest-completion/models/quest-completion.models.ts`
    - Define `QuestCompletionResult`, `CompletionFlowState`, `XpFillAnimationConfig`, `PerfectDayStats` interfaces
    - Export all types for use by service and components
    - _Requirements: 1.2, 1.4, 7.4_

  - [ ] 1.2 Implement pure XP calculation utilities
    - Create `frontend/src/features/quest-completion/utils/xp-calculations.ts`
    - Implement `calculateXpFillConfig(currentXp, xpEarned, levelThreshold)` returning `XpFillAnimationConfig`
    - Implement `interpolateXp(startXp, endXp, progress)` with clamped progress [0,1]
    - Implement `formatXpDisplay(currentXp, requiredXp)` returning locale-formatted string
    - Implement `isDailySetComplete(quests)` returning boolean
    - _Requirements: 4.1, 4.4, 4.5, 7.1_

  - [ ]* 1.3 Write property tests for XP calculation utilities
    - **Property 6: XP Fill Transition Calculation** — for any (currentXp, xpEarned, levelThreshold) where sum < threshold, verify newXp = sum, crossesLevel = false, overflowXp = 0
    - **Property 7: Level-Up Overflow Calculation** — for any triple where sum >= threshold, verify crossesLevel = true and overflowXp = sum - threshold
    - **Property 8: XP Interpolation Correctness** — for any (startXp, endXp, p∈[0,1]), verify result = round(start + (end-start)*p), p=0 → start, p=1 → end
    - **Validates: Requirements 4.1, 4.4, 4.5**

- [ ] 2. Implement animation definitions
  - [ ] 2.1 Create reward animation triggers
    - Create `frontend/src/features/quest-completion/animations/reward.animations.ts`
    - Define `xpFloatAnimation` trigger — enter: translateY(0)→translateY(-60px), opacity 1→0, 500ms ease-out
    - Define `glowAnimation` trigger — keyframes with boxShadow 0→20px→0 using #FF9800, 600ms ease-out
    - Define `slideUpAnimation` trigger — enter: translateY(100%)→0 300ms, leave: 0→translateY(100%) 200ms
    - Use only transform and opacity for GPU acceleration
    - _Requirements: 3.2, 3.3, 3.4, 9.1_

  - [ ] 2.2 Create Perfect Day animation triggers
    - Create `frontend/src/features/quest-completion/animations/perfect-day.animations.ts`
    - Define `scaleInAnimation` trigger — enter: scale(0.5)→scale(1), opacity 0→1, 400ms cubic-bezier(0.34,1.56,0.64,1)
    - Define `fadeOutAnimation` trigger — leave: opacity 1→0, 300ms ease-out
    - Use Web Animations API renderer for hardware acceleration
    - _Requirements: 7.3, 7.6, 9.4_

- [ ] 3. Implement QuestCompletionService orchestrator
  - [ ] 3.1 Create the singleton service with signal-based state machine
    - Create `frontend/src/features/quest-completion/services/quest-completion.service.ts`
    - Implement as `@Injectable({ providedIn: 'root' })` with signal-based `CompletionFlowState`
    - Expose `flowState` as readonly signal, `isFlowActive` as computed signal
    - Expose `completionEvent$` Subject for subscribers
    - Inject `QuestService`, `HapticService`, `DashboardStore`
    - Implement `preloadAnimations()` in constructor using dynamic import for Lottie JSON
    - _Requirements: 1.1, 1.2, 9.2_

  - [ ] 3.2 Implement completeQuest public API and concurrent call guard
    - Implement `completeQuest(quest: Quest): Observable<QuestCompletionResult>` method
    - Return EMPTY if `isFlowActive()` is true (concurrent guard)
    - Set state to `confirming` and expose quest for the sheet component
    - Return filtered `completionEvent$` for the specific quest
    - _Requirements: 1.2, 1.3, 1.5_

  - [ ]* 3.3 Write property test for concurrent call guard
    - **Property 2: Concurrent Call Guard** — for any two calls where first hasn't resolved, second returns EMPTY with no state change
    - **Validates: Requirements 1.5, 6.4**

  - [ ] 3.4 Implement onConfirm, onCancel, and flow lifecycle methods
    - `onConfirm()`: trigger light haptic, set state to `submitting`, call `questService.completeQuest(id)`
    - `handleSuccess()`: trigger success haptic, update DashboardStore, set state to `animating`, emit completion event
    - `handleError()`: 409 → silent dismiss + emit event; other → set error message, keep sheet open
    - `onCancel()`: reset state to idle
    - `onAnimationComplete()`: check Perfect Day → set `perfect-day` or `idle`
    - `onPerfectDayDismiss()`: reset state to idle, trigger heavy haptic on overlay appear
    - _Requirements: 1.4, 5.1, 5.2, 5.3, 5.4, 6.1, 6.2, 6.3_

  - [ ]* 3.5 Write property tests for error handling
    - **Property 9: 409 Conflict Graceful Handling** — for any 409 response, verify sheet dismissed, no error toast, quest marked completed, event emitted with xpReward
    - **Property 10: Non-409 Error Handling** — for any non-409 error, verify error message set, state remains confirming, no completion event emitted
    - **Validates: Requirements 6.1, 6.2, 6.3**

  - [ ]* 3.6 Write property test for completion event emission
    - **Property 3: Completion Event Emission** — for any successful completion (including 409), verify exactly one event emitted with correct questId, questTitle, xpEarned
    - **Validates: Requirements 1.6**

  - [ ]* 3.7 Write property test for Perfect Day detection
    - **Property 11: Perfect Day Detection** — for any daily quest set of size N>0, completing last quest (0 remaining) triggers Perfect Day overlay after animation
    - **Validates: Requirements 7.1**

- [ ] 4. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 5. Implement ConfirmationSheetComponent
  - [ ] 5.1 Create the standalone component with template and styles
    - Create `confirmation-sheet.component.ts` as standalone, OnPush, with `slideUpAnimation`
    - Use `input.required<Quest>()` for quest data, `input<boolean>()` for isSubmitting, `input<string|null>()` for errorMessage
    - Use `output<void>()` for confirm and cancel events
    - Create template with backdrop, sheet container, drag handle, quest details (title, description, difficulty badge, stat icon, XP preview)
    - Create "Complete Quest" button (#4CAF50, min 44x44px) with loading spinner when submitting
    - Create "Cancel" text button below primary action
    - Style with dark theme ($sheet-bg: #161616, 16px border-radius, rounded top corners)
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.7_

  - [ ] 5.2 Implement accessibility and dismiss behaviors
    - Add `role="dialog"`, `aria-modal="true"`, `aria-label="Quest completion confirmation"` on host
    - Implement focus trap using `cdkTrapFocus` with auto-capture
    - Implement Escape key dismissal via `(keydown.escape)` host listener
    - Implement swipe-down dismissal
    - Compute dynamic `aria-label` for complete button: `"Complete quest: {title}"`
    - Display error banner with `role="alert"` when errorMessage is set
    - _Requirements: 2.6, 8.1, 8.2, 8.3, 8.4_

  - [ ]* 5.3 Write property test for confirmation sheet data display
    - **Property 4: Confirmation Sheet Displays Quest Data** — for any quest with title T, description D, difficulty, statType, xpReward, verify all rendered and aria-label contains T
    - **Validates: Requirements 2.2, 8.4**

- [ ] 6. Implement RewardAnimationLayerComponent
  - [ ] 6.1 Create the standalone component with Lottie integration
    - Create `reward-animation-layer.component.ts` as standalone, OnPush
    - Use `input.required<number>()` for xpEarned, `input<unknown>()` for animationData
    - Use `output<void>()` for animationComplete
    - Render full-screen overlay with Lottie player for particle burst (300ms)
    - Display floating "+N XP" text with `xpFloatAnimation` (500ms upward fade)
    - Apply `glowAnimation` on XP progress bar area with #FF9800
    - Use mystic purple (#A855F7) as secondary accent in particle burst
    - Auto-dismiss after 800ms (300ms burst + 500ms float) via setTimeout
    - Clean up timer in ngOnDestroy
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.7, 9.1_

  - [ ] 6.2 Implement accessibility and DOM cleanup
    - Add `aria-live="polite"` region announcing XP earned value
    - Compute announcement text: `"Earned {N} experience points"`
    - Ensure component removes itself from DOM after animation (bound to flow state via *ngIf)
    - Use only CSS transform and opacity for GPU-accelerated rendering
    - _Requirements: 8.5, 9.1, 9.5_

  - [ ]* 6.3 Write property test for XP floating number
    - **Property 5: XP Floating Number Correctness** — for any xpEarned = N, verify displayed text is "+N XP"
    - **Validates: Requirements 3.4**

- [ ] 7. Implement PerfectDayOverlayComponent
  - [ ] 7.1 Create the standalone component with cinematic styling
    - Create `perfect-day-overlay.component.ts` as standalone, OnPush
    - Use `input.required<PerfectDayStats>()` for stats, `input<unknown>()` for confettiData
    - Use `output<void>()` for dismiss event
    - Render full-screen overlay with dark background (#0A0A0A) and radial gradient glow (#FF9800, #A855F7)
    - Display "PERFECT DAY" title with Orbitron font and `scaleInAnimation`
    - Display animated stat counters (quests completed, XP earned today, streak count)
    - Display "Continue" dismiss button (min 44x44px) at bottom
    - Render Lottie confetti animation looping in background until dismissed
    - Apply `fadeOutAnimation` on dismiss (300ms)
    - _Requirements: 7.2, 7.3, 7.4, 7.5, 7.6, 7.7_

  - [ ] 7.2 Implement accessibility for Perfect Day overlay
    - Add `role="dialog"`, `aria-modal="true"`, `aria-label="Perfect Day achievement"` on host
    - Move focus to "Continue" button in `ngAfterViewInit` via `@ViewChild`
    - Add `aria-live` region announcing "Perfect Day achieved"
    - _Requirements: 7.8, 8.6_

  - [ ]* 7.3 Write property test for Perfect Day stats display
    - **Property 12: Perfect Day Stats Display** — for any PerfectDayStats with Q, X, S values, verify all three rendered in stat counters
    - **Validates: Requirements 7.4**

- [ ] 8. Implement XP Progress Bar Fill Animation
  - [ ] 8.1 Integrate XP fill animation with the shared app-xp-progress-bar
    - Use `calculateXpFillConfig` to determine animation parameters
    - Animate width from previousXp/threshold to newXp/threshold using CSS transition (600ms ease-out)
    - Apply accent-to-secondary gradient (#FF9800 → #A855F7) for fill color
    - Add `will-change: width` during animation, remove after completion
    - Implement level-up sequence: fill to 100%, pause 200ms, reset to 0%, fill to overflow amount
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 9.3_

  - [ ] 8.2 Implement counting-up XP text display
    - Use `interpolateXp` to animate numeric text from previous to new value
    - Use `formatXpDisplay` for locale-formatted output (e.g., "2,400 / 3,000 XP")
    - Sync text update with bar fill animation using requestAnimationFrame
    - _Requirements: 4.5_

- [ ] 9. Wire orchestration into calling components
  - [ ] 9.1 Wire QuestCompletionService into the host view
    - Create a host-level template section (or app-level component) that listens to `flowState` signal
    - Conditionally render ConfirmationSheetComponent when status is `confirming` or `submitting`
    - Conditionally render RewardAnimationLayerComponent when status is `animating`
    - Conditionally render PerfectDayOverlayComponent when status is `perfect-day`
    - Pass appropriate inputs and wire outputs to service methods (onConfirm, onCancel, onAnimationComplete, onPerfectDayDismiss)
    - _Requirements: 1.3, 1.4_

  - [ ] 9.2 Integrate with Dashboard and Quest Board pages
    - Add `completeQuest(quest)` call from Dashboard page's quest card action
    - Add `completeQuest(quest)` call from Quest Board page's quest item action
    - Subscribe to `completionEvent$` for local state updates (e.g., removing quest from list)
    - _Requirements: 1.6_

- [ ] 10. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties from the design document
- Unit tests validate specific examples and edge cases
- All components use standalone architecture, OnPush change detection, and signal-based inputs
- Haptic calls are fire-and-forget (non-blocking) per Requirement 5.4
- Lottie animations are preloaded at service init to avoid playback delay (Requirement 9.2)
- Global constraint: Do not run any commands — only create files

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "2.1", "2.2"] },
    { "id": 1, "tasks": ["1.2", "3.1"] },
    { "id": 2, "tasks": ["1.3", "3.2"] },
    { "id": 3, "tasks": ["3.3", "3.4"] },
    { "id": 4, "tasks": ["3.5", "3.6", "3.7", "5.1"] },
    { "id": 5, "tasks": ["5.2", "6.1"] },
    { "id": 6, "tasks": ["5.3", "6.2", "7.1"] },
    { "id": 7, "tasks": ["6.3", "7.2", "8.1"] },
    { "id": 8, "tasks": ["7.3", "8.2"] },
    { "id": 9, "tasks": ["9.1"] },
    { "id": 10, "tasks": ["9.2"] }
  ]
}
```
