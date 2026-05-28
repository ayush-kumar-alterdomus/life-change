# 07 — Angular Ionic Architecture

# Ascend — Enter Arc Mode

---

## Architecture Philosophy

Optimized for:
- ✅ Scalability
- ✅ Maintainability
- ✅ Mobile-first UX
- ✅ Performance
- ✅ Lazy loading
- ✅ Reusable UI system
- ✅ Standalone components
- ✅ Enterprise-grade structure

Intentionally avoids:
- ❌ Messy component structure
- ❌ God services
- ❌ Giant app.module chaos
- ❌ Type-based architecture

---

## Architecture Style

# Feature-First Architecture (Domain-Driven Frontend)

---

## Frontend Stack

| Technology | Purpose |
|-----------|---------|
| Angular 20+ | Framework |
| Ionic Angular | UI Components |
| Capacitor | Native mobile builds |
| Signals + RxJS | State management |
| SCSS | Styling |
| ApexCharts | Data visualization |
| Lottie | Animations |
| Ionic Secure Storage | Local persistence |
| FCM + Capacitor Push | Notifications |

---

## Standalone Components Architecture

No NgModules. Angular modern best practice.

```typescript
@Component({
  standalone: true,
  selector: 'app-quest-card',
  templateUrl: './quest-card.component.html',
  imports: [CommonModule, IonicModule]
})
export class QuestCardComponent {}
```

---

## Frontend Data Flow Architecture

### State Flow Pattern
```
UI (Template)
    ↓
Component (Smart/Page)
    ↓
Store (Angular Signals)
    ↓
Service (API Layer)
    ↓
HTTP Client (with interceptors)
    ↓
Spring Boot API
    ↓
Database
    ↓
Response
    ↓
Store Update (signal.set/update)
    ↓
Reactive UI Refresh (automatic via signals)
```

### Example: Complete Quest Flow
```
Quest Card "Complete" Button Click
    ↓
QuestStore.completeQuest(id)
    ↓
QuestService.complete(id)
    ↓
POST /api/v1/quests/complete
    ↓
Spring Boot validates + calculates XP
    ↓
Response: { xpEarned: 50, newLevel: false, streak: 14 }
    ↓
XpStore.addXp(50)
    ↓
StreakStore.update(14)
    ↓
Dashboard auto-refreshes (computed signals)
    ↓
XP Animation triggered
```

### Key Principle
- Components never call HTTP directly
- Stores hold reactive state (signals)
- Services handle API communication
- Interceptors handle auth tokens and errors transparently

---

## Smart vs Dumb Components

### Smart Components (Pages)
Contain API calls, state logic, orchestration:
- `dashboard.page`
- `quest-board.page`
- `arc-mode.page`
- `guild.page`
- `profile.page`

### Dumb Components (Reusable)
No business logic, only emit events:
- `quest-card.component`
- `xp-bar.component`
- `level-badge.component`
- `streak-card.component`
- `reward-popup.component`
- `arc-card.component`

---

## Routing Architecture (Lazy Loaded)

```typescript
export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/dashboard/dashboard.page')
  },
  {
    path: 'quests',
    loadChildren: () => import('./features/quests/routes')
  },
  {
    path: 'arc-mode',
    loadChildren: () => import('./features/arc-mode/routes')
  },
  {
    path: 'guilds',
    loadChildren: () => import('./features/guilds/routes')
  }
];
```

---

## Mobile Navigation Architecture

### Tabs + Stack Hybrid

Bottom Navigation:
```
🏠 Home    ⚔ Quests    🔥 Arc Mode    👥 Social    👤 Profile
```

Deep Navigation (Stack):
```
Home → Quest Details → Quest Completion → Reward Screen
```

---

## Global State Management

### Signals + RxJS Hybrid

Global State:
```typescript
user, xp, level, quests, currentArc, streak, guild, league, premium, notifications, theme
```

### Store Structure
```
state/
├── user.store.ts
├── quest.store.ts
├── xp.store.ts
├── guild.store.ts
├── streak.store.ts
└── notification.store.ts
```

### Example Store
```typescript
@Injectable()
export class UserStore {
  user = signal<User | null>(null);

  level = computed(() => calculateLevel(this.user()?.xp));

  setUser(user: User) {
    this.user.set(user);
  }
}
```

---

## API Layer Architecture

Rule: Never call HttpClient directly inside page.

```
Page → Service → API Client
```

### Service Structure
```
services/
├── auth.service.ts
├── quest.service.ts
├── xp.service.ts
├── guild.service.ts
└── analytics.service.ts
```

### Example
```typescript
completeQuest(id: string) {
  return this.http.post('/quests/complete', { questId: id });
}
```

---

## Interceptor Architecture

### Auth Interceptor
Attach Firebase JWT: `Authorization: Bearer TOKEN`

### Error Interceptor
Centralized error handling. 401 → Auto logout.

### Loading Interceptor
Show loading overlay during API calls.

### Retry Interceptor
Retry failed network requests (max 3 attempts).

---

## Guard System

### Auth Guard
Protect routes requiring authentication.

### Premium Guard
Premium-only pages (AI Coach, advanced analytics).

### Onboarding Guard
User must complete onboarding before accessing main app.

---

## UI Component System

### Base Components
```
button, card, modal, toast, badge, loader, dialog, tabs
```

### Game Components
```
xp-bar, quest-card, arc-card, boss-card, streak-flame,
reward-popup, skill-tree-node, achievement-card, guild-card, leaderboard-card
```

---

## Theme Architecture

### Supported Themes
- Dark Mode (Default)
- Light Mode (Optional)

### Theme Variables
```scss
--primary: #ff8c00;
--secondary: #1f1f1f;
--accent: #9c27b0;
--success: #4caf50;
--error: #f44336;
--background: #0a0a0a;
--card: #161616;
```

Theme preference stored locally. Auto-sync with device theme.

---

## Animation System

Libraries: Angular Animations + Lottie

| Action | Animation |
|--------|-----------|
| Quest Completion | XP flies upward |
| Level Up | Full-screen glow |
| Streak Milestone | Fire animation |
| Loot Chest | Opening animation |

---

## Offline Mode

Strategy:
1. Complete quests offline → local save
2. Internet restored → sync API
3. Conflict resolution: server wins (merge safely)

Queue actions locally using Ionic Storage.

---

## Performance Strategy

Target: 60 FPS

Optimizations:
- Virtual scrolling (leaderboard)
- Lazy images (avatars)
- Skeleton loading (avoid blank screens)
- Cached API responses
- OnPush change detection
- TrackBy for ngFor loops

---

## Error UX Strategy

| Scenario | Message |
|----------|---------|
| API failed | "Could not load your Arc. Try again." |
| Offline | "You're in Offline Mode. Progress will sync later." |
| Auth expired | "Session expired. Please login again." |

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

## Screen Inventory

### Authentication
Splash, Login, Signup, Forgot Password, Guest Entry

### Onboarding
Welcome, Goal Selection, Difficulty Selection, Assessment Quiz, Arc Recommendation, Avatar Selection

### Core Gameplay
Dashboard, Quest Board, Quest Details, Arc Details, Skill Tree, Boss Battle, Rewards, Level Up

### Social
Guilds, Guild Chat, Leaderboard, Friend Profile, Challenges

### Premium
Subscription, Premium Benefits, Upgrade Flow

### Profile
Achievements, Stats, History, Customization

---

*This document defines the complete Angular Ionic frontend engineering architecture.*
