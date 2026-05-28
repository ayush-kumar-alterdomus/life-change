# 10 — Database Design

# Ascend — Enter Arc Mode

---

## Database Strategy

# Hybrid DB Architecture

### Firestore
Realtime + mobile-first lightweight state

### PostgreSQL
Complex logic, analytics, rankings, reporting

---

## Why PostgreSQL?

Better for:
- Analytics queries
- JSONB support
- Scaling
- Reporting
- Complex joins
- Leaderboard calculations

---

## Core ER Diagram

```
USER
 ├── QUEST_COMPLETION
 ├── USER_ARC_PROGRESS
 ├── XP_HISTORY
 ├── STREAK
 ├── USER_SKILL
 ├── USER_ACHIEVEMENT
 ├── USER_NOTIFICATION
 ├── GUILD_MEMBER
 ├── LEADERBOARD
 └── SUBSCRIPTION

ARC
 ├── ARC_MILESTONE
 ├── ARC_QUEST
 └── ARC_BOSS

QUEST
 ├── QUEST_COMPLETION
 ├── QUEST_REWARD
 └── QUEST_CATEGORY

GUILD
 ├── GUILD_MEMBER
 ├── GUILD_CHALLENGE
 └── GUILD_CHAT

BOSS
 └── BOSS_PROGRESS
```

---

## Core Entity Relationships

| Relationship | Type | Description |
|-------------|------|-------------|
| User → Quest Completion | 1:Many | One user completes many quests |
| User → Arc | Many:1 | Many users can join same arc |
| User → Guild | Many:Many | Through guild_members |
| User → Skill Tree | 1:Many | Multiple unlocked skills |
| User → Achievement | 1:Many | Multiple achievements |
| Guild → Chat | 1:Many | Many messages per guild |
| Arc → Milestone | 1:Many | Multiple milestones per arc |
| Arc → Boss | 1:Many | Multiple bosses per arc |

---

## SQL Tables

---

### users

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    firebase_uid VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE,
    avatar_url TEXT,
    level INT DEFAULT 1,
    xp BIGINT DEFAULT 0,
    league VARCHAR(50) DEFAULT 'Bronze',
    premium BOOLEAN DEFAULT FALSE,
    hard_mode BOOLEAN DEFAULT FALSE,
    timezone VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

---

### quests

```sql
CREATE TABLE quests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    difficulty VARCHAR(50) NOT NULL,
    xp_reward INT NOT NULL,
    stat_type VARCHAR(50),
    frequency VARCHAR(50) DEFAULT 'daily',
    recurring BOOLEAN DEFAULT TRUE,
    arc_id UUID REFERENCES arcs(id),
    created_by UUID REFERENCES users(id),
    is_custom BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);
```

---

### quest_completion

```sql
CREATE TABLE quest_completion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    quest_id UUID NOT NULL REFERENCES quests(id),
    completed_at TIMESTAMP DEFAULT NOW(),
    xp_earned INT NOT NULL,
    multiplier DECIMAL(4,2) DEFAULT 1.0,
    difficulty_at_completion VARCHAR(50),
    UNIQUE(user_id, quest_id, completed_at::date)
);
```

---

### arcs

```sql
CREATE TABLE arcs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    difficulty VARCHAR(50) DEFAULT 'medium',
    duration_days INT NOT NULL,
    is_prebuilt BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW()
);
```

---

### arc_milestones

```sql
CREATE TABLE arc_milestones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    arc_id UUID NOT NULL REFERENCES arcs(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    order_index INT NOT NULL,
    xp_reward INT DEFAULT 0
);
```

---

### user_arc_progress

```sql
CREATE TABLE user_arc_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    arc_id UUID NOT NULL REFERENCES arcs(id),
    progress_percent INT DEFAULT 0,
    current_phase INT DEFAULT 1,
    started_at TIMESTAMP DEFAULT NOW(),
    completed_at TIMESTAMP,
    status VARCHAR(50) DEFAULT 'active',
    UNIQUE(user_id, arc_id)
);
```

---

### xp_history

```sql
CREATE TABLE xp_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    source_type VARCHAR(50) NOT NULL,
    source_id UUID,
    xp_amount INT NOT NULL,
    multiplier DECIMAL(4,2) DEFAULT 1.0,
    stat_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW()
);
```

---

### streaks

```sql
CREATE TABLE streaks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL REFERENCES users(id),
    current_streak INT DEFAULT 0,
    longest_streak INT DEFAULT 0,
    combo_multiplier DECIMAL(4,2) DEFAULT 1.0,
    last_completed_at TIMESTAMP,
    shield_available BOOLEAN DEFAULT FALSE,
    shield_used_at TIMESTAMP,
    updated_at TIMESTAMP DEFAULT NOW()
);
```

---

### user_stats

```sql
CREATE TABLE user_stats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL REFERENCES users(id),
    strength INT DEFAULT 0,
    wisdom INT DEFAULT 0,
    focus INT DEFAULT 0,
    discipline INT DEFAULT 0,
    vitality INT DEFAULT 0,
    charisma INT DEFAULT 0,
    life_score DECIMAL(5,2) DEFAULT 0,
    updated_at TIMESTAMP DEFAULT NOW()
);
```

---

### user_skills

```sql
CREATE TABLE user_skills (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    skill_id UUID NOT NULL,
    skill_name VARCHAR(255),
    arc_id UUID REFERENCES arcs(id),
    unlocked BOOLEAN DEFAULT FALSE,
    unlocked_at TIMESTAMP,
    UNIQUE(user_id, skill_id)
);
```

---

### guilds

```sql
CREATE TABLE guilds (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    owner_id UUID NOT NULL REFERENCES users(id),
    type VARCHAR(50) DEFAULT 'public',
    max_members INT DEFAULT 10,
    guild_xp BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW()
);
```

---

### guild_members

```sql
CREATE TABLE guild_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    guild_id UUID NOT NULL REFERENCES guilds(id),
    user_id UUID NOT NULL REFERENCES users(id),
    role VARCHAR(50) DEFAULT 'member',
    joined_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(guild_id, user_id)
);
```

---

### leaderboard

```sql
CREATE TABLE leaderboard (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    weekly_xp BIGINT DEFAULT 0,
    weekly_rank INT,
    global_rank INT,
    league VARCHAR(50),
    consistency_score DECIMAL(5,2) DEFAULT 0,
    season_id VARCHAR(50),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

---

### boss_progress

```sql
CREATE TABLE boss_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    boss_id UUID NOT NULL,
    boss_name VARCHAR(255),
    current_stage INT DEFAULT 1,
    progress_percent INT DEFAULT 0,
    defeated BOOLEAN DEFAULT FALSE,
    defeated_at TIMESTAMP,
    UNIQUE(user_id, boss_id)
);
```

---

### achievements

```sql
CREATE TABLE achievements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    achievement_name VARCHAR(255) NOT NULL,
    achievement_type VARCHAR(100),
    description TEXT,
    unlocked_at TIMESTAMP DEFAULT NOW()
);
```

---

### subscriptions

```sql
CREATE TABLE subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL REFERENCES users(id),
    provider VARCHAR(50),
    plan_type VARCHAR(50),
    premium BOOLEAN DEFAULT FALSE,
    started_at TIMESTAMP,
    expires_at TIMESTAMP,
    auto_renew BOOLEAN DEFAULT TRUE
);
```

---

### notifications_log

```sql
CREATE TABLE notifications_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255),
    message TEXT,
    sent_at TIMESTAMP DEFAULT NOW(),
    read_at TIMESTAMP
);
```

---

## Database Indexing Strategy

```sql
-- User lookups
CREATE INDEX idx_users_firebase_uid ON users(firebase_uid);
CREATE INDEX idx_users_level_xp ON users(level DESC, xp DESC);

-- Quest completion queries
CREATE INDEX idx_quest_completion_user ON quest_completion(user_id);
CREATE INDEX idx_quest_completion_date ON quest_completion(completed_at);

-- Leaderboard performance
CREATE INDEX idx_leaderboard_league ON leaderboard(league);
CREATE INDEX idx_leaderboard_rank ON leaderboard(weekly_rank);
CREATE INDEX idx_leaderboard_xp ON leaderboard(weekly_xp DESC);

-- Streak lookups
CREATE INDEX idx_streaks_user ON streaks(user_id);

-- Arc progress
CREATE INDEX idx_arc_progress_user ON user_arc_progress(user_id);

-- XP history
CREATE INDEX idx_xp_history_user ON xp_history(user_id);
CREATE INDEX idx_xp_history_date ON xp_history(created_at);

-- Guild members
CREATE INDEX idx_guild_members_user ON guild_members(user_id);
CREATE INDEX idx_guild_members_guild ON guild_members(guild_id);

-- Achievements
CREATE INDEX idx_achievements_user ON achievements(user_id);
```

---

## Scalability Strategy

### At 10K users
- PostgreSQL handles everything
- Basic indexing sufficient

### At 100K users
- Add Redis for leaderboard cache
- Read replicas for analytics

### At 1M+ users
- Move leaderboard to Redis entirely
- Analytics to data warehouse
- Guild chat to dedicated WebSocket service
- Consider sharding by league/region

---

*This document defines the complete database design for Ascend.*
