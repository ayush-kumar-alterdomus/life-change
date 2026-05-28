# Implementation Plan: Social Features

## Overview

Friend system, challenges between friends, activity feed, accountability partners, and privacy controls.

## Tasks

- [ ] 1. Create Social data models
  - [ ] 1.1 Create tables migration
    - Create `V30__create_social_tables.sql`:
      - friendships: id (UUID PK), user_id (UUID FK), friend_id (UUID FK), status (VARCHAR — PENDING, ACCEPTED, BLOCKED), created_at (TIMESTAMP) — UNIQUE(user_id, friend_id)
      - challenges: id (UUID PK), challenger_id (UUID FK), challenged_id (UUID FK), title (VARCHAR), description (TEXT), target (INT), challenger_progress (INT default 0), challenged_progress (INT default 0), status (VARCHAR — ACTIVE, COMPLETED, EXPIRED), winner_id (UUID FK nullable), created_at (TIMESTAMP), ends_at (TIMESTAMP)
      - activity_feed: id (UUID PK), user_id (UUID FK), event_type (VARCHAR), title (VARCHAR), description (TEXT), created_at (TIMESTAMP)
      - accountability_partners: id (UUID PK), user_id (UUID FK), partner_id (UUID FK), active (BOOLEAN default true), created_at (TIMESTAMP) — UNIQUE(user_id, partner_id)
    - Add `privacy_level` column to users (VARCHAR default 'PUBLIC' — PUBLIC, FRIENDS_ONLY, PRIVATE)
  - [ ] 1.2 Create DTOs
    - Create `FriendResponse.java`: userId, username, avatarUrl, level, streak, status
    - Create `ChallengeResponse.java`: id, opponent, title, target, myProgress, opponentProgress, status, winner
    - Create `CreateChallengeRequest.java`: friendId, title, target, endsAt
    - Create `ActivityFeedItem.java`: userId, username, eventType, title, description, timestamp
    - Create `PrivacyLevel.java` enum: PUBLIC, FRIENDS_ONLY, PRIVATE

- [ ] 2. Implement Friend Service
  - [ ] 2.1 Create FriendService
    - Create `FriendService.java` in `social/service/` (new module)
    - `sendFriendRequest(UUID userId, UUID friendId)` — create PENDING friendship
    - `acceptFriendRequest(UUID userId, UUID friendId)` — update to ACCEPTED
    - `removeFriend(UUID userId, UUID friendId)` — delete friendship
    - `blockUser(UUID userId, UUID blockedId)` — set status to BLOCKED
    - `getFriends(UUID userId)` — list accepted friends
    - `getPendingRequests(UUID userId)` — list incoming requests

- [ ] 3. Implement Challenge System
  - [ ] 3.1 Create ChallengeService
    - Create `ChallengeService.java` in `social/service/`
    - `createChallenge(UUID challengerId, CreateChallengeRequest request)`:
      1. Verify users are friends
      2. Create challenge record
      3. Notify challenged user
    - `contributeToChallenge(UUID userId, UUID challengeId, int progress)`:
      1. Update user's progress
      2. Check if target reached → declare winner
      3. Publish ChallengeCompletedEvent
    - Listen for QuestCompletedEvent → auto-contribute to active challenges

- [ ] 4. Implement Activity Feed
  - [ ] 4.1 Create ActivityFeedService
    - Create `ActivityFeedService.java` in `social/service/`
    - `publishActivity(UUID userId, String eventType, String title, String description)`:
      1. Create activity_feed record
      2. Respect user's privacy level
    - `getFeed(UUID userId, int page)`:
      1. Get user's friends
      2. Fetch recent activities from friends
      3. Filter by privacy (exclude PRIVATE users, respect FRIENDS_ONLY)
      4. Return paginated feed
    - Listen for events: LevelUpEvent, AchievementUnlockedEvent, ArcCompletedEvent → publish to feed

- [ ] 5. Implement Accountability Partners
  - [ ] 5.1 Create AccountabilityService
    - Create `AccountabilityService.java` in `social/service/`
    - `pairPartner(UUID userId, UUID partnerId)` — create partnership
    - `notifyPartnerMissedQuest(UUID userId)`:
      1. Find active partner
      2. Send notification to partner about missed quest
    - Listen for StreakBrokenEvent → notify accountability partner

- [ ] 6. Create Social Controller
  - [ ] 6.1 Implement REST endpoints
    - Create `SocialController.java` in `social/controller/`
    - GET `/api/v1/social/friends` — list friends
    - POST `/api/v1/social/friends/request` — send friend request
    - POST `/api/v1/social/friends/accept` — accept request
    - POST `/api/v1/social/challenges` — create challenge
    - GET `/api/v1/social/challenges` — active challenges
    - GET `/api/v1/social/feed` — activity feed
    - POST `/api/v1/social/accountability/pair` — pair accountability partner
    - PUT `/api/v1/social/privacy` — update privacy level

- [ ] 7. Write property-based tests
  - [ ] 7.1 Create social property tests
    - Create `PrivacyPropertyTest.java`:
      - Property 52: Privacy enforcement (PRIVATE users never in public feeds/search)
    - Minimum 100 iterations per property

- [ ] 8. Checkpoint - Verify social features
  - Integration test: send friend request → accept → appears in friends list
  - Integration test: create challenge → contribute → winner declared
  - Integration test: private user's activities not visible in feed
  - Property tests all pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Privacy levels control visibility in feeds and search
- Challenges auto-contribute from quest completions
- Accountability partners get alerts for each other's missed quests
- Activity feed is friend-scoped (not global)
- Block prevents all interaction
