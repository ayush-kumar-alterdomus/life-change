# Requirements Document

## Introduction

This document defines the requirements for the Ascend app's UI Onboarding Flow — the multi-step experience that new users complete after authentication to personalize their journey. The flow includes goal selection (6 life-goal cards with neon glow on tap), difficulty selection (Casual/Balanced/Beast Mode), a personality assessment quiz, an Arc recommendation screen with override capability, avatar selection, and onboarding state persistence. The onboarding flow uses Angular 17+ standalone components with signals, Ionic Framework UI, and persists progress via Capacitor Preferences so users can resume mid-flow after app closure.

## Glossary

| Term | Definition |
|------|-----------|
| Onboarding_Flow | The multi-step wizard that guides new users through personalization after authentication, accessible at the `/onboarding` route |
| Goal_Selection_Screen | The screen displaying 6 life-goal cards where users select their primary improvement areas |
| Difficulty_Selection_Screen | The screen where users choose their challenge intensity tier (Casual, Balanced, or Beast Mode) |
| Quiz_Screen | The personality assessment screen presenting a series of questions to determine the user's play style and personality type |
| Arc_Recommendation_Screen | The screen that displays the system-recommended Arc based on quiz answers, with an option to override |
| Avatar_Selection_Screen | The screen where users choose a starter avatar to represent their character |
| Onboarding_Service | The Angular service that manages onboarding state, step navigation, and persistence logic |
| Onboarding_Store | The Angular signal-based store that holds the current onboarding state reactively |
| Arc | A personal growth path (30–90 days) containing quests, milestones, rewards, and bosses aligned to a life domain |
| Goal_Card | A selectable UI card representing a life-goal category (e.g., Fitness, Career, Mindfulness) |
| Difficulty_Tier | One of three challenge intensity levels: Casual (easy pace), Balanced (moderate), or Beast Mode (intense) |
| Personality_Type | The user's assessed play style derived from quiz answers, used to recommend an appropriate Arc |
| Storage_Service | The app's abstraction over Capacitor Preferences for local key-value persistence (prefixed with `ascend_`) |
| Stepper_Component | A visual progress indicator showing the user's current position within the onboarding steps |
| Neon_Glow_Effect | A CSS animation that applies a colored glow border and shadow to a card element on tap/selection |

## Requirements

### Requirement 1: Onboarding Flow Structure and Navigation

**User Story:** As a new user, I want a guided multi-step onboarding flow, so that I can personalize my experience without feeling overwhelmed by too many choices at once.

#### Acceptance Criteria

1. WHEN an authenticated user who has not completed onboarding navigates to `/onboarding`, THE Onboarding_Flow SHALL render a stepper layout with exactly 5 steps in order: Goal Selection, Difficulty Selection, Personality Quiz, Arc Recommendation, and Avatar Selection.
2. THE Onboarding_Flow SHALL display a Stepper_Component at the top of each screen showing the current step number, total steps, and visual progress (filled/unfilled dots or segments).
3. WHEN the user completes a step and taps the "Continue" button, THE Onboarding_Flow SHALL navigate forward to the next step with a slide-left page transition animation (duration 300ms).
4. WHEN the user taps the back button on any step after the first, THE Onboarding_Flow SHALL navigate backward to the previous step with a slide-right page transition animation (duration 300ms), preserving all previously entered selections.
5. THE Onboarding_Flow SHALL disable the "Continue" button until the current step's required selection is made.
6. THE Onboarding_Flow SHALL use Angular 17+ standalone components with signals for reactive state management within each step.
7. WHEN the user is on the first step (Goal Selection), THE Onboarding_Flow SHALL hide the back button to prevent navigation to the auth flow.

### Requirement 2: Goal Selection Screen

**User Story:** As a new user, I want to select my life-improvement goals from visually engaging cards, so that the app understands what areas I want to focus on.

#### Acceptance Criteria

1. THE Goal_Selection_Screen SHALL display exactly 6 Goal_Card elements in a responsive 2-column grid layout, each representing a life-goal category: Fitness, Career, Mindfulness, Relationships, Finance, and Learning.
2. WHEN the user taps a Goal_Card, THE Goal_Selection_Screen SHALL apply a Neon_Glow_Effect to the selected card using a CSS box-shadow with the app's primary accent color (`#FF9800`) and a glow radius of 12px, with a 200ms ease-in transition.
3. WHEN the user taps a Goal_Card, THE Goal_Selection_Screen SHALL trigger light haptic feedback via the Haptic Feedback Service.
4. THE Goal_Selection_Screen SHALL allow the user to select between 1 and 3 goals simultaneously (multi-select with a maximum of 3).
5. IF the user attempts to select a fourth Goal_Card while 3 are already selected, THEN THE Goal_Selection_Screen SHALL display a brief toast message: "Maximum 3 goals allowed" and prevent the fourth selection.
6. WHEN a selected Goal_Card is tapped again, THE Goal_Selection_Screen SHALL deselect the card, remove the Neon_Glow_Effect, and trigger light haptic feedback.
7. Each Goal_Card SHALL display an icon (emoji or ionicon), the goal category name, and a one-line description, rendered on a dark glass background (`#161616`) with rounded corners (12px border-radius).
8. THE "Continue" button SHALL remain disabled until at least 1 Goal_Card is selected.

### Requirement 3: Difficulty Selection Screen

**User Story:** As a new user, I want to choose my challenge intensity, so that the app matches my current capacity and motivation level.

#### Acceptance Criteria

1. THE Difficulty_Selection_Screen SHALL display exactly 3 Difficulty_Tier cards in a single-column vertical layout: Casual, Balanced, and Beast Mode.
2. Each Difficulty_Tier card SHALL display the tier name, a descriptive subtitle (Casual: "Easy pace, gentle reminders"; Balanced: "Moderate challenge, steady growth"; Beast Mode: "Intense discipline, maximum results"), and a visual intensity indicator (e.g., 1/2/3 flame icons).
3. THE Balanced tier card SHALL display a "Recommended" badge using the app's secondary accent color (`#A855F7`).
4. WHEN the user taps a Difficulty_Tier card, THE Difficulty_Selection_Screen SHALL apply a Neon_Glow_Effect to the selected card and deselect any previously selected card (single-select behavior).
5. WHEN the user taps a Difficulty_Tier card, THE Difficulty_Selection_Screen SHALL trigger light haptic feedback via the Haptic Feedback Service.
6. THE Beast Mode card SHALL use a darker dramatic background gradient and a red-tinted glow effect (`#F44336`) to visually communicate intensity.
7. THE "Continue" button SHALL remain disabled until exactly 1 Difficulty_Tier is selected.

### Requirement 4: Personality Assessment Quiz

**User Story:** As a new user, I want to answer a short personality quiz, so that the app can recommend the best growth path for my personality type.

#### Acceptance Criteria

1. THE Quiz_Screen SHALL present between 5 and 8 multiple-choice questions sequentially, one question at a time, with a progress indicator showing the current question number out of total questions.
2. Each quiz question SHALL display the question text and between 3 and 4 answer options as tappable cards.
3. WHEN the user taps an answer option, THE Quiz_Screen SHALL highlight the selected option with the app's primary accent color, trigger light haptic feedback, and automatically advance to the next question after a 400ms delay.
4. WHEN the user is on a question after the first, THE Quiz_Screen SHALL allow navigating back to the previous question with the previous answer pre-selected.
5. THE quiz questions SHALL assess the user's personality along dimensions relevant to Arc recommendation: discipline style, motivation triggers, preferred challenge type, and time availability.
6. WHEN the user answers the final question, THE Quiz_Screen SHALL compute a Personality_Type result based on the weighted combination of answers and store the result in the Onboarding_Store.
7. THE personality computation logic SHALL be deterministic: the same set of answers SHALL always produce the same Personality_Type result.
8. THE Quiz_Screen SHALL not display a separate "Continue" button; advancing is handled by answer selection on the final question, which triggers navigation to the Arc_Recommendation_Screen.

### Requirement 5: Arc Recommendation Screen

**User Story:** As a new user, I want to see a personalized Arc recommendation based on my quiz results, so that I can start a growth path tailored to my personality, with the freedom to choose differently if I prefer.

#### Acceptance Criteria

1. WHEN the Arc_Recommendation_Screen loads, THE screen SHALL display a hero card showing the recommended Arc name, Arc description, a visual icon/illustration, and a preview of 3–4 sample quests from that Arc.
2. THE Arc recommendation SHALL be computed from the combination of the user's selected goals, chosen difficulty tier, and Personality_Type result using a deterministic mapping algorithm.
3. THE Arc_Recommendation_Screen SHALL display a primary action button labeled "Begin This Arc" that accepts the recommendation.
4. THE Arc_Recommendation_Screen SHALL display a secondary text link labeled "Choose a different Arc" below the primary button.
5. WHEN the user taps "Choose a different Arc", THE Arc_Recommendation_Screen SHALL expand or navigate to a list of all available Arcs (Monk, Warrior, Scholar, Creator, Beast Mode) displayed as selectable cards, with the recommended Arc visually marked.
6. WHEN the user selects an alternative Arc from the override list, THE Onboarding_Store SHALL update the selected Arc to the user's override choice.
7. WHEN the user taps "Begin This Arc" (either the recommendation or an override selection), THE Onboarding_Flow SHALL navigate to the Avatar_Selection_Screen.
8. THE recommended Arc hero card SHALL use a cinematic presentation with the Arc's theme color as a subtle background gradient and the app's dark aesthetic.

### Requirement 6: Avatar Selection Screen

**User Story:** As a new user, I want to choose a starter avatar, so that I have a visual identity within the app from the beginning.

#### Acceptance Criteria

1. THE Avatar_Selection_Screen SHALL display a grid of at least 8 starter avatar options in a scrollable layout (2 or 3 columns depending on screen width).
2. Each avatar option SHALL be displayed as a circular image (64px minimum diameter) with a label or name below the image.
3. WHEN the user taps an avatar, THE Avatar_Selection_Screen SHALL apply a Neon_Glow_Effect ring around the selected avatar and deselect any previously selected avatar (single-select behavior).
4. WHEN the user taps an avatar, THE Avatar_Selection_Screen SHALL trigger light haptic feedback via the Haptic Feedback Service.
5. THE Avatar_Selection_Screen SHALL display a larger preview (128px diameter) of the currently selected avatar above the grid.
6. THE "Complete" button SHALL remain disabled until exactly 1 avatar is selected.
7. WHEN the user taps the "Complete" button, THE Onboarding_Service SHALL submit the complete onboarding payload to the backend.

### Requirement 7: Onboarding Completion and Profile Update

**User Story:** As a new user who has finished onboarding, I want my selections saved to my profile, so that the app is personalized when I enter the main experience.

#### Acceptance Criteria

1. WHEN the user taps "Complete" on the Avatar_Selection_Screen, THE Onboarding_Service SHALL send a PUT request to the backend endpoint `PUT /api/v1/users/onboarding` with the complete onboarding payload containing: selected goals, difficulty tier, personality type, selected Arc identifier, and selected avatar identifier.
2. WHEN the backend returns a successful response (HTTP 200), THE Onboarding_Service SHALL set the `onboarding_complete` flag to `true` in Capacitor Preferences via the Storage_Service.
3. WHEN the backend returns a successful response, THE Onboarding_Flow SHALL navigate to `/tabs/home` using `navigateRoot` (replacing the navigation stack) so the user cannot navigate back to onboarding via the back button.
4. WHEN the backend returns a successful response, THE Onboarding_Service SHALL trigger medium haptic feedback and display a brief success toast: "Welcome to Ascend! Your Arc begins now."
5. IF the backend request fails due to a network error or server error, THEN THE Onboarding_Flow SHALL display an inline error message: "Could not save your profile. Please try again." with a "Retry" button that re-submits the payload.
6. WHILE the completion request is in progress, THE "Complete" button SHALL display a loading spinner and be disabled to prevent duplicate submissions.
7. WHEN onboarding completes successfully, THE Onboarding_Store SHALL be reset to its initial state to free memory.

### Requirement 8: Onboarding State Persistence

**User Story:** As a user who closes the app mid-onboarding, I want to resume where I left off when I reopen the app, so that I don't have to repeat steps I've already completed.

#### Acceptance Criteria

1. WHEN the user completes any onboarding step (makes a selection and advances), THE Onboarding_Service SHALL persist the current onboarding state to Capacitor Preferences via the Storage_Service using the key `onboarding_state`.
2. THE persisted onboarding state SHALL include: current step index, selected goals array, selected difficulty tier, quiz answers array, computed personality type, selected Arc identifier, and selected avatar identifier.
3. WHEN the Onboarding_Flow initializes and a persisted `onboarding_state` exists in storage, THE Onboarding_Service SHALL restore the state into the Onboarding_Store and navigate directly to the step indicated by the persisted step index.
4. WHEN the Onboarding_Flow initializes and no persisted `onboarding_state` exists, THE Onboarding_Flow SHALL start from step 1 (Goal Selection) with an empty state.
5. WHEN onboarding completes successfully (backend confirmation received), THE Onboarding_Service SHALL remove the `onboarding_state` key from Capacitor Preferences via the Storage_Service.
6. THE persisted state SHALL be serialized as JSON and deserialized on restore, with the Onboarding_Service validating the structure before applying it to the store.
7. IF the persisted state is corrupted or fails validation (missing required fields, invalid step index), THEN THE Onboarding_Service SHALL discard the corrupted state, remove the key from storage, and start onboarding from step 1.

### Requirement 9: Onboarding Service and Store Architecture

**User Story:** As a developer, I want a centralized onboarding service and reactive store, so that onboarding state is managed consistently across all steps and persistence is handled transparently.

#### Acceptance Criteria

1. THE Onboarding_Store SHALL be injectable as a singleton (`providedIn: 'root'`) and expose the following signals: `currentStep: Signal<number>`, `selectedGoals: Signal<string[]>`, `selectedDifficulty: Signal<string | null>`, `quizAnswers: Signal<QuizAnswer[]>`, `personalityType: Signal<string | null>`, `selectedArc: Signal<string | null>`, and `selectedAvatar: Signal<string | null>`.
2. THE Onboarding_Store SHALL expose a computed signal `isStepValid: Signal<boolean>` that returns true when the current step's required selection criteria are met.
3. THE Onboarding_Service SHALL be injectable as a singleton (`providedIn: 'root'`) and provide methods: `initialize(): Promise<void>`, `advanceStep(): void`, `goBack(): void`, `setGoals(goals: string[]): void`, `setDifficulty(tier: string): void`, `addQuizAnswer(answer: QuizAnswer): void`, `setArc(arcId: string): void`, `setAvatar(avatarId: string): void`, `completeOnboarding(): Observable<void>`, and `reset(): void`.
4. WHEN any setter method on the Onboarding_Service is called, THE Onboarding_Service SHALL update the corresponding signal in the Onboarding_Store and persist the updated state to storage.
5. THE Onboarding_Service SHALL not call HTTP endpoints directly for data retrieval; it SHALL delegate backend communication to a dedicated OnboardingApiService for the completion payload submission.
6. THE Onboarding_Service SHALL use the Storage_Service (with key prefix `ascend_`) for all persistence operations, consistent with the app's storage architecture.

### Requirement 10: Onboarding Visual Design and Theming

**User Story:** As a user, I want the onboarding flow to feel premium and game-like with the app's dark/neon aesthetic, so that I'm excited to start my journey.

#### Acceptance Criteria

1. THE Onboarding_Flow SHALL use the app's dark theme with deep black background (`#0A0A0A`) and dark glass card backgrounds (`#161616`) on all screens.
2. THE Neon_Glow_Effect on selected cards SHALL use CSS `box-shadow` with the primary accent color (`#FF9800`) at 60% opacity for goal and difficulty cards, and the secondary accent color (`#A855F7`) for the recommended Arc badge.
3. THE Onboarding_Flow SHALL use the app's display font (Orbitron or Rajdhani) for screen titles and the body font (Inter or SF Pro) for descriptions and option text.
4. ALL interactive elements (cards, buttons) SHALL have a minimum touch target of 44px height, consistent with the app's mobile UX rules.
5. THE Stepper_Component SHALL use the primary accent color for completed steps, a muted gray for upcoming steps, and a pulsing glow animation on the current step indicator.
6. WHEN a card is tapped, THE card SHALL scale to 0.97 for 100ms (press feedback) before applying the selection state, providing immediate tactile visual feedback.
7. THE Onboarding_Flow screens SHALL use Ionic page components (`ion-content`, `ion-header`, `ion-toolbar`) for consistent scroll behavior and safe-area handling across devices.

### Requirement 11: Onboarding Routing and Guards

**User Story:** As a developer, I want the onboarding routes properly guarded, so that only authenticated users who haven't completed onboarding can access the flow, and completed users are redirected away.

#### Acceptance Criteria

1. THE Onboarding_Flow SHALL define routes under the `/onboarding` path with child routes for each step, using `loadChildren` with a dynamic `import()` expression for lazy loading.
2. WHEN an unauthenticated user attempts to navigate to `/onboarding`, THE auth guard SHALL redirect to `/auth/welcome`.
3. WHEN an authenticated user who has already completed onboarding (the `onboarding_complete` flag is `true` in Capacitor Preferences or the user profile) attempts to navigate to `/onboarding`, THE onboarding route guard SHALL redirect to `/tabs/home`.
4. THE onboarding child routes SHALL not display the bottom tab bar (the tab bar is only rendered within the `/tabs` layout).
5. WHEN the Onboarding_Flow completes, THE navigation SHALL use `navigateRoot('/tabs/home')` to replace the entire navigation stack, preventing back-navigation to onboarding screens.

### Requirement 12: Onboarding State Serialization

**User Story:** As a developer, I want the onboarding state to be reliably serialized and deserialized, so that persistence works correctly across app restarts.

#### Acceptance Criteria

1. THE Onboarding_Service SHALL serialize the onboarding state to JSON using `JSON.stringify` before persisting to the Storage_Service.
2. THE Onboarding_Service SHALL deserialize the persisted state using `JSON.parse` when restoring from the Storage_Service.
3. FOR ALL valid onboarding state objects, serializing then deserializing SHALL produce an equivalent object (round-trip property).
4. THE serialized state SHALL conform to a defined TypeScript interface (`OnboardingState`) with required fields: `currentStep` (number, 0–4), `selectedGoals` (string array, 0–3 items), `selectedDifficulty` (string or null), `quizAnswers` (QuizAnswer array), `personalityType` (string or null), `selectedArc` (string or null), and `selectedAvatar` (string or null).
5. WHEN deserializing, THE Onboarding_Service SHALL validate that `currentStep` is a number between 0 and 4 inclusive, `selectedGoals` is an array with 0–3 string elements, and all other fields are either strings or null.
6. IF any validation check fails during deserialization, THEN THE Onboarding_Service SHALL treat the state as corrupted and discard it.
