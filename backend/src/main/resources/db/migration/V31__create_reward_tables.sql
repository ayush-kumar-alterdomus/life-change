-- Reward Economy tables

CREATE TABLE user_currency (
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id    UUID NOT NULL UNIQUE,
    coins      BIGINT NOT NULL DEFAULT 0,
    gems       BIGINT NOT NULL DEFAULT 0,
    daily_coins_earned BIGINT NOT NULL DEFAULT 0,
    daily_reset_at     TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_user_currency_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE cosmetics (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(255) NOT NULL,
    type        VARCHAR(50) NOT NULL,
    rarity      VARCHAR(20) NOT NULL,
    description TEXT,
    gem_cost    INT,
    coin_cost   INT
);

CREATE TABLE user_cosmetics (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL,
    cosmetic_id UUID NOT NULL,
    unlocked_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_user_cosmetics_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_cosmetics_cosmetic FOREIGN KEY (cosmetic_id) REFERENCES cosmetics(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_cosmetics UNIQUE (user_id, cosmetic_id)
);

CREATE TABLE loot_chests (
    id        UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id   UUID NOT NULL,
    tier      VARCHAR(20) NOT NULL,
    source    VARCHAR(50) NOT NULL,
    opened    BOOLEAN NOT NULL DEFAULT FALSE,
    contents  JSONB,
    earned_at TIMESTAMP NOT NULL DEFAULT now(),
    opened_at TIMESTAMP,

    CONSTRAINT fk_loot_chests_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Alter existing achievements table to add reward-economy columns
ALTER TABLE achievements ADD COLUMN IF NOT EXISTS name VARCHAR(255);
ALTER TABLE achievements ADD COLUMN IF NOT EXISTS type VARCHAR(50);
ALTER TABLE achievements ADD COLUMN IF NOT EXISTS badge VARCHAR(255);

-- Backfill name/type from existing columns if they exist
UPDATE achievements SET name = achievement_name WHERE name IS NULL;
UPDATE achievements SET type = achievement_type WHERE type IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_achievements_user_name ON achievements(user_id, name);
CREATE INDEX IF NOT EXISTS idx_achievements_user ON achievements(user_id);
