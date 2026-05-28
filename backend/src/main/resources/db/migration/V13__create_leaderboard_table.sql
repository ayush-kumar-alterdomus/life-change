CREATE TABLE leaderboard (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id           UUID UNIQUE NOT NULL,
    weekly_xp         BIGINT NOT NULL DEFAULT 0,
    weekly_rank       INT,
    global_rank       INT,
    league            VARCHAR(20) NOT NULL DEFAULT 'BRONZE',
    consistency_score DECIMAL NOT NULL DEFAULT 0,
    season_id         VARCHAR(50),
    updated_at        TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_leaderboard_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
