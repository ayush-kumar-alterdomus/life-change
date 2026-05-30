# Implementation Plan: Guild System

## Overview

Social guilds with shared quests, real-time chat via WebSocket, guild leaderboards, and member management. Guilds provide community accountability and team challenges.

## Tasks

- [x] 1. Create Guild DTOs
  - [x] 1.1 Create guild data models
    - Create `GuildResponse.java`: id, name, description, type, memberCount, maxMembers, guildXp, ownerUsername
    - Create `GuildDetailResponse.java`: extends GuildResponse with members list, activeChallenges, rank
    - Create `CreateGuildRequest.java`: name, description, type (PUBLIC/PRIVATE/PREMIUM) — validated
    - Create `GuildMemberResponse.java`: userId, username, avatarUrl, role, joinedAt, weeklyContribution
    - Create `GuildChatMessage.java`: id, guildId, userId, username, message, timestamp
    - Create `GuildChallengeResponse.java`: id, title, target, currentProgress, endsAt, contributors

- [x] 2. Implement Guild Service
  - [-] 2.1 Create GuildService
    - Create `GuildService.java` in `guild/service/`
    - `createGuild(UUID userId, CreateGuildRequest request)`:
      1. Validate name uniqueness
      2. Determine max_members by user subscription (Free: 10, Premium: 50)
      3. Create guild record with owner
      4. Auto-add owner as LEADER role in guild_members
    - `joinGuild(UUID userId, UUID guildId)`:
      1. Verify guild is PUBLIC (or user has invite for PRIVATE)
      2. Verify guild not full (count < max_members)
      3. Verify user not already a member
      4. Add guild_members record with MEMBER role
    - `leaveGuild(UUID userId, UUID guildId)` — remove membership
    - `getGuildDetail(UUID guildId)` — full guild info with members
    - `listGuilds(String type, int page)` — paginated guild discovery
  - [-] 2.2 Create GuildRankingService
    - Create `GuildRankingService.java` in `guild/service/`
    - `calculateGuildRank()`:
      1. Score guilds by: avg consistency, total quests completed, avg streak
      2. Update guild rankings
    - `getGuildLeaderboard(int page)` — paginated guild rankings

- [x] 3. Implement Shared Guild Quests
  - [x] 3.1 Create guild challenge logic
    - `createChallenge(UUID guildId, String title, int target, Instant endsAt)`:
      1. Create guild_challenges record
      2. Notify guild members
    - `contributeToChallenge(UUID userId, UUID challengeId, int contribution)`:
      1. Increment current_progress
      2. If current_progress >= target → complete challenge, award guild XP
      3. Publish GuildChallengeCompleteEvent
    - Listen for QuestCompletedEvent — auto-contribute to active guild challenges

- [x] 4. Implement Guild Chat (WebSocket)
  - [x] 4.1 Create WebSocket chat handler
    - Create `GuildChatHandler.java` in `guild/service/`
    - Configure STOMP endpoint for guild chat: `/topic/guild/{guildId}/chat`
    - `sendMessage(UUID userId, UUID guildId, String message)`:
      1. Verify user is guild member
      2. Validate message (max 500 chars, no empty)
      3. Broadcast to `/topic/guild/{guildId}/chat`
      4. Persist to guild_chat collection in Firestore (for history)
    - Rate limit: 30 messages/minute per user
  - [x] 4.2 Configure WebSocket STOMP
    - Update `WebSocketConfig.java`:
      1. Enable STOMP over SockJS
      2. Configure message broker for `/topic` and `/user` prefixes
      3. Set application destination prefix `/app`
      4. Configure allowed origins

- [x] 5. Create Guild Controller
  - [x] 5.1 Implement REST endpoints
    - Create `GuildController.java` in `guild/controller/`
    - POST `/api/v1/guilds` — create guild
    - GET `/api/v1/guilds` — list/search guilds
    - GET `/api/v1/guilds/{id}` — guild detail
    - POST `/api/v1/guilds/{id}/join` — join guild
    - POST `/api/v1/guilds/{id}/leave` — leave guild
    - GET `/api/v1/guilds/{id}/chat/history` — chat history (paginated)
    - GET `/api/v1/guilds/leaderboard` — guild rankings

- [x] 6. Write property-based tests
  - [x] 6.1 Create guild property tests
    - Create `GuildPropertyTest.java`:
      - Property 30: Guild creation enforces tier caps (Free: 10, Premium: 50)
      - Property 31: Shared quest XP accumulates from all member contributions
    - Minimum 100 iterations per property

- [x] 7. Checkpoint - Verify guild system
  - Integration test: create guild → join → send chat message → received by members
  - Integration test: guild challenge → members contribute → challenge completes
  - Integration test: free user guild capped at 10 members
  - Property tests all pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- **RULE: Do NOT run any mvn, gradle, or test commands. Only create/edit files. No build or test verification steps.**
- Guild chat uses WebSocket STOMP for real-time delivery
- Chat history persisted in Firestore for quick retrieval
- Guild challenges auto-contribute from member quest completions
- Member cap enforced by subscription tier
- Guild rankings recalculated periodically (not real-time)

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1"] },
    { "id": 1, "tasks": ["2.1", "2.2"] },
    { "id": 2, "tasks": ["3.1", "4.1", "4.2"] },
    { "id": 3, "tasks": ["5.1"] },
    { "id": 4, "tasks": ["6.1"] },
    { "id": 5, "tasks": ["7"] }
  ]
}
```
