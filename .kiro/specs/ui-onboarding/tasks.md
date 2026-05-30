# Implementation Plan: UI Onboarding

## Overview

This plan implements the multi-step onboarding personalization wizard for the Ascend app: Goal Selection (6 neon-glow cards, multi-select 1–3), Difficulty Selection (Casual/Balanced/Beast Mode single-select), Personality Assessment Quiz (sequential questions with auto-advance), Arc Recommendation (deterministic algorithm + override), Avatar Selection (grid with preview), and state persistence via Capacitor Preferences. The implementation builds incrementally — data models and constants first, then OnboardingStore and OnboardingService, then shared UI (Stepper, GlowCard directive), then step components (goal → difficulty → quiz → arc → avatar), then the container page with routing/guards, and finally wiring everything together.

## Tasks

- [x] 1. Create onboarding data models, constants, and utility functions
  - [x] 1.1 Create onboarding data models and TypeScript interfaces
    - Create `src/features/onboarding/models/onboarding-state.model.ts` with `OnboardingState` interface (`currentStep: number`, `selectedGoals: string[]`, `selectedDifficulty: string | null`, `quizAnswers: QuizAnswer[]`, `personalityType: string | null`, `selectedArc: string | null`, `selectedAvatar: string | null`)
    - Create `src/features/onboarding/models/quiz.model.ts` with `QuizAnswer`, `QuizQuestion`, `QuizOption`, and `PersonalityDimension` interfaces/types
    - Create `src/features/onboarding/models/goal-category.model.ts` with `GoalCategory` interface
    - Create `src/features/onboarding/models/difficulty-tier.model.ts` with `DifficultyTier` interface
    - Create `src/features/onboarding/models/arc-definition.model.ts` with `ArcDefinition` interface
    - Create `src/features/onboarding/models/avatar-option.model.ts` with `AvatarOption` interface
    - Create `src/features/onboarding/models/onboarding-payload.model.ts` with `OnboardingPayload` interface
    - Create barrel export `src/features/onboarding/models/index.ts`
    - _Requirements: 9.1, 12.4_

  - [x] 1.2 Create onboarding constants and static data
    - Create `src/features/onboarding/constants/onboarding.constants.ts` with `ONBOARDING_CONSTANTS` (TOTAL_STEPS: 5, MIN_GOALS: 1, MAX_GOALS: 3, MIN_STEP: 0, MAX_STEP: 4, QUIZ_QUESTION_COUNT: 6, TRANSITION_DURATION_MS: 300, QUIZ_AUTO_ADVANCE_DELAY_MS: 400, GLOW_TRANSITION_MS: 200, PRESS_SCALE_DURATION_MS: 100)
    - Create `src/features/onboarding/constants/goal-categories.ts` with `GOAL_CATEGORIES` array (Fitness, Career, Mindfulness, Relationships, Finance, Learning) each with id, name, description, and ionicon
    - Create `src/features/onboarding/constants/difficulty-tiers.ts` with `DIFFICULTY_TIERS` array (casual, balanced, beast_mode) with subtitles, flame counts, recommended flag, and glow colors
    - Create `src/features/onboarding/constants/available-arcs.ts` with `AVAILABLE_ARCS` array (Monk, Warrior, Scholar, Creator, Beast Mode) with descriptions, theme colors, sample quests, and icons
    - Create `src/features/onboarding/constants/quiz-questions.ts` with 6 quiz questions covering discipline_style, motivation_triggers, challenge_type, and time_availability dimensions
    - Create `src/features/onboarding/constants/avatar-options.ts` with at least 8 starter avatar options (id, name, imageUrl)
    - Create barrel export `src/features/onboarding/constants/index.ts`
    - _Requirements: 2.1, 2.7, 3.1, 3.2, 3.3, 3.6, 4.1, 4.5, 5.1, 5.5, 6.1_

  - [x] 1.3 Create personality computation and Arc recommendation engine
    - Create `src/features/onboarding/utils/personality-computation.ts` with `computePersonalityType(answers: QuizAnswer[], questions: QuizQuestion[]): string` — pure deterministic function that computes personality type from weighted answer scores
    - Create `src/features/onboarding/utils/arc-recommendation.engine.ts` with `computeRecommendedArc(goals: string[], difficulty: string, personalityType: string): string` — pure deterministic function that returns an Arc id from AVAILABLE_ARCS based on goal affinities, difficulty multiplier, and personality bias
    - Create `src/features/onboarding/utils/state-validation.ts` with `validateOnboardingState(raw: unknown): OnboardingState | null` — validates deserialized state structure (currentStep 0–4, selectedGoals array 0–3 strings, etc.)
    - Create barrel export `src/features/onboarding/utils/index.ts`
    - _Requirements: 4.6, 4.7, 5.2, 8.6, 8.7, 12.5, 12.6_

- [x] 2. Implement OnboardingStore and OnboardingService
  - [x] 2.1 Create OnboardingStore with signal-based state management
    - Create `src/features/onboarding/services/onboarding.store.ts`
    - Implement as `@Injectable({ providedIn: 'root' })` singleton
    - Define private writable signals: `_currentStep`, `_selectedGoals`, `_selectedDifficulty`, `_quizAnswers`, `_personalityType`, `_selectedArc`, `_selectedAvatar`
    - Expose public readonly signals via `.asReadonly()`
    - Implement computed signal `isStepValid` with step-specific validation logic (step 0: 1–3 goals, step 1: non-null difficulty, step 2: all questions answered, step 3: non-null arc, step 4: non-null avatar)
    - Implement mutator methods: `setCurrentStep()`, `setGoals()`, `setDifficulty()`, `setQuizAnswers()`, `setPersonalityType()`, `setArc()`, `setAvatar()`
    - Implement `getSnapshot(): OnboardingState` returning plain object for serialization
    - Implement `restore(state: OnboardingState): void` to hydrate all signals from validated state
    - Implement `reset(): void` to clear all signals to initial values
    - _Requirements: 9.1, 9.2, 1.5, 2.8, 3.7, 6.6_

  - [ ]* 2.2 Write property test for step navigation preserves invariants (Property 1)
    - **Property 1: Step navigation preserves invariants**
    - For any valid onboarding state with currentStep in [0,4], advanceStep increments by 1 (when < 4) while all other fields remain unchanged; goBack decrements by 1 (when > 0) while all other fields remain unchanged
    - Create `src/features/onboarding/services/__tests__/onboarding.store.property.spec.ts`
    - **Validates: Requirements 1.3, 1.4**

  - [ ]* 2.3 Write property test for isStepValid computed signal (Property 2)
    - **Property 2: isStepValid reflects step-specific criteria**
    - For any combination of step index (0–4) and state values, isStepValid returns true iff the step-specific criteria are met
    - Add to `src/features/onboarding/services/__tests__/onboarding.store.property.spec.ts`
    - **Validates: Requirements 1.5, 2.8, 3.7, 6.6, 9.2**

  - [x] 2.4 Create OnboardingApiService for backend communication
    - Create `src/features/onboarding/services/onboarding-api.service.ts`
    - Implement as `@Injectable({ providedIn: 'root' })` singleton
    - Inject `HttpClient` and `API_URL` token
    - Implement `submitOnboarding(payload: OnboardingPayload): Observable<void>` calling `PUT /api/v1/users/onboarding`
    - _Requirements: 7.1, 9.5_

  - [x] 2.5 Create OnboardingService with state management, persistence, and completion logic
    - Create `src/features/onboarding/services/onboarding.service.ts`
    - Implement as `@Injectable({ providedIn: 'root' })` singleton
    - Inject `OnboardingStore`, `StorageService`, `OnboardingApiService`, `HapticService`, `NavController`
    - Implement `initialize(): Promise<void>` — reads `onboarding_state` from StorageService, validates with `validateOnboardingState()`, restores or starts fresh
    - Implement `advanceStep(): void` — increments currentStep (guard: < 4), persists state
    - Implement `goBack(): void` — decrements currentStep (guard: > 0), persists state
    - Implement `setGoals(goals: string[]): void` — enforces max 3, updates store, persists
    - Implement `setDifficulty(tier: string): void` — updates store, persists
    - Implement `addQuizAnswer(answer: QuizAnswer): void` — appends answer, computes personality type if final answer, persists
    - Implement `setArc(arcId: string): void` — updates store, persists
    - Implement `setAvatar(avatarId: string): void` — updates store, persists
    - Implement `completeOnboarding(): Observable<void>` — submits payload via OnboardingApiService, on success: sets `onboarding_complete` flag, removes `onboarding_state` key, triggers medium haptic, navigates to `/tabs/home`, resets store
    - Implement `reset(): void` — resets store, removes persisted state
    - Implement private `persistState(): Promise<void>` — serializes store snapshot to JSON, saves via StorageService with key `onboarding_state`
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7, 9.3, 9.4, 9.5, 9.6, 12.1, 12.2_

  - [ ]* 2.6 Write property test for goal selection bounds enforcement (Property 3)
    - **Property 3: Goal selection enforces 1–3 bounds**
    - For any sequence of goal toggle operations, selectedGoals never exceeds 3 elements; adding a 4th is rejected
    - Create `src/features/onboarding/services/__tests__/onboarding.service.property.spec.ts`
    - **Validates: Requirements 2.4, 2.5, 2.6**

  - [ ]* 2.7 Write property test for single-select fields (Property 4)
    - **Property 4: Single-select fields always equal last selection**
    - For any sequence of selections on difficulty or avatar, the signal value always equals the most recently selected item
    - Add to `src/features/onboarding/services/__tests__/onboarding.service.property.spec.ts`
    - **Validates: Requirements 3.4, 6.3**

  - [ ]* 2.8 Write property test for serialization round-trip (Property 7)
    - **Property 7: Onboarding state serialization round-trip**
    - For any valid OnboardingState object, JSON.stringify then JSON.parse produces a deeply equal object
    - Create `src/features/onboarding/services/__tests__/onboarding.service.serialization.property.spec.ts`
    - **Validates: Requirements 8.1, 8.2, 12.1, 12.2, 12.3**

  - [ ]* 2.9 Write property test for state validation (Property 8)
    - **Property 8: State validation correctly rejects invalid structures**
    - For any value not conforming to OnboardingState constraints, validateState returns null; for conforming values, it returns the validated object
    - Create `src/features/onboarding/utils/__tests__/state-validation.property.spec.ts`
    - **Validates: Requirements 8.7, 12.5, 12.6**

- [x] 3. Checkpoint - Ensure models, store, and service compile and pass tests
  - Ensure all tests pass, ask the user if questions arise.

- [x] 4. Implement shared UI components (Stepper and GlowCard directive)
  - [x] 4.1 Create StepperComponent
    - Create `src/shared/components/stepper/stepper.component.ts`, `.html`, `.scss`
    - Standalone component with `@Input({ required: true }) totalSteps: number` and `@Input({ required: true }) currentStep: number`
    - Render dots/segments: filled with primary accent (`#FF9800`) for completed steps, pulsing glow animation on current step, muted gray for upcoming steps
    - Use CSS keyframe animation for the pulsing glow on current step indicator
    - _Requirements: 1.2, 10.5_

  - [x] 4.2 Create GlowCardDirective
    - Create `src/shared/directives/glow-card/glow-card.directive.ts`
    - Standalone directive with selector `[appGlowCard]`
    - Inputs: `appGlowCard: boolean` (active state), `glowColor: string` (default `#FF9800`), `glowRadius: string` (default `12px`)
    - When active: apply CSS `box-shadow` with glow color at 60% opacity and configured radius, add `border-color` matching glow color
    - When inactive: remove glow styles
    - Include 200ms ease-in transition for glow application
    - Include 0.97 scale transform for 100ms on host element press (`:active` or touch event)
    - _Requirements: 2.2, 3.4, 6.3, 10.2, 10.6_

  - [ ]* 4.3 Write unit tests for StepperComponent and GlowCardDirective
    - Test StepperComponent renders correct number of dots
    - Test completed/current/upcoming step styling
    - Test pulsing animation class on current step
    - Test GlowCardDirective applies box-shadow when active
    - Test GlowCardDirective removes styles when inactive
    - Test glow color customization
    - Test press scale feedback
    - Create `src/shared/components/__tests__/stepper.component.spec.ts`
    - Create `src/shared/directives/__tests__/glow-card.directive.spec.ts`
    - _Requirements: 1.2, 2.2, 10.2, 10.5, 10.6_

- [x] 5. Implement Goal Selection step component
  - [x] 5.1 Create GoalSelectionStep component with 6 goal cards in 2-column grid
    - Create `src/features/onboarding/steps/goal-selection/goal-selection.step.ts`, `.html`, `.scss`
    - Standalone component injecting `OnboardingService`, `OnboardingStore`, `HapticService`
    - Display 6 `GoalCategory` cards from `GOAL_CATEGORIES` in a responsive 2-column CSS grid
    - Each card shows ionicon, category name, and one-line description on dark glass background (`#161616`) with 12px border-radius
    - Apply `[appGlowCard]` directive bound to selection state for each card
    - On card tap: toggle goal selection via `OnboardingService.setGoals()`, trigger light haptic
    - If 3 goals already selected and user taps a 4th: show toast "Maximum 3 goals allowed" and prevent selection
    - On deselect: remove goal from selection, trigger light haptic
    - Minimum 44px touch target height on all cards
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 10.1, 10.4_

  - [ ]* 5.2 Write unit tests for GoalSelectionStep
    - Test 6 cards rendered in 2-column grid
    - Test card content (icon, name, description)
    - Test single goal selection applies glow
    - Test multi-select up to 3 goals
    - Test 4th goal rejection with toast
    - Test deselection removes glow
    - Test haptic feedback on tap
    - Test dark glass background styling
    - Create `src/features/onboarding/__tests__/goal-selection.step.spec.ts`
    - _Requirements: 2.1–2.8_

- [x] 6. Implement Difficulty Selection step component
  - [x] 6.1 Create DifficultySelectionStep component with 3 tier cards
    - Create `src/features/onboarding/steps/difficulty-selection/difficulty-selection.step.ts`, `.html`, `.scss`
    - Standalone component injecting `OnboardingService`, `OnboardingStore`, `HapticService`
    - Display 3 `DifficultyTier` cards from `DIFFICULTY_TIERS` in single-column vertical layout
    - Each card shows tier name, descriptive subtitle, and flame icons (1/2/3)
    - Balanced tier card displays "Recommended" badge with secondary accent color (`#A855F7`)
    - Beast Mode card uses darker dramatic background gradient and red-tinted glow (`#F44336`)
    - Apply `[appGlowCard]` directive with tier-specific `glowColor` bound to selection state
    - On card tap: set difficulty via `OnboardingService.setDifficulty()`, trigger light haptic (single-select replaces previous)
    - Minimum 44px touch target height
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 10.1, 10.4_

  - [ ]* 6.2 Write unit tests for DifficultySelectionStep
    - Test 3 tier cards rendered vertically
    - Test card content (name, subtitle, flames)
    - Test "Recommended" badge on Balanced
    - Test Beast Mode red glow and gradient
    - Test single-select behavior (selecting new deselects previous)
    - Test haptic feedback on tap
    - Create `src/features/onboarding/__tests__/difficulty-selection.step.spec.ts`
    - _Requirements: 3.1–3.7_

- [x] 7. Implement Personality Quiz step component
  - [x] 7.1 Create QuizStep component with sequential questions and auto-advance
    - Create `src/features/onboarding/steps/quiz/quiz.step.ts`, `.html`, `.scss`
    - Standalone component injecting `OnboardingService`, `OnboardingStore`, `HapticService`
    - Display one question at a time from `QUIZ_QUESTIONS` with progress indicator (e.g., "Question 3 of 6")
    - Each question shows question text and 3–4 answer option cards
    - On answer tap: highlight selected option with primary accent, trigger light haptic, auto-advance to next question after 400ms delay
    - Allow back navigation to previous question with previous answer pre-selected (read from `quizAnswers` in store)
    - On final question answer: compute personality type via `computePersonalityType()`, store result, auto-advance to Arc Recommendation step
    - No separate "Continue" button — advancing is handled by answer selection
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8_

  - [ ]* 7.2 Write property test for personality type determinism (Property 5)
    - **Property 5: Personality type computation is deterministic**
    - For any valid set of quiz answers, computePersonalityType always produces the same result for the same inputs
    - Create `src/features/onboarding/utils/__tests__/personality-computation.property.spec.ts`
    - **Validates: Requirements 4.6, 4.7**

  - [ ]* 7.3 Write unit tests for QuizStep
    - Test sequential question display
    - Test progress indicator updates
    - Test answer highlight on selection
    - Test haptic on answer tap
    - Test 400ms auto-advance delay
    - Test back navigation with pre-selected answer
    - Test personality type computation on final answer
    - Test no Continue button rendered
    - Create `src/features/onboarding/__tests__/quiz.step.spec.ts`
    - _Requirements: 4.1–4.8_

- [x] 8. Implement Arc Recommendation step component
  - [x] 8.1 Create ArcRecommendationStep component with hero card and override
    - Create `src/features/onboarding/steps/arc-recommendation/arc-recommendation.step.ts`, `.html`, `.scss`
    - Standalone component injecting `OnboardingService`, `OnboardingStore`
    - On init: compute recommended Arc via `computeRecommendedArc(goals, difficulty, personalityType)` and set in store
    - Display hero card with recommended Arc name, description, icon, theme color gradient background, and 3–4 sample quests
    - Display primary button "Begin This Arc" that accepts recommendation and advances to Avatar Selection
    - Display secondary text link "Choose a different Arc" below primary button
    - On "Choose a different Arc" tap: expand/show list of all 5 available Arcs as selectable cards with recommended Arc visually marked
    - On alternative Arc selection: update store via `OnboardingService.setArc()`
    - Cinematic dark aesthetic with Arc theme color as subtle background gradient
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 5.8_

  - [ ]* 8.2 Write property test for Arc recommendation validity (Property 6)
    - **Property 6: Arc recommendation produces a valid Arc from available set**
    - For any valid combination of goals, difficulty, and personality type, computeRecommendedArc returns an id that exists in AVAILABLE_ARCS
    - Create `src/features/onboarding/utils/__tests__/arc-recommendation.engine.property.spec.ts`
    - **Validates: Requirements 5.2**

  - [ ]* 8.3 Write unit tests for ArcRecommendationStep
    - Test hero card displays Arc details (name, description, icon, quests)
    - Test "Begin This Arc" button advances to avatar step
    - Test "Choose a different Arc" reveals override list
    - Test override selection updates store
    - Test recommended Arc marked in override list
    - Test cinematic styling with theme color gradient
    - Create `src/features/onboarding/__tests__/arc-recommendation.step.spec.ts`
    - _Requirements: 5.1–5.8_

- [x] 9. Implement Avatar Selection step component
  - [x] 9.1 Create AvatarSelectionStep component with grid and preview
    - Create `src/features/onboarding/steps/avatar-selection/avatar-selection.step.ts`, `.html`, `.scss`
    - Standalone component injecting `OnboardingService`, `OnboardingStore`, `HapticService`
    - Display scrollable grid of 8+ avatar options (2–3 columns depending on screen width)
    - Each avatar displayed as circular image (64px min diameter) with label below
    - Apply `[appGlowCard]` directive (ring variant) bound to selection state
    - Display larger preview (128px diameter) of currently selected avatar above the grid
    - On avatar tap: set avatar via `OnboardingService.setAvatar()`, trigger light haptic (single-select)
    - Display "Complete" button disabled until avatar selected
    - On "Complete" tap: show loading spinner, disable button, call `OnboardingService.completeOnboarding()`
    - On success: toast "Welcome to Ascend! Your Arc begins now." (handled by service navigation)
    - On failure: display inline error "Could not save your profile. Please try again." with "Retry" button
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7_

  - [ ]* 9.2 Write unit tests for AvatarSelectionStep
    - Test 8+ avatars rendered in grid
    - Test circular avatar styling (64px)
    - Test selection applies glow ring
    - Test 128px preview of selected avatar
    - Test haptic on tap
    - Test Complete button disabled until selection
    - Test loading spinner during submission
    - Test success navigation and toast
    - Test error display with retry
    - Create `src/features/onboarding/__tests__/avatar-selection.step.spec.ts`
    - _Requirements: 6.1–6.7, 7.1–7.7_

- [x] 10. Checkpoint - Ensure all step components compile and pass tests
  - Ensure all tests pass, ask the user if questions arise.

- [x] 11. Implement OnboardingContainerPage with stepper, transitions, and routing
  - [x] 11.1 Create OnboardingContainerPage with step orchestration and slide transitions
    - Create `src/features/onboarding/pages/onboarding-container/onboarding-container.page.ts`, `.html`, `.scss`
    - Standalone component using Ionic page structure (`ion-content`, `ion-header`, `ion-toolbar`)
    - Inject `OnboardingService`, `OnboardingStore`
    - Call `OnboardingService.initialize()` on init to restore or start fresh
    - Render `<app-stepper>` at top with `totalSteps=5` and `currentStep` bound to store signal
    - Use `@switch` on `currentStep` signal to render the appropriate step component
    - Implement slide-left transition (300ms) on advance and slide-right transition (300ms) on back
    - Display "Continue" button at bottom (except on Quiz step which auto-advances and Avatar step which has "Complete")
    - Bind "Continue" button disabled state to `!isStepValid()`
    - Display back button on steps 1–4, hide on step 0
    - On "Continue" tap: call `OnboardingService.advanceStep()`
    - On back tap: call `OnboardingService.goBack()`
    - Use deep black background (`#0A0A0A`) and app display font for titles
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 10.1, 10.3, 10.7_

  - [x] 11.2 Create onboarding routes and guards
    - Create `src/features/onboarding/onboarding.routes.ts` with route config: `path: ''` loading `OnboardingContainerPage` via `loadComponent`
    - Create `src/features/onboarding/guards/onboarding.guard.ts` — `CanActivateFn` that checks if user has `onboarding_complete` flag; if true, redirects to `/tabs/home`; if unauthenticated, redirects to `/auth/welcome`
    - Update `src/app/app.routes.ts` to add `/onboarding` route with `loadChildren` pointing to `onboarding.routes.ts`, applying auth guard and onboarding guard
    - Ensure onboarding routes do NOT display the bottom tab bar
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5_

  - [ ]* 11.3 Write unit tests for OnboardingContainerPage and guards
    - Test stepper renders with correct step/total
    - Test step component switching on currentStep changes
    - Test slide transition animations
    - Test Continue button disabled when step invalid
    - Test back button hidden on step 0
    - Test onboarding guard redirects completed users to `/tabs/home`
    - Test onboarding guard redirects unauthenticated users to `/auth/welcome`
    - Test onboarding guard allows authenticated + incomplete users
    - Test navigateRoot prevents back-navigation after completion
    - Create `src/features/onboarding/__tests__/onboarding-container.page.spec.ts`
    - Create `src/features/onboarding/guards/__tests__/onboarding.guard.spec.ts`
    - _Requirements: 1.1–1.7, 11.1–11.5_

- [x] 12. Final checkpoint - Ensure all tests pass and onboarding flow compiles
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- **RULE: Do NOT run any mvn, gradle, npm, or test commands. Only create/edit files. No build or test verification steps.**

- **IMPORTANT: Do NOT run any terminal commands (npm, ng, npx, etc.). Only create or update files directly.**
- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties from the design document (8 properties total)
- Unit tests validate specific examples and edge cases
- The project uses Jasmine + Karma for testing; fast-check should be installed for property-based tests
- All components are standalone Angular 17+ components using signals and `inject()`
- The OnboardingContainerPage manages step navigation internally (no child routes per step — URL stays at `/onboarding`)
- OnboardingService delegates backend communication to OnboardingApiService (separation of concerns)
- Use `NavController.navigateRoot()` after successful completion to prevent back-navigation to onboarding screens
- The `StorageService` uses key prefix `ascend_` for all Capacitor Preferences operations
- The GlowCardDirective is reusable across goal, difficulty, and avatar selection screens
- Quiz auto-advances on answer selection (no Continue button on quiz step)
- Arc recommendation is computed client-side via a pure deterministic function

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1"] },
    { "id": 1, "tasks": ["1.2", "1.3"] },
    { "id": 2, "tasks": ["2.1", "2.4"] },
    { "id": 3, "tasks": ["2.2", "2.3", "2.5"] },
    { "id": 4, "tasks": ["2.6", "2.7", "2.8", "2.9", "4.1", "4.2"] },
    { "id": 5, "tasks": ["4.3", "5.1", "6.1"] },
    { "id": 6, "tasks": ["5.2", "6.2", "7.1"] },
    { "id": 7, "tasks": ["7.2", "7.3", "8.1"] },
    { "id": 8, "tasks": ["8.2", "8.3", "9.1"] },
    { "id": 9, "tasks": ["9.2", "11.1"] },
    { "id": 10, "tasks": ["11.2"] },
    { "id": 11, "tasks": ["11.3"] }
  ]
}
```
