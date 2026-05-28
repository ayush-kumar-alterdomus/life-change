CREATE TABLE achievements (
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id          UUID NOT NULL,
    achievement_name VARCHAR(255) NOT NULL,
    achievement_type VARCHAR(50) NOT NULL,
    description      TEXT,
    unlocked_at      TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_achievements_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
