package com.ascend.user.service;

import com.ascend.common.entity.Difficulty;
import com.ascend.common.entity.StatType;
import com.ascend.streak.entity.Streak;
import com.ascend.streak.repository.StreakRepository;
import com.ascend.user.entity.UserStats;
import com.ascend.user.repository.UserStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Service for awarding stat points on quest completion,
 * recalculating life_score, and checking identity title unlocks.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatService {

    private static final double DISCIPLINE_WEIGHT = 0.25;
    private static final double FOCUS_WEIGHT = 0.20;
    private static final double VITALITY_WEIGHT = 0.20;
    private static final double WISDOM_WEIGHT = 0.20;
    private static final double CONSISTENCY_WEIGHT = 0.15;
    private static final double CONSISTENCY_MAX_STREAK_DAYS = 30.0;

    private final UserStatsRepository userStatsRepository;
    private final StreakRepository streakRepository;

    /**
     * Awards stat points to a user based on quest difficulty and stat type.
     * Updates the relevant stat, recalculates life_score, and checks for title unlocks.
     *
     * @param userId     the user receiving stat points
     * @param statType   the stat category to increase
     * @param difficulty the quest difficulty determining point gain
     */
    @Transactional
    public void awardStatPoints(UUID userId, StatType statType, Difficulty difficulty) {
        UserStats stats = userStatsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultStats(userId));

        int statGain = calculateStatGain(difficulty);

        applyStatGain(stats, statType, statGain);

        BigDecimal newLifeScore = getLifeScore(stats, userId);
        stats.setLifeScore(newLifeScore);

        userStatsRepository.save(stats);

        log.debug("Awarded {} {} points to user {}. New life_score: {}",
                statGain, statType, userId, newLifeScore);

        checkIdentityTitleUnlocks(userId, stats, statType);
    }

    /**
     * Calculates the life_score based on weighted stat values and streak-based consistency.
     * Formula: 0.25×Discipline + 0.2×Focus + 0.2×Vitality + 0.2×Wisdom + 0.15×Consistency
     * Consistency is derived from the user's current streak (capped at 30 days → 100%).
     *
     * @param stats  the user's current stats
     * @param userId the user ID (for streak lookup)
     * @return the calculated life_score
     */
    public BigDecimal getLifeScore(UserStats stats, UUID userId) {
        double consistency = getConsistencyScore(userId);

        double score = (DISCIPLINE_WEIGHT * stats.getDiscipline())
                + (FOCUS_WEIGHT * stats.getFocus())
                + (VITALITY_WEIGHT * stats.getVitality())
                + (WISDOM_WEIGHT * stats.getWisdom())
                + (CONSISTENCY_WEIGHT * consistency);

        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates stat gain from quest difficulty.
     * BaseStat(1) × DifficultyMultiplier (EASY=1, MEDIUM=1, HARD=2, LEGENDARY=3).
     *
     * @param difficulty the quest difficulty
     * @return integer stat points to award
     */
    int calculateStatGain(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 1;
            case MEDIUM -> 1;
            case HARD -> 2;
            case LEGENDARY -> 3;
        };
    }

    private void applyStatGain(UserStats stats, StatType statType, int gain) {
        switch (statType) {
            case STRENGTH -> stats.setStrength(stats.getStrength() + gain);
            case WISDOM -> stats.setWisdom(stats.getWisdom() + gain);
            case FOCUS -> stats.setFocus(stats.getFocus() + gain);
            case DISCIPLINE -> stats.setDiscipline(stats.getDiscipline() + gain);
            case VITALITY -> stats.setVitality(stats.getVitality() + gain);
            case CHARISMA -> stats.setCharisma(stats.getCharisma() + gain);
        }
    }

    private double getConsistencyScore(UUID userId) {
        int currentStreak = streakRepository.findByUserId(userId)
                .map(Streak::getCurrentStreak)
                .orElse(0);

        return Math.min(currentStreak / CONSISTENCY_MAX_STREAK_DAYS, 1.0) * 100.0;
    }

    private UserStats createDefaultStats(UUID userId) {
        UserStats stats = UserStats.builder()
                .userId(userId)
                .build();
        return userStatsRepository.save(stats);
    }

    /**
     * Checks if the user has crossed any stat thresholds for identity title unlocks.
     * Thresholds: 25, 50, 100, 200, 500 per stat.
     */
    private void checkIdentityTitleUnlocks(UUID userId, UserStats stats, StatType statType) {
        int statValue = getStatValue(stats, statType);
        int[] thresholds = {25, 50, 100, 200, 500};

        for (int threshold : thresholds) {
            if (statValue >= threshold && (statValue - calculateStatGain(Difficulty.EASY)) < threshold) {
                log.info("User {} unlocked identity title for {} at threshold {}",
                        userId, statType, threshold);
                // Future: publish IdentityTitleUnlockedEvent for notification/achievement modules
            }
        }
    }

    private int getStatValue(UserStats stats, StatType statType) {
        return switch (statType) {
            case STRENGTH -> stats.getStrength();
            case WISDOM -> stats.getWisdom();
            case FOCUS -> stats.getFocus();
            case DISCIPLINE -> stats.getDiscipline();
            case VITALITY -> stats.getVitality();
            case CHARISMA -> stats.getCharisma();
        };
    }
}
