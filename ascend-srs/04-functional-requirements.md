# 04 — Functional Requirements

# Ascend — Enter Arc Mode

---

# EPIC 1 — AUTHENTICATION & USER MANAGEMENT

---

## FR-AUTH-001: User Registration

### Description
The system shall allow users to register using Email/Password, Google Sign-In, Apple Sign-In, and Anonymous Guest Mode.

### Objective
Reduce onboarding friction.

### Business Rules

**Email Registration:**
- Mandatory: email, password
- Validation: valid email format, password ≥ 8 chars, uppercase required, special character required

**Social Login:**
- One-tap login via Google and Apple

### User Flow
```
Launch App → Login Screen → Options:
1. Continue with Google
2. Continue with Apple
3. Email Signup
4. Continue as Guest
```

### Acceptance Criteria
- ✅ Account created successfully
- ✅ Firebase token generated
- ✅ User document created in Firestore
- ✅ Session persists after app restart

### Edge Cases
- Existing email → Error: "Account already exists"
- Network failure → Show retry state
- Firebase failure → Fallback error UI

---

## FR-AUTH-002: Guest Mode

### Description
Users may explore app without signup.

**Allowed:** Onboarding, quests, basic gameplay
**Restricted:** Leaderboard, cloud sync, guilds

### Business Rule
Guest account expires after 30 days inactivity. Prompt: "Save your progress"

### Acceptance Criteria
- ✅ Guest progress saved locally
- ✅ Upgrade to account preserves progress

---

## FR-AUTH-003: Session Persistence

### Description
User session shall remain active via Firebase refresh token with automatic refresh.

### Acceptance Criteria
- ✅ User remains logged in
- ✅ No forced login every launch

---

## FR-AUTH-004: Profile Creation

After authentication, user profile created:

```json
{
  "userId": "",
  "username": "",
  "avatar": "",
  "currentLevel": 1,
  "xp": 0,
  "rank": "Bronze",
  "selectedArc": "",
  "createdAt": "",
  "timezone": "",
  "preferences": {}
}
```

---

## FR-AUTH-005: Multi-device Sync

### Description
Progress sync across devices including quests, streaks, XP, stats, and arcs.

### Acceptance Criteria
- ✅ Progress visible instantly
- ✅ No duplicate XP

---

# EPIC 2 — RPG ONBOARDING ENGINE

---

## FR-ONBOARD-001: RPG Welcome Flow

### Objective
Make onboarding immersive instead of boring forms.

### Flow

**Screen 1: Cinematic Intro**
- Text: "Your strongest self awaits."
- CTA: **Enter Arc Mode**

**Screen 2: Choose Goal**
- Options: Discipline, Fitness, Learning, Productivity, Confidence, Mental Health

**Screen 3: Difficulty Selection**
- Casual Mode (low intensity)
- Balanced Mode (recommended)
- Beast Mode (hardcore, penalty enabled)

---

## FR-ONBOARD-002: Personality Assessment

### Questions
Example: "What stops you most?"
Options: procrastination, low motivation, distraction, inconsistency

### System Logic
AI score generated → Recommended arc returned

---

## FR-ONBOARD-003: Arc Recommendation Engine

### Algorithm
**Input:** goals, assessment, personality, available time
**Output:** Recommended Arc

### Sample Mapping
- Fitness + consistency → Warrior Arc
- Learning + coding → Scholar Arc
- Discipline + dopamine control → Monk Arc

### Acceptance Criteria
- ✅ Personalized recommendation shown
- ✅ User can override recommendation

---

# EPIC 3 — ARC MODE ENGINE

---

## FR-ARC-001: Prebuilt Arc Selection

Available Arcs:
- **Monk Arc** — Discipline
- **Warrior Arc** — Fitness
- **Scholar Arc** — Learning
- **Creator Arc** — Career growth
- **Beast Mode Arc** — Extreme discipline

Each arc contains: quests, milestones, rewards, bosses, skill path

---

## FR-ARC-002: Arc Progression

- Duration: 30–90 days
- Progression: Beginner → Intermediate → Elite → Master
- Rewards: titles, skins, XP multiplier, aura effects

### Acceptance Criteria
- ✅ Progress visible
- ✅ Rewards auto unlocked

---

## FR-ARC-003: Custom Arc Builder

Users create custom arcs with: title, goal, duration, milestones, quest frequency

### Validation
Minimum 1 milestone required.

---

# EPIC 4 — QUEST ENGINE

---

## FR-QUEST-001: Daily Quest System

### Quest Types
- **Main Quests** — High impact
- **Side Quests** — Optional
- **Daily Missions** — Recurring
- **Weekly Challenges** — Long-term

### Completion Rules
- Quest may only complete once/day
- Duplicate blocked

### Acceptance Criteria
- ✅ XP awarded instantly
- ✅ Animation shown
- ✅ Streak updated
- ✅ Quest marked complete

---

## FR-QUEST-002: Dynamic Difficulty

Difficulty adjusts based on consistency.

**Example:** If user skips gym, instead of 45-min workout, system adapts to 10-min recovery workout.

**Objective:** Reduce churn.

---

# EPIC 5 — XP SYSTEM & LEVELING ENGINE

---

## FR-XP-001: XP Reward Engine

### XP Sources

| Action | XP |
|--------|-----|
| Daily Quest | 10–40 |
| Main Quest | 50–150 |
| Weekly Challenge | 100–300 |
| Boss Battle | 300–1000 |
| Arc Completion | 500–5000 |
| Streak Bonus | Variable |
| First Completion Bonus | +25 |
| Perfect Day | +100 |

### XP Formula
```
FinalXP = BaseXP × DifficultyMultiplier × StreakMultiplier × ArcMultiplier + BonusXP
```

### Difficulty Multiplier
| Difficulty | Multiplier |
|-----------|-----------|
| Easy | ×1 |
| Medium | ×1.5 |
| Hard | ×2 |
| Legendary | ×3 |

### Acceptance Criteria
- ✅ XP granted instantly
- ✅ Animation triggered
- ✅ UI updated in real-time
- ✅ Server validation applied

### Edge Cases
- Duplicate completion → Prevent XP farming
- Offline completion → Store locally, sync later

---

## FR-XP-002: XP Animation System

Completing quests triggers immersive animations: particle burst, glow effect, level progress fill, vibration feedback.

---

## FR-XP-003: Combo Bonus System

| Combo | Multiplier |
|-------|-----------|
| 3-Day | +10% XP |
| 7-Day | +20% |
| 30-Day | +50% |
| 100-Day | +100% |

Formula: `FinalXP = XP × ComboMultiplier`

Anti-exploit: Combo breaks if user misses required quests (unless Streak Shield active).

---

## FR-XP-004: Perfect Day Bonus

All daily missions completed → +100 XP, chest unlock, multiplier token.

---

# EPIC 6 — LEVELING ENGINE

---

## FR-LEVEL-001: Player Level System

### Progression Curve (Non-linear)
Formula: `XP_Required = 100 × Level^1.5`

| Level | XP Needed |
|-------|-----------|
| 1 | 100 |
| 2 | 282 |
| 3 | 519 |
| 5 | 1,118 |
| 10 | 3,162 |
| 25 | 12,500 |
| 50 | 35,355 |
| 100 | 100,000+ |

### Acceptance Criteria
- ✅ Level updates automatically
- ✅ Animation shown
- ✅ Rewards granted

---

## FR-LEVEL-002: Level-Up Rewards

- Standard: coins, titles, cosmetics, profile effects
- Level 10: Unlock Leagues
- Level 25: Unlock Guilds
- Level 50: Legendary Avatar

---

## FR-LEVEL-003: Prestige System

After Level 100, user can reset for prestige badge, exclusive cosmetics, and global ranking boost.

Formula: `PrestigeXP = BaseXP × (1 + 0.1 × P)` where P = Prestige level

---

# EPIC 7 — STREAK ENGINE

---

## FR-STREAK-001: Daily Streak System

A streak continues if minimum 80% daily quests completed.

---

## FR-STREAK-002: Streak Shield (Premium)

Prevents streak loss. Earned via rewards, purchase, or milestones. Auto-activates once.

---

## FR-STREAK-003: Comeback Mode

When streak breaks, app enters recovery state with 48-hour redemption window.

---

## FR-STREAK-004: Relapse Prevention

AI detects declining completion, missed streaks, motivation drop. System adapts difficulty downward.

---

# EPIC 8 — CHARACTER STATS SYSTEM

---

## FR-STATS-001: RPG Stats

| Stat | Meaning |
|------|---------|
| Strength | Fitness |
| Wisdom | Learning |
| Focus | Deep work |
| Discipline | Consistency |
| Vitality | Sleep & health |
| Charisma | Social confidence |

Formula: `StatGain = BaseStat × DifficultyMultiplier`

---

## FR-STATS-002: Stat Decay (Hard Mode)

If user skips repeatedly, stats decline. Example: No workout 7 days → Strength -5

---

## FR-STATS-003: Identity Titles

Unlockable based on stats:
- Focus > 500 → "The Focused One"
- Strength > 1000 → "Iron Discipline"

---

# EPIC 9 — SKILL TREE SYSTEM

---

## FR-SKILL-001: Skill Tree Unlocking

Each arc contains skill tree with progressive unlocks.
- Monk Tree: Meditation → Deep Work → Mind Mastery
- Warrior Tree: Fitness → Endurance → Elite Athlete

Must unlock previous node before next.

---

## FR-SKILL-002: Passive Buffs

Skill unlocks give boosts. Example: "Mind Mastery" → +10% Focus XP
Formula: `BoostedXP = BaseXP × (1 + SkillBoost)`

---

## FR-SKILL-003: Skill Reset (Premium)

User can rebuild tree. Cooldown: 30 days.

---

# EPIC 10 — LEAGUE & RANKING SYSTEM

---

## FR-LEAGUE-001: Competitive League System

| Tier | Entry Requirement |
|------|------------------|
| Bronze | Default |
| Silver | Level 10 |
| Gold | Level 20 |
| Platinum | Level 35 |
| Diamond | Level 50 |
| Master | Level 75 |
| Ascendant | Invite/Elite |

Weekly cycle: Top 15 promoted, Bottom 15 demoted.

---

## FR-LEAGUE-002: Fair Matchmaking

Formula: `LeagueScore = 0.4(Level) + 0.3(Consistency) + 0.2(Streak) + 0.1(ActivityScore)`

---

## FR-LEAGUE-003: Seasonal Ranking

Global ranking resets each season with seasonal badges, cosmetics, titles, trophies.

---

## FR-LEAGUE-004: Anti-Cheat Detection

Detects suspicious completions, impossible streaks, bulk quest spam, XP farming.
Penalty: XP rollback, temporary suspension, leaderboard ban.

---

# EPIC 11 — GUILD / SQUAD SYSTEM

---

## FR-GUILD-001: Guild Creation

- Public Guild (anyone joins)
- Private Guild (invite only)
- Premium Guild (exclusive)
- Max members: Free=10, Premium=50

---

## FR-GUILD-002: Shared Quests

Guild members complete common goals for guild XP and rewards.

---

## FR-GUILD-003: Guild Leaderboard

Guilds compete on consistency, quests completed, average streak.

---

## FR-GUILD-004: Guild Penalty Mode (Optional)

Missing guild quest reduces guild score. Creates accountability.

---

## FR-GUILD-005: Guild Chat

Realtime communication via Spring WebSocket with reactions, quest sharing, celebrations.

---

# EPIC 12 — BOSS BATTLE SYSTEM

---

## FR-BOSS-001: Boss Battle Engine

Major milestone challenges:
- "Procrastination Boss" — 20 focus sessions
- "Discipline Demon" — 30-day streak
- "Sleep Destroyer" — Sleep before 11 PM for 14 days

Rewards: legendary XP, titles, cosmetics, aura effects.

---

## FR-BOSS-002: Multi-Stage Bosses

Bosses contain phases (7 days → 14 days → 30 days).

---

## FR-BOSS-003: Guild Boss Battles

Guild-wide challenges (e.g., 500 deep work hours) for guild prestige.

---

# EPIC 13 — REWARD ECONOMY

---

## FR-REWARD-001: Coins & Currency

- **Soft Currency (Coins):** Earned free through gameplay
- **Premium Currency (Gems):** Paid
- Anti-inflation rules: cap rewards, prevent farming

---

## FR-REWARD-002: Cosmetic System

Unlock avatars, profile frames, aura effects, titles, animations.

---

## FR-REWARD-003: Loot Chest System

Types: Common, Rare, Epic, Legendary
Formula: `DropRate = BaseRate × StreakBonus × EventMultiplier`

---

## FR-REWARD-004: Achievement System

Permanent achievements:
- "Early Riser" — Wake up 30 days
- "Iron Discipline" — 100-day streak
- "Coding Monk" — 100 focus sessions

---

# EPIC 14 — SOCIAL SYSTEM

---

## FR-SOCIAL-001: Friend System
Follow, add friends, challenge friends. Privacy levels: Public, Friends Only, Private.

## FR-SOCIAL-002: Challenges
Challenge friends (e.g., "First to complete 10 workouts wins").

## FR-SOCIAL-003: Social Feed
Activity feed showing friend achievements and milestones.

## FR-SOCIAL-004: Accountability Partner
Users pair with 1 accountability partner for missed quest alerts and encouragement.

---

# EPIC 15 — AI COACH SYSTEM

---

## FR-AI-001: Personalized AI Coach

Analyzes quest completion, streak history, activity timing, missed habits, Arc progress.

Detects: burnout probability, procrastination tendency, motivation drop, overtraining risk.

---

## FR-AI-002: Burnout Prevention Engine

Formula: `BurnoutRisk = (MissedQuests + StreakBreaks + DecliningActivity) / MotivationScore`

Activates Recovery Mode with easier quests, fewer tasks, recovery XP.

---

## FR-AI-003: Smart Schedule Optimization

AI suggests better timing based on patterns.

---

## FR-AI-004: Adaptive Difficulty System

Quest difficulty changes automatically based on user performance.

---

## FR-AI-005: AI Motivation Engine

Personalized messages based on personality type, streak state, emotional profile. Max 3 nudges/day.

---

# EPIC 16 — NOTIFICATION ENGINE

---

## FR-NOTIF-001: Smart Push Notifications (FCM)

Types: Quest Reminder, Streak Warning, Reward Alert, Guild Reminder.

## FR-NOTIF-002: Smart Notification Timing
AI determines best reminder time. Maximum 5/day.

## FR-NOTIF-003: Streak Emergency Alert
High-priority notification when streak at risk.

## FR-NOTIF-004: Silent Recovery Mode
During burnout, notifications reduce in frequency and intensity.

---

# EPIC 17 — PREMIUM & MONETIZATION

---

## FR-PREMIUM-001: Subscription Tiers

**Free:** Basic quests, basic arcs, XP system, limited analytics
**Premium:** Full AI Coach, unlimited custom arcs, advanced analytics, skill reset, premium cosmetics, streak shields, hard mode, accountability contracts

## FR-PREMIUM-002: Feature Gating
Locked features show premium teaser. No aggressive paywall.

## FR-PREMIUM-003: Free Trial
7 days, all premium features unlocked. No forced payment.

## FR-PREMIUM-004: Premium Economy Protection
No pay-to-win. Premium cannot buy leaderboard rank or unfair XP advantage.

---

# EPIC 18 — HARD MODE & ACCOUNTABILITY

---

## FR-HARD-001: Hard Mode
Penalties for missed quests: XP reduction, stat decay, streak damage.
Formula: `PenaltyXP = BaseXP × FailureMultiplier`

## FR-HARD-002: Accountability Contracts
Optional commitment with money stakes (₹500 stake, forfeited on failure).

## FR-HARD-003: Shame-Free Recovery
After failure, app avoids guilt. Message: "Your Arc continues tomorrow."

---

# EPIC 19 — ANALYTICS & INSIGHTS

---

## FR-ANALYTICS-001: Progress Dashboard
Metrics: XP growth, level growth, quest completion, streak history, stat trends.
Charts: heatmaps, consistency graph, monthly growth, radar stats.

## FR-ANALYTICS-002: Weekly Review
Every Sunday: quest summary, strongest/weakest stats, recommendations.

## FR-ANALYTICS-003: Life Score
Formula: `LifeScore = 0.25(Discipline) + 0.2(Focus) + 0.2(Health) + 0.2(Learning) + 0.15(Consistency)`

## FR-ANALYTICS-004: Habit Correlation Engine
Example insight: "Sleeping before 11 PM improves your Focus XP by 27%."

---

# EPIC 20 — ADMIN PANEL

---

## FR-ADMIN-001: Arc Management CMS
Manage arcs, quests, rewards, milestones, bosses.

## FR-ADMIN-002: User Moderation
Actions: warn, suspend, ban, leaderboard restriction.

## FR-ADMIN-003: Analytics Dashboard
Metrics: DAU, retention, premium conversion, churn, streak survival.

## FR-ADMIN-004: Event Management
Create seasonal events with duration and exclusive rewards.

---

# NON-FUNCTIONAL REQUIREMENTS

---

## NFR-001: Performance
- App startup: < 2 sec
- API latency: < 300 ms

## NFR-002: Availability
- Target uptime: 99.9%

## NFR-003: Scalability
- Support 1M+ users

## NFR-004: Security
- Firebase auth validation, encrypted communication, rate limiting, anti-cheat

## NFR-005: Mobile UX
- Smooth animations: 60 FPS minimum

---

*This document contains the complete functional specification for Ascend.*
