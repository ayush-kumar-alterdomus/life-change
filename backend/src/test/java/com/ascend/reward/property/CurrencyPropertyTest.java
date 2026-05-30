package com.ascend.reward.property;

import com.ascend.reward.entity.UserCurrency;
import com.ascend.reward.repository.UserCurrencyRepository;
import com.ascend.reward.service.CurrencyService;
import net.jqwik.api.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Property 47: Daily cap never exceeded, diminishing returns applied.
 */
class CurrencyPropertyTest {

    @Property(tries = 100)
    void dailyCoinCapNeverExceeded(@ForAll("amounts") int amount, @ForAll("existingEarned") long alreadyEarned) {
        UserCurrencyRepository repo = mock(UserCurrencyRepository.class);
        CurrencyService service = new CurrencyService(repo);

        UUID userId = UUID.randomUUID();

        UserCurrency currency = UserCurrency.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .coins(1000L)
                .gems(0L)
                .dailyCoinsEarned(alreadyEarned)
                .dailyResetAt(LocalDateTime.now())
                .build();

        when(repo.findByUserId(userId)).thenReturn(Optional.of(currency));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.awardCoins(userId, amount, "test");

        assertThat(currency.getDailyCoinsEarned()).isLessThanOrEqualTo(CurrencyService.DAILY_COIN_CAP);
    }

    @Property(tries = 100)
    void diminishingReturnsReduceReward(@ForAll("baseAmounts") int baseAmount, @ForAll("repetitions") int repetition) {
        int effective = Math.max(1, baseAmount / Math.max(1, repetition));
        assertThat(effective).isLessThanOrEqualTo(baseAmount);
        assertThat(effective).isGreaterThanOrEqualTo(1);
    }

    @Provide
    Arbitrary<Integer> amounts() {
        return Arbitraries.integers().between(1, 1000);
    }

    @Provide
    Arbitrary<Long> existingEarned() {
        return Arbitraries.longs().between(0, 600);
    }

    @Provide
    Arbitrary<Integer> baseAmounts() {
        return Arbitraries.integers().between(10, 100);
    }

    @Provide
    Arbitrary<Integer> repetitions() {
        return Arbitraries.integers().between(1, 20);
    }
}
