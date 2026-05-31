# Profile Tab — Feature Spec

## Overview
The Profile tab (Screen 11 per SRS) displays the user's RPG identity — avatar, level, stats radar chart, achievements, identity titles, and activity history. It should feel like a character sheet in an RPG.

## Backend Endpoints Available
- `GET /api/v1/profile/{userId}` — public profile (avatar, username, level, league, prestige, stats, achievement count)
- `PUT /api/v1/profile` — update own profile (username, avatarUrl, timezone, privacyLevel)
- `GET /api/v1/profile/achievements` — list of unlocked achievements
- `GET /api/v1/stats` — full stat breakdown (strength, wisdom, focus, discipline, vitality, charisma, lifeScore)
- `GET /api/v1/stats/radar` — radar chart data
- `GET /api/v1/stats/titles` — unlocked identity titles
- `GET /api/v1/auth/me` — current user info (level, xp, league, premium)
- `GET /api/v1/streak` — streak info (currentStreak, longestStreak, comboMultiplier)

## Design (from SRS UI/UX Blueprint)
- Dark theme (#0A0A0A background, #161616 cards)
- Primary accent: #FF9800 (orange)
- Sections stacked vertically, single-column mobile layout
- Skeleton loaders while fetching

---

## Requirements

### REQ-1: Profile Header Card
- Display avatar (circular, 80px), username, level badge, league badge, prestige stars
- "Edit Profile" button opens edit modal
- If premium user, show premium badge/glow

### REQ-2: Stats Radar Chart
- 6-axis radar chart: Strength, Wisdom, Focus, Discipline, Vitality, Charisma
- Use the existing `<app-stat-radar>` shared UI component
- Fetch data from `GET /api/v1/stats/radar`
- Show Life Score below the chart

### REQ-3: Achievements Section
- Scrollable horizontal list of achievement cards
- Each card: icon/badge, name, unlock date
- "View All" link if > 5 achievements
- Fetch from `GET /api/v1/profile/achievements`
- Empty state: "Complete quests to unlock achievements"

### REQ-4: Identity Titles Section
- List of earned identity titles (e.g., "The Focused One", "Iron Discipline")
- Active/equipped title highlighted with orange glow
- Fetch from `GET /api/v1/stats/titles`
- Empty state: "Reach stat milestones to earn titles"

### REQ-5: Activity Summary
- Current streak (with flame icon)
- Longest streak
- Total quests completed (derive from stats or add endpoint)
- Member since date
- Fetch streak from `GET /api/v1/streak`

### REQ-6: Edit Profile Modal
- Fields: username, avatar URL (or avatar picker), timezone, privacy level
- Save calls `PUT /api/v1/profile`
- Validation: username 1-50 chars, required
- Toast on success/error

### REQ-7: Settings Quick Links
- Notification preferences
- Privacy level toggle (Public / Friends Only / Private)
- Logout button
- Hard Mode toggle (if premium)

---

## Component Architecture

```
features/profile/
├── pages/
│   └── profile-page/
│       ├── profile-page.component.ts
│       ├── profile-page.component.html
│       └── profile-page.component.scss
├── components/
│   ├── profile-header/
│   │   └── profile-header.component.ts
│   ├── stats-section/
│   │   └── stats-section.component.ts
│   ├── achievements-section/
│   │   └── achievements-section.component.ts
│   ├── titles-section/
│   │   └── titles-section.component.ts
│   ├── activity-summary/
│   │   └── activity-summary.component.ts
│   └── edit-profile-modal/
│       └── edit-profile-modal.component.ts
├── services/
│   └── profile.service.ts
└── profile.routes.ts
```

---

## Tasks

### Task 1: Profile Service
Create `profile.service.ts` that calls:
- `GET /api/v1/auth/me` → user info
- `GET /api/v1/stats/radar` → radar data
- `GET /api/v1/profile/achievements` → achievements list
- `GET /api/v1/stats/titles` → identity titles
- `GET /api/v1/streak` → streak data
- `PUT /api/v1/profile` → update profile

### Task 2: Profile Header Component
- Circular avatar with level badge overlay
- Username, league badge, prestige indicator
- Edit button (pencil icon)
- Premium glow if applicable

### Task 3: Stats Radar Section
- Wrap existing `<app-stat-radar>` component
- Display Life Score as a large number below
- Skeleton loader while fetching

### Task 4: Achievements Section
- Horizontal scroll list using `ion-slides` or CSS scroll-snap
- Achievement card: badge icon, name, date
- "View All" navigation (future: dedicated achievements page)

### Task 5: Titles Section
- Vertical list of earned titles
- Active title has orange highlight
- Tap to equip (future: PUT endpoint)

### Task 6: Activity Summary
- Mini cards: Current Streak, Longest Streak, Member Since
- Use streak flame icon for streak display

### Task 7: Edit Profile Modal
- `ion-modal` with form fields
- Username input, avatar URL input, timezone select, privacy toggle
- Save/Cancel buttons
- Calls `PUT /api/v1/profile`

### Task 8: Profile Page (Smart Component)
- Orchestrates all child components
- Fetches data on init via ProfileService
- Pull-to-refresh support
- Skeleton loading per section
- Error states with retry

### Task 9: Settings Section
- Logout button (calls `authService.logout()`)
- Privacy level display
- Link to notification preferences (future)

### Task 10: Route Update
- Update `profile.routes.ts` to load the new profile page component

---

## Acceptance Criteria
- [ ] Profile page loads with all sections populated from real API data
- [ ] Stats radar chart renders 6 axes correctly
- [ ] Achievements display with proper empty state
- [ ] Identity titles show with active title highlighted
- [ ] Edit profile modal saves changes and reflects immediately
- [ ] Logout works and redirects to welcome screen
- [ ] Pull-to-refresh reloads all sections
- [ ] Skeleton loaders shown during fetch
- [ ] No console errors
- [ ] Accessible (ARIA labels, keyboard navigation)
