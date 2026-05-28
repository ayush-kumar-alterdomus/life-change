# 16 — Folder Structure

# Ascend — Enter Arc Mode

---

## Angular Ionic Frontend Structure

```
src/
│
├── app/
│   ├── app.component.ts
│   ├── app.config.ts
│   └── app.routes.ts
│
├── core/
│   │
│   ├── auth/
│   │   ├── firebase-auth.service.ts
│   │   ├── auth.guard.ts
│   │   ├── auth.interceptor.ts
│   │   ├── premium.guard.ts
│   │   └── onboarding.guard.ts
│   │
│   ├── interceptors/
│   │   ├── error.interceptor.ts
│   │   ├── loading.interceptor.ts
│   │   └── retry.interceptor.ts
│   │
│   ├── services/
│   │   ├── api.service.ts
│   │   ├── storage.service.ts
│   │   ├── notification.service.ts
│   │   ├── theme.service.ts
│   │   └── connectivity.service.ts
│   │
│   ├── config/
│   │   ├── app.config.ts
│   │   └── firebase.config.ts
│   │
│   ├── constants/
│   │   ├── api-endpoints.ts
│   │   ├── app-constants.ts
│   │   └── game-constants.ts
│   │
│   ├── utilities/
│   │   ├── xp-calculator.util.ts
│   │   ├── level-calculator.util.ts
│   │   ├── date.util.ts
│   │   └── format.util.ts
│   │
│   └── animations/
│       ├── page-transitions.ts
│       ├── xp-animation.ts
│       └── level-up-animation.ts
│
├── shared/
│   │
│   ├── components/
│   │   ├── app-button/
│   │   ├── app-card/
│   │   ├── app-modal/
│   │   ├── app-toast/
│   │   ├── app-loader/
│   │   ├── app-badge/
│   │   ├── app-progress/
│   │   └── app-dialog/
│   │
│   ├── ui/
│   │   ├── xp-progress-bar/
│   │   ├── level-badge/
│   │   ├── streak-flame/
│   │   ├── reward-popup/
│   │   ├── quest-card/
│   │   ├── arc-card/
│   │   ├── boss-card/
│   │   ├── achievement-card/
│   │   ├── guild-card/
│   │   ├── leaderboard-card/
│   │   └── stat-radar/
│   │
│   ├── directives/
│   │   ├── long-press.directive.ts
│   │   ├── swipe.directive.ts
│   │   └── animate-on-view.directive.ts
│   │
│   ├── pipes/
│   │   ├── time-ago.pipe.ts
│   │   ├── xp-format.pipe.ts
│   │   └── level-title.pipe.ts
│   │
│   ├── models/
│   │   ├── user.model.ts
│   │   ├── quest.model.ts
│   │   ├── arc.model.ts
│   │   ├── guild.model.ts
│   │   ├── streak.model.ts
│   │   ├── achievement.model.ts
│   │   ├── boss.model.ts
│   │   └── notification.model.ts
│   │
│   ├── enums/
│   │   ├── difficulty.enum.ts
│   │   ├── stat-type.enum.ts
│   │   ├── league.enum.ts
│   │   ├── arc-type.enum.ts
│   │   └── quest-frequency.enum.ts
│   │
│   └── validators/
│       ├── quest.validator.ts
│       └── profile.validator.ts
│
├── features/
│   │
│   ├── auth/
│   │   ├── login/
│   │   │   ├── login.page.ts
│   │   │   ├── login.page.html
│   │   │   └── login.page.scss
│   │   ├── signup/
│   │   │   ├── signup.page.ts
│   │   │   ├── signup.page.html
│   │   │   └── signup.page.scss
│   │   ├── forgot-password/
│   │   └── services/
│   │       └── auth.service.ts
│   │
│   ├── onboarding/
│   │   ├── welcome/
│   │   ├── goal-selection/
│   │   ├── difficulty-selection/
│   │   ├── assessment/
│   │   ├── arc-recommendation/
│   │   ├── avatar-selection/
│   │   └── services/
│   │       └── onboarding.service.ts
│   │
│   ├── dashboard/
│   │   ├── dashboard.page.ts
│   │   ├── dashboard.page.html
│   │   ├── dashboard.page.scss
│   │   ├── components/
│   │   │   ├── header/
│   │   │   ├── xp-card/
│   │   │   ├── streak-card/
│   │   │   ├── stats-overview/
│   │   │   ├── active-arc/
│   │   │   ├── quest-list/
│   │   │   ├── ai-widget/
│   │   │   └── leaderboard-preview/
│   │   └── services/
│   │       └── dashboard.service.ts
│   │
│   ├── quests/
│   │   ├── pages/
│   │   │   ├── quest-board/
│   │   │   ├── quest-detail/
│   │   │   ├── quest-create/
│   │   │   └── quest-complete/
│   │   ├── components/
│   │   │   ├── daily-tab/
│   │   │   ├── weekly-tab/
│   │   │   └── custom-tab/
│   │   ├── services/
│   │   │   └── quest.service.ts
│   │   ├── state/
│   │   │   └── quest.store.ts
│   │   └── routes.ts
│   │
│   ├── arc-mode/
│   │   ├── pages/
│   │   │   ├── arc-list/
│   │   │   ├── arc-detail/
│   │   │   └── arc-progress/
│   │   ├── components/
│   │   │   ├── arc-banner/
│   │   │   ├── milestone-timeline/
│   │   │   ├── skill-tree-preview/
│   │   │   ├── boss-card/
│   │   │   └── rewards-section/
│   │   ├── services/
│   │   │   └── arc.service.ts
│   │   ├── state/
│   │   │   └── arc.store.ts
│   │   └── routes.ts
│   │
│   ├── leveling/
│   │   ├── pages/
│   │   │   └── level-up/
│   │   ├── components/
│   │   │   ├── xp-animation/
│   │   │   └── level-rewards/
│   │   └── services/
│   │       └── xp.service.ts
│   │
│   ├── streaks/
│   │   ├── pages/
│   │   │   └── streak-detail/
│   │   ├── components/
│   │   │   ├── streak-counter/
│   │   │   ├── combo-display/
│   │   │   └── comeback-modal/
│   │   └── services/
│   │       └── streak.service.ts
│   │
│   ├── skill-tree/
│   │   ├── pages/
│   │   │   └── skill-tree/
│   │   ├── components/
│   │   │   ├── skill-node/
│   │   │   └── skill-detail/
│   │   └── services/
│   │       └── skill.service.ts
│   │
│   ├── guilds/
│   │   ├── pages/
│   │   │   ├── guild-list/
│   │   │   ├── guild-detail/
│   │   │   ├── guild-create/
│   │   │   └── guild-chat/
│   │   ├── components/
│   │   │   ├── member-list/
│   │   │   ├── challenge-card/
│   │   │   └── chat-message/
│   │   └── services/
│   │       └── guild.service.ts
│   │
│   ├── leagues/
│   │   ├── pages/
│   │   │   └── leaderboard/
│   │   ├── components/
│   │   │   ├── league-badge/
│   │   │   ├── rank-card/
│   │   │   └── promotion-modal/
│   │   └── services/
│   │       └── league.service.ts
│   │
│   ├── boss-battle/
│   │   ├── pages/
│   │   │   ├── boss-list/
│   │   │   └── boss-fight/
│   │   ├── components/
│   │   │   ├── boss-hp-bar/
│   │   │   └── boss-reward/
│   │   └── services/
│   │       └── boss.service.ts
│   │
│   ├── analytics/
│   │   ├── pages/
│   │   │   └── analytics-dashboard/
│   │   ├── components/
│   │   │   ├── heatmap/
│   │   │   ├── weekly-chart/
│   │   │   ├── stat-trends/
│   │   │   └── life-score/
│   │   └── services/
│   │       └── analytics.service.ts
│   │
│   ├── ai-coach/
│   │   ├── pages/
│   │   │   └── coach-dashboard/
│   │   ├── components/
│   │   │   ├── suggestion-card/
│   │   │   └── insight-widget/
│   │   └── services/
│   │       └── ai-coach.service.ts
│   │
│   ├── premium/
│   │   ├── pages/
│   │   │   ├── subscription/
│   │   │   └── benefits/
│   │   └── services/
│   │       └── premium.service.ts
│   │
│   ├── social/
│   │   ├── pages/
│   │   │   ├── friends/
│   │   │   ├── challenges/
│   │   │   └── feed/
│   │   └── services/
│   │       └── social.service.ts
│   │
│   ├── profile/
│   │   ├── pages/
│   │   │   ├── profile-view/
│   │   │   └── profile-edit/
│   │   ├── components/
│   │   │   ├── avatar-card/
│   │   │   ├── stats-radar/
│   │   │   └── achievement-list/
│   │   └── services/
│   │       └── profile.service.ts
│   │
│   └── settings/
│       ├── pages/
│       │   └── settings/
│       └── services/
│           └── settings.service.ts
│
├── state/
│   ├── user.store.ts
│   ├── quest.store.ts
│   ├── xp.store.ts
│   ├── arc.store.ts
│   ├── streak.store.ts
│   ├── guild.store.ts
│   └── notification.store.ts
│
├── layouts/
│   ├── tabs/
│   │   ├── tabs.page.ts
│   │   ├── tabs.page.html
│   │   └── tabs.routes.ts
│   └── auth-layout/
│
├── assets/
│   ├── icons/
│   ├── images/
│   ├── animations/  (Lottie JSON files)
│   └── fonts/
│
├── theme/
│   ├── variables.scss
│   ├── global.scss
│   └── mixins.scss
│
└── environments/
    ├── environment.ts
    └── environment.prod.ts
```

---

## Spring Boot Backend Structure

```
com.ascend/
│
├── AscendApplication.java
│
├── auth/
│   ├── controller/
│   │   └── AuthController.java
│   ├── service/
│   │   └── AuthService.java
│   ├── filter/
│   │   └── FirebaseTokenFilter.java
│   ├── dto/
│   │   ├── LoginRequest.java
│   │   └── LoginResponse.java
│   └── config/
│       └── SecurityConfig.java
│
├── user/
│   ├── controller/
│   │   └── UserController.java
│   ├── service/
│   │   └── UserService.java
│   ├── repository/
│   │   └── UserRepository.java
│   ├── entity/
│   │   └── User.java
│   ├── dto/
│   │   ├── UserDto.java
│   │   └── UserProfileDto.java
│   └── mapper/
│       └── UserMapper.java
│
├── quest/
│   ├── controller/
│   │   └── QuestController.java
│   ├── service/
│   │   ├── QuestService.java
│   │   └── QuestCompletionService.java
│   ├── repository/
│   │   ├── QuestRepository.java
│   │   └── QuestCompletionRepository.java
│   ├── entity/
│   │   ├── Quest.java
│   │   └── QuestCompletion.java
│   ├── dto/
│   │   ├── QuestDto.java
│   │   ├── QuestCreateRequest.java
│   │   └── QuestCompleteResponse.java
│   ├── validator/
│   │   └── QuestValidator.java
│   └── scheduler/
│       └── QuestResetScheduler.java
│
├── xp/
│   ├── controller/
│   │   └── XpController.java
│   ├── service/
│   │   ├── XpService.java
│   │   └── LevelService.java
│   ├── repository/
│   │   └── XpHistoryRepository.java
│   ├── entity/
│   │   └── XpHistory.java
│   ├── dto/
│   │   └── XpSummaryDto.java
│   └── engine/
│       ├── XpCalculator.java
│       └── LevelCalculator.java
│
├── streak/
│   ├── controller/
│   │   └── StreakController.java
│   ├── service/
│   │   └── StreakService.java
│   ├── repository/
│   │   └── StreakRepository.java
│   ├── entity/
│   │   └── Streak.java
│   └── scheduler/
│       └── StreakCalculationScheduler.java
│
├── arc/
│   ├── controller/
│   │   └── ArcController.java
│   ├── service/
│   │   ├── ArcService.java
│   │   └── ArcProgressService.java
│   ├── repository/
│   │   ├── ArcRepository.java
│   │   └── UserArcProgressRepository.java
│   ├── entity/
│   │   ├── Arc.java
│   │   ├── ArcMilestone.java
│   │   └── UserArcProgress.java
│   └── dto/
│       └── ArcProgressDto.java
│
├── skilltree/
│   ├── controller/
│   │   └── SkillTreeController.java
│   ├── service/
│   │   └── SkillTreeService.java
│   ├── repository/
│   │   └── UserSkillRepository.java
│   └── entity/
│       └── UserSkill.java
│
├── league/
│   ├── controller/
│   │   └── LeagueController.java
│   ├── service/
│   │   ├── LeagueService.java
│   │   └── MatchmakingService.java
│   ├── repository/
│   │   └── LeaderboardRepository.java
│   ├── entity/
│   │   └── Leaderboard.java
│   └── scheduler/
│       └── LeagueResetScheduler.java
│
├── guild/
│   ├── controller/
│   │   └── GuildController.java
│   ├── service/
│   │   └── GuildService.java
│   ├── repository/
│   │   ├── GuildRepository.java
│   │   └── GuildMemberRepository.java
│   ├── entity/
│   │   ├── Guild.java
│   │   └── GuildMember.java
│   └── websocket/
│       └── GuildChatHandler.java
│
├── boss/
│   ├── controller/
│   │   └── BossController.java
│   ├── service/
│   │   └── BossService.java
│   ├── repository/
│   │   └── BossProgressRepository.java
│   └── entity/
│       └── BossProgress.java
│
├── analytics/
│   ├── controller/
│   │   └── AnalyticsController.java
│   ├── service/
│   │   ├── AnalyticsService.java
│   │   └── LifeScoreService.java
│   └── dto/
│       ├── WeeklyReportDto.java
│       └── LifeScoreDto.java
│
├── ai/
│   ├── service/
│   │   ├── AiCoachService.java
│   │   ├── BurnoutDetectionService.java
│   │   └── AdaptiveDifficultyService.java
│   └── engine/
│       └── RecommendationEngine.java
│
├── notification/
│   ├── controller/
│   │   └── NotificationController.java
│   ├── service/
│   │   ├── NotificationService.java
│   │   └── FcmService.java
│   ├── repository/
│   │   └── NotificationLogRepository.java
│   └── scheduler/
│       └── NotificationScheduler.java
│
├── premium/
│   ├── controller/
│   │   └── PremiumController.java
│   ├── service/
│   │   └── SubscriptionService.java
│   ├── repository/
│   │   └── SubscriptionRepository.java
│   └── entity/
│       └── Subscription.java
│
├── admin/
│   ├── controller/
│   │   ├── AdminArcController.java
│   │   ├── AdminUserController.java
│   │   └── AdminAnalyticsController.java
│   └── service/
│       ├── AdminService.java
│       └── ModerationService.java
│
└── common/
    ├── config/
    │   ├── WebSocketConfig.java
    │   ├── RedisConfig.java
    │   ├── CorsConfig.java
    │   └── AsyncConfig.java
    ├── exception/
    │   ├── GlobalExceptionHandler.java
    │   ├── BusinessException.java
    │   └── ErrorResponse.java
    ├── dto/
    │   └── ApiResponse.java
    ├── util/
    │   ├── DateUtil.java
    │   └── ValidationUtil.java
    └── constants/
        └── AppConstants.java
```

---

## Folder Implementation Dependency Graph

Build features in this exact order — each depends on the previous:

```
core/auth (foundation — everything needs auth)
    │
    ├── shared/ui (reusable components needed by all features)
    │
    ├── features/auth (login/signup screens)
    │
    ├── features/onboarding (needs auth)
    │       │
    │       ├── features/dashboard (needs onboarding complete)
    │       │       │
    │       │       ├── features/quests (core gameplay)
    │       │       │       │
    │       │       │       ├── features/xp (triggered by quests)
    │       │       │       │
    │       │       │       ├── features/streaks (triggered by quests)
    │       │       │       │
    │       │       │       └── features/arc-mode (organizes quests)
    │       │       │
    │       │       ├── features/analytics (reads all data)
    │       │       │
    │       │       └── features/premium (gates features)
    │       │
    │       └── features/social (post-MVP)
    │               ├── features/guilds
    │               ├── features/leagues
    │               └── features/boss-battle
```

### Critical Rule
Never jump ahead in this graph. Each feature depends on the ones above it being functional.

---

## Folder-by-Folder Build Order

### Frontend
```
1.  core/auth
2.  features/auth
3.  features/onboarding
4.  features/dashboard
5.  features/quests
6.  features/leveling (xp)
7.  features/streaks
8.  features/arc-mode
9.  features/analytics
10. features/premium
11. features/leagues
12. features/guilds
13. features/social
```

### Backend
```
1.  auth module
2.  user module
3.  quest module
4.  xp module
5.  streak module
6.  arc module
7.  analytics module
8.  notification module
9.  premium module
10. league module
11. guild module
12. boss module
13. ai module
14. admin module
```

---

*This document provides the complete folder structure for both frontend and backend.*
