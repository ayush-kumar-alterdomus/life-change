# Implementation Plan: Boss Battles

## Overview

Multi-stage boss challenges requiring sustained effort over days/weeks. Includes individual and guild boss battles with legendary rewards on defeat.

## Tasks

- [x] 1. Create Boss data models
  - [x] 1.1 Create boss tables migration
    - Create `V25__create_boss_tables.sql`
    - bosses: id (UUID PK), name (VARCHAR NOT NULL), description (TEXT), total_stages (INT NOT NULL), stage_thresholds (JSONB — array of progress % per stage), reward_xp (INT NOT NULL), reward_title (VARCHAR), reward_cosmetic (VARCHAR), is_guild_boss (BOOLEAN default false), arc_id (UUID FK nullable)
    - guild_boss_progress: id (UUID PK), guild_id (UUID FK), boss_id (UUID FK), current_stage (INT default 1), progress_percent (INT default 0), defeated (BOOLEAN default false), defeated_at (TIMESTAMP) — UNIQUE(guild_id, boss_id)
  - [x] 1.2 Create DTOs
    - Create `BossResponse.java`: id, name, description, totalStages, currentStage, progressPercent, defeated, rewardXp, rewardTitle
    - Create `BossDetailResponse.java`: extends BossResponse with stageThresholds, contributors (for guild bosses)
    - Create `BossDefeatResponse.java`: bossName, xpAwarded, titleUnlocked, cosmeticUnlocked

- [x] 2. Implement Boss Service
  - [x] 2.1 Create BossService
    - Create `BossService.java` in `boss/service/`
    - `getUserBosses(UUID userId)` — list active and defeated bosses
    - `getBossDetail(UUID userId, UUID bossId)` — boss with progress
    - `contributeToBoss(UUID userId, UUID bossId, int damage)`:
      1. Fetch boss_progress for user
      2. Add damage to progress_percent
      3. Check stage thresholds — advance stage if threshold crossed
      4. If final stage at 100% → defeat boss
      5. Return updated progress
    - `defeatBoss(UUID userId, UUID bossId)`:
      1. Mark defeated = true, defeated_at = now()
      2. Award legendary XP (300-1000 based on boss)
      3. Unlock exclusive title and cosmetic
      4. Publish BossDefeatedEvent
  - [x] 2.2 Create BossProgressCalculator
    - Create `BossProgressCalculator.java` in `boss/service/`
    - `calculateDamage(QuestDifficulty difficulty, StatType statType, UUID bossId)`:
      - Map quest completion to boss damage based on difficulty
      - EASY=5%, MEDIUM=10%, HARD=15%, LEGENDARY=25% per stage
    - Called when QuestCompletedEvent fires and user has active boss

- [x] 3. Implement Guild Boss Battles
  - [x] 3.1 Create guild boss logic
    - `contributeToGuildBoss(UUID userId, UUID guildId, UUID bossId, int damage)`:
      1. Fetch guild_boss_progress
      2. Add damage (collective from all members)
      3. Check stage/defeat conditions
      4. If defeated → award rewards to all guild members
    - Listen for QuestCompletedEvent — if user's guild has active boss, contribute

- [x] 4. Create Boss Event Listener
  - [x] 4.1 Create event integration
    - Create `BossEventListener.java` in `boss/event/`
    - Listen for `QuestCompletedEvent`:
      1. Check if user has active (non-defeated) boss
      2. Calculate damage from quest
      3. Apply damage to boss progress
    - Create `BossDefeatedEvent.java`: userId, bossId, bossName, xpAwarded, titleUnlocked

- [x] 5. Create Boss Controller
  - [x] 5.1 Implement REST endpoints
    - Create `BossController.java` in `boss/controller/`
    - GET `/api/v1/boss` — list user's bosses (active + defeated)
    - GET `/api/v1/boss/{id}` — boss detail with progress
    - GET `/api/v1/boss/guild/{guildId}` — guild boss progress

- [x] 6. Write property-based tests
  - [x] 6.1 Create boss property tests
    - Create `BossPropertyTest.java`:
      - Property 32: Boss progress updates correctly, defeat at 100% final stage
      - Property 33: Guild boss collective progress aggregates all members
    - Minimum 100 iterations per property

- [x] 7. Checkpoint - Verify boss battles
  - Integration test: quest completions → boss damage → stage advance → defeat → rewards
  - Integration test: guild members contribute → collective progress → guild boss defeated
  - Property tests all pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- **RULE: Do NOT run any mvn, gradle, or test commands. Only create/edit files. No build or test verification steps.**
- Boss damage is derived from quest completions (passive — no separate "attack" action)
- Stage thresholds stored as JSONB for flexibility (e.g., [33, 66, 100] for 3 stages)
- Guild bosses aggregate damage from all members
- Legendary rewards include XP, titles, and cosmetics
- Bosses are tied to Arcs (optional) or standalone challenges

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.2"] },
    { "id": 1, "tasks": ["2.1", "2.2"] },
    { "id": 2, "tasks": ["3.1", "4.1"] },
    { "id": 3, "tasks": ["5.1"] },
    { "id": 4, "tasks": ["6.1"] },
    { "id": 5, "tasks": ["7"] }
  ]
}
```
