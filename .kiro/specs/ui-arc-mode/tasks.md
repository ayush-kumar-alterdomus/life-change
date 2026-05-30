# Implementation Plan: UI Arc Mode

## Overview

Implement the Arc Mode UI feature as a set of Angular 20+ standalone components following the smart pages + dumb components pattern. The implementation builds the data layer (models, service, store) first, then the dumb presentation components, then the smart page orchestrators, and finally wires routing together — replacing the existing placeholder page.

## Tasks

- [x] 1. Data layer: models, service, and store
  - [x] 1.1 Create Arc Mode frontend data models
    - Create `frontend/src/features/arc-mode/models/arc-detail.model.ts` with interfaces: `ArcDetail`, `ArcMilestoneDetail`, `ArcPhaseWithMilestones`, `ArcReward`, `SkillNode`, `IdentityTitleMap`, `CreateArcPayload`
    - Export all interfaces from a barrel `frontend/src/features/arc-mode/models/index.ts`
    - Extend the existing `Arc` interface from `@shared/models` for `ArcDetail`
    - _Requirements: 3.1, 5.4, 7.1, 8.1, 9.1, 10.1_

  - [x] 1.2 Create ArcService HTTP communication layer
    - Create `frontend/src/features/arc-mode/services/arc.service.ts`
    - Implement methods: `getAvailableArcs()`, `getActiveArcs()`, `getArcDetail(id)`, `getProgress(arcId)`, `completeMilestone(arcId, milestoneId)`, `createArc(payload)`, `startArc(arcId)`
    - Use `HttpClient` with typed `Observable` returns wrapping `ApiResponse<T>`
    - Use `environment.apiUrl` for base URL construction
    - _Requirements: 1.2, 1.3, 3.1, 9.7, 11.1_

  - [x] 1.3 Create ArcStore signals-based state management
    - Create `frontend/src/features/arc-mode/store/arc.store.ts`
    - Implement core state signals: `_arcs`, `_activeProgress`, `_selectedDetail`
    - Implement per-operation loading signals: `loadingList`, `loadingDetail`, `loadingCreate`
    - Implement error signals: `listError`, `detailError`, `createError`
    - Implement computed filtered signals: `prebuiltArcs`, `activeArcs`, `completedArcs`
    - Implement actions: `loadArcsIfEmpty()`, `loadArcs()`, `loadArcDetail(id)`, `completeMilestone(arcId, milestoneId)` with optimistic update/revert, `createArc(payload)`
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6, 12.5_

  - [x]* 1.4 Write property tests for ArcStore filtering and optimistic updates
    - **Property 1: Arc store computed signals correctly filter arcs by category**
    - **Property 12: Cache-first strategy prevents redundant fetches**
    - **Property 13: Optimistic milestone update reverts on API failure**
    - **Validates: Requirements 1.2, 1.4, 11.2, 11.3, 11.4, 11.6**

- [x] 2. Checkpoint - Ensure data layer compiles cleanly
  - Ensure all tests pass, ask the user if questions arise.

- [x] 3. Dumb components: cinematic banner, phase progress, identity title
  - [x] 3.1 Create CinematicBannerComponent
    - Create `frontend/src/features/arc-mode/components/cinematic-banner/cinematic-banner.component.ts` (standalone, OnPush)
    - Create template and SCSS files
    - Inputs: `arcName`, `arcType`, `currentPhase`, `progressPercentage`, `scrollOffset`
    - Implement parallax transform via computed signal (`translateY(scrollOffset * 0.5)`)
    - Implement computed `ariaLabel` with format: "{name}, {phase} phase, {progress}% complete"
    - Style with full-width, min-height 240px, Orbitron font, arc-type gradient background
    - _Requirements: 3.2, 3.3, 3.4, 3.5, 14.4_

  - [x] 3.2 Create PhaseProgressComponent
    - Create `frontend/src/features/arc-mode/components/phase-progress/phase-progress.component.ts` (standalone, OnPush)
    - Create template and SCSS files
    - Input: `currentPhase`
    - Wrap the shared `app-stepper` component for 4-phase display
    - Implement computed `currentStepIndex` and `completedCount`
    - Implement computed `ariaLabel` with format: "Phase progress: currently in {phase}, {n} of 4 phases completed"
    - Style completed phases with #FF9800, active with pulsing glow, upcoming with #B0B0B0
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

  - [x] 3.3 Create IdentityTitleComponent
    - Create `frontend/src/features/arc-mode/components/identity-title/identity-title.component.ts` (standalone, OnPush)
    - Create template and SCSS files
    - Inputs: `currentPhase`, `titles` (IdentityTitleMap), `arcType`
    - Implement computed `currentTitle` resolving phase to title string
    - Implement computed `ariaLabel` with format: "Your arc identity: {title}"
    - Style with Orbitron font and arc-type gradient text color
    - Add reveal animation (fade-in with scale 0.8→1.0 over 300ms)
    - _Requirements: 10.1, 10.2, 10.4, 10.6_

  - [x]* 3.4 Write property tests for phase progress and identity title
    - **Property 4: Phase progress correctly classifies phases based on current phase**
    - **Property 11: Identity title maps correctly to current phase**
    - **Property 14: Cinematic banner aria-label contains arc name, phase, and progress**
    - **Validates: Requirements 4.3, 4.5, 10.2, 10.6, 3.4, 14.4**

- [x] 4. Dumb components: milestone timeline, boss section, rewards, skill tree preview
  - [x] 4.1 Create MilestoneTimelineComponent
    - Create `frontend/src/features/arc-mode/components/milestone-timeline/milestone-timeline.component.ts` (standalone, OnPush)
    - Create template and SCSS files
    - Inputs: `phases` (ArcPhaseWithMilestones[]), `currentPhase`
    - Implement `expandedPhases` signal with effect to auto-expand current phase
    - Implement `togglePhase(phaseName)` method for accordion behavior
    - Implement `getCompletionSummary(phase)` returning "{completed}/{total} milestones"
    - Display ✓ for completed milestones, ○ for upcoming
    - Add `aria-expanded` on headers, `aria-controls` linking to panel IDs
    - Add 200ms slide animation on expand/collapse
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 14.2_

  - [x] 4.2 Create BossSectionComponent
    - Create `frontend/src/features/arc-mode/components/boss-section/boss-section.component.ts` (standalone, OnPush)
    - Create template and SCSS files
    - Input: `boss` (Boss | null)
    - Import and use shared `app-boss-card` and `app-xp-progress-bar` components
    - Implement computed `hasBoss` and `isDefeated` signals
    - Show "Defeated" badge with strike-through when healthPercentage is 0
    - Hide entire section when boss is null
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

  - [x] 4.3 Create RewardsSectionComponent
    - Create `frontend/src/features/arc-mode/components/rewards-section/rewards-section.component.ts` (standalone, OnPush)
    - Create template and SCSS files
    - Input: `rewards` (ArcReward[])
    - Implement computed `groupedRewards` grouping by `unlocksAtPhase`
    - Style earned rewards at full opacity with green checkmark, locked at 50% opacity with lock icon
    - Implement `getRewardAriaLabel(reward)` with name and earned/locked status
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

  - [x] 4.4 Create SkillTreePreviewComponent
    - Create `frontend/src/features/arc-mode/components/skill-tree-preview/skill-tree-preview.component.ts` (standalone, OnPush)
    - Create template and SCSS files
    - Input: `nodes` (SkillNode[])
    - Output: `navigate` event
    - Implement computed `displayNodes` (max 6), `hasMore`, `unlockedCount`
    - Implement computed `ariaLabel` with format: "Skill tree preview, {unlocked} of {total} skills unlocked, tap to view full tree"
    - Style unlocked nodes with #FF9800 glow, locked with #B0B0B0, connecting lines between nodes
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6_

  - [x]* 4.5 Write property tests for milestone timeline, rewards, and skill tree preview
    - **Property 5: Milestone timeline groups milestones by phase with active phase expanded**
    - **Property 6: Milestone items display correct completion state and summary counts**
    - **Property 7: Boss section visibility follows boss presence**
    - **Property 8: Rewards display correct earned/locked state grouped by phase**
    - **Property 9: Skill tree preview limits displayed nodes and reports correct counts**
    - **Validates: Requirements 5.1, 5.2, 5.4, 5.5, 5.6, 6.1, 6.5, 7.1, 7.2, 7.3, 7.5, 8.2, 8.5, 8.6**

- [x] 5. Checkpoint - Ensure all dumb components compile cleanly
  - Ensure all tests pass, ask the user if questions arise.

- [x] 6. Smart pages: Arc List, Arc Detail, Arc Create
  - [x] 6.1 Create ArcListPage smart component
    - Create `frontend/src/features/arc-mode/pages/arc-list/arc-list.page.ts` (standalone, OnPush)
    - Create `arc-list.page.html` and `arc-list.page.scss`
    - Inject `ArcStore` and `Router`
    - Implement `selectedTab` signal with default logic (my-arcs if active arcs exist, else explore)
    - Render ion-segment with three tabs (Explore, My Arcs, Completed)
    - Render arc cards using shared `app-arc-card` component
    - Show `skeleton-loader` during loading, `error-state` on failure with retry
    - Show empty state messages per tab context
    - Include FAB "Create Arc" button navigating to /arc-mode/create
    - Implement `onArcTap(arcId)` navigating to /arc-mode/:id
    - Add `role="tablist"` and `aria-selected` attributes on tabs
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 2.1, 2.2, 2.3, 2.4, 2.5, 13.1, 13.3, 14.1_

  - [x]* 6.2 Write property tests for Arc List Page tab logic
    - **Property 2: Default tab selection follows active arc presence**
    - **Property 3: Empty state message matches selected tab context**
    - **Validates: Requirements 1.5, 1.7**

  - [x] 6.3 Create ArcDetailPage smart component
    - Create `frontend/src/features/arc-mode/pages/arc-detail/arc-detail.page.ts` (standalone, OnPush)
    - Create `arc-detail.page.html` and `arc-detail.page.scss`
    - Inject `ArcStore`, `ActivatedRoute`, `Router`
    - Read arc ID from route params and call `arcStore.loadArcDetail(id)`
    - Compose all dumb components: cinematic-banner, phase-progress, identity-title, milestone-timeline, boss-section, rewards-section, skill-tree-preview
    - Show `skeleton-loader` during loading, `error-state` on failure with retry
    - Implement back button navigating to /arc-mode
    - Implement `onSkillTreeTap()` navigating to /skill-tree with arcId query param
    - Use semantic heading hierarchy (h1 arc name, h2 section titles)
    - _Requirements: 3.1, 3.6, 3.7, 4.1, 5.1, 6.1, 7.1, 8.1, 10.1, 12.2, 12.3, 13.2, 13.4, 14.5_

  - [x] 6.4 Create ArcCreatePage smart component
    - Create `frontend/src/features/arc-mode/pages/arc-create/arc-create.page.ts` (standalone, OnPush)
    - Create `arc-create.page.html` and `arc-create.page.scss`
    - Inject `ArcStore`, `Router`, `ToastController`
    - Implement reactive form with fields: title, goal, durationDays, milestones (FormArray), questFrequency
    - Add validators: title (required, maxLength 100), goal (required, maxLength 500), duration (required, min 30, max 90), milestones (at least 1), questFrequency (required)
    - Implement `addMilestone()` and `removeMilestone(index)` with minimum-1 guard
    - Implement `onSubmit()` calling `arcStore.createArc()`, showing success toast, navigating to detail
    - Display inline validation errors on blur
    - Disable submit button when form invalid or submitting
    - Show loading spinner on submit button during submission
    - Include back button navigating to /arc-mode
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7, 9.8, 9.9, 9.11, 9.12, 12.4, 13.5, 14.3_

  - [x]* 6.5 Write property tests for Arc Creation Form validation
    - **Property 10: Form validation rejects invalid inputs and controls button state**
    - **Validates: Requirements 9.2, 9.3, 9.4, 9.5, 9.12**

- [x] 7. Routing and integration
  - [x] 7.1 Update arc-mode routes and remove placeholder
    - Update `frontend/src/features/arc-mode/arc-mode.routes.ts` with lazy-loaded routes for ArcListPage (path: ''), ArcCreatePage (path: 'create'), and ArcDetailPage (path: ':id')
    - Delete the placeholder `frontend/src/features/arc-mode/pages/arc-mode.component.ts`
    - Ensure 'create' route is defined before ':id' to avoid route conflicts
    - _Requirements: 12.1, 12.2_

  - [x] 7.2 Create component barrel exports
    - Create `frontend/src/features/arc-mode/components/index.ts` barrel exporting all dumb components
    - Create `frontend/src/features/arc-mode/services/index.ts` barrel exporting ArcService
    - Create `frontend/src/features/arc-mode/store/index.ts` barrel exporting ArcStore
    - _Requirements: 12.1_

- [x] 8. Final checkpoint - Ensure all components compile and integrate
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties from the design document
- Unit tests validate specific examples and edge cases
- The existing placeholder component at `pages/arc-mode.component.ts` will be removed in task 7.1
- Shared components (`app-arc-card`, `app-boss-card`, `app-xp-progress-bar`, `skeleton-loader`, `error-state`, `stepper`) are already available in `frontend/src/shared/`
- Models `Arc`, `Boss`, and `ArcType` enum already exist in `frontend/src/shared/models/` and `frontend/src/shared/enums/`

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1"] },
    { "id": 1, "tasks": ["1.2"] },
    { "id": 2, "tasks": ["1.3"] },
    { "id": 3, "tasks": ["1.4", "3.1", "3.2", "3.3"] },
    { "id": 4, "tasks": ["3.4", "4.1", "4.2", "4.3", "4.4"] },
    { "id": 5, "tasks": ["4.5", "6.1"] },
    { "id": 6, "tasks": ["6.2", "6.3", "6.4"] },
    { "id": 7, "tasks": ["6.5", "7.1", "7.2"] }
  ]
}
```
