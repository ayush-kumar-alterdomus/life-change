# 06 — UI/UX Blueprint

# Ascend — Enter Arc Mode

---

## Design Philosophy

Ascend should feel like a **premium self-improvement RPG**.

Not:
- ❌ Boring productivity app
- ❌ Childish game
- ❌ Spreadsheet tracker

---

## Emotional Goals

| Day | User Feeling |
|-----|-------------|
| Day 1 | "This feels exciting." |
| Day 7 | "I don't want to lose my streak." |
| Day 30 | "I've changed." |
| Day 90 | "This is who I am." |

---

# DESIGN SYSTEM

---

## Color Palette

| Element | Color | Hex |
|---------|-------|-----|
| Background | Deep black | `#0A0A0A` |
| Cards | Dark glass | `#161616` |
| Primary Accent | Progression orange | `#FF9800` |
| Secondary Accent | Mystic purple | `#A855F7` |
| Success | Quest completion | `#4CAF50` |
| Error | Missed quest | `#F44336` |
| Text Primary | White | `#FFFFFF` |
| Text Secondary | Light gray | `#B0B0B0` |

---

## Typography

### Headings
Bold, strong, impactful.
Examples: "LEVEL UP", "MONK ARC", "QUEST COMPLETE"

### Body
Minimal, readable, never cluttered.

### Font Recommendations
- Primary: Inter or SF Pro
- Display: Orbitron or Rajdhani (for RPG headers)

---

# UX RULES

---

## Rule 1: Every Tap Gives Feedback
Quest tap → animation + vibration + sound + glow. Never silent.

## Rule 2: No Empty Screens
Always show motivation, recommendations, or rewards.

## Rule 3: Always Show Progress
Every screen displays XP, Level, and Streak.

## Rule 4: Immediate Reward Loop
Quest complete → instant gratification, not delayed.

## Rule 5: Avoid Overwhelm
Never show 20 quests. Max 5–8 visible daily.

## Rule 6: Thumb-Friendly
Bottom-heavy layout. Most actions reachable one-handed.

---

# INFORMATION ARCHITECTURE

---

## Bottom Navigation

```
🏠 Home    ⚔ Quests    🔥 Arc    👥 Social    👤 Profile
```

High-frequency actions with low thumb distance.

---

# SCREEN-BY-SCREEN BLUEPRINT

---

## Screen 1 — Splash Screen

- Full black background
- Center logo: **ASCEND**
- Subtext: "Enter Arc Mode"
- Background: Subtle glowing particles
- Animated loading indicator
- **Goal:** Feel premium immediately

---

## Screen 2 — Welcome Screen

- Large cinematic text: "Become the strongest version of yourself."
- Hero illustration: Stylized avatar evolving
- Buttons:
  - **Enter Arc Mode** (Primary)
  - **Continue as Guest** (Secondary)

---

## Screen 3 — Goal Selection

Grid cards:
- ⚡ Discipline
- 💪 Fitness
- 🧠 Learning
- 💼 Productivity
- 😌 Mental Wellness
- 🗣 Confidence

Card expands on tap with glow effect.

---

## Screen 4 — Difficulty Selection

Cards:
- **Casual Mode** — Low intensity
- **Balanced Mode** — Recommended (🔥 badge)
- **Beast Mode** — Hardcore, dark dramatic theme

---

## Screen 5 — Arc Recommendation

Hero Card showing recommended arc:
- Arc name and description
- Quest preview (3–4 sample quests)
- **Begin Arc** button
- Secondary: "Choose another arc"

---

## Screen 6 — Dashboard (Home)

Most important screen. Must feel addictive.

### Layout Structure
```
Top Header (Greeting + Level Badge + Streak Flame)
XP Progress Card
Daily Summary Cards
Active Arc Card
Today's Quests
Motivation Widget
Leaderboard Snapshot
```

### Section 1 — Header
- Greeting: "Good Evening, Ayush"
- Level badge
- Streak flame icon

### Section 2 — XP Card
Large premium card:
```
Level 12
████████░░
2400 / 3000 XP
```
Animated progress bar.

### Section 3 — Daily Summary
Mini cards: Quests Completed, Current Streak, Focus Score, Life Score

### Section 4 — Active Arc
```
MONK ARC — 43% Complete
[Continue Arc →]
```

### Section 5 — Daily Quests
Quest cards with difficulty, XP, stat icon, complete button. Swipe support.

### Section 6 — Motivation Widget
Dynamic AI message: "You're strongest in Focus. Keep building momentum."

### Section 7 — Leaderboard Preview
Current rank + guild position + "View Full Rankings" CTA.

---

## Screen 7 — Quest Board

Tabs: Daily | Weekly | Custom

Quest Card Layout:
- Title
- XP Reward
- Difficulty badge
- Time estimate
- Stat affected
- Actions: Complete, Edit, Skip

---

## Screen 8 — Arc Mode

Cinematic hero screen:
- Arc Banner (full-width image)
- Progress percentage
- Current Phase name
- Milestone Timeline (✓ completed, ○ upcoming)
- Boss Section (progress bar)
- Skill Tree Preview
- Rewards Section

---

## Screen 9 — Skill Tree

Visual RPG tree:
```
Meditation → Deep Focus → Mind Mastery
```
- Locked nodes: grayed out
- Unlocked nodes: glowing
- Tap node: shows reward, buff, requirements

---

## Screen 10 — Level Up Screen

Full-screen celebration:
- Glow explosion animation
- "LEVEL UP" text
- "Level 13 Achieved"
- Rewards list (coins, title, cosmetic)
- **Continue Journey** button

---

## Screen 11 — Profile

Sections:
- Avatar card (customizable)
- Stats Radar Chart (Focus, Strength, Wisdom, Vitality, Discipline)
- Achievements list
- Titles history
- Activity history

---

## Screen 12 — Analytics

Cards:
- Weekly Progress graph
- Heatmap (daily activity)
- XP Growth chart
- Streak Trends
- Habit Consistency
- AI Insight: "You perform best before 9 PM."

---

## Screen 13 — Leaderboard

Tabs: Friends | Local | Global | Guild

User Row: Avatar, Level, XP, Rank
Top 3: Special gold/silver/bronze styling

---

## Screen 14 — Guild

Sections:
- Guild Chat (realtime)
- Shared Challenges
- Members list
- Guild Ranking

---

## Screen 15 — Premium

Premium card with messaging: "Unlock Your Strongest Self"

Features list:
- AI Coach
- Advanced Analytics
- Hard Mode
- Skill Reset

CTA: **Upgrade to Premium**

---

# MICROINTERACTIONS

Critical for retention:

| Action | Animation |
|--------|-----------|
| Quest Complete | XP flies upward + particle burst |
| Level Up | Full-screen glow celebration |
| Streak Milestone | Fire burst animation |
| Chest Opening | Loot reveal with suspense |
| Boss Defeat | Epic cinematic sequence |
| Stat Increase | Number counter + glow |

---

# MOBILE UX RULES

- Buttons minimum: 44px (thumb friendly)
- Bottom-heavy layout
- Most actions reachable one-handed
- Avoid tiny text
- Avoid complex forms
- Skeleton loading (no blank screens)
- Virtual scrolling for long lists

---

# RESPONSIVE STRATEGY

| Platform | Layout |
|----------|--------|
| Mobile | Primary (single column) |
| Tablet | Grid layout (2 columns) |
| Desktop | Dashboard layout with sidebar |

---

# ANIMATION SYSTEM

Libraries:
- Angular Animations (transitions)
- Lottie (complex animations)

### Animation Guidelines
- Quest completion: 300ms particle burst
- Level up: 1.5s full-screen celebration
- XP gain: 200ms fly-up number
- Streak flame: Continuous subtle animation
- Card interactions: 150ms scale + shadow

---

# OFFLINE UX

When offline:
- Show: "You're in Offline Mode. Progress will sync later."
- Allow quest completion (stored locally)
- Queue actions for sync
- Server wins on conflict (merge safely)

---

# ERROR UX

Avoid technical errors.

| Bad | Good |
|-----|------|
| "API failed" | "Could not load your Arc. Try again." |
| "Network error" | "You're in Offline Mode. Progress will sync later." |
| "500 Internal Server Error" | "Something went wrong. We're on it." |

---

*This document defines the complete visual and interaction design for Ascend.*
