CREATE TABLE quests (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    difficulty      VARCHAR(20) NOT NULL,
    xp_reward       INT NOT NULL,
    stat_type       VARCHAR(20) NOT NULL,
    frequency       VARCHAR(20) NOT NULL,
    recurring       BOOLEAN NOT NULL DEFAULT FALSE,
    arc_id          UUID,
    created_by      UUID,
    is_custom       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_quests_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- FK to arcs table will be added in V8 after arcs table is created
