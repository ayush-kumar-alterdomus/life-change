# 11 — API Contracts

# Ascend — Enter Arc Mode

---

## API Standards

### Base URL
```
/api/v1/
```

### Authentication
All endpoints require Firebase JWT token:
```
Authorization: Bearer {firebase_jwt_token}
```

### Response Format (Success)
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {}
}
```

### Response Format (Error)
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

# AUTHENTICATION APIs

---

## POST /api/v1/auth/login

Validate Firebase token and create/retrieve user session.

**Request:**
```json
{
  "firebaseToken": "eyJhbGciOiJSUzI1NiIs..."
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "userId": "uuid",
    "username": "Ayush",
    "level": 12,
    "xp": 2400,
    "league": "Silver",
    "currentArc": "monk_arc",
    "premium": false
  }
}
```

---

## GET /api/v1/auth/me

Get current authenticated user profile.

**Response:**
```json
{
  "success": true,
  "data": {
    "userId": "uuid",
    "username": "Ayush",
    "email": "user@email.com",
    "avatar": "url",
    "level": 12,
    "xp": 2400,
    "league": "Silver",
    "premium": false,
    "stats": {
      "strength": 120,
      "wisdom": 85,
      "focus": 200,
      "discipline": 150,
      "vitality": 90,
      "charisma": 45
    }
  }
}
```

---

# DASHBOARD APIs

---

## GET /api/v1/dashboard

Get complete dashboard data.

**Response:**
```json
{
  "success": true,
  "data": {
    "user": {
      "username": "Ayush",
      "level": 12,
      "xp": 2400,
      "xpToNextLevel": 3000
    },
    "dailyQuests": [],
    "streak": {
      "days": 14,
      "comboMultiplier": 1.14
    },
    "currentArc": {
      "name": "Monk Arc",
      "progress": 43
    },
    "leaderboardPosition": 45,
    "lifeScore": 72.5
  }
}
```

---

## GET /api/v1/dashboard/summary

Get daily summary stats.

**Response:**
```json
{
  "success": true,
  "data": {
    "questsCompleted": 4,
    "questsTotal": 6,
    "xpEarnedToday": 180,
    "streakDays": 14,
    "focusScore": 85
  }
}
```

---

# QUEST APIs

---

## GET /api/v1/quests/daily

Get today's daily quests.

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "quest_001",
      "title": "Morning Workout",
      "description": "Complete 45 min strength training",
      "xpReward": 40,
      "difficulty": "medium",
      "statType": "strength",
      "completed": false,
      "recurring": true
    }
  ]
}
```

---

## GET /api/v1/quests/weekly

Get weekly challenges.

---

## POST /api/v1/quests/complete

Complete a quest.

**Request:**
```json
{
  "questId": "quest_001"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Quest completed!",
  "data": {
    "xpEarned": 50,
    "multiplierApplied": 1.5,
    "totalXp": 75,
    "newLevel": false,
    "currentLevel": 12,
    "currentXp": 2475,
    "streak": 14,
    "statGain": {
      "type": "strength",
      "amount": 5
    },
    "rewardUnlocked": null
  }
}
```

---

## POST /api/v1/quests

Create custom quest.

**Request:**
```json
{
  "title": "Learn Spring Boot",
  "description": "Study for 1 hour",
  "difficulty": "hard",
  "frequency": "daily",
  "statType": "wisdom",
  "xpReward": 60
}
```

---

## PUT /api/v1/quests/{id}

Update quest.

---

## DELETE /api/v1/quests/{id}

Delete quest.

---

# ARC APIs

---

## GET /api/v1/arcs

Get all available arcs.

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "monk_arc",
      "name": "Monk Arc",
      "description": "Build discipline and focus",
      "durationDays": 60,
      "difficulty": "medium",
      "questCount": 45,
      "milestoneCount": 12
    }
  ]
}
```

---

## GET /api/v1/arcs/{id}

Get arc details with milestones.

---

## POST /api/v1/arcs/start

Start an arc.

**Request:**
```json
{
  "arcId": "monk_arc"
}
```

---

## GET /api/v1/arcs/progress

Get current arc progress.

**Response:**
```json
{
  "success": true,
  "data": {
    "arcId": "monk_arc",
    "arcName": "Monk Arc",
    "progressPercent": 43,
    "currentPhase": 2,
    "phaseName": "Discipline",
    "milestonesCompleted": 5,
    "milestonesTotal": 12,
    "daysRemaining": 34
  }
}
```

---

## PATCH /api/v1/arcs/progress

Update arc progress (milestone completion).

---

# XP APIs

---

## GET /api/v1/xp/summary

Get XP and level summary.

**Response:**
```json
{
  "success": true,
  "data": {
    "currentXp": 2400,
    "currentLevel": 12,
    "xpToNextLevel": 3000,
    "xpEarnedToday": 180,
    "dailyCap": 1240,
    "comboMultiplier": 1.14
  }
}
```

---

## GET /api/v1/xp/history

Get XP history.

**Query:** `?page=1&size=20&period=week`

**Response:**
```json
{
  "success": true,
  "data": {
    "history": [
      {
        "source": "quest",
        "sourceTitle": "Morning Workout",
        "xp": 50,
        "multiplier": 1.5,
        "statType": "strength",
        "createdAt": "2026-05-28T07:30:00Z"
      }
    ],
    "totalPages": 5
  }
}
```

---

# LEAGUE APIs

---

## GET /api/v1/league/leaderboard

Get current league leaderboard.

**Query:** `?league=silver&page=1&size=50`

**Response:**
```json
{
  "success": true,
  "data": {
    "league": "Silver",
    "myRank": 23,
    "users": [
      {
        "rank": 1,
        "username": "DisciplineKing",
        "level": 15,
        "weeklyXp": 2400,
        "avatar": "url"
      }
    ]
  }
}
```

---

## GET /api/v1/league/rank

Get user's current rank and league status.

---

# GUILD APIs

---

## POST /api/v1/guilds

Create guild.

**Request:**
```json
{
  "name": "Backend Warriors",
  "description": "Coding consistency guild",
  "type": "public",
  "maxMembers": 10
}
```

---

## GET /api/v1/guilds/{id}

Get guild details.

---

## POST /api/v1/guilds/{id}/join

Join a guild.

---

## GET /api/v1/guilds/{id}/members

Get guild members.

---

## GET /api/v1/guilds/{id}/challenges

Get guild challenges.

---

# SKILL TREE APIs

---

## GET /api/v1/skills/tree

Get user's skill tree.

**Response:**
```json
{
  "success": true,
  "data": {
    "arcId": "monk_arc",
    "skills": [
      {
        "id": "meditation",
        "name": "Meditation",
        "unlocked": true,
        "buff": "+5% Focus XP",
        "children": ["deep_focus"]
      },
      {
        "id": "deep_focus",
        "name": "Deep Focus",
        "unlocked": false,
        "buff": "+10% Focus XP",
        "requirements": ["meditation"],
        "children": ["mind_mastery"]
      }
    ]
  }
}
```

---

## POST /api/v1/skills/unlock

Unlock a skill node.

**Request:**
```json
{
  "skillId": "deep_focus"
}
```

---

# BOSS APIs

---

## GET /api/v1/boss/{id}

Get boss details and progress.

**Response:**
```json
{
  "success": true,
  "data": {
    "bossId": "discipline_demon",
    "name": "Discipline Demon",
    "description": "Maintain 30-day streak",
    "currentStage": 2,
    "totalStages": 3,
    "progressPercent": 65,
    "reward": {
      "xp": 500,
      "title": "Discipline Master",
      "cosmetic": "golden_aura"
    }
  }
}
```

---

## POST /api/v1/boss/attack

Record boss progress (triggered by quest completion).

**Response:**
```json
{
  "success": true,
  "data": {
    "damage": 15,
    "bossRemainingHp": 40,
    "defeated": false
  }
}
```

---

# ANALYTICS APIs

---

## GET /api/v1/analytics/weekly

Get weekly analytics report.

**Response:**
```json
{
  "success": true,
  "data": {
    "questsCompleted": 24,
    "questsMissed": 3,
    "xpEarned": 1200,
    "strongestStat": "focus",
    "weakestStat": "vitality",
    "streakDays": 14,
    "recommendation": "Sleep consistency should improve"
  }
}
```

---

## GET /api/v1/analytics/life-score

Get life score breakdown.

**Response:**
```json
{
  "success": true,
  "data": {
    "lifeScore": 72.5,
    "breakdown": {
      "discipline": 80,
      "focus": 75,
      "health": 60,
      "learning": 70,
      "consistency": 78
    }
  }
}
```

---

## GET /api/v1/analytics/heatmap

Get activity heatmap data.

**Query:** `?period=month`

---

# PREMIUM APIs

---

## GET /api/v1/premium/status

Get subscription status.

---

## POST /api/v1/premium/upgrade

Initiate premium upgrade.

---

## POST /api/v1/premium/webhook

Payment provider webhook handler.

---

# NOTIFICATION APIs

---

## GET /api/v1/notifications

Get user notifications.

**Query:** `?page=1&size=20&unreadOnly=true`

---

## PATCH /api/v1/notifications/read

Mark notifications as read.

**Request:**
```json
{
  "notificationIds": ["notif_001", "notif_002"]
}
```

---

# STREAK APIs

---

## GET /api/v1/streak

Get current streak info.

**Response:**
```json
{
  "success": true,
  "data": {
    "currentStreak": 14,
    "longestStreak": 32,
    "comboMultiplier": 1.14,
    "shieldAvailable": true,
    "lastCompletedAt": "2026-05-28T22:00:00Z"
  }
}
```

---

# PROFILE APIs

---

## GET /api/v1/profile/{userId}

Get public profile.

---

## PUT /api/v1/profile

Update own profile.

---

## GET /api/v1/profile/achievements

Get user achievements.

---

*This document defines the complete API contract for Ascend.*
