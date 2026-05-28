# 12 — Security Architecture

# Ascend — Enter Arc Mode

---

## Security Layers

### Layer 1: Firebase Authentication
Client-side authentication with JWT token generation.

### Layer 2: Spring JWT Validation
Backend validates Firebase JWT on every request.

### Layer 3: Role Authorization (RBAC)
Role-based access control for all endpoints.

### Layer 4: Rate Limiting
Prevent abuse and spam.

### Layer 5: Anti-Cheat
Detect and prevent gameplay exploitation.

---

## Role-Based Access Control (RBAC)

### Roles

| Role | Access Level |
|------|-------------|
| USER | Standard app features |
| PREMIUM_USER | All premium features |
| MODERATOR | User moderation tools |
| ADMIN | Full admin panel |
| SUPER_ADMIN | System configuration |

### Route Protection

```
/api/v1/**           → USER (minimum)
/api/v1/premium/**   → PREMIUM_USER
/api/v1/admin/**     → ADMIN
/api/v1/system/**    → SUPER_ADMIN
```

---

## Authentication Flow

```
1. User logs in via Firebase (client-side)
2. Firebase returns JWT token
3. Angular stores token securely
4. Every API request includes: Authorization: Bearer {token}
5. Spring Security filter validates token
6. Firebase Admin SDK verifies signature + expiration
7. User context created for request lifecycle
8. Role checked against endpoint requirements
```

### Token Validation Rules
- Verify JWT signature (Firebase public keys)
- Check token expiration
- Validate issuer claim
- Extract user ID and roles
- Never trust frontend role claims

---

## Rate Limiting

### Purpose
Prevent abuse, spam, and bot activity.

### Limits

| Endpoint Category | Rate Limit |
|------------------|-----------|
| Quest completion | 20/minute |
| API general | 100/minute |
| Auth endpoints | 10/minute |
| Guild chat | 30/minute |
| Leaderboard | 60/minute |

### Implementation
- Spring Boot rate limiter (Bucket4j or custom)
- Redis-backed token bucket
- Per-user rate tracking
- 429 Too Many Requests response

---

## Anti-Cheat System

### Detection Rules

| Violation | Detection | Penalty |
|-----------|-----------|---------|
| 50 quests in 2 mins | Speed detection | Account flag |
| Impossible streak growth | Pattern analysis | XP rollback |
| Duplicate reward claims | Idempotency check | Block + log |
| XP farming (repeat easy quests) | Diminishing returns | Reduced XP |
| Bot-like behavior | Activity pattern analysis | Temporary ban |

### XP Validation
- Server calculates all XP (never trust client)
- Validate quest completion timing
- Check for impossible combinations
- Log all XP transactions for audit

### Speed Detection
```
IF (questsCompleted > 10 AND timePeriod < 5 minutes)
  THEN flagAccount()
```

### Duplicate Prevention
- Unique constraint: (user_id, quest_id, date)
- Idempotency keys for critical operations
- Server-side completion timestamp validation

---

## Secure Storage (Client)

### Ionic Secure Storage
Store sensitive data:
- JWT token
- Session data
- User preferences

### Never store in localStorage:
- Tokens
- Passwords
- Payment info
- Personal data

### Implementation
```typescript
import { Preferences } from '@capacitor/preferences';

// Store securely
await Preferences.set({ key: 'auth_token', value: token });

// Retrieve
const { value } = await Preferences.get({ key: 'auth_token' });
```

---

## Encryption

### Transport Security
- HTTPS only (TLS 1.3)
- No HTTP fallback
- Certificate pinning (mobile)
- HSTS headers

### Data at Rest
- Database encryption (PostgreSQL)
- Firebase encryption (automatic)
- Sensitive fields encrypted in application layer

---

## Firebase Security Rules

### Core Principle
Frontend is untrusted. Never calculate:
- ❌ XP
- ❌ Streaks
- ❌ Rewards
- ❌ Rankings

in Firebase or client-side.

### Firestore Rules
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can only access own data
    match /users/{userId} {
      allow read, write: if request.auth.uid == userId;
    }
    
    // Leaderboard: read-only for users, write-only for backend
    match /leaderboard-cache/{doc} {
      allow read: if request.auth != null;
      allow write: if false;
    }
  }
}
```

---

## Input Validation

### Server-Side Validation
All inputs validated on backend:
- String length limits
- Numeric range checks
- Enum value validation
- SQL injection prevention (parameterized queries)
- XSS prevention (output encoding)

### Example
```java
@Valid
public class QuestCompletionRequest {
    @NotNull
    @Size(min = 1, max = 36)
    private String questId;
}
```

---

## CORS Configuration

```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        // Allow only known origins
        // Allow specific HTTP methods
        // Allow specific headers
        // Credentials support
    }
}
```

### Allowed Origins
- `https://ascend-app.web.app` (production)
- `http://localhost:8100` (development)
- Mobile apps (Capacitor - no CORS needed)

---

## Session Management

### Stateless Architecture
- No server-side sessions
- JWT-based authentication
- Token refresh via Firebase
- Automatic token rotation

### Token Lifecycle
- Access token: 1 hour (Firebase default)
- Refresh token: automatic renewal
- Revocation: Firebase Admin SDK

---

## Logging & Audit

### Security Events Logged
- Login attempts (success/failure)
- Permission denied events
- Rate limit violations
- Anti-cheat flags
- Admin actions
- Data access patterns

### Log Format
```json
{
  "timestamp": "2026-05-28T10:30:00Z",
  "event": "QUEST_COMPLETION",
  "userId": "uuid",
  "ip": "masked",
  "result": "SUCCESS",
  "metadata": {}
}
```

### PII Protection
- Never log passwords
- Mask email addresses in logs
- Anonymize IP addresses
- Rotate logs (30-day retention)

---

## Vulnerability Prevention

### OWASP Top 10 Coverage

| Vulnerability | Mitigation |
|--------------|-----------|
| Injection | Parameterized queries, input validation |
| Broken Auth | Firebase Auth, JWT validation |
| Sensitive Data Exposure | HTTPS, encryption, secure storage |
| XXE | Disabled XML parsing |
| Broken Access Control | RBAC, resource ownership checks |
| Security Misconfiguration | Hardened defaults, no debug in prod |
| XSS | Output encoding, CSP headers |
| Insecure Deserialization | Type-safe DTOs, validation |
| Known Vulnerabilities | Dependency scanning, updates |
| Insufficient Logging | Comprehensive audit trail |

---

## Incident Response

### Severity Levels

| Level | Example | Response Time |
|-------|---------|--------------|
| Critical | Data breach, auth bypass | Immediate |
| High | XP exploit, mass cheating | 1 hour |
| Medium | Rate limit bypass | 4 hours |
| Low | Minor UI exploit | 24 hours |

### Response Steps
1. Detect (monitoring + alerts)
2. Contain (disable affected feature)
3. Investigate (audit logs)
4. Fix (patch + deploy)
5. Communicate (if user-facing)
6. Review (post-mortem)

---

*This document defines the complete security architecture for Ascend.*
