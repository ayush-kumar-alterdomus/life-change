# Ascend App — Spec Execution Order

## Overview

This document defines the recommended execution order for all 21 feature specs. Specs are grouped into phases with dependencies clearly marked. Execute specs within a phase sequentially (top to bottom). Do not start a phase until the previous phase is complete.

---

## Phase 1: Foundation

These specs establish the project skeleton, database, and security layer. Everything else depends on them.

| Order | Spec | Description | Dependencies |
|-------|------|-------------|--------------|
| 1 | `project-setup` | Spring Boot + Angular/Ionic scaffolding, Docker Compose, CI | None |
| 2 | `database-schema` | Flyway migrations, JPA entities, repositories, seed data | project-setup |
| 3 | `auth-system` | Firebase auth, JWT filter, RBAC, rate limiting, guest mode | project-setup, database-schema |

**Phase 1 Deliverable:** Both apps compile, database runs with all tables, auth flow works end-to-end.

---

## Phase 2: Core Gameplay Loop

The fundamental game mechanics that drive daily engagement. These form the heart of the app.

| Order | Spec | Description | Dependencies |
|-------|------|-------------|--------------|
| 4 | `quest-system` | Quest CRUD, completion, daily reset, custom quests | Phase 1 |
| 5 | `xp-engine` | XP calculation, daily cap, level-up, prestige, stat gains | Phase 1, quest-system |
| 6 | `streak-system` | Streak tracking, combo multiplier, shields, comeback mode | Phase 1, quest-system |

**Phase 2 Deliverable:** Users can complete quests, earn XP, level up, and maintain streaks. The core loop is playable.

---

## Phase 3: Progression Systems

Long-term progression mechanics that give users goals beyond daily quests.

| Order | Spec | Description | Dependencies |
|-------|------|-------------|--------------|
| 7 | `arc-mode` | Guided growth journeys, milestones, phases, recommendations | Phase 2 |
| 8 | `character-stats` | Six RPG stats, stat gains, decay, life score, identity titles | Phase 2 (xp-engine) |
| 9 | `skill-tree` | Skill nodes, prerequisites, passive XP buffs, reset | Phase 2, arc-mode |

**Phase 3 Deliverable:** Users can follow structured Arcs, grow character stats, and unlock skill buffs.

---

## Phase 4: Competition & Social

Multiplayer features that add social accountability and competitive motivation.

| Order | Spec | Description | Dependencies |
|-------|------|-------------|--------------|
| 10 | `league-system` | Tiers, league score, weekly promotion/demotion, anti-cheat | Phase 2 (xp-engine, levels) |
| 11 | `guild-system` | Guild CRUD, shared quests, chat (WebSocket), rankings | Phase 2, realtime-communication (partial) |
| 12 | `boss-battles` | Multi-stage bosses, progress from quests, guild bosses | Phase 2, guild-system (for guild bosses) |

**Phase 4 Deliverable:** Users compete in leagues, collaborate in guilds, and fight bosses together.

---

## Phase 5: Intelligence & Monetization

Smart features and revenue systems.

| Order | Spec | Description | Dependencies |
|-------|------|-------------|--------------|
| 13 | `premium-system` | Subscription tiers, trial, feature gating, downgrade | Phase 1 (auth-system for roles) |
| 14 | `notification-engine` | FCM push notifications, daily cap, streak alerts | Phase 1, streak-system |
| 15 | `ai-coach` | Burnout detection, adaptive difficulty, recommendations | Phase 2, Phase 3, premium-system |
| 16 | `reward-economy` | Coins, gems, loot chests, achievements, cosmetics | Phase 2 (quest-system, xp-engine) |

**Phase 5 Deliverable:** Premium monetization works, notifications keep users engaged, AI coach prevents burnout, reward economy motivates.

---

## Phase 6: Analytics & Administration

Insights for users and management tools for operators.

| Order | Spec | Description | Dependencies |
|-------|------|-------------|--------------|
| 17 | `analytics-insights` | Dashboard, weekly reports, heatmaps, life score, correlations | Phase 2, Phase 3 (character-stats) |
| 18 | `admin-panel` | CMS for arcs/quests, moderation, system analytics, events | Phase 1 (auth RBAC), database-schema |

**Phase 6 Deliverable:** Users see progress analytics, admins can manage content and moderate users.

---

## Phase 7: Polish & Real-Time

Final features that make the app feel alive and work seamlessly offline.

| Order | Spec | Description | Dependencies |
|-------|------|-------------|--------------|
| 19 | `realtime-communication` | WebSocket STOMP, private channels, presence, leaderboard broadcast | Phase 1, guild-system |
| 20 | `social-features` | Friends, challenges, activity feed, accountability partners | Phase 2, notification-engine |
| 21 | `offline-sync` | Offline queue, conflict resolution, Firestore persistence, sync UI | Phase 2 (quest-system) |

**Phase 7 Deliverable:** App feels responsive with real-time updates, works offline, and has full social features.

---

## Dependency Graph

```
Phase 1: project-setup → database-schema → auth-system
              │                  │               │
              ▼                  ▼               ▼
Phase 2: quest-system → xp-engine → streak-system
              │              │            │
              ▼              ▼            ▼
Phase 3: arc-mode → character-stats → skill-tree
              │              │
              ▼              ▼
Phase 4: league-system → guild-system → boss-battles
                              │
Phase 5: premium-system → notification-engine → ai-coach → reward-economy
                              │
Phase 6: analytics-insights → admin-panel
                              │
Phase 7: realtime-communication → social-features → offline-sync
```

---

## Execution Commands

To execute a spec, use:
```
"run all tasks for {spec-name}"
```

Example sequence:
```
run all tasks for project-setup
run all tasks for database-schema
run all tasks for auth-system
run all tasks for quest-system
...
```

---

## Estimated Effort

| Phase | Specs | Estimated Time |
|-------|-------|---------------|
| Phase 1 | 3 specs | 2-3 days |
| Phase 2 | 3 specs | 3-4 days |
| Phase 3 | 3 specs | 2-3 days |
| Phase 4 | 3 specs | 3-4 days |
| Phase 5 | 4 specs | 3-4 days |
| Phase 6 | 2 specs | 1-2 days |
| Phase 7 | 3 specs | 2-3 days |
| **Total** | **21 specs** | **~16-23 days** |

---

## Notes

- Each spec is self-contained with its own tasks.md
- Specs within the same phase can sometimes be parallelized (e.g., league-system and guild-system)
- Always run checkpoints at the end of each spec to verify everything works
- Property-based tests are included in relevant specs — don't skip them
- The `ascend-app` spec in `.kiro/specs/ascend-app/` contains the master requirements and design documents for reference
