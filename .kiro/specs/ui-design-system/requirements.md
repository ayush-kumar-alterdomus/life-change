# Requirements Document

## Introduction

The UI Design System provides a unified, reusable visual foundation for the Ascend mobile application. It defines the color palette, typography, SCSS infrastructure, theme switching, shared base components, gamification-specific UI components, custom directives, pipes, and shared data models. The design system ensures visual consistency across all features while delivering a premium RPG-inspired aesthetic optimized for mobile-first interaction.

## Glossary

- **Design_System**: The collection of SCSS variables, mixins, components, directives, pipes, and models that form the shared UI foundation of the Ascend application.
- **Theme_Service**: An Angular injectable service that manages dark/light mode switching and persists user theme preference.
- **Base_Component**: A reusable, presentation-only Angular standalone component with no business logic (app-button, app-card, app-modal, app-toast, app-loader, app-badge, app-progress).
- **Game_Component**: A reusable, presentation-only Angular standalone component specific to gamification UI (xp-progress-bar, level-badge, streak-flame, quest-card, arc-card, boss-card, achievement-card, guild-card, leaderboard-card, stat-radar).
- **Directive**: An Angular attribute directive that adds behavior to host elements (long-press, swipe, animate-on-view).
- **Pipe**: An Angular pipe that transforms display values in templates (time-ago, xp-format, level-title).
- **SCSS_Variables**: CSS custom properties and SCSS variables defining the color palette, spacing, typography, and elevation tokens.
- **SCSS_Mixins**: Reusable SCSS mixins for common styling patterns (cards, gradients, responsive breakpoints, animations).
- **Dark_Mode**: The default theme using #0A0A0A background with light text and vibrant accent colors.
- **Light_Mode**: An alternative theme using light backgrounds with dark text while preserving accent colors.
- **Shared_Model**: A TypeScript interface or type definition representing a domain entity used across multiple features.
- **Shared_Enum**: A TypeScript enum representing a fixed set of domain values used across multiple features.

## Requirements

### Requirement 1: Color Palette SCSS Variables

**User Story:** As a developer, I want a centralized color palette defined as SCSS variables and CSS custom properties, so that all components use consistent colors without hardcoding hex values.

#### Acceptance Criteria

1. THE Design_System SHALL define CSS custom properties for background (#0A0A0A), card (#161616), accent/primary (#FF9800), secondary (#A855F7), success (#4CAF50), error (#F44336), text-primary (#FFFFFF), and text-secondary (#B0B0B0) colors.
2. THE Design_System SHALL define SCSS variables for spacing scale (4px, 8px, 12px, 16px, 24px, 32px, 48px), border-radius tokens (4px, 8px, 12px, 16px, 24px), and elevation/shadow tokens.
3. THE Design_System SHALL define CSS custom properties for Ionic color overrides (--ion-color-primary, --ion-color-secondary, --ion-color-success, --ion-color-danger) mapped to the Ascend palette.
4. WHEN a developer imports the variables file, THE Design_System SHALL expose all tokens without requiring additional configuration.

### Requirement 2: Typography System

**User Story:** As a developer, I want a typography system with Inter for body text and Orbitron for headings and game elements, so that the app has a consistent premium RPG aesthetic.

#### Acceptance Criteria

1. THE Design_System SHALL define Inter as the primary font family for body text, labels, and UI elements.
2. THE Design_System SHALL define Orbitron as the display font family for headings, level indicators, XP values, and game-related text.
3. THE Design_System SHALL define a type scale with at least six sizes: xs (12px), sm (14px), base (16px), lg (18px), xl (24px), and 2xl (32px).
4. THE Design_System SHALL define font-weight tokens for regular (400), medium (500), semibold (600), and bold (700).
5. THE Design_System SHALL include @font-face declarations or Google Fonts imports for Inter and Orbitron.

### Requirement 3: SCSS Mixins Library

**User Story:** As a developer, I want reusable SCSS mixins for common UI patterns, so that I can build consistent interfaces without duplicating styles.

#### Acceptance Criteria

1. THE Design_System SHALL provide a mixin for glass-morphism card styling (semi-transparent background, backdrop blur, border).
2. THE Design_System SHALL provide mixins for responsive breakpoints (mobile, tablet, desktop).
3. THE Design_System SHALL provide a mixin for gradient backgrounds using the accent and secondary colors.
4. THE Design_System SHALL provide a mixin for text truncation (single-line ellipsis and multi-line clamp).
5. THE Design_System SHALL provide a mixin for flex layout helpers (center, between, column).
6. THE Design_System SHALL provide a mixin for glow/neon effects using accent and secondary colors.
7. THE Design_System SHALL provide a mixin for skeleton loading placeholder animations.

### Requirement 4: Theme Service (Dark/Light Mode)

**User Story:** As a user, I want to switch between dark and light themes, so that I can use the app comfortably in different lighting conditions.

#### Acceptance Criteria

1. THE Theme_Service SHALL default to dark mode on first launch.
2. WHEN the user selects a theme preference, THE Theme_Service SHALL persist the selection to local storage.
3. WHEN the app initializes, THE Theme_Service SHALL restore the previously saved theme preference from local storage.
4. WHEN the theme changes, THE Theme_Service SHALL apply the corresponding CSS custom property values to the document root element.
5. THE Theme_Service SHALL expose a reactive signal indicating the current active theme (dark or light).
6. WHEN no saved preference exists and the device prefers light mode, THE Theme_Service SHALL use the device preference as the initial theme.
7. THE Theme_Service SHALL toggle all color tokens (background, card, text-primary, text-secondary, borders) when switching between dark and light modes.

### Requirement 5: App Button Component

**User Story:** As a developer, I want a reusable button component with multiple variants, so that all buttons across the app have consistent styling and behavior.

#### Acceptance Criteria

1. THE Base_Component (app-button) SHALL support variant inputs: primary, secondary, outline, ghost, and danger.
2. THE Base_Component (app-button) SHALL support size inputs: small, medium, and large.
3. THE Base_Component (app-button) SHALL support a disabled state that prevents interaction and applies reduced opacity.
4. THE Base_Component (app-button) SHALL support a loading state that displays a spinner and prevents interaction.
5. THE Base_Component (app-button) SHALL emit a click event only when the button is not disabled and not loading.
6. THE Base_Component (app-button) SHALL meet minimum touch target size of 44px height for mobile accessibility.

### Requirement 6: App Card Component

**User Story:** As a developer, I want a reusable card component with glass-morphism styling, so that content containers are visually consistent.

#### Acceptance Criteria

1. THE Base_Component (app-card) SHALL render with the glass-morphism card style (dark semi-transparent background, subtle border, border-radius).
2. THE Base_Component (app-card) SHALL support content projection for header, body, and footer sections.
3. THE Base_Component (app-card) SHALL support an elevated variant with increased shadow depth.
4. THE Base_Component (app-card) SHALL support a clickable variant that applies hover/press feedback and emits a click event.

### Requirement 7: App Modal Component

**User Story:** As a developer, I want a reusable modal component, so that overlay dialogs have consistent presentation and behavior.

#### Acceptance Criteria

1. THE Base_Component (app-modal) SHALL display content in a centered overlay with a backdrop.
2. WHEN the user taps the backdrop, THE Base_Component (app-modal) SHALL emit a dismiss event.
3. THE Base_Component (app-modal) SHALL support content projection for header, body, and footer sections.
4. THE Base_Component (app-modal) SHALL animate in with a scale-up and fade-in transition.
5. THE Base_Component (app-modal) SHALL support a closable input that controls whether the dismiss button is visible.

### Requirement 8: App Toast Component

**User Story:** As a developer, I want a reusable toast notification component, so that transient messages are displayed consistently.

#### Acceptance Criteria

1. THE Base_Component (app-toast) SHALL support type variants: success, error, warning, and info.
2. THE Base_Component (app-toast) SHALL display with an icon, message text, and optional action button.
3. THE Base_Component (app-toast) SHALL auto-dismiss after a configurable duration (default 3000ms).
4. THE Base_Component (app-toast) SHALL animate in from the top of the viewport and animate out on dismiss.
5. WHEN multiple toasts are active, THE Base_Component (app-toast) SHALL stack them vertically without overlap.

### Requirement 9: App Loader Component

**User Story:** As a developer, I want a reusable loading indicator component, so that loading states are presented consistently.

#### Acceptance Criteria

1. THE Base_Component (app-loader) SHALL support display modes: spinner, skeleton, and progress.
2. THE Base_Component (app-loader) SHALL support a size input: small, medium, and large.
3. WHEN in skeleton mode, THE Base_Component (app-loader) SHALL render animated placeholder shapes matching common content layouts (text lines, circles, rectangles).
4. THE Base_Component (app-loader) SHALL use the accent color (#FF9800) for the spinner animation.

### Requirement 10: App Badge Component

**User Story:** As a developer, I want a reusable badge component, so that status indicators and labels are displayed consistently.

#### Acceptance Criteria

1. THE Base_Component (app-badge) SHALL support color variants: primary, secondary, success, danger, warning, and neutral.
2. THE Base_Component (app-badge) SHALL support size inputs: small and medium.
3. THE Base_Component (app-badge) SHALL support a dot-only mode that displays a colored indicator without text.
4. THE Base_Component (app-badge) SHALL render with rounded-pill shape and appropriate padding.

### Requirement 11: App Progress Component

**User Story:** As a developer, I want a reusable progress bar component, so that progress indicators are displayed consistently.

#### Acceptance Criteria

1. THE Base_Component (app-progress) SHALL accept a value input between 0 and 100 representing completion percentage.
2. THE Base_Component (app-progress) SHALL support color variants: primary (accent), secondary, success, and danger.
3. THE Base_Component (app-progress) SHALL support an animated mode that transitions smoothly when the value changes.
4. THE Base_Component (app-progress) SHALL support displaying the percentage label inside or above the bar.
5. THE Base_Component (app-progress) SHALL support a striped variant for indeterminate-style visual feedback.

### Requirement 12: XP Progress Bar Component

**User Story:** As a user, I want to see my XP progress toward the next level in a visually engaging bar, so that I understand how close I am to leveling up.

#### Acceptance Criteria

1. THE Game_Component (xp-progress-bar) SHALL accept currentXp and requiredXp numeric inputs.
2. THE Game_Component (xp-progress-bar) SHALL display the fill percentage calculated as (currentXp / requiredXp) × 100.
3. THE Game_Component (xp-progress-bar) SHALL display the current XP and required XP as formatted text (e.g., "2,400 / 3,000 XP").
4. THE Game_Component (xp-progress-bar) SHALL animate the fill width smoothly when the currentXp value changes.
5. THE Game_Component (xp-progress-bar) SHALL use a gradient fill from the accent color (#FF9800) to the secondary color (#A855F7).

### Requirement 13: Level Badge Component

**User Story:** As a user, I want to see my current level displayed as a prominent badge, so that my progression is always visible.

#### Acceptance Criteria

1. THE Game_Component (level-badge) SHALL accept a level numeric input.
2. THE Game_Component (level-badge) SHALL display the level number using the Orbitron display font.
3. THE Game_Component (level-badge) SHALL support size variants: small (for inline use) and large (for profile/header).
4. THE Game_Component (level-badge) SHALL apply a glow effect using the accent color.

### Requirement 14: Streak Flame Component

**User Story:** As a user, I want to see my current streak displayed with a flame icon, so that I feel motivated to maintain my streak.

#### Acceptance Criteria

1. THE Game_Component (streak-flame) SHALL accept a streakDays numeric input.
2. THE Game_Component (streak-flame) SHALL display the streak count alongside a flame icon.
3. WHEN streakDays is 0, THE Game_Component (streak-flame) SHALL display the flame in a dimmed/inactive state.
4. WHEN streakDays exceeds milestone thresholds (7, 30, 100), THE Game_Component (streak-flame) SHALL apply progressively more intense visual styling (color intensity, size, animation).
5. THE Game_Component (streak-flame) SHALL use the Orbitron font for the streak number.

### Requirement 15: Quest Card Component

**User Story:** As a user, I want to see quests displayed as interactive cards with key information, so that I can quickly understand and act on available quests.

#### Acceptance Criteria

1. THE Game_Component (quest-card) SHALL accept inputs for title, xpReward, difficulty, timeEstimate, and statType.
2. THE Game_Component (quest-card) SHALL display the difficulty as a colored badge (easy=green, medium=orange, hard=red, legendary=purple).
3. THE Game_Component (quest-card) SHALL display the XP reward formatted with the xp-format pipe.
4. THE Game_Component (quest-card) SHALL emit events for complete, edit, and skip actions.
5. THE Game_Component (quest-card) SHALL support a completed state with reduced opacity and a checkmark overlay.

### Requirement 16: Arc Card Component

**User Story:** As a user, I want to see my active arc displayed as a visually rich card, so that I can track my long-term progression.

#### Acceptance Criteria

1. THE Game_Component (arc-card) SHALL accept inputs for arcName, progressPercentage, currentPhase, and arcType.
2. THE Game_Component (arc-card) SHALL display a progress bar showing the arc completion percentage.
3. THE Game_Component (arc-card) SHALL display the current phase name.
4. THE Game_Component (arc-card) SHALL emit a navigate event when tapped.
5. THE Game_Component (arc-card) SHALL apply a themed gradient background based on the arcType.

### Requirement 17: Boss Card Component

**User Story:** As a user, I want to see boss battles displayed as dramatic cards, so that I feel the challenge and excitement of upcoming bosses.

#### Acceptance Criteria

1. THE Game_Component (boss-card) SHALL accept inputs for bossName, bossLevel, healthPercentage, and defeated status.
2. THE Game_Component (boss-card) SHALL display a health bar showing the boss remaining health.
3. WHEN defeated is true, THE Game_Component (boss-card) SHALL display a defeated overlay with reduced opacity.
4. THE Game_Component (boss-card) SHALL emit a challenge event when the challenge button is tapped.
5. THE Game_Component (boss-card) SHALL use dramatic styling with the secondary color (#A855F7) as the dominant accent.

### Requirement 18: Achievement Card Component

**User Story:** As a user, I want to see achievements displayed as collectible cards, so that I feel rewarded for my accomplishments.

#### Acceptance Criteria

1. THE Game_Component (achievement-card) SHALL accept inputs for title, description, iconUrl, unlockedAt date, and locked status.
2. WHEN locked is true, THE Game_Component (achievement-card) SHALL display in a grayscale/dimmed state with a lock icon overlay.
3. WHEN locked is false, THE Game_Component (achievement-card) SHALL display the unlock date formatted with the time-ago pipe.
4. THE Game_Component (achievement-card) SHALL emit a tap event for viewing achievement details.

### Requirement 19: Guild Card Component

**User Story:** As a user, I want to see guilds displayed as informative cards, so that I can browse and compare guilds.

#### Acceptance Criteria

1. THE Game_Component (guild-card) SHALL accept inputs for guildName, memberCount, guildLevel, and guildRank.
2. THE Game_Component (guild-card) SHALL display the member count and guild level.
3. THE Game_Component (guild-card) SHALL emit a join event and a view event.
4. THE Game_Component (guild-card) SHALL display a rank badge when guildRank is provided.

### Requirement 20: Leaderboard Card Component

**User Story:** As a user, I want to see leaderboard entries as compact cards, so that I can compare my ranking with others.

#### Acceptance Criteria

1. THE Game_Component (leaderboard-card) SHALL accept inputs for rank, username, level, xpTotal, and avatarUrl.
2. THE Game_Component (leaderboard-card) SHALL apply special styling (gold, silver, bronze) for ranks 1, 2, and 3.
3. THE Game_Component (leaderboard-card) SHALL highlight the current user entry with a distinct border or background.
4. THE Game_Component (leaderboard-card) SHALL display the XP total formatted with the xp-format pipe.

### Requirement 21: Stat Radar Component

**User Story:** As a user, I want to see my character stats displayed as a radar/spider chart, so that I can visualize my strengths and weaknesses.

#### Acceptance Criteria

1. THE Game_Component (stat-radar) SHALL accept an array of stat objects containing name and value (0–100 scale).
2. THE Game_Component (stat-radar) SHALL render a radar/spider chart with labeled axes for each stat.
3. THE Game_Component (stat-radar) SHALL use the accent color (#FF9800) for the filled area and the secondary color (#A855F7) for the border.
4. THE Game_Component (stat-radar) SHALL support a size input to control the chart dimensions.
5. THE Game_Component (stat-radar) SHALL render using SVG for crisp display at all resolutions.

### Requirement 22: Long Press Directive

**User Story:** As a developer, I want a long-press directive, so that I can add long-press interactions to elements without duplicating touch-handling logic.

#### Acceptance Criteria

1. WHEN the user presses and holds an element for a configurable duration (default 500ms), THE Directive (long-press) SHALL emit a longPress event.
2. IF the user moves their finger beyond a threshold distance during the press, THEN THE Directive (long-press) SHALL cancel the long-press detection.
3. THE Directive (long-press) SHALL accept a duration input to customize the hold time threshold.
4. THE Directive (long-press) SHALL not interfere with normal tap events when the hold duration is not reached.

### Requirement 23: Swipe Directive

**User Story:** As a developer, I want a swipe directive, so that I can detect swipe gestures on elements for actions like dismissing cards or revealing options.

#### Acceptance Criteria

1. WHEN the user swipes left on the host element, THE Directive (swipe) SHALL emit a swipeLeft event.
2. WHEN the user swipes right on the host element, THE Directive (swipe) SHALL emit a swipeRight event.
3. THE Directive (swipe) SHALL require a minimum swipe distance threshold (default 50px) before emitting.
4. THE Directive (swipe) SHALL require a minimum swipe velocity to distinguish swipes from slow drags.
5. THE Directive (swipe) SHALL accept a disabled input to conditionally disable swipe detection.

### Requirement 24: Animate On View Directive

**User Story:** As a developer, I want an animate-on-view directive, so that elements animate into view when they enter the viewport for engaging scroll experiences.

#### Acceptance Criteria

1. WHEN the host element enters the viewport, THE Directive (animate-on-view) SHALL apply a CSS animation class to the element.
2. THE Directive (animate-on-view) SHALL support animation type inputs: fade-in, slide-up, slide-left, scale-in.
3. THE Directive (animate-on-view) SHALL accept a delay input to stagger animations for lists of elements.
4. THE Directive (animate-on-view) SHALL use IntersectionObserver for efficient viewport detection.
5. THE Directive (animate-on-view) SHALL trigger the animation only once per element by default.

### Requirement 25: Time Ago Pipe

**User Story:** As a developer, I want a time-ago pipe, so that dates are displayed as human-readable relative time strings.

#### Acceptance Criteria

1. WHEN a Date or timestamp is provided, THE Pipe (time-ago) SHALL transform it into a relative time string (e.g., "2 hours ago", "3 days ago", "just now").
2. THE Pipe (time-ago) SHALL handle edge cases: null input returns an empty string, future dates return "just now".
3. THE Pipe (time-ago) SHALL support granularity levels: seconds, minutes, hours, days, weeks, months, years.

### Requirement 26: XP Format Pipe

**User Story:** As a developer, I want an xp-format pipe, so that XP values are displayed with consistent formatting across the app.

#### Acceptance Criteria

1. WHEN a numeric XP value is provided, THE Pipe (xp-format) SHALL format it with thousand separators and an "XP" suffix (e.g., 2400 becomes "2,400 XP").
2. WHEN the value exceeds 1,000,000, THE Pipe (xp-format) SHALL abbreviate it (e.g., 1,500,000 becomes "1.5M XP").
3. WHEN the value exceeds 1,000 but is below 1,000,000, THE Pipe (xp-format) SHALL optionally abbreviate it (e.g., 2,400 becomes "2.4K XP") based on a compact input flag.
4. WHEN a null or undefined value is provided, THE Pipe (xp-format) SHALL return "0 XP".

### Requirement 27: Level Title Pipe

**User Story:** As a developer, I want a level-title pipe, so that numeric levels are displayed with their corresponding title names.

#### Acceptance Criteria

1. WHEN a numeric level is provided, THE Pipe (level-title) SHALL return the corresponding title string based on defined level ranges (e.g., 1–15: "Beginner", 16–40: "Intermediate", 41–75: "Advanced", 76–100: "Elite").
2. THE Pipe (level-title) SHALL accept an optional format input to return either the title alone or "Level X — Title" format.
3. WHEN a null or undefined level is provided, THE Pipe (level-title) SHALL return "Unknown".

### Requirement 28: Shared Models

**User Story:** As a developer, I want shared TypeScript interfaces for domain entities, so that type safety is maintained across all features.

#### Acceptance Criteria

1. THE Design_System SHALL define TypeScript interfaces for: User, Quest, Arc, Guild, Streak, Achievement, Boss, and Notification models.
2. THE Design_System SHALL define each model with required and optional properties matching the API contract.
3. THE Design_System SHALL export all models from a single barrel file (index.ts) for convenient importing.

### Requirement 29: Shared Enums

**User Story:** As a developer, I want shared TypeScript enums for domain constants, so that magic strings are eliminated and type safety is enforced.

#### Acceptance Criteria

1. THE Design_System SHALL define TypeScript enums for: Difficulty (easy, medium, hard, legendary), StatType (focus, strength, wisdom, vitality, discipline), League (bronze, silver, gold, platinum, diamond), ArcType (monk, warrior, scholar, creator, athlete), and QuestFrequency (daily, weekly, custom).
2. THE Design_System SHALL export all enums from a single barrel file (index.ts) for convenient importing.
3. THE Design_System SHALL define enum values as lowercase string literals for API compatibility.

### Requirement 30: Component Accessibility

**User Story:** As a user with accessibility needs, I want all design system components to be accessible, so that I can use the app with assistive technologies.

#### Acceptance Criteria

1. THE Design_System SHALL ensure all interactive Base_Components include appropriate ARIA attributes (role, aria-label, aria-disabled).
2. THE Design_System SHALL ensure all Base_Components support keyboard navigation where applicable.
3. THE Design_System SHALL ensure color contrast ratios meet WCAG 2.1 AA standards (4.5:1 for normal text, 3:1 for large text) in both dark and light themes.
4. THE Design_System SHALL ensure all Game_Components with interactive elements include focus indicators visible in both themes.
