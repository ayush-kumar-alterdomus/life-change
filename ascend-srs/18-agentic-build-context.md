# 18 — Agentic Build Context

# Ascend — Enter Arc Mode

---

## Purpose

This document provides context for AI-assisted development tools (Kiro, Copilot, Cursor, etc.) to understand the project structure, conventions, and build patterns.

---

## Project Identity

- **Product:** Ascend — Real Life RPG for Self-Improvement
- **Type:** Mobile-first gamified habit/productivity app
- **Frontend:** Angular 20+ / Ionic / Capacitor / Standalone Components
- **Backend:** Spring Boot (Java 21) / Modular Monolith
- **Database:** Firestore (realtime) + PostgreSQL (analytics/rankings)
- **Auth:** Firebase Authentication
- **Notifications:** Firebase Cloud Messaging
- **State:** Angular Signals + RxJS
- **Styling:** SCSS + Ionic Design System + Dark Theme

---

## Architecture Conventions

### Frontend Rules
1. All components are **standalone** (no NgModules)
2. Feature-first folder structure (not type-based)
3. Smart components (pages) contain logic; dumb components are reusable
4. Services handle API calls — never call HttpClient in components
5. State managed via Angular Signals (not NgRx)
6. Lazy-loaded routes for all features
7. Ionic components for mobile-first UI
8. Dark theme is default (orange-gold accent)

### Backend Rules
1. Clean Architecture per module (controller → service → repository)
2. All endpoints under `/api/v1/`
3. Firebase JWT validation on every request
4. Standard response format: `{ success, message, data }`
5. Custom exceptions with error codes
6. Async processing for non-critical operations
7. Redis caching for leaderboard and frequently accessed data
8. Scheduled jobs for daily resets, streak calculations, league resets

---

## Naming Conventions

### Frontend
- Pages: `feature-name.page.ts`
- Components: `component-name.component.ts`
- Services: `feature-name.service.ts`
- Stores: `feature-name.store.ts`
- Guards: `feature-name.guard.ts`
- Models: `feature-name.model.ts`
- Enums: `feature-name.enum.ts`

### Backend
- Controllers: `FeatureController.java`
- Services: `FeatureService.java`
- Repositories: `FeatureRepository.java`
- Entities: `Feature.java`
- DTOs: `FeatureDto.java`, `FeatureRequest.java`, `FeatureResponse.java`
- Mappers: `FeatureMapper.java`
- Schedulers: `FeatureScheduler.java`

---

## Key Domain Concepts

| Concept | Description |
|---------|-------------|
| Quest | A task/habit that awards XP when completed |
| Arc | A guided transformation journey (30–90 days) |
| XP | Experience points earned from quests |
| Level | User progression tier based on total XP |
| Streak | Consecutive days of quest completion |
| Combo | Multiplier that increases with streak length |
| Stat | RPG attribute (Strength, Focus, Wisdom, etc.) |
| League | Competitive tier (Bronze → Ascendant) |
| Guild | Social group for accountability |
| Boss | Major challenge requiring sustained effort |
| Skill Tree | Progressive unlock system within arcs |
| Life Score | Composite score of all improvement areas |

---

## Game Formulas (Reference)

### XP Calculation
```
FinalXP = BaseXP × DifficultyMultiplier × ComboMultiplier × ArcBonus
```

### Level Requirement
```
XP_Required = 100 × Level^1.5
```

### Combo Multiplier
```
ComboMultiplier = 1 + (0.01 × StreakDays)  // Cap: 2.0
```

### Life Score
```
LifeScore = 0.25(Discipline) + 0.2(Focus) + 0.2(Health) + 0.2(Learning) + 0.15(Consistency)
```

### Daily XP Cap
```
DailyCap = 1000 + (Level × 20)
```

### Burnout Risk
```
BurnoutRisk = (MissedQuests + StreakBreaks + DecliningActivity) / MotivationScore
```

---

## API Patterns

### Standard Request
```typescript
// Frontend service
completeQuest(questId: string): Observable<QuestCompleteResponse> {
  return this.http.post<ApiResponse<QuestCompleteResponse>>(
    `${environment.apiUrl}/quests/complete`,
    { questId }
  );
}
```

### Standard Response
```json
{
  "success": true,
  "message": "Quest completed!",
  "data": {
    "xpEarned": 50,
    "newLevel": false,
    "currentLevel": 12,
    "streak": 14
  }
}
```

---

## Component Patterns

### Smart Component (Page)
```typescript
@Component({
  standalone: true,
  selector: 'app-dashboard',
  templateUrl: './dashboard.page.html',
  imports: [CommonModule, IonicModule, XpCardComponent, QuestListComponent]
})
export class DashboardPage implements OnInit {
  private dashboardService = inject(DashboardService);
  private userStore = inject(UserStore);

  dashboardData = signal<DashboardData | null>(null);

  ngOnInit() {
    this.loadDashboard();
  }

  private loadDashboard() {
    this.dashboardService.getDashboard().subscribe(data => {
      this.dashboardData.set(data);
    });
  }
}
```

### Dumb Component (Reusable)
```typescript
@Component({
  standalone: true,
  selector: 'app-quest-card',
  templateUrl: './quest-card.component.html',
  imports: [CommonModule, IonicModule]
})
export class QuestCardComponent {
  @Input() quest!: Quest;
  @Output() complete = new EventEmitter<string>();
  @Output() edit = new EventEmitter<string>();
}
```

### Store Pattern
```typescript
@Injectable({ providedIn: 'root' })
export class QuestStore {
  quests = signal<Quest[]>([]);
  completed = computed(() => this.quests().filter(q => q.done));
  pending = computed(() => this.quests().filter(q => !q.done));

  setQuests(quests: Quest[]) { this.quests.set(quests); }
  completeQuest(id: string) {
    this.quests.update(qs => qs.map(q => q.id === id ? {...q, done: true} : q));
  }
}
```

---

## Testing Strategy

### Frontend
- Unit tests: Jasmine + Karma (components, services)
- E2E tests: Cypress or Playwright
- Focus on: quest completion flow, XP calculation, streak logic

### Backend
- Unit tests: JUnit 5 + Mockito
- Integration tests: Spring Boot Test + Testcontainers
- Focus on: XP engine, streak calculation, anti-cheat validation

---

## Environment Variables

### Frontend (environment.ts)
```typescript
firebase.apiKey
firebase.authDomain
firebase.projectId
apiUrl
```

### Backend (application.yml)
```yaml
firebase.credentials-path
spring.datasource.url
spring.redis.host
app.jwt.secret
app.xp.daily-cap
app.streak.recovery-hours
```

---

## Common Tasks for AI Agents

### When creating a new feature:
1. Create folder under `features/{feature-name}/`
2. Create page component (standalone)
3. Create service for API calls
4. Create store if state needed
5. Add route (lazy loaded)
6. Create corresponding Spring Boot module

### When creating an API endpoint:
1. Add to controller with proper annotations
2. Create DTO for request/response
3. Implement service logic
4. Add repository method if DB access needed
5. Add validation
6. Update API documentation

### When adding a game mechanic:
1. Define formula in this document
2. Implement calculation in Spring Boot engine
3. Create API endpoint
4. Build frontend visualization
5. Add animation/feedback
6. Test edge cases (anti-cheat)

---

## Critical Business Rules

1. **XP is NEVER calculated client-side** — always server-validated
2. **Quests can only complete once per day** — duplicate prevention
3. **Streak requires 80% daily quest completion** — not 100%
4. **Combo multiplier caps at 2.0** — prevents infinite scaling
5. **Daily XP cap prevents grinding** — healthy engagement
6. **Premium cannot buy leaderboard rank** — no pay-to-win
7. **Recovery mode activates automatically** — anti-burnout
8. **Frontend is untrusted** — all game logic server-side

---

*This document provides AI development tools with full context to assist in building Ascend.*
