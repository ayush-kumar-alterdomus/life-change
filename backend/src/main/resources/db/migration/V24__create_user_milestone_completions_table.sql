-- Track which milestones a user has completed within an arc
CREATE TABLE user_milestone_completions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id),
    arc_id UUID NOT NULL REFERENCES arcs(id),
    milestone_id UUID NOT NULL REFERENCES arc_milestones(id),
    completed_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_user_milestone UNIQUE (user_id, milestone_id)
);

CREATE INDEX idx_user_milestone_completions_user ON user_milestone_completions(user_id);
CREATE INDEX idx_user_milestone_completions_arc ON user_milestone_completions(arc_id);
