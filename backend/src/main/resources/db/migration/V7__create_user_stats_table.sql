CREATE TABLE user_stats (
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id    UUID UNIQUE NOT NULL,
    strength   INT NOT NULL DEFAULT 0,
    wisdom     INT NOT NULL DEFAULT 0,
    focus      INT NOT NULL DEFAULT 0,
    discipline INT NOT NULL DEFAULT 0,
    vitality   INT NOT NULL DEFAULT 0,
    charisma   INT NOT NULL DEFAULT 0,
    life_score DECIMAL NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_user_stats_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
