# Implementation Plan: Premium System

## Overview

Subscription tier management with free trial, feature gating, graceful downgrade, and no pay-to-win enforcement.

## Tasks

- [ ] 1. Create Premium DTOs and enums
  - [ ] 1.1 Create premium data models
    - Create `SubscriptionTier.java` enum: FREE, PREMIUM
    - Create `PremiumFeature.java` enum: AI_COACH, UNLIMITED_ARCS, ADVANCED_ANALYTICS, SKILL_RESET, PREMIUM_COSMETICS, STREAK_SHIELDS, HARD_MODE, UNLIMITED_GUILDS
    - Create `SubscriptionStatusResponse.java`: tier, premium, trialActive, trialEndsAt, expiresAt, autoRenew, features (list of unlocked features)
    - Create `StartTrialRequest.java`: (empty — just triggers trial)

- [ ] 2. Implement Subscription Service
  - [ ] 2.1 Create SubscriptionService
    - Create `SubscriptionService.java` in `premium/service/`
    - `getSubscriptionStatus(UUID userId)` — return current tier and features
    - `startTrial(UUID userId)`:
      1. Verify user hasn't used trial before (add `trial_used` boolean to subscriptions)
      2. Set premium = true, plan_type = 'TRIAL'
      3. Set expires_at = now() + 7 days
      4. No payment required
    - `activatePremium(UUID userId, String provider, String receiptToken)`:
      1. Validate receipt with provider (Apple/Google)
      2. Set premium = true, plan_type = 'MONTHLY'/'YEARLY'
      3. Set expires_at based on plan
      4. Update user.premium = true
    - `cancelSubscription(UUID userId)`:
      1. Set auto_renew = false
      2. Premium remains active until expires_at
  - [ ] 2.2 Create FeatureGateService
    - Create `FeatureGateService.java` in `premium/service/`
    - `hasAccess(UUID userId, PremiumFeature feature)`:
      1. Check user.premium status
      2. If premium → all features accessible
      3. If free → only free-tier features
    - `requirePremium(UUID userId)` — throw SubscriptionExpiredException if not premium
    - Used by other services to gate premium features

- [ ] 3. Implement Subscription Expiry
  - [ ] 3.1 Create expiry scheduler
    - Create `SubscriptionExpiryScheduler.java` in `premium/scheduler/`
    - Run hourly
    - Find subscriptions where expires_at < now() AND premium = true AND auto_renew = false
    - For each: call `downgradeToFree(userId)`
    - `downgradeToFree(UUID userId)`:
      1. Set user.premium = false
      2. Set subscription.premium = false
      3. Preserve ALL earned progress (XP, levels, achievements, stats, cosmetics)
      4. Only restrict access to premium features going forward
      5. Send notification about downgrade

- [ ] 4. Implement No Pay-to-Win Guard
  - [ ] 4.1 Create pay-to-win prevention
    - Premium benefits are convenience/cosmetic only:
      - More custom arcs (not better XP)
      - AI coaching (guidance, not XP boost)
      - Streak shields (protection, not XP gain)
      - Cosmetics (visual only)
    - `validatePurchase(String itemType, UUID userId)`:
      - Reject any purchase that would directly grant XP or leaderboard rank
      - Log attempt as potential abuse

- [ ] 5. Create Premium Controller
  - [ ] 5.1 Implement REST endpoints
    - Create `PremiumController.java` in `premium/controller/`
    - GET `/api/v1/premium/status` — subscription status
    - POST `/api/v1/premium/trial` — start 7-day trial
    - POST `/api/v1/premium/subscribe` — activate subscription
    - POST `/api/v1/premium/cancel` — cancel auto-renewal
    - GET `/api/v1/premium/features` — list all features with access status

- [ ] 6. Write property-based tests
  - [ ] 6.1 Create premium property tests
    - Create `PremiumPropertyTest.java`:
      - Property 50: Downgrade preserves all earned progress
      - Property 51: No purchase grants direct XP or leaderboard rank
    - Minimum 100 iterations per property

- [ ] 7. Checkpoint - Verify premium system
  - Integration test: start trial → features unlocked → trial expires → downgraded
  - Integration test: downgrade preserves XP, levels, achievements
  - Integration test: feature gate blocks premium features for free users
  - Property tests all pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- **RULE: Do NOT run any mvn, gradle, npm, or test commands. Only create/edit files. No build or test verification steps.**

- Trial is one-time only per user
- Downgrade is graceful — never loses earned progress
- Premium doesn't grant competitive advantages (no pay-to-win)
- Feature gating is checked at service layer, not controller
- Receipt validation is provider-specific (Apple/Google)
