CREATE TABLE user_behavior_metrics (
    id                       UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id                  UUID NOT NULL UNIQUE,
    missed_quests_7d         INT NOT NULL DEFAULT 0,
    streak_breaks_30d        INT NOT NULL DEFAULT 0,
    declining_activity_score DECIMAL(5,4) NOT NULL DEFAULT 0,
    motivation_score         DECIMAL(5,4) NOT NULL DEFAULT 1.0,
    burnout_risk             DECIMAL(5,4) NOT NULL DEFAULT 0,
    recovery_mode_active     BOOLEAN NOT NULL DEFAULT FALSE,
    recovery_started_at      TIMESTAMP,
    updated_at               TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_behavior_metrics_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
