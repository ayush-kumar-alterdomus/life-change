# 15 — Coding Blueprint

# Ascend — Enter Arc Mode

---

## Project Initialization

### Step 1 — Create Ionic Angular App

```bash
npm install -g @ionic/cli
ionic start ascend tabs --type=angular --capacitor --standalone
```

Options:
- ✅ Tabs navigation
- ✅ Standalone architecture
- ✅ Capacitor ready

---

### Step 2 — Install Core Dependencies

**Firebase:**
```bash
npm install firebase @angular/fire
```

**Charts:**
```bash
npm install apexcharts ng-apexcharts
```

**Animations:**
```bash
npm install lottie-web ngx-lottie
```

**Utilities:**
```bash
npm install dayjs uuid
```

**Storage:**
```bash
npm install @ionic/storage-angular
npm install @capacitor/preferences
```

**Push Notifications:**
```bash
npm install @capacitor/push-notifications
```

---

## Build Order (Critical)

Never randomly code screens. Build dependency-first.

### Phase A — Foundation

**Sprint 1: App runs + auth works**
```
Firebase setup → Theme setup → Routing → Bottom tabs → Auth → Guest mode
```
Folder: `core/auth`, `features/auth`
Deliverable: User can login/logout/continue as guest

**Sprint 2: Onboarding complete**
```
Goal Selection → Difficulty Selection → Assessment → Arc Recommendation
```
Folder: `features/onboarding`
Deliverable: User reaches dashboard

**Sprint 3: Dashboard UI**
```
XP card → streak card → daily quests → active arc
```
Folder: `features/dashboard`
Deliverable: Home screen alive

---

### Phase B — Core Gameplay

**Sprint 4: Quest engine**
```
Create quest → edit quest → complete quest → XP reward
```
Folder: `features/quests`
Deliverable: Core gameplay works

**Sprint 5: XP + leveling**
```
Level logic → XP animation → milestones
```
Folder: `features/xp`
Deliverable: Dopamine loop complete

**Sprint 6: Streak system**
```
Streak logic → combo system → comeback mode
```
Folder: `features/streaks`
Deliverable: Retention system active

---

### Phase C — Identity

**Sprint 7: Arc mode**
```
Monk Arc → Warrior Arc → Creator Arc → progress system
```
Folder: `features/arc-mode`
Deliverable: Identity progression

**Sprint 8: Analytics**
```
Heatmaps → weekly summary → graphs
```
Folder: `features/analytics`
Deliverable: Progress visibility

---

## Component Tree

### Dashboard Page
```
dashboard.page
├── header.component
├── xp-card.component
├── streak-card.component
├── stats-overview.component
├── active-arc.component
├── quest-list.component
│   └── quest-card.component
├── ai-widget.component
└── leaderboard-preview.component
```

### Quest Module
```
quests.page
├── daily-tab.component
├── weekly-tab.component
├── custom-tab.component
└── quest-card.component
```

### Arc Mode Module
```
arc-mode.page
├── arc-banner.component
├── milestone-timeline.component
├── skill-tree-preview.component
├── boss-card.component
└── rewards-section.component
```

### Profile Module
```
profile.page
├── avatar-card.component
├── stats-radar.component
├── achievement-list.component
├── title-history.component
└── settings-shortcut.component
```

---

## Reusable UI Components

### Build Early (Huge Time Saver)

**Core UI:**
```
app-button, app-card, app-loader, app-modal,
app-dialog, app-badge, app-progress
```

**Game UI:**
```
xp-progress-bar, level-badge, streak-flame,
reward-popup, quest-card, arc-card, boss-card,
achievement-card, guild-card, leaderboard-card
```

---

## State Management Structure

### User Store
```typescript
user, level, premium, avatar, league
```

### Quest Store
```typescript
dailyQuests, weeklyQuests, completedQuests
```

### XP Store
```typescript
xp, level, history
```

### Arc Store
```typescript
currentArc, progress, milestones
```

### Streak Store
```typescript
streakDays, comboMultiplier
```

### Example Store Implementation
```typescript
@Injectable({ providedIn: 'root' })
export class QuestStore {
  quests = signal<Quest[]>([]);

  completed = computed(() =>
    this.quests().filter(q => q.done)
  );

  completeQuest(id: string) {
    this.quests.update(quests =>
      quests.map(q =>
        q.id === id ? { ...q, done: true } : q
      )
    );
  }
}
```

---

## Routing Structure

```typescript
/auth
/onboarding
/home
/quests
/arc-mode
/analytics
/profile
/settings
```

### Protected Routes
```typescript
canActivate: [AuthGuard]
```

---

## Environment Config

```typescript
export const environment = {
  production: false,
  firebase: {
    apiKey: '',
    authDomain: '',
    projectId: '',
    storageBucket: '',
    messagingSenderId: '',
    appId: ''
  },
  apiUrl: 'http://localhost:8080/api/v1'
};
```

---

## Firebase Setup (app.config.ts)

```typescript
provideFirebaseApp(() => initializeApp(environment.firebase)),
provideAuth(() => getAuth()),
provideFirestore(() => getFirestore())
```

---

## Spring Boot Build Order

| Week | Module |
|------|--------|
| 1 | Auth module |
| 2 | Quest module |
| 3 | XP system |
| 4 | Streak engine |
| 5 | Arc progression |
| 6 | Analytics |
| 7 | Notifications |
| 8 | Premium |

---

## First Screen to Build

Do NOT start with login. Start with:

### Dashboard Mock UI

Reason: Motivation. Visual progress.

Then: Auth → Backend

### Recommended First Flow
```
Splash → Onboarding → Dashboard → Quest Complete → XP Animation
```

If this loop feels addictive: you're building correctly.

---

## Exact First Month Execution Plan

### Week 1 — Project Foundation
**Goal:** App runs with auth.

```
Tasks:
- Angular Ionic setup (standalone + tabs)
- Firebase project creation
- Spring Boot project initialization
- Theme setup (dark + orange-gold)
- Bottom tab navigation
- Firebase Auth integration
- Login/Signup/Guest mode
```

**Deliverable:** User can login and see themed app shell.

---

### Week 2 — Onboarding
**Goal:** User reaches dashboard.

```
Tasks:
- Welcome screen (cinematic)
- Goal selection (6 categories)
- Difficulty selection
- Personality assessment (3 questions)
- Arc recommendation algorithm
- Save onboarding state
```

**Deliverable:** User completes onboarding and gets arc recommendation.

---

### Week 3 — Dashboard
**Goal:** App feels real.

```
Tasks:
- Dashboard layout
- XP progress card
- Streak card with flame
- Active arc card
- Quest list (mock data initially)
- Connect to backend API
```

**Deliverable:** Home screen shows progress and daily quests.

---

### Week 4 — Core Loop
**Goal:** Addictive gameplay loop live.

```
Tasks:
- Quest completion flow
- XP calculation (server-side)
- Level-up detection + animation
- Streak update on completion
- Quest completion animation (XP fly-up)
```

**Deliverable:** Quest → XP → Level → Reward loop works end-to-end.

---

### MVP Validation Rule

Before building guilds, social, AI coach, or boss battles — first validate:

**Quest → XP → Level → Return Tomorrow**

If users love this core loop, everything else becomes easier to build on top.

---

## MVP Screen Build Order

1. Splash
2. Welcome
3. Goal Selection
4. Arc Recommendation
5. Dashboard
6. Quest Board
7. Quest Completion
8. XP Reward
9. Streak Card
10. Profile

Build social later.

---

## Key Implementation Patterns

### Service Pattern
```typescript
@Injectable({ providedIn: 'root' })
export class QuestService {
  private http = inject(HttpClient);

  getDailyQuests() {
    return this.http.get<Quest[]>('/api/v1/quests/daily');
  }

  completeQuest(questId: string) {
    return this.http.post('/api/v1/quests/complete', { questId });
  }
}
```

### Guard Pattern
```typescript
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.isAuthenticated() ? true : redirect('/auth/login');
};
```

### Interceptor Pattern
```typescript
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(AuthService).getToken();
  if (token) {
    req = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }
  return next(req);
};
```

---

*This document provides the exact implementation blueprint to start coding Ascend.*
