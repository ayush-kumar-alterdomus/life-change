# Implementation Plan: Arc Mode

## Overview

Guided growth journeys (Arcs) with phases, milestones, bosses, and adaptive difficulty. Arcs provide structured 30-90 day personal growth paths that drive daily quest assignment.

## Tasks

- [x] 1. Create Arc DTOs and enums
  > **Hint:** Only create files. Do not run any compile or test commands.
  - [x] 1.1 Create arc data models
    > **Hint:** Only create files. Do not run any compile or test commands.
    - Create `ArcResponse.java`: id, name, description, type, difficulty, durationDays, isPrebuilt, phases, milestoneCount
    - Create `ArcDetailResponse.java`: extends ArcResponse with milestones list, questFrequency, skillTreePath
    - Create `ArcProgressResponse.java`: arcId, arcName, progressPercent, currentPhase, startedAt, status, milestonesCompleted, totalMilestones
    - Create `CreateArcRequest.java`: title, goal, durationDays, milestones (list), questFrequency — with validation
    - Create `StartArcRequest.java`: arcId
    - Create `ArcPhase.java` enum: BEGINNER, INTERMEDIATE, ELITE, MASTER
    - Create `ArcStatus.java` enum: ACTIVE, COMPLETED, PAUSED, ABANDONED
    - Create `ArcType.java` enum: MONK, WARRIOR, SCHOLAR, CREATOR, BEAST_MODE, CUSTOM

- [x] 2. Implement Arc Service
  > **Hint:** Only create files. Do not run any compile or test commands.
  - [x] 2.1 Create ArcService for catalog and lifecycle
    > **Hint:** Only create files. Do not run any compile or test commands.
    - Create `ArcService.java` in `arc/service/`
    - `getAvailableArcs()` — return all prebuilt arcs + user's custom arcs
    - `getArcDetail(UUID arcId)` — return arc with milestones
    - `startArc(UUID userId, UUID arcId)`:
      1. Verify user doesn't have an active arc (or allow multiple?)
      2. Create user_arc_progress record (progress=0, phase=BEGINNER, status=ACTIVE)
      3. Assign arc-specific daily quests to user
      4. Return progress info
    - `abandonArc(UUID userId, UUID arcId)` — set status to ABANDONED
  - [x] 2.2 Create ArcProgressService
    > **Hint:** Only create files. Do not run any compile or test commands.
    - Create `ArcProgressService.java` in `arc/service/`
    - `completeMilestone(UUID userId, UUID arcId, UUID milestoneId)`:
      1. Verify milestone belongs to arc and user has active progress
      2. Mark milestone completed
      3. Recalculate progress_percent = (completed / total × 100)
      4. Award milestone XP
      5. Check phase transition (25% → INTERMEDIATE, 50% → ELITE, 75% → MASTER)
      6. Publish ArcPhaseCompleteEvent if phase changed
      7. If 100% → mark arc COMPLETED, award completion rewards
    - `getProgress(UUID userId, UUID arcId)` — return current progress

- [x] 3. Implement Arc Recommendation
  > **Hint:** Only create files. Do not run any compile or test commands.
  - [x] 3.1 Create ArcRecommendationEngine
    > **Hint:** Only create files. Do not run any compile or test commands.
    - Create `ArcRecommendationEngine.java` in `arc/service/`
    - `recommend(List<String> goals, Map<String, Object> assessmentAnswers, int availableMinutesPerDay)`:
      1. Map goals to Arc types (Fitness → WARRIOR, Learning → SCHOLAR, etc.)
      2. Filter arcs by difficulty matching assessment
      3. Score arcs by goal alignment
      4. Return top recommendation + alternatives
    - Simple rule-based matching (not ML) for MVP

- [x] 4. Implement Custom Arc creation
  > **Hint:** Only create files. Do not run any compile or test commands.
  - [x] 4.1 Create custom arc validation and persistence
    > **Hint:** Only create files. Do not run any compile or test commands.
    - Create `ArcValidator.java` in `arc/validator/`
    - Validate: title (required, max 100 chars), goal (required), duration (30-90 days), at least 1 milestone, questFrequency (required)
    - `createCustomArc(UUID userId, CreateArcRequest request)`:
      1. Validate request
      2. Create Arc record with is_prebuilt=false
      3. Create ArcMilestone records
      4. Return created arc
    - Premium check: free users limited to 1 custom arc, premium unlimited

- [x] 5. Implement Adaptive Difficulty
  > **Hint:** Only create files. Do not run any compile or test commands.
  - [x] 5.1 Create AdaptiveDifficultyService for arcs
    > **Hint:** Only create files. Do not run any compile or test commands.
    - Create `ArcAdaptiveDifficultyService.java` in `arc/service/`
    - `evaluatePerformance(UUID userId, UUID arcId)`:
      1. Calculate completion rate over last 7 days
      2. If rate < 50% → reduce quest difficulty by one level
      3. If rate > 90% → increase quest difficulty by one level
      4. Never fail the arc due to performance decline
    - Called weekly or on streak break within an arc
    - Publish `DifficultyAdjustedEvent`

- [x] 6. Create Arc Events
  > **Hint:** Only create files. Do not run any compile or test commands.
  - [x] 6.1 Create domain events
    > **Hint:** Only create files. Do not run any compile or test commands.
    - Create `ArcPhaseCompleteEvent.java`: userId, arcId, previousPhase, newPhase, progressPercent
    - Create `ArcCompletedEvent.java`: userId, arcId, arcName, totalDays, completionXp
    - Create `DifficultyAdjustedEvent.java`: userId, arcId, previousDifficulty, newDifficulty, reason

- [x] 7. Create Arc Controller
  > **Hint:** Only create files. Do not run any compile or test commands.
  - [x] 7.1 Implement REST endpoints
    > **Hint:** Only create files. Do not run any compile or test commands.
    - Create `ArcController.java` in `arc/controller/`
    - GET `/api/v1/arcs` — list available arcs
    - GET `/api/v1/arcs/{id}` — arc detail with milestones
    - POST `/api/v1/arcs/start` — start an arc
    - GET `/api/v1/arcs/progress` — current arc progress
    - PATCH `/api/v1/arcs/progress` — update progress (milestone completion)
    - POST `/api/v1/arcs` — create custom arc (validated)
    - POST `/api/v1/arcs/recommend` — get arc recommendation

- [x] 8. Checkpoint - Verify arc mode
  > **Hint:** Only create files. Do not run any compile or test commands.
  - Integration test: start arc → complete milestones → phase transitions → arc completion
  - Unit test: ArcValidator rejects invalid custom arcs
  - Unit test: ArcRecommendationEngine returns valid arc for any goal combination
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- **RULE: Do NOT run any mvn, gradle, npm, or test commands. Only create/edit files. No build or test verification steps.**

- Arcs drive the daily quest assignment — starting an arc changes what quests appear
- Phase transitions are automatic based on milestone completion percentage
- Adaptive difficulty prevents user frustration without failing the arc
- Arc recommendation is rule-based for MVP, can be enhanced with ML later
- Custom arc limits prevent abuse (free: 1, premium: unlimited)
