CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    firebase_uid    VARCHAR(255) UNIQUE NOT NULL,
    username        VARCHAR(50) UNIQUE,
    email           VARCHAR(255) UNIQUE,
    avatar_url      TEXT,
    level           INT NOT NULL DEFAULT 1,
    xp              BIGINT NOT NULL DEFAULT 0,
    league          VARCHAR(20) NOT NULL DEFAULT 'BRONZE',
    premium         BOOLEAN NOT NULL DEFAULT FALSE,
    hard_mode       BOOLEAN NOT NULL DEFAULT FALSE,
    timezone        VARCHAR(50) NOT NULL DEFAULT 'UTC',
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);
