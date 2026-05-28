CREATE TABLE user_arc_progress (
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id          UUID NOT NULL,
    arc_id           UUID NOT NULL,
    progress_percent INT NOT NULL DEFAULT 0,
    current_phase    INT NOT NULL DEFAULT 1,
    started_at       TIMESTAMP NOT NULL DEFAULT now(),
    completed_at     TIMESTAMP,
    status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT fk_user_arc_progress_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_arc_progress_arc FOREIGN KEY (arc_id) REFERENCES arcs(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_arc_progress_user_arc UNIQUE (user_id, arc_id)
);
