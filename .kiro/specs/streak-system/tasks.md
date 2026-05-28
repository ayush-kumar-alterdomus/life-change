# Implementation Plan: Streak System

## Overview

Daily streak tracking, combo multiplier calculation, streak shields, comeback mode, and streak-related notifications. Streaks are the primary retention mechanic — they reward daily consistency with increasing XP multipliers.

## Tasks

- [ ] 1. Create Streak DTOs and enums
  - [ ] 1.1 Create streak response DTOs
    - Create `StreakResponse.java`: currentStreak, longestStreak, comboMultiplier, shieldAvailable, lastCompletedAt, comebackModeActive, comebackExpiresAt
    - Create `StreakStatus.java` enum: ACTIVE, BROKEN, COMEBACK_MODE, SHIELDED
    - Create `StreakMilestone.java` enum: WEEK(7), TWO_WEEKS(14), MONTH(30), QUARTER(90), YEAR(365) with XP bonus values

- [ ] 2. Implement Streak Service
  - [ ] 2.1 Create StreakService core logic
    - Create `StreakService.java` in `streak/service/`
    - `getStreak(UUID userId)` — fetch current streak data
    - `evaluateDailyStreak(UUID userId)`:
      1. Count user's assigned daily quests
      2. Count user's completed daily quests
      3. If completed >= 80% of assigned → increment streak
      4. If completed < 80% → check shield → break or protect
      5. Update combo_multiplier
      6. Update longest_streak if current > longest
    - `incrementStreak(UUID userId)`:
      1. Increment current_streak
      2. Recalculate combo_multiplier: min(1 + 0.01 × currentStreak, 2.0)
      3. Update last_completed_at
      4. Check for streak milestones (7, 14, 30, 90, 365)
      5. Publish StreakMilestoneEvent if milestone reached
  - [ ] 2.2 Create streak break logic
    - `breakStreak(UUID userId)`:
      1. Check if shield_available = true
      2. If shield available → activate shield, preserve streak, publish StreakShieldedEvent
      3. If no shield → reset current_streak to 0, activate Comeback Mode
      4. Publish StreakBrokenEvent
    - `activateShield(UUID userId)`:
      1. Set shield_available = false
      2. Set shield_used_at = now()
      3. Streak remains intact

- [ ] 3. Implement Comeback Mode
  - [ ] 3.1 Create ComebackModeService
    - Create `ComebackModeService.java` in `streak/service/`
    - Add `comeback_mode_active` (BOOLEAN) and `comeback_expires_at` (TIMESTAMP) columns to streaks table (migration `V23__add_comeback_mode.sql`)
    - `activateComebackMode(UUID userId)`:
      1. Set comeback_mode_active = true
      2. Set comeback_expires_at = now() + 48 hours
      3. Reduce assigned quest difficulty temporarily
      4. Enable recovery XP bonuses (1.5x for comeback quests)
    - `checkComebackExpiry(UUID userId)`:
      1. If now() > comeback_expires_at → deactivate comeback mode
      2. Reset to normal difficulty
    - `isComebackActive(UUID userId)` — check if within 48-hour window

- [ ] 4. Implement Streak Evaluation Scheduler
  - [ ] 4.1 Create StreakCalculationScheduler
    - Create `StreakCalculationScheduler.java` in `streak/scheduler/`
    - Triggered by `DailyResetEvent` from Quest module (per timezone)
    - For each user in the resetting timezone:
      1. Call `evaluateDailyStreak(userId)`
      2. Handle streak breaks and comebacks
    - Also run hourly to check comeback mode expiry
  - [ ] 4.2 Create streak event listener for quest completions
    - Create `StreakEventListener.java` in `streak/event/`
    - Listen for `QuestCompletedEvent`
    - Update real-time completion count for today (for UI progress display)
    - Do NOT evaluate streak here — that happens at daily reset

- [ ] 5. Create Streak Events
  - [ ] 5.1 Create domain events
    - Create `StreakMilestoneEvent.java`: userId, milestoneType, streakDays, bonusXp
    - Create `StreakBrokenEvent.java`: userId, previousStreak, comebackModeActivated
    - Create `StreakShieldedEvent.java`: userId, streakPreserved, shieldsRemaining
    - All consumed by Notification module for user alerts

- [ ] 6. Create Streak Controller
  - [ ] 6.1 Implement REST endpoints
    - Create `StreakController.java` in `streak/controller/`
    - GET `/api/v1/streak` — returns current streak info for authenticated user
    - GET `/api/v1/streak/history` — returns streak history (past 30 days)
    - POST `/api/v1/streak/shield/activate` — manually activate shield (if available)

- [ ] 7. Write property-based tests for streak logic
  - [ ] 7.1 Create streak property tests
    - Create `ComboCalculatorPropertyTest.java`:
      - Property 2: Combo multiplier always in [1.0, 2.0] for any non-negative streak
    - Create `StreakServicePropertyTest.java`:
      - Property 9: Streak tracking invariant (consecutive days >= 80%)
      - Property 10: Streak reset without shield
      - Property 12: Shield auto-activation preserves streak
    - Minimum 100 iterations per property

- [ ] 8. Checkpoint - Verify streak system
  - Integration test: complete 80%+ quests → streak increments
  - Integration test: complete < 80% without shield → streak resets, comeback activates
  - Integration test: complete < 80% with shield → shield consumed, streak preserved
  - Property tests all pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Streak evaluation happens at daily reset (timezone-aware), not on each quest completion
- Combo multiplier is recalculated on every streak change and cached in the streaks table
- Comeback Mode provides a 48-hour grace period with easier quests
- Streak shields are a premium feature (awarded via rewards or purchased)
- The 80% threshold means users don't need to complete every single quest to maintain streaks
