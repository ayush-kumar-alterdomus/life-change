# Requirements Document

## Introduction

This document defines the requirements for the Ascend app's main Dashboard screen — the primary home view that aggregates user progress, daily quests, active arc status, motivation content, and leaderboard data into a single scrollable interface. The dashboard replaces the existing placeholder and serves as the central hub users interact with after authentication and onboarding.

## Glossary

| Term | Definition |
|------|-----------|
| Dashboard_Page | The smart (page-level) Angular standalone component that orchestrates data loading, state management, and child component composition for the home tab |
| Header_Section | The top area of the dashboard displaying a time-based greeting, the user's level badge, and streak flame indicator |
| XP_Progress_Card | A prominent card displaying the user's current level, XP progress bar with animation, and numeric XP values |
| Daily_Summary_Section | A horizontal row of mini-cards showing quests completed, current streak, focus score, and life score |
| Active_Arc_Card | A card displaying the user's currently active arc name, completion percentage, and navigation action |
| Quest_List | A vertical list of today's quests rendered using Ionic ion-item-sliding for swipe actions |
| Motivation_Widget | A card displaying a motivational message selected from a local static content pool |
| Leaderboard_Preview | A compact snapshot showing the user's current rank and a call-to-action to view full rankings |
| Pull_To_Refresh | An Ionic ion-refresher component enabling the user to refresh all dashboard data by pulling down |
| Skeleton_State | Placeholder UI rendered while dashboard data is loading, matching the layout dimensions of actual content |
| Dashboard_Store | An Angular signal-based store that holds aggregated dashboard state and exposes computed signals for each section |
| UserStore | The existing global user state store providing user profile, level, and XP data |

## Requirements

### Requirement 1: Dashboard Page Structure and Layout

**User Story:** As a user, I want a single scrollable dashboard that shows all my key information at a glance, so that I can quickly assess my progress and take action.

#### Acceptance Criteria

1. THE Dashboard_Page SHALL render as a standalone Angular component with OnPush change detection strategy, registered as the default route for the home tab.
2. THE Dashboard_Page SHALL render sections in top-to-bottom order: Header_Section, XP_Progress_Card, Daily_Summary_Section, Active_Arc_Card, Quest_List, Motivation_Widget, Leaderboard_Preview.
3. THE Dashboard_Page SHALL use Ionic's `ion-content` with `fullscreen` attribute as the scrollable container for all dashboard sections.
4. THE Dashboard_Page SHALL apply the app's dark theme background color (#0A0A0A) to the content area.
5. WHEN the Dashboard_Page initializes, THE Dashboard_Page SHALL call multiple backend service endpoints in parallel using `forkJoin` or equivalent parallel execution to load all section data concurrently.
6. THE Dashboard_Page SHALL use Angular Signals from the Dashboard_Store to reactively render each section based on the current state.

### Requirement 2: Header Section

**User Story:** As a user, I want to see a personalized greeting with my level and streak when I open the dashboard, so that I feel recognized and motivated.

#### Acceptance Criteria

1. THE Header_Section SHALL display a time-based greeting message: "Good Morning" (5:00–11:59), "Good Afternoon" (12:00–16:59), or "Good Evening" (17:00–4:59), followed by the user's display name.
2. THE Header_Section SHALL render the LevelBadgeComponent (shared) displaying the user's current level in the small size variant.
3. THE Header_Section SHALL render the StreakFlameComponent (shared) displaying the user's current streak day count.
4. THE Header_Section SHALL position the greeting text on the left and the level badge and streak flame on the right in a single horizontal row.
5. IF the user's display name is not yet loaded, THEN THE Header_Section SHALL display the greeting without a name (e.g., "Good Evening") until the name becomes available.

### Requirement 3: XP Progress Card

**User Story:** As a user, I want to see my XP progress toward the next level in a visually prominent card, so that I understand how close I am to leveling up.

#### Acceptance Criteria

1. THE XP_Progress_Card SHALL display the user's current level number using the Orbitron display font.
2. THE XP_Progress_Card SHALL render the XpProgressBarComponent (shared) with the user's currentXp and requiredXp values.
3. THE XP_Progress_Card SHALL display the current XP and required XP as formatted text below the progress bar (e.g., "2,400 / 3,000 XP").
4. WHEN the currentXp value changes (e.g., after quest completion), THE XP_Progress_Card SHALL animate the progress bar fill width smoothly over 600ms using CSS transitions.
5. THE XP_Progress_Card SHALL render inside a glass-morphism styled card container using the design system card styling (#161616 background).
6. THE XP_Progress_Card SHALL use the accent-to-secondary gradient (#FF9800 to #A855F7) for the progress bar fill.

### Requirement 4: Daily Summary Mini-Cards

**User Story:** As a user, I want to see a quick summary of my daily stats (quests, streak, focus, life score), so that I can gauge my overall performance at a glance.

#### Acceptance Criteria

1. THE Daily_Summary_Section SHALL display exactly four mini-cards in a 2x2 grid layout: Quests Completed, Current Streak, Focus Score, and Life Score.
2. EACH mini-card SHALL display an icon, a numeric value, and a descriptive label.
3. THE Quests Completed mini-card SHALL display the count of quests completed today out of total daily quests (e.g., "3/5").
4. THE Current Streak mini-card SHALL display the streak day count with a flame icon.
5. THE Focus Score mini-card SHALL display a numeric score (0–100) representing the user's daily focus metric.
6. THE Life Score mini-card SHALL display a numeric score (0–100) representing the user's overall life balance metric.
7. EACH mini-card SHALL render with the design system card styling (#161616 background) and rounded corners (12px border-radius).

### Requirement 5: Active Arc Card

**User Story:** As a user, I want to see my currently active arc with its progress, so that I stay aware of my long-term journey.

#### Acceptance Criteria

1. THE Active_Arc_Card SHALL render the ArcCardComponent (shared) with the user's active arc name, progress percentage, current phase, and arc type.
2. WHEN the user taps the Active_Arc_Card, THE Dashboard_Page SHALL navigate to the arc-mode detail route.
3. IF the user has no active arc, THEN THE Active_Arc_Card SHALL display a prompt card with the message "Start your first Arc" and a call-to-action button that navigates to arc selection.
4. THE Active_Arc_Card SHALL display the arc completion percentage as both a progress bar and a numeric label (e.g., "43% Complete").

### Requirement 6: Quest List with Swipe Actions

**User Story:** As a user, I want to see today's quests with swipe actions, so that I can quickly complete, skip, or edit quests without extra navigation.

#### Acceptance Criteria

1. THE Quest_List SHALL display today's active quests using `ion-list` with `ion-item-sliding` for each quest item.
2. EACH quest item SHALL render the QuestCardComponent (shared) displaying the quest title, XP reward, difficulty badge, and stat type icon.
3. WHEN the user swipes a quest item to the right, THE Quest_List SHALL reveal a "Complete" action button with a success color (#4CAF50).
4. WHEN the user swipes a quest item to the left, THE Quest_List SHALL reveal "Skip" and "Edit" action buttons.
5. WHEN the user taps the "Complete" swipe action, THE Dashboard_Page SHALL call the quest completion service, trigger haptic feedback (success notification), and remove the quest from the active list with an exit animation.
6. WHEN the user taps the "Skip" swipe action, THE Dashboard_Page SHALL call the quest skip service and move the quest to a skipped state with reduced opacity.
7. WHEN the user taps the "Edit" swipe action, THE Dashboard_Page SHALL navigate to the quest edit route with the quest ID as a parameter.
8. THE Quest_List SHALL display a maximum of 8 quests. IF more than 8 quests exist, THEN THE Quest_List SHALL display a "View All Quests" link that navigates to the full quest board.
9. IF no quests are available for today, THEN THE Quest_List SHALL display an empty state message: "No quests for today. Enjoy your rest!" with a relevant icon.

### Requirement 7: AI Motivation Widget

**User Story:** As a user, I want to see a motivational message on my dashboard, so that I feel encouraged and inspired to continue my journey.

#### Acceptance Criteria

1. THE Motivation_Widget SHALL display a motivational message selected from a local static content pool of at least 20 messages.
2. THE Motivation_Widget SHALL select a different message each time the dashboard loads, using a rotation strategy that avoids repeating the same message on consecutive loads.
3. THE Motivation_Widget SHALL render the message inside a styled card with an icon (lightbulb or brain icon) and a subtle accent-colored left border.
4. THE Motivation_Widget SHALL display the message text using the body font (Inter) at the base size (16px) with text-secondary color (#B0B0B0).
5. THE Motivation_Widget SHALL store the index of the last displayed message in local storage to maintain rotation state across sessions.

### Requirement 8: Leaderboard Preview Snapshot

**User Story:** As a user, I want to see my current leaderboard rank on the dashboard, so that I stay competitive without navigating to the full leaderboard.

#### Acceptance Criteria

1. THE Leaderboard_Preview SHALL display the user's current rank position (e.g., "#12"), the user's XP total, and the league name.
2. THE Leaderboard_Preview SHALL display the top 3 users as compact entries using the LeaderboardCardComponent (shared) with special gold/silver/bronze styling.
3. THE Leaderboard_Preview SHALL include a "View Full Rankings" button that navigates to the social/leaderboard route.
4. IF leaderboard data fails to load, THEN THE Leaderboard_Preview SHALL display a compact error state with a retry option without affecting other dashboard sections.
5. THE Leaderboard_Preview SHALL render inside a card container with the design system card styling.

### Requirement 9: Pull-to-Refresh

**User Story:** As a user, I want to pull down on the dashboard to refresh all data, so that I can see the latest information without restarting the app.

#### Acceptance Criteria

1. WHEN the user pulls down on the dashboard content, THE Dashboard_Page SHALL display Ionic's `ion-refresher` component with a pulling indicator.
2. WHEN the pull-to-refresh is triggered, THE Dashboard_Page SHALL re-fetch all dashboard data from backend services in parallel.
3. WHEN all data requests complete (success or failure), THE Dashboard_Page SHALL call `event.target.complete()` to dismiss the refresher indicator.
4. WHILE a pull-to-refresh is in progress, THE Dashboard_Page SHALL NOT display skeleton loaders over existing content — the current data SHALL remain visible until new data arrives.
5. WHEN new data arrives after a pull-to-refresh, THE Dashboard_Page SHALL update the Dashboard_Store signals, causing reactive UI updates without full page re-render.
6. THE pull-to-refresh indicator SHALL use the accent color (#FF9800) for the spinner.

### Requirement 10: Skeleton Loading States

**User Story:** As a user, I want to see placeholder shapes while the dashboard loads, so that the app feels responsive and I know where content will appear.

#### Acceptance Criteria

1. WHEN the Dashboard_Page is loading data for the first time (no cached data available), THE Dashboard_Page SHALL display skeleton placeholders for each section matching the dimensions of the actual content.
2. THE Header_Section skeleton SHALL display a text-line placeholder for the greeting and circle placeholders for the badge and flame.
3. THE XP_Progress_Card skeleton SHALL display a rectangle placeholder matching the card dimensions with a shimmer animation.
4. THE Daily_Summary_Section skeleton SHALL display four rectangle placeholders in a 2x2 grid matching the mini-card dimensions.
5. THE Quest_List skeleton SHALL display three rectangle placeholders matching quest card dimensions with staggered shimmer animations.
6. WHEN data arrives for a section, THAT section's skeleton SHALL transition to actual content with a fade transition of 200ms duration.
7. IF cached data is available from a previous session, THEN THE Dashboard_Page SHALL display the cached data immediately and update silently when fresh data arrives from the backend.

### Requirement 11: Error Handling and Resilience

**User Story:** As a user, I want the dashboard to remain usable even when some data fails to load, so that partial failures don't block my entire experience.

#### Acceptance Criteria

1. WHEN an individual section's data request fails, THEN THE Dashboard_Page SHALL render that section's error state independently without affecting other sections that loaded successfully.
2. WHEN a section is in an error state, THE section SHALL display a compact error message with a "Retry" button that re-fetches only that section's data.
3. IF all data requests fail simultaneously, THEN THE Dashboard_Page SHALL display the full-page error state component with a "Try Again" button that re-fetches all data.
4. WHEN the app transitions from offline to online, THE Dashboard_Page SHALL automatically attempt to refresh any sections that are in an error state.
5. THE Dashboard_Page SHALL use the skip-loading HttpContext token for all dashboard API requests to prevent the global loading overlay from appearing (dashboard uses its own skeleton states).

### Requirement 12: Dashboard Data Store

**User Story:** As a developer, I want a dedicated signal-based store for dashboard state, so that data flows reactively and components update efficiently.

#### Acceptance Criteria

1. THE Dashboard_Store SHALL expose individual signals for each section's data: userSummary, xpProgress, dailyStats, activeArc, todayQuests, leaderboardPreview.
2. THE Dashboard_Store SHALL expose a computed signal `isLoading` that is true when any section's data has not yet been fetched.
3. THE Dashboard_Store SHALL expose individual loading signals for each section (e.g., `questsLoading`, `arcLoading`) to enable per-section skeleton rendering.
4. THE Dashboard_Store SHALL expose individual error signals for each section to enable per-section error states.
5. WHEN a quest is completed via the dashboard, THE Dashboard_Store SHALL update both the todayQuests signal (remove quest) and the dailyStats signal (increment completed count) and the xpProgress signal (add earned XP) within a single synchronous update.
6. THE Dashboard_Store SHALL be injectable as a singleton (`providedIn: 'root'`) and use Angular Signals for all reactive state.

### Requirement 13: Performance and Accessibility

**User Story:** As a user, I want the dashboard to load quickly and be accessible, so that I have a smooth experience regardless of device capability or accessibility needs.

#### Acceptance Criteria

1. THE Dashboard_Page SHALL use `trackBy` functions on all `@for` loops to minimize DOM re-creation during list updates.
2. THE Dashboard_Page SHALL lazy-load the Leaderboard_Preview section data only after the above-the-fold content (Header, XP Card, Daily Summary) has rendered.
3. THE Dashboard_Page SHALL ensure all interactive elements (buttons, cards, swipe actions) have a minimum touch target of 44x44 CSS pixels.
4. THE Dashboard_Page SHALL include appropriate ARIA labels on all interactive elements and section headings for screen reader compatibility.
5. THE Dashboard_Page SHALL use semantic HTML structure with heading hierarchy (h1 for greeting, h2 for section titles) for assistive technology navigation.
6. WHEN the dashboard content exceeds the viewport height, THE Dashboard_Page SHALL use Ionic's virtual scroll or standard overflow scrolling without jank (maintaining 60fps scroll performance).
