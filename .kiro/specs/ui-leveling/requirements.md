# Requirements Document

## Introduction

This document defines the requirements for the Level-Up Celebration UI in Ascend — a full-screen overlay system that delivers an immersive, sequential celebration experience when a user levels up. The system includes a glow explosion animation, XP fly-up number, level rewards display, feature unlock announcements at milestone levels (10/25/50/100), and a prestige screen. The overlay is triggered by an independent LevelUpService that listens for level-up events from any source (quests, bosses, arcs) via API responses and WebSocket fallback.

## Glossary

- **LevelUp_Service**: A standalone Angular injectable service that listens for LevelUpEvent from any source and orchestrates the full-screen level-up celebration overlay independently of the quest completion flow
- **LevelUp_Event**: A domain event published by the xp-engine backend containing userId, previousLevel, newLevel, and unlockedFeatures, delivered via API response or WebSocket channel `/user/{userId}/queue/level`
- **Celebration_Overlay**: A full-screen overlay component that renders the sequential level-up celebration flow including glow explosion, rewards card, feature unlock announcement, and continue button
- **Glow_Explosion**: A Lottie animation displaying a radial glow burst effect lasting 1.5 seconds as the primary celebration visual when a level-up occurs
- **XP_FlyUp_Number**: An Angular Animation that displays the new level number animating upward with a fade-in effect over 200ms
- **Rewards_Card**: A slide-up panel within the Celebration_Overlay displaying the list of rewards earned for reaching the new level (coins, titles, cosmetics)
- **Feature_Unlock_Announcement**: A dedicated step in the celebration flow that announces newly unlocked features at milestone levels (Level 10: Leagues, Level 25: Guilds, Level 50: Elite cosmetics, Level 100: Prestige system)
- **Prestige_Screen**: A cinematic full-screen overlay displayed when a user reaches Level 100 and activates the prestige system, showing prestige level increment and prestige badge award
- **Prestige_System**: A game mechanic that resets the user level to 1, increments prestige_level, and awards a prestige badge when activated at Level 100
- **Milestone_Level**: A predefined level (10, 25, 50, 100) at which a major game feature is unlocked for the user
- **WebSocket_Level_Channel**: The private WebSocket channel `/user/{userId}/queue/level` that delivers LEVEL_UP events for level-ups triggered by non-API sources

## Requirements

### Requirement 1: LevelUp Service Event Listening

**User Story:** As a user, I want level-up celebrations to trigger regardless of what action caused the level-up, so that I always experience the full celebration whether it came from a quest, boss battle, or arc completion.

#### Acceptance Criteria

1. THE LevelUp_Service SHALL be implemented as a singleton Angular injectable service provided at the root level
2. THE LevelUp_Service SHALL subscribe to the WebSocket_Level_Channel on user authentication to receive LevelUp_Event messages from non-API sources
3. WHEN an API response from any endpoint includes a newLevel field indicating a level change, THE LevelUp_Service SHALL intercept the response and trigger the celebration flow
4. WHEN a LEVEL_UP message is received on the WebSocket_Level_Channel, THE LevelUp_Service SHALL trigger the celebration flow with the event data
5. WHILE a celebration flow is already in progress, THE LevelUp_Service SHALL queue subsequent LevelUp_Event messages and process them sequentially after the current celebration completes
6. THE LevelUp_Service SHALL expose a public signal `celebrationActive` that emits true while the overlay is displayed and false when dismissed
7. THE LevelUp_Service SHALL unsubscribe from the WebSocket_Level_Channel when the user logs out or the service is destroyed

### Requirement 2: Full-Screen Celebration Overlay

**User Story:** As a user, I want a dramatic full-screen celebration when I level up, so that I feel a powerful sense of achievement and progression.

#### Acceptance Criteria

1. WHEN the LevelUp_Service triggers a celebration, THE Celebration_Overlay SHALL render as a full-screen overlay above all other content including navigation elements
2. THE Celebration_Overlay SHALL use the dark background color (#0A0A0A) with full viewport coverage
3. THE Celebration_Overlay SHALL block interaction with underlying content while displayed
4. THE Celebration_Overlay SHALL execute the celebration steps sequentially: Glow_Explosion auto-plays (1.5s) → Rewards_Card slides up → Feature_Unlock_Announcement displays if applicable → Continue button appears
5. THE Celebration_Overlay SHALL include a "Continue Journey" button that dismisses the overlay and advances to the next queued celebration or returns to the previous screen
6. THE Celebration_Overlay SHALL apply a fade-in entrance animation over 200ms when appearing

### Requirement 3: Glow Explosion Animation

**User Story:** As a user, I want to see an explosive glow animation when I level up, so that the moment feels epic and memorable.

#### Acceptance Criteria

1. WHEN the Celebration_Overlay appears, THE Glow_Explosion SHALL auto-play as the first visual element of the celebration sequence
2. THE Glow_Explosion SHALL be rendered using a Lottie animation file with a total duration of 1500ms
3. THE Glow_Explosion SHALL use the progression orange (#FF9800) and mystic purple (#A855F7) as primary colors in the radial burst effect
4. THE Glow_Explosion SHALL originate from the center of the viewport and expand outward to fill the screen
5. WHEN the Glow_Explosion animation reaches 50% progress (750ms), THE Celebration_Overlay SHALL begin displaying the "LEVEL UP" title text with a scale-in animation
6. THE Glow_Explosion Lottie file SHALL be preloaded during LevelUp_Service initialization to prevent loading delay during playback

### Requirement 4: XP Fly-Up Number Animation

**User Story:** As a user, I want to see my new level number animate dramatically into view, so that I clearly understand what level I have reached.

#### Acceptance Criteria

1. WHEN the Glow_Explosion reaches 75% progress (1125ms), THE XP_FlyUp_Number SHALL begin its entrance animation
2. THE XP_FlyUp_Number SHALL display the text "Level {newLevel}" using the display font (Orbitron) in a large size
3. THE XP_FlyUp_Number SHALL animate upward by 40 pixels from its starting position with simultaneous opacity transition from 0 to 1 over 200ms
4. THE XP_FlyUp_Number SHALL use an ease-out timing function for the upward translation
5. THE XP_FlyUp_Number SHALL be implemented using Angular Animations with CSS transform and opacity properties for GPU-accelerated rendering
6. WHEN the XP_FlyUp_Number animation completes, THE XP_FlyUp_Number SHALL remain visible in its final position for the duration of the celebration

### Requirement 5: Level Rewards Display

**User Story:** As a user, I want to see what rewards I earned for leveling up, so that I feel the tangible value of my progression.

#### Acceptance Criteria

1. WHEN the Glow_Explosion animation completes (after 1500ms), THE Rewards_Card SHALL slide up from the bottom of the Celebration_Overlay with an ease-out animation over 400ms
2. THE Rewards_Card SHALL display a list of rewards received for the new level, including reward type icon, reward name, and reward amount or value
3. THE Rewards_Card SHALL support reward types including coins, titles, cosmetics, and XP multiplier tokens
4. THE Rewards_Card SHALL use the dark card background (#161616) with rounded corners (16px border-radius) and a subtle border using the progression orange (#FF9800) at 30% opacity
5. THE Rewards_Card SHALL display each reward item with a staggered fade-in animation, with each item appearing 100ms after the previous one
6. IF the LevelUp_Event contains an empty rewards list, THEN THE Rewards_Card SHALL display the text "Level {newLevel} Achieved" with the level badge component instead of a rewards list

### Requirement 6: Feature Unlock Announcement

**User Story:** As a user, I want to be informed when I unlock a new game feature at a milestone level, so that I know about new capabilities available to me.

#### Acceptance Criteria

1. WHEN the newLevel in the LevelUp_Event equals a Milestone_Level (10, 25, 50, or 100), THE Feature_Unlock_Announcement SHALL display as a dedicated step after the Rewards_Card
2. THE Feature_Unlock_Announcement SHALL display the unlocked feature name, a descriptive tagline, and a feature icon
3. WHEN Level 10 is reached, THE Feature_Unlock_Announcement SHALL announce "Leagues Unlocked" with the description "Compete with players at your level"
4. WHEN Level 25 is reached, THE Feature_Unlock_Announcement SHALL announce "Guilds Unlocked" with the description "Join forces with other players"
5. WHEN Level 50 is reached, THE Feature_Unlock_Announcement SHALL announce "Elite Cosmetics Unlocked" with the description "Exclusive visual upgrades await"
6. WHEN Level 100 is reached, THE Feature_Unlock_Announcement SHALL announce "Prestige System Unlocked" with the description "Reset and ascend to legendary status"
7. THE Feature_Unlock_Announcement SHALL use a scale-in entrance animation over 300ms with the mystic purple (#A855F7) as the accent color for the feature icon glow
8. IF the newLevel does not equal a Milestone_Level, THEN THE Celebration_Overlay SHALL skip the Feature_Unlock_Announcement step and proceed directly to the Continue button

### Requirement 7: Prestige Screen

**User Story:** As a user who has reached Level 100, I want a unique cinematic prestige experience, so that I feel the gravity of resetting my level and ascending to a higher tier.

#### Acceptance Criteria

1. WHEN the user activates the Prestige_System from the Feature_Unlock_Announcement at Level 100, THE Prestige_Screen SHALL replace the Celebration_Overlay with a dedicated cinematic sequence
2. THE Prestige_Screen SHALL display the current prestige_level incrementing from the previous value to the new value with a counting animation over 800ms
3. THE Prestige_Screen SHALL display the prestige badge earned for the new prestige level with a reveal animation using the mystic purple (#A855F7) glow effect
4. THE Prestige_Screen SHALL display the text "Prestige {prestige_level}" using the display font (Orbitron) with a gold gradient color (#FFD700 to #FF9800)
5. THE Prestige_Screen SHALL display a summary showing "Level reset to 1" and "Prestige Badge Earned" as confirmation of the prestige effects
6. THE Prestige_Screen SHALL include a "Begin New Journey" button that dismisses the screen and navigates the user back to the dashboard
7. THE Prestige_Screen SHALL play a unique Lottie animation featuring ascending particle trails and a badge materialization effect with a total duration of 2000ms
8. IF the user dismisses the Feature_Unlock_Announcement at Level 100 without activating prestige, THEN THE Celebration_Overlay SHALL dismiss normally and the user can activate prestige later from the profile screen

### Requirement 8: Continue Button and Flow Control

**User Story:** As a user, I want clear control over when to dismiss the celebration, so that I can enjoy the moment at my own pace without feeling rushed.

#### Acceptance Criteria

1. WHEN all celebration steps have completed (Glow_Explosion + Rewards_Card + Feature_Unlock_Announcement if applicable), THE Celebration_Overlay SHALL display the "Continue Journey" button
2. THE "Continue Journey" button SHALL use the success green color (#4CAF50) with a minimum touch target of 44x44 CSS pixels
3. THE "Continue Journey" button SHALL appear with a fade-in animation over 200ms
4. WHEN the user taps the "Continue Journey" button, THE Celebration_Overlay SHALL dismiss with a fade-out animation over 300ms
5. WHEN the Celebration_Overlay dismisses and the LevelUp_Service has queued celebrations, THE LevelUp_Service SHALL trigger the next queued celebration after a 500ms delay
6. THE "Continue Journey" button SHALL be the only interactive element within the Celebration_Overlay during the celebration flow

### Requirement 9: Celebration Overlay Accessibility

**User Story:** As a user with accessibility needs, I want the level-up celebration to be navigable with assistive technology, so that I can experience the celebration regardless of my abilities.

#### Acceptance Criteria

1. THE Celebration_Overlay SHALL trap focus within the overlay while it is displayed, returning focus to the previously focused element when dismissed
2. THE Celebration_Overlay SHALL include an aria-label of "Level up celebration" on the overlay container
3. THE Celebration_Overlay SHALL announce "Level up! You reached level {newLevel}" via aria-live="assertive" when the overlay appears
4. THE Rewards_Card SHALL include an aria-label listing all rewards earned (e.g., "Rewards: 100 coins, Focused Mind title")
5. THE Feature_Unlock_Announcement SHALL announce the unlocked feature via aria-live="polite" (e.g., "New feature unlocked: Leagues")
6. THE "Continue Journey" button SHALL receive focus automatically when it appears and include an aria-label of "Continue Journey, dismiss level up celebration"
7. THE Celebration_Overlay SHALL be dismissible via the Escape key on keyboard-enabled devices

### Requirement 10: Animation Performance and Resource Management

**User Story:** As a user, I want the level-up celebration to run smoothly without frame drops or loading delays, so that the experience feels polished and premium.

#### Acceptance Criteria

1. THE Glow_Explosion Lottie animation SHALL be preloaded into memory when the LevelUp_Service initializes to eliminate loading delay during playback
2. THE XP_FlyUp_Number and Rewards_Card animations SHALL use CSS transform and opacity properties exclusively to ensure GPU-accelerated rendering at 60fps
3. THE Celebration_Overlay SHALL use Angular Animations with the Web Animations API renderer for hardware-accelerated transitions
4. WHEN the Celebration_Overlay is dismissed, THE Celebration_Overlay SHALL remove itself from the DOM to free GPU memory and prevent layout interference
5. THE Prestige_Screen Lottie animation SHALL be loaded on-demand only when Level 100 is reached, rather than preloaded at service initialization
6. THE Celebration_Overlay SHALL use a CSS will-change hint on animated elements during the celebration and remove the hint after animations complete

### Requirement 11: Deduplication and Conflict Prevention

**User Story:** As a user, I want to see exactly one celebration per level-up event, so that I never experience duplicate or missing celebrations.

#### Acceptance Criteria

1. THE LevelUp_Service SHALL maintain a record of the last processed level to prevent duplicate celebrations from both API response and WebSocket delivering the same event
2. WHEN a LevelUp_Event is received with a newLevel equal to or less than the last processed level, THE LevelUp_Service SHALL discard the event without triggering a celebration
3. THE LevelUp_Service SHALL coordinate with the Quest_Completion_Service to ensure the quest completion reward animation (300ms particle burst) completes before the level-up celebration begins
4. WHEN a level-up is triggered by a quest completion API response, THE LevelUp_Service SHALL wait for the Quest_Completion_Service reward animation to finish before displaying the Celebration_Overlay
5. THE LevelUp_Service SHALL persist the last processed level in local storage to prevent duplicate celebrations across page refreshes

### Requirement 12: Multi-Level Jump Handling

**User Story:** As a user who gains enough XP to skip multiple levels at once (e.g., from a boss battle), I want to see celebrations for each level gained, so that I do not miss any rewards or feature unlocks.

#### Acceptance Criteria

1. WHEN a LevelUp_Event indicates a jump of more than one level (newLevel - previousLevel > 1), THE LevelUp_Service SHALL generate individual celebration events for each intermediate level
2. THE LevelUp_Service SHALL process multi-level celebrations sequentially, displaying one celebration per level gained in ascending order
3. WHEN processing multi-level celebrations, THE LevelUp_Service SHALL include the correct rewards for each individual level in the respective Rewards_Card
4. WHEN a multi-level jump crosses a Milestone_Level, THE LevelUp_Service SHALL include the Feature_Unlock_Announcement in the celebration for that specific milestone level
5. THE LevelUp_Service SHALL display a summary indicator (e.g., "1 of 3") on the Celebration_Overlay when multiple celebrations are queued from a multi-level jump
