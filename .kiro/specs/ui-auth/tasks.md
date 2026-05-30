# Implementation Plan: UI Auth

## Overview

This plan implements the complete authentication flow for the Ascend app: splash screen with auth state resolution, welcome screen with social/guest sign-in options, login page, signup page with password strength indicators, forgot password page with cooldown timer, AuthService with signal-based state management, UserService for backend profile operations, noAuthGuard, form validators, and auth routing. The implementation builds incrementally — shared validators and models first, then AuthService and UserService, then pages (splash → welcome → login → signup → forgot-password), then routing and guards, and finally wiring everything together.

## Tasks

- [x] 1. Create auth data models, error mapping, and shared validators
  - [x] 1.1 Create AuthError interface, error code mapping, and platform detection utility
    - Create `src/core/models/auth-error.model.ts` with `AuthError` interface (`code: string`, `message: string`)
    - Create `src/features/auth/constants/auth-error-messages.ts` with `AUTH_ERROR_MESSAGES` record mapping Firebase error codes to user-facing strings
    - Create `src/features/auth/utils/platform-detection.ts` with `getAuthPlatform()` function returning `'ios' | 'android' | 'web'`
    - Create `src/features/auth/models/password-strength.model.ts` with `PasswordStrength` interface
    - Create `src/features/auth/models/forgot-password-state.model.ts` with `ForgotPasswordState` interface
    - _Requirements: 10.7, 6.1, 7.1_

  - [x] 1.2 Create email validator, password strength validator, and password match validator
    - Create `src/shared/validators/email.validator.ts` — synchronous Angular validator that checks standard email format (one `@`, non-empty local part, domain with at least one dot)
    - Create `src/shared/validators/password-strength.validator.ts` — synchronous validator checking min 8 chars, 1 uppercase, 1 special character; also export a pure function `getPasswordStrength(value: string): PasswordStrength`
    - Create `src/shared/validators/password-match.validator.ts` — cross-field validator ensuring `password === confirmPassword`
    - All validators use Angular Reactive Forms `ValidatorFn` / `AbstractControl` patterns
    - _Requirements: 3.1, 4.1, 4.4, 5.1, 12.1, 12.2, 12.3, 12.7_

  - [x]* 1.3 Write property test for email validator (Property 1)
    - **Property 1: Email validation correctness**
    - For any string input, the email validator returns valid iff the string contains exactly one `@`, has a non-empty local part, and has a domain with at least one dot
    - Create `src/shared/validators/__tests__/email.validator.property.spec.ts`
    - **Validates: Requirements 3.1, 4.1, 5.1, 12.1**

  - [x]* 1.4 Write property test for password strength validator (Property 2)
    - **Property 2: Password strength indicator correctness**
    - For any string input, `minLength = (length >= 8)`, `hasUppercase = (/[A-Z]/.test(string))`, `hasSpecial = (/[!@#$%^&*(),.?":{}|<>]/.test(string))`
    - Create `src/shared/validators/__tests__/password-strength.validator.property.spec.ts`
    - **Validates: Requirements 4.4, 12.2**

  - [x]* 1.5 Write property test for password match validator (Property 3)
    - **Property 3: Confirm password mismatch detection**
    - For any two strings, the error is displayed iff the two strings are not identical
    - Create `src/shared/validators/__tests__/password-match.validator.property.spec.ts`
    - **Validates: Requirements 12.3**

- [x] 2. Implement AuthService with signal-based state management
  - [x] 2.1 Create AuthService with signals and Firebase Auth subscription
    - Create `src/core/services/auth.service.ts`
    - Inject `Auth` from `@angular/fire/auth`, subscribe to `onAuthStateChanged` at construction
    - Expose signals: `currentUser: Signal<FirebaseUser | null>`, `isAuthenticated: Signal<boolean>` (computed), `isGuest: Signal<boolean>` (computed), `authReady: Signal<boolean>`
    - `authReady` transitions to `true` on first `onAuthStateChanged` emission or after 5-second timeout
    - Configure Firebase Auth persistence (`browserLocalPersistence` on web, native secure storage on mobile)
    - Provide as singleton (`providedIn: 'root'`)
    - _Requirements: 9.1, 9.5, 9.7, 10.1, 10.2, 10.3, 10.4, 10.5_

  - [x] 2.2 Implement AuthService authentication methods
    - Implement `loginWithEmail(email, password)` — calls `signInWithEmailAndPassword`, throws typed `AuthError` on failure
    - Implement `signupWithEmail(email, password)` — calls `createUserWithEmailAndPassword`, delegates to `UserService.register()`, throws typed `AuthError` on failure
    - Implement `loginWithGoogle()` — uses Capacitor Google Auth plugin on native, `signInWithPopup` with GoogleAuthProvider on web; delegates to `UserService.resolveUserAfterAuth()`
    - Implement `loginWithApple()` — uses Capacitor Sign In with Apple plugin on iOS, `signInWithPopup` with OAuthProvider('apple.com') on web; delegates to `UserService.resolveUserAfterAuth()`
    - Implement `loginAsGuest()` — calls `signInAnonymously`, delegates to `UserService.register({ isGuest: true })`
    - Implement `sendPasswordReset(email)` — calls `sendPasswordResetEmail`; suppresses `auth/user-not-found` error (returns success)
    - Implement `linkAccount(credential)` — calls `linkWithCredential` on current user
    - Implement `logout()` — calls `signOut`, clears UserStore, navigates to `/auth/welcome`
    - Implement `getIdToken(forceRefresh?)` — returns current user's ID token
    - All methods throw typed `AuthError` objects using the error code mapping
    - _Requirements: 3.6, 4.7, 5.4, 5.6, 6.1, 6.2, 7.1, 7.2, 8.1, 8.6, 9.4, 9.6, 10.6, 10.7, 10.8_

  - [x]* 2.3 Write property test for isGuest signal (Property 9)
    - **Property 9: isGuest signal reflects anonymous state**
    - For any Firebase Auth user object (or null), `isGuest` returns `true` iff user is not null AND `user.isAnonymous === true`
    - Create `src/core/services/__tests__/auth.service.property.spec.ts`
    - **Validates: Requirements 8.5, 10.5**

  - [x]* 2.4 Write property test for isAuthenticated signal (Property 10)
    - **Property 10: isAuthenticated signal reflects currentUser presence**
    - For any value of `currentUser` (User or null), `isAuthenticated` returns `true` iff `currentUser` is not null
    - Add to `src/core/services/__tests__/auth.service.property.spec.ts`
    - **Validates: Requirements 10.4**

  - [x]* 2.5 Write property test for auth error code mapping (Property 5)
    - **Property 5: Auth error code to message mapping**
    - For any Firebase Auth error code in the mapping, the displayed message is exactly the mapped string; for empty-string mappings (cancellation), no error is displayed
    - Add to `src/core/services/__tests__/auth.service.property.spec.ts`
    - **Validates: Requirements 3.8, 3.9, 3.10, 4.9, 4.10, 4.11, 5.7, 10.7**

- [x] 3. Implement UserService for backend profile operations
  - [x] 3.1 Create UserService with profile fetch and registration
    - Create `src/core/services/user.service.ts`
    - Inject `HttpClient` and `UserStore`
    - Implement `getMe(): Promise<User | null>` — calls `GET /api/v1/users/me`, returns null on 404
    - Implement `register(data: { isGuest: boolean }): Promise<User>` — calls `POST /api/v1/users/register`
    - Implement `resolveUserAfterAuth(): Promise<'existing' | 'new'>` — calls `getMe()`, if 404 calls `register()`, updates UserStore
    - _Requirements: 4.8, 6.3, 6.4, 6.5, 7.3, 7.4, 7.5, 8.2, 10.8_

- [x] 4. Checkpoint - Ensure AuthService and UserService compile and pass tests
  - Ensure all tests pass, ask the user if questions arise.

- [x] 5. Implement Splash Page
  - [x] 5.1 Create SplashPage component with branded loading UI
    - Create `src/features/auth/pages/splash/splash.page.ts` and `.html` and `.scss`
    - Display Ascend logo centered on deep black background (`#0A0A0A`)
    - Add subtle animated glowing particles in background (CSS keyframe animation)
    - Display "Enter Arc Mode" subtext below logo using display font
    - Display animated loading indicator (pulsing dots or spinner) below subtext
    - Standalone component with Ionic page structure
    - _Requirements: 1.1, 1.2, 1.3_

  - [x] 5.2 Implement SplashPage auth resolution and navigation logic
    - Wait for `authReady` signal to become `true` (AuthService handles 5s timeout internally)
    - Enforce minimum 1.5s display time using `Promise.all` with a timer
    - If `currentUser` exists and profile has `onboardingComplete`, navigate to `/tabs/home`
    - If `currentUser` exists but onboarding incomplete, navigate to `/onboarding`
    - If no user (null) or timeout, navigate to `/auth/welcome`
    - Use `NavController.navigateRoot()` to replace navigation stack
    - _Requirements: 1.4, 1.5, 1.6, 1.7, 1.8, 1.9_

  - [x]* 5.3 Write unit tests for SplashPage
    - Test DOM structure (logo, subtext, loading indicator)
    - Test navigation to `/tabs/home` when user authenticated + onboarding complete
    - Test navigation to `/onboarding` when user authenticated + onboarding incomplete
    - Test navigation to `/auth/welcome` when no user
    - Test minimum 1.5s display time enforcement
    - Test 5s timeout fallback to welcome screen
    - Create `src/features/auth/__tests__/splash.page.spec.ts`
    - _Requirements: 1.1–1.9_

- [x] 6. Implement Welcome Page
  - [x] 6.1 Create WelcomePage component with sign-in options UI
    - Create `src/features/auth/pages/welcome/welcome.page.ts` and `.html` and `.scss`
    - Display tagline "Become the strongest version of yourself" as large cinematic text
    - Display hero illustration/stylized avatar graphic below tagline
    - Display "Continue with Google" primary button with Google icon
    - Display "Continue with Apple" secondary button with Apple icon (conditionally rendered: iOS and web only)
    - Display "Sign in with Email" tertiary button navigating to `/auth/login`
    - Display "Continue as Guest" text link
    - Use dark theme with deep black background and neon accent colors
    - Standalone component
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7_

  - [x] 6.2 Implement WelcomePage sign-in action handlers
    - On "Continue with Google" tap: call `AuthService.loginWithGoogle()`, handle success navigation and error toast
    - On "Continue with Apple" tap: call `AuthService.loginWithApple()`, handle success navigation and error toast
    - On "Continue as Guest" tap: call `AuthService.loginAsGuest()`, handle success navigation and error toast
    - On success: navigate to `/tabs/home` (existing user) or `/onboarding` (new user) using `NavController.navigateRoot()`
    - On user cancellation (empty error message): remain on welcome screen silently
    - On failure: display toast "Sign-in failed. Please try again." or "Could not start guest session. Please try again."
    - Inject `HapticService` for success feedback
    - _Requirements: 2.8, 2.9, 2.10, 6.5, 6.6, 6.7, 7.5, 7.6, 7.7, 8.1, 8.2, 8.3_

  - [x]* 6.3 Write property test for Apple button platform visibility (Property 8)
    - **Property 8: Apple sign-in button platform visibility**
    - For any platform value, Apple button is rendered iff platform is `'ios'` or `'web'`; NOT rendered on `'android'`
    - Create `src/features/auth/__tests__/welcome.page.property.spec.ts`
    - **Validates: Requirements 2.4, 7.8**

  - [x]* 6.4 Write property test for platform-specific sign-in method selection (Property 7)
    - **Property 7: Platform-specific sign-in method selection**
    - For any platform value, Google uses Capacitor plugin on native and `signInWithPopup` on web; Apple uses Capacitor plugin on iOS and `signInWithPopup` on web
    - Add to `src/features/auth/__tests__/welcome.page.property.spec.ts`
    - **Validates: Requirements 6.1, 7.1**

  - [x]* 6.5 Write unit tests for WelcomePage
    - Test button presence and conditional Apple button rendering
    - Test navigation to login page on "Sign in with Email" tap
    - Test Google sign-in flow (success, cancel, error)
    - Test Apple sign-in flow (success, cancel, error)
    - Test guest sign-in flow (success, error)
    - Test toast display on errors
    - Create `src/features/auth/__tests__/welcome.page.spec.ts`
    - _Requirements: 2.1–2.10, 6.5–6.7, 7.5–7.7, 8.1–8.3_

- [x] 7. Implement Login Page
  - [x] 7.1 Create LoginPage component with form and UI
    - Create `src/features/auth/pages/login/login.page.ts` and `.html` and `.scss`
    - Create reactive form with `email` (required, email validator) and `password` (required) controls
    - Display email input with placeholder "Email address" and validation error on blur
    - Display password input with placeholder "Password" and visibility toggle icon
    - Display "Log In" primary button, disabled until form is valid
    - Display "Forgot Password?" link navigating to `/auth/forgot-password`
    - Display "Don't have an account? Sign Up" link navigating to `/auth/signup`
    - Include `ion-back-button` for back navigation
    - Standalone component with Ionic page structure
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 12.1, 12.5, 12.7_

  - [x] 7.2 Implement LoginPage submission logic and error handling
    - On form submit: call `AuthService.loginWithEmail(email, password)`
    - Show loading spinner on button and disable during request
    - On success: trigger haptic feedback, navigate to `/tabs/home` or stored redirect URL via `NavController.navigateRoot()`
    - On `auth/user-not-found` or `auth/wrong-password` or `auth/invalid-credential`: display inline error "Invalid email or password. Please try again."
    - On `auth/too-many-requests`: display inline error "Too many failed attempts. Please try again later."
    - On network error: display inline error "Network error. Check your connection and try again."
    - On submit with invalid form: focus first invalid field and scroll into view
    - Error messages disappear when field becomes valid
    - _Requirements: 3.6, 3.7, 3.8, 3.9, 3.10, 3.11, 3.12, 12.4, 12.6_

  - [x]* 7.3 Write property test for form submit button disabled state (Property 4 — Login)
    - **Property 4: Form submit button disabled state reflects validation**
    - For any combination of email and password values, the "Log In" button is disabled iff the form is invalid
    - Create `src/features/auth/__tests__/login.page.property.spec.ts`
    - **Validates: Requirements 3.3**

  - [x]* 7.4 Write unit tests for LoginPage
    - Test form validation (email format, required fields)
    - Test password visibility toggle
    - Test button disabled state
    - Test successful login navigation
    - Test error message display for each error code
    - Test loading spinner during request
    - Test haptic feedback on success
    - Test navigation links
    - Create `src/features/auth/__tests__/login.page.spec.ts`
    - _Requirements: 3.1–3.12_

- [x] 8. Implement Signup Page
  - [x] 8.1 Create SignupPage component with form, strength indicators, and UI
    - Create `src/features/auth/pages/signup/signup.page.ts` and `.html` and `.scss`
    - Create reactive form with `email` (required, email validator), `password` (required, minLength 8, passwordStrengthValidator), `confirmPassword` (required) controls and `passwordMatchValidator` group validator
    - Display email input with placeholder "Email address" and validation error on blur
    - Display password input with placeholder "Create password" and visibility toggle
    - Display confirm password input with placeholder "Confirm password"
    - Display real-time password strength indicators (checkmark/cross for each requirement)
    - Display "Passwords do not match" error when confirm password differs (on input)
    - Display "Create Account" primary button, disabled until form is valid and passwords match
    - Display "Already have an account? Log In" link navigating to `/auth/login`
    - Include `ion-back-button` for back navigation
    - Standalone component
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 12.1, 12.2, 12.3, 12.5, 12.7_

  - [x] 8.2 Implement SignupPage submission logic and error handling
    - On form submit: call `AuthService.signupWithEmail(email, password)`
    - Show loading spinner on button and disable during request
    - On success: trigger haptic feedback, navigate to `/onboarding` via `NavController.navigateRoot()`
    - On `auth/email-already-in-use`: display inline error "An account with this email already exists. Try logging in instead."
    - On `auth/weak-password`: display inline error "Password is too weak. Please meet all requirements."
    - On network error: display inline error "Network error. Check your connection and try again."
    - On submit with invalid form: focus first invalid field and scroll into view
    - Error messages disappear when field becomes valid
    - _Requirements: 4.7, 4.8, 4.9, 4.10, 4.11, 4.12, 4.13, 12.4, 12.6_

  - [x]* 8.3 Write property test for form submit button disabled state (Property 4 — Signup)
    - **Property 4: Form submit button disabled state reflects validation**
    - For any combination of email, password, and confirmPassword values, the "Create Account" button is disabled iff the form is invalid
    - Create `src/features/auth/__tests__/signup.page.property.spec.ts`
    - **Validates: Requirements 4.5**

  - [x]* 8.4 Write unit tests for SignupPage
    - Test form validation (email, password strength, password match)
    - Test real-time strength indicator updates
    - Test password visibility toggle
    - Test button disabled state
    - Test successful signup navigation to onboarding
    - Test error message display for each error code
    - Test loading spinner during request
    - Test haptic feedback on success
    - Create `src/features/auth/__tests__/signup.page.spec.ts`
    - _Requirements: 4.1–4.13_

- [x] 9. Implement Forgot Password Page
  - [x] 9.1 Create ForgotPasswordPage component with form, cooldown timer, and UI
    - Create `src/features/auth/pages/forgot-password/forgot-password.page.ts` and `.html` and `.scss`
    - Create reactive form with `email` (required, email validator) control
    - Display email input with placeholder "Email address" and validation error on blur
    - Display "Send Reset Link" primary button, disabled until email is valid
    - Display "Back to Login" link navigating to `/auth/login`
    - Include `ion-back-button` for back navigation
    - Standalone component
    - _Requirements: 5.1, 5.2, 5.3, 12.1, 12.5, 12.7_

  - [x] 9.2 Implement ForgotPasswordPage submission logic with cooldown
    - On form submit: call `AuthService.sendPasswordReset(email)`
    - Show loading spinner on button and disable during request
    - On success (including suppressed `auth/user-not-found`): display success message "Password reset email sent. Check your inbox."
    - Disable submit button for 60 seconds after successful submission; display countdown timer
    - After 60 seconds, re-enable the button for another submission
    - On network error: display inline error "Network error. Check your connection and try again."
    - _Requirements: 5.4, 5.5, 5.6, 5.7, 5.8, 5.9_

  - [x]* 9.3 Write property test for forgot password cooldown timer (Property 6)
    - **Property 6: Forgot password cooldown timer**
    - For any time T (seconds) after successful submission, button is disabled if T < 60, enabled if T >= 60
    - Create `src/features/auth/__tests__/forgot-password.page.property.spec.ts`
    - **Validates: Requirements 5.9**

  - [x]* 9.4 Write unit tests for ForgotPasswordPage
    - Test form validation (email format)
    - Test button disabled state
    - Test success message display
    - Test cooldown timer countdown
    - Test re-enable after 60 seconds
    - Test email enumeration prevention (same message for user-not-found)
    - Test network error display
    - Create `src/features/auth/__tests__/forgot-password.page.spec.ts`
    - _Requirements: 5.1–5.9_

- [x] 10. Checkpoint - Ensure all pages compile and pass tests
  - Ensure all tests pass, ask the user if questions arise.

- [x] 11. Implement noAuthGuard and auth routing
  - [x] 11.1 Create noAuthGuard to prevent authenticated users from accessing auth routes
    - Create `src/core/auth/no-auth.guard.ts`
    - Inject `AuthService` and `Router`
    - If `isAuthenticated()` is true, redirect to `/tabs/home`
    - Otherwise, allow access
    - Export as `CanActivateFn`
    - _Requirements: 11.4_

  - [x] 11.2 Create auth routes configuration with lazy-loaded pages
    - Create `src/features/auth/auth.routes.ts`
    - Define routes: `''` → redirect to `welcome`, `'welcome'` → WelcomePage, `'login'` → LoginPage, `'signup'` → SignupPage, `'forgot-password'` → ForgotPasswordPage
    - All page components loaded via `loadComponent` with dynamic `import()`
    - Apply `noAuthGuard` as `canActivate` on the parent `/auth` route
    - _Requirements: 11.1, 11.2, 11.4, 11.5_

  - [x] 11.3 Update app.routes.ts to include auth routes and splash route
    - Add splash route at root path (`''`) loading `SplashPage`
    - Add `/auth` route with `loadChildren` pointing to `auth.routes.ts`
    - Ensure auth routes use Ionic page transition animations
    - Ensure `navigateRoot` is used after successful auth to replace stack
    - _Requirements: 11.2, 11.5, 11.6_

  - [x]* 11.4 Write property test for noAuthGuard (Property 11)
    - **Property 11: noAuthGuard redirects authenticated users from auth routes**
    - For any `/auth/*` route, when user is authenticated, guard redirects to `/tabs/home`
    - Create `src/core/auth/__tests__/no-auth.guard.property.spec.ts`
    - **Validates: Requirements 11.4**

  - [x]* 11.5 Write unit tests for auth routing and noAuthGuard
    - Test route definitions and lazy loading
    - Test noAuthGuard allows unauthenticated access
    - Test noAuthGuard redirects authenticated users
    - Test back button navigation between auth pages
    - Create `src/core/auth/__tests__/no-auth.guard.spec.ts`
    - _Requirements: 11.1–11.6_

- [x] 12. Final checkpoint - Ensure all tests pass and app compiles
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
- All components are standalone Angular 17+ components using signals and inject()
- AuthService delegates backend communication to UserService (separation of concerns)
- Platform detection determines which sign-in plugin to use (Capacitor native vs Firebase popup)
- The splash page is the app entry point; auth routes are under `/auth` prefix
- Use `NavController.navigateRoot()` after successful auth to prevent back-navigation to auth screens

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1"] },
    { "id": 1, "tasks": ["1.2"] },
    { "id": 2, "tasks": ["1.3", "1.4", "1.5", "2.1"] },
    { "id": 3, "tasks": ["2.2", "3.1"] },
    { "id": 4, "tasks": ["2.3", "2.4", "2.5", "5.1"] },
    { "id": 5, "tasks": ["5.2", "6.1"] },
    { "id": 6, "tasks": ["5.3", "6.2", "7.1"] },
    { "id": 7, "tasks": ["6.3", "6.4", "6.5", "7.2", "8.1"] },
    { "id": 8, "tasks": ["7.3", "7.4", "8.2", "9.1"] },
    { "id": 9, "tasks": ["8.3", "8.4", "9.2"] },
    { "id": 10, "tasks": ["9.3", "9.4", "11.1"] },
    { "id": 11, "tasks": ["11.2"] },
    { "id": 12, "tasks": ["11.3"] },
    { "id": 13, "tasks": ["11.4", "11.5"] }
  ]
}
```
