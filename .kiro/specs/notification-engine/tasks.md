# Implementation Plan: Notification Engine

## Overview

Firebase Cloud Messaging integration for push notifications with daily cap enforcement, optimal timing, streak alerts, and recovery mode awareness.

## Tasks

- [ ] 1. Create Notification data models
  - [ ] 1.1 Create DTOs and enums
    - Create `NotificationType.java` enum: QUEST_REMINDER, STREAK_WARNING, REWARD_ALERT, GUILD_REMINDER, LEVEL_UP, BOSS_PROGRESS, ACHIEVEMENT
    - Create `NotificationResponse.java`: id, type, title, message, sentAt, readAt, read (boolean)
    - Create `NotificationPreferences.java`: enabled, quietHoursStart, quietHoursEnd, types (map of type → enabled)
    - Add `fcm_token` and `notification_preferences` (JSONB) columns to users table (migration `V27__add_notification_fields.sql`)

- [ ] 2. Implement FCM Service
  - [ ] 2.1 Create FcmService
    - Create `FcmService.java` in `notification/service/`
    - `sendPushNotification(UUID userId, String title, String body, Map<String, String> data)`:
      1. Fetch user's FCM token
      2. Build Firebase Message with title, body, data payload
      3. Send via FirebaseMessaging.getInstance().send()
      4. Handle token expiry (mark invalid, don't retry)
    - `registerToken(UUID userId, String fcmToken)` — save/update FCM token
    - `removeToken(UUID userId)` — on logout

- [ ] 3. Implement Notification Service
  - [ ] 3.1 Create NotificationService with daily cap
    - Create `NotificationService.java` in `notification/service/`
    - `sendNotification(UUID userId, NotificationType type, String title, String message)`:
      1. Check daily cap: count today's notifications for user
      2. If count >= 5 → suppress (don't send)
      3. Check quiet hours (user preferences)
      4. Check recovery mode (reduce frequency if active)
      5. Send via FcmService
      6. Log to notifications_log table
    - `getNotifications(UUID userId, int page)` — paginated notification history
    - `markAsRead(UUID userId, UUID notificationId)` — update read_at

- [ ] 4. Implement Streak Alert Logic
  - [ ] 4.1 Create StreakAlertScheduler
    - Create `StreakAlertScheduler.java` in `notification/scheduler/`
    - Run every 15 minutes
    - For each user where daily reset is < 45 minutes away AND quests incomplete:
      1. Send high-priority streak warning
      2. Type: STREAK_WARNING
      3. Title: "⚡ Streak at risk!"
      4. Body: "You have X quests left. Complete them to keep your Y-day streak!"
    - Only send once per day (check notifications_log)

- [ ] 5. Create Notification Event Listeners
  - [ ] 5.1 Create event-driven notifications
    - Create `NotificationEventListener.java` in `notification/event/`
    - Listen for `LevelUpEvent` → send LEVEL_UP notification
    - Listen for `StreakMilestoneEvent` → send REWARD_ALERT
    - Listen for `BossDefeatedEvent` → send ACHIEVEMENT notification
    - Listen for `AchievementUnlockedEvent` → send ACHIEVEMENT notification
    - Listen for `GuildChallengeCompleteEvent` → send GUILD_REMINDER
    - Each listener calls NotificationService (which enforces cap)

- [ ] 6. Create Notification Controller
  - [ ] 6.1 Implement REST endpoints
    - Create `NotificationController.java` in `notification/controller/`
    - GET `/api/v1/notifications` — paginated notification history
    - POST `/api/v1/notifications/register-token` — register FCM token
    - PATCH `/api/v1/notifications/{id}/read` — mark as read
    - PUT `/api/v1/notifications/preferences` — update notification preferences

- [ ] 7. Write property-based tests
  - [ ] 7.1 Create notification property tests
    - Create `NotificationPropertyTest.java`:
      - Property 39: Daily cap never exceeded (max 5 per user per day)
    - Minimum 100 iterations per property

- [ ] 8. Checkpoint - Verify notification engine
  - Integration test: level up → notification sent → logged
  - Integration test: 6th notification in a day → suppressed
  - Integration test: streak warning sent 45 min before reset
  - Property tests all pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- **RULE: Do NOT run any mvn, gradle, npm, or test commands. Only create/edit files. No build or test verification steps.**

- Daily cap of 5 prevents notification fatigue
- Streak warnings are the highest priority notification
- Recovery mode reduces notification frequency
- FCM tokens can expire — handle gracefully
- Quiet hours respect user preferences

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1"] },
    { "id": 1, "tasks": ["2.1"] },
    { "id": 2, "tasks": ["3.1", "6.1"] },
    { "id": 3, "tasks": ["4.1", "5.1"] },
    { "id": 4, "tasks": ["7.1"] }
  ]
}
```
