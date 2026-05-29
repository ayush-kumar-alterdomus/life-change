# Implementation Plan: UI Design System

## Overview

This plan implements the Ascend UI Design System in dependency order: SCSS tokens first, then the theme service, followed by shared models/enums, pipes, directives, base components, and finally game components. Each task builds on the previous, ensuring no orphaned code. Property-based tests use `fast-check` with Jest.

## Tasks

- [x] 1. Set up SCSS token layer
  - [x] 1.1 Create `src/theme/_variables.scss` with CSS custom properties and SCSS variables
    - Define color tokens: background (#0A0A0A), card (#161616), primary (#FF9800), secondary (#A855F7), success (#4CAF50), error (#F44336), text-primary (#FFFFFF), text-secondary (#B0B0B0), border
    - Define Ionic color overrides: --ion-color-primary, --ion-color-secondary, --ion-color-success, --ion-color-danger, --ion-background-color, --ion-card-background
    - Define spacing scale: --space-1 (4px) through --space-12 (48px)
    - Define border-radius tokens: --radius-sm (4px) through --radius-full (9999px)
    - Define elevation/shadow tokens: --shadow-sm, --shadow-md, --shadow-lg, --shadow-glow-primary, --shadow-glow-secondary
    - Define SCSS breakpoint variables: $breakpoint-mobile (576px), $breakpoint-tablet (768px), $breakpoint-desktop (1024px)
    - _Requirements: 1.1, 1.2, 1.3, 1.4_

  - [x] 1.2 Create `src/theme/_typography.scss` with font imports and type scale
    - Add @font-face or Google Fonts imports for Inter and Orbitron
    - Define CSS custom properties for font families: --font-body (Inter), --font-display (Orbitron)
    - Define type scale custom properties: --text-xs (12px), --text-sm (14px), --text-base (16px), --text-lg (18px), --text-xl (24px), --text-2xl (32px)
    - Define font-weight tokens: --font-regular (400), --font-medium (500), --font-semibold (600), --font-bold (700)
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

  - [x] 1.3 Create `src/theme/_mixins.scss` with reusable SCSS mixins
    - Implement glass-morphism card mixin (semi-transparent background, backdrop-filter blur, border)
    - Implement responsive breakpoint mixins (mobile, tablet, desktop)
    - Implement gradient background mixin using primary and secondary colors
    - Implement text truncation mixins (single-line ellipsis, multi-line clamp)
    - Implement flex layout helper mixins (center, between, column)
    - Implement glow/neon effect mixin using accent and secondary colors
    - Implement skeleton loading placeholder animation mixin
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7_

  - [x] 1.4 Create `src/theme/_animations.scss` with keyframe animations
    - Define fade-in, slide-up, slide-left, scale-in keyframes for animate-on-view directive
    - Define modal scale-up and fade-in animation
    - Define toast slide-in-from-top and slide-out animations
    - Define skeleton shimmer animation
    - Define progress bar fill transition
    - _Requirements: 7.4, 8.4, 24.2_

  - [x] 1.5 Create `src/theme/_dark-theme.scss` and `src/theme/_light-theme.scss`
    - Dark theme: define all color token values (default — #0A0A0A background, #FFFFFF text)
    - Light theme: define overridden color token values (light backgrounds, dark text, preserved accents)
    - Use `[data-theme="dark"]` and `[data-theme="light"]` selectors on :root
    - _Requirements: 4.7_

  - [x] 1.6 Update `src/theme/global.scss` to import all partials
    - Import _variables, _typography, _mixins, _animations, _dark-theme, _light-theme
    - Set global body styles using design tokens
    - _Requirements: 1.4_

- [x] 2. Implement shared models and enums
  - [x] 2.1 Create shared enum files with barrel export
    - Create `src/shared/enums/difficulty.enum.ts` with Easy, Medium, Hard, Legendary (lowercase string values)
    - Create `src/shared/enums/stat-type.enum.ts` with Focus, Strength, Wisdom, Vitality, Discipline
    - Create `src/shared/enums/league.enum.ts` with Bronze, Silver, Gold, Platinum, Diamond
    - Create `src/shared/enums/arc-type.enum.ts` with Monk, Warrior, Scholar, Creator, Athlete
    - Create `src/shared/enums/quest-frequency.enum.ts` with Daily, Weekly, Custom
    - Create `src/shared/enums/index.ts` barrel export
    - _Requirements: 29.1, 29.2, 29.3_

  - [ ]* 2.2 Write property test for enum values (Property 14)
    - **Property 14: Enum values are lowercase strings**
    - Verify all enum member values match `/^[a-z]+$/` pattern
    - **Validates: Requirements 29.3**

  - [x] 2.3 Create shared model interfaces with barrel export
    - Create `src/shared/models/user.model.ts` with User interface
    - Create `src/shared/models/quest.model.ts` with Quest interface
    - Create `src/shared/models/arc.model.ts` with Arc, ArcPhase, ArcMilestone interfaces
    - Create `src/shared/models/guild.model.ts` with Guild interface
    - Create `src/shared/models/streak.model.ts` with Streak interface
    - Create `src/shared/models/achievement.model.ts` with Achievement interface
    - Create `src/shared/models/boss.model.ts` with Boss interface
    - Create `src/shared/models/notification.model.ts` with Notification interface
    - Create `src/shared/models/index.ts` barrel export
    - _Requirements: 28.1, 28.2, 28.3_

- [ ] 3. Implement Theme Service
  - [ ] 3.1 Create `src/core/services/theme.service.ts`
    - Implement ThemeService as injectable with `providedIn: 'root'`
    - Create `currentTheme` WritableSignal<'dark' | 'light'> defaulting to 'dark'
    - Create `isDarkMode` computed signal
    - Implement `initialize()` method: read from Ionic Storage, fall back to `prefers-color-scheme`, default to dark
    - Implement `setTheme(theme)` method: update signal, persist to storage, apply CSS custom properties to document.documentElement
    - Implement `toggleTheme()` method
    - Handle errors: corrupted storage → fallback, write failure → in-memory only
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7_

  - [ ]* 3.2 Write property test for theme persistence round-trip (Property 1)
    - **Property 1: Theme persistence round-trip**
    - For any valid theme value, set → persist → restore produces same value
    - **Validates: Requirements 4.2, 4.3**

  - [ ]* 3.3 Write property test for theme toggle idempotence (Property 2)
    - **Property 2: Theme toggle idempotence**
    - For any initial theme, toggle twice returns to original state
    - **Validates: Requirements 4.7**

- [ ] 4. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 5. Implement pipes
  - [ ] 5.1 Create `src/shared/pipes/time-ago.pipe.ts`
    - Implement TimeAgoPipe as standalone pure pipe
    - Handle null/undefined → empty string
    - Handle future dates → "just now"
    - Implement time bucket mapping: seconds, minutes, hours, days, weeks, months, years
    - _Requirements: 25.1, 25.2, 25.3_

  - [ ]* 5.2 Write property test for time-ago pipe (Property 11)
    - **Property 11: Time-ago bucket mapping**
    - Generate random past dates, verify correct bucket string is returned
    - Verify null/undefined returns empty string, future dates return "just now"
    - **Validates: Requirements 25.1, 25.2, 25.3**

  - [ ] 5.3 Create `src/shared/pipes/xp-format.pipe.ts`
    - Implement XpFormatPipe as standalone pure pipe
    - Handle null/undefined → "0 XP"
    - Handle ≥ 1,000,000 → "X.XM XP" format
    - Handle ≥ 1,000 with compact=true → "X.XK XP" format
    - Handle standard values → thousand-separated + " XP" suffix
    - _Requirements: 26.1, 26.2, 26.3, 26.4_

  - [ ]* 5.4 Write property test for xp-format pipe (Property 12)
    - **Property 12: XP format correctness**
    - Generate random non-negative numbers, verify formatting rules
    - Verify null/undefined returns "0 XP"
    - **Validates: Requirements 26.1, 26.2, 26.3, 26.4**

  - [ ] 5.5 Create `src/shared/pipes/level-title.pipe.ts`
    - Implement LevelTitlePipe as standalone pure pipe
    - Handle null/undefined → "Unknown"
    - Map level ranges: 1–15 → "Beginner", 16–40 → "Intermediate", 41–75 → "Advanced", 76+ → "Elite"
    - Support format='full' → "Level {n} — {title}"
    - _Requirements: 27.1, 27.2, 27.3_

  - [ ]* 5.6 Write property test for level-title pipe (Property 13)
    - **Property 13: Level title mapping**
    - Generate random positive integers, verify title mapping
    - Verify format='full' produces "Level {n} — {title}"
    - **Validates: Requirements 27.1, 27.2, 27.3**

  - [ ] 5.7 Create `src/shared/pipes/index.ts` barrel export
    - Export TimeAgoPipe, XpFormatPipe, LevelTitlePipe
    - _Requirements: 25, 26, 27_

- [ ] 6. Implement directives
  - [ ] 6.1 Create `src/shared/directives/long-press.directive.ts`
    - Implement LongPressDirective as standalone directive with selector `[appLongPress]`
    - Accept `duration` input (default 500ms)
    - Listen to touchstart/mousedown to start timer
    - Listen to touchmove to detect movement beyond 10px threshold → cancel
    - Listen to touchend/mouseup to cancel if timer hasn't fired
    - Emit `longPress` output after duration elapses without cancellation
    - Clean up timer on destroy
    - _Requirements: 22.1, 22.2, 22.3, 22.4_

  - [ ]* 6.2 Write property tests for long-press directive (Properties 8, 9)
    - **Property 8: Long press timing emission**
    - For any positive duration, verify emission only after duration elapses
    - **Property 9: Long press movement cancellation**
    - For any movement > 10px threshold, verify no emission regardless of hold time
    - **Validates: Requirements 22.1, 22.2**

  - [ ] 6.3 Create `src/shared/directives/swipe.directive.ts`
    - Implement SwipeDirective as standalone directive with selector `[appSwipe]`
    - Accept `disabled` input (default false) and `minDistance` input (default 50px)
    - Track touchstart position, calculate delta on touchend
    - Require minimum velocity (distance/time > 0.3px/ms) to distinguish from slow drags
    - Emit `swipeLeft` for negative delta, `swipeRight` for positive delta
    - Do not emit when disabled is true
    - _Requirements: 23.1, 23.2, 23.3, 23.4, 23.5_

  - [ ]* 6.4 Write property test for swipe direction detection (Property 10)
    - **Property 10: Swipe direction detection**
    - Generate random gesture vectors, verify correct direction emission based on distance and velocity thresholds
    - **Validates: Requirements 23.1, 23.2, 23.3, 23.4**

  - [ ] 6.5 Create `src/shared/directives/animate-on-view.directive.ts`
    - Implement AnimateOnViewDirective as standalone directive with selector `[appAnimateOnView]`
    - Accept `animationType` input: 'fade-in' | 'slide-up' | 'slide-left' | 'scale-in' (default 'fade-in')
    - Accept `delay` input (default 0ms) for staggering
    - Accept `once` input (default true) to trigger only once
    - Use IntersectionObserver (threshold 0.1) for viewport detection
    - Apply animation CSS class after delay when element enters viewport
    - Disconnect observer after first trigger if `once` is true
    - Handle IntersectionObserver not supported: skip animation, element remains visible
    - _Requirements: 24.1, 24.2, 24.3, 24.4, 24.5_

  - [ ] 6.6 Create `src/shared/directives/index.ts` barrel export
    - Export LongPressDirective, SwipeDirective, AnimateOnViewDirective
    - _Requirements: 22, 23, 24_

- [ ] 7. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 8. Implement base components
  - [ ] 8.1 Create `src/shared/components/app-button/` component
    - Create app-button.component.ts as standalone with OnPush change detection
    - Use signal inputs: variant, size, disabled, loading
    - Use output: clicked (emit only when not disabled and not loading)
    - Create app-button.component.html with button element, spinner for loading state, ng-content for label
    - Create app-button.component.scss using design tokens and mixins
    - Ensure minimum 44px touch target height
    - Add ARIA attributes: aria-disabled, aria-busy for loading
    - Support keyboard activation (Enter/Space)
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 30.1, 30.2_

  - [ ]* 8.2 Write property test for button click guard (Property 3)
    - **Property 3: Button click guard**
    - For any combination of disabled/loading states, verify clicked emits iff both are false
    - **Validates: Requirements 5.5**

  - [ ] 8.3 Create `src/shared/components/app-card/` component
    - Create app-card.component.ts as standalone with OnPush change detection
    - Use signal inputs: elevated, clickable
    - Use output: cardClick (emit only when clickable is true)
    - Create template with ng-content slots: [card-header], default, [card-footer]
    - Create SCSS using glass-morphism mixin, elevated variant with increased shadow
    - Add hover/press feedback for clickable variant
    - Add appropriate ARIA role for clickable cards
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 30.1_

  - [ ] 8.4 Create `src/shared/components/app-modal/` component
    - Create app-modal.component.ts as standalone with OnPush change detection
    - Use signal inputs: closable (default true)
    - Use output: dismiss
    - Create template with backdrop overlay, content container with ng-content slots (header, body, footer)
    - Emit dismiss on backdrop tap
    - Show/hide close button based on closable input
    - Apply scale-up + fade-in animation on entry
    - Add ARIA attributes: role="dialog", aria-modal="true"
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 30.1_

  - [ ] 8.5 Create `src/shared/components/app-toast/` component
    - Create app-toast.component.ts as standalone with OnPush change detection
    - Use signal inputs: type, message (required), duration (default 3000), actionLabel
    - Use outputs: dismissed, actionClicked
    - Create template with icon (per type), message text, optional action button
    - Implement auto-dismiss timer based on duration
    - Animate in from top, animate out on dismiss
    - Handle stacking: manage vertical offset for multiple toasts
    - Add ARIA: role="alert", aria-live="polite"
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 30.1_

  - [ ] 8.6 Create `src/shared/components/app-loader/` component
    - Create app-loader.component.ts as standalone with OnPush change detection
    - Use signal inputs: mode ('spinner' | 'skeleton' | 'progress'), size
    - Create template with conditional rendering per mode
    - Spinner mode: animated circular spinner using accent color
    - Skeleton mode: animated placeholder shapes (text lines, circles, rectangles)
    - Progress mode: simple progress indicator
    - Apply skeleton shimmer animation from _animations.scss
    - _Requirements: 9.1, 9.2, 9.3, 9.4_

  - [ ] 8.7 Create `src/shared/components/app-badge/` component
    - Create app-badge.component.ts as standalone with OnPush change detection
    - Use signal inputs: color, size, dotOnly
    - Create template: dot-only mode shows colored circle, otherwise shows ng-content text
    - Apply rounded-pill shape with appropriate padding per size
    - Map color variants to design token colors
    - _Requirements: 10.1, 10.2, 10.3, 10.4_

  - [ ] 8.8 Create `src/shared/components/app-progress/` component
    - Create app-progress.component.ts as standalone with OnPush change detection
    - Use signal inputs: value (0-100), color, animated, showLabel, labelPosition, striped
    - Implement value clamping: clamp(value, 0, 100)
    - Create template with track and fill bar, optional percentage label
    - Apply smooth transition when animated is true
    - Apply striped CSS pattern when striped is true
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5_

  - [ ]* 8.9 Write property test for progress value clamping (Property 4)
    - **Property 4: Progress value clamping**
    - For any numeric value, verify rendered fill equals clamp(value, 0, 100)
    - **Validates: Requirements 11.1**

  - [ ] 8.10 Create `src/shared/components/index.ts` barrel export
    - Export all base components
    - _Requirements: 5, 6, 7, 8, 9, 10, 11_

- [ ] 9. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 10. Implement game components (Part 1)
  - [ ] 10.1 Create `src/shared/ui/xp-progress-bar/` component
    - Create xp-progress-bar.component.ts as standalone with OnPush change detection
    - Use signal inputs: currentXp (required), requiredXp (required)
    - Implement computed fillPercentage: min((currentXp / requiredXp) * 100, 100)
    - Handle requiredXp = 0 → display 0% fill
    - Create template with gradient fill bar (accent → secondary), formatted XP text
    - Use xp-format pipe for display text (e.g., "2,400 / 3,000 XP")
    - Animate fill width on value change
    - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5_

  - [ ]* 10.2 Write property test for XP fill percentage (Property 5)
    - **Property 5: XP fill percentage calculation**
    - For any currentXp >= 0 and requiredXp > 0, verify fill = min((currentXp / requiredXp) * 100, 100)
    - **Validates: Requirements 12.2**

  - [ ] 10.3 Create `src/shared/ui/level-badge/` component
    - Create level-badge.component.ts as standalone with OnPush change detection
    - Use signal inputs: level (required), size ('small' | 'large')
    - Create template displaying level number with Orbitron font
    - Apply glow effect using accent color
    - Size variants: small for inline, large for profile/header
    - _Requirements: 13.1, 13.2, 13.3, 13.4_

  - [ ] 10.4 Create `src/shared/ui/streak-flame/` component
    - Create streak-flame.component.ts as standalone with OnPush change detection
    - Use signal inputs: streakDays (required)
    - Implement computed intensity: inactive (0), low (1-6), active (7-29), epic (30-99), legendary (100+)
    - Create template with flame icon and streak count (Orbitron font)
    - Apply progressive styling based on intensity (color, size, animation)
    - Dimmed/inactive state when streakDays is 0
    - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5_

  - [ ]* 10.5 Write property test for streak intensity mapping (Property 6)
    - **Property 6: Streak intensity mapping**
    - For any non-negative integer, verify correct intensity level based on thresholds
    - **Validates: Requirements 14.4**

  - [ ] 10.6 Create `src/shared/ui/quest-card/` component
    - Create quest-card.component.ts as standalone with OnPush change detection
    - Use signal inputs: title (required), xpReward (required), difficulty (required), timeEstimate, statType, completed
    - Use outputs: complete, edit, skip
    - Create template with difficulty badge (colored per difficulty), XP reward (xp-format pipe), time estimate
    - Apply completed state: reduced opacity + checkmark overlay
    - Import and use AppBadgeComponent for difficulty display
    - _Requirements: 15.1, 15.2, 15.3, 15.4, 15.5_

  - [ ] 10.7 Create `src/shared/ui/arc-card/` component
    - Create arc-card.component.ts as standalone with OnPush change detection
    - Use signal inputs: arcName (required), progressPercentage (required), currentPhase (required), arcType (required)
    - Use output: navigate
    - Create template with progress bar, phase name, themed gradient background per arcType
    - Emit navigate on card tap
    - _Requirements: 16.1, 16.2, 16.3, 16.4, 16.5_

- [ ] 11. Implement game components (Part 2)
  - [ ] 11.1 Create `src/shared/ui/boss-card/` component
    - Create boss-card.component.ts as standalone with OnPush change detection
    - Use signal inputs: bossName (required), bossLevel (required), healthPercentage (required), defeated
    - Use output: challenge
    - Create template with health bar, defeated overlay (reduced opacity), challenge button
    - Use secondary color (#A855F7) as dominant accent
    - Emit challenge on button tap
    - _Requirements: 17.1, 17.2, 17.3, 17.4, 17.5_

  - [ ] 11.2 Create `src/shared/ui/achievement-card/` component
    - Create achievement-card.component.ts as standalone with OnPush change detection
    - Use signal inputs: title (required), description (required), iconUrl (required), unlockedAt, locked
    - Use output: tap
    - Create template: locked state → grayscale + lock icon overlay; unlocked → show date with time-ago pipe
    - Emit tap on card interaction
    - _Requirements: 18.1, 18.2, 18.3, 18.4_

  - [ ] 11.3 Create `src/shared/ui/guild-card/` component
    - Create guild-card.component.ts as standalone with OnPush change detection
    - Use signal inputs: guildName (required), memberCount (required), guildLevel (required), guildRank
    - Use outputs: join, view
    - Create template with member count, guild level, optional rank badge
    - _Requirements: 19.1, 19.2, 19.3, 19.4_

  - [ ] 11.4 Create `src/shared/ui/leaderboard-card/` component
    - Create leaderboard-card.component.ts as standalone with OnPush change detection
    - Use signal inputs: rank (required), username (required), level (required), xpTotal (required), avatarUrl, isCurrentUser
    - Create template with rank display (gold/silver/bronze for top 3), avatar, username, level, XP (xp-format pipe)
    - Highlight current user entry with distinct border/background
    - _Requirements: 20.1, 20.2, 20.3, 20.4_

  - [ ] 11.5 Create `src/shared/ui/stat-radar/` component
    - Create stat-radar.component.ts as standalone with OnPush change detection
    - Use signal inputs: stats (required array of {name, value}), size (default 200)
    - Render SVG radar/spider chart with labeled axes
    - Use accent color (#FF9800) for filled area, secondary (#A855F7) for border
    - Calculate polygon points based on stat values (0-100 scale)
    - Handle < 3 stats: render nothing, log warning in dev mode
    - _Requirements: 21.1, 21.2, 21.3, 21.4, 21.5_

  - [ ]* 11.6 Write property test for stat radar axis count (Property 7)
    - **Property 7: Stat radar axis count**
    - For any array of N stats (N >= 3), verify SVG contains exactly N axis lines and N labels
    - **Validates: Requirements 21.2**

  - [ ] 11.7 Create `src/shared/ui/index.ts` barrel export
    - Export all game components
    - _Requirements: 12–21_

- [ ] 12. Implement accessibility and focus indicators
  - [ ] 12.1 Add focus indicators to all interactive components
    - Add visible focus ring styles to app-button, app-card (clickable), app-modal close button
    - Add focus indicators to game components with interactive elements (quest-card actions, boss-card challenge, etc.)
    - Ensure focus indicators are visible in both dark and light themes
    - Add keyboard navigation support where applicable (Enter/Space activation)
    - _Requirements: 30.1, 30.2, 30.4_

  - [ ] 12.2 Verify color contrast compliance
    - Ensure text-primary on background meets 4.5:1 ratio in both themes
    - Ensure text-secondary on background meets 4.5:1 ratio or adjust
    - Ensure large text (headings) meets 3:1 ratio
    - Document any contrast adjustments made
    - _Requirements: 30.3_

- [ ] 13. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties from the design document
- Unit tests validate specific examples and edge cases
- All components use Angular 17+ standalone pattern with signal inputs and OnPush change detection
- SCSS tokens must be created before any component that references them
- Theme service must be created before components that react to theme changes
- Pipes must be created before game components that use them (xp-format, time-ago, level-title)
