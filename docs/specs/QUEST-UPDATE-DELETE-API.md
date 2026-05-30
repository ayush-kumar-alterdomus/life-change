# Quest Update/Delete API — Requirements, Design & Tasks

## Problem Statement

Users can create custom quests via `POST /api/v1/quests` but **cannot modify or remove them**. The controller has no `PUT` or `DELETE` endpoints, and the service layer has no update/delete methods. This means:

- Users are stuck with typos in quest titles forever
- Users cannot adjust XP rewards or difficulty as they progress
- Users cannot remove quests they no longer want (only skip them daily)
- The 5-quest limit for free users becomes punishing — one bad quest wastes a slot permanently

---

## 1. Requirements

| ID | Requirement | Priority |
|----|-------------|----------|
| QUEST-UPD-01 | Users can update title, description, difficulty, xpReward, statType, and frequency of their own custom quests | Critical |
| QUEST-UPD-02 | Users **cannot** update system/recurring quests (only custom quests they created) | Critical |
| QUEST-UPD-03 | Updates are validated with the same rules as creation (title length, XP range, etc.) | High |
| QUEST-UPD-04 | Updating a quest does NOT reset today's completion status | High |
| QUEST-DEL-01 | Users can delete their own custom quests | Critical |
| QUEST-DEL-02 | Users **cannot** delete system/recurring quests | Critical |
| QUEST-DEL-03 | Deleting a quest also removes associated completion history (cascade) | Medium |
| QUEST-DEL-04 | Deleting a quest frees up the custom quest slot for free users | High |
| QUEST-DEL-05 | Arc-linked quests cannot be deleted (they belong to the arc lifecycle) | Medium |

---

## 2. API Design

### `PUT /api/v1/quests/{id}`

**Auth:** Required (Firebase token)

**Request Body:**
```json
{
  "title": "Run 10km",
  "description": "Morning run in the park",
  "difficulty": "HARD",
  "xpReward": 100,
  "statType": "ENDURANCE",
  "frequency": "DAILY"
}
```

All fields are optional — only provided fields are updated (partial update semantics).

**Response: 200 OK**
```json
{
  "success": true,
  "message": "Quest updated",
  "data": {
    "id": "uuid",
    "title": "Run 10km",
    "description": "Morning run in the park",
    "difficulty": "HARD",
    "xpReward": 100,
    "statType": "ENDURANCE",
    "frequency": "DAILY",
    "recurring": true,
    "isCustom": true,
    "completed": false
  }
}
```

**Error Responses:**
| Status | Code | Condition |
|--------|------|-----------|
| 404 | QUEST_NOT_FOUND | Quest ID doesn't exist |
| 403 | NOT_QUEST_OWNER | User didn't create this quest |
| 403 | SYSTEM_QUEST | Attempting to modify a system/recurring quest |
| 400 | VALIDATION_FAILED | Invalid field values |

---

### `DELETE /api/v1/quests/{id}`

**Auth:** Required (Firebase token)

**Response: 204 No Content**

**Error Responses:**
| Status | Code | Condition |
|--------|------|-----------|
| 404 | QUEST_NOT_FOUND | Quest ID doesn't exist |
| 403 | NOT_QUEST_OWNER | User didn't create this quest |
| 403 | SYSTEM_QUEST | Attempting to delete a system quest |
| 409 | ARC_LINKED_QUEST | Quest is linked to an arc and cannot be deleted independently |

---

## 3. Backend Design

### 3.1 New DTO

```java
// UpdateQuestRequest.java — all fields optional for partial update
public class UpdateQuestRequest {
    @Size(max = 100) String title;
    @Size(max = 500) String description;
    Difficulty difficulty;
    @Min(10) @Max(300) Integer xpReward;
    StatType statType;
    Frequency frequency;
}
```

### 3.2 Service Methods

```java
// QuestService.java — new methods
QuestResponse updateQuest(UUID userId, UUID questId, UpdateQuestRequest request);
void deleteQuest(UUID userId, UUID questId);
```

**Update logic:**
1. Load quest by ID → 404 if not found
2. Verify `quest.isCustom()` → 403 SYSTEM_QUEST if false
3. Verify `quest.getCreatedBy().getId().equals(userId)` → 403 NOT_QUEST_OWNER if false
4. Validate non-null fields via `QuestValidator`
5. Apply non-null fields to entity
6. Save and return updated response

**Delete logic:**
1. Load quest by ID → 404 if not found
2. Verify `quest.isCustom()` → 403 SYSTEM_QUEST if false
3. Verify `quest.getCreatedBy().getId().equals(userId)` → 403 NOT_QUEST_OWNER if false
4. Verify `quest.getArcId() == null` → 409 ARC_LINKED_QUEST if linked
5. Delete completion history for this quest
6. Delete the quest

### 3.3 Repository Addition

```java
// QuestCompletionRepository — new method
void deleteByQuestId(UUID questId);
```

---

## 4. Tasks

### Phase 1: DTO

| Task | Description | Estimate |
|------|-------------|----------|
| QUEST-T1 | Create `UpdateQuestRequest` DTO with optional fields and validation annotations | 0.25h |

### Phase 2: Service Layer

| Task | Description | Estimate |
|------|-------------|----------|
| QUEST-T2 | Add `updateQuest(UUID userId, UUID questId, UpdateQuestRequest request)` to `QuestService` | 1h |
| QUEST-T3 | Add `deleteQuest(UUID userId, UUID questId)` to `QuestService` | 0.75h |
| QUEST-T4 | Add `deleteByQuestId(UUID questId)` to `QuestCompletionRepository` | 0.1h |

### Phase 3: Controller Layer

| Task | Description | Estimate |
|------|-------------|----------|
| QUEST-T5 | Add `PUT /api/v1/quests/{id}` endpoint to `QuestController` | 0.5h |
| QUEST-T6 | Add `DELETE /api/v1/quests/{id}` endpoint to `QuestController` | 0.5h |

### Phase 4: Tests

| Task | Description | Estimate |
|------|-------------|----------|
| QUEST-T7 | Unit tests for `updateQuest` — happy path, not owner, system quest, validation failure | 1h |
| QUEST-T8 | Unit tests for `deleteQuest` — happy path, not owner, system quest, arc-linked | 1h |
| QUEST-T9 | Controller tests for both endpoints | 0.75h |

### Phase 5: Frontend

| Task | Description | Estimate |
|------|-------------|----------|
| QUEST-T10 | Add `updateQuest(id, payload)` and `deleteQuest(id)` to frontend quest service | 0.25h |

---

## 5. Acceptance Criteria

- [ ] `PUT /api/v1/quests/{id}` with valid partial body updates only the provided fields
- [ ] `PUT /api/v1/quests/{id}` returns 403 when user is not the quest creator
- [ ] `PUT /api/v1/quests/{id}` returns 403 when quest is a system quest (`custom = false`)
- [ ] `PUT /api/v1/quests/{id}` returns 400 when title exceeds 100 chars or xpReward out of range
- [ ] `PUT /api/v1/quests/{id}` does NOT affect today's completion status for that quest
- [ ] `DELETE /api/v1/quests/{id}` returns 204 and quest is no longer retrievable
- [ ] `DELETE /api/v1/quests/{id}` returns 403 when user is not the quest creator
- [ ] `DELETE /api/v1/quests/{id}` returns 409 when quest has a non-null `arcId`
- [ ] After deletion, `countByCreatedBy_IdAndCustomTrue` decreases by 1 (slot freed)
- [ ] After deletion, completion history for that quest is also removed

---

## 6. Non-Functional Requirements

| Concern | Requirement |
|---------|-------------|
| Auth | Firebase token required; ownership verified server-side |
| Idempotency | DELETE is idempotent — second call returns 404 |
| Cascade | Completion history deleted with quest (no orphan records) |
| Concurrency | Optimistic locking not needed (single-user ownership) |
| Response Format | Uses existing `ApiResponse<T>` wrapper |
| Error Handling | Uses existing `BusinessException` pattern |

---

## 7. Security Considerations

- **Ownership check is mandatory** — users must not be able to modify/delete other users' quests
- The `createdBy` relationship is loaded eagerly enough to verify ownership without extra queries
- System quests (`custom = false`) are immutable — no user can modify them regardless of role
- Arc-linked quests are protected from deletion to maintain arc integrity
