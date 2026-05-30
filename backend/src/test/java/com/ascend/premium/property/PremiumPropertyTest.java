package com.ascend.premium.property;

import com.ascend.common.exception.BusinessException;
import com.ascend.premium.entity.Subscription;
import com.ascend.premium.repository.SubscriptionRepository;
import com.ascend.premium.service.PayToWinGuard;
import com.ascend.premium.service.SubscriptionService;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import net.jqwik.api.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PremiumPropertyTest {

    /**
     * Property 50: Downgrade preserves all earned progress (XP, level, achievements remain unchanged).
     */
    @Property(tries = 100)
    void downgradePreservesEarnedProgress(
            @ForAll("levels") int level,
            @ForAll("xpValues") long xp) {

        SubscriptionRepository subRepo = mock(SubscriptionRepository.class);
        UserRepository userRepo = mock(UserRepository.class);

        SubscriptionService service = new SubscriptionService(subRepo, userRepo);

        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId).firebaseUid("uid").username("test")
                .level(level).xp(xp).premium(true).build();

        Subscription sub = Subscription.builder()
                .id(UUID.randomUUID()).userId(userId)
                .premium(true).planType("MONTHLY")
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();

        when(subRepo.findByUserId(userId)).thenReturn(Optional.of(sub));
        when(subRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(userRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.downgradeToFree(userId);

        // Progress preserved
        assertThat(user.getLevel()).isEqualTo(level);
        assertThat(user.getXp()).isEqualTo(xp);
        // Premium revoked
        assertThat(user.getPremium()).isFalse();
        assertThat(sub.getPremium()).isFalse();
    }

    /**
     * Property 51: No purchase grants direct XP or leaderboard rank.
     */
    @Property(tries = 100)
    void noPurchaseGrantsDirectXp(@ForAll("blockedTypes") String itemType) {
        PayToWinGuard guard = new PayToWinGuard();
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> guard.validatePurchase(itemType, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not allowed");
    }

    @Provide
    Arbitrary<Integer> levels() {
        return Arbitraries.integers().between(1, 200);
    }

    @Provide
    Arbitrary<Long> xpValues() {
        return Arbitraries.longs().between(0, 1_000_000);
    }

    @Provide
    Arbitrary<String> blockedTypes() {
        return Arbitraries.of("XP_BOOST", "XP_PACK", "LEADERBOARD_RANK", "LEVEL_SKIP", "INSTANT_XP");
    }
}
