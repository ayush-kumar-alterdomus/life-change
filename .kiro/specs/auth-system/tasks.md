# Implementation Plan: Auth System

## Overview

Firebase authentication integration with Spring Boot backend. Covers JWT validation, multi-provider auth (Google, Apple, Email, Guest), RBAC, rate limiting, and security configuration.

## Tasks

- [x] 1. Configure Firebase Admin SDK
  - [x] 1.1 Add Firebase Admin SDK dependency and initialization
    - Add `firebase-admin` dependency to `pom.xml`
    - Create `FirebaseConfig.java` in `auth/config/` that initializes FirebaseApp from service account JSON
    - Add `firebase-service-account.json` path to `application.yml` (externalized, not committed)
    - Add `.gitignore` entry for service account files
  - [x] 1.2 Create Firebase token verification service
    - Create `FirebaseTokenService.java` in `auth/service/`
    - Implement `verifyToken(String idToken)` that calls `FirebaseAuth.getInstance().verifyIdToken()`
    - Return decoded token with uid, email, provider, and custom claims
    - Handle `FirebaseAuthException` with appropriate error responses

- [x] 2. Implement JWT security filter
  - [x] 2.1 Create Firebase JWT authentication filter
    - Create `FirebaseTokenFilter.java` extending `OncePerRequestFilter`
    - Extract Bearer token from Authorization header
    - Verify token via `FirebaseTokenService`
    - Set `SecurityContextHolder` with authenticated principal containing uid, email, roles
    - Skip filter for public endpoints (/auth/login, /actuator/health)
  - [x] 2.2 Configure Spring Security
    - Update `SecurityConfig.java` with `SecurityFilterChain` bean
    - Disable CSRF (stateless API)
    - Configure CORS for frontend origins (localhost:4200, production domain)
    - Add `FirebaseTokenFilter` before `UsernamePasswordAuthenticationFilter`
    - Define public endpoints: POST /auth/login, GET /actuator/health
    - All other endpoints require authentication

- [x] 3. Implement auth endpoints
  - [x] 3.1 Create auth DTOs
    - Create `LoginRequest.java` with `idToken` field
    - Create `LoginResponse.java` with user profile fields (id, username, email, level, xp, league, premium, avatarUrl)
    - Create `RegisterRequest.java` with email, password, username fields
    - Create `UserProfileResponse.java` for GET /auth/me
  - [x] 3.2 Create AuthController
    - Create `AuthController.java` in `auth/controller/`
    - POST `/api/v1/auth/login` — accepts Firebase ID token, validates, finds/creates user, returns profile
    - GET `/api/v1/auth/me` — returns current authenticated user profile
    - POST `/api/v1/auth/register` — creates Firebase user + PostgreSQL record (email/password flow)
  - [x] 3.3 Create AuthService
    - Create `AuthService.java` in `auth/service/`
    - `loginOrRegister(String firebaseUid, String email, String provider)` — find existing user or create new one
    - `getCurrentUser(String firebaseUid)` — fetch user by firebase_uid
    - `createUser(String firebaseUid, String email, String username)` — create user record with defaults (level 1, xp 0, Bronze league)
    - Also create initial `Streak` and `UserStats` records for new users

- [x] 4. Implement RBAC
  - [x] 4.1 Create role enum and role-based access
    - Create `UserRole.java` enum: USER, PREMIUM_USER, MODERATOR, ADMIN, SUPER_ADMIN
    - Add `role` column to users table (migration `V19__add_user_role.sql`)
    - Update `User.java` entity with role field (default USER)
    - Create `@RequireRole` custom annotation for method-level security
  - [x] 4.2 Implement role checking in security context
    - Update `FirebaseTokenFilter` to load user role from database and set as granted authority
    - Create `RoleCheckAspect.java` or use `@PreAuthorize` with custom expressions
    - Verify ADMIN/SUPER_ADMIN access for admin endpoints
    - Verify PREMIUM_USER access for premium-gated endpoints

- [x] 5. Implement rate limiting
  - [x] 5.1 Create rate limiting infrastructure
    - Add `bucket4j-spring-boot-starter` dependency (or implement with Redis)
    - Create `RateLimitConfig.java` defining rate limit buckets per endpoint category
    - Quest completion: 20 requests/minute
    - API general: 100 requests/minute
    - Auth endpoints: 10 requests/minute
    - Guild chat: 30 requests/minute
  - [x] 5.2 Create rate limit filter
    - Create `RateLimitFilter.java` or use Bucket4j annotations
    - Key rate limits by user ID (authenticated) or IP (unauthenticated)
    - Return 429 Too Many Requests with `Retry-After` header when exceeded
    - Log rate limit violations to security event log

- [x] 6. Implement security event logging
  - [x] 6.1 Create security audit logging
    - Create `security_events` table migration (`V20__create_security_events_table.sql`)
    - Columns: id, user_id, event_type, ip_address, details (JSONB), created_at
    - Create `SecurityEvent.java` entity and `SecurityEventRepository.java`
    - Create `SecurityAuditService.java` that logs: login attempts, permission denials, rate limit violations, anti-cheat flags
    - Mask PII in log details (email → a***@example.com)

- [x] 7. Implement Guest Mode
  - [x] 7.1 Create guest user handling
    - In `AuthService`, detect anonymous Firebase tokens (no email, anonymous provider)
    - Create guest user record with generated username ("Guest_XXXXX"), role=USER
    - Create `GuestRestrictionAspect.java` or middleware that blocks: leaderboard access, guild features, cloud sync for guest users
    - Add `is_guest` boolean to users table (migration `V21__add_guest_flag.sql`)

- [x] 8. Checkpoint - Verify auth system
  - Test login flow with Firebase ID token (mock in integration test)
  - Test JWT validation rejects expired/invalid tokens
  - Test RBAC blocks unauthorized access
  - Test rate limiting returns 429 on excess requests
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- **RULE: Do NOT run any mvn, gradle, npm, or test commands. Only create/edit files. No build or test verification steps.**

- Firebase Admin SDK handles token verification — no custom JWT signing needed
- Rate limiting uses Redis for distributed state across instances
- Security events use JSONB for flexible detail storage
- Guest users get full quest/XP access but restricted social features
- All auth errors return generic messages to prevent information leakage
