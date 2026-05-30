# Implementation Plan: UI Core Shell

## Overview

This plan implements the foundational infrastructure layer of the Ascend app: routing configuration, bottom tab navigation, route guards (auth, onboarding, premium), HTTP interceptor pipeline (auth, loading, retry, error), Firebase/Firestore initialization, offline connectivity handling, skeleton loaders, error states, and platform services (haptic feedback, connectivity detection, local storage). The implementation builds incrementally — environment/types first, then services, then guards, then interceptors, then shared UI components, and finally wiring everything together in the app shell.

## Tasks

- [x] 1. Set up environment types, Firebase initialization, and HttpContext tokens
  - [x] 1.1 Create typed environment interfaces and update environment files
    - Create `src/environments/environment.interface.ts` with `FirebaseConfig` and `Environment` interfaces
    - Update `environment.ts` and `environment.prod.ts` to satisfy the typed interface
    - Ensures compile-time enforcement of required Firebase properties
    - _Requirements: 10.4, 10.8_

  - [x] 1.2 Install and configure Firebase and @angular/fire providers in app.config.ts
    - Add `provideFirebaseApp`, `provideAuth`, `provideFirestore` to `app.config.ts` providers
    - Enable Firestore offline persistence with `persistentLocalCache`
    - Register Firebase providers before router and HTTP providers
    - Handle multi-tab persistence failure gracefully (log warning, continue with in-memory cache)
    - _Requirements: 10.1, 10.2, 10.3, 10.5, 10.6, 10.7_

  - [x] 1.3 Create HttpContext tokens for skip-loading and retryable markers
    - Create `src/core/interceptors/http-context-tokens.ts`
    - Define `SKIP_LOADING` HttpContext token (default: false)
    - Define `RETRYABLE` HttpContext token (default: false)
    - _Requirements: 8.7, 9.4_

- [ ] 2. Implement platform services (Storage, Connectivity, Haptic, Loading)
  - [x] 2.1 Implement StorageService with Capacitor Preferences
    - Create `src/core/services/storage.service.ts`
    - Implement `get<T>(key)`, `set(key, value)`, `remove(key)`, `clear()` methods
    - Add `ascend_` key prefix, 256-char key validation, empty key rejection
    - Handle JSON serialization/deserialization with error handling for circular refs
    - All Capacitor Preferences failures resolve gracefully (log warning, return null/void)
    - _Requirements: 16.1, 16.2, 16.3, 16.4, 16.5, 16.6, 16.7, 16.8, 16.9_

  - [ ]* 2.2 Write property tests for StorageService
    - **Property 16: Storage service round-trip** — For any JSON-serializable value and valid key, `set` then `get` returns deeply equal value
    - **Property 17: Storage key prefixing** — For any valid key, underlying Capacitor call receives `ascend_` prefix
    - **Property 18: Storage graceful failure handling** — For any storage operation where Capacitor throws, service does not propagate exception
    - **Property 19: Storage key validation** — For any empty or >256 char key, operation is rejected gracefully
    - **Validates: Requirements 16.3, 16.4, 16.6, 16.9**

  - [x] 2.3 Implement ConnectivityService with Capacitor Network plugin
    - Create `src/core/services/connectivity.service.ts`
    - Expose `isOnline: Signal<boolean>` initialized with actual network state
    - Use Capacitor Network plugin on native, fallback to `navigator.onLine` + window events on web
    - Handle plugin unavailability gracefully (fallback without error)
    - Signal updates within 1 second of actual network change
    - _Requirements: 15.1, 15.2, 15.3, 15.4, 15.5, 15.6, 15.7, 15.8_

  - [x] 2.4 Implement HapticService with Capacitor Haptics plugin
    - Create `src/core/services/haptic.service.ts`
    - Implement `impact(style)`, `notification(type)`, `vibrate(duration)` methods
    - Read `haptic_enabled` preference from StorageService (default: true)
    - No-op when device doesn't support haptics or preference is disabled
    - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5, 14.6, 14.7_

  - [ ]* 2.5 Write property test for HapticService
    - **Property 15: Haptic no-op when unsupported or disabled** — For any haptic method call, if device unsupported OR preference disabled, resolves without vibration or error
    - **Validates: Requirements 14.3, 14.4**

  - [x] 2.6 Implement LoadingService with signal-based loading counter
    - Create `src/core/services/loading.service.ts`
    - Expose `isLoading: Signal<boolean>` with 300ms debounce logic
    - Implement `increment()` and `decrement()` methods (counter never below zero)
    - Show indicator only after 300ms continuous loading; hide immediately when counter hits zero
    - _Requirements: 8.3, 8.4, 8.5, 8.6_

- [ ] 3. Checkpoint - Ensure all services compile and pass tests
  - Ensure all tests pass, ask the user if questions arise.

- [x] 4. Implement route guards (Auth, Onboarding, Premium)
  - [x] 4.1 Implement authGuard with Firebase Auth observable and timeout
    - Update `src/core/auth/auth.guard.ts`
    - Subscribe to Firebase Auth `onAuthStateChanged` via @angular/fire
    - Wait for first emission with 5-second timeout
    - Store originally requested URL for post-login redirect
    - Redirect to `/auth/login` if unauthenticated or timeout
    - Apply as `canActivate` on the `tabs` route
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

  - [ ]* 4.2 Write property test for authGuard URL storage
    - **Property 1: Auth guard stores return URL** — For any valid route URL attempted by unauthenticated user, the guard stores that exact URL and redirects to `/auth/login`
    - **Validates: Requirements 3.1**

  - [x] 4.3 Implement onboardingGuard with dual-source check
    - Update `src/core/auth/onboarding.guard.ts`
    - Check Capacitor Preferences (`onboarding_complete`) first via StorageService
    - Fall back to user profile onboarding status if preference not `true`
    - Redirect to `/onboarding` if incomplete or both sources fail within 5 seconds
    - Apply as `canActivate` on the `tabs` route
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6_

  - [x] 4.4 Implement premiumGuard with UserStore signal check
    - Update `src/core/auth/premium.guard.ts`
    - Read `premiumStatus` from UserStore signal
    - Redirect to premium upgrade page if not premium or user is null/undefined
    - Pass attempted feature name as query param for display on upgrade page
    - Apply to AI Coach and advanced analytics routes
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6_

  - [ ]* 4.5 Write unit tests for all three guards
    - Test auth guard timeout, emission, and redirect scenarios
    - Test onboarding guard dual-source logic and fallback
    - Test premium guard boolean logic and null user handling
    - _Requirements: 3.1–3.6, 4.1–4.6, 5.1–5.6_

- [-] 5. Implement HTTP interceptors (Auth, Loading, Retry, Error)
  - [x] 5.1 Implement authInterceptor with token refresh and request queuing
    - Update `src/core/interceptors/auth.interceptor.ts`
    - Attach Bearer token only for requests starting with `environment.apiUrl`
    - Force-refresh token when less than 5 minutes to expiry
    - Queue concurrent requests during token refresh (single refresh call)
    - Forward without token if retrieval fails or times out (10s)
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6_

  - [ ]* 5.2 Write property tests for authInterceptor
    - **Property 2: Auth interceptor attaches token for API URLs** — For any request URL starting with `environment.apiUrl` with valid user, clones request with Bearer header
    - **Property 3: Auth interceptor passes through non-API URLs** — For any request URL NOT starting with `environment.apiUrl`, forwards without modifying Authorization header
    - **Validates: Requirements 6.1, 6.2**

  - [x] 5.3 Implement loadingInterceptor with counter management
    - Create `src/core/interceptors/loading.interceptor.ts`
    - Increment LoadingService counter on request start (unless SKIP_LOADING token)
    - Decrement on request complete (success, error, or cancellation)
    - Skip counter management entirely for requests with SKIP_LOADING token
    - _Requirements: 8.1, 8.2, 8.7_

  - [ ]* 5.4 Write property tests for loadingInterceptor
    - **Property 6: Loading counter increments for trackable requests** — For any request without SKIP_LOADING token, counter increments by exactly 1
    - **Property 7: Loading counter invariant — never negative** — For any sequence of starts/completions, counter never falls below zero
    - **Property 8: Loading counter ignores skip-loading requests** — For any request with SKIP_LOADING token, counter remains unchanged
    - **Validates: Requirements 8.1, 8.2, 8.7**

  - [x] 5.5 Implement retryInterceptor with exponential backoff
    - Create `src/core/interceptors/retry.interceptor.ts`
    - Retry up to 3 times with 1000ms, 2000ms, 4000ms delays for transient failures
    - Respect Retry-After header for 429 responses (clamped to 60s max)
    - No retry for non-retryable client errors (4xx except 408, 429)
    - No retry for non-idempotent methods (POST, PUT, PATCH) unless RETRYABLE token present
    - Abort retry sequence on caller cancellation (unsubscribe)
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6_

  - [ ]* 5.6 Write property tests for retryInterceptor
    - **Property 9: Retry with exponential backoff for transient failures** — For any retryable request failing with network/timeout/5xx, retries up to 3 times with correct delays
    - **Property 10: Retry-After header clamped to 60 seconds** — For any 429 with Retry-After, uses min(value, 60000) as delay
    - **Property 11: No retry for non-retryable client errors** — For any 4xx (not 408/429), immediately propagates error
    - **Property 12: No retry for non-idempotent requests without retryable token** — For any POST/PUT/PATCH without RETRYABLE token, no retry regardless of error
    - **Validates: Requirements 9.1, 9.2, 9.3, 9.4**

  - [x] 5.7 Implement errorInterceptor with toast notifications and 401 logout
    - Update `src/core/interceptors/error.interceptor.ts`
    - Handle 401: clear Firebase Auth, reset UserStore, navigate to login, re-throw
    - Handle 403: show "insufficient permissions" toast (3000ms), re-throw
    - Handle 5xx: show "server problem, retry" toast (4000ms), re-throw
    - Handle network error (status 0): re-throw silently (no toast)
    - Handle other 4xx: re-throw without toast
    - Deduplicate toasts of same category while one is visible
    - Log errors to console in non-production environments
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7_

  - [ ]* 5.8 Write property tests for errorInterceptor
    - **Property 4: Error interceptor toasts for server errors** — For any 500-599 response, displays error toast (4000ms) and re-throws
    - **Property 5: Error interceptor silent for non-special client errors** — For any 400-499 (not 401/403), re-throws without toast
    - **Validates: Requirements 7.3, 7.6**

- [ ] 6. Checkpoint - Ensure all interceptors and guards compile and pass tests
  - Ensure all tests pass, ask the user if questions arise.

- [x] 7. Implement shared UI components (Skeleton Loader, Error State, Offline Banner)
  - [x] 7.1 Implement SkeletonLoaderComponent
    - Create `src/shared/components/skeleton-loader/skeleton-loader.component.ts` and `.scss`
    - Support `shape` (rectangle, circle, text-line), `width`, `height`, `borderRadius` inputs
    - Implement shimmer animation via CSS keyframes (1-2s cycle)
    - Derive colors from CSS custom properties (light/dark mode support)
    - Fall back to defaults for zero/negative dimension inputs
    - Export as standalone component
    - _Requirements: 12.1, 12.2, 12.3, 12.5, 12.6, 12.7_

  - [ ]* 7.2 Write property test for SkeletonLoaderComponent
    - **Property 13: Skeleton loader dimension handling** — For any dimension input, renders with provided value if valid positive, or falls back to defaults if zero/negative/invalid
    - **Validates: Requirements 12.2, 12.7**

  - [x] 7.3 Implement ErrorStateComponent with retry functionality
    - Create `src/shared/components/error-state/error-state.component.ts` and `.scss`
    - Accept `message` input (max 150 chars displayed, truncated if longer) and `icon` input
    - Emit `retry` event via @Output() on "Try Again" button tap
    - Disable button during retry, show skeleton loader while retrying
    - Re-display error state if retry also fails
    - Default message: "Something went wrong. Tap to try again." Default icon: `alert-circle-outline`
    - Export as standalone component
    - _Requirements: 13.1, 13.2, 13.3, 13.4, 13.5, 13.6, 13.7, 13.8_

  - [ ]* 7.4 Write property test for ErrorStateComponent
    - **Property 14: Error state message truncation** — For any message string, displayed text is at most 150 characters; longer inputs are truncated
    - **Validates: Requirements 13.3**

  - [x] 7.5 Implement OfflineBannerComponent with connectivity transitions
    - Create `src/shared/components/offline-banner/offline-banner.component.ts` and `.scss`
    - Inject ConnectivityService, show amber banner when offline
    - Show "Back online" green banner on reconnection, auto-dismiss after 3 seconds
    - Animate with slide-down/slide-up CSS transition (300ms)
    - Non-intrusive: no overlay, no pointer-events blocking
    - Show immediately if app starts offline
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6, 11.7_

  - [ ]* 7.6 Write unit tests for shared UI components
    - Test skeleton loader shape rendering and default fallbacks
    - Test error state retry emission and message truncation
    - Test offline banner visibility transitions and auto-dismiss timing
    - _Requirements: 11.1–11.7, 12.1–12.7, 13.1–13.8_

- [x] 8. Wire routing, guards, interceptors, and shell together
  - [x] 8.1 Update app.routes.ts with guards and wildcard redirect
    - Add `authGuard` and `onboardingGuard` as `canActivate` on the `tabs` route
    - Update root redirect from `tabs` to `tabs/home`
    - Add wildcard route (`**`) redirecting to `/tabs/home`
    - _Requirements: 1.1, 1.4, 3.5, 5.6_

  - [x] 8.2 Update tabs.routes.ts with premium guard on specific child routes
    - Apply `premiumGuard` to AI Coach and advanced analytics child routes
    - Ensure all child routes use `loadChildren` with dynamic `import()` for separate chunks
    - _Requirements: 1.3, 1.6, 4.4_

  - [x] 8.3 Update app.config.ts with full interceptor pipeline and Firebase providers
    - Register interceptors in order: authInterceptor, loadingInterceptor, retryInterceptor, errorInterceptor
    - Ensure Firebase providers are registered before router and HTTP providers
    - Register `PreloadAllModules` strategy (already present, verify)
    - _Requirements: 1.5, 10.6_

  - [x] 8.4 Update app.component.ts to include OfflineBannerComponent
    - Add `<app-offline-banner>` above `<ion-router-outlet>` in template
    - Import OfflineBannerComponent in component imports array
    - _Requirements: 11.6_

  - [x] 8.5 Update TabsPage with active tab styling and tap-to-root/scroll behavior
    - Implement filled icon variant for active tab with primary accent color
    - Implement tap-on-active-tab: pop to root if stack > 1, scroll to top if at root
    - Ensure 48x48 CSS pixel minimum touch targets
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7_

  - [ ]* 8.6 Write unit tests for routing configuration and wiring
    - Verify route paths, lazy loading, guard application
    - Verify interceptor registration order
    - Verify tab bar rendering and active state
    - _Requirements: 1.1–1.6, 2.1–2.7_

- [ ] 9. Final checkpoint - Ensure all tests pass and app compiles
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- **RULE: Do NOT run any mvn, gradle, npm, or test commands. Only create/edit files. No build or test verification steps.**

- **IMPORTANT: Do NOT run any terminal commands (npm, ng, npx, etc.). Only create or update files directly.**
- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties from the design document
- Unit tests validate specific examples and edge cases
- The project uses Jasmine + Karma for testing; fast-check should be installed for property-based tests
- Existing placeholder guards and interceptors will be updated in-place rather than recreated
- All components are standalone Angular components following the project's existing patterns

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.3"] },
    { "id": 1, "tasks": ["1.2", "2.1"] },
    { "id": 2, "tasks": ["2.2", "2.3", "2.6"] },
    { "id": 3, "tasks": ["2.4", "2.5", "5.3"] },
    { "id": 4, "tasks": ["4.1", "4.3", "4.4", "5.4"] },
    { "id": 5, "tasks": ["4.2", "4.5", "5.1", "5.5"] },
    { "id": 6, "tasks": ["5.2", "5.6", "5.7"] },
    { "id": 7, "tasks": ["5.8", "7.1", "7.3"] },
    { "id": 8, "tasks": ["7.2", "7.4", "7.5"] },
    { "id": 9, "tasks": ["7.6", "8.1", "8.2"] },
    { "id": 10, "tasks": ["8.3", "8.4", "8.5"] },
    { "id": 11, "tasks": ["8.6"] }
  ]
}
```
