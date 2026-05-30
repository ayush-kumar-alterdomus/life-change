package com.ascend.premium.service;

import com.ascend.common.exception.BusinessException;
import com.ascend.premium.dto.PremiumFeature;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeatureGateService {

    private final SubscriptionService subscriptionService;

    public boolean hasAccess(UUID userId, PremiumFeature feature) {
        return subscriptionService.isPremiumUser(userId);
    }

    public void requirePremium(UUID userId) {
        if (!subscriptionService.isPremiumUser(userId)) {
            throw new BusinessException("PREMIUM_REQUIRED", "This feature requires a premium subscription");
        }
    }
}
