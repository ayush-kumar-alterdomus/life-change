package com.ascend.premium.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SubscriptionStatusResponse(
        SubscriptionTier tier,
        boolean premium,
        boolean trialActive,
        LocalDateTime trialEndsAt,
        LocalDateTime expiresAt,
        boolean autoRenew,
        List<PremiumFeature> features
) {}
