-- Add guild_rankings table
CREATE TABLE IF NOT EXISTS guild_rankings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    guild_id UUID NOT NULL UNIQUE,
    avg_consistency DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    total_quests_completed BIGINT NOT NULL DEFAULT 0,
    avg_streak DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    score DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    rank INTEGER,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Add moderation fields to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS suspended BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS suspended_until TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS banned BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS flagged BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_active TIMESTAMP;

-- Add seasonal_events table
CREATE TABLE IF NOT EXISTS seasonal_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    rewards JSONB,
    challenges JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Add security_violations table
CREATE TABLE IF NOT EXISTS security_violations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    violation_type VARCHAR(30) NOT NULL,
    description TEXT,
    completions_detected INTEGER NOT NULL,
    time_window_minutes INTEGER NOT NULL,
    xp_rolled_back BIGINT NOT NULL DEFAULT 0,
    leaderboard_banned BOOLEAN NOT NULL DEFAULT FALSE,
    resolved BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
