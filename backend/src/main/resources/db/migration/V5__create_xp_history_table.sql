CREATE TABLE xp_history (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL,
    source_type     VARCHAR(50) NOT NULL,
    source_id       UUID,
    xp_amount       INT NOT NULL,
    multiplier      DECIMAL,
    stat_type       VARCHAR(20),
    created_at      TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_xp_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
