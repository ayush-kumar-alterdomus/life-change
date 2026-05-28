-- Performance indexes for the Ascend platform

-- Users indexes
CREATE INDEX IF NOT EXISTS idx_users_firebase_uid ON users(firebase_uid);
CREATE INDEX IF NOT EXISTS idx_users_level_xp ON users(level DESC, xp DESC);

-- Quest completion indexes
CREATE INDEX IF NOT EXISTS idx_quest_completion_user ON quest_completion(user_id);
CREATE INDEX IF NOT EXISTS idx_quest_completion_date ON quest_completion(completed_at);

-- Leaderboard indexes
CREATE INDEX IF NOT EXISTS idx_leaderboard_league ON leaderboard(league);
CREATE INDEX IF NOT EXISTS idx_leaderboard_rank ON leaderboard(weekly_rank);
CREATE INDEX IF NOT EXISTS idx_leaderboard_xp ON leaderboard(weekly_xp DESC);

-- Streaks indexes
CREATE INDEX IF NOT EXISTS idx_streaks_user ON streaks(user_id);

-- XP history indexes
CREATE INDEX IF NOT EXISTS idx_xp_history_user ON xp_history(user_id);
CREATE INDEX IF NOT EXISTS idx_xp_history_date ON xp_history(created_at);

-- Guild members indexes
CREATE INDEX IF NOT EXISTS idx_guild_members_user ON guild_members(user_id);
CREATE INDEX IF NOT EXISTS idx_guild_members_guild ON guild_members(guild_id);
