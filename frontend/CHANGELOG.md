# Changelog

All notable changes to the Ascend frontend will be documented in this file.

## [Unreleased]

### Breaking Changes

- **Selector rename**: All shared UI component selectors changed from `game-*` to `app-*` prefix.
  - `game-achievement-card` → `app-achievement-card`
  - `game-arc-card` → `app-arc-card`
  - `game-boss-card` → `app-boss-card`
  - `game-guild-card` → `app-guild-card`
  - `game-leaderboard-card` → `app-leaderboard-card`
  - `game-level-badge` → `app-level-badge`
  - `game-quest-card` → `app-quest-card`
  - `game-stat-radar` → `app-stat-radar`
  - `game-streak-flame` → `app-streak-flame`
  - `game-xp-progress-bar` → `app-xp-progress-bar`

- **Class renames**: All `*Page` and `*Step` classes renamed to `*Component`.
  - Example: `LoginPage` → `LoginComponent`, `QuizStep` → `QuizComponent`

- **File renames**: All `.page.ts` and `.step.ts` files renamed to `.component.ts` (including associated `.html` and `.scss` files).

### Added

- `toAuthError()` type guard and safe extractor in `core/models/auth-error.model.ts`
- `sanitizeRedirectUrl()` in login component to prevent open redirect attacks
- Content Security Policy meta tag in `index.html`
- `DashboardStore` service to separate data fetching from `DashboardComponent`
- `CreateQuestModalComponent` extracted from `QuestBoardComponent`
- `takeUntilDestroyed` on all subscriptions in `QuestBoardComponent`
- Unit tests for `QuestService`, `OnboardingService`, auth guard contract, and `AuthService` error mapping

### Changed

- `host` metadata replaced with `@HostBinding`/`@HostListener` across all 17 shared components
- `document.querySelector` replaced with `ElementRef.nativeElement.querySelector` in login/signup
- `scrollbar-width: none` wrapped in `@supports` for cross-browser compatibility
- Viewport meta tag no longer restricts user zoom (accessibility fix)

### Removed

- Dead `auth/pages/signup.page.ts` (inline template placeholder)
- Stale nested `frontend/frontend/` duplicate directory

### Fixed

- "Continue as Guest" now uses `signInAnonymously` so auth guard allows navigation
- `-webkit-background-clip` ordered before `background-clip` in streak-flame styles
