CREATE TABLE boss_progress (
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id          UUID NOT NULL,
    boss_id          UUID NOT NULL,
    boss_name        VARCHAR(100) NOT NULL,
    current_stage    INT NOT NULL DEFAULT 1,
    progress_percent INT NOT NULL DEFAULT 0,
    defeated         BOOLEAN NOT NULL DEFAULT false,
    defeated_at      TIMESTAMP,

    CONSTRAINT fk_boss_progress_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_boss_progress_user_boss UNIQUE (user_id, boss_id)
);
