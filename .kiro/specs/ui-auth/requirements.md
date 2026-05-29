# Requirements Document

## Introduction

This document defines the requirements for the Ascend app's UI Authentication Flow — the set of screens and services that handle user onboarding into the authentication system. This includes the splash screen, welcome screen, login page, signup page, forgot password page, social sign-in (Google, Apple, Email/Password, Guest), and session persistence. The feature integrates with Firebase Authentication on the client side and communicates with the Spring Boot backend for user profile creation and validation.

## Glossary

| Term | Definition |
|------|-----------|
| Auth_Module | The Angular feature module containing all authentication-related pages, components, and services |
| Splash_Screen | The initial branded loading screen displayed during app initialization and auth state resolution |
| Welcome_Screen | The screen displayed to unauthenticated users presenting sign-in options and the app tagline |
| Login_Page | The screen where existing users authenticate via email/password credentials |
| Signup_Page | The screen where new users create an account via email/password registration |
| Forgot_Password_Page | The screen where users request a password reset email |
| Social_Sign_In | Authentication via third-party identity providers (Google, Apple) using Firebase Auth |
| Guest_Mode | Anonymous Firebase Authentication allowing users to explore the app without creating an account |
| Session_Persistence | The mechanism by which a user's authenticated state survives app restarts using Firebase Auth persistence |
| Auth_Service | The Angular service that wraps Firebase Auth operations and exposes authentication state as signals |
| Firebase_Auth | The Firebase Authentication SDK used for identity management on the client |
| ID_Token | The JWT issued by Firebase Auth after successful authentication, used to authorize backend API requests |

## Requirements

### Requirement 1: Splash Screen

**User Story:** As a user, I want to see a branded loading screen when I open the app, so that I know the app is loading and I get an immediate premium feel.

#### Acceptance Criteria

1. WHEN the app launches, THE Splash_Screen SHALL display the Ascend logo centered on a deep black background (`#0A0A0A`) with subtle animated glowing particles in the background.
2. THE Splash_Screen SHALL display the subtext "Enter Arc Mode" below the logo using the app's display font.
3. THE Splash_Screen SHALL display an animated loading indicator below the subtext to communicate that initialization is in progress.
4. WHILE the Splash_Screen is displayed, THE Auth_Service SHALL resolve the current Firebase Auth state by subscribing to `onAuthStateChanged` and waiting for the first emission.
5. WHEN the auth state resolves to an authenticated user who has completed onboarding, THE Splash_Screen SHALL navigate to `/tabs/home` within 500ms of state resolution.
6. WHEN the auth state resolves to an authenticated user who has not completed onboarding, THE Splash_Screen SHALL navigate to `/onboarding`.
7. WHEN the auth state resolves to no authenticated user (null), THE Splash_Screen SHALL navigate to the Welcome_Screen.
8. IF the auth state does not resolve within 5 seconds, THEN THE Splash_Screen SHALL navigate to the Welcome_Screen as a fallback.
9. THE Splash_Screen SHALL be displayed for a minimum of 1.5 seconds regardless of how quickly auth state resolves, to prevent a jarring flash.

### Requirement 2: Welcome Screen

**User Story:** As a new or returning unauthenticated user, I want to see an inspiring welcome screen with clear sign-in options, so that I feel motivated to start and can easily choose how to enter the app.

#### Acceptance Criteria

1. THE Welcome_Screen SHALL display the tagline "Become the strongest version of yourself" as large cinematic text using the app's display font.
2. THE Welcome_Screen SHALL display a hero illustration or stylized avatar graphic below the tagline.
3. THE Welcome_Screen SHALL display a primary action button labeled "Continue with Google" with the Google icon, using the app's primary button styling.
4. THE Welcome_Screen SHALL display a secondary action button labeled "Continue with Apple" with the Apple icon, rendered only on iOS devices or when running in a browser.
5. THE Welcome_Screen SHALL display a tertiary action button labeled "Sign in with Email" that navigates to the Login_Page.
6. THE Welcome_Screen SHALL display a text link labeled "Continue as Guest" that initiates Guest_Mode authentication.
7. THE Welcome_Screen SHALL use the app's dark theme with deep black background and neon accent colors consistent with the design system.
8. WHEN the user taps "Continue with Google", THE Auth_Service SHALL initiate the Google Social_Sign_In flow.
9. WHEN the user taps "Continue with Apple", THE Auth_Service SHALL initiate the Apple Social_Sign_In flow.
10. WHEN the user taps "Continue as Guest", THE Auth_Service SHALL initiate anonymous Firebase Authentication.

### Requirement 3: Login Page

**User Story:** As a returning user with an email account, I want to log in with my email and password, so that I can access my existing progress and data.

#### Acceptance Criteria

1. THE Login_Page SHALL display an email input field with type `email`, placeholder text "Email address", and client-side validation for valid email format.
2. THE Login_Page SHALL display a password input field with type `password`, placeholder text "Password", and a visibility toggle icon to show/hide the password.
3. THE Login_Page SHALL display a "Log In" primary action button that is disabled until both fields pass client-side validation.
4. THE Login_Page SHALL display a "Forgot Password?" text link that navigates to the Forgot_Password_Page.
5. THE Login_Page SHALL display a "Don't have an account? Sign Up" text link that navigates to the Signup_Page.
6. WHEN the user submits valid credentials, THE Auth_Service SHALL call Firebase Auth `signInWithEmailAndPassword` with the provided email and password.
7. WHEN Firebase Auth returns a successful authentication result, THE Login_Page SHALL navigate to `/tabs/home` or the stored redirect URL if one exists.
8. IF Firebase Auth returns an error with code `auth/user-not-found` or `auth/wrong-password`, THEN THE Login_Page SHALL display an inline error message: "Invalid email or password. Please try again."
9. IF Firebase Auth returns an error with code `auth/too-many-requests`, THEN THE Login_Page SHALL display an inline error message: "Too many failed attempts. Please try again later."
10. IF a network error occurs during login, THEN THE Login_Page SHALL display an inline error message: "Network error. Check your connection and try again."
11. WHILE the login request is in progress, THE "Log In" button SHALL display a loading spinner and be disabled to prevent duplicate submissions.
12. THE Login_Page SHALL trigger light haptic feedback on successful login.

### Requirement 4: Signup Page

**User Story:** As a new user, I want to create an account with my email and password, so that I can save my progress and access the full app experience.

#### Acceptance Criteria

1. THE Signup_Page SHALL display an email input field with type `email`, placeholder text "Email address", and client-side validation for valid email format.
2. THE Signup_Page SHALL display a password input field with type `password`, placeholder text "Create password", and a visibility toggle icon.
3. THE Signup_Page SHALL display a confirm password input field with type `password`, placeholder text "Confirm password".
4. THE Signup_Page SHALL display real-time password strength indicators showing requirements: minimum 8 characters, at least one uppercase letter, and at least one special character.
5. THE Signup_Page SHALL display a "Create Account" primary action button that is disabled until all fields pass client-side validation and passwords match.
6. THE Signup_Page SHALL display an "Already have an account? Log In" text link that navigates to the Login_Page.
7. WHEN the user submits valid registration data, THE Auth_Service SHALL call Firebase Auth `createUserWithEmailAndPassword` with the provided email and password.
8. WHEN Firebase Auth returns a successful account creation result, THE Auth_Service SHALL call the Spring Boot backend `POST /api/v1/users/register` to create the user profile, then navigate to `/onboarding`.
9. IF Firebase Auth returns an error with code `auth/email-already-in-use`, THEN THE Signup_Page SHALL display an inline error message: "An account with this email already exists. Try logging in instead."
10. IF Firebase Auth returns an error with code `auth/weak-password`, THEN THE Signup_Page SHALL display an inline error message: "Password is too weak. Please meet all requirements."
11. IF a network error occurs during signup, THEN THE Signup_Page SHALL display an inline error message: "Network error. Check your connection and try again."
12. WHILE the signup request is in progress, THE "Create Account" button SHALL display a loading spinner and be disabled to prevent duplicate submissions.
13. THE Signup_Page SHALL trigger light haptic feedback on successful account creation.

### Requirement 5: Forgot Password Page

**User Story:** As a user who has forgotten my password, I want to request a password reset email, so that I can regain access to my account.

#### Acceptance Criteria

1. THE Forgot_Password_Page SHALL display an email input field with type `email`, placeholder text "Email address", and client-side validation for valid email format.
2. THE Forgot_Password_Page SHALL display a "Send Reset Link" primary action button that is disabled until the email field passes validation.
3. THE Forgot_Password_Page SHALL display a "Back to Login" text link that navigates back to the Login_Page.
4. WHEN the user submits a valid email, THE Auth_Service SHALL call Firebase Auth `sendPasswordResetEmail` with the provided email address.
5. WHEN Firebase Auth confirms the reset email was sent, THE Forgot_Password_Page SHALL display a success message: "Password reset email sent. Check your inbox." and disable the submit button for 60 seconds to prevent spam.
6. IF Firebase Auth returns an error with code `auth/user-not-found`, THEN THE Forgot_Password_Page SHALL still display the generic success message to prevent email enumeration attacks.
7. IF a network error occurs, THEN THE Forgot_Password_Page SHALL display an inline error message: "Network error. Check your connection and try again."
8. WHILE the reset request is in progress, THE "Send Reset Link" button SHALL display a loading spinner and be disabled.
9. AFTER a successful submission, THE Forgot_Password_Page SHALL display a 60-second countdown timer before allowing another submission.

### Requirement 6: Google Sign-In

**User Story:** As a user, I want to sign in with my Google account in one tap, so that I can get started without creating a new password.

#### Acceptance Criteria

1. WHEN the user initiates Google Sign-In, THE Auth_Service SHALL use the appropriate platform-specific method: Capacitor Google Auth plugin on native (iOS/Android) and Firebase Auth `signInWithPopup` with GoogleAuthProvider on web.
2. WHEN Google Sign-In returns a valid credential, THE Auth_Service SHALL sign in to Firebase Auth using `signInWithCredential` with the Google credential.
3. WHEN Firebase Auth confirms authentication, THE Auth_Service SHALL check if the user profile exists in the backend by calling `GET /api/v1/users/me`.
4. IF the backend returns a 404 (user not found), THEN THE Auth_Service SHALL call `POST /api/v1/users/register` to create the user profile and navigate to `/onboarding`.
5. IF the backend returns a valid user profile, THEN THE Auth_Service SHALL navigate to `/tabs/home` or the stored redirect URL.
6. IF the user cancels the Google Sign-In flow, THEN THE Welcome_Screen SHALL remain displayed without showing an error message.
7. IF Google Sign-In fails due to a network error or provider error, THEN THE Auth_Service SHALL display a toast notification: "Sign-in failed. Please try again."

### Requirement 7: Apple Sign-In

**User Story:** As an iOS user, I want to sign in with my Apple ID, so that I can use a familiar and privacy-focused authentication method.

#### Acceptance Criteria

1. WHEN the user initiates Apple Sign-In, THE Auth_Service SHALL use the Capacitor Sign In with Apple plugin on native iOS and Firebase Auth `signInWithPopup` with OAuthProvider('apple.com') on web.
2. WHEN Apple Sign-In returns a valid credential, THE Auth_Service SHALL sign in to Firebase Auth using `signInWithCredential` with the Apple credential.
3. WHEN Firebase Auth confirms authentication, THE Auth_Service SHALL check if the user profile exists in the backend by calling `GET /api/v1/users/me`.
4. IF the backend returns a 404 (user not found), THEN THE Auth_Service SHALL call `POST /api/v1/users/register` to create the user profile and navigate to `/onboarding`.
5. IF the backend returns a valid user profile, THEN THE Auth_Service SHALL navigate to `/tabs/home` or the stored redirect URL.
6. IF the user cancels the Apple Sign-In flow, THEN THE Welcome_Screen SHALL remain displayed without showing an error message.
7. IF Apple Sign-In fails due to a network error or provider error, THEN THE Auth_Service SHALL display a toast notification: "Sign-in failed. Please try again."
8. THE Apple Sign-In button SHALL only be rendered on iOS native devices and web browsers; it SHALL NOT be rendered on Android native devices.

### Requirement 8: Guest Mode (Anonymous Authentication)

**User Story:** As a curious user, I want to explore the app without creating an account, so that I can evaluate the experience before committing to registration.

#### Acceptance Criteria

1. WHEN the user taps "Continue as Guest", THE Auth_Service SHALL call Firebase Auth `signInAnonymously` to create an anonymous session.
2. WHEN anonymous authentication succeeds, THE Auth_Service SHALL call `POST /api/v1/users/register` with a guest flag to create a guest user profile, then navigate to `/onboarding`.
3. IF anonymous authentication fails, THEN THE Welcome_Screen SHALL display a toast notification: "Could not start guest session. Please try again."
4. WHILE in Guest_Mode, THE app SHALL store quest progress and user data locally via the Storage Service.
5. THE Auth_Service SHALL expose a signal `isGuest: Signal<boolean>` that returns true when the current Firebase user is anonymous (`user.isAnonymous === true`).
6. WHEN a guest user later signs up or links a social account, THE Auth_Service SHALL use Firebase Auth `linkWithCredential` to upgrade the anonymous account, preserving the existing user ID and progress.
7. IF account linking fails because the credential is already associated with another account, THEN THE Auth_Service SHALL display an error message: "This account is already registered. Please log in instead." and offer to sign in with the existing account.

### Requirement 9: Session Persistence

**User Story:** As a user, I want to remain logged in after closing and reopening the app, so that I don't have to sign in every time.

#### Acceptance Criteria

1. THE Auth_Service SHALL configure Firebase Auth persistence to `browserLocalPersistence` on web and rely on native Capacitor secure storage on mobile platforms, ensuring the auth state survives app restarts.
2. WHEN the app launches and a persisted auth session exists, THE Splash_Screen SHALL resolve the authenticated user from the persisted state without requiring user interaction.
3. WHEN the persisted Firebase ID_Token has expired, THE Auth_Service SHALL automatically refresh the token using Firebase Auth's built-in token refresh mechanism before any backend API call is made.
4. IF the token refresh fails (e.g., account disabled, user deleted from Firebase), THEN THE Auth_Service SHALL clear the local session, reset the UserStore to null, and navigate to the Welcome_Screen.
5. THE Auth_Service SHALL expose a signal `currentUser: Signal<User | null>` that reflects the current Firebase Auth user state and updates reactively when auth state changes.
6. WHEN the user explicitly logs out, THE Auth_Service SHALL call Firebase Auth `signOut`, clear all locally persisted auth data, reset the UserStore, and navigate to the Welcome_Screen.
7. THE Auth_Service SHALL subscribe to Firebase Auth `onAuthStateChanged` at app initialization and keep the `currentUser` signal synchronized with every auth state transition.

### Requirement 10: Auth Service State Management

**User Story:** As a developer, I want a centralized auth service that exposes reactive authentication state, so that components and guards can respond to auth changes consistently.

#### Acceptance Criteria

1. THE Auth_Service SHALL be injectable as a singleton (`providedIn: 'root'`).
2. THE Auth_Service SHALL expose the following signals: `currentUser: Signal<User | null>`, `isAuthenticated: Signal<boolean>`, `isGuest: Signal<boolean>`, and `authReady: Signal<boolean>`.
3. THE `authReady` signal SHALL be `false` during initial auth state resolution and transition to `true` once the first `onAuthStateChanged` emission is processed or the 5-second timeout elapses.
4. THE `isAuthenticated` signal SHALL be a computed signal that returns `true` when `currentUser` is not null.
5. THE `isGuest` signal SHALL be a computed signal that returns `true` when `currentUser` is not null and `currentUser.isAnonymous` is true.
6. THE Auth_Service SHALL provide methods: `loginWithEmail(email, password)`, `signupWithEmail(email, password)`, `loginWithGoogle()`, `loginWithApple()`, `loginAsGuest()`, `sendPasswordReset(email)`, `linkAccount(credential)`, and `logout()`.
7. WHEN any authentication method fails, THE Auth_Service SHALL throw a typed error object containing a `code` string and a `message` string, allowing calling components to display appropriate user-facing messages.
8. THE Auth_Service SHALL not call HTTP endpoints directly; it SHALL delegate backend communication to a dedicated UserService for profile creation and retrieval.

### Requirement 11: Auth Page Navigation and Routing

**User Story:** As a user, I want smooth navigation between auth screens with proper back-button behavior, so that the authentication flow feels cohesive and I can easily move between screens.

#### Acceptance Criteria

1. THE Auth_Module SHALL define routes under the `/auth` path prefix with child routes: `/auth/welcome`, `/auth/login`, `/auth/signup`, and `/auth/forgot-password`.
2. WHEN an unauthenticated user accesses the app, THE router SHALL navigate to `/auth/welcome` as the default auth entry point.
3. THE Login_Page, Signup_Page, and Forgot_Password_Page SHALL include a back navigation button (ion-back-button) that returns to the previous auth screen in the navigation stack.
4. WHEN an authenticated user attempts to navigate to any `/auth/*` route, THE Auth_Module route guard SHALL redirect to `/tabs/home` to prevent authenticated users from seeing auth screens.
5. THE auth routes SHALL use Ionic page transition animations (slide-in from right for forward navigation, slide-out to right for back navigation) consistent with the app's navigation patterns.
6. WHEN authentication succeeds on any auth screen, THE navigation SHALL use `navigateRoot` (Ionic NavController) to replace the navigation stack, preventing the user from navigating back to auth screens via the back button.

### Requirement 12: Form Validation and Error Display

**User Story:** As a user, I want clear, immediate feedback on form errors, so that I can correct my input before submitting.

#### Acceptance Criteria

1. THE Login_Page and Signup_Page SHALL validate the email field on blur and display an error message "Please enter a valid email address" when the format is invalid.
2. THE Signup_Page SHALL validate the password field in real-time (on input) and display checkmark/cross indicators for each requirement: minimum 8 characters, at least one uppercase letter, at least one special character.
3. THE Signup_Page SHALL validate the confirm password field on input and display an error message "Passwords do not match" when the value differs from the password field.
4. WHEN a form field has a validation error and the user corrects the input, THE error message SHALL disappear immediately upon the field becoming valid.
5. THE form error messages SHALL be displayed below the respective input field using the app's error color (`#F44336`) and a font size no smaller than 12px for readability.
6. WHEN the user submits a form with validation errors, THE page SHALL focus the first invalid field and scroll it into view if necessary.
7. THE email and password input fields SHALL use Angular Reactive Forms with synchronous validators for client-side rules.
