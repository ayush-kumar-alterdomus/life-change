package com.ascend.reward.service;

import com.ascend.common.exception.BusinessException;
import com.ascend.reward.dto.CurrencyResponse;
import com.ascend.reward.entity.UserCurrency;
import com.ascend.reward.repository.UserCurrencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final UserCurrencyRepository currencyRepository;

    public static final int DAILY_COIN_CAP = 500;

    @Transactional(readOnly = true)
    public CurrencyResponse getBalance(UUID userId) {
        UserCurrency currency = getOrCreate(userId);
        return new CurrencyResponse(currency.getCoins(), currency.getGems());
    }

    @Transactional
    public void awardCoins(UUID userId, int amount, String source) {
        if (amount <= 0) return;

        UserCurrency currency = getOrCreate(userId);
        resetDailyIfNeeded(currency);

        long remaining = DAILY_COIN_CAP - currency.getDailyCoinsEarned();
        if (remaining <= 0) {
            log.debug("Daily coin cap reached for user={}", userId);
            return;
        }

        long awarded = Math.min(amount, remaining);
        currency.setCoins(currency.getCoins() + awarded);
        currency.setDailyCoinsEarned(currency.getDailyCoinsEarned() + awarded);
        currencyRepository.save(currency);

        log.debug("Awarded {} coins to user={} source={}", awarded, userId, source);
    }

    /**
     * Awards coins with diminishing returns: amount × (1 / repetition).
     */
    @Transactional
    public void awardCoinsWithDiminishing(UUID userId, int baseAmount, int repetition) {
        int effective = Math.max(1, baseAmount / Math.max(1, repetition));
        awardCoins(userId, effective, "diminishing");
    }

    @Transactional
    public void awardGems(UUID userId, int amount, String source) {
        if (amount <= 0) return;
        UserCurrency currency = getOrCreate(userId);
        currency.setGems(currency.getGems() + amount);
        currencyRepository.save(currency);
        log.debug("Awarded {} gems to user={} source={}", amount, userId, source);
    }

    @Transactional
    public void spendCoins(UUID userId, int amount) {
        UserCurrency currency = getOrCreate(userId);
        if (currency.getCoins() < amount) {
            throw new BusinessException("INSUFFICIENT_COINS", "Not enough coins");
        }
        currency.setCoins(currency.getCoins() - amount);
        currencyRepository.save(currency);
    }

    @Transactional
    public void spendGems(UUID userId, int amount) {
        UserCurrency currency = getOrCreate(userId);
        if (currency.getGems() < amount) {
            throw new BusinessException("INSUFFICIENT_GEMS", "Not enough gems");
        }
        currency.setGems(currency.getGems() - amount);
        currencyRepository.save(currency);
    }

    private UserCurrency getOrCreate(UUID userId) {
        return currencyRepository.findByUserId(userId).orElseGet(() -> {
            UserCurrency c = UserCurrency.builder().userId(userId).build();
            return currencyRepository.save(c);
        });
    }

    private void resetDailyIfNeeded(UserCurrency currency) {
        LocalDate resetDate = currency.getDailyResetAt().toLocalDate();
        if (resetDate.isBefore(LocalDate.now())) {
            currency.setDailyCoinsEarned(0L);
            currency.setDailyResetAt(LocalDateTime.now());
        }
    }
}
