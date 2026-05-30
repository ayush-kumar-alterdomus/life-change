# Implementation Plan: AI Coach

## Overview

Premium AI coaching system providing burnout detection, adaptive difficulty, personalized recommendations, and optimal quest timing. Uses behavioral analysis to keep users optimally challenged.

## Tasks

- [x] 1. Create AI Coach data models
  - [x] 1.1 Create tables and DTOs
    - Create `V33__create_ai_coach_tables.sql`:
      - user_behavior_metrics: id, user_id (FK UNIQUE), missed_quests_7d (INT), streak_breaks_30d (INT), declining_activity_score (DECIMAL), motivation_score (DECIMAL), burnout_risk (DECIMAL), recovery_mode_active (BOOLEAN), recovery_started_at (TIMESTAMP), updated_at (TIMESTAMP)
    - Create `CoachRecommendationResponse.java`: recommendations (list), burnoutRisk, recoveryModeActive, optimalQuestTime, difficultyAdjustment
    - Create `BurnoutRiskResponse.java`: riskScore, riskLevel (LOW/MEDIUM/HIGH/CRITICAL), factors (list)

- [x] 2. Implement Burnout Detection
  - [x] 2.1 Create BurnoutDetectionService
    - Create `BurnoutDetectionService.java` in `aicoach/service/`
    - `calculateBurnoutRisk(UUID userId)`:
      1. Fetch missed quests in last 7 days
      2. Fetch streak breaks in last 30 days
      3. Calculate declining activity (compare last 7 days vs previous 7 days)
      4. Calculate motivation score (completion rate × streak bonus)
      5. Formula: BurnoutRisk = (MissedQuests + StreakBreaks + DecliningActivity) / MotivationScore
      6. Normalize to 0-1 scale
    - `evaluateAndAct(UUID userId)`:
      1. Calculate risk
      2. If risk > 0.7 → activate Recovery Mode
      3. Update user_behavior_metrics
  - [x] 2.2 Create Recovery Mode
    - `activateRecoveryMode(UUID userId)`:
      1. Set recovery_mode_active = true
      2. Reduce daily quest count by 50%
      3. Lower quest difficulty by one level
      4. Enable recovery XP bonus (1.5x)
      5. Notify user with encouraging message
    - `deactivateRecoveryMode(UUID userId)`:
      1. Check if risk has dropped below 0.3
      2. Gradually restore normal difficulty
      3. Set recovery_mode_active = false

- [x] 3. Implement Adaptive Difficulty
  - [x] 3.1 Create AdaptiveDifficultyService
    - Create `AdaptiveDifficultyService.java` in `aicoach/service/`
    - `adjustDifficulty(UUID userId)`:
      1. Analyze completion rate over last 14 days
      2. If rate > 90% consistently → suggest harder quests
      3. If rate < 50% consistently → suggest easier quests
      4. Apply adjustment to next day's quest assignment
    - Run daily after streak evaluation

- [x] 4. Implement Recommendation Engine
  - [x] 4.1 Create AiCoachService
    - Create `AiCoachService.java` in `aicoach/service/`
    - `getRecommendations(UUID userId)`:
      1. Analyze quest completion patterns (time of day, day of week)
      2. Identify strongest and weakest stats
      3. Generate recommendations: "Focus on Wisdom quests this week", "Your best time is 7-8 AM"
      4. Suggest optimal quest timing based on historical patterns
    - `getOptimalQuestTime(UUID userId)`:
      1. Analyze completion timestamps
      2. Find peak activity hours
      3. Return suggested time window

- [x] 5. Create AI Coach Scheduler
  - [x] 5.1 Create daily evaluation scheduler
    - Create `AiCoachScheduler.java` in `aicoach/scheduler/`
    - Run daily for premium users
    - Call `evaluateAndAct()` for burnout detection
    - Call `adjustDifficulty()` for adaptive difficulty
    - Update behavior metrics

- [x] 6. Create AI Coach Controller
  - [x] 6.1 Implement REST endpoints (premium-gated)
    - Create `AiCoachController.java` in `aicoach/controller/`
    - GET `/api/v1/coach/recommendations` — personalized recommendations
    - GET `/api/v1/coach/burnout-risk` — current burnout risk assessment
    - GET `/api/v1/coach/optimal-time` — suggested quest timing
    - All endpoints require PREMIUM_USER role

- [x] 7. Write property-based tests
  - [x] 7.1 Create AI coach property tests
    - Create `BurnoutDetectionPropertyTest.java`:
      - Property 37: Burnout risk formula correctness (always >= 0)
      - Property 38: Recovery mode activates only when risk > threshold
    - Minimum 100 iterations per property

- [x] 8. Checkpoint - Verify AI coach
  - Integration test: high missed quests → burnout risk increases → recovery mode activates
  - Integration test: recovery mode reduces quest difficulty
  - Unit test: recommendations generated for various user patterns
  - Property tests all pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- **RULE: Do NOT run any mvn, gradle, npm, or test commands. Only create/edit files. No build or test verification steps.**

- AI Coach is premium-only — free users see teaser previews
- Burnout detection is rule-based for MVP (not ML)
- Recovery Mode is temporary — deactivates when risk drops
- Adaptive difficulty prevents both boredom and frustration
- Optimal timing is based on historical completion patterns

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1"] },
    { "id": 1, "tasks": ["2.1", "3.1", "4.1"] },
    { "id": 2, "tasks": ["2.2", "5.1", "6.1"] },
    { "id": 3, "tasks": ["7.1"] },
    { "id": 4, "tasks": [] }
  ]
}
```
