-- Skill tree nodes: each arc has its own skill tree
CREATE TABLE skill_nodes (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    arc_id          UUID NOT NULL,
    name            VARCHAR(100) NOT NULL,
    description     TEXT,
    stat_type       VARCHAR(50),
    buff_percent    DECIMAL NOT NULL,
    parent_node_id  UUID,
    order_index     INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_skill_nodes_arc FOREIGN KEY (arc_id) REFERENCES arcs(id) ON DELETE CASCADE,
    CONSTRAINT fk_skill_nodes_parent FOREIGN KEY (parent_node_id) REFERENCES skill_nodes(id) ON DELETE SET NULL
);

CREATE INDEX idx_skill_nodes_arc ON skill_nodes(arc_id);
CREATE INDEX idx_skill_nodes_parent ON skill_nodes(parent_node_id);

-- Add last_skill_reset_at to users for premium reset cooldown tracking
ALTER TABLE users ADD COLUMN last_skill_reset_at TIMESTAMP;

-- Update user_skills to reference skill_nodes.id via the existing skill_id column
ALTER TABLE user_skills
    ADD CONSTRAINT fk_user_skills_skill_node FOREIGN KEY (skill_id) REFERENCES skill_nodes(id) ON DELETE CASCADE;
