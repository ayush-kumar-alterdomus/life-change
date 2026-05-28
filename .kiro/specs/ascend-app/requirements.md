# Requirements Document

## Introduction

Ascend is a game-first self-improvement platform that transforms personal growth into an immersive RPG experience. Users complete quests, earn XP, level up character stats, compete in leagues, join guilds, and progress through personalized growth journeys called "Arcs." The platform combines gamification mechanics, behavioral psychology, and AI-driven coaching to maximize long-term retention and habit consistency.

The system consists of an Angular/Ionic mobile-first frontend (Android, iOS, Web), a Spring Boot modular monolith backend, Firebase for authentication and realtime state, and PostgreSQL for business logic, analytics, and rankings.

## Glossary

- **Ascend_System**: The complete Ascend platform including frontend, backend, and infrastructure
- **Auth_Service**: The authentication subsystem handling Firebase JWT validation and session management
- **Quest_Engine**: The subsystem responsible for quest creation, completion, validation, and reward distribution
- **XP_Engine**: The subsystem that calculates, validates, and awards experience points
- **Level_Engine**: The subsystem that manages player level progression and level-up rewards
- **Streak_Engine**: The subsystem tracking daily streak continuity, combo multipliers, and recovery
- **Arc_Engine**: The subsystem managing personalized growth journeys (Arcs), milestones, and phases
- **League_Engine**: The subsystem handling competitive rankings, matchmaking, and seasonal resets
- **Guild_Engine**: The subsystem managing guild creation, membership, shared quests, and chat
- **Boss_Engine**: The subsystem managing boss battle challenges, stages, and rewards
- **Skill_Tree_Engine**: The subsystem managing skill node unlocking and passive buff application
- **AI_Coach**: The premium subsystem providing adaptive difficulty, burnout detection, and personalized coaching
- **Notification_Engine**: The subsystem orchestrating push notifications via Firebase Cloud Messaging
- **Analytics_Engine**: The subsystem aggregating user data into reports, life scores, and insights
- **Premium_Service**: The subsystem managing subscription tiers, feature gating, and payment processing
- **Admin_Panel**: The administrative interface for content management, moderation, and system analytics
- **Anti_Cheat_System**: The subsystem detecting and preventing gameplay exploitation
- **Reward_Economy**: The subsystem managing currencies (coins, gems), loot chests, cosmetics, and achievements
- **User**: A registered or guest player of the Ascend platform
- **Quest**: A task or habit mapped to an RPG mission with XP rewards and stat gains
- **Arc**: A guided 30-90 day personal growth journey with phases, milestones, and bosses
- **XP**: Experience points earned through quest completion and other activities
- **Streak**: Consecutive days of meeting minimum quest completion threshold (80%)
- **Combo_Multiplier**: An XP bonus that increases with streak length (formula: 1 + 0.01 × StreakDays, cap 2x)
- **League**: A competitive tier (Bronze through Ascendant) with weekly rankings
- **Guild**: A social group of users completing shared quests and competing together
- **Boss**: A multi-stage challenge requiring sustained effort over days or weeks
- **Skill_Node**: A progression point in a skill tree that grants passive XP buffs
- **Life_Score**: A composite metric measuring overall personal growth across dimensions
- **Hard_Mode**: A premium mode with penalties for missed quests (XP deduction, stat decay)
- **Firestore**: Firebase's NoSQL realtime database used for lightweight state sync
- **PostgreSQL**: The relational database used for business logic, analytics, and complex queries

## Requirements

### Requirement 1: User Registration and Authentication

**User Story:** As a new user, I want to register and authenticate using multiple providers, so that I can start using Ascend with minimal friction.

#### Acceptance Criteria

1. WHEN a user selects Google Sign-In, THE Auth_Service SHALL authenticate the user via Firebase Google provider and return a valid JWT token within 3 seconds
2. WHEN a user selects Apple Sign-In, THE Auth_Service SHALL authenticate the user via Firebase Apple provider and return a valid JWT token within 3 seconds
3. WHEN a user submits email registration with a valid email and password meeting complexity requirements (minimum 8 characters, one uppercase, one special character), THE Auth_Service SHALL create a Firebase account and a corresponding user record in PostgreSQL
4. WHEN a user selects Guest Mode, THE Auth_Service SHALL create an anonymous Firebase session allowing access to onboarding, quests, and basic gameplay while restricting leaderboard, cloud sync, and guild features
5. IF a user attempts to register with an email that already exists, THEN THE Auth_Service SHALL return an error message "Account already exists" without revealing account details
6. WHEN a user successfully authenticates, THE Auth_Service SHALL persist the session via Firebase refresh token so that the user remains logged in across app restarts
7. WHEN the Spring Boot backend receives an API request, THE Auth_Service SHALL validate the Firebase JWT signature, expiration, and issuer claim before processing the request

### Requirement 2: Multi-Device Synchronization

**User Story:** As a user with multiple devices, I want my progress to sync across all devices, so that I can continue my journey from any device.

#### Acceptance Criteria

1. WHEN a user completes a quest on one device, THE Ascend_System SHALL reflect the updated XP, streak, and quest status on all other authenticated devices within 5 seconds
2. THE Ascend_System SHALL prevent duplicate XP awards when the same quest completion syncs from multiple devices
3. WHEN a user is offline, THE Ascend_System SHALL queue completed actions locally and sync them to the server when connectivity is restored
4. IF a sync conflict occurs between local and server state, THEN THE Ascend_System SHALL resolve the conflict using server-wins strategy while preserving locally queued completions

### Requirement 3: RPG Onboarding Flow

**User Story:** As a new user, I want an immersive onboarding experience, so that I feel engaged from the first interaction and receive a personalized Arc recommendation.

#### Acceptance Criteria

1. WHEN a new user launches the app for the first time, THE Ascend_System SHALL present a cinematic welcome screen with the text "Your strongest self awaits" and a call-to-action "Enter Arc Mode"
2. WHEN the user proceeds past the welcome screen, THE Ascend_System SHALL present goal selection options (Discipline, Fitness, Learning, Productivity, Confidence, Mental Health)
3. WHEN the user selects a goal, THE Ascend_System SHALL present difficulty selection (Casual Mode, Balanced Mode, Beast Mode) with clear descriptions of each intensity level
4. WHEN the user completes the personality assessment, THE Arc_Engine SHALL generate a personalized Arc recommendation based on goals, assessment answers, personality profile, and available time
5. THE Ascend_System SHALL allow the user to override the recommended Arc and select any available Arc manually

### Requirement 4: Quest System

**User Story:** As a user, I want to complete daily quests that feel like RPG missions, so that I earn XP and build real-life habits through engaging gameplay.

#### Acceptance Criteria

1. THE Quest_Engine SHALL categorize quests into Main Quests (high impact), Side Quests (optional), Daily Missions (recurring), Weekly Challenges (long-term), and Epic Quests (major milestones)
2. WHEN a user requests daily quests, THE Quest_Engine SHALL return the user's assigned quests for the current day including title, description, XP reward, difficulty, and stat type
3. WHEN a user completes a quest, THE Quest_Engine SHALL validate the completion on the server, award XP, update the relevant character stat, and update the streak counter
4. THE Quest_Engine SHALL enforce a unique constraint preventing the same quest from being completed more than once per day per user
5. WHEN a quest is completed, THE Ascend_System SHALL trigger an XP animation (particle burst, glow effect, progress bar fill) and haptic feedback
6. WHEN a user creates a custom quest, THE Quest_Engine SHALL validate that the quest has a title, difficulty, frequency, stat type, and XP reward within allowed ranges
7. WHEN the daily reset occurs at 00:00 user local time, THE Quest_Engine SHALL reset recurring daily quests to incomplete status

### Requirement 5: XP Calculation and Award Engine

**User Story:** As a user, I want to earn XP through a fair and transparent formula, so that my progress feels earned and meaningful.

#### Acceptance Criteria

1. WHEN a quest is completed, THE XP_Engine SHALL calculate final XP using the formula: FinalXP = BaseXP × DifficultyMultiplier × StreakMultiplier × ArcMultiplier + BonusXP
2. THE XP_Engine SHALL apply difficulty multipliers of 1x (Easy), 1.5x (Medium), 2x (Hard), and 3x (Legendary)
3. THE XP_Engine SHALL apply the combo multiplier based on current streak days using the formula: ComboMultiplier = 1 + (0.01 × StreakDays) with a maximum cap of 2x
4. THE XP_Engine SHALL enforce a daily XP cap calculated as: DailyCap = 1000 + (Level × 20)
5. THE XP_Engine SHALL calculate all XP on the server side and reject any client-submitted XP values
6. WHEN a user completes all daily missions in a single day, THE XP_Engine SHALL award a Perfect Day bonus of 100 XP plus a chest unlock
7. THE XP_Engine SHALL log every XP transaction to the xp_history table with source type, source ID, amount, multiplier, and stat type

### Requirement 6: Leveling System

**User Story:** As a user, I want to level up through a non-linear progression curve, so that early levels feel fast and later levels feel like earned mastery.

#### Acceptance Criteria

1. THE Level_Engine SHALL calculate XP required for each level using the formula: XP_Required = 100 × Level^1.5
2. WHEN a user's total XP reaches the threshold for the next level, THE Level_Engine SHALL automatically increment the user's level and trigger a level-up event
3. WHEN a level-up occurs, THE Ascend_System SHALL display a full-screen level-up animation with the new level number and any unlocked rewards
4. WHEN a user reaches Level 10, THE Level_Engine SHALL unlock access to the League system
5. WHEN a user reaches Level 25, THE Level_Engine SHALL unlock access to the Guild system
6. WHEN a user reaches Level 100, THE Level_Engine SHALL unlock the Prestige system allowing level reset for prestige badges and a permanent XP bonus (PrestigeXP = BaseXP × (1 + 0.1 × PrestigeLevel))

### Requirement 7: Streak and Combo System

**User Story:** As a user, I want my daily consistency to be tracked and rewarded with increasing multipliers, so that I am motivated to maintain my habits every day.

#### Acceptance Criteria

1. THE Streak_Engine SHALL increment the user's streak counter when the user completes at least 80% of assigned daily quests before the daily reset
2. IF a user misses the 80% threshold and no Streak Shield is active, THEN THE Streak_Engine SHALL reset the current streak to zero and enter Comeback Mode
3. WHEN Comeback Mode activates, THE Streak_Engine SHALL provide a 48-hour redemption window with reduced difficulty quests and recovery XP bonuses
4. WHERE a user has a Streak Shield available, THE Streak_Engine SHALL auto-activate the shield to protect one missed day without breaking the streak
5. THE Streak_Engine SHALL track and display both current streak and longest streak for the user
6. THE Streak_Engine SHALL update the combo multiplier in real-time as the streak changes, applying the formula: ComboMultiplier = 1 + (0.01 × StreakDays) capped at 2x

### Requirement 8: Arc Mode Progression

**User Story:** As a user, I want to embark on guided growth journeys (Arcs) with phases, milestones, and bosses, so that I have structured long-term goals driving my daily habits.

#### Acceptance Criteria

1. THE Arc_Engine SHALL provide prebuilt Arcs (Monk, Warrior, Scholar, Creator, Beast Mode) each containing phases, milestones, quests, bosses, and a skill tree path
2. WHEN a user starts an Arc, THE Arc_Engine SHALL create a user_arc_progress record tracking progress percentage, current phase, and start date
3. WHEN a user completes an Arc milestone, THE Arc_Engine SHALL update progress percentage, award milestone XP, and check for phase transition
4. THE Arc_Engine SHALL support Arc durations of 30 to 90 days with progression phases (Beginner, Intermediate, Elite, Master)
5. WHEN a user creates a custom Arc, THE Arc_Engine SHALL validate that the Arc has a title, goal, duration, at least one milestone, and quest frequency
6. IF a user's performance declines during an Arc, THEN THE Arc_Engine SHALL reduce quest difficulty temporarily rather than failing the Arc

### Requirement 9: Character Stats System

**User Story:** As a user, I want my real-life actions to build RPG character stats, so that I can see my growth across multiple life dimensions.

#### Acceptance Criteria

1. THE Ascend_System SHALL maintain six character stats per user: Strength (fitness), Wisdom (learning), Focus (deep work), Discipline (consistency), Vitality (sleep and health), and Charisma (social confidence)
2. WHEN a quest is completed, THE XP_Engine SHALL award stat points to the stat type associated with that quest using the formula: StatGain = BaseStat × DifficultyMultiplier
3. WHILE Hard Mode is enabled for a user, THE Ascend_System SHALL apply stat decay when the user skips quests repeatedly (example: no workout for 7 days results in Strength -5)
4. WHEN a user's stat reaches defined thresholds, THE Ascend_System SHALL unlock identity titles (example: Focus > 500 unlocks "The Focused One")

### Requirement 10: League and Ranking System

**User Story:** As a competitive user, I want to compete in weekly leagues against similar players, so that I stay motivated through healthy competition.

#### Acceptance Criteria

1. THE League_Engine SHALL segment users into tiers: Bronze (default), Silver (Level 10), Gold (Level 20), Platinum (Level 35), Diamond (Level 50), Master (Level 75), Ascendant (invite/elite)
2. THE League_Engine SHALL calculate league score using the formula: LeagueScore = 0.4(Level) + 0.3(Consistency) + 0.2(Streak) + 0.1(ActivityScore)
3. WHEN the weekly league cycle ends (Sunday), THE League_Engine SHALL promote the top 15 users and demote the bottom 15 users in each league group
4. THE League_Engine SHALL match users into league groups of similar skill level for fair competition
5. THE Anti_Cheat_System SHALL detect suspicious activity (50+ quests in 2 minutes, impossible streak growth, bulk quest spam) and apply penalties including XP rollback and leaderboard ban

### Requirement 11: Guild System

**User Story:** As a social user, I want to join or create guilds for shared accountability and team challenges, so that I have community support for my growth journey.

#### Acceptance Criteria

1. WHEN a user creates a guild, THE Guild_Engine SHALL create the guild with name, description, type (public/private/premium), and member cap (Free: 10, Premium: 50)
2. WHEN a user joins a public guild, THE Guild_Engine SHALL add the user as a member and update the guild member count
3. THE Guild_Engine SHALL support shared guild quests where members contribute to common goals for guild XP
4. THE Guild_Engine SHALL maintain a guild leaderboard ranking guilds by consistency, quests completed, and average streak
5. WHEN a guild member sends a chat message, THE Guild_Engine SHALL deliver the message to all guild members in real-time via WebSocket

### Requirement 12: Boss Battle System

**User Story:** As a user, I want to face boss challenges that require sustained effort over days or weeks, so that I have epic milestone goals with legendary rewards.

#### Acceptance Criteria

1. THE Boss_Engine SHALL define bosses as multi-stage challenges (example: "Discipline Demon" requires a 30-day streak across 3 stages of 7, 14, and 30 days)
2. WHEN a user's quest completions contribute to boss progress, THE Boss_Engine SHALL update the boss progress percentage and check for stage completion
3. WHEN a boss is defeated, THE Boss_Engine SHALL award legendary XP (300-1000), exclusive titles, cosmetics, and aura effects
4. THE Boss_Engine SHALL support guild boss battles where guild members collectively contribute to defeating a shared boss

### Requirement 13: Skill Tree System

**User Story:** As a user progressing through an Arc, I want to unlock skill nodes that grant passive XP buffs, so that my investment in specific areas compounds over time.

#### Acceptance Criteria

1. THE Skill_Tree_Engine SHALL enforce prerequisite ordering where a user must unlock parent nodes before child nodes (example: Meditation → Deep Focus → Mind Mastery)
2. WHEN a user unlocks a skill node, THE Skill_Tree_Engine SHALL apply the passive buff to future XP calculations (example: "Mind Mastery" grants +10% Focus XP, formula: BoostedXP = BaseXP × (1 + SkillBoost))
3. WHERE a user has Premium subscription, THE Skill_Tree_Engine SHALL allow skill tree reset with a 30-day cooldown

### Requirement 14: AI Coach System

**User Story:** As a premium user, I want an AI coach that adapts my experience based on my behavior patterns, so that I avoid burnout and stay optimally challenged.

#### Acceptance Criteria

1. THE AI_Coach SHALL analyze quest completion patterns, streak history, activity timing, and Arc progress to generate personalized recommendations
2. THE AI_Coach SHALL calculate burnout risk using the formula: BurnoutRisk = (MissedQuests + StreakBreaks + DecliningActivity) / MotivationScore
3. WHEN burnout risk exceeds a defined threshold, THE AI_Coach SHALL activate Recovery Mode with reduced quest count, lower difficulty, and recovery XP bonuses
4. THE AI_Coach SHALL suggest optimal quest timing based on the user's historical activity patterns
5. THE AI_Coach SHALL adjust quest difficulty automatically based on user performance (adaptive difficulty)

### Requirement 15: Notification Engine

**User Story:** As a user, I want timely and intelligent notifications, so that I am reminded of quests and streak risks without being overwhelmed.

#### Acceptance Criteria

1. WHEN a user's streak is at risk (less than 45 minutes before daily reset with incomplete quests), THE Notification_Engine SHALL send a high-priority streak emergency alert via FCM
2. THE Notification_Engine SHALL limit notifications to a maximum of 5 per day per user
3. THE Notification_Engine SHALL determine optimal notification timing based on user activity patterns
4. WHILE a user is in burnout Recovery Mode, THE Notification_Engine SHALL reduce notification frequency and intensity
5. THE Notification_Engine SHALL support notification types: Quest Reminder, Streak Warning, Reward Alert, Guild Reminder, and Level Up

### Requirement 16: Premium Subscription System

**User Story:** As a user, I want a clear free-to-premium upgrade path with meaningful benefits, so that I can choose to enhance my experience without feeling forced.

#### Acceptance Criteria

1. THE Premium_Service SHALL provide a Free tier (basic quests, limited arcs, XP system, limited analytics) and a Premium tier (full AI Coach, unlimited custom arcs, advanced analytics, skill reset, premium cosmetics, streak shields, hard mode)
2. THE Premium_Service SHALL offer a 7-day free trial with all premium features unlocked and no forced payment
3. THE Premium_Service SHALL gate premium features with a teaser preview rather than aggressive paywalls
4. THE Premium_Service SHALL prevent pay-to-win by ensuring premium users cannot purchase leaderboard rank or unfair XP advantages
5. WHEN a premium subscription expires, THE Premium_Service SHALL gracefully downgrade the user to Free tier while preserving all earned progress

### Requirement 17: Reward Economy

**User Story:** As a user, I want to earn and spend in-game currencies on cosmetics and power-ups, so that I have additional motivation beyond XP and levels.

#### Acceptance Criteria

1. THE Reward_Economy SHALL maintain two currency types: Coins (earned free through gameplay) and Gems (premium paid currency)
2. THE Reward_Economy SHALL enforce anti-inflation rules including daily reward caps and diminishing returns on repeated actions
3. WHEN a loot chest is earned, THE Reward_Economy SHALL determine contents using the formula: DropRate = BaseRate × StreakBonus × EventMultiplier across tiers (Common, Rare, Epic, Legendary)
4. THE Reward_Economy SHALL support unlockable cosmetics including avatars, profile frames, aura effects, titles, and animations
5. WHEN a user meets achievement criteria, THE Reward_Economy SHALL permanently unlock the achievement with its associated title and badge

### Requirement 18: Analytics and Insights

**User Story:** As a user, I want to see detailed analytics about my progress, so that I can understand my strengths, weaknesses, and growth trends.

#### Acceptance Criteria

1. THE Analytics_Engine SHALL provide a progress dashboard showing XP growth, level growth, quest completion rate, streak history, and stat trends
2. WHEN Sunday arrives, THE Analytics_Engine SHALL generate a weekly review including quests completed, strongest stat, weakest stat, and personalized recommendations
3. THE Analytics_Engine SHALL calculate Life Score using the formula: LifeScore = 0.25(Discipline) + 0.2(Focus) + 0.2(Health) + 0.2(Learning) + 0.15(Consistency)
4. THE Analytics_Engine SHALL provide activity heatmap data showing quest completion patterns over time
5. THE Analytics_Engine SHALL detect habit correlations and surface insights (example: "Sleeping before 11 PM improves your Focus XP by 27%")

### Requirement 19: Security and Anti-Cheat

**User Story:** As a platform operator, I want robust security and anti-cheat measures, so that the competitive integrity of the platform is maintained.

#### Acceptance Criteria

1. THE Auth_Service SHALL implement role-based access control with roles: USER, PREMIUM_USER, MODERATOR, ADMIN, SUPER_ADMIN
2. THE Anti_Cheat_System SHALL detect speed violations (more than 10 quests completed in less than 5 minutes) and flag the account
3. THE Anti_Cheat_System SHALL enforce idempotency on quest completions using the unique constraint (user_id, quest_id, date)
4. THE Ascend_System SHALL enforce rate limiting per endpoint category (Quest completion: 20/min, API general: 100/min, Auth: 10/min, Guild chat: 30/min)
5. THE Ascend_System SHALL communicate exclusively over HTTPS with TLS 1.3 and reject HTTP connections
6. THE Ascend_System SHALL validate all inputs server-side including string length limits, numeric range checks, enum validation, and SQL injection prevention via parameterized queries
7. THE Ascend_System SHALL log all security events (login attempts, permission denials, rate limit violations, anti-cheat flags) with timestamps and masked PII

### Requirement 20: Performance and Scalability

**User Story:** As a user, I want the app to be fast and responsive, so that the gamification experience feels smooth and immersive.

#### Acceptance Criteria

1. THE Ascend_System SHALL achieve app startup time of less than 2 seconds on target devices
2. THE Ascend_System SHALL maintain API response latency below 300 milliseconds for standard endpoints
3. THE Ascend_System SHALL render UI animations at 60 frames per second minimum
4. THE Ascend_System SHALL support lazy loading of feature modules to minimize initial bundle size
5. THE Ascend_System SHALL implement Redis caching for leaderboard rankings, user stats summaries, and active arc state with TTL of 5-30 minutes
6. THE Ascend_System SHALL support virtual scrolling for leaderboard lists and skeleton loading for data-dependent screens
7. THE Ascend_System SHALL target 99.9% uptime availability

### Requirement 21: Offline Mode and Data Sync

**User Story:** As a mobile user, I want to complete quests offline and have them sync when I reconnect, so that my progress is never lost due to connectivity issues.

#### Acceptance Criteria

1. WHILE the device has no internet connectivity, THE Ascend_System SHALL allow quest completion with local storage of actions
2. WHEN internet connectivity is restored, THE Ascend_System SHALL sync all locally queued actions to the Spring Boot backend for validation
3. IF the backend rejects a locally queued action during sync, THEN THE Ascend_System SHALL roll back the optimistic local state and notify the user
4. THE Ascend_System SHALL display an "Offline Mode" indicator when the device lacks connectivity
5. THE Ascend_System SHALL enable Firestore offline persistence for immediate UI responsiveness during connectivity gaps

### Requirement 22: Admin Panel

**User Story:** As an administrator, I want a content management and moderation system, so that I can manage arcs, quests, users, events, and system health.

#### Acceptance Criteria

1. THE Admin_Panel SHALL provide an Arc Management CMS for creating and editing arcs, quests, rewards, milestones, and bosses
2. THE Admin_Panel SHALL provide user moderation tools with actions: warn, suspend, ban, and leaderboard restriction
3. THE Admin_Panel SHALL display system analytics including DAU, retention rates, premium conversion, churn, and streak survival rates
4. THE Admin_Panel SHALL support creating seasonal events with duration, exclusive rewards, and themed challenges
5. THE Admin_Panel SHALL restrict access to users with ADMIN or SUPER_ADMIN roles only

### Requirement 23: Hard Mode and Accountability

**User Story:** As a disciplined user, I want an optional Hard Mode with real consequences for missed habits, so that I have higher stakes driving my consistency.

#### Acceptance Criteria

1. WHERE a user enables Hard Mode, THE Ascend_System SHALL apply penalties for missed quests including XP reduction (PenaltyXP = BaseXP × FailureMultiplier) and stat decay
2. WHERE a user enables Hard Mode, THE Streak_Engine SHALL apply streak damage for missed days without a shield
3. IF a user fails in Hard Mode, THEN THE Ascend_System SHALL display a shame-free recovery message ("Your Arc continues tomorrow") rather than guilt-inducing language
4. WHERE a user opts into Accountability Contracts, THE Ascend_System SHALL support optional money stakes that are forfeited on sustained failure

### Requirement 24: Social Features

**User Story:** As a social user, I want to connect with friends, issue challenges, and see an activity feed, so that I have social accountability and community engagement.

#### Acceptance Criteria

1. THE Ascend_System SHALL support a friend system with follow, add, and challenge capabilities with privacy levels (Public, Friends Only, Private)
2. WHEN a user issues a challenge to a friend, THE Ascend_System SHALL create a tracked competition (example: "First to complete 10 workouts wins")
3. THE Ascend_System SHALL provide a social activity feed showing friend achievements, level-ups, and milestone completions
4. THE Ascend_System SHALL support accountability partner pairing where partners receive alerts for each other's missed quests

### Requirement 25: Real-Time Communication

**User Story:** As a user, I want real-time updates for leaderboards, guild chat, and XP notifications, so that the app feels alive and responsive.

#### Acceptance Criteria

1. WHEN a guild member sends a chat message, THE Ascend_System SHALL deliver the message to all online guild members via WebSocket within 1 second
2. WHEN a user earns XP, THE Ascend_System SHALL update the leaderboard position in real-time for users viewing the leaderboard
3. THE Ascend_System SHALL maintain user presence status (online/offline/last seen) via Firestore realtime listeners
4. WHEN a user receives a reward or achievement, THE Ascend_System SHALL deliver an in-app notification in real-time without requiring a page refresh
