# Implementation Plan: Real-Time Communication

## Overview

WebSocket STOMP channels for guild chat, leaderboard updates, XP notifications, and user presence via Firestore realtime listeners.

## Tasks

- [x] 1. Configure WebSocket Infrastructure
  - [x] 1.1 Set up Spring WebSocket with STOMP
    - Update `WebSocketConfig.java`:
      1. Enable STOMP messaging (@EnableWebSocketMessageBroker)
      2. Configure SockJS fallback endpoint: `/ws`
      3. Set application destination prefix: `/app`
      4. Set broker prefixes: `/topic` (broadcast), `/user` (private)
      5. Configure allowed origins (frontend URLs)
      6. Set message size limits and send buffer limits
  - [x] 1.2 Create WebSocket authentication
    - Create `WebSocketAuthInterceptor.java`
    - Validate Firebase JWT from STOMP CONNECT frame headers
    - Set authenticated principal for the WebSocket session
    - Reject connections with invalid/expired tokens

- [x] 2. Implement Private User Channels
  - [x] 2.1 Create user notification broadcasting
    - Create `UserNotificationBroadcaster.java`
    - `sendXpUpdate(UUID userId, int xpGained, int newTotal, int newLevel)`:
      - Send to `/user/{userId}/queue/xp`
    - `sendLevelUp(UUID userId, int newLevel, List<String> unlocks)`:
      - Send to `/user/{userId}/queue/level`
    - `sendStreakAlert(UUID userId, String message, int currentStreak)`:
      - Send to `/user/{userId}/queue/streak`
    - `sendBossProgress(UUID userId, String bossName, int progress)`:
      - Send to `/user/{userId}/queue/boss`
    - `sendNotification(UUID userId, String title, String message)`:
      - Send to `/user/{userId}/queue/notifications`
  - [x] 2.2 Create event listeners for real-time broadcasting
    - Listen for `XpAwardedEvent` → broadcast XP update to user
    - Listen for `LevelUpEvent` → broadcast level-up to user
    - Listen for `StreakBrokenEvent` → broadcast streak alert
    - Listen for `BossDefeatedEvent` → broadcast boss update
    - Listen for `AchievementUnlockedEvent` → broadcast notification

- [x] 3. Implement Leaderboard Broadcasting
  - [x] 3.1 Create leaderboard real-time updates
    - Create `LeaderboardBroadcaster.java`
    - `broadcastRankUpdate(String league, LeaderboardEntry entry)`:
      - Send to `/topic/leaderboard/{league}`
    - Triggered when user's weekly XP changes significantly (debounced — not every XP award)
    - Debounce: broadcast at most every 30 seconds per league

- [x] 4. Implement User Presence (Firestore)
  - [x] 4.1 Create presence service (Frontend)
    - Create `src/core/services/presence.service.ts`
    - On app open: set Firestore `presence/{userId}` to { online: true, lastSeen: now() }
    - On app close/background: set { online: false, lastSeen: now() }
    - Use Firestore `onDisconnect` equivalent (or periodic heartbeat)
    - Expose `getUserPresence(userId)` for UI display
  - [x] 4.2 Create presence display components
    - Create online/offline indicator component (green/gray dot)
    - Show "last seen X minutes ago" for offline users
    - Used in guild member lists, friend lists, leaderboards

- [x] 5. Create WebSocket Frontend Client
  - [x] 5.1 Create STOMP client service
    - Create `src/core/services/websocket.service.ts`
    - Connect to `/ws` endpoint with SockJS
    - Attach Firebase JWT in CONNECT headers
    - Subscribe to user-specific channels on connect
    - Handle reconnection with exponential backoff (5s, 10s, 20s, 40s)
    - Expose observables/signals for each channel type
  - [x] 5.2 Create real-time notification handling
    - Create `src/core/services/realtime-notifications.service.ts`
    - Subscribe to `/user/{userId}/queue/notifications`
    - Display toast/snackbar for incoming notifications
    - Update relevant stores (XP, level, streak) on real-time updates
    - No page refresh needed — signals update reactively

- [x] 6. Checkpoint - Verify real-time communication
  - Integration test: XP awarded → WebSocket message received by connected client
  - Integration test: guild chat message → broadcast to all guild members
  - Integration test: WebSocket reconnects after disconnect
  - Frontend test: presence updates on app open/close
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- **RULE: Do NOT run any mvn, gradle, or test commands. Only create/edit files. No build or test verification steps.**
- WebSocket uses STOMP over SockJS for broad browser compatibility
- Private channels use /user prefix (Spring handles routing by principal)
- Leaderboard broadcasts are debounced to prevent flooding
- Presence uses Firestore (not WebSocket) for simplicity and offline detection
- Reconnection is automatic with exponential backoff

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.2"] },
    { "id": 1, "tasks": ["2.1", "2.2", "3.1"] },
    { "id": 2, "tasks": ["4.1", "4.2", "5.1", "5.2"] },
    { "id": 3, "tasks": ["6"] }
  ]
}
```
