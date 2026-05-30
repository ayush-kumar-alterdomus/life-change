# Dashboard Aggregated API — Requirements, Design & Tasks

## Problem Statement

The frontend dashboard currently makes **6 parallel HTTP requests** on every load:

1. `GET /api/v1/users/me/summary` — user profile header
2. `GET /api/v1/xp/summary` — XP progress card
3. `GET /api/v1/stats/daily` — ❌ **does not exist** (daily quest stats)
4. `GET /api/v1/arcs/active` — active arc section
5. `GET /api/v1/quests/today` — quest list
6. `GET /api/v1/league/leaderboard` — leaderboard preview

This causes:
- **6 round-trips** on mobile (high latency on cellular)
- **N+1 auth token validations** per dashboard load
- **No server-side caching** opportunity for the composite view
- `GET /api/v1/stats/daily` doesn't exist — frontend gets a 404

**Solution:** A single `GET /api/v1/dashboard` endpoint that aggregates all dashboard data in one response.

---

## 1. Requirements

| ID | Requirement | Priority |
|----|-------------|----------|
| DASH-01 | Single endpoint returns all data needed to render the dashboard home screen | Critical |
| DASH-02 | Response includes: user summary, XP progress, daily quest stats, active arc, today's quests, streak info, unread notification count | Critical |
| DASH-03 | Leaderboard preview is **excluded** from the aggregated call (lazy-loaded separately below the fold) | Medium |
| DASH-04 | Response is cacheable with Redis (TTL 30s) to reduce DB load on rapid refreshes | Medium |
| DASH-05 | Endpoint completes within 200ms p95 under normal load | High |
| DASH-06 | If any sub-section fails, the endpoint still returns partial data with null for the failed section (graceful degradation) | High |
| DASH-07 | Frontend can optionally continue using individual endpoints for granular refresh (backward compatible) | Low |

---

## 2. API Design

### `GET /api/v1/dashboard`

**Auth:** Required (Firebase token via `@AuthenticationPrincipal FirebasePrincipal`)

**Response: 200 OK**

```json
{
  "success": true,
  "data": {
    "user": {
      "displayName": "alice",
      "level": 12,
      "avatarUrl": "https://...",
      "premium": false
    },
    "xp": {
      "totalXp": 4850,
      "level": 12,
      "xpToNextLevel": 150,
      "dailyXpEarned": 75,
      "dailyCap": 300,
      "comboMultiplier": 1.5
    },
    "streak": {
      "currentStreak": 14,
      "longestStreak": 21,
      "shieldAvailable": true,
      "comebackModeActive": false
    },
    "dailyStats": {
      "questsCompleted": 3,
      "questsTotal": 5,
      "completionPercentage": 60
    },
    "quests": [
      {
        "id": "uuid",
        "title": "Run 5km",
        "difficulty": "MEDIUM",
        "xpReward": 50,
        "completed": false,
        "statType": "ENDURANCE",
        "frequency": "DAILY"
      }
    ],
    "activeArc": {
      "id": "uuid",
      "name": "Fitness Warrior",
      "arcType": "FITNESS",
      "progressPercentage": 45,
      "currentPhase": "RISING_ACTION"
    },
    "notifications": {
      "unreadCount": 3
    }
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

**Null sections (graceful degradation):**
```json
{
  "success": true,
  "data": {
    "user": { ... },
    "xp": null,
    "streak": { ... },
    "dailyStats": { ... },
    "quests": null,
    "activeArc": null,
    "notifications": { "unreadCount": 0 }
  }
}
```

---

## 3. Backend Design

### 3.1 Module Structure

```
backend/src/main/java/com/ascend/
├── dashboard/
│   ├── controller/
│   │   └── DashboardController.java
│   ├── dto/
│   │   ├── DashboardResponse.java
│   │   ├── DashboardUserSection.java
│   │   ├── DashboardXpSection.java
│   │   ├── DashboardStreakSection.java
│   │   ├── DashboardDailyStatsSection.java
│   │   ├── DashboardArcSection.java
│   │   └── DashboardNotificationSection.java
│   └── service/
│       └── DashboardService.java
```

### 3.2 Service Design

`DashboardService` aggregates data from existing services — **no new repositories or entities needed**.

```java
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AuthService authService;
    private final XpService xpService;
    private final StreakService streakService;
    private final QuestService questService;
    private final ArcProgressService arcProgressService;
    private final NotificationService notificationService;
    private final LevelCalculator levelCalculator;
    private final StreakRepository streakRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(UUID userId) { ... }
}
```

**Key design decisions:**
- All reads in a single `@Transactional(readOnly = true)` — one DB connection, one transaction
- Each section wrapped in try/catch — partial failure returns `null` for that section
- No new DB queries — reuses existing service methods
- Optional Redis caching via `@Cacheable("dashboard")` with 30s TTL

### 3.3 Data Source Mapping

| Response Section | Source Service/Method | Existing? |
|-----------------|----------------------|-----------|
| `user` | `User` entity (already loaded by auth) | ✅ |
| `xp` | `XpService.getDailyXpEarned()` + `LevelCalculator.xpToNextLevel()` + `ComboCalculator` | ✅ |
| `streak` | `StreakService.getStreak()` | ✅ |
| `dailyStats` | `QuestService.getDailyQuests()` → extract counts | ✅ |
| `quests` | `QuestService.getDailyQuests()` → quest list | ✅ |
| `activeArc` | `ArcProgressService.getActiveArc()` | ✅ |
| `notifications` | `NotificationService.countUnread()` | ✅ (just added) |

### 3.4 Caching Strategy

```java
@Cacheable(value = "dashboard", key = "#userId", unless = "#result == null")
public DashboardResponse getDashboard(UUID userId) { ... }
```

- **TTL:** 30 seconds (configured in Redis config)
- **Eviction:** On quest completion, XP gain, streak update (via `@CacheEvict`)
- **Rationale:** Dashboard is read-heavy; 30s staleness is acceptable for a gamified app

---

## 4. Frontend Impact

### 4.1 Current State (6 calls)

```typescript
// dashboard.service.ts — current
getUserSummary()        → GET /users/me/summary
getXpProgress()         → GET /xp/summary
getDailyStats()         → GET /stats/daily        ← 404!
getActiveArc()          → GET /arcs/active
getTodayQuests()        → GET /quests/today
getLeaderboardPreview() → GET /league/leaderboard  ← stays separate (lazy)
```

### 4.2 Target State (1 call + 1 lazy)

```typescript
// dashboard.service.ts — new
getDashboard()          → GET /dashboard           ← single aggregated call
getLeaderboardPreview() → GET /league/leaderboard  ← lazy, below fold
```

### 4.3 Frontend Migration

- Add `getDashboard()` method to `DashboardService`
- Update `DashboardStore.fetchAllSections()` to use single call
- Map response sections to existing signal state
- Keep individual methods for `retryErroredSections()` fallback
- Remove `getDailyStats()` (was 404 anyway)

---

## 5. Tasks

### Phase 1: Backend — DTO Layer

| Task | Description | Estimate |
|------|-------------|----------|
| DASH-T1 | Create `dashboard/` module directory structure | 0.25h |
| DASH-T2 | Create `DashboardResponse` record (top-level response DTO) | 0.25h |
| DASH-T3 | Create `DashboardUserSection` record (displayName, level, avatarUrl, premium) | 0.25h |
| DASH-T4 | Create `DashboardXpSection` record (totalXp, level, xpToNextLevel, dailyXpEarned, dailyCap, comboMultiplier) | 0.25h |
| DASH-T5 | Create `DashboardStreakSection` record (currentStreak, longestStreak, shieldAvailable, comebackModeActive) | 0.25h |
| DASH-T6 | Create `DashboardDailyStatsSection` record (questsCompleted, questsTotal, completionPercentage) | 0.25h |
| DASH-T7 | Create `DashboardArcSection` record (id, name, arcType, progressPercentage, currentPhase) | 0.25h |
| DASH-T8 | Create `DashboardNotificationSection` record (unreadCount) | 0.1h |

### Phase 2: Backend — Service Layer

| Task | Description | Estimate |
|------|-------------|----------|
| DASH-T9 | Create `DashboardService` with `getDashboard(UUID userId)` method | 1.5h |
| DASH-T10 | Implement graceful degradation (try/catch per section, return null on failure) | 0.5h |
| DASH-T11 | Add `@Cacheable` with Redis 30s TTL | 0.5h |
| DASH-T12 | Add `@CacheEvict` listeners on quest completion, XP gain, streak events | 0.5h |

### Phase 3: Backend — Controller Layer

| Task | Description | Estimate |
|------|-------------|----------|
| DASH-T13 | Create `DashboardController` with `GET /api/v1/dashboard` | 0.5h |

### Phase 4: Backend — Tests

| Task | Description | Estimate |
|------|-------------|----------|
| DASH-T14 | Unit tests for `DashboardService` (happy path, partial failure, null arc) | 1.5h |
| DASH-T15 | Unit tests for `DashboardController` | 0.5h |
| DASH-T16 | Integration test: verify single call returns all sections | 1h |

### Phase 5: Frontend — Migration

| Task | Description | Estimate |
|------|-------------|----------|
| DASH-T17 | Add `getDashboard(): Observable<DashboardResponse>` to `DashboardService` | 0.25h |
| DASH-T18 | Update `DashboardStore.fetchAllSections()` to use aggregated endpoint | 0.5h |
| DASH-T19 | Update `DashboardStore.refreshDashboard()` to use aggregated endpoint | 0.25h |
| DASH-T20 | Keep individual fetch methods as fallback for `retryErroredSections()` | 0.25h |
| DASH-T21 | Remove dead `getDailyStats()` call (was 404) | 0.1h |
| DASH-T22 | Update `dashboard.models.ts` to match new response shape | 0.25h |

---

## 6. Implementation Priority

### Sprint 1 (Critical Path)
1. DASH-T1→T8 — DTOs
2. DASH-T9→T10 — Service with graceful degradation
3. DASH-T13 — Controller
4. DASH-T14→T15 — Tests

### Sprint 2 (Optimization + Frontend)
5. DASH-T11→T12 — Redis caching
6. DASH-T17→T22 — Frontend migration

---

## 7. Acceptance Criteria

- [ ] `GET /api/v1/dashboard` returns 200 with all 7 sections populated for a user with active data
- [ ] If user has no active arc, `activeArc` is `null` (not an error)
- [ ] If streak service throws, response still returns with `streak: null` and other sections intact
- [ ] Response time < 200ms p95 (measured with all services healthy)
- [ ] After quest completion, next dashboard call reflects updated `dailyStats` and `xp`
- [ ] Frontend loads dashboard with 1 HTTP call instead of 6 (verified in Network tab)
- [ ] `GET /api/v1/stats/daily` 404 is eliminated (frontend no longer calls it)
- [ ] Existing individual endpoints remain functional (backward compatible)

---

## 8. Non-Functional Requirements

| Concern | Requirement |
|---------|-------------|
| Auth | Firebase token required |
| Caching | Redis, 30s TTL, evicted on state-changing events |
| Performance | Single DB transaction, read-only, no N+1 queries |
| Resilience | Partial failure → null section, not 500 |
| Response Size | ~2-4 KB typical (acceptable for mobile) |
| Rate Limit | Standard API rate limit (60/min) |
| Backward Compat | Individual endpoints remain unchanged |

---

## 9. Sequence Diagram

```
Mobile App                    DashboardController         DashboardService          Existing Services
    │                                │                          │                         │
    │── GET /api/v1/dashboard ──────►│                          │                         │
    │                                │── getDashboard(userId) ──►│                         │
    │                                │                          │── getUser() ───────────►│
    │                                │                          │◄── User ────────────────│
    │                                │                          │── getDailyXpEarned() ──►│
    │                                │                          │◄── long ────────────────│
    │                                │                          │── getStreak() ─────────►│
    │                                │                          │◄── StreakResponse ──────│
    │                                │                          │── getDailyQuests() ────►│
    │                                │                          │◄── DailyQuestsResponse ─│
    │                                │                          │── getActiveArc() ──────►│
    │                                │                          │◄── ArcProgressResponse ─│
    │                                │                          │── countUnread() ────────►│
    │                                │                          │◄── long ────────────────│
    │                                │                          │                         │
    │                                │◄── DashboardResponse ────│                         │
    │◄── 200 OK (JSON) ─────────────│                          │                         │
    │                                │                          │                         │
```

---

## 10. Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Service timeout cascades | Low | High | Per-section timeout (500ms) + graceful null |
| Cache staleness after quest complete | Medium | Low | `@CacheEvict` on QuestCompletedEvent |
| Response payload too large | Low | Low | Exclude quest descriptions; limit quest list to 10 |
| Breaking frontend during migration | Medium | Medium | Feature flag: frontend tries `/dashboard` first, falls back to individual calls |
