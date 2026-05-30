package com.ascend.premium.service;

import com.ascend.common.exception.BusinessException;
import com.ascend.premium.dto.*;
import com.ascend.premium.entity.Subscription;
import com.ascend.premium.repository.SubscriptionRepository;
import com.ascend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public SubscriptionStatusResponse getSubscriptionStatus(UUID userId) {
        Subscription sub = getOrCreate(userId);
        boolean active = isActive(sub);
        boolean trialActive = "TRIAL".equals(sub.getPlanType()) && active;

        List<PremiumFeature> features = active
                ? Arrays.asList(PremiumFeature.values())
                : List.of();

        return new SubscriptionStatusResponse(
                active ? SubscriptionTier.PREMIUM : SubscriptionTier.FREE,
                active,
                trialActive,
                trialActive ? sub.getExpiresAt() : null,
                sub.getExpiresAt(),
                Boolean.TRUE.equals(sub.getAutoRenew()),
                features);
    }

    @Transactional
    public void startTrial(UUID userId) {
        Subscription sub = getOrCreate(userId);

        if (Boolean.TRUE.equals(sub.getTrialUsed())) {
            throw new BusinessException("TRIAL_ALREADY_USED", "Free trial has already been used");
        }

        sub.setPremium(true);
        sub.setPlanType("TRIAL");
        sub.setTrialUsed(true);
        sub.setStartedAt(LocalDateTime.now());
        sub.setExpiresAt(LocalDateTime.now().plusDays(7));
        sub.setAutoRenew(false);
        subscriptionRepository.save(sub);

        userRepository.findById(userId).ifPresent(user -> {
            user.setPremium(true);
            userRepository.save(user);
        });

        log.info("Trial started for user={}", userId);
    }

    @Transactional
    public void activatePremium(UUID userId, String provider, String planType) {
        Subscription sub = getOrCreate(userId);

        LocalDateTime expiresAt = "YEARLY".equals(planType)
                ? LocalDateTime.now().plusYears(1)
                : LocalDateTime.now().plusMonths(1);

        sub.setPremium(true);
        sub.setPlanType(planType);
        sub.setProvider(provider);
        sub.setStartedAt(LocalDateTime.now());
        sub.setExpiresAt(expiresAt);
        sub.setAutoRenew(true);
        subscriptionRepository.save(sub);

        userRepository.findById(userId).ifPresent(user -> {
            user.setPremium(true);
            userRepository.save(user);
        });

        log.info("Premium activated for user={} plan={} provider={}", userId, planType, provider);
    }

    @Transactional
    public void cancelSubscription(UUID userId) {
        Subscription sub = subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("NO_SUBSCRIPTION", "No subscription found"));

        sub.setAutoRenew(false);
        subscriptionRepository.save(sub);
        log.info("Subscription cancelled (remains active until expiry) for user={}", userId);
    }

    @Transactional
    public void downgradeToFree(UUID userId) {
        Subscription sub = subscriptionRepository.findByUserId(userId).orElse(null);
        if (sub == null) return;

        sub.setPremium(false);
        sub.setPlanType("FREE");
        subscriptionRepository.save(sub);

        userRepository.findById(userId).ifPresent(user -> {
            user.setPremium(false);
            userRepository.save(user);
        });

        log.info("User downgraded to free: user={}", userId);
    }

    public boolean isPremiumUser(UUID userId) {
        return subscriptionRepository.findByUserId(userId)
                .map(this::isActive)
                .orElse(false);
    }

    private boolean isActive(Subscription sub) {
        if (!Boolean.TRUE.equals(sub.getPremium())) return false;
        if (sub.getExpiresAt() != null) {
            return sub.getExpiresAt().isAfter(LocalDateTime.now());
        }
        return true;
    }

    private Subscription getOrCreate(UUID userId) {
        return subscriptionRepository.findByUserId(userId).orElseGet(() -> {
            Subscription s = Subscription.builder().userId(userId).build();
            return subscriptionRepository.save(s);
        });
    }
}
