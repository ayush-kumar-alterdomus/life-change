# Requirements Document

## Introduction

This document defines the requirements for the Ascend app's core UI shell — the foundational layer that provides app routing, bottom tab navigation, route guards, HTTP interceptors, Firebase/Firestore initialization, offline connectivity handling, skeleton loaders, error states, and essential platform services (haptic feedback, connectivity detection, local storage).

## Glossary

| Term | Definition |
|------|-----------|
| Shell | The root application layout that wraps all feature modules with navigation, guards, and global UI overlays |
| Guard | An Angular route guard that conditionally allows or blocks navigation based on app state |
| Interceptor | An Angular HTTP interceptor that transforms requests/responses in the HTTP pipeline |
| Skeleton Loader | A placeholder UI that mimics the shape of content while data is loading |
| Haptic Feedback | Device vibration patterns triggered by user interactions for tactile confirmation |
| Connectivity Service | A service that monitors network status and exposes online/offline state reactively |
| Storage Service | An abstraction over Capacitor Preferences for local key-value persistence |

## Requirements

### Requirement 1: App Routing Configuration

**User Story:** As a user, I want the app to load feature modules on demand, so that initial startup is fast and only the code I need is downloaded.

#### Acceptance Criteria

1. WHEN the app navigates to the root path (`/`), THE router SHALL redirect to `/tabs/home`.
2. THE app routes SHALL define exactly three top-level paths: `auth`, `onboarding`, and `tabs`, each using `loadChildren` with a dynamic `import()` expression pointing to the respective feature route file.
3. THE `tabs` layout SHALL define child routes for `home`, `quests`, `arc-mode`, `social`, and `profile`, each using `loadChildren` with a dynamic `import()` expression so that the build produces a separate JavaScript chunk per feature.
4. WHEN a path that does not match any defined route is accessed, THE router SHALL redirect to `/tabs/home`.
5. THE route configuration SHALL register Angular's `PreloadAllModules` strategy via `withPreloading(PreloadAllModules)` in `provideRouter`, so that remaining lazy chunks begin downloading in the background after the initial route renders.
6. WHEN a user navigates to a tab path for the first time, THE router SHALL load only the JavaScript chunk for that feature before rendering the view, and no other feature chunks SHALL be required for that navigation to complete.

### Requirement 2: Bottom Tab Navigation

**User Story:** As a user, I want a persistent bottom tab bar with Home, Quests, Arc, Social, and Profile tabs, so that I can quickly switch between core sections of the app.

#### Acceptance Criteria

1. THE tab bar SHALL display exactly five tabs in left-to-right order: Home, Quests, Arc Mode, Social, and Profile, each showing an icon above a text label.
2. WHEN a tab is selected, THE tab bar SHALL display that tab's filled icon variant and apply the app's primary accent color to both the icon and label, while all other tabs SHALL display their outline icon variant and default text color.
3. THE tab bar SHALL remain visible on all pages within the tabs layout and SHALL NOT be rendered on auth or onboarding flows.
4. WHEN the user taps the currently active tab and the tab's navigation stack contains more than one view, THE system SHALL pop to the root view of that tab's stack. WHEN the user taps the currently active tab and the stack contains only the root view, THE system SHALL scroll the content to the top.
5. THE tab bar SHALL be positioned at the bottom of the viewport with each tab touch target measuring at least 48x48 CSS pixels.
6. THE tab bar SHALL use Ionic's `ion-tabs` component with `ion-tab-bar` slot="bottom".
7. WHEN the app is launched after authentication and onboarding are complete, THE tab bar SHALL default to the Home tab as the initially active tab.

### Requirement 3: Auth Guard

**User Story:** As a user, I want to be redirected to the login screen if I'm not authenticated, so that protected content remains secure.

#### Acceptance Criteria

1. WHEN an unauthenticated user attempts to access a guarded route, THEN the auth guard SHALL redirect to `/auth/login` and store the originally requested URL so the user can be redirected back after successful login.
2. THE auth guard SHALL determine authentication state by subscribing to Firebase Auth's `onAuthStateChanged` observable via @angular/fire and waiting for the first emission before making a navigation decision, with a maximum wait time of 5 seconds.
3. WHEN `onAuthStateChanged` emits a non-null `User` object, THEN the auth guard SHALL allow navigation to the requested route.
4. IF `onAuthStateChanged` emits `null` or fails to emit within 5 seconds, THEN the auth guard SHALL block navigation and redirect to `/auth/login`.
5. THE auth guard SHALL be applied as a `canActivate` guard on the `tabs` route, thereby protecting it and all its child routes.
6. WHILE the auth guard is waiting for the `onAuthStateChanged` observable to emit its first value, THE auth guard SHALL not allow navigation to proceed (the guard returns only after auth state resolves or the 5-second timeout elapses).

### Requirement 4: Premium Guard

**User Story:** As a non-premium user, I want to be shown an upgrade prompt when I try to access premium features, so that I understand what's available with a subscription.

#### Acceptance Criteria

1. WHEN a non-premium user navigates to a premium-only route, THE premium guard SHALL redirect the user to the premium upgrade page.
2. WHEN the premium guard evaluates a navigation request, THE premium guard SHALL read the current user's `premiumStatus` boolean from the UserStore signal to determine subscription status.
3. WHEN a user with `premiumStatus` equal to true navigates to a premium-only route, THE premium guard SHALL allow the navigation to proceed without redirection.
4. THE premium guard SHALL be applied as a `CanActivateFn` to the AI Coach route and the advanced analytics route.
5. IF the UserStore signal returns no user (null or undefined), THEN THE premium guard SHALL deny navigation and redirect to the premium upgrade page.
6. WHEN a non-premium user is redirected to the premium upgrade page, THE premium upgrade page SHALL display the name of the feature the user attempted to access.

### Requirement 5: Onboarding Guard

**User Story:** As a new user who hasn't completed onboarding, I want to be guided through the onboarding flow before accessing the main app, so that my experience is properly personalized.

#### Acceptance Criteria

1. WHEN an authenticated user who has not completed onboarding navigates to the `tabs` route, THEN the onboarding guard SHALL redirect to `/onboarding` and prevent access to the tabs layout.
2. THE onboarding guard SHALL read the onboarding completion flag from Capacitor Preferences (key: `onboarding_complete`) first, and if the value is `true`, allow navigation without waiting for the user profile response.
3. IF the Capacitor Preferences flag is not `true` and the user profile is available, THEN the onboarding guard SHALL use the user profile's onboarding completion status as the authoritative source.
4. WHEN onboarding completion is confirmed by either Capacitor Preferences or the user profile, THEN the onboarding guard SHALL allow navigation to the tabs layout.
5. IF the Capacitor Preferences read fails and the user profile is unavailable within 5 seconds, THEN the onboarding guard SHALL redirect to `/onboarding` as a safe default.
6. THE onboarding guard SHALL be registered as a `canActivate` guard on the `tabs` route in the application route configuration.

### Requirement 6: Auth Interceptor

**User Story:** As a developer, I want the Firebase JWT token automatically attached to all API requests, so that backend authentication is handled transparently.

#### Acceptance Criteria

1. WHEN an HTTP request URL starts with the configured `environment.apiUrl`, THEN the interceptor SHALL retrieve the current Firebase ID token using `getIdToken(false)` and attach it as a `Bearer` token in the `Authorization` header.
2. WHEN an HTTP request URL does not start with the configured `environment.apiUrl`, THEN the interceptor SHALL forward the request without modifying the `Authorization` header.
3. WHEN the Firebase ID token has less than 5 minutes remaining before expiry, THEN the interceptor SHALL call `getIdToken(true)` to force-refresh the token before attaching it.
4. IF the token retrieval fails or no user is currently authenticated in Firebase Auth, THEN the interceptor SHALL forward the request without the `Authorization` header and without delaying the request.
5. IF the token retrieval or refresh does not resolve within 10 seconds, THEN the interceptor SHALL forward the request without the `Authorization` header.
6. WHILE a token refresh is in progress, THE interceptor SHALL queue concurrent backend API requests and attach the same refreshed token to all queued requests once the refresh completes, rather than triggering multiple simultaneous refresh calls.

### Requirement 7: Error Interceptor

**User Story:** As a user, I want meaningful error messages when something goes wrong, so that I understand what happened and can take action.

#### Acceptance Criteria

1. WHEN a 401 response is received, THEN the interceptor SHALL clear the Firebase Auth session and reset the UserStore signal to null, navigate to the login route, and re-throw the error to the calling code.
2. WHEN a 403 response is received, THEN the interceptor SHALL display a toast notification of type "error" indicating insufficient permissions, with a duration of 3000 milliseconds, and re-throw the error to the calling code.
3. WHEN a response with status code between 500 and 599 (inclusive) is received, THEN the interceptor SHALL display a toast notification of type "error" with a generic message indicating a server problem and a suggestion to retry, with a duration of 4000 milliseconds, and re-throw the error to the calling code.
4. WHEN a network error occurs (HTTP status 0), THEN the interceptor SHALL NOT display a toast notification and SHALL re-throw the error to the calling code.
5. IF the application is running in a non-production environment (environment.production is false), THEN the interceptor SHALL log the HTTP status code and error message to the browser console for each intercepted error.
6. WHEN a client error response is received with a status code between 400 and 499 (inclusive) that is not 401 or 403, THEN the interceptor SHALL re-throw the error to the calling code without displaying a toast notification.
7. WHILE a toast notification from this interceptor is already visible, IF another interceptor error of the same category occurs, THEN the interceptor SHALL NOT display a duplicate toast until the current one is dismissed.

### Requirement 8: Loading Interceptor

**User Story:** As a user, I want to see a loading indicator during API calls, so that I know the app is working on my request.

#### Acceptance Criteria

1. WHEN an HTTP request starts and the request does not carry the skip-loading HttpContext token, THEN the interceptor SHALL increment a global loading counter by 1.
2. WHEN an HTTP request completes (success, error, or cancellation), THEN the interceptor SHALL decrement the loading counter by 1, and the counter SHALL never fall below zero.
3. WHILE the loading counter is greater than zero for longer than 300 milliseconds continuously, the system SHALL display a loading indicator.
4. WHEN the loading counter returns to zero, the system SHALL hide the loading indicator immediately (within one change-detection cycle).
5. IF a request completes and the loading counter transitions from greater-than-zero to zero within 300 milliseconds of the counter first becoming greater-than-zero, THEN the system SHALL NOT display the loading indicator for that interval.
6. THE interceptor SHALL expose the loading state as a boolean Angular signal (true when the loading indicator is visible, false otherwise) via the LoadingService, with an initial value of false.
7. WHEN a request is configured with the skip-loading HttpContext token, THEN the interceptor SHALL NOT increment or decrement the loading counter for that request.

### Requirement 9: Retry Interceptor

**User Story:** As a user on an unstable connection, I want failed requests to be automatically retried, so that transient network issues don't disrupt my experience.

#### Acceptance Criteria

1. WHEN an HTTP request fails due to a network error (status 0), a timeout error, or a server error (status 500-599), THEN the interceptor SHALL retry the request up to 3 times with exponential backoff delays of 1000ms, 2000ms, and 4000ms between attempts.
2. IF the failing response is a 429 (Too Many Requests) and includes a Retry-After header, THEN the interceptor SHALL use the Retry-After value as the delay before the next retry attempt, up to a maximum of 60 seconds.
3. IF the request fails with a 4xx client error other than 408 (Request Timeout) or 429 (Too Many Requests), THEN the interceptor SHALL NOT retry and SHALL immediately propagate the error to the caller.
4. IF the request method is non-idempotent (POST, PUT, PATCH) and the request does not carry the retryable HttpContext token, THEN the interceptor SHALL NOT retry and SHALL immediately propagate the error to the caller.
5. WHEN all retry attempts are exhausted without a successful response, THEN the interceptor SHALL propagate the error from the final failed attempt to the caller.
6. WHEN the caller cancels the request (unsubscribes from the observable) during a pending retry delay, THEN the interceptor SHALL abort the retry sequence and SHALL NOT issue further requests.

### Requirement 10: Firebase/Firestore Setup

**User Story:** As a developer, I want Firebase and Firestore properly initialized at app startup, so that authentication and real-time data features work correctly.

#### Acceptance Criteria

1. THE app SHALL initialize Firebase using `provideFirebaseApp` with the `environment.firebase` configuration object registered in the `app.config.ts` providers array.
2. THE app SHALL provide Firebase Auth via `provideAuth(() => getAuth())` in the `app.config.ts` providers array.
3. THE app SHALL provide Firestore via `provideFirestore(() => getFirestore())` in the `app.config.ts` providers array.
4. THE Firebase configuration SHALL be sourced from environment files (`environment.ts` / `environment.prod.ts`) and SHALL contain all required properties: `apiKey`, `authDomain`, `projectId`, `storageBucket`, `messagingSenderId`, and `appId`.
5. WHEN Firestore is initialized, THEN offline persistence SHALL be enabled using `persistentLocalCache` so that cached data is available without network connectivity.
6. THE Firebase providers SHALL be registered before router and HTTP providers in the `app.config.ts` providers array, ensuring Firebase services are available when route guards and interceptors execute.
7. IF Firestore persistence enablement fails (e.g., due to multiple browser tabs), THEN the app SHALL continue operating with in-memory caching and log a warning to the console without blocking app startup.
8. IF the `environment.firebase` configuration object is missing any required property, THEN the app SHALL fail to compile (enforced by TypeScript interface typing on the environment object).

### Requirement 11: Offline Indicator Banner

**User Story:** As a user who loses internet connectivity, I want to see a clear banner indicating I'm offline, so that I understand my actions will sync later.

#### Acceptance Criteria

1. WHEN the ConnectivityService `isOnline` signal transitions from `true` to `false`, THEN a banner SHALL appear at the top of the screen with the message: "You're in Offline Mode. Progress will sync later."
2. WHEN the ConnectivityService `isOnline` signal transitions from `false` to `true`, THEN the offline banner SHALL be replaced with a "Back online" confirmation banner that auto-dismisses after 3 seconds.
3. THE banner SHALL be non-intrusive and SHALL NOT block interaction with the app content below it (no overlay or pointer-events blocking).
4. THE offline banner SHALL use an amber/warning background color to differentiate from error states (red) and success states (green).
5. THE banner SHALL animate in/out using a slide-down/slide-up CSS transition with a duration of 300ms.
6. THE offline banner component SHALL be rendered in the `app.component.ts` template above the `ion-router-outlet` so it appears on all screens regardless of current route.
7. WHEN the app starts in an offline state, THE banner SHALL be visible immediately without waiting for a connectivity change event.

### Requirement 12: Skeleton Loaders

**User Story:** As a user, I want to see placeholder shapes while content loads, so that the app feels responsive and I know where content will appear.

#### Acceptance Criteria

1. WHEN a page or component is loading data, THEN skeleton placeholders SHALL be displayed occupying the same width and height as the expected content elements to prevent layout shift.
2. THE skeleton loader component SHALL support configurable shapes: rectangle, circle, and text-line variants, accepting `width`, `height`, and `borderRadius` inputs with default values of width: 100%, height: 16px, and borderRadius: 4px.
3. THE skeleton loader SHALL display a shimmer animation using CSS keyframe animation with a cycle duration between 1 and 2 seconds, repeating continuously until content loads.
4. WHEN data arrives, THEN the skeleton SHALL transition to the actual content using a fade transition of 200ms to 300ms duration with zero cumulative layout shift.
5. THE skeleton loader SHALL be a reusable Angular standalone component importable by any feature module.
6. THE skeleton loader SHALL derive its background and shimmer colors from CSS custom properties, rendering with a neutral muted background in light mode and a darker muted background in dark mode, updating automatically when the theme changes.
7. IF the `width` or `height` input receives a value of zero or negative, THEN the skeleton loader SHALL fall back to its default dimensions.

### Requirement 13: Error States with Retry

**User Story:** As a user who encounters a loading failure, I want to see a friendly error message with a retry button, so that I can attempt to reload without navigating away.

#### Acceptance Criteria

1. WHEN a page fails to load data due to a network error (status 0) or server error (5xx), THEN the error state component SHALL be displayed with the configured error message and a "Try Again" button.
2. WHEN the user taps "Try Again", THEN the error state component SHALL emit a retry event via @Output(), the "Try Again" button SHALL enter a disabled state, and the parent component SHALL re-execute the failed data request.
3. THE error state component SHALL accept an optional error message string input (maximum 150 characters displayed) and an optional icon input (ionicon name string), falling back to defaults when not provided.
4. THE error state component SHALL be a reusable standalone component importable by any feature module.
5. WHEN a load failure occurs while a skeleton loader is displayed, THEN the skeleton loader SHALL be removed from the DOM and the error state component SHALL be rendered in its place (no overlay or stacking).
6. THE default error message SHALL be: "Something went wrong. Tap to try again." and the default icon SHALL be a generic alert/warning ionicon.
7. WHEN a retry is in progress, THEN the error state component SHALL display the skeleton loader in place of the error state until the request succeeds or fails again.
8. IF the retried request also fails, THEN the error state component SHALL be re-displayed with the same error message and "Try Again" button, allowing unlimited manual retry attempts.

### Requirement 14: Haptic Feedback Service

**User Story:** As a user, I want to feel tactile feedback when I complete actions like finishing a quest, so that interactions feel satisfying and responsive.

#### Acceptance Criteria

1. THE haptic service SHALL provide an `impact(style)` method accepting styles `light`, `medium`, and `heavy`, a `notification(type)` method accepting types `success`, `warning`, and `error`, and a `vibrate(duration)` method accepting a duration in milliseconds between 1 and 500.
2. THE haptic service SHALL use Capacitor's Haptics plugin for native device vibration.
3. IF the device does not support haptics (e.g., desktop browser), THEN the haptic service SHALL resolve all method calls as no-ops without throwing errors or rejecting promises.
4. IF haptic feedback is disabled in user preferences, THEN the haptic service SHALL skip vibration and resolve all method calls as no-ops without throwing errors.
5. THE haptic preference SHALL default to enabled for new users and be persisted to local storage via the Storage Service using the key `haptic_enabled`.
6. THE haptic service SHALL be injectable as a singleton (`providedIn: 'root'`).
7. WHEN a haptic method is called and both the device supports haptics and the user preference is enabled, THEN the haptic service SHALL trigger the corresponding Capacitor Haptics plugin call and resolve within 100ms.

### Requirement 15: Connectivity Service

**User Story:** As a developer, I want a reactive service that tracks online/offline status, so that components and services can adapt behavior based on network availability.

#### Acceptance Criteria

1. THE connectivity service SHALL expose a reactive signal (`isOnline: Signal<boolean>`) indicating current network status.
2. WHEN the service initializes, THE service SHALL query the current network status and set the `isOnline` signal to the actual connectivity state before any consumer reads it.
3. WHILE running on a native platform (iOS/Android), THE service SHALL use Capacitor's Network plugin to detect connectivity changes.
4. WHILE running on the web platform, THE service SHALL use browser `navigator.onLine` for initial status and `online`/`offline` window events for change detection.
5. WHEN connectivity changes, THE signal SHALL update within 1 second of the actual network state change.
6. THE service SHALL be initialized at app startup and begin monitoring within 500ms of app initialization completing.
7. THE service SHALL be injectable as a singleton (`providedIn: 'root'`).
8. IF the Capacitor Network plugin fails to initialize or is unavailable, THEN the service SHALL fall back to browser-based detection (`navigator.onLine` and window events) without throwing an error.

### Requirement 16: Storage Service

**User Story:** As a developer, I want a unified storage abstraction, so that I can persist and retrieve key-value data without coupling to a specific storage implementation.

#### Acceptance Criteria

1. THE storage service SHALL provide async methods: `get<T>(key): Promise<T | null>`, `set(key, value): Promise<void>`, `remove(key): Promise<void>`, and `clear(): Promise<void>`.
2. THE storage service SHALL use Capacitor Preferences as the underlying storage mechanism.
3. WHEN `set(key, value)` is called, THEN the storage service SHALL serialize the value using `JSON.stringify` before persisting, and WHEN `get(key)` is called, THEN the service SHALL deserialize the stored string using `JSON.parse` before returning.
4. THE storage service SHALL automatically prefix all keys with `ascend_` before passing them to the underlying storage mechanism, so that the app's data is namespaced separately from other storage consumers.
5. IF `get` is called with a key that does not exist in storage, THEN the service SHALL return `null`.
6. IF a Capacitor Preferences operation fails (read, write, or delete), THEN the storage service SHALL log a warning to the console and return `null` for `get` or resolve silently (void) for `set`, `remove`, and `clear` without throwing an exception to the caller.
7. IF `set` is called with a value that cannot be serialized via `JSON.stringify` (e.g., circular references, BigInt), THEN the storage service SHALL log a warning to the console and resolve without persisting the value.
8. THE storage service SHALL be injectable as a singleton (`providedIn: 'root'`).
9. THE storage service SHALL accept keys of type `string` with a maximum length of 256 characters (before prefix). IF a key exceeds 256 characters or is an empty string, THEN the service SHALL log a warning and return `null` for `get` or resolve silently for `set`, `remove`.
