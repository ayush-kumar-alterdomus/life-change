-- Social features: friendships, challenges, activity feed, accountability partners

CREATE TABLE friendships (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL,
    friend_id   UUID NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_friendships_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_friendships_friend FOREIGN KEY (friend_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_friendships_user_friend UNIQUE (user_id, friend_id)
);

CREATE TABLE challenges (
    id                   UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    challenger_id        UUID NOT NULL,
    challenged_id        UUID NOT NULL,
    title                VARCHAR(255) NOT NULL,
    description          TEXT,
    target               INT NOT NULL,
    challenger_progress  INT NOT NULL DEFAULT 0,
    challenged_progress  INT NOT NULL DEFAULT 0,
    status               VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    winner_id            UUID,
    created_at           TIMESTAMP NOT NULL DEFAULT now(),
    ends_at              TIMESTAMP NOT NULL,

    CONSTRAINT fk_challenges_challenger FOREIGN KEY (challenger_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_challenges_challenged FOREIGN KEY (challenged_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_challenges_winner FOREIGN KEY (winner_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE activity_feed (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL,
    event_type  VARCHAR(50) NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_activity_feed_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE accountability_partners (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL,
    partner_id  UUID NOT NULL,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_accountability_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_accountability_partner FOREIGN KEY (partner_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_accountability_user_partner UNIQUE (user_id, partner_id)
);

-- Add privacy_level column to users table
ALTER TABLE users ADD COLUMN privacy_level VARCHAR(20) NOT NULL DEFAULT 'PUBLIC';

-- Performance indexes on user_id columns
CREATE INDEX IF NOT EXISTS idx_friendships_user ON friendships(user_id);
CREATE INDEX IF NOT EXISTS idx_friendships_friend ON friendships(friend_id);
CREATE INDEX IF NOT EXISTS idx_challenges_challenger ON challenges(challenger_id);
CREATE INDEX IF NOT EXISTS idx_challenges_challenged ON challenges(challenged_id);
CREATE INDEX IF NOT EXISTS idx_activity_feed_user ON activity_feed(user_id);
CREATE INDEX IF NOT EXISTS idx_activity_feed_created_at ON activity_feed(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_accountability_partners_user ON accountability_partners(user_id);
CREATE INDEX IF NOT EXISTS idx_accountability_partners_partner ON accountability_partners(partner_id);
