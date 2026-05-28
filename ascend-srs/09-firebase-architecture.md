# 09 — Firebase Architecture

# Ascend — Enter Arc Mode

---

## Firebase Responsibilities

Firebase is **not the primary backend**. It acts as the Identity + Realtime Layer.

### Firebase handles:
- ✅ Authentication
- ✅ Push Notifications
- ✅ Realtime sync
- ✅ Lightweight profile state
- ✅ Offline persistence

### Firebase does NOT handle:
- ❌ XP calculation
- ❌ League ranking
- ❌ Quest validation
- ❌ Anti-cheat
- ❌ Analytics logic

Those stay in **Spring Boot**.

---

## Firebase Services Used

| Service | Purpose |
|---------|---------|
| Firebase Auth | Authentication |
| Firestore | Lightweight realtime state |
| Firebase Storage | Avatar/media uploads |
| FCM | Push notifications |
| Firebase Analytics | Engagement tracking |
| Crashlytics | Error monitoring |

---

## Authentication Architecture

### Providers

| Provider | Purpose |
|----------|---------|
| Google Sign-In | Primary login |
| Apple Sign-In | Required for iOS |
| Email/Password | Fallback option |
| Anonymous Login | Guest mode |

### Authentication Flow

```
App Launch
    ↓
Firebase Auth (client-side)
    ↓
JWT Token Generated
    ↓
Angular sends token to Spring Boot
    ↓
Spring Boot validates via Firebase Admin SDK
    ↓
Backend User Session Created
```

### Token Validation (Spring Boot)

Spring Boot validates:
- Firebase JWT signature
- Token expiration
- User identity claims

Implementation: Firebase Admin SDK (Java)

### Security Rule
**Never trust frontend role claims.** Backend validates everything.

---

## Firestore Collections

Firestore stores lightweight, realtime UX data — not core business logic.

### Collection Structure
```
users/
arcs/
quest-progress/
notifications/
guild-chat/
leaderboard-cache/
settings/
presence/
realtime-progress/
```

---

## Firestore Schema

### users collection
```json
{
  "uid": "firebase_uid",
  "username": "Ayush",
  "email": "user@email.com",
  "avatar": "avatar-url",
  "level": 12,
  "xp": 2800,
  "league": "Silver",
  "currentArcId": "arc_001",
  "premium": false,
  "timezone": "Asia/Kolkata",
  "createdAt": "timestamp",
  "lastLogin": "timestamp"
}
```

### arcs collection
```json
{
  "arcId": "monk_arc",
  "name": "Monk Arc",
  "description": "Build discipline",
  "durationDays": 60,
  "difficulty": "medium",
  "milestones": [],
  "bosses": []
}
```

### quest-progress collection
```json
{
  "questId": "quest001",
  "userId": "uid123",
  "completed": true,
  "completedAt": "timestamp",
  "xpEarned": 40
}
```

### guild-chat collection
```json
{
  "guildId": "guild123",
  "senderId": "uid123",
  "senderName": "Ayush",
  "message": "Completed workout!",
  "createdAt": "timestamp"
}
```

### presence collection
```json
{
  "userId": "123",
  "online": true,
  "lastSeen": "timestamp"
}
```

### live-notifications collection
```json
{
  "userId": "123",
  "title": "Quest Completed",
  "message": "+50 XP earned",
  "read": false,
  "createdAt": "timestamp"
}
```

### realtime-progress collection
```json
{
  "userId": "123",
  "currentQuest": "deep_work",
  "progress": 60,
  "updatedAt": "timestamp"
}
```

---

## Firebase Security Rules

### Core Rule
Users can only access their own data.

```javascript
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {

    // Users can only read/write their own profile
    match /users/{userId} {
      allow read, write: if request.auth.uid == userId;
    }

    // Quest progress - user's own data only
    match /quest-progress/{docId} {
      allow read, write: if request.auth.uid == resource.data.userId;
    }

    // Guild chat - members only
    match /guild-chat/{guildId}/messages/{messageId} {
      allow read, write: if request.auth != null;
    }

    // Notifications - user's own
    match /notifications/{userId}/items/{notifId} {
      allow read, write: if request.auth.uid == userId;
    }

    // Presence - authenticated users
    match /presence/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId;
    }

    // Leaderboard cache - read only for authenticated
    match /leaderboard-cache/{docId} {
      allow read: if request.auth != null;
      allow write: if false; // Only backend writes
    }
  }
}
```

### Sensitive Logic Rule
**Never calculate XP, streaks, or rewards inside Firebase.** Frontend is untrusted. Backend validates all gameplay.

---

## Firebase Cloud Messaging (FCM)

### Notification Types

| Type | Priority | Example |
|------|----------|---------|
| Quest Reminder | Normal | "Warrior Quest starts in 30 mins" |
| Streak Warning | High | "Your 14-day streak is at risk!" |
| Reward Alert | Normal | "Legendary chest unlocked!" |
| Guild Reminder | Normal | "Your guild needs you" |
| Level Up | Normal | "You reached Level 13!" |

### Implementation

**Server-side (Spring Boot):**
```java
// Send notification via Firebase Admin SDK
Message message = Message.builder()
    .setToken(userFcmToken)
    .setNotification(Notification.builder()
        .setTitle("Streak Warning!")
        .setBody("45 mins left to save your streak")
        .build())
    .build();

FirebaseMessaging.getInstance().send(message);
```

**Client-side (Capacitor):**
```typescript
PushNotifications.addListener('pushNotificationReceived', (notification) => {
  // Handle in-app notification
});
```

### Smart Timing
- AI determines best reminder time based on user activity patterns
- Maximum 5 notifications/day
- Silent mode during detected sleep hours
- Reduced frequency during burnout recovery

---

## Firebase Storage

### Usage
- User avatars
- Arc banner images
- Achievement badges
- Guild logos

### Storage Rules
```javascript
rules_version = '2';

service firebase.storage {
  match /b/{bucket}/o {
    match /avatars/{userId}/{fileName} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId
                   && request.resource.size < 5 * 1024 * 1024;
    }
  }
}
```

### File Size Limits
- Avatars: 5 MB max
- Guild logos: 2 MB max
- Supported formats: JPEG, PNG, WebP

---

## Firebase Analytics

### Tracked Events
- `quest_completed`
- `level_up`
- `arc_started`
- `streak_milestone`
- `premium_upgrade`
- `guild_joined`
- `boss_defeated`

### User Properties
- `user_level`
- `current_arc`
- `premium_status`
- `streak_days`
- `league_tier`

---

## Crashlytics Integration

### Tracked Errors
- API failures
- Auth errors
- Sync conflicts
- UI crashes
- WebSocket disconnections

### Custom Keys
- `user_id`
- `current_screen`
- `last_action`
- `network_state`

---

## Offline Persistence

Firestore offline persistence enabled:
- Users can complete quests offline
- Data syncs when connection restored
- Conflict resolution: server wins

```typescript
// Enable offline persistence
enableIndexedDbPersistence(firestore);
```

---

## Data Sync Strategy

### Firestore → Spring Boot Sync
1. User completes action in app
2. Firestore updated immediately (optimistic UI)
3. Spring Boot API called for validation
4. If validation fails, Firestore rolled back
5. If validation passes, both stay in sync

### Spring Boot → Firestore Sync
1. Backend calculates new state (XP, level, rank)
2. Backend updates Firestore via Admin SDK
3. Client receives realtime update via Firestore listener

---

*This document defines the complete Firebase integration architecture for Ascend.*
