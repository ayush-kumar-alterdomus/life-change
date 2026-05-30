# Implementation Plan: Reward Economy

## Overview

In-game currency system (Coins and Gems), loot chests with tiered drop rates, achievements, cosmetics, and anti-inflation mechanics.

## Tasks

- [x] 1. Create Reward Economy data models
  - [x] 1.1 Create tables migration
    - Create `V31__create_reward_tables.sql`:
      - user_currency: id (UUID PK), user_id (UUID FK UNIQUE), coins (BIGINT default 0), gems (BIGINT default 0), updated_at (TIMESTAMP)
      - cosmetics: id (UUID PK), name (VARCHAR), type (VARCHAR — avatar, frame, aura, title, animation), rarity (VARCHAR), description (TEXT), gem_cost (INT nullable), coin_cost (INT nullable)
      - user_cosmetics: id (UUID PK), user_id (UUID FK), cosmetic_id (UUID FK), unlocked_at (TIMESTAMP) — UNIQUE(user_id, cosmetic_id)
      - loot_chests: id (UUID PK), user_id (UUID FK), tier (VARCHAR), source (VARCHAR), opened (BOOLEAN default false), contents (JSONB), earned_at (TIMESTAMP), opened_at (TIMESTAMP)
  - [x] 1.2 Create DTOs
    - Create `CurrencyResponse.java`: coins, gems
    - Create `CosmeticResponse.java`: id, name, type, rarity, owned, equipped
    - Create `LootChestResponse.java`: id, tier, source, opened, contents
    - Create `ChestOpenResult.java`: items (list of CosmeticResponse), coinsEarned, gemsEarned
    - Create `AchievementResponse.java`: name, type, description, unlockedAt, badge

- [x] 2. Implement Currency Service
  - [x] 2.1 Create CurrencyService
    - Create `CurrencyService.java` in `reward/service/`
    - `getBalance(UUID userId)` — return coins and gems
    - `awardCoins(UUID userId, int amount, String source)`:
      1. Check daily coin cap (max 500 coins/day)
      2. Apply diminishing returns if same source repeated
      3. Update balance
    - `awardGems(UUID userId, int amount, String source)` — premium currency (from purchases)
    - `spendCoins(UUID userId, int amount)` — deduct with insufficient funds check
    - `spendGems(UUID userId, int amount)` — deduct with insufficient funds check

- [x] 3. Implement Loot Chest System
  - [x] 3.1 Create LootChestService
    - Create `LootChestService.java`
    - `awardChest(UUID userId, String tier, String source)`:
      1. Create loot_chests record (unopened)
      2. Notify user
    - `openChest(UUID userId, UUID chestId)`:
      1. Verify chest belongs to user and is unopened
      2. Generate contents using drop rate formula
      3. Award contents (cosmetics, coins, gems)
      4. Mark chest as opened
      5. Return ChestOpenResult
  - [x] 3.2 Create DropRateCalculator
    - Create `DropRateCalculator.java`
    - `calculateDropRates(String tier, double streakBonus, double eventMultiplier)`:
      - Formula: DropRate = BaseRate × StreakBonus × EventMultiplier
      - Tiers: Common (60%), Rare (25%), Epic (12%), Legendary (3%) — base rates
      - Adjust by streak bonus and event multiplier
      - Sum must equal 1.0
    - `rollLoot(Map<String, Double> rates)` — random selection based on rates

- [x] 4. Implement Achievement System
  - [x] 4.1 Create AchievementService
    - Create `AchievementService.java`
    - `checkAndUnlock(UUID userId, String achievementName, String type)`:
      1. Check if already unlocked (idempotent)
      2. If new → create Achievement record
      3. Publish AchievementUnlockedEvent
    - `getAchievements(UUID userId)` — list all earned achievements
    - Define achievement criteria (can be event-driven):
      - First quest completed, 7-day streak, Level 10, First boss defeated, etc.
  - [x] 4.2 Create achievement event listeners
    - Listen for various events and check achievement criteria:
      - QuestCompletedEvent → "First Quest", "100 Quests", "1000 Quests"
      - StreakMilestoneEvent → "Week Warrior", "Month Master"
      - LevelUpEvent → "Level 10", "Level 50", "Level 100"
      - BossDefeatedEvent → "Boss Slayer"

- [x] 5. Implement Anti-Inflation Mechanics
  - [x] 5.1 Create reward cap enforcement
    - Daily coin cap: 500 coins/day
    - Diminishing returns: Nth repetition of same action yields coins × (1 / N)
    - Track daily earnings in user_currency or separate daily_rewards table
    - Reset daily at user's local midnight

- [x] 6. Create Reward Controller
  - [x] 6.1 Implement REST endpoints
    - Create `RewardController.java`
    - GET `/api/v1/rewards/currency` — coin and gem balance
    - GET `/api/v1/rewards/cosmetics` — owned cosmetics
    - GET `/api/v1/rewards/shop` — available cosmetics for purchase
    - POST `/api/v1/rewards/shop/buy` — purchase cosmetic
    - GET `/api/v1/rewards/chests` — unopened chests
    - POST `/api/v1/rewards/chests/{id}/open` — open a chest
    - GET `/api/v1/rewards/achievements` — earned achievements

- [x] 7. Write property-based tests
  - [x] 7.1 Create reward property tests
    - Create `DropRatePropertyTest.java`:
      - Property 46: Drop rates sum to 1.0 for all valid inputs
    - Create `CurrencyPropertyTest.java`:
      - Property 47: Daily cap never exceeded, diminishing returns applied
    - Minimum 100 iterations per property

- [x] 8. Checkpoint - Verify reward economy
  - Integration test: complete quest → coins awarded → daily cap enforced
  - Integration test: earn chest → open → contents generated by drop rates
  - Integration test: achievement criteria met → achievement unlocked
  - Property tests all pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- **RULE: Do NOT run any mvn, gradle, npm, or test commands. Only create/edit files. No build or test verification steps.**

- Coins are earned through gameplay, Gems are purchased (premium currency)
- Anti-inflation prevents economy devaluation over time
- Loot chest contents are determined at open time, not earn time
- Achievements are permanent and never revoked
- Cosmetics are visual only — no gameplay advantage
