# Implementation Plan: League System

## Overview

Competitive weekly leagues with tier-based matchmaking, league score calculation, weekly promotion/demotion cycles, and anti-cheat detection.

## Tasks

- [ ] 1. Create League DTOs and enums
  - [ ] 1.1 Create league data models
    - Create `LeagueTier.java` enum: BRONZE, SILVER, GOLD, PLATINUM, DIAMOND, MASTER, ASCENDANT with min level requirements (0, 10, 20, 35, 50, 75, invite-only)
    - Create `LeaderboardResponse.java`: list of LeaderboardEntry, userRank, totalUsers, league
    - Create `LeaderboardEntry.java`: rank, userId, username, avatarUrl, level, weeklyXp, leagueScore, streak
    - Create `LeagueInfoResponse.java`: currentTier, leagueScore, weeklyRank, promotionZone, demotionZone, groupSize

- [ ] 2. Implement League Service
  - [ ] 2.1 Create LeagueService
    - Create `LeagueService.java` in `league/service/`
    - `assignTier(int userLevel)` — determine tier based on level thresholds
    - `calculateLeagueScore(int level, double consistency, int streak, double activityScore)`:
      - Formula: 0.4×Level + 0.3×Consistency + 0.2×Streak + 0.1×ActivityScore
    - `getLeaderboard(String league, int page, int size)` — paginated leaderboard for a tier
    - `getUserLeagueInfo(UUID userId)` — current tier, rank, score
  - [ ] 2.2 Create MatchmakingService
    - Create `MatchmakingService.java` in `league/service/`
    - `assignToGroup(UUID userId)`:
      1. Find or create a league group for user's tier
      2. Groups of ~50 users with similar league scores
      3. Assign user to group
    - Groups are created at the start of each weekly cycle

- [ ] 3. Implement Weekly League Cycle
  - [ ] 3.1 Create LeagueResetScheduler
    - Create `LeagueResetScheduler.java` in `league/scheduler/`
    - Run every Sunday at 23:59 UTC
    - For each league group:
      1. Rank users by league score
      2. Top 15 → promote to next tier
      3. Bottom 15 → demote to previous tier
      4. Reset weekly_xp to 0
      5. Publish LeaguePromotionEvent / LeagueDemotionEvent
    - Reassign groups for new week
  - [ ] 3.2 Create XP event listener for leaderboard updates
    - Create `LeagueEventListener.java` in `league/event/`
    - Listen for `XpAwardedEvent`
    - Update user's weekly_xp in leaderboard table
    - Recalculate rank (or defer to periodic batch)
    - Invalidate Redis leaderboard cache

- [ ] 4. Implement Anti-Cheat Detection
  - [ ] 4.1 Create AntiCheatService
    - Create `AntiCheatService.java` in `league/service/` (or `common/security/`)
    - `detectSpeedViolation(UUID userId)`:
      1. Count quest completions in last 5 minutes
      2. If > 10 → flag account
      3. Apply penalties: XP rollback, leaderboard ban
      4. Log security event
    - `detectBulkSpam(UUID userId)`:
      1. Check for 50+ completions in 2 minutes
      2. Flag and penalize
    - Called on each QuestCompletedEvent

- [ ] 5. Create League Controller
  - [ ] 5.1 Implement REST endpoints
    - Create `LeagueController.java` in `league/controller/`
    - GET `/api/v1/league/leaderboard?tier={tier}&page={page}` — paginated leaderboard
    - GET `/api/v1/league/info` — current user's league info
    - GET `/api/v1/league/history` — past week results and promotions

- [ ] 6. Write property-based tests
  - [ ] 6.1 Create league property tests
    - Create `LeaguePropertyTest.java`:
      - Property 26: Tier assignment correctness for all levels
      - Property 27: League score formula correctness
      - Property 28: Promotion/demotion (top 15 promoted, bottom 15 demoted)
      - Property 29: Anti-cheat speed detection (>10 in 5 min flagged)
    - Minimum 100 iterations per property

- [ ] 7. Checkpoint - Verify league system
  - Integration test: user levels up → tier assignment updates
  - Integration test: weekly reset → promotions and demotions applied
  - Integration test: speed violation detected → account flagged
  - Property tests all pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Leaderboard data is cached in Redis (5 min TTL) for performance
- Anti-cheat runs on every quest completion — must be fast
- Weekly cycle runs once (Sunday) — idempotent in case of retry
- Ascendant tier is invite-only (admin action)
- Virtual scrolling on frontend for large leaderboards
