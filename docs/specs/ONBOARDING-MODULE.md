# Onboarding Module — Requirements, Design & Tasks

## Current State Analysis

### Frontend ✅ Complete
The frontend onboarding flow is **fully implemented**:
- 5-step wizard: Goals → Difficulty → Quiz → Arc Recommendation → Avatar
- Signal-based store with persistence (survives app kill)
- Personality computation engine (pure, deterministic)
- Arc recommendation engine (goal affinities × difficulty multipliers × personality bias)
- Route guard (prevents re-entry after completion)
- Submits payload to `PUT /api/v1/users/onboarding`

### Backend ❌ Incomplete
The backend `PUT /api/v1/users/onboarding` endpoint exists but only:
- Saves `avatarUrl` and `hardMode` flag
- Logs the goals/difficulty/arc but **does not persist them**
- Does NOT start the recommended arc
- Does NOT save personality type
- Does NOT save selected goals
- Does NOT mark onboarding as complete on the user entity

**Result:** After onboarding, the user's goals, personality, and arc selection are lost. The frontend stores them locally but the backend has no record.

---

## 1. Requirements

| ID | Requirement | Priority |
|----|-------------|----------|
| ONB-01 | Backend persists selected goals on the user profile | Critical |
| ONB-02 | Backend persists personality type on the user profile | Critical |
| ONB-03 | Backend persists difficulty preference on the user profile | Critical |
| ONB-04 | Backend auto-starts the selected arc for the user after onboarding | Critical |
| ONB-05 | Backend marks user as `onboardingComplete = true` | Critical |
| ONB-06 | Backend returns onboarding status via `GET /api/v1/users/me/onboarding-status` | High |
| ONB-07 | Backend validates that onboarding can only be completed once (idempotent) | High |
| ONB-08 | If arc start fails, onboarding still completes (arc is optional) | Medium |
| ONB-09 | Backend returns the started arc info in the onboarding response | Medium |

---

## 2. API Design

### `PUT /api/v1/users/onboarding` (Enhanced)

**Request Body:** (unchanged from frontend)
```json
{
  "selectedGoals": ["fitness", "mindfulness", "learning"],
  "difficulty": "balanced",
  "personalityType": "disciplined",
  "selectedArc": "warrior",
  "selectedAvatar": "avatar_knight_01"
}
```

**Response: 200 OK** (enhanced)
```json
{
  "success": true,
  "message": "Onboarding completed",
  "data": {
    "level": 1,
    "arcStarted": true,
    "arcId": "uuid-of-started-arc",
    "arcName": "Fitness Warrior"
  }
}
```

**Error Responses:**
| Status | Code | Condition |
|--------|------|-----------|
| 400 | VALIDATION_FAILED | Missing required fields |
| 409 | ONBOARDING_ALREADY_COMPLETE | User already completed onboarding |

---

### `GET /api/v1/users/me/onboarding-status` (New)

**Response: 200 OK**
```json
{
  "success": true,
  "data": {
    "complete": true,
    "selectedGoals": ["fitness", "mindfulness", "learning"],
    "personalityType": "disciplined",
    "difficulty": "balanced"
  }
}
```

If not completed:
```json
{
  "success": true,
  "data": {
    "complete": false,
    "selectedGoals": null,
    "personalityType": null,
    "difficulty": null
  }
}
```

---

## 3. Backend Design

### 3.1 User Entity Changes

Add columns to the `users` table:

```sql
ALTER TABLE users ADD COLUMN onboarding_complete BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE users ADD COLUMN selected_goals JSONB;
ALTER TABLE users ADD COLUMN personality_type VARCHAR(30);
ALTER TABLE users ADD COLUMN difficulty_preference VARCHAR(20);
```

```java
// User.java — new fields
@Builder.Default
@Column(name = "onboarding_complete", nullable = false)
private Boolean onboardingComplete = false;

@JdbcTypeCode(SqlTypes.JSON)
@Column(name = "selected_goals", columnDefinition = "jsonb")
private String selectedGoals; // JSON array: ["fitness","mindfulness"]

@Column(name = "personality_type", length = 30)
private String personalityType;

@Column(name = "difficulty_preference", length = 20)
private String difficultyPreference;
```

### 3.2 New DTO

```java
// OnboardingResponse.java
public record OnboardingResponse(
    int level,
    boolean arcStarted,
    String arcId,
    String arcName
) {}

// OnboardingStatusResponse.java
public record OnboardingStatusResponse(
    boolean complete,
    List<String> selectedGoals,
    String personalityType,
    String difficulty
) {}
```

### 3.3 Service Layer

Create `OnboardingService` in the `user` module (since it primarily mutates the User entity):

```java
@Service
public class OnboardingService {
    // Dependencies: UserRepository, ArcService, ObjectMapper

    @Transactional
    public OnboardingResponse completeOnboarding(UUID userId, OnboardingRequest request) {
        // 1. Load user
        // 2. Check if already complete → throw 409
        // 3. Persist: goals, personality, difficulty, avatar, hardMode, onboardingComplete=true
        // 4. Attempt to start the selected arc (graceful — catch failure)
        // 5. Return response with arc info
    }

    @Transactional(readOnly = true)
    public OnboardingStatusResponse getOnboardingStatus(UUID userId) {
        // Load user, return status
    }
}
```

### 3.4 Controller Changes

Enhance existing `UserController.completeOnboarding()` to delegate to the new service and return a proper response.

### 3.5 Data Flow

```
Frontend                    UserController          OnboardingService         ArcService
    │                            │                        │                       │
    │── PUT /users/onboarding ──►│                        │                       │
    │                            │── completeOnboarding ──►│                       │
    │                            │                        │── save user fields ───►│ (UserRepo)
    │                            │                        │── startArc(userId) ───►│
    │                            │                        │◄── ArcProgressResp ────│
    │                            │◄── OnboardingResponse ─│                       │
    │◄── 200 OK ────────────────│                        │                       │
```

---

## 4. Database Migration

```sql
-- V__add_onboarding_fields.sql
ALTER TABLE users ADD COLUMN IF NOT EXISTS onboarding_complete BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE users ADD COLUMN IF NOT EXISTS selected_goals JSONB;
ALTER TABLE users ADD COLUMN IF NOT EXISTS personality_type VARCHAR(30);
ALTER TABLE users ADD COLUMN IF NOT EXISTS difficulty_preference VARCHAR(20);
```

---

## 5. Tasks

### Phase 1: Database + Entity

| Task | Description | Estimate |
|------|-------------|----------|
| ONB-T1 | Create Flyway migration adding 4 columns to `users` table | 0.25h |
| ONB-T2 | Add `onboardingComplete`, `selectedGoals`, `personalityType`, `difficultyPreference` fields to `User` entity | 0.25h |

### Phase 2: DTOs

| Task | Description | Estimate |
|------|-------------|----------|
| ONB-T3 | Create `OnboardingResponse` record (level, arcStarted, arcId, arcName) | 0.1h |
| ONB-T4 | Create `OnboardingStatusResponse` record (complete, selectedGoals, personalityType, difficulty) | 0.1h |

### Phase 3: Service Layer

| Task | Description | Estimate |
|------|-------------|----------|
| ONB-T5 | Create `OnboardingService.completeOnboarding()` — persist all fields + start arc | 1.5h |
| ONB-T6 | Create `OnboardingService.getOnboardingStatus()` — read user fields | 0.25h |
| ONB-T7 | Add idempotency check (409 if already complete) | 0.25h |
| ONB-T8 | Add graceful arc start (try/catch — onboarding succeeds even if arc fails) | 0.25h |

### Phase 4: Controller Layer

| Task | Description | Estimate |
|------|-------------|----------|
| ONB-T9 | Refactor `UserController.completeOnboarding()` to delegate to `OnboardingService` and return `OnboardingResponse` | 0.5h |
| ONB-T10 | Add `GET /api/v1/users/me/onboarding-status` endpoint | 0.25h |

### Phase 5: Tests

| Task | Description | Estimate |
|------|-------------|----------|
| ONB-T11 | Unit tests for `OnboardingService` — happy path, already complete, arc start failure | 1.5h |
| ONB-T12 | Controller tests for both endpoints | 0.5h |

### Phase 6: Frontend Alignment

| Task | Description | Estimate |
|------|-------------|----------|
| ONB-T13 | Update `OnboardingApiService` to handle the new response shape | 0.25h |
| ONB-T14 | Update onboarding guard to check backend status via `GET /onboarding-status` (instead of local storage only) | 0.5h |

---

## 6. Implementation Priority

### Sprint 1 (Critical — Onboarding is broken without this)
1. ONB-T1→T2 — Database + Entity
2. ONB-T3→T4 — DTOs
3. ONB-T5→T8 — Service
4. ONB-T9→T10 — Controller
5. ONB-T11→T12 — Tests

### Sprint 2 (Frontend hardening)
6. ONB-T13→T14 — Frontend alignment

---

## 7. Acceptance Criteria

- [ ] `PUT /api/v1/users/onboarding` persists goals, personality, difficulty, avatar on the user record
- [ ] `PUT /api/v1/users/onboarding` sets `onboarding_complete = true`
- [ ] `PUT /api/v1/users/onboarding` auto-starts the selected arc (verified via `GET /arcs/active`)
- [ ] `PUT /api/v1/users/onboarding` returns 409 on second call (idempotent)
- [ ] `PUT /api/v1/users/onboarding` succeeds even if arc start fails (graceful degradation)
- [ ] `GET /api/v1/users/me/onboarding-status` returns `complete: true` with persisted data after onboarding
- [ ] `GET /api/v1/users/me/onboarding-status` returns `complete: false` for new users
- [ ] After onboarding, dashboard shows the started arc in the active arc section
- [ ] Frontend guard checks backend status (not just local storage) for cross-device consistency

---

## 8. Non-Functional Requirements

| Concern | Requirement |
|---------|-------------|
| Auth | Firebase token required for both endpoints |
| Idempotency | Second PUT returns 409, does not re-process |
| Data Integrity | All fields saved in single transaction |
| Graceful Degradation | Arc start failure → onboarding still succeeds, `arcStarted: false` |
| Migration | Flyway migration is backward-compatible (new columns are nullable/defaulted) |
| Cross-device | Backend is source of truth for onboarding status (not local storage alone) |

---

## 9. What Already Works (No Changes Needed)

| Component | Status |
|-----------|--------|
| Frontend 5-step wizard UI | ✅ Complete |
| Frontend signal store + persistence | ✅ Complete |
| Frontend personality computation | ✅ Complete |
| Frontend arc recommendation engine | ✅ Complete |
| Frontend route guard (local check) | ✅ Complete |
| Backend `ArcRecommendationEngine` | ✅ Complete |
| Backend `ArcService.startArc()` | ✅ Complete |
| Backend `OnboardingRequest` DTO | ✅ Complete (has all fields) |
| Frontend `OnboardingApiService.submitOnboarding()` | ✅ Complete |
