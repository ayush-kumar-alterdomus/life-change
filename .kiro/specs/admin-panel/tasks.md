# Implementation Plan: Admin Panel

## Overview

Administrative interface for content management (Arcs, Quests, Bosses), user moderation, system analytics, and seasonal events. Restricted to ADMIN and SUPER_ADMIN roles.

## Tasks

- [ ] 1. Create Admin DTOs
  - [ ] 1.1 Create admin data models
    - Create `AdminUserResponse.java`: id, username, email, level, premium, role, createdAt, lastActive, flagged, banned
    - Create `ModerationAction.java` enum: WARN, SUSPEND, BAN, LEADERBOARD_RESTRICT, UNFLAG
    - Create `ModerationRequest.java`: userId, action, reason, duration (for suspend)
    - Create `SystemAnalyticsResponse.java`: dau, wau, mau, retentionRate, premiumConversion, churnRate, streakSurvivalRate, avgSessionLength
    - Create `SeasonalEventRequest.java`: title, description, startDate, endDate, rewards (list), themedChallenges (list)

- [ ] 2. Implement Admin Content Management
  - [ ] 2.1 Create AdminService for Arc/Quest CMS
    - Create `AdminService.java` in `admin/service/`
    - `createArc(CreateArcRequest request)` — admin can create prebuilt arcs
    - `updateArc(UUID arcId, UpdateArcRequest request)` — edit existing arcs
    - `createQuest(CreateQuestRequest request)` — create system quests
    - `updateQuest(UUID questId, UpdateQuestRequest request)` — edit quests
    - `createBoss(CreateBossRequest request)` — create boss challenges
    - All operations invalidate relevant Redis caches

- [ ] 3. Implement User Moderation
  - [ ] 3.1 Create ModerationService
    - Create `ModerationService.java` in `admin/service/`
    - `moderateUser(UUID adminId, ModerationRequest request)`:
      - WARN: Send warning notification, log event
      - SUSPEND: Set user.suspended = true, duration-based (add migration)
      - BAN: Set user.banned = true, revoke all sessions
      - LEADERBOARD_RESTRICT: Remove from leaderboard, prevent future ranking
      - UNFLAG: Clear anti-cheat flags
    - `getflaggedUsers(int page)` — users flagged by anti-cheat
    - `getUserDetail(UUID userId)` — full admin view of user

- [ ] 4. Implement System Analytics
  - [ ] 4.1 Create SystemAnalyticsService
    - Create `SystemAnalyticsService.java` in `admin/service/`
    - `getSystemAnalytics()`:
      1. DAU: distinct users with activity today
      2. Retention: users active this week who were active last week
      3. Premium conversion: premium users / total users
      4. Churn: users inactive > 14 days / total users
      5. Streak survival: users with streak > 0 / total active users
    - Cache results (refresh every 15 min)

- [ ] 5. Implement Seasonal Events
  - [ ] 5.1 Create EventService
    - Create `EventService.java` in `admin/service/`
    - Create `seasonal_events` table (migration `V29__create_seasonal_events.sql`): id, title, description, start_date, end_date, active, rewards (JSONB), challenges (JSONB)
    - `createEvent(SeasonalEventRequest request)` — create new event
    - `getActiveEvents()` — return currently active events
    - `endEvent(UUID eventId)` — deactivate event

- [ ] 6. Create Admin Controller
  - [ ] 6.1 Implement REST endpoints (ADMIN role required)
    - Create `AdminController.java` in `admin/controller/`
    - All endpoints require @RequireRole(ADMIN) or @PreAuthorize("hasRole('ADMIN')")
    - POST `/api/v1/admin/arcs` — create arc
    - PUT `/api/v1/admin/arcs/{id}` — update arc
    - POST `/api/v1/admin/quests` — create quest
    - POST `/api/v1/admin/bosses` — create boss
    - GET `/api/v1/admin/users?flagged=true` — list flagged users
    - POST `/api/v1/admin/moderation` — moderate user
    - GET `/api/v1/admin/analytics` — system analytics
    - POST `/api/v1/admin/events` — create seasonal event

- [ ] 7. Checkpoint - Verify admin panel
  - Integration test: admin creates arc → arc appears in catalog
  - Integration test: admin bans user → user cannot login
  - Integration test: non-admin user → 403 on admin endpoints
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Admin panel is API-only (frontend can be a separate Angular admin app or same app with admin routes)
- All admin actions are logged for audit trail
- System analytics are cached to avoid expensive queries on every request
- Seasonal events use JSONB for flexible reward/challenge definitions
- Moderation actions are reversible (unsuspend, unban)
