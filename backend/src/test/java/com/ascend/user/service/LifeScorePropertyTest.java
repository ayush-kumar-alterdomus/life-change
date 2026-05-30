package com.ascend.user.service;

import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for LifeScoreCalculator.
 * Validates formula correctness and range constraints across randomized inputs.
 */
class LifeScorePropertyTest {

    private final LifeScoreCalculator calculator = new LifeScoreCalculator();

    // ========================================================================
    // Property 48: Life Score formula correctness
    // Validates: Requirements 1.2
    // ========================================================================

    @Property(tries = 200)
    void lifeScoreMatchesWeightedFormula(
            @ForAll @IntRange(min = 0, max = 1000) int discipline,
            @ForAll @IntRange(min = 0, max = 1000) int focus,
            @ForAll @IntRange(min = 0, max = 1000) int vitality,
            @ForAll @IntRange(min = 0, max = 1000) int wisdom,
            @ForAll @DoubleRange(min = 0.0, max = 1.0) double consistency) {

        BigDecimal result = calculator.calculate(discipline, focus, vitality, wisdom, consistency);

        // Replicate the formula: normalize stats to 0-1 (capped at 1.0), apply weights, scale to 100
        double normalizedDiscipline = Math.min(discipline / 1000.0, 1.0);
        double normalizedFocus = Math.min(focus / 1000.0, 1.0);
        double normalizedVitality = Math.min(vitality / 1000.0, 1.0);
        double normalizedWisdom = Math.min(wisdom / 1000.0, 1.0);
        double clampedConsistency = Math.max(0.0, Math.min(consistency, 1.0));

        double weightedScore = (0.25 * normalizedDiscipline)
                + (0.20 * normalizedFocus)
                + (0.20 * normalizedVitality)
                + (0.20 * normalizedWisdom)
                + (0.15 * clampedConsistency);

        double expectedRaw = weightedScore * 100.0;
        expectedRaw = Math.max(0.0, Math.min(expectedRaw, 100.0));
        BigDecimal expected = BigDecimal.valueOf(expectedRaw).setScale(2, RoundingMode.HALF_UP);

        assertThat(result)
                .as("Life Score for discipline=%d, focus=%d, vitality=%d, wisdom=%d, consistency=%.2f",
                        discipline, focus, vitality, wisdom, consistency)
                .isEqualByComparingTo(expected);
    }

    @Property(tries = 200)
    void lifeScoreFormulaWithStatsExceedingMax(
            @ForAll @IntRange(min = 1000, max = 5000) int discipline,
            @ForAll @IntRange(min = 1000, max = 5000) int focus,
            @ForAll @IntRange(min = 1000, max = 5000) int vitality,
            @ForAll @IntRange(min = 1000, max = 5000) int wisdom,
            @ForAll @DoubleRange(min = 0.0, max = 1.0) double consistency) {

        BigDecimal result = calculator.calculate(discipline, focus, vitality, wisdom, consistency);

        // When all stats exceed max (1000), they are capped at 1.0 in normalization
        // So the stat contribution is: 0.25 + 0.20 + 0.20 + 0.20 = 0.85
        // Plus consistency contribution: 0.15 × consistency
        double clampedConsistency = Math.max(0.0, Math.min(consistency, 1.0));
        double expectedRaw = (0.85 + 0.15 * clampedConsistency) * 100.0;
        expectedRaw = Math.max(0.0, Math.min(expectedRaw, 100.0));
        BigDecimal expected = BigDecimal.valueOf(expectedRaw).setScale(2, RoundingMode.HALF_UP);

        assertThat(result).isEqualByComparingTo(expected);
    }

    // ========================================================================
    // Life Score always in [0, 100] range
    // Validates: Requirements 1.2
    // ========================================================================

    @Property(tries = 200)
    void lifeScoreAlwaysInZeroToHundredRange(
            @ForAll @IntRange(min = 0, max = 5000) int discipline,
            @ForAll @IntRange(min = 0, max = 5000) int focus,
            @ForAll @IntRange(min = 0, max = 5000) int vitality,
            @ForAll @IntRange(min = 0, max = 5000) int wisdom,
            @ForAll @DoubleRange(min = -1.0, max = 2.0) double consistency) {

        BigDecimal result = calculator.calculate(discipline, focus, vitality, wisdom, consistency);

        assertThat(result.doubleValue())
                .as("Life Score should be in [0, 100] for discipline=%d, focus=%d, vitality=%d, wisdom=%d, consistency=%.2f",
                        discipline, focus, vitality, wisdom, consistency)
                .isBetween(0.0, 100.0);
    }

    @Property(tries = 200)
    void lifeScoreIsZeroWhenAllStatsAreZeroAndNoConsistency() {
        BigDecimal result = calculator.calculate(0, 0, 0, 0, 0.0);

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Property(tries = 200)
    void lifeScoreIsMaxWhenAllStatsMaxAndFullConsistency() {
        BigDecimal result = calculator.calculate(1000, 1000, 1000, 1000, 1.0);

        assertThat(result).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Property(tries = 200)
    void lifeScoreIsMonotonicInDiscipline(
            @ForAll @IntRange(min = 0, max = 1000) int discipline1,
            @ForAll @IntRange(min = 0, max = 1000) int discipline2,
            @ForAll @IntRange(min = 0, max = 1000) int focus,
            @ForAll @IntRange(min = 0, max = 1000) int vitality,
            @ForAll @IntRange(min = 0, max = 1000) int wisdom,
            @ForAll @DoubleRange(min = 0.0, max = 1.0) double consistency) {

        BigDecimal score1 = calculator.calculate(discipline1, focus, vitality, wisdom, consistency);
        BigDecimal score2 = calculator.calculate(discipline2, focus, vitality, wisdom, consistency);

        if (discipline1 <= discipline2) {
            assertThat(score1.compareTo(score2))
                    .as("Life Score should be monotonic in discipline: score(%d)=%s <= score(%d)=%s",
                            discipline1, score1, discipline2, score2)
                    .isLessThanOrEqualTo(0);
        } else {
            assertThat(score1.compareTo(score2))
                    .as("Life Score should be monotonic in discipline: score(%d)=%s >= score(%d)=%s",
                            discipline1, score1, discipline2, score2)
                    .isGreaterThanOrEqualTo(0);
        }
    }

    @Property(tries = 200)
    void lifeScoreIsMonotonicInConsistency(
            @ForAll @IntRange(min = 0, max = 1000) int discipline,
            @ForAll @IntRange(min = 0, max = 1000) int focus,
            @ForAll @IntRange(min = 0, max = 1000) int vitality,
            @ForAll @IntRange(min = 0, max = 1000) int wisdom,
            @ForAll @DoubleRange(min = 0.0, max = 1.0) double consistency1,
            @ForAll @DoubleRange(min = 0.0, max = 1.0) double consistency2) {

        BigDecimal score1 = calculator.calculate(discipline, focus, vitality, wisdom, consistency1);
        BigDecimal score2 = calculator.calculate(discipline, focus, vitality, wisdom, consistency2);

        if (consistency1 <= consistency2) {
            assertThat(score1.compareTo(score2))
                    .as("Life Score should be monotonic in consistency: score(%.2f)=%s <= score(%.2f)=%s",
                            consistency1, score1, consistency2, score2)
                    .isLessThanOrEqualTo(0);
        } else {
            assertThat(score1.compareTo(score2))
                    .as("Life Score should be monotonic in consistency: score(%.2f)=%s >= score(%.2f)=%s",
                            consistency1, score1, consistency2, score2)
                    .isGreaterThanOrEqualTo(0);
        }
    }

    // ========================================================================
    // Consistency derivation tests
    // ========================================================================

    @Property(tries = 200)
    void deriveConsistencyAlwaysInZeroToOneRange(
            @ForAll @IntRange(min = 0, max = 365) int currentStreak) {

        double consistency = LifeScoreCalculator.deriveConsistency(currentStreak);

        assertThat(consistency)
                .as("Consistency for streak %d should be in [0, 1]", currentStreak)
                .isBetween(0.0, 1.0);
    }

    @Property(tries = 200)
    void deriveConsistencyMatchesFormula(
            @ForAll @IntRange(min = 0, max = 365) int currentStreak) {

        double consistency = LifeScoreCalculator.deriveConsistency(currentStreak);
        double expected = Math.min(currentStreak / 30.0, 1.0);

        assertThat(consistency)
                .as("Consistency for streak %d should be min(%d/30, 1.0) = %f",
                        currentStreak, currentStreak, expected)
                .isEqualTo(expected);
    }

    @Property(tries = 200)
    void deriveConsistencyCapsAtOne(
            @ForAll @IntRange(min = 30, max = 365) int currentStreak) {

        double consistency = LifeScoreCalculator.deriveConsistency(currentStreak);

        assertThat(consistency)
                .as("Consistency for streak %d (>= 30) should be capped at 1.0", currentStreak)
                .isEqualTo(1.0);
    }
}
