# API Controller Gaps — Requirements, Design & Tasks

## Status Summary

After code inspection, all three modules **do have controllers**, but each has **missing endpoints** relative to the service layer capabilities.

| Module | Controller | Missing Endpoints |
|--------|-----------|-------------------|
| Notification | ✅ Exists | `PATCH /api/v1/notifications/read` (batch), `GET /unread-count`, `DELETE /{id}` |
| Premium | ✅ Exists | `POST /api/v1/premium/downgrade`, `GET /api/v1/premium/feature-access/{feature}` |
| Social | ✅ Exists | `GET /friends/pending`, `DELETE /friends/{friendId}`, `POST /friends/block` |

---

## 1. Notification Module

### 1.1 Requirements

| ID | Requirement | Priority |
|----|-------------|----------|
| NOTIF-01 | Users can batch-mark all notifications as read | High |
| NOTIF-02 | Users can get unread notification count (for badge UI) | High |
| NOTIF-03 | Users can delete a notification | Low |

### 1.2 API Design

#### PATCH `/api/v1/notifications/read` — Batch Mark as Read

```
Request:  (no body — marks all unread as read)
Response: 200 OK
{
  "status": "success",
  "message": "All notifications marked as read"
}
```

#### GET `/api/v1/notifications/unread-count`

```
Response: 200 OK
{
  "status": "success",
  "data": { "count": 7 }
}
```

#### DELETE `/api/v1/notifications/{id}`

```
Response: 204 No Content
Error:    404 Not Found (if not owned by user)
```

### 1.3 Tasks

| Task | Description | Estimate |
|------|-------------|----------|
| NOTIF-T1 | Add `markAllAsRead(UUID userId)` to `NotificationService` | 0.5h |
| NOTIF-T2 | Add `countUnread(UUID userId)` to `NotificationService` | 0.5h |
| NOTIF-T3 | Add `delete(UUID userId, UUID notificationId)` to `NotificationService` | 0.5h |
| NOTIF-T4 | Add `countByUserIdAndReadAtIsNull(UUID userId)` to `NotificationLogRepository` | 0.25h |
| NOTIF-T5 | Add `PATCH /read`, `GET /unread-count`, `DELETE /{id}` endpoints to `NotificationController` | 1h |
| NOTIF-T6 | Unit tests for new service methods | 1h |
| NOTIF-T7 | Integration tests for new endpoints | 1h |

---

## 2. Premium Module

### 2.1 Requirements

| ID | Requirement | Priority |
|----|-------------|----------|
| PREM-01 | Users can explicitly downgrade to free tier | Medium |
| PREM-02 | Users can check access to a specific premium feature by name | Medium |

### 2.2 API Design

#### POST `/api/v1/premium/downgrade`

```
Request:  (no body)
Response: 200 OK
{
  "status": "success",
  "message": "Downgraded to free tier"
}
```

#### GET `/api/v1/premium/feature-access/{feature}`

```
Path param: feature (enum: AI_COACH, ADVANCED_ANALYTICS, CUSTOM_QUESTS, etc.)
Response: 200 OK
{
  "status": "success",
  "data": { "feature": "AI_COACH", "accessible": true }
}
Error: 400 Bad Request (invalid feature name)
```

### 2.3 Tasks

| Task | Description | Estimate |
|------|-------------|----------|
| PREM-T1 | Add `POST /downgrade` endpoint to `PremiumController` (delegates to existing `SubscriptionService.downgradeToFree`) | 0.5h |
| PREM-T2 | Add `GET /feature-access/{feature}` endpoint to `PremiumController` (delegates to existing `FeatureGateService.hasAccess`) | 0.5h |
| PREM-T3 | Unit tests for new endpoints | 0.5h |
| PREM-T4 | Integration tests for new endpoints | 0.5h |

---

## 3. Social Module

### 3.1 Requirements

| ID | Requirement | Priority |
|----|-------------|----------|
| SOC-01 | Users can view pending incoming friend requests | High |
| SOC-02 | Users can remove an existing friend | High |
| SOC-03 | Users can block a user | Medium |
| SOC-04 | Users can get their accountability partner info | Low |

### 3.2 API Design

#### GET `/api/v1/social/friends/pending`

```
Response: 200 OK
{
  "status": "success",
  "data": [
    { "userId": "...", "username": "...", "avatarUrl": "...", "level": 5, "streak": 12, "status": "PENDING" }
  ]
}
```

#### DELETE `/api/v1/social/friends/{friendId}`

```
Response: 200 OK
{
  "status": "success",
  "message": "Friend removed"
}
Error: 404 (no friendship found)
```

#### POST `/api/v1/social/friends/block`

```
Request:
{
  "userId": "uuid-of-user-to-block"
}
Response: 200 OK
{
  "status": "success",
  "message": "User blocked"
}
```

#### GET `/api/v1/social/accountability`

```
Response: 200 OK
{
  "status": "success",
  "data": { "partnerId": "...", "username": "...", "pairedAt": "..." }
}
Error: 404 (no partner paired)
```

### 3.3 Tasks

| Task | Description | Estimate |
|------|-------------|----------|
| SOC-T1 | Add `GET /friends/pending` endpoint to `SocialController` (delegates to existing `FriendService.getPendingRequests`) | 0.5h |
| SOC-T2 | Add `DELETE /friends/{friendId}` endpoint to `SocialController` (delegates to existing `FriendService.removeFriend`) | 0.5h |
| SOC-T3 | Add `POST /friends/block` endpoint to `SocialController` (delegates to existing `FriendService.blockUser`) | 0.5h |
| SOC-T4 | Add `getPartner(UUID userId)` method to `AccountabilityService` | 0.5h |
| SOC-T5 | Add `GET /accountability` endpoint to `SocialController` | 0.5h |
| SOC-T6 | Create `AccountabilityPartnerResponse` DTO | 0.25h |
| SOC-T7 | Unit tests for new endpoints | 1h |
| SOC-T8 | Integration tests for new endpoints | 1h |

---

## 4. Implementation Priority Order

### Phase 1 — High Priority (Sprint 1)
1. **NOTIF-T1→T5** — Batch read + unread count (critical for mobile badge UX)
2. **SOC-T1** — Pending friend requests (users can't accept what they can't see)
3. **SOC-T2** — Remove friend (basic social hygiene)

### Phase 2 — Medium Priority (Sprint 2)
4. **PREM-T1→T2** — Downgrade + feature access check
5. **SOC-T3** — Block user
6. **SOC-T4→T6** — Accountability partner info

### Phase 3 — Low Priority (Backlog)
7. **NOTIF-T3** — Delete notification

---

## 5. Acceptance Criteria

### Notification
- [ ] `PATCH /api/v1/notifications/read` marks all unread notifications for the authenticated user as read
- [ ] `GET /api/v1/notifications/unread-count` returns accurate count; returns 0 after batch-read
- [ ] `DELETE /api/v1/notifications/{id}` returns 404 if notification belongs to another user

### Premium
- [ ] `POST /api/v1/premium/downgrade` sets user to FREE tier and `user.premium = false`
- [ ] `GET /api/v1/premium/feature-access/AI_COACH` returns `accessible: true` for premium users, `false` for free

### Social
- [ ] `GET /api/v1/social/friends/pending` returns only PENDING requests where current user is the recipient
- [ ] `DELETE /api/v1/social/friends/{friendId}` removes friendship in both directions
- [ ] `POST /api/v1/social/friends/block` prevents future friend requests from blocked user
- [ ] `GET /api/v1/social/accountability` returns partner info or 404

---

## 6. Non-Functional Requirements

| Concern | Requirement |
|---------|-------------|
| Auth | All endpoints require valid Firebase token via `@AuthenticationPrincipal FirebasePrincipal` |
| Rate Limiting | Notification batch-read: max 10 calls/min per user |
| Response Format | All responses use `ApiResponse<T>` wrapper |
| Pagination | Feed/notification list endpoints already paginated — no change needed |
| Error Handling | Use existing `BusinessException` pattern with error codes |
