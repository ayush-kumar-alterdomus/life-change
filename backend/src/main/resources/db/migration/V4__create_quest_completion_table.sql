CREATE TABLE quest_completion (
    id                       UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id                  UUID NOT NULL,
    quest_id                 UUID NOT NULL,
    completed_at             TIMESTAMP NOT NULL DEFAULT now(),
    xp_earned                INT NOT NULL,
    multiplier               DECIMAL,
    difficulty_at_completion VARCHAR(20),

    CONSTRAINT fk_quest_completion_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_quest_completion_quest FOREIGN KEY (quest_id) REFERENCES quests(id) ON DELETE CASCADE
);

-- Unique constraint: one completion per user per quest per day
CREATE UNIQUE INDEX uq_quest_completion_user_quest_day
    ON quest_completion (user_id, quest_id, CAST(completed_at AS DATE));
