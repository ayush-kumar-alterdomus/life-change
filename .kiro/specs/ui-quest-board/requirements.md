# Requirements Document

## Introduction

The Quest Board is the primary interface for users to view, manage, and interact with their quests in Ascend. This feature replaces the current quest-board page with a fresh implementation featuring Daily/Weekly/Custom tabs, quest cards using the shared `app-quest-card` component, a bottom-sheet quest detail modal, an enhanced custom quest creation form, and a free-user 5-quest limit gate that blocks creation beyond 5 active custom quests.

## Glossary

- **Quest_Board**: The main page component that displays quests organized by frequency tabs and provides quest management actions
- **Quest_Card**: The existing shared dumb component (`app-quest-card`) that renders individual quest information including title, XP reward, difficulty, stat type, and time estimate
- **Tab_Bar**: The segmented control at the top of the quest list that allows switching between Daily, Weekly, and Custom quest views
- **Quest_Detail_Sheet**: A bottom-sheet modal that slides up over the Quest_Board to display full quest information and action controls
- **Create_Quest_Form**: The modal form used to create new custom quests with fields for title, description, difficulty, XP reward, stat type, frequency, and time estimate
- **Quest_Limit_Gate**: The mechanism that prevents free-tier users from creating more than 5 active custom quests and displays an upgrade prompt
- **Free_User**: A user on the free tier who has restricted access to custom quest creation (maximum 5 active custom quests)
- **Premium_User**: A user on a paid subscription tier with unrestricted custom quest creation
- **Active_Custom_Quest**: A custom quest that has not been completed or deleted

## Requirements

### Requirement 1: Tab-Based Quest Organization

**User Story:** As a user, I want to view my quests organized by frequency (Daily, Weekly, Custom), so that I can focus on the relevant quests for my current needs.

#### Acceptance Criteria

1. THE Quest_Board SHALL display a Tab_Bar with three tabs labeled "Daily", "Weekly", and "Custom"
2. WHEN the user selects the "Daily" tab, THE Quest_Board SHALL display only quests with daily frequency
3. WHEN the user selects the "Weekly" tab, THE Quest_Board SHALL display only quests with weekly frequency
4. WHEN the user selects the "Custom" tab, THE Quest_Board SHALL display only quests with custom frequency
5. WHEN the Quest_Board loads for the first time, THE Tab_Bar SHALL default to the "Daily" tab as the active selection
6. THE Quest_Board SHALL preserve the selected tab state during pull-to-refresh operations

### Requirement 2: Quest Card Display

**User Story:** As a user, I want to see my quests displayed as cards with key information at a glance, so that I can quickly assess quest details without opening each one.

#### Acceptance Criteria

1. THE Quest_Board SHALL render each quest using the shared Quest_Card component
2. THE Quest_Card SHALL display the quest title, XP reward, difficulty badge, stat type, and time estimate for each quest
3. WHEN a quest is marked as completed, THE Quest_Card SHALL display a completed visual state
4. THE Quest_Board SHALL display a loading skeleton state while quest data is being fetched
5. WHEN no quests exist for the selected tab, THE Quest_Board SHALL display an empty state with contextual messaging

### Requirement 3: Quest Detail Bottom Sheet

**User Story:** As a user, I want to tap a quest card to see full details in a bottom sheet, so that I can review quest information without leaving the quest board.

#### Acceptance Criteria

1. WHEN the user taps a Quest_Card, THE Quest_Board SHALL open the Quest_Detail_Sheet as a bottom-sheet modal sliding up from the bottom of the screen
2. THE Quest_Detail_Sheet SHALL display the quest title, full description, XP reward with formula breakdown, difficulty level, stat type, time estimate, and frequency
3. THE Quest_Detail_Sheet SHALL display an action bar with "Complete", "Edit", and "Skip" buttons for incomplete quests
4. WHEN the user taps the "Complete" button in the Quest_Detail_Sheet, THE Quest_Board SHALL mark the quest as completed and award XP
5. WHEN the user swipes down or taps outside the Quest_Detail_Sheet, THE Quest_Detail_Sheet SHALL dismiss and return focus to the Quest_Board
6. WHILE a quest is already completed, THE Quest_Detail_Sheet SHALL hide the "Complete" and "Skip" action buttons and display completion status

### Requirement 4: Custom Quest Creation Form

**User Story:** As a user, I want to create custom quests with detailed parameters, so that I can tailor my quest experience to my personal goals.

#### Acceptance Criteria

1. WHEN the user taps the floating action button on the Quest_Board, THE Create_Quest_Form SHALL open as a modal
2. THE Create_Quest_Form SHALL provide input fields for title (required), description (optional), difficulty selection (Easy, Medium, Hard, Legendary), stat type selection, frequency selection (Daily, Weekly, Custom), and time estimate (optional)
3. THE Create_Quest_Form SHALL calculate and display the XP reward based on the selected difficulty level
4. WHEN the user submits a valid form, THE Create_Quest_Form SHALL create the quest and close the modal
5. IF the user submits the form with missing required fields, THEN THE Create_Quest_Form SHALL display inline validation errors for each invalid field
6. WHEN a quest is successfully created, THE Quest_Board SHALL refresh the quest list and display a success confirmation toast

### Requirement 5: Free User Quest Limit Gate

**User Story:** As a free user, I want to understand my quest creation limits, so that I know when I need to upgrade to create more custom quests.

#### Acceptance Criteria

1. THE Quest_Board SHALL allow Free_User accounts to view all quests across all tabs without restriction
2. WHILE a Free_User has fewer than 5 Active_Custom_Quests, THE Create_Quest_Form SHALL allow quest creation without restriction
3. WHEN a Free_User with 5 Active_Custom_Quests taps the floating action button, THE Quest_Board SHALL display an upgrade prompt instead of opening the Create_Quest_Form
4. THE upgrade prompt SHALL communicate that the user has reached the free-tier limit of 5 custom quests and provide a call-to-action to upgrade to premium
5. THE Quest_Limit_Gate SHALL not apply to Premium_User accounts

### Requirement 6: Progress Summary

**User Story:** As a user, I want to see my daily quest completion progress, so that I can track how many quests I have finished today.

#### Acceptance Criteria

1. THE Quest_Board SHALL display a progress summary card showing the count of completed quests versus total quests for the current day
2. THE progress summary card SHALL include a visual progress bar representing the completion ratio
3. WHEN a quest is completed, THE Quest_Board SHALL update the progress summary in real time without requiring a page refresh

### Requirement 7: Pull-to-Refresh

**User Story:** As a user, I want to pull down to refresh my quest list, so that I can see the latest quest data without navigating away.

#### Acceptance Criteria

1. WHEN the user performs a pull-to-refresh gesture on the Quest_Board, THE Quest_Board SHALL fetch fresh quest data from the server
2. WHEN the refresh completes successfully, THE Quest_Board SHALL update the displayed quests and progress summary
3. IF the refresh request fails, THEN THE Quest_Board SHALL display an error toast and retain the previously loaded data
