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

CREATE TABLE achievements (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL,
    name        VARCHAR(255) NOT NULL,
    type        VARCHAR(50) NOT NULL,
    description TEXT,
    badge       VARCHAR(255),
    unlocked_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_achievements_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_achievements_user_name UNIQUE (user_id, name)
);

CREATE INDEX idx_loot_chests_user ON loot_chests(user_id);
CREATE INDEX idx_user_cosmetics_user ON user_cosmetics(user_id);
CREATE INDEX idx_achievements_user ON achievements(user_id);
