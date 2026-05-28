CREATE TABLE guilds (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    owner_id    UUID NOT NULL,
    type        VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    max_members INT NOT NULL DEFAULT 10,
    guild_xp    BIGINT NOT NULL DEFAULT 0,
    created_at  TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_guilds_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE guild_members (
    id        UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    guild_id  UUID NOT NULL,
    user_id   UUID NOT NULL,
    role      VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_guild_members_guild FOREIGN KEY (guild_id) REFERENCES guilds(id) ON DELETE CASCADE,
    CONSTRAINT fk_guild_members_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_guild_members_guild_user UNIQUE (guild_id, user_id)
);
