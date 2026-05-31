# Requirements Document: Social Tab UI

## Introduction

This document defines the requirements for the Social tab in Ascend — a multi-section screen providing access to the Leaderboard, Guild, Friends, and Challenges features. The tab serves as the social hub where users compete, collaborate, and connect with other players. It uses a segmented tab layout with four sub-sections, each backed by existing backend APIs.

## Glossary

- **Social_Tab**: The fourth tab in the main navigation, containing Leaderboard, Guild, Friends, and Challenges sub-sections
- **Leaderboard_Section**: Displays ranked players within the user's current league tier with weekly XP totals
- **Guild_Section**: Shows the user's guild (if joined), guild discovery, and guild leaderboard
- **Friends_Section**: Manages friend list, pending requests, and friend search
- **Challenges_Section**: Displays active friend-vs-friend challenges with progress tracking
- **League_Tier**: One of BRONZE, SILVER, GOLD, PLATINUM, DIAMOND — determines which leaderboard the user sees
- **Friend_Request**: A pending connection request between two users (PENDING → ACCEPTED/BLOCKED)
- **Challenge**: A competitive goal between two friends with progress tracking and a winner declaration

## Requirements

### Requirement 1: Social Tab Layout and Navigation

**User Story:** As a user, I want a clear tabbed layout within the Social screen so I can quickly switch between leaderboard, guild, friends, and challenges.

#### Acceptance Criteria

1. THE Social_Tab SHALL display a segmented control with four segments: Leaderboard, Guild, Friends, Challenges
2. THE default selected segment SHALL be "Leaderboard" on first visit
3. WHEN the user taps a segment, THE content area SHALL switch to the corresponding section without a full page reload
4. THE Social_Tab SHALL persist the last selected segment within the session (navigating away and back restores the segment)
5. EACH segment button SHALL have a minimum touch target of 44×44px
6. THE segmented control SHALL use `role="tablist"` with `aria-selected` on the active tab
7. THE Social_Tab SHALL show a pull-to-refresh gesture that reloads the current section's data

### Requirement 2: Leaderboard Section

**User Story:** As a user, I want to see my ranking among other players in my league so I feel motivated to compete.

#### Acceptance Criteria

1. THE Leaderboard_Section SHALL display the user's current league tier name and icon at the top
2. THE Leaderboard_Section SHALL display the user's current rank, weekly XP, and promotion/demotion zone status
3. THE Leaderboard_Section SHALL display a scrollable list of ranked players using the shared `app-leaderboard-card` component
4. THE top 3 players SHALL have distinct visual styling (gold, silver, bronze medal indicators)
5. THE current user's entry SHALL be visually highlighted in the list
6. THE Leaderboard_Section SHALL display the user's league info (tier, rank, promotion zone) from `GET /api/v1/league/info`
7. THE Leaderboard_Section SHALL load leaderboard entries from `GET /api/v1/league/leaderboard?tier={tier}&page={page}`
8. THE Leaderboard_Section SHALL support infinite scroll pagination for the leaderboard list
9. THE Leaderboard_Section SHALL show a skeleton loader during initial data fetch
10. THE Leaderboard_Section SHALL show promotion zone (top 5) in green and demotion zone (bottom 5) in red

### Requirement 3: Guild Section

**User Story:** As a user, I want to view my guild, discover new guilds, and see guild rankings so I can collaborate with others.

#### Acceptance Criteria

1. IF the user is a guild member, THE Guild_Section SHALL display the user's guild card with name, level, member count, and rank
2. IF the user is a guild member, THE Guild_Section SHALL show a "View Guild" button navigating to guild detail
3. IF the user is NOT a guild member, THE Guild_Section SHALL display a "Find a Guild" prompt with a search/browse interface
4. THE Guild_Section SHALL display a "Guild Leaderboard" sub-section showing top guilds from `GET /api/v1/guilds/leaderboard`
5. THE Guild_Section SHALL display a "Browse Guilds" list from `GET /api/v1/guilds` with join buttons
6. THE Guild_Section SHALL use the shared `app-guild-card` component for guild display
7. WHEN the user taps "Join" on a guild card, THE system SHALL call `POST /api/v1/guilds/{id}/join` and show a success toast
8. THE Guild_Section SHALL show a skeleton loader during data fetch
9. THE Guild_Section SHALL show an empty state with "Create Guild" CTA if no guilds are available

### Requirement 4: Friends Section

**User Story:** As a user, I want to manage my friends, accept requests, and find new friends so I can build my social network.

#### Acceptance Criteria

1. THE Friends_Section SHALL display the user's accepted friends list from `GET /api/v1/friends`
2. EACH friend entry SHALL show username, avatar, level, current streak, and online status indicator
3. THE Friends_Section SHALL display a "Pending Requests" badge count and expandable section from `GET /api/v1/friends/pending`
4. EACH pending request SHALL show Accept and Decline buttons
5. WHEN the user taps Accept, THE system SHALL call `POST /api/v1/friends/accept` and move the friend to the accepted list
6. THE Friends_Section SHALL include a search input to find users by username
7. THE Friends_Section SHALL show a "Send Request" button next to search results
8. WHEN the user taps "Send Request", THE system SHALL call `POST /api/v1/friends/request`
9. THE Friends_Section SHALL show an empty state "No friends yet. Search for players to connect!" when the list is empty
10. THE Friends_Section SHALL support swipe-to-remove on friend entries (calls `DELETE /api/v1/friends/{friendId}`)
11. THE Friends_Section SHALL show a skeleton loader during data fetch

### Requirement 5: Challenges Section

**User Story:** As a user, I want to see my active challenges against friends so I can track my competitive progress.

#### Acceptance Criteria

1. THE Challenges_Section SHALL display active challenges from `GET /api/v1/social/challenges`
2. EACH challenge card SHALL show: title, opponent name/avatar, target, my progress, opponent progress, and time remaining
3. THE challenge progress SHALL be displayed as dual progress bars (user vs opponent)
4. COMPLETED challenges SHALL show the winner with a trophy icon and "Won"/"Lost" badge
5. THE Challenges_Section SHALL include a "Challenge a Friend" FAB button
6. WHEN the user taps "Challenge a Friend", THE system SHALL navigate to a challenge creation form
7. THE challenge creation form SHALL allow selecting a friend, setting a title, target count, and end date
8. THE Challenges_Section SHALL show an empty state "No active challenges. Challenge a friend!" when empty
9. THE Challenges_Section SHALL show a skeleton loader during data fetch

### Requirement 6: Error Handling and Loading States

**User Story:** As a user, I want clear feedback when data is loading or when errors occur so I understand the app's state.

#### Acceptance Criteria

1. EACH section SHALL independently manage its own loading state (skeleton loaders)
2. EACH section SHALL display an error state with retry button on API failure
3. THE pull-to-refresh SHALL reload only the currently visible section
4. NETWORK errors SHALL show the global offline banner (handled by existing infrastructure)
5. THE Social_Tab SHALL use cache-first strategy — show cached data immediately, then refresh in background

### Requirement 7: Accessibility

**User Story:** As a user with accessibility needs, I want the social tab to be fully navigable with assistive technology.

#### Acceptance Criteria

1. THE segmented control SHALL use `role="tablist"` with each segment having `role="tab"` and `aria-selected`
2. THE content panels SHALL use `role="tabpanel"` with `aria-labelledby` linking to the corresponding tab
3. ALL interactive elements SHALL have minimum 44×44px touch targets
4. ALL list items SHALL have descriptive `aria-label` attributes
5. THE pending requests badge SHALL use `aria-label="X pending friend requests"`
6. SCREEN readers SHALL announce section changes when tabs are switched
7. THE challenge progress bars SHALL have `aria-valuenow`, `aria-valuemin`, `aria-valuemax` attributes

### Requirement 8: Performance

**User Story:** As a user, I want the social tab to load quickly and feel responsive.

#### Acceptance Criteria

1. ALL components SHALL use `ChangeDetectionStrategy.OnPush`
2. THE leaderboard SHALL use virtual scrolling for lists exceeding 50 items
3. ONLY the active section SHALL fetch data — inactive sections defer loading until selected
4. THE Social_Tab SHALL use signals-based state management consistent with the project architecture
5. IMAGES (avatars) SHALL use lazy loading
