# Implementation Plan: Analytics & Insights

## Overview

Progress dashboard, Life Score calculation, weekly reports, activity heatmaps, and habit correlation detection.

## Tasks

- [x] 1. Create Analytics DTOs
  - [x] 1.1 Create response models
    - Create `DashboardResponse.java`: xpGrowth (list), levelGrowth, questCompletionRate, streakHistory (list), statTrends (map)
    - Create `WeeklyReportResponse.java`: questsCompleted, questsMissed, xpEarned, strongestStat, weakestStat, recommendations (list), lifeScore
    - Create `HeatmapResponse.java`: data (list of date → completionCount), startDate, endDate
    - Create `InsightResponse.java`: message, confidence, category, actionable

- [x] 2. Implement Analytics Service
  - [x] 2.1 Create AnalyticsService
    - Create `AnalyticsService.java` in `analytics/service/`
    - `getDashboard(UUID userId)`:
      1. Aggregate XP growth over last 30 days
      2. Calculate quest completion rate (completed / assigned)
      3. Get streak history
      4. Get stat trends (weekly averages)
    - `getHeatmap(UUID userId, int days)`:
      1. Query quest_completion grouped by date
      2. Return date → count map for heatmap visualization
  - [x] 2.2 Create WeeklyReportService
    - Create `WeeklyReportService.java` in `analytics/service/`
    - `generateWeeklyReport(UUID userId)`:
      1. Count quests completed this week
      2. Count quests missed
      3. Sum XP earned
      4. Identify strongest stat (highest gain this week)
      5. Identify weakest stat (lowest gain)
      6. Generate 1-3 recommendations based on patterns
      7. Calculate current Life Score
    - `WeeklyReportScheduler` — run every Sunday, generate for all active users

- [x] 3. Implement Life Score
  - [x] 3.1 Create LifeScoreService
    - Create `LifeScoreService.java` in `analytics/service/`
    - `calculateLifeScore(UUID userId)`:
      1. Fetch user_stats
      2. Fetch streak data for consistency metric
      3. Apply formula: 0.25×Discipline + 0.2×Focus + 0.2×Vitality + 0.2×Wisdom + 0.15×Consistency
      4. Normalize to 0-100 scale
      5. Update user_stats.life_score
    - Called after stat changes and in weekly report

- [x] 4. Implement Correlation Detection
  - [x] 4.1 Create CorrelationService (simplified)
    - Create `CorrelationService.java` in `analytics/service/`
    - `detectCorrelations(UUID userId)`:
      1. Analyze quest completion times vs stat gains
      2. Simple rule-based insights (not ML):
         - "You complete more quests on weekdays"
         - "Morning completions correlate with higher Focus gains"
      3. Return list of InsightResponse
    - Premium feature only

- [x] 5. Create Analytics Controller
  - [x] 5.1 Implement REST endpoints
    - Create `AnalyticsController.java` in `analytics/controller/`
    - GET `/api/v1/analytics/dashboard` — full dashboard data
    - GET `/api/v1/analytics/weekly` — latest weekly report
    - GET `/api/v1/analytics/heatmap?days={days}` — activity heatmap
    - GET `/api/v1/analytics/life-score` — current life score
    - GET `/api/v1/analytics/insights` — habit correlations (premium)

- [x] 6. Write property-based tests
  - [x] 6.1 Create analytics property tests
    - Create `LifeScorePropertyTest.java`:
      - Property 48: Life Score formula correctness, always in [0, 100]
    - Create `WeeklyReportPropertyTest.java`:
      - Property 49: Weekly report contains all required fields
    - Minimum 100 iterations per property

- [x] 7. Checkpoint - Verify analytics
  - Integration test: complete quests over multiple days → dashboard shows trends
  - Integration test: Sunday → weekly report generated with correct stats
  - Unit test: Life Score calculation for various stat combinations
  - Property tests all pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- **RULE: Do NOT run any mvn, gradle, or test commands. Only create/edit files. No build or test verification steps.**
- Dashboard data is aggregated from multiple tables — consider caching
- Weekly reports are generated asynchronously (scheduler)
- Correlation detection is rule-based for MVP
- Life Score updates on every stat change
- Heatmap data is similar to GitHub contribution graph

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1"] },
    { "id": 1, "tasks": ["2.1", "2.2", "3.1"] },
    { "id": 2, "tasks": ["4.1"] },
    { "id": 3, "tasks": ["5.1"] },
    { "id": 4, "tasks": ["6.1"] },
    { "id": 5, "tasks": ["7"] }
  ]
}
```
