# Implementation Plan: Social Tab UI

## Overview

Implement the Social tab as a multi-section feature with Leaderboard, Guild, Friends, and Challenges sub-sections. Follows smart page + dumb components pattern with signals-based store, replacing the existing placeholder.

## Tasks

- [x] 1. Data layer: models, services, and store
  - [x] 1.1 Create Social data models
    - Create `frontend/src/features/social/models/social.models.ts`
    - Define interfaces: `LeagueInfo`, `LeaderboardEntry`, `GuildInfo`, `FriendInfo`, `ChallengeInfo`, `CreateChallengePayload`
    - Export all from barrel `frontend/src/features/social/models/index.ts`
    - _Requirements: 2.1, 3.1, 4.1, 5.1_

  - [x] 1.2 Create LeagueService HTTP layer
    - Create `frontend/src/features/social/services/league.service.ts`
    - `getLeagueInfo()` → GET `/api/v1/league/info`
    - `getLeaderboard(tier, page)` → GET `/api/v1/league/leaderboard?tier={tier}&page={page}&size=50`
    - _Requirements: 2.6, 2.7_

  - [x] 1.3 Create GuildService HTTP layer
    - Create `frontend/src/features/social/services/guild.service.ts`
    - `listGuilds(page)` → GET `/api/v1/guilds?page={page}`
    - `getGuildLeaderboard(page)` → GET `/api/v1/guilds/leaderboard?page={page}`
    - `joinGuild(guildId)` → POST `/api/v1/guilds/{id}/join`
    - `leaveGuild(guildId)` → POST `/api/v1/guilds/{id}/leave`
    - _Requirements: 3.4, 3.5, 3.7_

  - [x] 1.4 Create SocialService HTTP layer
    - Create `frontend/src/features/social/services/social.service.ts`
    - `getFriends()` → GET `/api/v1/friends`
    - `getPendingRequests()` → GET `/api/v1/friends/pending`
    - `sendFriendRequest(friendId)` → POST `/api/v1/friends/request`
    - `acceptFriendRequest(friendId)` → POST `/api/v1/friends/accept`
    - `removeFriend(friendId)` → DELETE `/api/v1/friends/{friendId}`
    - `getChallenges()` → GET `/api/v1/social/challenges`
    - `createChallenge(payload)` → POST `/api/v1/social/challenges`
    - _Requirements: 4.1, 4.3, 4.5, 4.7, 4.8, 4.10, 5.1, 5.6_

  - [x] 1.5 Create SocialStore signals-based state
    - Create `frontend/src/features/social/store/social.store.ts`
    - State signals: `leagueInfo`, `leaderboardEntries`, `myGuild`, `browseGuilds`, `guildLeaderboard`, `friends`, `pendingRequests`, `challenges`
    - Per-section loading signals: `loadingLeaderboard`, `loadingGuilds`, `loadingFriends`, `loadingChallenges`
    - Per-section error signals: `leaderboardError`, `guildsError`, `friendsError`, `challengesError`
    - Actions: `loadLeaderboard()`, `loadMoreLeaderboard()`, `loadGuilds()`, `joinGuild(id)`, `loadFriends()`, `acceptFriendRequest(id)`, `removeFriend(id)`, `sendFriendRequest(id)`, `loadChallenges()`
    - _Requirements: 6.1, 6.2, 6.5, 8.4_

- [x] 2. Checkpoint - Data layer compiles cleanly
  - Ensure all tests pass, ask the user if questions arise.

- [x] 3. Dumb components: Leaderboard and Guild sections
  - [x] 3.1 Create LeaderboardSectionComponent
    - Create `frontend/src/features/social/components/leaderboard-section/leaderboard-section.component.ts`
    - Inputs: `leagueInfo`, `entries`, `loading`, `error`
    - Outputs: `loadMore`, `retry`
    - Display league tier badge, user rank, promotion/demotion zone indicators
    - Render entries using shared `app-leaderboard-card`
    - Show skeleton loader during loading, error state with retry on failure
    - Highlight promotion zone (top 5) in green, demotion zone (bottom 5) in red
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.9, 2.10_

  - [x] 3.2 Create GuildSectionComponent
    - Create `frontend/src/features/social/components/guild-section/guild-section.component.ts`
    - Inputs: `myGuild`, `browseGuilds`, `guildLeaderboard`, `loading`, `error`
    - Outputs: `joinGuild`, `viewGuild`, `createGuild`, `retry`
    - Show user's guild card if member, else show "Find a Guild" prompt
    - Display guild leaderboard (top 5) and browse list
    - Use shared `app-guild-card` for all guild displays
    - Show skeleton loader during loading, error state with retry
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9_

- [x] 4. Dumb components: Friends and Challenges sections
  - [x] 4.1 Create FriendsSectionComponent
    - Create `frontend/src/features/social/components/friends-section/friends-section.component.ts`
    - Inputs: `friends`, `pendingRequests`, `loading`, `error`
    - Outputs: `acceptRequest`, `declineRequest`, `removeFriend`, `sendRequest`, `retry`
    - Display accepted friends list with avatar, level, streak, online indicator
    - Display pending requests section with badge count, Accept/Decline buttons
    - Include search input for finding users by username
    - Support swipe-to-remove gesture on friend entries
    - Show empty state when no friends
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 4.9, 4.10, 4.11_

  - [x] 4.2 Create ChallengeCardComponent
    - Create `frontend/src/features/social/components/challenge-card/challenge-card.component.ts`
    - Input: `challenge` (ChallengeInfo)
    - Display title, opponent info, dual progress bars, time remaining
    - Show winner badge (trophy + "Won"/"Lost") for completed challenges
    - Progress bars with `aria-valuenow`, `aria-valuemin`, `aria-valuemax`
    - _Requirements: 5.2, 5.3, 5.4, 7.7_

  - [x] 4.3 Create ChallengesSectionComponent
    - Create `frontend/src/features/social/components/challenges-section/challenges-section.component.ts`
    - Inputs: `challenges`, `loading`, `error`
    - Outputs: `createChallenge`, `retry`
    - Render challenge cards for active and completed challenges
    - Include "Challenge a Friend" FAB button
    - Show empty state when no challenges
    - _Requirements: 5.1, 5.5, 5.6, 5.8, 5.9_

- [x] 5. Checkpoint - Dumb components compile cleanly
  - Ensure all tests pass, ask the user if questions arise.

- [x] 6. Smart page: SocialHubComponent
  - [x] 6.1 Create SocialHubComponent
    - Create `frontend/src/features/social/pages/social-hub/social-hub.component.ts`
    - Inject `SocialStore` and `Router`
    - Implement segmented control with 4 tabs (Leaderboard, Guild, Friends, Challenges)
    - Default to "Leaderboard" tab
    - Only load data for the active section (lazy section loading)
    - Implement pull-to-refresh reloading current section
    - Wire all dumb component outputs to store actions
    - Handle navigation: guild detail, challenge create
    - Add `role="tablist"` / `role="tab"` / `role="tabpanel"` accessibility pattern
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 6.3, 7.1, 7.2, 7.6, 8.3_

  - [x] 6.2 Create ChallengeCreateComponent (optional page)
    - Create `frontend/src/features/social/pages/challenge-create/challenge-create.component.ts`
    - Reactive form: friend selection, title, target, end date
    - Validate: friend required, title required, target min 1, end date in future
    - Submit calls `SocialStore.createChallenge()`
    - Navigate back to social tab on success with toast
    - _Requirements: 5.6, 5.7_

- [x] 7. Routing and integration
  - [x] 7.1 Update social routes and remove placeholder
    - Update `frontend/src/features/social/social.routes.ts` with lazy-loaded routes
    - Delete placeholder `frontend/src/features/social/pages/social.component.ts`
    - Routes: '' (SocialHub), 'challenge/create' (ChallengeCreate)
    - _Requirements: 1.1_

  - [x] 7.2 Create barrel exports
    - Create `frontend/src/features/social/components/index.ts`
    - Create `frontend/src/features/social/services/index.ts`
    - Create `frontend/src/features/social/store/index.ts`
    - Create `frontend/src/features/social/models/index.ts`

- [x] 8. Property-based tests (optional)
  - [x]* 8.1 Create social property tests
    - **Property 1: Leaderboard entries are sorted by rank ascending**
    - **Property 2: Promotion zone contains only top 5, demotion zone only bottom 5**
    - **Property 3: Challenge progress percentage never exceeds 100%**
    - **Property 4: Pending requests count matches badge display**
    - **Property 5: Tab switching only loads data for the active section**
    - Minimum 100 iterations per property

- [x] 9. Final checkpoint
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- The existing placeholder at `pages/social.component.ts` will be removed in task 7.1
- Shared components available: `app-leaderboard-card`, `app-guild-card`, `skeleton-loader`, `error-state`
- Backend APIs are already implemented: `/api/v1/friends/*`, `/api/v1/guilds/*`, `/api/v1/league/*`, `/api/v1/social/challenges`
- The `ChallengeService` on the backend is in the `social` module (not a separate challenges module)
- Online presence indicator uses the existing `PresenceService` (Firestore-based)

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1"] },
    { "id": 1, "tasks": ["1.2", "1.3", "1.4"] },
    { "id": 2, "tasks": ["1.5"] },
    { "id": 3, "tasks": ["3.1", "3.2"] },
    { "id": 4, "tasks": ["4.1", "4.2", "4.3"] },
    { "id": 5, "tasks": ["6.1", "6.2"] },
    { "id": 6, "tasks": ["7.1", "7.2"] },
    { "id": 7, "tasks": ["8.1"] }
  ]
}
```
