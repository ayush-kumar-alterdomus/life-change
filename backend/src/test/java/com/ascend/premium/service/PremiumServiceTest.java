package com.ascend.premium.service;

import com.ascend.premium.entity.Subscription;
import com.ascend.premium.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PremiumServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    private PremiumService premiumService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        premiumService = new PremiumService(subscriptionRepository);
        userId = UUID.randomUUID();
    }

    @Test
    void isPremiumUser_activeSubscription_returnsTrue() {
        Subscription subscription = Subscription.builder()
                .userId(userId)
                .premium(true)
                .planType("PREMIUM")
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.of(subscription));

        assertThat(premiumService.isPremiumUser(userId)).isTrue();
    }

    @Test
    void isPremiumUser_expiredSubscription_returnsFalse() {
        Subscription subscription = Subscription.builder()
                .userId(userId)
                .premium(true)
                .planType("PREMIUM")
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();

        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.of(subscription));

        assertThat(premiumService.isPremiumUser(userId)).isFalse();
    }

    @Test
    void isPremiumUser_freeSubscription_returnsFalse() {
        Subscription subscription = Subscription.builder()
                .userId(userId)
                .premium(false)
                .planType("FREE")
                .build();

        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.of(subscription));

        assertThat(premiumService.isPremiumUser(userId)).isFalse();
    }

    @Test
    void isPremiumUser_noSubscription_returnsFalse() {
        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThat(premiumService.isPremiumUser(userId)).isFalse();
    }

    @Test
    void isPremiumUser_premiumWithNoExpiry_returnsTrue() {
        Subscription subscription = Subscription.builder()
                .userId(userId)
                .premium(true)
                .planType("LIFETIME")
                .expiresAt(null)
                .build();

        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.of(subscription));

        assertThat(premiumService.isPremiumUser(userId)).isTrue();
    }
}
