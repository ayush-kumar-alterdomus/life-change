# Implementation Plan: UI Leveling

## Overview

Implement the Level-Up Celebration UI as a self-contained Angular feature module under `frontend/src/features/leveling/`. The implementation follows a bottom-up approach: data models and utilities first, then the core service with state machine and event sources, followed by the component tree (overlay, animations, prestige), and finally integration wiring and accessibility.

## Tasks

- [x] 1. Set up feature module structure and data models
  - [x] 1.1 Create data models and type definitions
    - Create `frontend/src/features/leveling/models/level-up.models.ts`
    - Define interfaces: `LevelUpEvent`, `LevelReward`, `MilestoneConfig`, `CelebrationItem`, `CelebrationStep`, `CelebrationFlowState`, `PrestigeData`
    - Export all types from the models file
    - _Requirements: 1.1, 2.4, 5.3, 7.1, 12.1_

  - [x] 1.2 Create milestone configuration utility
    - Create `frontend/src/features/leveling/utils/milestone-config.ts`
    - Define `MILESTONE_CONFIGS` array with all four milestones (Level 10: Leagues, Level 25: Guilds, Level 50: Elite Cosmetics, Level 100: Prestige System)
    - Implement `getMilestoneConfig(level)` and `isMilestoneLevel(level)` functions
    - _Requirements: 6.1, 6.3, 6.4, 6.5, 6.6_

  - [x] 1.3 Create celebration queue utility
    - Create `frontend/src/features/leveling/utils/celebration-queue.ts`
    - Implement `decomposeLevelJump(event: LevelUpEvent): CelebrationItem[]` that splits multi-level jumps into individual items
    - Each item includes correct `queuePosition`, `queueTotal`, milestone detection, and reward assignment
    - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5_

  - [ ]* 1.4 Write property tests for milestone configuration
    - **Property 8: Milestone level detection is bidirectional**
    - **Property 9: Milestone content correctness**
    - **Validates: Requirements 6.1, 6.2, 6.8**

  - [ ]* 1.5 Write property tests for celebration queue decomposition
    - **Property 19: Multi-level jump decomposition count**
    - **Property 20: Multi-level celebrations process in ascending order**
    - **Property 21: Multi-level milestone inclusion**
    - **Property 22: Queue position indicator correctness**
    - **Validates: Requirements 12.1, 12.2, 12.4, 12.5**

- [x] 2. Implement LevelUpService core logic
  - [x] 2.1 Create LevelUpService with signal-based state and deduplication
    - Create `frontend/src/features/leveling/services/level-up.service.ts`
    - Implement as singleton (`providedIn: 'root'`) with signal-based `CelebrationFlowState`
    - Expose read-only signals: `flowState`, `celebrationActive`, `currentStep`, `currentItem`, `queueIndicator`
    - Implement deduplication: `shouldProcessEvent()`, `persistLastProcessedLevel()`, `loadLastProcessedLevel()` using localStorage
    - Implement `triggerLevelUp(event)` that validates, deduplicates, decomposes multi-level jumps, and enqueues items
    - _Requirements: 1.1, 1.5, 1.6, 11.1, 11.2, 11.5_

  - [x] 2.2 Implement celebration state machine transitions
    - Implement `advanceStep()` method with full state machine transition logic
    - Implement `dismiss()` method with fade-out timing (300ms) and queue processing (500ms delay)
    - Implement `activatePrestige()` method for Level 100 prestige flow
    - Handle coordination with `QuestCompletionService` (wait for quest animation to complete before starting celebration)
    - _Requirements: 2.4, 7.1, 8.4, 8.5, 11.3, 11.4_

  - [x] 2.3 Implement WebSocket subscription for level events
    - Implement `connectWebSocket(userId)` using RxStomp to subscribe to `/user/{userId}/queue/level`
    - Parse incoming messages, filter for `LEVEL_UP` type, and call `triggerLevelUp()`
    - Implement `disconnectWebSocket()` for cleanup on logout/destroy
    - Implement `ngOnDestroy()` lifecycle hook
    - _Requirements: 1.2, 1.4, 1.7_

  - [x] 2.4 Implement Lottie asset preloading
    - Preload glow explosion Lottie JSON during service constructor
    - Expose `glowExplosionData` for the glow explosion component
    - Implement on-demand loading for prestige animation (only at Level 100)
    - _Requirements: 3.6, 10.1, 10.5_

  - [ ]* 2.5 Write property tests for LevelUpService deduplication
    - **Property 17: Deduplication discards stale events**
    - **Property 18: Last processed level persistence round-trip**
    - **Validates: Requirements 11.2, 11.5**

  - [ ]* 2.6 Write property tests for event queue processing
    - **Property 2: Event queue preserves sequential processing order**
    - **Property 3: celebrationActive signal reflects overlay state**
    - **Validates: Requirements 1.5, 1.6, 8.5**

  - [ ]* 2.7 Write property tests for state machine transitions
    - **Property 4: Celebration state machine executes steps in correct order**
    - **Property 11: Continue button appears only after all steps complete**
    - **Validates: Requirements 2.4, 6.1, 6.8, 8.1**

- [x] 3. Implement HTTP interceptor for level detection
  - [x] 3.1 Create level-up HTTP interceptor
    - Create `frontend/src/features/leveling/interceptors/level-up.interceptor.ts`
    - Implement `levelUpInterceptor` as an `HttpInterceptorFn`
    - Detect `newLevel` field in any HTTP response body
    - Construct `LevelUpEvent` from response data and call `levelUpService.triggerLevelUp()`
    - Register interceptor in the app's HTTP provider configuration
    - _Requirements: 1.3_

  - [ ]* 3.2 Write property test for API response level detection
    - **Property 1: API response level detection triggers celebration**
    - **Validates: Requirements 1.3**

- [x] 4. Checkpoint - Core service and event sources
  - Ensure all tests pass, ask the user if questions arise.

- [x] 5. Implement animation definitions
  - [x] 5.1 Create Angular animation triggers
    - Create `frontend/src/features/leveling/components/celebration-overlay/celebration-overlay.animations.ts`
    - Implement `flyUpAnimation` trigger (translateY 40px → 0, opacity 0 → 1, 200ms ease-out)
    - Implement `slideUpAnimation` trigger (translateY 100% → 0, opacity 0 → 1, 400ms ease-out)
    - Implement `scaleInAnimation` trigger (scale 0 → 1, opacity 0 → 1, 300ms ease-out)
    - Implement `fadeInAnimation` trigger (opacity 0 → 1, 200ms ease-out)
    - Implement `fadeOutAnimation` trigger (opacity 1 → 0, 300ms ease-out)
    - Implement `staggeredFadeIn` trigger (100ms stagger between items, translateY 10px → 0)
    - All animations use only `transform` and `opacity` for GPU acceleration
    - _Requirements: 2.6, 3.5, 4.3, 4.4, 4.5, 5.1, 5.5, 6.7, 8.3, 10.2, 10.3_

- [x] 6. Implement celebration overlay and child components
  - [x] 6.1 Create GlowExplosion component
    - Create `frontend/src/features/leveling/components/glow-explosion/` component files
    - Standalone component using `ngx-lottie` `LottieComponent`
    - Bind preloaded animation data from `LevelUpService`
    - Emit `progress` events (percentage) and `complete` event from Lottie `enterFrame`/`complete` listeners
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

  - [x] 6.2 Create XpFlyUpNumber component
    - Create `frontend/src/features/leveling/components/xp-fly-up-number/` component files
    - Standalone component with `@Input() level: number`
    - Display "Level {N}" using Orbitron font
    - Apply `flyUpAnimation` trigger on entrance
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6_

  - [x] 6.3 Create RewardsCard component
    - Create `frontend/src/features/leveling/components/rewards-card/` component files
    - Standalone component with `@Input() rewards: LevelReward[]` and `@Input() level: number`
    - Apply `slideUpAnimation` on entrance
    - Render reward list with type icon, name, and amount/value
    - Apply `staggeredFadeIn` to reward items (100ms delay between each)
    - Handle empty rewards case: display "Level {N} Achieved" with level badge
    - Style with dark card background (#161616), 16px border-radius, orange border at 30% opacity
    - Include `aria-label` listing all rewards
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 9.4_

  - [x] 6.4 Create FeatureUnlockAnnouncement component
    - Create `frontend/src/features/leveling/components/feature-unlock-announcement/` component files
    - Standalone component with `@Input() milestoneConfig: MilestoneConfig`
    - Display feature name, tagline, and icon
    - Apply `scaleInAnimation` on entrance with mystic purple (#A855F7) accent glow
    - Include `aria-live="polite"` announcement for the unlocked feature
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 9.5_

  - [ ]* 6.5 Write property tests for component rendering
    - **Property 5: Level number display format**
    - **Property 6: Rewards card displays all reward items**
    - **Property 7: Staggered reward animation timing**
    - **Validates: Requirements 4.2, 5.2, 5.5**

  - [ ]* 6.6 Write property tests for accessibility
    - **Property 13: Aria-live level announcement format**
    - **Property 14: Rewards aria-label completeness**
    - **Validates: Requirements 9.3, 9.4**

- [x] 7. Implement CelebrationOverlay container component
  - [x] 7.1 Create CelebrationOverlay component with full celebration flow
    - Create `frontend/src/features/leveling/components/celebration-overlay/` component files
    - Standalone component importing all child components
    - Full-screen overlay with dark background (#0A0A0A), full viewport coverage, z-index above all content
    - Orchestrate sequential step rendering based on `currentStep` signal from `LevelUpService`
    - Coordinate glow progress events (50% → show title, 75% → show fly-up, 100% → show rewards)
    - Conditionally render `FeatureUnlockAnnouncement` for milestone levels
    - Render "Continue Journey" button (success green #4CAF50, 44x44px min touch target) after all steps complete
    - Render queue indicator ("1 of 3") when multiple celebrations queued
    - Apply `fadeInAnimation` on entrance, `fadeOutAnimation` on exit
    - Apply `will-change` CSS hint during animations, remove after completion
    - Conditionally render with `@if (celebrationActive())` for DOM cleanup
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 8.1, 8.2, 8.3, 8.4, 8.6, 10.4, 10.6, 12.5_

  - [x] 7.2 Implement accessibility features in CelebrationOverlay
    - Add `role="dialog"`, `aria-modal="true"`, `aria-label="Level up celebration"` to host
    - Implement focus trap: save previously focused element on init, restore on destroy
    - Add `aria-live="assertive"` region announcing "Level up! You reached level {N}"
    - Auto-focus "Continue Journey" button when it appears
    - Implement Escape key dismissal (only when in `continue-ready` state)
    - _Requirements: 9.1, 9.2, 9.3, 9.6, 9.7_

  - [ ]* 7.3 Write property tests for overlay lifecycle
    - **Property 12: Focus trap and restoration**
    - **Property 15: DOM cleanup after dismissal**
    - **Property 16: will-change lifecycle management**
    - **Validates: Requirements 9.1, 10.4, 10.6**

- [x] 8. Checkpoint - Components and overlay
  - Ensure all tests pass, ask the user if questions arise.

- [x] 9. Implement Prestige Screen
  - [x] 9.1 Create PrestigeScreen component
    - Create `frontend/src/features/leveling/components/prestige-screen/` component files
    - Standalone component with on-demand Lottie animation loading (prestige-ascend.json)
    - Display prestige level counting animation (previous → new over 800ms)
    - Display prestige badge with mystic purple glow reveal animation
    - Display "Prestige {P}" text in Orbitron font with gold gradient (#FFD700 to #FF9800)
    - Display summary: "Level reset to 1" and "Prestige Badge Earned"
    - Include "Begin New Journey" button that dismisses and navigates to dashboard
    - Lottie animation: ascending particle trails + badge materialization, 2000ms duration
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8_

  - [ ]* 9.2 Write property test for prestige text format
    - **Property 10: Prestige text format**
    - **Validates: Requirements 7.4**

- [x] 10. Integration and wiring
  - [x] 10.1 Register HTTP interceptor and wire LevelUpService to app
    - Register `levelUpInterceptor` in the app's `provideHttpClient(withInterceptors([...]))` configuration
    - Wire `LevelUpService.connectWebSocket()` call to the authentication flow (on login success)
    - Wire `LevelUpService.disconnectWebSocket()` call to the logout flow
    - _Requirements: 1.2, 1.3, 1.7_

  - [x] 10.2 Integrate CelebrationOverlay into app shell
    - Add `<app-celebration-overlay>` (conditionally rendered) to the app's root layout or shell component
    - Ensure overlay renders above all navigation elements (ion-tabs, ion-router-outlet)
    - Wire coordination with `QuestCompletionService` — listen for quest animation completion before starting celebration
    - _Requirements: 2.1, 2.3, 11.3, 11.4_

  - [x] 10.3 Create feature barrel export
    - Create `frontend/src/features/leveling/index.ts`
    - Export `LevelUpService`, `levelUpInterceptor`, `CelebrationOverlayComponent`, and all models
    - _Requirements: 1.1_

  - [ ]* 10.4 Write integration tests for event flow
    - Test HTTP interceptor detecting `newLevel` in real response shapes
    - Test WebSocket message parsing and celebration triggering
    - Test coordination with QuestCompletionService (wait for animation)
    - Test full celebration flow end-to-end with mocked timers
    - _Requirements: 1.3, 1.4, 11.3, 11.4_

- [x] 11. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties from the design document (22 properties total)
- Unit tests validate specific examples and edge cases
- All components are standalone Angular components using signals
- Tech stack: Angular 17+, Ionic, ngx-lottie, RxStomp, Angular Animations (Web Animations API)
- All animations use only `transform` and `opacity` for GPU-accelerated 60fps rendering

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.2", "1.3"] },
    { "id": 1, "tasks": ["1.4", "1.5", "2.1", "5.1"] },
    { "id": 2, "tasks": ["2.2", "2.3", "2.4", "2.5"] },
    { "id": 3, "tasks": ["2.6", "2.7", "3.1", "6.1", "6.2", "6.3", "6.4"] },
    { "id": 4, "tasks": ["3.2", "6.5", "6.6", "7.1"] },
    { "id": 5, "tasks": ["7.2", "9.1"] },
    { "id": 6, "tasks": ["7.3", "9.2", "10.1", "10.2", "10.3"] },
    { "id": 7, "tasks": ["10.4"] }
  ]
}
```
