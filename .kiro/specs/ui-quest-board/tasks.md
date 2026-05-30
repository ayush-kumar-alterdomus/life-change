# Implementation Plan: UI Quest Board

## Overview

Replace the existing quest-board page with a fresh implementation featuring tab-based quest organization (Daily/Weekly/Custom), a bottom-sheet quest detail modal, an enhanced custom quest creation form, a progress summary card, and a free-user 5-quest limit gate. The implementation uses Angular signals, the existing shared `app-quest-card` component, and the existing `QuestService`.

## Tasks

- [ ] 1. Set up pure logic functions and data models
  - [x] 1.1 Create pure logic utility file with all business logic functions
    - ⚠️ **Do not run any commands — only create files.**
    - Create `src/features/quests/utils/quest-board.utils.ts`
    - Implement `filterQuestsByFrequency`, `computeProgress`, `calculateXpFromDifficulty`, `canUserCreateQuest`, `countActiveCustomQuests`, `validateCreateQuestForm`
    - Export `ProgressSummary`, `ValidationResult`, and `CreateQuestPayload` interfaces
    - _Requirements: 1.2, 1.3, 1.4, 4.3, 4.5, 5.2, 5.3, 5.5, 6.1_

  - [ ]* 1.2 Write property tests for filterQuestsByFrequency
    - ⚠️ **Do not run any commands — only create files.**
    - **Property 1: Tab frequency filter returns only matching quests**
    - **Validates: Requirements 1.2, 1.3, 1.4**

  - [ ]* 1.3 Write property tests for computeProgress
    - ⚠️ **Do not run any commands — only create files.**
    - **Property 7: Progress computation correctness**
    - **Validates: Requirements 6.1, 6.2**

  - [ ]* 1.4 Write property tests for calculateXpFromDifficulty
    - ⚠️ **Do not run any commands — only create files.**
    - **Property 4: XP reward calculation from difficulty**
    - **Validates: Requirements 4.3**

  - [ ]* 1.5 Write property tests for canUserCreateQuest and countActiveCustomQuests
    - ⚠️ **Do not run any commands — only create files.**
    - **Property 6: Quest limit gate logic**
    - **Property 8: Active custom quest count accuracy**
    - **Validates: Requirements 5.2, 5.3, 5.5**

  - [ ]* 1.6 Write property tests for validateCreateQuestForm
    - ⚠️ **Do not run any commands — only create files.**
    - **Property 5: Form validation reports errors for exactly the missing required fields**
    - **Validates: Requirements 4.5**

- [x] 2. Extend QuestService with new API methods
  - [x] 2.1 Add getAllQuests, getWeeklyQuests, and getCustomQuests methods to QuestService
    - ⚠️ **Do not run any commands — only create files.**
    - Add `getAllQuests(): Observable<Quest[]>` method to fetch all quests
    - Add `getWeeklyQuests(): Observable<Quest[]>` method
    - Add `getCustomQuests(): Observable<Quest[]>` method
    - Update the `Quest` interface to include `timeEstimate` and `frequency` fields if missing
    - _Requirements: 1.2, 1.3, 1.4, 7.1_

- [x] 3. Implement dumb UI components
  - [x] 3.1 Create ProgressSummaryComponent
    - ⚠️ **Do not run any commands — only create files.**
    - Create `src/features/quests/components/progress-summary/progress-summary.component.ts` and template
    - Accept `completed` and `total` inputs, compute `ratio`
    - Render progress bar using `ion-progress-bar` and display "X / Y quests completed"
    - _Requirements: 6.1, 6.2_

  - [x] 3.2 Create QuestListComponent
    - ⚠️ **Do not run any commands — only create files.**
    - Create `src/features/quests/components/quest-list/quest-list.component.ts` and template
    - Accept `quests` input array and `emptyMessage` input
    - Render each quest using the shared `app-quest-card` component
    - Emit `questTapped` output when a card is tapped
    - Display empty state with contextual message when quests array is empty
    - _Requirements: 2.1, 2.2, 2.3, 2.5_

  - [x] 3.3 Create QuestDetailSheetComponent
    - ⚠️ **Do not run any commands — only create files.**
    - Create `src/features/quests/components/quest-detail-sheet/quest-detail-sheet.component.ts` and template
    - Accept `quest` and `isOpen` inputs
    - Display quest title, description, XP reward with formula, difficulty, stat type, time estimate, frequency
    - Render action bar with Complete, Edit, Skip buttons for incomplete quests
    - Hide action buttons and show completion status for completed quests
    - Emit `complete`, `edit`, `skip`, and `dismissed` outputs
    - _Requirements: 3.1, 3.2, 3.3, 3.5, 3.6_

  - [x] 3.4 Create CreateQuestFormComponent
    - ⚠️ **Do not run any commands — only create files.**
    - Create `src/features/quests/components/create-quest-form/create-quest-form.component.ts` and template
    - Build reactive form with title, description, difficulty, statType, frequency, timeEstimate fields
    - Implement XP calculation display based on selected difficulty using `calculateXpFromDifficulty`
    - Show inline validation errors for required fields on submit
    - Emit `created` output with `CreateQuestPayload` on valid submission, `dismissed` on close
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

  - [x] 3.5 Create UpgradePromptComponent
    - ⚠️ **Do not run any commands — only create files.**
    - Create `src/features/quests/components/upgrade-prompt/upgrade-prompt.component.ts` and template
    - Accept `isOpen`, `activeQuestCount`, and `maxQuests` inputs
    - Display message about reaching the 5-quest free-tier limit
    - Provide upgrade call-to-action button and dismiss option
    - Emit `upgrade` and `dismissed` outputs
    - _Requirements: 5.3, 5.4_

- [x] 4. Checkpoint - Verify component compilation
  - ⚠️ **Do not run any commands — only create files.** Simply verify that all created files have no syntax errors by reviewing them.
  - Ensure all files are syntactically correct, ask the user if questions arise.

- [x] 5. Implement QuestBoardPage smart component
  - [x] 5.1 Create the new QuestBoardPage component with signal-based state
    - ⚠️ **Do not run any commands — only create files.**
    - Create `src/features/quests/pages/quest-board/quest-board.page.ts`, template, and styles
    - Set up signal state: `allQuests`, `activeTab`, `loading`, `selectedQuest`, `showCreateForm`, `showUpgradePrompt`
    - Set up computed signals: `filteredQuests`, `progressSummary`, `canCreateQuest`, `activeCustomQuestCount`
    - Inject `QuestService` and `UserStore`, load all quests on init
    - Default `activeTab` to `QuestFrequency.Daily`
    - _Requirements: 1.1, 1.5, 2.4_

  - [x] 5.2 Implement tab switching and quest list rendering in QuestBoardPage template
    - ⚠️ **Do not run any commands — only create files.**
    - Add `ion-segment` with Daily/Weekly/Custom segment buttons bound to `activeTab` signal
    - Render `app-progress-summary` with computed progress data
    - Render `app-quest-list` with `filteredQuests` computed signal
    - Show loading skeleton while `loading` signal is true
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 2.1, 2.4, 6.1, 6.2_

  - [x] 5.3 Wire quest detail bottom sheet in QuestBoardPage
    - ⚠️ **Do not run any commands — only create files.**
    - Add `ion-modal` with breakpoints `[0, 0.5, 0.75]` and `initialBreakpoint` of `0.5`
    - Bind modal open state to `selectedQuest() !== null`
    - Handle `questTapped` from QuestListComponent to set `selectedQuest`
    - Handle `complete` event: call `QuestService.completeQuest()`, update `allQuests` signal, show success toast
    - Handle `dismissed` event: clear `selectedQuest` signal
    - _Requirements: 3.1, 3.4, 3.5, 6.3_

  - [x] 5.4 Wire FAB button with quest limit gate logic
    - ⚠️ **Do not run any commands — only create files.**
    - Add floating action button that checks `canCreateQuest` computed signal on tap
    - If `canCreateQuest` is true: set `showCreateForm` to true
    - If `canCreateQuest` is false: set `showUpgradePrompt` to true
    - Handle `created` event from CreateQuestFormComponent: call `QuestService.createQuest()`, refresh quest list, show success toast
    - Handle `dismissed` events from both form and upgrade prompt
    - _Requirements: 4.1, 4.4, 4.6, 5.2, 5.3, 5.5_

  - [x] 5.5 Implement pull-to-refresh in QuestBoardPage
    - ⚠️ **Do not run any commands — only create files.**
    - Add `ion-refresher` with `ionRefresh` handler
    - Fetch fresh quest data, update `allQuests` signal, preserve `activeTab` state
    - Show error toast on failure, retain previously loaded data
    - Call `event.target.complete()` when done
    - _Requirements: 7.1, 7.2, 7.3, 1.6_

- [ ] 6. Update routing and remove old component
  - [-] 6.1 Update quests.routes.ts to point to new QuestBoardPage
    - ⚠️ **Do not run any commands — only create files.**
    - Update the route to lazy-load `quest-board.page.ts` from the new path
    - Remove or archive the old `quest-board.component.ts`, `.html`, `.scss` files
    - _Requirements: 1.1_

- [~] 7. Checkpoint - Full integration verification
  - ⚠️ **Do not run any commands — only create files.** Simply verify that all created files are consistent and correctly wired together.
  - Ensure all files are syntactically correct and properly integrated, ask the user if questions arise.

- [ ]* 8. Write unit tests for components
  - [ ]* 8.1 Write unit tests for QuestBoardPage
    - ⚠️ **Do not run any commands — only create files.**
    - Test tab bar renders 3 tabs, default tab is Daily
    - Test FAB opens create form when under limit, shows upgrade prompt at limit
    - Test quest card tap opens detail sheet
    - Test pull-to-refresh preserves tab state
    - _Requirements: 1.1, 1.5, 1.6, 5.3_

  - [ ]* 8.2 Write unit tests for QuestDetailSheetComponent
    - ⚠️ **Do not run any commands — only create files.**
    - Test all quest fields are displayed
    - Test action buttons hidden for completed quests
    - Test dismiss behavior
    - _Requirements: 3.2, 3.3, 3.6_

  - [ ]* 8.3 Write unit tests for CreateQuestFormComponent
    - ⚠️ **Do not run any commands — only create files.**
    - Test form validation shows inline errors
    - Test XP calculation updates on difficulty change
    - Test successful submission emits payload
    - _Requirements: 4.2, 4.3, 4.5_

## Notes

- ⚠️ **GLOBAL CONSTRAINT: Do not run any commands (no npm, no ng, no shell commands) — only create files.**
- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties from the design document
- The implementation uses TypeScript with Angular 17+ signals and Ionic 7 components
- The existing shared `app-quest-card` component is reused without modification
- The existing `QuestService` is extended (not replaced) with new methods

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "2.1"] },
    { "id": 1, "tasks": ["1.2", "1.3", "1.4", "1.5", "1.6", "3.1", "3.2", "3.3", "3.4", "3.5"] },
    { "id": 2, "tasks": ["5.1"] },
    { "id": 3, "tasks": ["5.2", "5.3", "5.4", "5.5"] },
    { "id": 4, "tasks": ["6.1"] },
    { "id": 5, "tasks": ["8.1", "8.2", "8.3"] }
  ]
}
```
