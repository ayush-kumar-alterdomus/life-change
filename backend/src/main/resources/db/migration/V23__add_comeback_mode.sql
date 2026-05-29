ALTER TABLE streaks ADD COLUMN comeback_mode_active BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE streaks ADD COLUMN comeback_expires_at TIMESTAMP;
