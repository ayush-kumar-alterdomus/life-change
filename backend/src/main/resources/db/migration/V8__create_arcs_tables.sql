CREATE TABLE arcs (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name          VARCHAR(100) NOT NULL,
    description   TEXT,
    type          VARCHAR(50),
    difficulty    VARCHAR(20),
    duration_days INT NOT NULL,
    is_prebuilt   BOOLEAN NOT NULL DEFAULT false,
    created_at    TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE arc_milestones (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    arc_id      UUID NOT NULL,
    title       VARCHAR(200) NOT NULL,
    description TEXT,
    order_index INT NOT NULL,
    xp_reward   INT NOT NULL,

    CONSTRAINT fk_arc_milestones_arc FOREIGN KEY (arc_id) REFERENCES arcs(id) ON DELETE CASCADE
);
