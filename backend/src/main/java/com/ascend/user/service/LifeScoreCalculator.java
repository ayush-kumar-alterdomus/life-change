package com.ascend.user.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Pure function calculator for the Life Score metric.
 * <p>
 * Life Score is a composite metric (0-100) derived from weighted character stats
 * and streak-based consistency. It provides a single number representing overall
 * life progress for the analytics dashboard.
 * <p>
 * Formula: 0.25×Discipline + 0.2×Focus + 0.2×Vitality + 0.2×Wisdom + 0.15×Consistency
 * Normalized to a 0-100 scale based on maximum possible stat values.
 */
@Component
public class LifeScoreCalculator {

    private static final double DISCIPLINE_WEIGHT = 0.25;
    private static final double FOCUS_WEIGHT = 0.20;
    private static final double VITALITY_WEIGHT = 0.20;
    private static final double WISDOM_WEIGHT = 0.20;
    private static final double CONSISTENCY_WEIGHT = 0.15;

    /**
     * Maximum stat value used for normalization.
     * Stats are unbounded in theory, but we normalize against a practical max of 1000
     * (the "Master" threshold) to keep the score in 0-100 range.
     */
    private static final double MAX_STAT_VALUE = 1000.0;

    /**
     * Maximum consistency value (1.0 = 30-day streak achieved).
     */
    private static final double MAX_CONSISTENCY = 1.0;

    /**
     * Calculates the Life Score as a normalized 0-100 value.
     * <p>
     * The formula applies weighted contributions from each stat and consistency:
     * <ul>
     *   <li>Discipline: 25% weight</li>
     *   <li>Focus: 20% weight</li>
     *   <li>Vitality: 20% weight</li>
     *   <li>Wisdom: 20% weight</li>
     *   <li>Consistency: 15% weight (derived from current_streak / 30, capped at 1.0)</li>
     * </ul>
     * <p>
     * Each stat is normalized against the max possible value (1000) before weighting.
     * Consistency is already a 0-1 ratio. The final score is scaled to 0-100.
     *
     * @param discipline  the user's discipline stat (0+)
     * @param focus       the user's focus stat (0+)
     * @param vitality    the user's vitality stat (0+)
     * @param wisdom      the user's wisdom stat (0+)
     * @param consistency the user's consistency ratio (current_streak / 30, capped at 1.0)
     * @return the Life Score as a BigDecimal in the range [0, 100], rounded to 2 decimal places
     */
    public BigDecimal calculate(int discipline, int focus, int vitality, int wisdom, double consistency) {
        // Clamp consistency to [0, 1]
        double clampedConsistency = Math.max(0.0, Math.min(consistency, MAX_CONSISTENCY));

        // Normalize each stat to a 0-1 ratio (capped at 1.0 for stats exceeding max)
        double normalizedDiscipline = Math.min(discipline / MAX_STAT_VALUE, 1.0);
        double normalizedFocus = Math.min(focus / MAX_STAT_VALUE, 1.0);
        double normalizedVitality = Math.min(vitality / MAX_STAT_VALUE, 1.0);
        double normalizedWisdom = Math.min(wisdom / MAX_STAT_VALUE, 1.0);

        // Apply weighted formula
        double weightedScore = (DISCIPLINE_WEIGHT * normalizedDiscipline)
                + (FOCUS_WEIGHT * normalizedFocus)
                + (VITALITY_WEIGHT * normalizedVitality)
                + (WISDOM_WEIGHT * normalizedWisdom)
                + (CONSISTENCY_WEIGHT * clampedConsistency);

        // Scale to 0-100
        double lifeScore = weightedScore * 100.0;

        // Clamp to [0, 100] for safety (should already be in range)
        lifeScore = Math.max(0.0, Math.min(lifeScore, 100.0));

        return BigDecimal.valueOf(lifeScore).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Derives the consistency value from streak data.
     * Consistency = current_streak / 30, capped at 1.0.
     *
     * @param currentStreak the user's current streak in days
     * @return consistency ratio between 0.0 and 1.0
     */
    public static double deriveConsistency(int currentStreak) {
        return Math.min(currentStreak / 30.0, MAX_CONSISTENCY);
    }
}
