# 13 — WebSocket Events

# Ascend — Enter Arc Mode

---

## Purpose

Realtime features powered by Spring WebSocket:
- Guild chat
- Live XP updates
- Leaderboard changes
- Notifications
- Friend status
- Boss battle progress

---

## Technology

- Spring WebSocket (STOMP protocol)
- SockJS fallback for browser compatibility
- Redis pub/sub for horizontal scaling

---

## Connection

### WebSocket Endpoint
```
ws://api.ascend.app/ws
```

### Authentication
JWT token passed as query parameter or in STOMP headers:
```
CONNECT
Authorization: Bearer {firebase_jwt_token}
```

---

## Channel Architecture

### Channel Naming Convention
```
/topic/{resource}/{id}        — Broadcast to all subscribers
/user/{userId}/queue/{type}   — Private user messages
```

---

## Event Types

---

### Guild Chat

**Channel:** `/topic/guild/{guildId}/chat`

**Subscribe:** When user enters guild screen

**Message Format:**
```json
{
  "type": "GUILD_MESSAGE",
  "data": {
    "senderId": "uuid",
    "senderName": "Ayush",
    "senderAvatar": "url",
    "message": "Completed my workout!",
    "timestamp": "2026-05-28T10:30:00Z"
  }
}
```

**Send Message:**
```
SEND /app/guild/{guildId}/chat
```

**Payload:**
```json
{
  "message": "Completed my workout!"
}
```

---

### Live XP Updates

**Channel:** `/user/{userId}/queue/xp`

**Triggered:** When user completes quest (from any device)

**Message Format:**
```json
{
  "type": "XP_GAINED",
  "data": {
    "xpAmount": 50,
    "source": "quest",
    "sourceTitle": "Morning Workout",
    "totalXp": 2450,
    "statGain": {
      "type": "strength",
      "amount": 5
    }
  }
}
```

---

### Level Up Notification

**Channel:** `/user/{userId}/queue/level`

**Message Format:**
```json
{
  "type": "LEVEL_UP",
  "data": {
    "newLevel": 13,
    "rewards": [
      {
        "type": "coins",
        "amount": 100
      },
      {
        "type": "title",
        "value": "Focused Mind"
      }
    ]
  }
}
```

---

### Leaderboard Updates

**Channel:** `/topic/leaderboard/{league}`

**Triggered:** Every 5 minutes or on significant rank change

**Message Format:**
```json
{
  "type": "LEADERBOARD_UPDATE",
  "data": {
    "league": "Silver",
    "topUsers": [
      {
        "rank": 1,
        "username": "DisciplineKing",
        "weeklyXp": 2400
      }
    ],
    "updatedAt": "2026-05-28T10:30:00Z"
  }
}
```

---

### Streak Alert

**Channel:** `/user/{userId}/queue/streak`

**Triggered:** When streak is at risk (configurable threshold)

**Message Format:**
```json
{
  "type": "STREAK_WARNING",
  "data": {
    "currentStreak": 14,
    "timeRemaining": "2h 30m",
    "questsNeeded": 1,
    "message": "Complete 1 more quest to save your streak!"
  }
}
```

---

### Streak Broken

**Channel:** `/user/{userId}/queue/streak`

**Message Format:**
```json
{
  "type": "STREAK_BROKEN",
  "data": {
    "previousStreak": 14,
    "shieldUsed": false,
    "recoveryAvailable": true,
    "recoveryDeadline": "2026-05-30T00:00:00Z"
  }
}
```

---

### Boss Battle Progress

**Channel:** `/user/{userId}/queue/boss`

**Message Format:**
```json
{
  "type": "BOSS_DAMAGE",
  "data": {
    "bossId": "discipline_demon",
    "bossName": "Discipline Demon",
    "damage": 15,
    "remainingHp": 40,
    "stage": 2
  }
}
```

---

### Boss Defeated

**Channel:** `/user/{userId}/queue/boss`

**Message Format:**
```json
{
  "type": "BOSS_DEFEATED",
  "data": {
    "bossId": "discipline_demon",
    "bossName": "Discipline Demon",
    "rewards": {
      "xp": 500,
      "title": "Discipline Master",
      "cosmetic": "golden_aura"
    }
  }
}
```

---

### Guild Challenge Update

**Channel:** `/topic/guild/{guildId}/challenge`

**Message Format:**
```json
{
  "type": "GUILD_CHALLENGE_PROGRESS",
  "data": {
    "challengeId": "challenge_001",
    "title": "100 Gym Sessions",
    "currentProgress": 67,
    "target": 100,
    "contributor": "Ayush",
    "contributionAmount": 1
  }
}
```

---

### Friend Activity

**Channel:** `/user/{userId}/queue/social`

**Message Format:**
```json
{
  "type": "FRIEND_ACTIVITY",
  "data": {
    "friendId": "uuid",
    "friendName": "Neha",
    "activity": "completed_quest",
    "details": "Completed Monk Arc milestone",
    "timestamp": "2026-05-28T10:30:00Z"
  }
}
```

---

### Presence (Online Status)

**Channel:** `/topic/presence/{guildId}`

**Message Format:**
```json
{
  "type": "PRESENCE_UPDATE",
  "data": {
    "userId": "uuid",
    "username": "Ayush",
    "online": true,
    "lastSeen": "2026-05-28T10:30:00Z"
  }
}
```

---

### Achievement Unlocked

**Channel:** `/user/{userId}/queue/achievement`

**Message Format:**
```json
{
  "type": "ACHIEVEMENT_UNLOCKED",
  "data": {
    "achievementId": "early_riser",
    "name": "Early Riser",
    "description": "Wake up before 6 AM for 30 days",
    "reward": {
      "xp": 200,
      "badge": "sunrise_badge"
    }
  }
}
```

---

### Notification Push

**Channel:** `/user/{userId}/queue/notifications`

**Message Format:**
```json
{
  "type": "NOTIFICATION",
  "data": {
    "id": "notif_001",
    "title": "Quest Reminder",
    "message": "Warrior Quest starts in 30 mins",
    "priority": "normal",
    "actionUrl": "/quests/quest_001",
    "createdAt": "2026-05-28T10:30:00Z"
  }
}
```

---

## Spring WebSocket Configuration

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            .setAllowedOrigins("*")
            .withSockJS();
    }
}
```

---

## Client-Side Connection (Angular)

```typescript
import { RxStomp } from '@stomp/rx-stomp';

const rxStomp = new RxStomp();
rxStomp.configure({
  brokerURL: 'ws://api.ascend.app/ws',
  connectHeaders: {
    Authorization: `Bearer ${token}`
  },
  heartbeatIncoming: 10000,
  heartbeatOutgoing: 10000,
  reconnectDelay: 5000
});

// Subscribe to XP updates
rxStomp.watch('/user/queue/xp').subscribe((message) => {
  const event = JSON.parse(message.body);
  // Handle XP update
});
```

---

## Error Handling

### Connection Lost
- Auto-reconnect with exponential backoff
- Queue messages during disconnection
- Replay missed events on reconnection

### Invalid Token
- Disconnect with error frame
- Client redirects to login

### Rate Limiting
- Max 30 messages/minute per user (chat)
- Throttle leaderboard updates to every 5 minutes

---

## Scaling Strategy

### Single Server
- In-memory message broker sufficient

### Multiple Servers
- Redis pub/sub as external message broker
- Sticky sessions or shared state
- Consider dedicated WebSocket service at scale

---

*This document defines the complete WebSocket event architecture for Ascend.*
