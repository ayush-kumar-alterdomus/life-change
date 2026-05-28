-- ============================================================
-- DEVELOPMENT SEED DATA
-- This migration should only run in dev/test environments.
-- Configure Flyway profiles to exclude in production.
-- ============================================================

-- ============================================================
-- 1. PREBUILT ARCS
-- ============================================================
INSERT INTO arcs (id, name, description, type, difficulty, duration_days, is_prebuilt) VALUES
('a1000000-0000-0000-0000-000000000001', 'Monk', 'Master mindfulness, meditation, and inner peace through daily spiritual practices.', 'SPIRITUAL', 'MEDIUM', 30, true),
('a1000000-0000-0000-0000-000000000002', 'Warrior', 'Build physical strength, endurance, and discipline through intense training.', 'PHYSICAL', 'HARD', 45, true),
('a1000000-0000-0000-0000-000000000003', 'Scholar', 'Expand your knowledge through reading, study, and intellectual challenges.', 'INTELLECTUAL', 'MEDIUM', 30, true),
('a1000000-0000-0000-0000-000000000004', 'Creator', 'Unleash creativity through art, writing, music, or building projects.', 'CREATIVE', 'EASY', 21, true),
('a1000000-0000-0000-0000-000000000005', 'Beast Mode', 'Push beyond all limits with extreme physical and mental challenges.', 'PHYSICAL', 'LEGENDARY', 60, true);

-- ============================================================
-- 2. ARC MILESTONES (3-5 per Arc)
-- ============================================================

-- Monk milestones
INSERT INTO arc_milestones (id, arc_id, title, description, order_index, xp_reward) VALUES
('b1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000001', 'First Breath', 'Complete 7 consecutive days of meditation.', 1, 100),
('b1000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000001', 'Silent Mind', 'Achieve 15 minutes of uninterrupted meditation.', 2, 200),
('b1000000-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000001', 'Inner Peace', 'Complete a full digital detox day.', 3, 300),
('b1000000-0000-0000-0000-000000000004', 'a1000000-0000-0000-0000-000000000001', 'Enlightened', 'Maintain a 30-day mindfulness streak.', 4, 500);

-- Warrior milestones
INSERT INTO arc_milestones (id, arc_id, title, description, order_index, xp_reward) VALUES
('b1000000-0000-0000-0000-000000000005', 'a1000000-0000-0000-0000-000000000002', 'First Blood', 'Complete your first intense workout.', 1, 100),
('b1000000-0000-0000-0000-000000000006', 'a1000000-0000-0000-0000-000000000002', 'Iron Will', 'Work out 5 days in a row.', 2, 200),
('b1000000-0000-0000-0000-000000000007', 'a1000000-0000-0000-0000-000000000002', 'Unbreakable', 'Hit a new personal record in any exercise.', 3, 350),
('b1000000-0000-0000-0000-000000000008', 'a1000000-0000-0000-0000-000000000002', 'Spartan', 'Complete 30 workouts total.', 4, 500),
('b1000000-0000-0000-0000-000000000009', 'a1000000-0000-0000-0000-000000000002', 'Legendary Warrior', 'Maintain a 45-day fitness streak.', 5, 750);

-- Scholar milestones
INSERT INTO arc_milestones (id, arc_id, title, description, order_index, xp_reward) VALUES
('b1000000-0000-0000-0000-000000000010', 'a1000000-0000-0000-0000-000000000003', 'Curious Mind', 'Read for 30 minutes daily for 5 days.', 1, 100),
('b1000000-0000-0000-0000-000000000011', 'a1000000-0000-0000-0000-000000000003', 'Deep Thinker', 'Complete a full book or course module.', 2, 250),
('b1000000-0000-0000-0000-000000000012', 'a1000000-0000-0000-0000-000000000003', 'Knowledge Seeker', 'Study 3 different subjects in one week.', 3, 300),
('b1000000-0000-0000-0000-000000000013', 'a1000000-0000-0000-0000-000000000003', 'Sage', 'Maintain a 30-day learning streak.', 4, 500);

-- Creator milestones
INSERT INTO arc_milestones (id, arc_id, title, description, order_index, xp_reward) VALUES
('b1000000-0000-0000-0000-000000000014', 'a1000000-0000-0000-0000-000000000004', 'Spark', 'Complete your first creative session.', 1, 75),
('b1000000-0000-0000-0000-000000000015', 'a1000000-0000-0000-0000-000000000004', 'Flow State', 'Create for 1 hour without interruption.', 2, 150),
('b1000000-0000-0000-0000-000000000016', 'a1000000-0000-0000-0000-000000000004', 'Masterpiece', 'Finish a complete creative project.', 3, 400);

-- Beast Mode milestones
INSERT INTO arc_milestones (id, arc_id, title, description, order_index, xp_reward) VALUES
('b1000000-0000-0000-0000-000000000017', 'a1000000-0000-0000-0000-000000000005', 'Awakening', 'Complete a 5AM wake-up challenge for 7 days.', 1, 200),
('b1000000-0000-0000-0000-000000000018', 'a1000000-0000-0000-0000-000000000005', 'No Excuses', 'Complete all daily quests for 14 consecutive days.', 2, 400),
('b1000000-0000-0000-0000-000000000019', 'a1000000-0000-0000-0000-000000000005', 'Titan', 'Reach a 30-day streak in hard mode.', 3, 600),
('b1000000-0000-0000-0000-000000000020', 'a1000000-0000-0000-0000-000000000005', 'Ascended', 'Complete the full 60-day Beast Mode arc.', 4, 1000),
('b1000000-0000-0000-0000-000000000021', 'a1000000-0000-0000-0000-000000000005', 'Immortal', 'Achieve all stat types above level 5 simultaneously.', 5, 1500);


-- ============================================================
-- 3. SAMPLE QUESTS (20+ across all stat types and difficulties)
-- ============================================================
INSERT INTO quests (id, title, description, difficulty, xp_reward, stat_type, frequency, recurring, arc_id, is_custom) VALUES
-- STRENGTH quests
('c1000000-0000-0000-0000-000000000001', 'Morning Push-ups', 'Complete 20 push-ups first thing in the morning.', 'EASY', 25, 'STRENGTH', 'DAILY', true, 'a1000000-0000-0000-0000-000000000002', false),
('c1000000-0000-0000-0000-000000000002', 'Heavy Lifting', 'Complete a full weightlifting session at the gym.', 'MEDIUM', 50, 'STRENGTH', 'DAILY', true, 'a1000000-0000-0000-0000-000000000002', false),
('c1000000-0000-0000-0000-000000000003', 'Run 5K', 'Run 5 kilometers without stopping.', 'HARD', 100, 'STRENGTH', 'WEEKLY', false, 'a1000000-0000-0000-0000-000000000002', false),
('c1000000-0000-0000-0000-000000000004', 'Cold Shower Challenge', 'Take a 3-minute cold shower.', 'MEDIUM', 40, 'STRENGTH', 'DAILY', true, 'a1000000-0000-0000-0000-000000000005', false),

-- WISDOM quests
('c1000000-0000-0000-0000-000000000005', 'Read 20 Pages', 'Read at least 20 pages of a non-fiction book.', 'EASY', 25, 'WISDOM', 'DAILY', true, 'a1000000-0000-0000-0000-000000000003', false),
('c1000000-0000-0000-0000-000000000006', 'Journal Reflection', 'Write a reflective journal entry about your day.', 'EASY', 20, 'WISDOM', 'DAILY', true, 'a1000000-0000-0000-0000-000000000001', false),
('c1000000-0000-0000-0000-000000000007', 'Learn Something New', 'Spend 30 minutes learning a new skill or topic.', 'MEDIUM', 45, 'WISDOM', 'DAILY', true, 'a1000000-0000-0000-0000-000000000003', false),
('c1000000-0000-0000-0000-000000000008', 'Complete Online Course Module', 'Finish one module of an online course.', 'HARD', 80, 'WISDOM', 'WEEKLY', false, 'a1000000-0000-0000-0000-000000000003', false),

-- FOCUS quests
('c1000000-0000-0000-0000-000000000009', 'Deep Work Session', 'Complete 90 minutes of uninterrupted focused work.', 'MEDIUM', 50, 'FOCUS', 'DAILY', true, NULL, false),
('c1000000-0000-0000-0000-000000000010', 'No Social Media', 'Avoid all social media for the entire day.', 'HARD', 75, 'FOCUS', 'DAILY', false, NULL, false),
('c1000000-0000-0000-0000-000000000011', 'Pomodoro Sprint', 'Complete 4 Pomodoro sessions (25 min each).', 'EASY', 30, 'FOCUS', 'DAILY', true, NULL, false),

-- DISCIPLINE quests
('c1000000-0000-0000-0000-000000000012', 'Wake Up at 5AM', 'Wake up at 5:00 AM and start your routine.', 'HARD', 60, 'DISCIPLINE', 'DAILY', true, 'a1000000-0000-0000-0000-000000000005', false),
('c1000000-0000-0000-0000-000000000013', 'Make Your Bed', 'Make your bed immediately after waking up.', 'EASY', 10, 'DISCIPLINE', 'DAILY', true, NULL, false),
('c1000000-0000-0000-0000-000000000014', 'No Junk Food', 'Avoid all processed and junk food for the day.', 'MEDIUM', 35, 'DISCIPLINE', 'DAILY', true, NULL, false),
('c1000000-0000-0000-0000-000000000015', 'Evening Routine', 'Complete your full evening wind-down routine.', 'EASY', 20, 'DISCIPLINE', 'DAILY', true, NULL, false),

-- VITALITY quests
('c1000000-0000-0000-0000-000000000016', 'Drink 3L Water', 'Drink at least 3 liters of water throughout the day.', 'EASY', 15, 'VITALITY', 'DAILY', true, NULL, false),
('c1000000-0000-0000-0000-000000000017', 'Sleep 8 Hours', 'Get a full 8 hours of quality sleep.', 'MEDIUM', 30, 'VITALITY', 'DAILY', true, NULL, false),
('c1000000-0000-0000-0000-000000000018', 'Healthy Meal Prep', 'Prepare healthy meals for the next 3 days.', 'HARD', 70, 'VITALITY', 'WEEKLY', false, NULL, false),

-- CHARISMA quests
('c1000000-0000-0000-0000-000000000019', 'Talk to a Stranger', 'Have a meaningful conversation with someone new.', 'MEDIUM', 40, 'CHARISMA', 'DAILY', false, NULL, false),
('c1000000-0000-0000-0000-000000000020', 'Help Someone', 'Perform an act of kindness or help someone in need.', 'EASY', 25, 'CHARISMA', 'DAILY', true, NULL, false),
('c1000000-0000-0000-0000-000000000021', 'Public Speaking Practice', 'Practice speaking in front of a mirror or record yourself for 10 minutes.', 'HARD', 65, 'CHARISMA', 'WEEKLY', false, NULL, false),
('c1000000-0000-0000-0000-000000000022', 'Gratitude Message', 'Send a genuine thank-you message to someone.', 'EASY', 15, 'CHARISMA', 'DAILY', true, NULL, false);

-- ============================================================
-- 4. SAMPLE BOSSES (metadata reference)
-- ============================================================
-- Boss progress is per-user and created at runtime.
-- Boss definitions:
--   Boss 1: The Procrastinator (3 stages) - defeat by completing quests on time
--   Boss 2: The Inner Critic (4 stages) - defeat by maintaining streaks
--   Boss 3: The Comfort Zone (5 stages) - defeat by completing hard/legendary quests

-- ============================================================
-- 5. SAMPLE SKILL TREE NODES (metadata reference)
-- ============================================================
-- Skills are unlocked per-user and tracked in user_skills table.
-- Skill tree structure per Arc:
--   Monk: Breathing (tier 1), Meditation (tier 2), Mindfulness (tier 3), Transcendence (tier 4)
--   Warrior: Endurance (tier 1), Power (tier 2), Resilience (tier 3), Mastery (tier 4)
--   Scholar: Reading (tier 1), Analysis (tier 2), Synthesis (tier 3), Expertise (tier 4)
--   Creator: Ideation (tier 1), Craft (tier 2), Innovation (tier 3), Vision (tier 4)
--   Beast Mode: Grit (tier 1), Intensity (tier 2), Dominance (tier 3), Transcendence (tier 4)
