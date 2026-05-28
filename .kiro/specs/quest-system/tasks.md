# Implementation Plan: Quest System

## Overview

Quest CRUD operations, completion flow, daily reset scheduler, custom quest validation, and quest categorization. The quest system is the core gameplay loop — users complete quests to earn XP and build habits.

## Tasks

- [x] 1. Create Quest DTOs and validation
  - [x] 1.1 Create quest request/response DTOs
    - Create `QuestResponse.java` with fields: id, title, description, xpReward, difficulty, statType, frequency, recurring, isCustom, completed (boolean for daily status)
    - Create `CreateQuestRequest.java` with fields: title, description, difficulty, xpReward, statType, frequency — all with Jakarta validation annotations
    - Create `CompleteQuestRequest.java` with field: questId
    - Create `DailyQuestsResponse.java` wrapping list of QuestResponse with date and completion stats
  - [x] 1.2 Create quest enums
    - Create `QuestDifficulty.java` enum: EASY, MEDIUM, HARD, LEGENDARY
    - Create `QuestCategory.java` enum: MAIN_QUEST, SIDE_QUEST, DAILY_MISSION, WEEKLY_CHALLENGE, EPIC_QUEST
    - Create `StatType.java` enum: STRENGTH, WISDOM, FOCUS, DISCIPLINE, VITALITY, CHARISMA
    - Create `QuestFrequency.java` enum: DAILY, WEEKLY, ONE_TIME

- [x] 2. Implement Quest Service
  - [x] 2.1 Create QuestService for quest retrieval
    - Create `QuestService.java` in `quest/service/`
    - `getDailyQuests(UUID userId)` — return user's assigned quests for today with completion status
    - `getQuestById(UUID questId)` — fetch single quest
    - `getQuestsByArc(UUID arcId)` — fetch quests belonging to an arc
    - Join with quest_completion to determine if each quest is completed today
  - [x] 2.2 Create QuestValidator
    - Create `QuestValidator.java` in `quest/validator/`
    - Validate title: not blank, max 100 chars
    - Validate difficulty: must be valid enum value
    - Validate xpReward: range [10, 300] for custom quests
    - Validate statType: must be valid enum value
    - Validate frequency: must be valid enum value
    - Return structured validation errors

- [x] 3. Implement Quest Completion flow
  - [x] 3.1 Create QuestCompletionService
    - Create `QuestCompletionService.java` in `quest/service/`
    - `completeQuest(UUID userId, UUID questId)`:
      1. Verify quest exists
      2. Check idempotency — reject if (user_id, quest_id, today) already exists
      3. Create QuestCompletion record
      4. Publish `QuestCompletedEvent` with userId, questId, difficulty, statType, xpReward
      5. Return completion confirmation with XP earned
    - Handle `DuplicateCompletionException` gracefully (409 Conflict)
  - [x] 3.2 Create QuestCompletedEvent
    - Create `QuestCompletedEvent.java` in `quest/event/`
    - Fields: userId, questId, questTitle, difficulty, statType, baseXpReward, completedAt
    - Extends `ApplicationEvent`
    - This event is consumed by XP, Streak, Analytics, Boss, Achievement, and Notification modules

- [x] 4. Implement Custom Quest creation
  - [x] 4.1 Create custom quest endpoint
    - Add `createCustomQuest(UUID userId, CreateQuestRequest request)` to QuestService
    - Validate request via QuestValidator
    - Set `is_custom = true`, `created_by = userId`
    - Set default frequency if not provided
    - Persist and return created quest
  - [x] 4.2 Add quest limits for free users
    - Free users: max 5 custom quests
    - Premium users: unlimited custom quests
    - Check count before creation, throw `CustomQuestLimitException` if exceeded

- [x] 5. Implement Daily Reset Scheduler
  - [x] 5.1 Create QuestResetScheduler
    - Create `QuestResetScheduler.java` in `quest/scheduler/`
    - Run every hour (to handle multiple timezones)
    - For each timezone where it's now 00:00, find users in that timezone
    - Mark recurring daily quests as available (no action needed — absence of completion record = available)
    - Publish `DailyResetEvent` for streak evaluation
  - [x] 5.2 Create daily quest assignment logic
    - When daily reset occurs, ensure users have their assigned quests for the new day
    - For Arc users: assign Arc-specific daily quests
    - For non-Arc users: assign default daily missions based on their stat preferences

- [x] 6. Create QuestController
  - [x] 6.1 Implement REST endpoints
    - Create `QuestController.java` in `quest/controller/`
    - GET `/api/v1/quests/daily` — returns today's quests for authenticated user
    - POST `/api/v1/quests/complete` — completes a quest (body: questId)
    - POST `/api/v1/quests` — creates a custom quest
    - GET `/api/v1/quests/{id}` — get quest details
    - All endpoints require authentication
    - Apply rate limiting (20/min for completion)

- [-] 7. Checkpoint - Verify quest system
  - Write integration test: create quest → complete quest → verify completion record exists
  - Write integration test: attempt duplicate completion → verify 409 response
  - Write unit test: QuestValidator rejects invalid inputs
  - Write unit test: QuestCompletedEvent is published on completion
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Quest completion is the trigger for the entire game loop (XP, streaks, stats, bosses)
- Idempotency is critical — the UNIQUE(user_id, quest_id, date) constraint is the last line of defense
- Daily reset is timezone-aware — users in different timezones reset at their local midnight
- Custom quest XP is capped at 300 to prevent abuse
- The QuestCompletedEvent is the most important domain event in the system
