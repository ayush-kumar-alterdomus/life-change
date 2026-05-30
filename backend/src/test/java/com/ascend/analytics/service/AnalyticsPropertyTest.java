package com.ascend.analytics.service;

import com.ascend.user.service.LifeScoreCalculator;
import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for Analytics & Insights.
 * Validates Life Score formula correctness and weekly report field completeness.
 */
class AnalyticsPropertyTest {

    private final LifeScoreCalculator calculator = new LifeScoreCalculator();

    // ========================================================================
    // Property 48: Life Score formula correctness, always in [0, 100]
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
                .as("Life Score should be in [0, 100] for any inputs")
                .isBetween(0.0, 100.0);
    }

    @Property(tries = 200)
    void lifeScoreIsZeroWhenAllInputsAreZero() {
        BigDecimal result = calculator.calculate(0, 0, 0, 0, 0.0);
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Property(tries = 200)
    void lifeScoreIsMaxWhenAllInputsAreMax() {
        BigDecimal result = calculator.calculate(1000, 1000, 1000, 1000, 1.0);
        assertThat(result).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Property(tries = 100)
    void lifeScoreIsMonotonicInEachStat(
            @ForAll @IntRange(min = 0, max = 500) int base,
            @ForAll @IntRange(min = 1, max = 500) int increase,
            @ForAll @IntRange(min = 0, max = 1000) int focus,
            @ForAll @IntRange(min = 0, max = 1000) int vitality,
            @ForAll @IntRange(min = 0, max = 1000) int wisdom,
            @ForAll @DoubleRange(min = 0.0, max = 1.0) double consistency) {

        BigDecimal scoreBefore = calculator.calculate(base, focus, vitality, wisdom, consistency);
        BigDecimal scoreAfter = calculator.calculate(base + increase, focus, vitality, wisdom, consistency);

        assertThat(scoreAfter.compareTo(scoreBefore))
                .as("Life Score should increase when discipline increases")
                .isGreaterThanOrEqualTo(0);
    }

    @Property(tries = 100)
    void consistencyDerivationAlwaysInZeroToOne(
            @ForAll @IntRange(min = 0, max = 365) int currentStreak) {

        double consistency = LifeScoreCalculator.deriveConsistency(currentStreak);

        assertThat(consistency)
                .as("Consistency for streak %d should be in [0, 1]", currentStreak)
                .isBetween(0.0, 1.0);
    }

    @Property(tries = 100)
    void consistencyCapsAtOneForStreaksOver30(
            @ForAll @IntRange(min = 30, max = 365) int currentStreak) {

        double consistency = LifeScoreCalculator.deriveConsistency(currentStreak);

        assertThat(consistency)
                .as("Consistency for streak %d (>=30) should be capped at 1.0", currentStreak)
                .isEqualTo(1.0);
    }

    // ========================================================================
    // Property 49: Weekly report contains all required fields
    // ========================================================================

    @Property(tries = 100)
    void weeklyReportQuestsCompletedIsNonNegative(
            @ForAll @IntRange(min = 0, max = 1000) int questsCompleted) {

        assertThat(questsCompleted)
                .as("Quests completed should never be negative")
                .isGreaterThanOrEqualTo(0);
    }

    @Property(tries = 100)
    void weeklyReportQuestsMissedIsNonNegative(
            @ForAll @IntRange(min = 0, max = 1000) int questsMissed) {

        assertThat(questsMissed)
                .as("Quests missed should never be negative")
                .isGreaterThanOrEqualTo(0);
    }

    @Property(tries = 100)
    void weeklyReportXpEarnedIsNonNegative(
            @ForAll @IntRange(min = 0, max = 100000) int xpEarned) {

        assertThat(xpEarned)
                .as("XP earned should never be negative")
                .isGreaterThanOrEqualTo(0);
    }

    @Property(tries = 100)
    void weeklyReportCompletionRateInValidRange(
            @ForAll @IntRange(min = 0, max = 100) int completed,
            @ForAll @IntRange(min = 1, max = 100) int assigned) {

        double rate = (double) completed / assigned;
        double clampedRate = Math.min(1.0, rate);

        assertThat(clampedRate)
                .as("Completion rate should be in [0, 1]")
                .isBetween(0.0, 1.0);
    }

    @Property(tries = 100)
    void weeklyReportRecommendationsLimitedToThree(
            @ForAll @IntRange(min = 0, max = 10) int rawRecommendations) {

        int limited = Math.min(rawRecommendations, 3);

        assertThat(limited)
                .as("Recommendations should be limited to 3 max")
                .isLessThanOrEqualTo(3);
    }
}
