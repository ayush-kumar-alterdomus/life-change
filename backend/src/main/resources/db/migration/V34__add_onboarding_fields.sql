-- V34__add_onboarding_fields.sql
ALTER TABLE users ADD COLUMN IF NOT EXISTS onboarding_complete BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE users ADD COLUMN IF NOT EXISTS selected_goals JSONB;
ALTER TABLE users ADD COLUMN IF NOT EXISTS personality_type VARCHAR(30);
ALTER TABLE users ADD COLUMN IF NOT EXISTS difficulty_preference VARCHAR(20);
