CREATE TABLE guild_challenges (
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    guild_id         UUID NOT NULL,
    title            VARCHAR(200) NOT NULL,
    target           INT NOT NULL,
    current_progress INT NOT NULL DEFAULT 0,
    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    ends_at          TIMESTAMP,

    CONSTRAINT fk_guild_challenges_guild FOREIGN KEY (guild_id) REFERENCES guilds(id) ON DELETE CASCADE
);
