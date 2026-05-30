# Requirements Document

## Introduction

This document defines the requirements for the Quest Completion UI flow in Ascend — a reusable orchestration layer that any screen (Dashboard, Quest Board, etc.) can invoke to deliver the full reward experience when a user completes a quest. The flow encompasses a themed confirmation bottom-sheet, hybrid reward animations (Lottie particle burst + Angular Animations glow), XP progress bar fill transitions, haptic feedback, silent duplicate handling, and a full-screen cinematic Perfect Day bonus overlay triggered when all daily quests are completed.

## Glossary

- **Quest_Completion_Service**: A shared Angular injectable service that orchestrates the entire quest completion flow — from confirmation through reward animations to Perfect Day detection — callable from any screen in the application
- **Confirmation_Sheet**: A themed bottom-sheet modal displaying quest details, XP preview, and a prominent "Complete Quest" action button, styled to match the RPG dark theme
- **Reward_Animation_Layer**: A full-screen overlay component that renders the XP particle burst (Lottie) and glow effect (Angular Animations) after a quest is successfully completed
- **XP_Particle_Burst**: A Lottie animation displaying particles radiating outward from the quest card position, lasting 300ms as specified in the animation system
- **Glow_Effect**: An Angular Animation that applies a pulsing glow border effect using the progression orange color (#FF9800) on the XP progress bar during XP gain
- **Progress_Bar_Fill**: An animated transition on the shared app-xp-progress-bar component that smoothly fills to the new XP value after quest completion
- **Perfect_Day_Overlay**: A full-screen cinematic overlay displayed when the user completes all daily quests, featuring animated stats, a "Perfect Day" title, and a dismiss button
- **Haptic_Feedback**: Device vibration patterns triggered via the existing HapticService during key moments of the completion flow
- **Quest_Completion_Response**: The backend response object returned by QuestService.completeQuest containing xpEarned, questId, questTitle, completedAt, and message fields
- **Duplicate_Completion**: A scenario where the frontend attempts to complete an already-completed quest, resulting in a 409 HTTP conflict response from the backend
- **Daily_Quest_Set**: The collection of all quests assigned to the user for the current day, used to determine Perfect Day eligibility

## Requirements

### Requirement 1: Quest Completion Service Orchestration

**User Story:** As a developer, I want a shared service that orchestrates the entire quest completion flow, so that any screen can trigger the same immersive reward experience without duplicating logic.

#### Acceptance Criteria

1. THE Quest_Completion_Service SHALL be implemented as a singleton Angular injectable service provided at the root level
2. THE Quest_Completion_Service SHALL expose a public method `completeQuest(quest: Quest)` that accepts a quest object and returns an Observable emitting the completion result
3. WHEN `completeQuest` is called, THE Quest_Completion_Service SHALL open the Confirmation_Sheet as the first step of the flow
4. THE Quest_Completion_Service SHALL coordinate the sequential execution of confirmation, API call, reward animations, progress bar update, and Perfect Day check
5. WHILE a completion flow is already in progress, THE Quest_Completion_Service SHALL ignore additional `completeQuest` calls until the current flow resolves
6. THE Quest_Completion_Service SHALL emit a completion event that calling components can subscribe to for updating their local state (e.g., removing the quest from a list)

### Requirement 2: Confirmation Bottom-Sheet

**User Story:** As a user, I want to see quest details and XP preview before confirming completion, so that I can verify I am completing the correct quest.

#### Acceptance Criteria

1. WHEN the Quest_Completion_Service initiates a completion flow, THE Confirmation_Sheet SHALL slide up from the bottom of the screen as a modal bottom-sheet
2. THE Confirmation_Sheet SHALL display the quest title, quest description, difficulty badge, stat type icon, and XP reward value
3. THE Confirmation_Sheet SHALL display a prominent "Complete Quest" button using the success green color (#4CAF50) with a minimum touch target of 44x44 CSS pixels
4. THE Confirmation_Sheet SHALL display a "Cancel" text button below the primary action that dismisses the sheet without completing the quest
5. THE Confirmation_Sheet SHALL apply the dark theme styling with card background (#161616), rounded top corners (16px border-radius), and a drag handle indicator at the top
6. WHEN the user swipes down on the Confirmation_Sheet, THE Confirmation_Sheet SHALL dismiss without completing the quest
7. WHEN the user taps the "Complete Quest" button, THE Confirmation_Sheet SHALL display a loading state on the button and call the QuestService.completeQuest API endpoint

### Requirement 3: Reward Animation Sequence

**User Story:** As a user, I want to see a celebratory animation when I complete a quest, so that I feel an immediate sense of accomplishment and reward.

#### Acceptance Criteria

1. WHEN the QuestService.completeQuest API returns a successful response, THE Reward_Animation_Layer SHALL render as a full-screen overlay above all other content
2. THE Reward_Animation_Layer SHALL play the XP_Particle_Burst Lottie animation with a duration of 300ms
3. THE Reward_Animation_Layer SHALL simultaneously trigger the Glow_Effect Angular Animation on the XP progress bar area with the progression orange color (#FF9800)
4. THE Reward_Animation_Layer SHALL display the earned XP value as a floating number (e.g., "+50 XP") that animates upward and fades out over 500ms
5. WHEN the XP_Particle_Burst animation completes, THE Reward_Animation_Layer SHALL dismiss itself automatically
6. THE Reward_Animation_Layer SHALL not block user interaction with the underlying screen after the animation completes
7. THE Reward_Animation_Layer SHALL use the mystic purple color (#A855F7) as a secondary accent in the particle burst animation

### Requirement 4: XP Progress Bar Fill Animation

**User Story:** As a user, I want to see my XP progress bar fill smoothly after completing a quest, so that I can visualize my progression toward the next level.

#### Acceptance Criteria

1. WHEN the reward animation sequence begins, THE Progress_Bar_Fill SHALL animate the shared app-xp-progress-bar from the previous XP value to the new XP value
2. THE Progress_Bar_Fill animation SHALL use a smooth ease-out timing function with a duration of 600ms
3. THE Progress_Bar_Fill SHALL use the accent-to-secondary gradient (#FF9800 to #A855F7) for the fill color
4. WHEN the new XP value exceeds the level threshold, THE Progress_Bar_Fill SHALL animate to 100%, briefly pause (200ms), then reset to 0% and fill to the overflow amount representing progress in the new level
5. THE Progress_Bar_Fill SHALL update the numeric XP display text (e.g., "2,400 / 3,000 XP") in sync with the bar fill animation using a counting-up effect

### Requirement 5: Haptic Feedback Integration

**User Story:** As a user, I want to feel tactile feedback during quest completion, so that the experience feels physical and satisfying on my mobile device.

#### Acceptance Criteria

1. WHEN the user taps the "Complete Quest" button in the Confirmation_Sheet, THE Quest_Completion_Service SHALL trigger a light impact haptic via HapticService
2. WHEN the reward animation begins playing, THE Quest_Completion_Service SHALL trigger a success notification haptic via HapticService
3. WHEN the Perfect_Day_Overlay appears, THE Quest_Completion_Service SHALL trigger a heavy impact haptic via HapticService
4. THE Quest_Completion_Service SHALL call haptic methods without awaiting their completion to avoid blocking the animation flow

### Requirement 6: Duplicate Completion Handling

**User Story:** As a user, I want the app to handle accidental double-taps gracefully, so that I never see confusing error messages when completing a quest.

#### Acceptance Criteria

1. IF the QuestService.completeQuest API returns a 409 conflict status, THEN THE Quest_Completion_Service SHALL dismiss the Confirmation_Sheet silently without displaying an error message to the user
2. IF a 409 conflict response is received, THEN THE Quest_Completion_Service SHALL mark the quest as completed in the local state and emit a completion event as if the completion succeeded
3. IF the QuestService.completeQuest API returns any error other than 409, THEN THE Quest_Completion_Service SHALL dismiss the loading state, display an error toast with the message "Could not complete quest. Try again.", and keep the Confirmation_Sheet open for retry
4. WHILE the Quest_Completion_Service is processing a completeQuest call for a specific quest, THE Quest_Completion_Service SHALL disable the "Complete Quest" button to prevent duplicate submissions

### Requirement 7: Perfect Day Bonus Overlay

**User Story:** As a user, I want to see a cinematic celebration when I complete all my daily quests, so that I feel a strong sense of achievement for finishing everything.

#### Acceptance Criteria

1. WHEN a quest completion results in all Daily_Quest_Set quests being marked as completed, THE Quest_Completion_Service SHALL trigger the Perfect_Day_Overlay after the reward animation sequence finishes
2. THE Perfect_Day_Overlay SHALL render as a full-screen overlay with the dark background (#0A0A0A) and a radial gradient glow effect using the progression orange (#FF9800) and mystic purple (#A855F7)
3. THE Perfect_Day_Overlay SHALL display a "PERFECT DAY" title using the display font (Orbitron) with a scale-in entrance animation
4. THE Perfect_Day_Overlay SHALL display animated stat counters showing total quests completed, total XP earned today, and current streak count
5. THE Perfect_Day_Overlay SHALL display a "Continue" dismiss button at the bottom of the screen with a minimum touch target of 44x44 CSS pixels
6. WHEN the user taps the "Continue" button, THE Perfect_Day_Overlay SHALL dismiss with a fade-out animation over 300ms
7. THE Perfect_Day_Overlay SHALL trigger a Lottie confetti animation in the background that loops until the overlay is dismissed
8. THE Perfect_Day_Overlay SHALL include appropriate ARIA labels and focus management for screen reader accessibility

### Requirement 8: Confirmation Sheet Accessibility

**User Story:** As a user with accessibility needs, I want the completion flow to be fully navigable with assistive technology, so that I can complete quests regardless of my abilities.

#### Acceptance Criteria

1. THE Confirmation_Sheet SHALL trap focus within the sheet while it is open, returning focus to the triggering element when dismissed
2. THE Confirmation_Sheet SHALL be dismissible via the Escape key on keyboard-enabled devices
3. THE Confirmation_Sheet SHALL include an aria-label of "Quest completion confirmation" on the sheet container
4. THE "Complete Quest" button SHALL include an aria-label that includes the quest title (e.g., "Complete quest: Morning Meditation")
5. THE Reward_Animation_Layer SHALL include aria-live="polite" announcements for the XP earned value so screen readers communicate the reward
6. THE Perfect_Day_Overlay SHALL move focus to the "Continue" button when it appears and announce "Perfect Day achieved" via aria-live

### Requirement 9: Animation Performance

**User Story:** As a user, I want animations to run smoothly without frame drops, so that the reward experience feels polished and premium.

#### Acceptance Criteria

1. THE Reward_Animation_Layer SHALL use CSS transform and opacity properties exclusively for animations to ensure GPU-accelerated rendering
2. THE XP_Particle_Burst Lottie animation SHALL be preloaded when the Quest_Completion_Service initializes to avoid loading delay during playback
3. THE Progress_Bar_Fill animation SHALL use CSS transitions on the width property with will-change hints to maintain 60fps performance
4. THE Perfect_Day_Overlay animations SHALL use Angular Animations with the Web Animations API renderer for hardware-accelerated transitions
5. WHEN animations complete, THE Reward_Animation_Layer SHALL remove itself from the DOM to free GPU memory and prevent layout interference

