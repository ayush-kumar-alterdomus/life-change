-- Boss definitions table
CREATE TABLE bosses (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name            VARCHAR(100) NOT NULL,
    description     TEXT,
    total_stages    INT NOT NULL,
    stage_thresholds JSONB,
    reward_xp       INT NOT NULL,
    reward_title    VARCHAR(100),
    reward_cosmetic VARCHAR(100),
    is_guild_boss   BOOLEAN NOT NULL DEFAULT false,
    arc_id          UUID,

    CONSTRAINT fk_bosses_arc FOREIGN KEY (arc_id) REFERENCES arcs(id) ON DELETE SET NULL
);

-- Guild boss collective progress table
CREATE TABLE guild_boss_progress (
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    guild_id         UUID NOT NULL,
    boss_id          UUID NOT NULL,
    current_stage    INT NOT NULL DEFAULT 1,
    progress_percent INT NOT NULL DEFAULT 0,
    defeated         BOOLEAN NOT NULL DEFAULT false,
    defeated_at      TIMESTAMP,

    CONSTRAINT fk_guild_boss_progress_guild FOREIGN KEY (guild_id) REFERENCES guilds(id) ON DELETE CASCADE,
    CONSTRAINT fk_guild_boss_progress_boss FOREIGN KEY (boss_id) REFERENCES bosses(id) ON DELETE CASCADE,
    CONSTRAINT uq_guild_boss_progress_guild_boss UNIQUE (guild_id, boss_id)
);

-- Add FK from existing boss_progress table to the new bosses table
ALTER TABLE boss_progress
    ADD CONSTRAINT fk_boss_progress_boss FOREIGN KEY (boss_id) REFERENCES bosses(id) ON DELETE CASCADE;
