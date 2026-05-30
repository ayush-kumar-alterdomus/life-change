# Implementation Plan: UI Dashboard

## Overview

Replace the existing placeholder dashboard with a fully-featured signal-based home screen. The implementation builds the data layer first (models, service, store), then the utility functions, followed by the dumb child components, and finally wires everything together in the smart DashboardPage. Property-based tests validate correctness properties using fast-check, and unit tests cover component rendering and interaction wiring.

## Tasks

- [x] 1. Create data models and utility functions
  - [x] 1.1 Create dashboard data model interfaces
    - Create `features/dashboard/models/dashboard.models.ts` with all TypeScript interfaces: `SectionState<T>`, `DashboardXpProgress`, `DashboardDailyStats`, `DashboardActiveArc`, `DashboardLeaderboardPreview`, `LeaderboardEntry`, `DashboardUserSummary`
    - Export all interfaces from a barrel file
    - _Requirements: 12.1, 12.3, 12.4_

  - [x] 1.2 Implement time-based greeting utility
    - Create `features/dashboard/utils/greeting.util.ts` with `getTimeBasedGreeting(displayName: string | null, hour?: number): string`
    - Return "Good Morning" for hours 5–11, "Good Afternoon" for 12–16, "Good Evening" for 17–4
    - Append ", {displayName}" when displayName is non-null
    - _Requirements: 2.1, 2.5_

  - [x] 1.3 Implement motivation rotation utility
    - Create `features/dashboard/utils/motivation.util.ts` with `selectMotivationMessage(lastIndex: number | null, poolSize: number): number`
    - Create `MOTIVATION_MESSAGES` array with 20+ static motivational messages
    - Ensure returned index differs from lastIndex and is within [0, poolSize)
    - Export `LAST_MOTIVATION_INDEX_KEY` constant for storage key
    - _Requirements: 7.1, 7.2, 7.5_

  - [x] 1.4 Implement XP formatting utility
    - Create `features/dashboard/utils/xp-format.util.ts` with `formatXpDisplay(currentXp: number, requiredXp: number): string`
    - Format with locale-appropriate thousand separators, " / " separator, and " XP" suffix
    - _Requirements: 3.3_

  - [ ]* 1.5 Write property tests for greeting utility
    - **Property 1: Time-based greeting correctness**
    - Use fast-check to generate arbitrary hours [0–23] and optional display names
    - Verify correct greeting prefix for each hour range and name suffix behavior
    - **Validates: Requirements 2.1, 2.5**

  - [ ]* 1.6 Write property tests for XP formatting utility
    - **Property 2: XP display formatting**
    - Use fast-check to generate arbitrary non-negative integer pairs
    - Verify output contains both formatted values, " / " separator, and " XP" suffix
    - **Validates: Requirements 3.3**

  - [ ]* 1.7 Write property tests for motivation rotation utility
    - **Property 5: Motivation rotation avoids consecutive repeats**
    - Use fast-check to generate arbitrary lastIndex values and pool sizes ≥ 2
    - Verify returned index differs from lastIndex and is within bounds [0, poolSize)
    - **Validates: Requirements 7.2, 7.5**

- [x] 2. Implement DashboardService and DashboardStore
  - [x] 2.1 Create DashboardService
    - Create `features/dashboard/services/dashboard.service.ts` as an injectable service
    - Inject `ApiService` and implement methods: `getUserSummary()`, `getXpProgress()`, `getDailyStats()`, `getActiveArc()`, `getTodayQuests()`, `getLeaderboardPreview()`, `completeQuest(questId)`, `skipQuest(questId)`
    - Use `HttpContext` with `SKIP_LOADING` token on all requests to prevent global loading overlay
    - _Requirements: 1.5, 11.5, 12.1_

  - [x] 2.2 Rewrite DashboardStore with SectionState signals
    - Replace existing `features/dashboard/services/dashboard.store.ts` with the new signal-based store
    - Implement `SectionState<T>` signals for each section: `userSummary`, `xpProgress`, `dailyStats`, `activeArc`, `todayQuests`, `leaderboardPreview`
    - Implement computed signals: `isLoading`, per-section loading signals, per-section error signals
    - Implement `loadDashboard()` that restores cache then fetches all sections in parallel via `forkJoin`
    - Implement `refreshDashboard()` that fetches without setting loading state (keeps existing data visible)
    - Implement `completeQuest(questId, xpEarned)` for atomic multi-signal update
    - Implement `retryErroredSections()` to re-fetch only sections in error state
    - Implement `loadLeaderboardPreview()` for lazy-loaded leaderboard data
    - Inject `StorageService` for cache persistence; save data on successful fetch, restore on init
    - _Requirements: 9.2, 9.4, 9.5, 10.7, 11.1, 12.1, 12.2, 12.3, 12.4, 12.5, 12.6_

  - [ ]* 2.3 Write property test for isLoading computed signal
    - **Property 8: isLoading computed signal correctness**
    - Use fast-check to generate arbitrary combinations of SectionState statuses
    - Verify `isLoading` returns true iff at least one section has status 'loading'
    - **Validates: Requirements 12.2**

  - [ ]* 2.4 Write property test for quest completion atomicity
    - **Property 9: Quest completion atomic multi-signal update**
    - Use fast-check to generate arbitrary quest lists, questId, and xpEarned values
    - Verify after `completeQuest`: quest removed, questsCompleted incremented by 1, currentXp incremented by xpEarned
    - **Validates: Requirements 12.5**

  - [ ]* 2.5 Write property test for section error isolation
    - **Property 7: Independent section error isolation**
    - Use fast-check to generate arbitrary combinations where some sections are 'error' and some 'loaded'
    - Verify loaded sections expose data and errored sections expose error state independently
    - **Validates: Requirements 11.1, 11.2**

- [x] 3. Checkpoint - Ensure data layer tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 4. Implement dumb child components
  - [x] 4.1 Create HeaderSectionComponent
    - Create `features/dashboard/components/header-section/` with standalone component, template, and styles
    - Accept inputs: `displayName`, `level`, `streakDays`
    - Use `getTimeBasedGreeting` for computed greeting signal
    - Render `LevelBadgeComponent` (shared) with small size variant and `StreakFlameComponent` (shared)
    - Layout: greeting text left, badge + flame right in a horizontal row
    - Use semantic `<h1>` for greeting text
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 13.5_

  - [x] 4.2 Create XpProgressCardComponent
    - Create `features/dashboard/components/xp-progress-card/` with standalone component, template, and styles
    - Accept inputs: `currentLevel`, `currentXp`, `requiredXp`
    - Render level number with Orbitron font, `XpProgressBarComponent` (shared), and formatted XP text
    - Apply glass-morphism card styling (#161616 background), accent-to-secondary gradient for progress fill
    - Add 600ms CSS transition on progress bar width for animated XP changes
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

  - [x] 4.3 Create MiniCardComponent and DailySummaryComponent
    - Create `features/dashboard/components/mini-card/` — accepts `icon`, `value`, `label` inputs
    - Create `features/dashboard/components/daily-summary/` — accepts `stats: DashboardDailyStats` input
    - Render 4 mini-cards in a 2x2 grid: Quests (completed/total), Streak (flame icon), Focus Score, Life Score
    - Apply design system card styling (#161616 background, 12px border-radius) to each mini-card
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7_

  - [x] 4.4 Create ActiveArcSectionComponent
    - Create `features/dashboard/components/active-arc-section/` with standalone component
    - Accept input: `activeArc: DashboardActiveArc | null`
    - When arc exists: render `ArcCardComponent` (shared) with name, progress, phase, type; emit `navigateToArc` output on tap
    - When arc is null: render prompt card "Start your first Arc" with CTA button; emit `navigateToArcSelection` output
    - Display progress as both bar and numeric label (e.g., "43% Complete")
    - _Requirements: 5.1, 5.2, 5.3, 5.4_

  - [x] 4.5 Create QuestListComponent with swipe actions
    - Create `features/dashboard/components/quest-list/` with standalone component
    - Accept input: `quests: Quest[]`; outputs: `completeQuest`, `skipQuest`, `editQuest`, `viewAll`
    - Render `ion-list` with `ion-item-sliding` for each quest; use `QuestCardComponent` (shared) inside each item
    - Right swipe: "Complete" action (#4CAF50); Left swipe: "Skip" and "Edit" actions
    - Cap display at 8 quests; show "View All Quests" link when overflow
    - Show empty state "No quests for today. Enjoy your rest!" with icon when list is empty
    - Use `trackBy` on the `@for` loop with quest ID
    - Ensure 44x44px minimum touch targets on swipe action buttons
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8, 6.9, 13.1, 13.3_

  - [x] 4.6 Create MotivationWidgetComponent
    - Create `features/dashboard/components/motivation-widget/` with standalone component
    - Inject `StorageService` to read/write last motivation index
    - Use `selectMotivationMessage` utility to pick a non-repeating message on init
    - Render message in styled card with lightbulb icon, accent-colored left border, Inter font 16px, text-secondary color (#B0B0B0)
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

  - [x] 4.7 Create LeaderboardPreviewComponent
    - Create `features/dashboard/components/leaderboard-preview/` with standalone component
    - Accept input: `preview: DashboardLeaderboardPreview`; output: `viewFullRankings`
    - Display user rank ("#12"), XP total, league name
    - Render top 3 entries using `LeaderboardCardComponent` (shared) with gold/silver/bronze styling
    - Include "View Full Rankings" button that emits output event
    - Apply design system card styling
    - _Requirements: 8.1, 8.2, 8.3, 8.5_

  - [ ]* 4.8 Write property test for quest list capping
    - **Property 4: Quest list capping**
    - Use fast-check to generate arbitrary quest arrays of varying lengths
    - Verify displayed count is `min(N, 8)` and "View All" link presence matches N > 8
    - **Validates: Requirements 6.8**

  - [ ]* 4.9 Write property test for mini-card data completeness
    - **Property 3: Mini-card data completeness**
    - Use fast-check to generate arbitrary `DashboardDailyStats` objects
    - Verify exactly 4 mini-cards are produced, each with icon, value, and label
    - **Validates: Requirements 4.1, 4.2, 4.3**

- [x] 5. Checkpoint - Ensure component tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 6. Wire DashboardPage and integrate all sections
  - [x] 6.1 Rewrite DashboardPage as smart component
    - Replace existing `features/dashboard/pages/dashboard.component.ts` with the new smart component
    - Use separate template file (`dashboard.page.html`) and styles file (`dashboard.page.scss`)
    - Set `changeDetection: ChangeDetectionStrategy.OnPush`
    - Import all child components, `IonicModule` (for `ion-content`, `ion-refresher`, `ion-refresher-content`)
    - Inject `DashboardStore`, `ConnectivityService`, `Router`, `HapticService`
    - Expose store signals to template for reactive rendering
    - Apply dark theme background (#0A0A0A) to `ion-content` with `fullscreen` attribute
    - Render sections in order: Header, XP Card, Daily Summary, Active Arc, Quest List, Motivation, Leaderboard
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6_

  - [x] 6.2 Implement skeleton loading states
    - Add conditional skeleton rendering per section using `@if` with section state checks
    - Header skeleton: text-line + circle placeholders; XP card: rectangle with shimmer; Daily summary: 4 rectangles in 2x2 grid; Quest list: 3 staggered rectangles
    - Use the shared `SkeletonLoaderComponent` or custom skeleton markup
    - Apply 200ms fade transition when data arrives
    - Show cached data immediately when available (no skeleton)
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6, 10.7_

  - [x] 6.3 Implement pull-to-refresh
    - Add `ion-refresher` with `ion-refresher-content` at the top of `ion-content`
    - Handle `ionRefresh` event: call `store.refreshDashboard()`, then `event.target.complete()` on completion
    - Use accent color (#FF9800) for the spinner
    - Do NOT show skeleton overlay during refresh — existing data stays visible
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6_

  - [x] 6.4 Implement quest swipe action handlers
    - Handle `completeQuest` output: call `DashboardService.completeQuest()`, trigger haptic success feedback, call `store.completeQuest(questId, xpEarned)` on success
    - Handle `skipQuest` output: call `DashboardService.skipQuest()`, update store to move quest to skipped state
    - Handle `editQuest` output: navigate to quest edit route with quest ID parameter
    - Handle `viewAll` output: navigate to full quest board route
    - _Requirements: 6.5, 6.6, 6.7_

  - [x] 6.5 Implement leaderboard lazy loading
    - In `ngAfterViewInit`, use `setTimeout(() => store.loadLeaderboardPreview(), 0)` to defer leaderboard fetch until above-the-fold content renders
    - Show leaderboard skeleton until data arrives
    - Handle leaderboard error state with retry option independently of other sections
    - _Requirements: 8.4, 13.2_

  - [x] 6.6 Implement connectivity recovery and per-section error handling
    - Set up `effect()` watching `ConnectivityService.isOnline` signal; on transition to online, call `store.retryErroredSections()`
    - Render per-section error states with "Retry" button using shared `ErrorStateComponent`
    - When all sections fail, show full-page error overlay with "Try Again" button
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5_

  - [x] 6.7 Add accessibility attributes and semantic structure
    - Add ARIA labels to all interactive elements (swipe actions, buttons, cards)
    - Use heading hierarchy: `<h1>` for greeting, `<h2>` for section titles
    - Ensure all touch targets are minimum 44x44px
    - Add `role` attributes where needed for screen reader navigation
    - _Requirements: 13.3, 13.4, 13.5_

  - [x] 6.8 Update dashboard route configuration
    - Update `dashboard.routes.ts` if needed to point to the new component file path
    - Ensure lazy loading works correctly with the standalone component
    - _Requirements: 1.1_

- [ ] 7. Remaining property and integration tests
  - [ ]* 7.1 Write property test for skeleton visibility logic
    - **Property 6: Skeleton visibility depends on cache and loading state**
    - Use fast-check to generate arbitrary section states and cache availability flags
    - Verify skeleton visible only when loading AND no cache; hidden when cache exists
    - **Validates: Requirements 9.4, 10.1, 10.7**

  - [ ]* 7.2 Write property test for leaderboard preview completeness
    - **Property 10: Leaderboard preview data completeness**
    - Use fast-check to generate arbitrary `DashboardLeaderboardPreview` objects
    - Verify rendered output includes rank, XP total, league name, and exactly 3 top entries
    - **Validates: Requirements 8.1, 8.2**

  - [ ]* 7.3 Write property test for quest card field rendering
    - **Property 11: Quest card renders all required fields**
    - Use fast-check to generate arbitrary quest objects
    - Verify each rendered quest item includes title, XP reward, difficulty badge, and stat type icon
    - **Validates: Requirements 6.2**

  - [ ]* 7.4 Write unit tests for DashboardPage integration
    - Test parallel API fetching on init (verify forkJoin calls all endpoints)
    - Test pull-to-refresh flow (refresher triggers store.refreshDashboard, completes event)
    - Test connectivity recovery auto-retry (online signal triggers retryErroredSections)
    - Test cache restore behavior (cached data shown immediately, no skeleton)
    - _Requirements: 1.5, 9.2, 10.7, 11.4_

- [x] 8. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- **IMPORTANT: Do NOT run any shell commands (ng generate, npm, etc.) during task execution. Only create and edit files directly.**
- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties using fast-check (already installed)
- Unit tests use Jasmine + Karma (existing test infrastructure)
- Shared UI components (LevelBadge, StreakFlame, XpProgressBar, QuestCard, ArcCard, LeaderboardCard) already exist in `src/shared/ui/`
- Core services (ApiService, StorageService, ConnectivityService, HapticService) already exist in `src/core/services/`
- The existing placeholder dashboard component and store will be replaced in-place

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.2", "1.3", "1.4"] },
    { "id": 1, "tasks": ["1.5", "1.6", "1.7", "2.1"] },
    { "id": 2, "tasks": ["2.2"] },
    { "id": 3, "tasks": ["2.3", "2.4", "2.5", "4.1", "4.2", "4.3", "4.4", "4.5", "4.6", "4.7"] },
    { "id": 4, "tasks": ["4.8", "4.9", "6.1"] },
    { "id": 5, "tasks": ["6.2", "6.3", "6.4", "6.5", "6.6", "6.7", "6.8"] },
    { "id": 6, "tasks": ["7.1", "7.2", "7.3", "7.4"] }
  ]
}
```
