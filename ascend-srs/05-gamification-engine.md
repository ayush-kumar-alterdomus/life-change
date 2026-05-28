# 05 — Gamification Engine

# Ascend — Enter Arc Mode

---

## Game Economy Philosophy

Ascend must avoid:
- **Too Easy** — Users level too fast → boring
- **Too Hard** — Users feel punished → quit
- **Fake Progress** — Everything gives rewards → meaningless progression

### Core Rule
**Progress must feel earned but achievable.**

Users should think: "I'm getting stronger" without feeling overwhelmed.

---

# XP ECONOMY DESIGN

---

## XP Sources Distribution

### Primary Actions (80%)
Core habits: gym, coding, sleep, reading, meditation

### Secondary Actions (15%)
Engagement: guild participation, social challenges, streak saves

### Rare Rewards (5%)
Memorable dopamine: legendary chests, boss victories, seasonal rewards

---

## XP Distribution Table

| Activity | XP Range |
|----------|----------|
| Small Quest | 10–25 |
| Medium Quest | 30–60 |
| Hard Quest | 60–120 |
| Legendary Quest | 150–300 |
| Boss Battle | 300–1000 |
| Arc Completion | 1000–5000 |

---

## Daily XP Cap (Anti-Addiction)

Purpose: Prevent obsessive grinding.

Formula: `DailyCap = 1000 + (Level × 20)`

Example at Level 10: Cap = 1,200 XP

Prevents:
- Unhealthy obsession
- Leaderboard abuse
- Burnout

---

## XP Decay Prevention

Instead of losing levels during inactivity:
- Lose temporary bonus multiplier
- 7 inactive days: streak frozen, XP boost removed
- Never: level reset

---

## Difficulty Balancing

| Difficulty | Multiplier |
|-----------|-----------|
| Easy | 1x |
| Medium | 1.5x |
| Hard | 2x |
| Legendary | 3x |

### Final XP Formula
```
FinalXP = BaseXP × Difficulty × Consistency × ArcBonus
```

### Example
- Gym Quest, Base: 25, Difficulty: Hard (2x), Consistency: 1.5x, Arc: 1.2x
- Result: 25 × 2 × 1.5 × 1.2 = **90 XP**

---

# LEVEL BALANCING SYSTEM

---

## Level Curve Philosophy

- Early levels: FAST (retention critical)
- Later levels: MEANINGFUL (earned mastery)

### Formula
```
XP_Required = 100 × Level^1.6
```

---

## Level Phases

| Phase | Levels | Character |
|-------|--------|-----------|
| Beginner | 1–15 | Fast growth, retention critical |
| Intermediate | 15–40 | Moderate growth, identity building |
| Advanced | 40–75 | Meaningful grind, prestige unlocked |
| Elite | 75–100+ | Mastery, social prestige |

---

## Level Rewards

- Every level: Small reward (coins, minor cosmetic)
- Every 5 levels: Meaningful reward (title, theme)
- Every 10 levels: Major unlock (feature access)

### Key Milestones
- Level 10: Unlock Leagues
- Level 25: Unlock Guilds
- Level 50: Elite cosmetics
- Level 100: Prestige system

---

# STREAK PSYCHOLOGY ENGINE

---

## Streak Philosophy

Traditional apps: Miss one day → streak dead → user quits.

Ascend: Recovery-friendly with multiple safety nets.

---

## Combo Multiplier System

Formula: `ComboMultiplier = 1 + (0.01 × StreakDays)` (Cap: 2x)

| Streak | Multiplier |
|--------|-----------|
| 10 days | 1.1x |
| 50 days | 1.5x |
| 100 days | 2.0x (max) |

Users emotionally protect their streak because of the multiplier investment.

---

## Streak Shield Economy

Earned by: milestones, premium, legendary chests.

Rare resource that auto-activates to protect 1 missed day.

---

## Comeback Loop

Most habit apps fail here. Broken streak = quit.

Ascend system:
- **Redemption Window:** 48 hours to recover streak
- **Recovery XP:** Bonus for returning after break
- **Reduced difficulty:** Easier quests during recovery

---

## Anti-Guilt Design

Never show: "You failed"
Always show: "Continue your Arc"

---

# ARC PROGRESSION ENGINE

---

## Arc Structure

Each Arc: 30–90 days containing:
- Phases
- Milestones
- Bosses
- Quests
- Rewards
- Identity evolution

### Example: Monk Arc

**Phase 1 — Foundation:** Wake up early, meditate
**Phase 2 — Discipline:** Reduce distraction, deep work
**Phase 3 — Mastery:** Consistency, advanced habits

---

## Arc Progression Formula

```
ArcProgress = CompletedMilestones / TotalMilestones
```

---

## Arc Failure System

No hard failure. Instead:
- Quest difficulty reduced
- Easier quests during recovery
- Lower threshold temporarily

---

## Arc Identity Evolution

User identity evolves through the arc:

**Monk Arc Example:**
- Beginner: "Distracted Mind"
- Intermediate: "Focused Student"
- Advanced: "Disciplined Mind"
- Elite: "Zen Master"

---

# LEADERBOARD BALANCING

---

## Problem
Power users dominate → new users quit.

## Solution
- League segmentation (compete with similar users)
- Weekly reset (keeps competition fair)

### Formula
```
LeaderboardScore = XP + (Consistency × 2) + StreakBonus
```

**Key:** Consistency > Grinding

---

# BOSS BATTLE BALANCING

---

## Adaptive Difficulty
```
BossDifficulty = UserLevel × ArcDifficulty × MotivationScore
```

Always challenging, never impossible.

## Reward Philosophy
Boss rewards must feel memorable:
- Aura effects
- Rare titles
- Animated profile elements
- Legendary badges

---

# ANTI-BURNOUT ENGINE

---

## Burnout Detection Signals
- Declining activity
- Repeated failures
- Lower session duration
- Reduced engagement

### Formula
```
BurnoutRisk = MissedQuests + FatigueScore + ReducedEngagement
```

---

## Recovery Mode

When burnout detected:
- Lower difficulty
- Fewer quests
- Recovery XP (bonus for small wins)
- Wellness reminders

Example: Instead of 5 tasks/day → 2 easy wins/day

---

# RETENTION ENGINE

---

## Retention Timeline

| Day | Strategy |
|-----|----------|
| Day 1 | Quick win — user must level up fast |
| Day 7 | Streak attachment — emotional investment |
| Day 30 | Identity attachment — "I am disciplined" |
| Day 90 | Community + prestige — social belonging |

---

## Core Retention Loop
```
Quest → XP → Reward → Identity Growth → Social Recognition → Repeat
```

---

# ANTI-ADDICTION PRINCIPLES

We want healthy engagement, not exploitative addiction.

### Rules
- XP cap per day
- Recovery days encouraged
- Burnout mode activates automatically
- No infinite grinding
- Wellness reminders

Example message:
> "Time to rest. Growth needs recovery too."

---

# REWARD ECONOMY

---

## Currency Types

### Soft Currency (Coins)
- Earned free through gameplay
- Used for: themes, cosmetics, XP boosts, streak shields

### Premium Currency (Gems)
- Paid only
- Used for: exclusive cosmetics, skill resets

### Anti-Inflation Rules
- Cap rewards per day
- Prevent farming exploits
- Diminishing returns on repeated actions

---

## Loot Chest System

| Type | Contents |
|------|----------|
| Common | Basic rewards, small coins |
| Rare | XP multiplier tokens |
| Epic | Rare cosmetics |
| Legendary | Exclusive unlocks |

Formula: `DropRate = BaseRate × StreakBonus × EventMultiplier`

---

## Achievement System

Permanent achievements with titles and badges:
- "Early Riser" — Wake up 30 days
- "Iron Discipline" — 100-day streak
- "Coding Monk" — 100 focus sessions
- "Zen Master" — Complete Monk Arc
- "Warrior Elite" — Complete Warrior Arc

---

*This document defines the complete game economy that drives engagement and retention.*
