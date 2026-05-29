# Implementation Plan: XP Engine

## Overview

XP calculation, award, daily cap enforcement, level-up detection, prestige system, and XP history logging. All XP is calculated server-side — client values are never trusted.

## Tasks

- [x] 1. Create XP calculation pure functions
  - [x] 1.1 Create XpCalculator
    - Create `XpCalculator.java` in `xp/service/`
    - Pure static method: `calculateFinalXp(int baseXp, QuestDifficulty difficulty, double streakMultiplier, double arcMultiplier, int bonusXp)`
    - Formula: FinalXP = BaseXP × DifficultyMultiplier × StreakMultiplier × ArcMultiplier + BonusXP
    - Difficulty multipliers: EASY=1.0, MEDIUM=1.5, HARD=2.0, LEGENDARY=3.0
    - All inputs validated (no negatives, multipliers >= 1.0)
    - Returns integer (floor)
  - [x] 1.2 Create ComboCalculator
    - Create `ComboCalculator.java` in `xp/service/`
    - Pure static method: `calculateComboMultiplier(int streakDays)`
    - Formula: min(1 + 0.01 × streakDays, 2.0)
    - Input: non-negative integer
    - Output: double in range [1.0, 2.0]
  - [x] 1.3 Create LevelCalculator
    - Create `LevelCalculator.java` in `xp/service/`
    - Pure static method: `xpRequiredForLevel(int level)` — returns floor(100 × level^1.5)
    - Pure static method: `calculateLevel(long totalXp)` — returns highest level where cumulative XP requirement <= totalXp
    - Pure static method: `xpToNextLevel(int currentLevel, long totalXp)` — returns remaining XP needed

- [x] 2. Implement XP award service
  - [x] 2.1 Create XpService
    - Create `XpService.java` in `xp/service/`
    - `awardXp(UUID userId, QuestCompletedEvent event)`:
      1. Fetch user's current streak (for combo multiplier)
      2. Fetch user's active arc multiplier (if any)
      3. Fetch user's skill buffs for the stat type
      4. Calculate final XP via XpCalculator
      5. Check daily cap — reduce award if it would exceed cap
      6. Update user's total XP
      7. Log to xp_history
      8. Check for level-up
      9. Publish XpAwardedEvent
    - `getDailyXpEarned(UUID userId)` — sum today's xp_history for cap checking
    - `getDailyCap(int level)` — returns 1000 + (level × 20)
  - [x] 2.2 Create XpAwardedEvent
    - Create `XpAwardedEvent.java` in `xp/event/`
    - Fields: userId, xpAmount, newTotalXp, newLevel, statType, source
    - Published after every XP award for leaderboard and analytics consumption
  - [x] 2.3 Create QuestCompletedEvent listener
    - Create `XpEventListener.java` in `xp/event/`
    - Listen for `QuestCompletedEvent`
    - Call `XpService.awardXp()` with event data
    - Handle errors gracefully (log and continue, don't break the event chain)

- [x] 3. Implement Level-up system
  - [x] 3.1 Create LevelService
    - Create `LevelService.java` in `xp/service/`
    - `checkAndProcessLevelUp(UUID userId, long newTotalXp, int currentLevel)`:
      1. Calculate new level from total XP
      2. If new level > current level, update user record
      3. Publish `LevelUpEvent` for each level gained
      4. Determine unlocks: Level 10 → Leagues, Level 25 → Guilds, Level 100 → Prestige
      5. Return level-up info (new level, rewards)
  - [x] 3.2 Create LevelUpEvent
    - Create `LevelUpEvent.java` in `xp/event/`
    - Fields: userId, previousLevel, newLevel, unlockedFeatures (list)
    - Consumed by Notification, Achievement, and League modules

- [x] 4. Implement Perfect Day bonus
  - [x] 4.1 Create PerfectDayService
    - Create `PerfectDayService.java` in `xp/service/`
    - `checkPerfectDay(UUID userId)`:
      1. Count user's assigned daily missions for today
      2. Count user's completed daily missions for today
      3. If all completed → award 100 bonus XP + trigger chest unlock
      4. Publish `PerfectDayEvent`
    - Called after each quest completion to check if it was the last one

- [x] 5. Implement Prestige system
  - [x] 5.1 Create PrestigeService
    - Create `PrestigeService.java` in `xp/service/`
    - `prestige(UUID userId)`:
      1. Verify user is Level 100+
      2. Reset level to 1, reset XP to 0
      3. Increment prestige_level (add column via migration `V22__add_prestige.sql`)
      4. Award prestige badge
      5. Future XP formula: BaseXP × (1 + 0.1 × PrestigeLevel)
    - `getPrestigeMultiplier(int prestigeLevel)` — returns 1 + 0.1 × prestigeLevel

- [x] 6. Implement XP history and stats endpoints
  - [x] 6.1 Create XP DTOs and controller
    - Create `XpSummaryResponse.java`: totalXp, level, xpToNextLevel, dailyXpEarned, dailyCap, prestigeLevel, comboMultiplier
    - Create `XpHistoryResponse.java`: list of transactions with source, amount, multiplier, stat, timestamp
    - Create `XpController.java` in `xp/controller/`
    - GET `/api/v1/xp/summary` — returns XP summary for authenticated user
    - GET `/api/v1/xp/history` — returns paginated XP history

- [x] 7. Implement stat gain on quest completion
  - [x] 7.1 Create StatService
    - Create `StatService.java` in `user/service/`
    - `awardStatPoints(UUID userId, StatType statType, QuestDifficulty difficulty)`:
      1. Calculate stat gain: BaseStat(1) × DifficultyMultiplier
      2. Update user_stats for the relevant stat
      3. Check for identity title unlocks (stat thresholds)
      4. Recalculate life_score
    - `getLifeScore(UserStats stats)` — formula: 0.25×Discipline + 0.2×Focus + 0.2×Vitality + 0.2×Wisdom + 0.15×(streak-based consistency)
  - [x] 7.2 Create stat gain event listener
    - Create `StatEventListener.java` in `user/event/`
    - Listen for `QuestCompletedEvent`
    - Call `StatService.awardStatPoints()` with event's statType and difficulty

- [x] 8. Write property-based tests for XP calculations
  - [x] 8.1 Add jqwik dependency and create XP property tests
    - Add `net.jqwik:jqwik` dependency to pom.xml (test scope)
    - Create `XpCalculatorPropertyTest.java`:
      - Property 1: FinalXP formula correctness for all valid inputs
      - Property 2: Combo multiplier always in [1.0, 2.0]
      - Property 3: Daily cap never exceeded
    - Create `LevelCalculatorPropertyTest.java`:
      - Property 7: Level formula correctness
      - Property 8: Prestige multiplier correctness
    - Minimum 100 iterations per property

- [-] 9. Checkpoint - Verify XP engine
  - Run property-based tests — all must pass
  - Integration test: complete quest → XP awarded → level up triggered
  - Integration test: daily cap enforcement (award XP up to cap, verify excess discarded)
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- XP calculations are pure functions — easy to test with property-based testing
- Server-side authority is non-negotiable — client XP values are always rejected
- Daily cap prevents grinding abuse while still rewarding consistent play
- Level-up events cascade to unlock features (leagues, guilds, prestige)
- Stat gains happen in parallel with XP awards (both listen to QuestCompletedEvent)
