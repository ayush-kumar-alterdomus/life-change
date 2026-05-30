package com.ascend.analytics.service;

import com.ascend.streak.repository.StreakRepository;
import com.ascend.user.entity.UserStats;
import com.ascend.user.repository.UserStatsRepository;
import com.ascend.user.service.LifeScoreCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Service responsible for calculating and persisting the Life Score metric.
 * Delegates the actual formula to LifeScoreCalculator and handles persistence.
 * Called after stat changes and in weekly report generation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LifeScoreService {

    private final UserStatsRepository userStatsRepository;
    private final StreakRepository streakRepository;
    private final LifeScoreCalculator lifeScoreCalculator;

    /**
     * Calculates and persists the Life Score for a user.
     * Steps:
     * 1. Fetch user_stats
     * 2. Fetch streak data for consistency metric
     * 3. Apply formula: 0.25×Discipline + 0.2×Focus + 0.2×Vitality + 0.2×Wisdom + 0.15×Consistency
     * 4. Normalize to 0-100 scale
     * 5. Update user_stats.life_score
     *
     * @param userId the user's ID
     * @return the calculated Life Score (0-100)
     */
    @Transactional
    public BigDecimal calculateLifeScore(UUID userId) {
        // 1. Fetch user_stats
        UserStats stats = userStatsRepository.findByUserId(userId)
                .orElse(null);

        if (stats == null) {
            log.debug("No stats found for user {} — Life Score is 0", userId);
            return BigDecimal.ZERO;
        }

        // 2. Fetch streak data for consistency metric
        int currentStreak = streakRepository.findByUserId(userId)
                .map(streak -> streak.getCurrentStreak())
                .orElse(0);

        // 3. Derive consistency (current_streak / 30, capped at 1.0)
        double consistency = LifeScoreCalculator.deriveConsistency(currentStreak);

        // 4. Apply formula via LifeScoreCalculator (normalized to 0-100)
        BigDecimal lifeScore = lifeScoreCalculator.calculate(
                stats.getDiscipline(),
                stats.getFocus(),
                stats.getVitality(),
                stats.getWisdom(),
                consistency
        );

        // 5. Update user_stats.life_score
        stats.setLifeScore(lifeScore);
        userStatsRepository.save(stats);

        log.debug("Life Score calculated for user {}: {} (streak={}, consistency={})",
                userId, lifeScore, currentStreak, consistency);

        return lifeScore;
    }

    /**
     * Gets the current Life Score for a user without recalculating.
     *
     * @param userId the user's ID
     * @return the stored Life Score, or ZERO if no stats exist
     */
    @Transactional(readOnly = true)
    public BigDecimal getCurrentLifeScore(UUID userId) {
        return userStatsRepository.findByUserId(userId)
                .map(UserStats::getLifeScore)
                .orElse(BigDecimal.ZERO);
    }
}
