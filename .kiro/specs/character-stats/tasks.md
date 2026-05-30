# Implementation Plan: Character Stats

## Overview

Six RPG character stats (Strength, Wisdom, Focus, Discipline, Vitality, Charisma) that grow from quest completions. Includes stat decay in Hard Mode, identity title unlocks at thresholds, and Life Score calculation.

## Tasks

- [ ] 1. Create Stats DTOs and constants
  - [ ] 1.1 Create stat data models
    - Create `UserStatsResponse.java`: strength, wisdom, focus, discipline, vitality, charisma, lifeScore, titles (list)
    - Create `StatGainResponse.java`: statType, previousValue, newValue, gain, titleUnlocked (nullable)
    - Create `IdentityTitle.java` record: statType, threshold, titleName, description
    - Create `StatThresholds.java` constants class with title unlock thresholds:
      - 100: "The Beginner" (per stat)
      - 250: "The Dedicated" (per stat)
      - 500: "The Focused One" / "The Strong One" / etc.
      - 1000: "The Master" (per stat)

- [ ] 2. Implement Stat Service
  - [ ] 2.1 Create StatService (if not already in xp-engine)
    - Create or update `StatService.java` in `user/service/`
    - `awardStatPoints(UUID userId, StatType statType, QuestDifficulty difficulty)`:
      1. Calculate gain: BaseStat(1) × DifficultyMultiplier (EASY=1, MEDIUM=1.5, HARD=2, LEGENDARY=3)
      2. Fetch current user_stats
      3. Increment the relevant stat
      4. Check title unlock thresholds
      5. Recalculate life_score
      6. Save updated stats
      7. Return StatGainResponse
    - `getUserStats(UUID userId)` — return full stats with earned titles
  - [ ] 2.2 Create title unlock logic
    - `checkTitleUnlocks(UUID userId, StatType statType, int newValue)`:
      1. Compare newValue against thresholds
      2. If threshold crossed, check if title already unlocked (achievements table)
      3. If new title → create Achievement record, publish AchievementUnlockedEvent
      4. Titles are permanent — never revoked even if stat decreases

- [ ] 3. Implement Hard Mode stat decay
  - [ ] 3.1 Create StatDecayService
    - Create `StatDecayService.java` in `user/service/`
    - `evaluateStatDecay(UUID userId)`:
      1. Only applies if user.hard_mode = true
      2. Check quest completion history per stat type over last 7 days
      3. If no quests completed for a stat type in 7 days → apply decay (-5 points)
      4. Minimum stat value is 0 (never negative)
      5. Log decay event
    - Called by daily scheduler for Hard Mode users
  - [ ] 3.2 Create stat decay scheduler
    - Create `StatDecayScheduler.java` in `user/scheduler/`
    - Run daily for users with hard_mode = true
    - Call `evaluateStatDecay` for each Hard Mode user
    - Publish `StatDecayEvent` for notification

- [ ] 4. Implement Life Score calculation
  - [ ] 4.1 Create LifeScoreCalculator
    - Create `LifeScoreCalculator.java` in `user/service/`
    - Pure function: `calculate(int discipline, int focus, int vitality, int wisdom, double consistency)`
    - Formula: 0.25×Discipline + 0.2×Focus + 0.2×Vitality + 0.2×Wisdom + 0.15×Consistency
    - Normalize to 0-100 scale based on max possible values
    - Consistency derived from streak data (current_streak / 30, capped at 1.0)

- [ ] 5. Create Stats Controller
  - [ ] 5.1 Implement REST endpoints
    - Create `StatsController.java` in `user/controller/` (or extend UserController)
    - GET `/api/v1/stats` — returns user's character stats and life score
    - GET `/api/v1/stats/titles` — returns all earned identity titles
    - GET `/api/v1/stats/radar` — returns stats formatted for radar chart display

- [ ] 6. Write property-based tests
  - [ ] 6.1 Create stat property tests
    - Create `StatServicePropertyTest.java`:
      - Property 24: Stat gain = BaseStat × DifficultyMultiplier for all valid inputs
      - Property 25: Title unlock is permanent (once unlocked, always available)
    - Create `LifeScorePropertyTest.java`:
      - Property 48: Life Score formula correctness
      - Life Score always in [0, 100] range
    - Minimum 100 iterations per property

- [ ] 7. Checkpoint - Verify character stats
  - Integration test: complete quest → stat increases by correct amount
  - Integration test: stat reaches threshold → title unlocked
  - Integration test: Hard Mode decay applies after 7 days inactivity
  - Property tests all pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Stats are the RPG visualization of real-life progress
- Stat gains happen via QuestCompletedEvent listener (parallel to XP award)
- Hard Mode decay is optional and only for users who explicitly enable it
- Life Score is a composite metric for the analytics dashboard
- Identity titles are achievements — permanent once earned

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1"] },
    { "id": 1, "tasks": ["2.1", "2.2"] },
    { "id": 2, "tasks": ["3.1", "4.1"] },
    { "id": 3, "tasks": ["3.2", "5.1"] },
    { "id": 4, "tasks": ["6.1"] },
    { "id": 5, "tasks": ["7"] }
  ]
}
```
