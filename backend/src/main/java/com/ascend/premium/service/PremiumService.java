package com.ascend.premium.service;

import com.ascend.premium.entity.Subscription;
import com.ascend.premium.repository.SubscriptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for checking user premium/subscription status.
 */
@Slf4j
@Service
public class PremiumService {

    private final SubscriptionRepository subscriptionRepository;

    public PremiumService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    /**
     * Checks whether a user currently has an active premium subscription.
     *
     * @param userId the user's ID
     * @return true if the user has an active premium subscription
     */
    @Transactional(readOnly = true)
    public boolean isPremiumUser(UUID userId) {
        return subscriptionRepository.findByUserId(userId)
                .map(this::isSubscriptionActive)
                .orElse(false);
    }

    private boolean isSubscriptionActive(Subscription subscription) {
        if (!Boolean.TRUE.equals(subscription.getPremium())) {
            return false;
        }
        // If there's an expiry date, check it hasn't passed
        if (subscription.getExpiresAt() != null) {
            return subscription.getExpiresAt().isAfter(LocalDateTime.now());
        }
        return true;
    }
}
