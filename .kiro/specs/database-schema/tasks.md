# Implementation Plan: Database Schema

## Overview

PostgreSQL schema setup for the Ascend platform using Flyway migrations, JPA entities, and Spring Data repositories. Covers all 18 tables, constraints, indexes, and development seed data.

## Tasks

- [x] 1. Configure Flyway migrations
  - [x] 1.1 Add Flyway dependency and configuration
    - Add `flyway-core` and `flyway-database-postgresql` dependencies to `pom.xml`
    - Configure Flyway in `application.yml` (locations: classpath:db/migration, baseline-on-migrate: true)
    - Create `src/main/resources/db/migration/` directory
    - Set JPA ddl-auto to `validate` (Flyway manages schema)
  - [x] 1.2 Create initial migration with UUID extension
    - Create `V1__init_extensions.sql` enabling `uuid-ossp` extension
    - Verify migration runs on application startup

- [x] 2. Create core user and quest tables
  - [x] 2.1 Create users table migration
    - Create `V2__create_users_table.sql`
    - Columns: id (UUID PK default uuid_generate_v4()), firebase_uid (VARCHAR UNIQUE NOT NULL), username (VARCHAR UNIQUE), email (VARCHAR UNIQUE), avatar_url (TEXT), level (INT default 1), xp (BIGINT default 0), league (VARCHAR default 'BRONZE'), premium (BOOLEAN default false), hard_mode (BOOLEAN default false), timezone (VARCHAR default 'UTC'), created_at (TIMESTAMP default now()), updated_at (TIMESTAMP default now())
  - [x] 2.2 Create quests table migration
    - Create `V3__create_quests_table.sql`
    - Columns: id (UUID PK), title (VARCHAR NOT NULL), description (TEXT), difficulty (VARCHAR NOT NULL), xp_reward (INT NOT NULL), stat_type (VARCHAR NOT NULL), frequency (VARCHAR NOT NULL), recurring (BOOLEAN default false), arc_id (UUID FK nullable), created_by (UUID FK nullable), is_custom (BOOLEAN default false), created_at (TIMESTAMP default now())
  - [x] 2.3 Create quest_completion table migration
    - Create `V4__create_quest_completion_table.sql`
    - Columns: id (UUID PK), user_id (UUID FK NOT NULL), quest_id (UUID FK NOT NULL), completed_at (TIMESTAMP NOT NULL default now()), xp_earned (INT NOT NULL), multiplier (DECIMAL), difficulty_at_completion (VARCHAR)
    - Add UNIQUE constraint on (user_id, quest_id, completed_at::date)
  - [x] 2.4 Create xp_history table migration
    - Create `V5__create_xp_history_table.sql`
    - Columns: id (UUID PK), user_id (UUID FK NOT NULL), source_type (VARCHAR NOT NULL), source_id (UUID), xp_amount (INT NOT NULL), multiplier (DECIMAL), stat_type (VARCHAR), created_at (TIMESTAMP default now())

- [x] 3. Create progression tables
  - [x] 3.1 Create streaks table migration
    - Create `V6__create_streaks_table.sql`
    - Columns: id (UUID PK), user_id (UUID FK UNIQUE NOT NULL), current_streak (INT default 0), longest_streak (INT default 0), combo_multiplier (DECIMAL default 1.0), last_completed_at (TIMESTAMP), shield_available (BOOLEAN default false), shield_used_at (TIMESTAMP), updated_at (TIMESTAMP default now())
  - [x] 3.2 Create user_stats table migration
    - Create `V7__create_user_stats_table.sql`
    - Columns: id (UUID PK), user_id (UUID FK UNIQUE NOT NULL), strength (INT default 0), wisdom (INT default 0), focus (INT default 0), discipline (INT default 0), vitality (INT default 0), charisma (INT default 0), life_score (DECIMAL default 0), updated_at (TIMESTAMP default now())
  - [x] 3.3 Create arcs and arc_milestones tables migration
    - Create `V8__create_arcs_tables.sql`
    - arcs: id (UUID PK), name (VARCHAR NOT NULL), description (TEXT), type (VARCHAR), difficulty (VARCHAR), duration_days (INT NOT NULL), is_prebuilt (BOOLEAN default false), created_at (TIMESTAMP default now())
    - arc_milestones: id (UUID PK), arc_id (UUID FK NOT NULL), title (VARCHAR NOT NULL), description (TEXT), order_index (INT NOT NULL), xp_reward (INT NOT NULL)
  - [x] 3.4 Create user_arc_progress table migration
    - Create `V9__create_user_arc_progress_table.sql`
    - Columns: id (UUID PK), user_id (UUID FK NOT NULL), arc_id (UUID FK NOT NULL), progress_percent (INT default 0), current_phase (INT default 1), started_at (TIMESTAMP default now()), completed_at (TIMESTAMP), status (VARCHAR default 'ACTIVE')
    - Add UNIQUE constraint on (user_id, arc_id)
  - [x] 3.5 Create user_skills table migration
    - Create `V10__create_user_skills_table.sql`
    - Columns: id (UUID PK), user_id (UUID FK NOT NULL), skill_id (UUID NOT NULL), skill_name (VARCHAR NOT NULL), arc_id (UUID FK), unlocked (BOOLEAN default false), unlocked_at (TIMESTAMP)
    - Add UNIQUE constraint on (user_id, skill_id)

- [x] 4. Create social and competition tables
  - [x] 4.1 Create guilds and guild_members tables migration
    - Create `V11__create_guilds_tables.sql`
    - guilds: id (UUID PK), name (VARCHAR UNIQUE NOT NULL), description (TEXT), owner_id (UUID FK NOT NULL), type (VARCHAR default 'PUBLIC'), max_members (INT default 10), guild_xp (BIGINT default 0), created_at (TIMESTAMP default now())
    - guild_members: id (UUID PK), guild_id (UUID FK NOT NULL), user_id (UUID FK NOT NULL), role (VARCHAR default 'MEMBER'), joined_at (TIMESTAMP default now())
    - Add UNIQUE constraint on (guild_id, user_id)
  - [x] 4.2 Create guild_challenges table migration
    - Create `V12__create_guild_challenges_table.sql`
    - Columns: id (UUID PK), guild_id (UUID FK NOT NULL), title (VARCHAR NOT NULL), target (INT NOT NULL), current_progress (INT default 0), created_at (TIMESTAMP default now()), ends_at (TIMESTAMP)
  - [x] 4.3 Create leaderboard table migration
    - Create `V13__create_leaderboard_table.sql`
    - Columns: id (UUID PK), user_id (UUID FK UNIQUE NOT NULL), weekly_xp (BIGINT default 0), weekly_rank (INT), global_rank (INT), league (VARCHAR default 'BRONZE'), consistency_score (DECIMAL default 0), season_id (VARCHAR), updated_at (TIMESTAMP default now())
  - [x] 4.4 Create boss_progress table migration
    - Create `V14__create_boss_progress_table.sql`
    - Columns: id (UUID PK), user_id (UUID FK NOT NULL), boss_id (UUID NOT NULL), boss_name (VARCHAR NOT NULL), current_stage (INT default 1), progress_percent (INT default 0), defeated (BOOLEAN default false), defeated_at (TIMESTAMP)
    - Add UNIQUE constraint on (user_id, boss_id)

- [x] 5. Create support tables
  - [x] 5.1 Create achievements table migration
    - Create `V15__create_achievements_table.sql`
    - Columns: id (UUID PK), user_id (UUID FK NOT NULL), achievement_name (VARCHAR NOT NULL), achievement_type (VARCHAR NOT NULL), description (TEXT), unlocked_at (TIMESTAMP default now())
  - [x] 5.2 Create subscriptions table migration
    - Create `V16__create_subscriptions_table.sql`
    - Columns: id (UUID PK), user_id (UUID FK UNIQUE NOT NULL), provider (VARCHAR), plan_type (VARCHAR default 'FREE'), premium (BOOLEAN default false), started_at (TIMESTAMP), expires_at (TIMESTAMP), auto_renew (BOOLEAN default false)
  - [x] 5.3 Create notifications_log table migration
    - Create `V17__create_notifications_log_table.sql`
    - Columns: id (UUID PK), user_id (UUID FK NOT NULL), type (VARCHAR NOT NULL), title (VARCHAR NOT NULL), message (TEXT), sent_at (TIMESTAMP default now()), read_at (TIMESTAMP)

- [x] 6. Create indexes migration
  - [x] 6.1 Create performance indexes
    - Create `V18__create_indexes.sql`
    - idx_users_firebase_uid ON users(firebase_uid)
    - idx_users_level_xp ON users(level DESC, xp DESC)
    - idx_quest_completion_user ON quest_completion(user_id)
    - idx_quest_completion_date ON quest_completion(completed_at)
    - idx_leaderboard_league ON leaderboard(league)
    - idx_leaderboard_rank ON leaderboard(weekly_rank)
    - idx_leaderboard_xp ON leaderboard(weekly_xp DESC)
    - idx_streaks_user ON streaks(user_id)
    - idx_xp_history_user ON xp_history(user_id)
    - idx_xp_history_date ON xp_history(created_at)
    - idx_guild_members_user ON guild_members(user_id)
    - idx_guild_members_guild ON guild_members(guild_id)

- [x] 7. Create JPA entities
  - [x] 7.1 Create core entities (User, Quest, QuestCompletion, XpHistory)
    - Create `User.java` entity in `user/entity/` with all columns, @Entity, @Table, @Id with UUID generation
    - Create `Quest.java` entity in `quest/entity/`
    - Create `QuestCompletion.java` entity in `quest/entity/` with unique constraint annotation
    - Create `XpHistory.java` entity in `xp/entity/`
    - Use Lombok @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor
    - Add @CreationTimestamp and @UpdateTimestamp where applicable
  - [x] 7.2 Create progression entities (Streak, UserStats, Arc, ArcMilestone, UserArcProgress, UserSkill)
    - Create `Streak.java` entity in `streak/entity/`
    - Create `UserStats.java` entity in `user/entity/`
    - Create `Arc.java` entity in `arc/entity/`
    - Create `ArcMilestone.java` entity in `arc/entity/`
    - Create `UserArcProgress.java` entity in `arc/entity/`
    - Create `UserSkill.java` entity in `skilltree/entity/`
  - [x] 7.3 Create social entities (Guild, GuildMember, GuildChallenge, Leaderboard, BossProgress)
    - Create `Guild.java` entity in `guild/entity/`
    - Create `GuildMember.java` entity in `guild/entity/`
    - Create `GuildChallenge.java` entity in `guild/entity/`
    - Create `Leaderboard.java` entity in `league/entity/`
    - Create `BossProgress.java` entity in `boss/entity/`
  - [x] 7.4 Create support entities (Achievement, Subscription, NotificationLog)
    - Create `Achievement.java` entity in `analytics/entity/`
    - Create `Subscription.java` entity in `premium/entity/`
    - Create `NotificationLog.java` entity in `notification/entity/`

- [x] 8. Create Spring Data JPA repositories
  - [x] 8.1 Create core repositories
    - Create `UserRepository.java` with findByFirebaseUid, findByEmail, findByUsername
    - Create `QuestRepository.java` with findByArcId, findByCreatedBy, findByRecurringTrue
    - Create `QuestCompletionRepository.java` with findByUserIdAndCompletedAtBetween, existsByUserIdAndQuestIdAndCompletedAtBetween
    - Create `XpHistoryRepository.java` with findByUserIdOrderByCreatedAtDesc, sumXpAmountByUserIdAndCreatedAtBetween
  - [x] 8.2 Create progression repositories
    - Create `StreakRepository.java` with findByUserId
    - Create `UserStatsRepository.java` with findByUserId
    - Create `ArcRepository.java` with findByIsPrebuiltTrue
    - Create `ArcMilestoneRepository.java` with findByArcIdOrderByOrderIndex
    - Create `UserArcProgressRepository.java` with findByUserIdAndStatus, findByUserIdAndArcId
    - Create `UserSkillRepository.java` with findByUserIdAndArcId, findByUserIdAndUnlockedTrue
  - [x] 8.3 Create social repositories
    - Create `GuildRepository.java` with findByOwnerId, findByType
    - Create `GuildMemberRepository.java` with findByGuildId, findByUserId, countByGuildId
    - Create `GuildChallengeRepository.java` with findByGuildIdAndEndsAtAfter
    - Create `LeaderboardRepository.java` with findByLeagueOrderByWeeklyXpDesc, findByUserId
    - Create `BossProgressRepository.java` with findByUserIdAndDefeatedFalse, findByUserIdAndBossId
  - [x] 8.4 Create support repositories
    - Create `AchievementRepository.java` with findByUserId, existsByUserIdAndAchievementName
    - Create `SubscriptionRepository.java` with findByUserId
    - Create `NotificationLogRepository.java` with findByUserIdOrderBySentAtDesc, countByUserIdAndSentAtBetween

- [x] 9. Create development seed data
  - [x] 9.1 Create seed data migration
    - Create `V99__seed_dev_data.sql` (only runs in dev profile via Flyway config)
    - Insert 5 prebuilt Arcs: Monk, Warrior, Scholar, Creator, Beast Mode with descriptions and durations
    - Insert 3-5 milestones per Arc
    - Insert 20+ sample quests across all stat types and difficulties
    - Insert 3 sample bosses with stages
    - Insert sample skill tree nodes for each Arc

- [x] 10. Checkpoint - Verify database schema
  - Run `./mvnw spring-boot:run` and verify Flyway migrations execute successfully
  - Verify all tables are created with correct constraints
  - Verify JPA entity validation passes (ddl-auto=validate)
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- **RULE: Do NOT run any mvn, gradle, npm, or test commands. Only create/edit files. No build or test verification steps.**

- All UUIDs use PostgreSQL's uuid_generate_v4() for default generation
- Flyway migrations are numbered sequentially (V1 through V18, V99 for seed)
- Seed data (V99) should be conditionally applied only in dev/test environments
- JPA entities use Lombok for boilerplate reduction
- Repositories extend JpaRepository<Entity, UUID> for standard CRUD
