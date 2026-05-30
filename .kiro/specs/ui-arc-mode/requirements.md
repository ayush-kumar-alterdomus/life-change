# Requirements Document

## Introduction

This document defines the requirements for the Arc Mode UI feature in Ascend — a comprehensive set of screens enabling users to browse, explore, manage, and create Arcs (long-term growth journeys). The feature includes an Arc list page with tabbed segments (Explore, My Arcs, Completed), an Arc detail page with cinematic banner, phase-grouped milestone timeline accordion, phase progress visualization (Beginner → Intermediate → Elite → Master), boss section with progress bar, rewards section, skill tree preview, a custom Arc creation form, and Arc identity titles that evolve as the user progresses (e.g., "Distracted Mind" → "Zen Master").

## Glossary

- **Arc_List_Page**: The smart page component that displays all arcs organized into tabbed segments (Explore, My Arcs, Completed) using ion-segment navigation
- **Arc_Detail_Page**: The smart page component that displays full arc information including cinematic banner, milestone timeline, boss section, rewards, skill tree preview, and identity title
- **Cinematic_Banner**: A full-width hero image section at the top of the Arc_Detail_Page displaying the arc name, arc type gradient, current phase, and overall progress percentage
- **Milestone_Timeline**: A phase-grouped accordion component displaying milestones organized by phase (Beginner, Intermediate, Elite, Master) with completion indicators (✓ completed, ○ upcoming)
- **Phase_Progress**: A visual stepper component showing the user's progression through the four arc phases: Beginner → Intermediate → Elite → Master
- **Boss_Section**: A section within the Arc_Detail_Page displaying the arc's boss encounter with a health progress bar indicating damage dealt
- **Rewards_Section**: A section within the Arc_Detail_Page listing rewards the user earns upon completing milestones and phases within the arc
- **Skill_Tree_Preview**: A compact visual preview of the arc's associated skill tree path showing locked and unlocked nodes
- **Arc_Creation_Form**: A single-page form allowing users to create a custom arc with title, goal, duration, milestones list, and quest frequency
- **Identity_Title**: A dynamic title assigned to the user based on their current phase within an arc, evolving from a starting title (e.g., "Distracted Mind") to a mastery title (e.g., "Zen Master") as the user progresses
- **Arc_Store**: An Angular injectable service using signals to manage arc-related reactive state including available arcs, active arc progress, and arc detail data
- **Arc_Service**: An Angular injectable service responsible for HTTP communication with the Arc API endpoints
- **Explore_Tab**: The ion-segment tab displaying the catalog of prebuilt arcs available for the user to start
- **My_Arcs_Tab**: The ion-segment tab displaying the user's active arcs (both prebuilt and custom)
- **Completed_Tab**: The ion-segment tab displaying arcs the user has finished

## Requirements

### Requirement 1: Arc List Page with Tabbed Segments

**User Story:** As a user, I want to browse arcs organized by category (explore, my active arcs, completed), so that I can quickly find what I need without scrolling through irrelevant content.

#### Acceptance Criteria

1. THE Arc_List_Page SHALL display an ion-segment control with three tabs: "Explore", "My Arcs", and "Completed"
2. WHEN the user selects the Explore_Tab, THE Arc_List_Page SHALL display a scrollable list of prebuilt arc cards fetched from the GET /api/v1/arcs endpoint filtered to prebuilt arcs only
3. WHEN the user selects the My_Arcs_Tab, THE Arc_List_Page SHALL display the user's active arcs (both prebuilt and custom) fetched from the GET /api/v1/arcs/active endpoint
4. WHEN the user selects the Completed_Tab, THE Arc_List_Page SHALL display arcs the user has finished with their final completion date and total duration
5. THE Arc_List_Page SHALL default to the My_Arcs_Tab when the user has at least one active arc, and to the Explore_Tab when the user has no active arcs
6. WHILE arc data is loading, THE Arc_List_Page SHALL display skeleton-loader placeholders matching the arc card layout
7. IF the selected tab contains no arcs, THEN THE Arc_List_Page SHALL display an empty state message with a contextual call-to-action (e.g., "Start your first Arc" for Explore_Tab, "No completed arcs yet" for Completed_Tab)
8. THE Arc_List_Page SHALL include a floating action button labeled "Create Arc" that navigates to the Arc_Creation_Form

### Requirement 2: Arc Card Display in List

**User Story:** As a user, I want each arc in the list to show key information at a glance, so that I can compare arcs and decide which to explore further.

#### Acceptance Criteria

1. THE Arc_List_Page SHALL render each arc using the shared app-arc-card component
2. THE app-arc-card component SHALL display the arc name, current phase, progress percentage, and arc type gradient styling
3. WHEN the user taps an arc card, THE Arc_List_Page SHALL navigate to the Arc_Detail_Page for the selected arc
4. THE app-arc-card component SHALL apply the correct themed gradient class based on the arc type (Monk, Warrior, Scholar, Creator, Beast Mode)
5. THE app-arc-card component SHALL include an accessible aria-label containing the arc name, progress percentage, and current phase

### Requirement 3: Arc Detail Page with Cinematic Banner

**User Story:** As a user, I want the arc detail page to feel cinematic and immersive, so that I feel motivated and engaged with my growth journey.

#### Acceptance Criteria

1. WHEN the user navigates to the Arc_Detail_Page, THE Arc_Detail_Page SHALL fetch arc detail data from the GET /api/v1/arcs/{id} endpoint
2. THE Cinematic_Banner SHALL occupy the full viewport width and a minimum height of 240 CSS pixels at the top of the Arc_Detail_Page
3. THE Cinematic_Banner SHALL display the arc name in the display font (Orbitron) with the arc type themed gradient as the background
4. THE Cinematic_Banner SHALL display the current phase name and overall progress percentage overlaid on the gradient background
5. THE Cinematic_Banner SHALL apply a parallax scroll effect where the banner scrolls at 50% of the content scroll speed
6. WHILE arc detail data is loading, THE Arc_Detail_Page SHALL display a skeleton-loader placeholder matching the banner and content layout
7. IF the arc detail request fails, THEN THE Arc_Detail_Page SHALL display the shared error-state component with a retry action

### Requirement 4: Phase Progress Visualization

**User Story:** As a user, I want to see my progression through the four arc phases visually, so that I understand where I am in my journey and what lies ahead.

#### Acceptance Criteria

1. THE Arc_Detail_Page SHALL display the Phase_Progress component below the Cinematic_Banner
2. THE Phase_Progress component SHALL render four sequential phase indicators labeled "Beginner", "Intermediate", "Elite", and "Master"
3. THE Phase_Progress component SHALL visually distinguish completed phases (filled with progression orange #FF9800), the current active phase (pulsing glow animation), and upcoming phases (grayed out with #B0B0B0)
4. THE Phase_Progress component SHALL use the shared stepper component to render the phase progression
5. THE Phase_Progress component SHALL include an aria-label describing the current phase (e.g., "Phase progress: currently in Intermediate, 2 of 4 phases completed")

### Requirement 5: Milestone Timeline Accordion

**User Story:** As a user, I want to see my milestones organized by phase in collapsible sections, so that I can focus on my current phase without being overwhelmed by future milestones.

#### Acceptance Criteria

1. THE Milestone_Timeline SHALL group milestones into collapsible accordion sections, one section per phase (Beginner, Intermediate, Elite, Master)
2. THE Milestone_Timeline SHALL display the current active phase section expanded by default and all other phase sections collapsed
3. WHEN the user taps a phase section header, THE Milestone_Timeline SHALL toggle that section between expanded and collapsed states with a slide animation over 200ms
4. THE Milestone_Timeline SHALL display each milestone with a completion indicator: ✓ (checkmark icon with success green #4CAF50) for completed milestones and ○ (circle outline with #B0B0B0) for upcoming milestones
5. THE Milestone_Timeline SHALL display the milestone title and XP reward value for each milestone item
6. THE Milestone_Timeline SHALL display a phase completion summary in each section header showing completed count versus total (e.g., "3/5 milestones")
7. THE Milestone_Timeline SHALL include aria-expanded attributes on accordion headers and aria-controls linking headers to their content panels

### Requirement 6: Boss Section with Progress Bar

**User Story:** As a user, I want to see my progress against the arc's boss encounter, so that I feel the challenge and excitement of working toward defeating the boss.

#### Acceptance Criteria

1. THE Boss_Section SHALL display the boss name, boss level, and a health progress bar showing remaining health percentage
2. THE Boss_Section SHALL render the boss using the shared boss-card component
3. THE Boss_Section SHALL display the health progress bar using the shared xp-progress-bar component with a red-to-green gradient indicating damage dealt
4. WHEN the boss is defeated (healthPercentage equals 0), THE Boss_Section SHALL display a "Defeated" badge with a strike-through effect on the boss name
5. IF the arc has no associated boss, THEN THE Arc_Detail_Page SHALL hide the Boss_Section entirely
6. THE Boss_Section SHALL include an aria-label describing the boss status (e.g., "Boss: Shadow of Doubt, Level 5, 60% health remaining")

### Requirement 7: Rewards Section

**User Story:** As a user, I want to see what rewards I can earn from this arc, so that I stay motivated to complete milestones and phases.

#### Acceptance Criteria

1. THE Rewards_Section SHALL display a list of rewards associated with the arc, including reward type icon, reward name, and earned status
2. THE Rewards_Section SHALL visually distinguish earned rewards (full opacity with success green checkmark) from locked rewards (50% opacity with a lock icon)
3. THE Rewards_Section SHALL group rewards by phase, showing which phase completion unlocks each reward
4. THE Rewards_Section SHALL support reward types including XP bonuses, identity titles, cosmetic items, and coins
5. THE Rewards_Section SHALL include an aria-label on each reward item indicating its name and earned status (e.g., "Reward: Zen Master title, locked, unlocks at Master phase")

### Requirement 8: Skill Tree Preview

**User Story:** As a user, I want a compact preview of the arc's skill tree, so that I can see what skills I will develop without navigating away from the arc detail.

#### Acceptance Criteria

1. THE Skill_Tree_Preview SHALL display a horizontal scrollable preview of skill tree nodes associated with the arc
2. THE Skill_Tree_Preview SHALL visually distinguish unlocked nodes (glowing with progression orange #FF9800) from locked nodes (grayed out with #B0B0B0)
3. THE Skill_Tree_Preview SHALL display connecting lines between sequential nodes to indicate the progression path
4. WHEN the user taps the Skill_Tree_Preview, THE Arc_Detail_Page SHALL navigate to the full Skill Tree screen filtered to the current arc's path
5. THE Skill_Tree_Preview SHALL display a maximum of 6 nodes in the preview, with a "View Full Tree" indicator if more nodes exist
6. THE Skill_Tree_Preview SHALL include an aria-label of "Skill tree preview, {unlocked} of {total} skills unlocked, tap to view full tree"

### Requirement 9: Custom Arc Creation Form

**User Story:** As a user, I want to create my own custom arc with personalized goals and milestones, so that I can tailor my growth journey to my specific needs.

#### Acceptance Criteria

1. THE Arc_Creation_Form SHALL display all form fields on a single page: title, goal, duration (days), milestones list, and quest frequency
2. THE Arc_Creation_Form SHALL validate the title field as required with a maximum length of 100 characters
3. THE Arc_Creation_Form SHALL validate the goal field as required with a maximum length of 500 characters
4. THE Arc_Creation_Form SHALL validate the duration field as required with a minimum of 30 days and a maximum of 90 days
5. THE Arc_Creation_Form SHALL provide add and remove controls for the milestones list, requiring at least one milestone
6. THE Arc_Creation_Form SHALL validate the quest frequency field as required with selectable options (daily, weekly, custom)
7. WHEN the user taps the "Create Arc" button with valid form data, THE Arc_Creation_Form SHALL submit the data to the POST /api/v1/arcs endpoint
8. WHEN the API returns a successful response, THE Arc_Creation_Form SHALL navigate to the Arc_Detail_Page for the newly created arc and display a success toast
9. IF the API returns a validation error, THEN THE Arc_Creation_Form SHALL display the error message below the relevant form field
10. IF the user is a free-tier user who already has one custom arc, THEN THE Arc_Creation_Form SHALL display a message indicating the custom arc limit and offer a premium upgrade option
11. THE Arc_Creation_Form SHALL display inline validation errors as the user interacts with each field (on blur)
12. THE Arc_Creation_Form SHALL disable the "Create Arc" button while the form is invalid or while a submission is in progress

### Requirement 10: Arc Identity Titles

**User Story:** As a user, I want to earn evolving identity titles as I progress through an arc, so that I feel a sense of transformation and personal growth.

#### Acceptance Criteria

1. THE Arc_Detail_Page SHALL display the user's current Identity_Title for the active arc prominently below the Cinematic_Banner
2. THE Identity_Title SHALL evolve based on the user's current phase: a starting title at Beginner phase, an intermediate title at Intermediate phase, an elite title at Elite phase, and a mastery title at Master phase
3. WHEN the user advances to a new phase, THE Arc_Detail_Page SHALL display the new Identity_Title with a reveal animation (fade-in with scale from 0.8 to 1.0 over 300ms)
4. THE Identity_Title SHALL be styled using the display font (Orbitron) with the arc type themed gradient as the text color
5. THE Arc_Detail_Page SHALL display both the previous title and the current title during the phase transition animation with a crossfade effect
6. THE Identity_Title SHALL include an aria-label of "Your arc identity: {title}" for screen reader accessibility

### Requirement 11: Arc Store State Management

**User Story:** As a user, I want arc data to load efficiently and update reactively, so that the UI feels responsive and I see up-to-date information without manual refreshes.

#### Acceptance Criteria

1. THE Arc_Store SHALL manage reactive state using Angular signals for: available arcs list, active arc progress, selected arc detail, and loading states
2. THE Arc_Store SHALL expose computed signals for filtered arc lists (prebuilt arcs, user active arcs, completed arcs) derived from the available arcs signal
3. WHEN the Arc_List_Page initializes, THE Arc_Store SHALL fetch available arcs from the API only if the arcs signal is empty (cache-first strategy)
4. WHEN the user completes a milestone, THE Arc_Store SHALL optimistically update the local progress state and reconcile with the API response
5. THE Arc_Store SHALL expose a loading signal per operation (fetching list, fetching detail, creating arc) to enable granular skeleton loading in the UI
6. IF an API request fails, THEN THE Arc_Store SHALL revert any optimistic updates and expose an error signal with the failure message

### Requirement 12: Navigation and Routing

**User Story:** As a user, I want smooth navigation between arc screens with proper back navigation, so that I can move through the arc experience without losing context.

#### Acceptance Criteria

1. THE arc-mode feature SHALL define lazy-loaded routes for: Arc_List_Page (default route), Arc_Detail_Page (route parameter :id), and Arc_Creation_Form (route /create)
2. WHEN the user navigates to the Arc_Detail_Page, THE route SHALL include the arc ID as a path parameter and the Arc_Detail_Page SHALL use this parameter to fetch arc detail
3. THE Arc_Detail_Page SHALL include a back button in the toolbar that navigates to the Arc_List_Page
4. THE Arc_Creation_Form SHALL include a back button in the toolbar that navigates to the Arc_List_Page
5. WHEN the user navigates back from the Arc_Detail_Page, THE Arc_List_Page SHALL restore the previously selected tab segment without re-fetching data

### Requirement 13: Loading and Error States

**User Story:** As a user, I want clear feedback when content is loading or when something goes wrong, so that I understand the app state and can take corrective action.

#### Acceptance Criteria

1. WHILE any arc data is loading, THE Arc_List_Page SHALL display the shared skeleton-loader component with card-shaped placeholders matching the arc card dimensions
2. WHILE arc detail data is loading, THE Arc_Detail_Page SHALL display skeleton placeholders for the banner, phase progress, timeline, and boss section
3. IF the arc list request fails, THEN THE Arc_List_Page SHALL display the shared error-state component with a "Retry" button that re-triggers the data fetch
4. IF the arc detail request fails, THEN THE Arc_Detail_Page SHALL display the shared error-state component with a "Retry" button
5. WHILE the Arc_Creation_Form submission is in progress, THE Arc_Creation_Form SHALL display a loading spinner on the "Create Arc" button and disable all form inputs

### Requirement 14: Accessibility

**User Story:** As a user with accessibility needs, I want all arc screens to be navigable with assistive technology, so that I can use the feature regardless of my abilities.

#### Acceptance Criteria

1. THE Arc_List_Page ion-segment tabs SHALL include role="tablist" with each tab having role="tab" and aria-selected reflecting the active state
2. THE Milestone_Timeline accordion SHALL use aria-expanded on section headers and aria-controls linking to content panel IDs
3. THE Arc_Creation_Form SHALL associate each input with a label element and display aria-describedby references to validation error messages
4. THE Cinematic_Banner SHALL include an aria-label summarizing the arc name, phase, and progress (e.g., "Monk Arc, Intermediate phase, 43% complete")
5. THE Arc_Detail_Page SHALL use semantic heading hierarchy (h1 for arc name, h2 for section titles like "Milestones", "Boss", "Rewards")
6. ALL interactive elements across arc screens SHALL meet the minimum touch target size of 44x44 CSS pixels
