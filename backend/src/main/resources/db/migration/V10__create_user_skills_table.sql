CREATE TABLE user_skills (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL,
    skill_id    UUID NOT NULL,
    skill_name  VARCHAR(100) NOT NULL,
    arc_id      UUID,
    unlocked    BOOLEAN NOT NULL DEFAULT false,
    unlocked_at TIMESTAMP,

    CONSTRAINT fk_user_skills_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_skills_arc FOREIGN KEY (arc_id) REFERENCES arcs(id) ON DELETE SET NULL,
    CONSTRAINT uq_user_skills_user_skill UNIQUE (user_id, skill_id)
);
