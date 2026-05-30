package com.ascend.premium.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Legacy service — delegates to SubscriptionService.
 */
@Service
@RequiredArgsConstructor
public class PremiumService {

    private final SubscriptionService subscriptionService;

    public boolean isPremiumUser(UUID userId) {
        return subscriptionService.isPremiumUser(userId);
    }
}
