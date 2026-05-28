CREATE TABLE streaks (
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id          UUID UNIQUE NOT NULL,
    current_streak   INT NOT NULL DEFAULT 0,
    longest_streak   INT NOT NULL DEFAULT 0,
    combo_multiplier DECIMAL NOT NULL DEFAULT 1.0,
    last_completed_at TIMESTAMP,
    shield_available BOOLEAN NOT NULL DEFAULT false,
    shield_used_at   TIMESTAMP,
    updated_at       TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_streaks_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
