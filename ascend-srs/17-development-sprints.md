# 17 — Development Sprints

# Ascend — Enter Arc Mode

---

## Sprint Philosophy

- Each sprint: 1–2 weeks
- Each sprint must increase **retention**
- Ship ugly if needed — polish later
- Priority: Working loop > Perfect UI

---

## MVP Sprint Plan (12 Sprints)

---

### Sprint 1 — Project Setup & Foundation
**Duration:** 1 week

**Tasks:**
- [ ] Create Ionic Angular project with standalone architecture
- [ ] Configure Capacitor for Android/iOS
- [ ] Set up Firebase project (Auth, Firestore, FCM)
- [ ] Create Spring Boot project with modular structure
- [ ] Configure Firebase Admin SDK in Spring Boot
- [ ] Set up environment configurations (dev/prod)
- [ ] Implement dark theme with orange-gold accent
- [ ] Set up bottom tab navigation (5 tabs)
- [ ] Configure CI/CD pipeline (GitHub Actions)
- [ ] Create shared UI component library (button, card, loader)

**Deliverable:** App launches with themed UI and navigation.

---

### Sprint 2 — Authentication
**Duration:** 1 week

**Tasks:**
- [ ] Implement Firebase Google Sign-In
- [ ] Implement Email/Password registration
- [ ] Implement Guest Mode (anonymous auth)
- [ ] Create auth interceptor (attach JWT to requests)
- [ ] Create auth guard (protect routes)
- [ ] Build login page UI
- [ ] Build signup page UI
- [ ] Implement session persistence
- [ ] Create Spring Boot auth validation endpoint
- [ ] Create user profile on first login (Firestore + backend)

**Deliverable:** User can login/signup/continue as guest.

---

### Sprint 3 — RPG Onboarding
**Duration:** 1 week

**Tasks:**
- [ ] Build welcome screen (cinematic intro)
- [ ] Build goal selection screen (6 categories)
- [ ] Build difficulty selection screen (Casual/Balanced/Beast)
- [ ] Build personality assessment (3–5 questions)
- [ ] Implement arc recommendation algorithm
- [ ] Build arc recommendation screen
- [ ] Save onboarding state to prevent re-showing
- [ ] Create onboarding guard
- [ ] Connect onboarding to user profile

**Deliverable:** User completes onboarding and gets arc recommendation.

---

### Sprint 4 — Dashboard
**Duration:** 1.5 weeks

**Tasks:**
- [ ] Build dashboard page layout
- [ ] Create XP progress card component
- [ ] Create streak card component (with flame animation)
- [ ] Create daily summary cards (quests, focus, life score)
- [ ] Create active arc card
- [ ] Create quest list component (today's quests)
- [ ] Create motivation widget (static initially)
- [ ] Build dashboard API endpoint (Spring Boot)
- [ ] Connect dashboard to backend data
- [ ] Implement pull-to-refresh

**Deliverable:** Home screen shows user progress and daily quests.

---

### Sprint 5 — Quest Engine
**Duration:** 2 weeks

**Tasks:**
- [ ] Build quest board page with tabs (Daily/Weekly/Custom)
- [ ] Create quest card component (title, XP, difficulty, stat)
- [ ] Implement quest completion flow (tap → confirm → reward)
- [ ] Build quest creation form (custom quests)
- [ ] Implement quest editing and deletion
- [ ] Create quest completion API (Spring Boot)
- [ ] Implement duplicate completion prevention
- [ ] Implement recurring quest logic
- [ ] Add XP reward calculation on completion
- [ ] Create quest completion animation (XP fly-up)
- [ ] Implement offline quest completion (queue + sync)

**Deliverable:** Core gameplay loop works — complete quests, earn XP.

---

### Sprint 6 — XP & Leveling System
**Duration:** 1.5 weeks

**Tasks:**
- [ ] Implement XP calculation engine (Spring Boot)
- [ ] Implement level progression formula (100 × Level^1.5)
- [ ] Create level-up detection logic
- [ ] Build level-up celebration screen (full-screen animation)
- [ ] Implement XP history tracking
- [ ] Create XP summary API endpoint
- [ ] Build XP progress bar with animation
- [ ] Implement difficulty multiplier
- [ ] Implement daily XP cap
- [ ] Create level badge component

**Deliverable:** Users level up with satisfying animations.

---

### Sprint 7 — Streak Engine
**Duration:** 1 week

**Tasks:**
- [ ] Implement daily streak calculation (Spring Boot scheduler)
- [ ] Implement combo multiplier formula
- [ ] Build streak display component (flame + counter)
- [ ] Implement comeback mode (48-hour recovery window)
- [ ] Create streak warning notification
- [ ] Build streak broken modal (shame-free messaging)
- [ ] Implement streak API endpoints
- [ ] Add streak data to dashboard

**Deliverable:** Streak system creates daily return motivation.

---

### Sprint 8 — Arc Mode
**Duration:** 2 weeks

**Tasks:**
- [ ] Create prebuilt arcs data (Monk, Warrior, Creator)
- [ ] Build arc list page
- [ ] Build arc detail page (banner, milestones, progress)
- [ ] Implement arc start/join flow
- [ ] Build milestone timeline component
- [ ] Implement arc progression tracking
- [ ] Create arc progress API endpoints
- [ ] Implement phase transitions
- [ ] Build arc identity titles (Beginner → Master)
- [ ] Connect arc quests to quest engine

**Deliverable:** Users can enter and progress through arcs.

---

### Sprint 9 — Analytics
**Duration:** 1 week

**Tasks:**
- [ ] Build analytics dashboard page
- [ ] Create weekly progress chart (ApexCharts)
- [ ] Create activity heatmap component
- [ ] Implement weekly report generation (Spring Boot)
- [ ] Create stat trends visualization
- [ ] Build life score card
- [ ] Implement life score formula
- [ ] Create weekly report API endpoint

**Deliverable:** Users can see their progress and trends.

---

### Sprint 10 — Notifications
**Duration:** 1 week

**Tasks:**
- [ ] Configure FCM in Capacitor
- [ ] Implement push notification service (Spring Boot)
- [ ] Create quest reminder notifications
- [ ] Create streak warning notifications (high priority)
- [ ] Implement smart notification timing
- [ ] Build in-app notification list
- [ ] Create notification preferences (settings)
- [ ] Implement notification rate limiting (max 5/day)

**Deliverable:** Users receive timely reminders and alerts.

---

### Sprint 11 — Premium & Monetization
**Duration:** 1 week

**Tasks:**
- [ ] Build premium benefits page
- [ ] Create subscription paywall UI
- [ ] Implement feature gating logic
- [ ] Create premium guard for protected features
- [ ] Set up payment integration (Stripe/Razorpay)
- [ ] Implement 7-day free trial
- [ ] Create subscription status API
- [ ] Build upgrade prompts (non-aggressive)

**Deliverable:** Monetization system ready.

---

### Sprint 12 — Polish & Launch Prep
**Duration:** 1.5 weeks

**Tasks:**
- [ ] Add Lottie animations (quest complete, level up, streak)
- [ ] Optimize performance (lazy loading, virtual scroll)
- [ ] Implement skeleton loading screens
- [ ] Fix responsive issues
- [ ] Add haptic feedback (vibration on actions)
- [ ] Bug fixes and edge cases
- [ ] App Store / Play Store listing preparation
- [ ] Create landing page
- [ ] Final testing on devices
- [ ] Deploy to production

**Deliverable:** MVP ready for launch.

---

## Post-MVP Sprints (V1)

### Sprint 13–14 — Leagues & Leaderboard
- League system implementation
- Weekly ranking calculation
- Promotion/demotion logic
- Leaderboard UI

### Sprint 15–16 — Guilds
- Guild creation/joining
- Shared challenges
- Guild chat (WebSocket)
- Guild leaderboard

### Sprint 17–18 — Boss Battles & Skill Trees
- Boss battle system
- Multi-stage progression
- Skill tree visualization
- Passive buff system

### Sprint 19–20 — Social Features
- Friend system
- Social feed
- Challenges
- Accountability partners

### Sprint 21–22 — AI Coach
- Behavior analysis
- Burnout detection
- Adaptive difficulty
- Smart recommendations

### Sprint 23–24 — Seasonal Events
- Season system
- Seasonal challenges
- Exclusive rewards
- Season reset logic

---

## Estimated Timeline Summary

| Phase | Sprints | Duration |
|-------|---------|----------|
| MVP | 1–12 | 8–12 weeks |
| V1 (Social + Competition) | 13–20 | 8–10 weeks |
| V2 (AI + Premium) | 21–24 | 4–6 weeks |
| Total to Production | 24 sprints | ~6–7 months |

---

## Sprint Velocity Assumptions

- Solo developer
- 4–6 hours/day coding
- Weekends for planning/design
- Buffer for bugs and unexpected issues

---

*This document provides the complete sprint-by-sprint development plan for Ascend.*
