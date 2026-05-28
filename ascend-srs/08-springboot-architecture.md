# 08 — Spring Boot Architecture

# Ascend — Enter Arc Mode

---

## Architecture Style

# Clean Architecture + Modular Monolith

Optimized for:
- ✅ Solo/lean development
- ✅ Faster iteration
- ✅ Simpler deployment
- ✅ Lower infrastructure cost
- ✅ Scalable to microservices later

---

## High-Level System Architecture

```
+------------------------------------------------+
|                  Ionic Angular                  |
|            (Android / iOS / Web)               |
+------------------------------------------------+
                    |
                    | HTTPS + WebSocket
                    |
+------------------------------------------------+
|                Spring Boot API                  |
|                                                |
| Auth Validation Layer                          |
| Arc Engine                                     |
| Quest Engine                                   |
| XP Engine                                      |
| League Engine                                  |
| Notification Engine                            |
| AI Recommendation Engine                       |
| Analytics Engine                               |
+------------------------------------------------+
                    |
         --------------------------
         |            |            |
+----------------+  +------------------+
| Firebase Auth  |  | Firestore DB     |
+----------------+  +------------------+
         |
+---------------------------+
| Firebase Cloud Messaging  |
+---------------------------+

         Optional
+---------------------------+
| PostgreSQL                |
| Analytics + Reporting     |
+---------------------------+
```

---

## Why Hybrid Architecture?

### Firebase handles:
- Authentication
- User profile (lightweight)
- Realtime state sync
- Push notifications

### Spring Boot handles:
- Business logic
- XP calculations
- Quest validation
- League rankings
- AI logic
- Anti-cheat

### SQL (PostgreSQL) handles:
- Analytics
- Leaderboard queries
- Reporting
- Complex aggregations

---

## Package Structure

```
com.ascend
├── auth/
├── user/
├── onboarding/
├── quest/
├── xp/
├── streak/
├── arc/
├── skilltree/
├── league/
├── guild/
├── boss/
├── analytics/
├── ai/
├── notification/
├── premium/
├── admin/
└── common/
```

---

## Layered Structure (Per Module)

Each module follows:

```
{module}/
├── controller/      — REST endpoints
├── service/         — Business logic
├── repository/      — Data access
├── dto/             — Request/Response objects
├── mapper/          — Entity ↔ DTO conversion
├── entity/          — Database entities
├── validator/       — Input validation
└── scheduler/       — Scheduled tasks
```

---

## Authentication Flow

```
Login
  ↓
Firebase generates JWT token
  ↓
Angular sends token in header
  ↓
Spring validates token (Firebase Admin SDK)
  ↓
Backend generates session context
```

### Security Header
```
Authorization: Bearer JWT_TOKEN
```

### Validation
- Firebase Admin SDK validates JWT
- Check expiration
- Verify user identity
- Never trust frontend role claims

---

## API Standards

### Base Path
```
/api/v1/
```

### Response Standard (Success)
```json
{
  "success": true,
  "message": "Quest completed",
  "data": {}
}
```

### Response Standard (Error)
```json
{
  "success": false,
  "message": "Quest not found",
  "errorCode": "QUEST_404"
}
```

### Pagination
```
?page=1&size=20
```

### Sorting
```
?sort=xp,desc
```

---

## Scheduler Engine

Spring Scheduler for:
- Daily reset (00:00)
- Weekly league reset (Sunday)
- Streak calculation
- Reminder triggers
- Leaderboard refresh
- Season transitions

---

## WebSocket Architecture

Realtime features via Spring WebSocket:
- Guild chat
- Leaderboard updates
- Live XP notifications
- Social feed
- Friend status

---

## Caching Strategy

### Redis Cache

Cached data:
- Leaderboard rankings
- User stats summaries
- Profile data
- Active arc state

TTL: 5–30 minutes depending on data type.

---

## Module Details

### Auth Module
- Firebase token validation
- Session management
- Role assignment
- Device tracking

### User Module
- Profile CRUD
- Avatar management
- Preferences
- Account linking

### Quest Module
- Quest creation/editing
- Completion validation
- XP calculation trigger
- Recurring quest scheduling
- Anti-duplicate logic

### XP Module
- XP calculation engine
- Multiplier application
- Level-up detection
- XP history logging
- Daily cap enforcement

### Streak Module
- Daily streak calculation
- Combo multiplier
- Shield activation
- Comeback detection
- Relapse prevention

### Arc Module
- Arc assignment
- Progress tracking
- Milestone completion
- Phase transitions
- Arc completion rewards

### League Module
- Weekly ranking calculation
- Promotion/demotion logic
- Matchmaking
- Anti-cheat validation
- Season management

### Guild Module
- Guild CRUD
- Member management
- Shared quest tracking
- Guild scoring
- Chat orchestration

### Boss Module
- Boss assignment
- Progress tracking
- Stage management
- Reward distribution

### AI Module
- Behavior analysis
- Burnout detection
- Schedule optimization
- Adaptive difficulty
- Motivation engine

### Notification Module
- FCM integration
- Smart timing
- Template management
- Rate limiting
- Priority handling

### Premium Module
- Subscription validation
- Feature gating
- Trial management
- Payment webhook handling

### Analytics Module
- Data aggregation
- Weekly reports
- Life score calculation
- Correlation analysis
- Trend detection

### Admin Module
- Arc CMS
- User moderation
- Event management
- System analytics
- Configuration management

---

## Event-Driven Module Communication

Avoid tightly coupled modules. Use Spring Application Events for cross-module communication.

### Quest Completed Event Flow
```
Quest Completed
    │
    ▼
Quest Module (publishes QuestCompletedEvent)
    │
    ├── XP Module (calculates and awards XP)
    ├── Streak Module (updates streak count)
    ├── Achievement Module (checks unlock conditions)
    ├── Analytics Module (logs completion)
    └── Notification Module (sends reward notification)
```

### Implementation Pattern
```java
// Event
public record QuestCompletedEvent(
    UUID userId,
    UUID questId,
    String difficulty,
    String statType
) {}

// Publisher (Quest Service)
applicationEventPublisher.publishEvent(
    new QuestCompletedEvent(userId, questId, difficulty, statType)
);

// Listeners
@EventListener
public void onQuestCompleted(QuestCompletedEvent event) {
    xpService.awardXp(event.userId(), event.questId(), event.difficulty());
}

@EventListener
public void onQuestCompletedStreak(QuestCompletedEvent event) {
    streakService.recordCompletion(event.userId());
}

@EventListener
public void onQuestCompletedAchievement(QuestCompletedEvent event) {
    achievementService.checkUnlocks(event.userId());
}
```

### Key Events

| Event | Triggered By | Listeners |
|-------|-------------|-----------|
| QuestCompletedEvent | Quest completion | XP, Streak, Achievement, Analytics, Notification |
| LevelUpEvent | XP threshold reached | Notification, Achievement, League |
| StreakMilestoneEvent | Streak reaches milestone | Notification, Achievement, Reward |
| ArcPhaseCompleteEvent | Arc phase finished | Notification, Achievement, Boss |
| BossDefeatedEvent | Boss HP reaches 0 | Notification, Achievement, Reward |
| GuildChallengeCompleteEvent | Guild target met | Notification, Guild Ranking |

This keeps the backend clean and each module independently testable.

---

## Error Handling Strategy

### Global Exception Handler
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    // Handle validation errors
    // Handle business logic errors
    // Handle authentication errors
    // Handle unexpected errors
}
```

### Custom Exceptions
- `QuestNotFoundException`
- `DuplicateCompletionException`
- `InsufficientXPException`
- `StreakExpiredException`
- `UnauthorizedAccessException`

---

## Async Processing

Use `@Async` for:
- Notification sending
- Analytics logging
- XP history recording
- Leaderboard updates

---

## Health & Monitoring

### Actuator Endpoints
- `/actuator/health`
- `/actuator/metrics`
- `/actuator/info`

### Metrics (Micrometer + Prometheus)
- API latency
- Active users
- Quest completion rate
- Error rates

---

## Security Configuration

```java
@Configuration
public class SecurityConfig {
    // Firebase token filter
    // CORS configuration
    // Rate limiting
    // RBAC rules
}
```

### Roles
```
USER, PREMIUM_USER, MODERATOR, ADMIN, SUPER_ADMIN
```

---

## Build & Deployment

### Build Tool
Gradle or Maven

### Java Version
Java 21

### Docker
```dockerfile
FROM eclipse-temurin:21-jre
COPY build/libs/ascend.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Profiles
- `dev` — Local development
- `staging` — Testing environment
- `prod` — Production

---

*This document defines the complete Spring Boot backend engineering architecture.*
