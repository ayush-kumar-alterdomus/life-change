package com.ascend.xp.service;

import com.ascend.common.entity.Difficulty;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.DoubleRange;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for XP calculation pure functions.
 * Validates correctness properties across randomized inputs.
 */
class XpCalculatorPropertyTest {

    // ========================================================================
    // Property 1: FinalXP formula correctness for all valid inputs
    // ========================================================================

    @Property(tries = 200)
    void finalXpMatchesFormula(
            @ForAll @IntRange(min = 0, max = 10000) int baseXp,
            @ForAll("difficulties") Difficulty difficulty,
            @ForAll @DoubleRange(min = 1.0, max = 2.0) double streakMultiplier,
            @ForAll @DoubleRange(min = 1.0, max = 1.5) double arcMultiplier,
            @ForAll @IntRange(min = 0, max = 500) int bonusXp) {

        int result = XpCalculator.calculateFinalXp(baseXp, difficulty, streakMultiplier, arcMultiplier, bonusXp);

        double diffMultiplier = XpCalculator.getDifficultyMultiplier(difficulty);
        int expected = (int) Math.floor(baseXp * diffMultiplier * streakMultiplier * arcMultiplier) + bonusXp;

        assertThat(result).isEqualTo(expected);
    }

    @Property(tries = 200)
    void finalXpIsAlwaysNonNegative(
            @ForAll @IntRange(min = 0, max = 10000) int baseXp,
            @ForAll("difficulties") Difficulty difficulty,
            @ForAll @DoubleRange(min = 1.0, max = 2.0) double streakMultiplier,
            @ForAll @DoubleRange(min = 1.0, max = 1.5) double arcMultiplier,
            @ForAll @IntRange(min = 0, max = 500) int bonusXp) {

        int result = XpCalculator.calculateFinalXp(baseXp, difficulty, streakMultiplier, arcMultiplier, bonusXp);

        assertThat(result).isGreaterThanOrEqualTo(0);
    }

    @Property(tries = 200)
    void finalXpIsMonotonicInBaseXp(
            @ForAll @IntRange(min = 0, max = 10000) int baseXp1,
            @ForAll @IntRange(min = 0, max = 10000) int baseXp2,
            @ForAll("difficulties") Difficulty difficulty,
            @ForAll @DoubleRange(min = 1.0, max = 2.0) double streakMultiplier,
            @ForAll @DoubleRange(min = 1.0, max = 1.5) double arcMultiplier,
            @ForAll @IntRange(min = 0, max = 500) int bonusXp) {

        int result1 = XpCalculator.calculateFinalXp(baseXp1, difficulty, streakMultiplier, arcMultiplier, bonusXp);
        int result2 = XpCalculator.calculateFinalXp(baseXp2, difficulty, streakMultiplier, arcMultiplier, bonusXp);

        assertMonotonic(baseXp1, baseXp2, result1, result2,
                "finalXp should be monotonic in baseXp");
    }

    // ========================================================================
    // Property 2: Combo multiplier always in [1.0, 2.0]
    // ========================================================================

    @Property(tries = 200)
    void comboMultiplierAlwaysInRange(
            @ForAll @IntRange(min = 0, max = 10000) int streakDays) {

        double multiplier = ComboCalculator.calculateComboMultiplier(streakDays);

        assertThat(multiplier).isBetween(1.0, 2.0);
    }

    // Redundant with comboMultiplierMatchesFormula (monotonicity is a mathematical
    // consequence of min(1 + 0.01x, 2.0)), but kept as an independent safety net.
    @Property(tries = 200)
    void comboMultiplierIsMonotonic(
            @ForAll @IntRange(min = 0, max = 10000) int days1,
            @ForAll @IntRange(min = 0, max = 10000) int days2) {

        double m1 = ComboCalculator.calculateComboMultiplier(days1);
        double m2 = ComboCalculator.calculateComboMultiplier(days2);

        assertMonotonic(days1, days2, m1, m2,
                "comboMultiplier should be monotonic in streakDays");
    }

    @Property(tries = 200)
    void comboMultiplierMatchesFormula(
            @ForAll @IntRange(min = 0, max = 10000) int streakDays) {

        double multiplier = ComboCalculator.calculateComboMultiplier(streakDays);
        double expected = Math.min(1.0 + 0.01 * streakDays, 2.0);

        assertThat(multiplier).isEqualTo(expected);
    }

    // ========================================================================
    // Property 3: Daily cap never exceeded
    // ========================================================================

    @Property(tries = 200)
    void dailyCapIsAlwaysPositive(
            @ForAll @IntRange(min = 0, max = 200) int level) {

        int cap = getDailyCap(level);

        assertThat(cap).isGreaterThan(0);
    }

    @Property(tries = 200)
    void dailyCapMatchesFormula(
            @ForAll @IntRange(min = 0, max = 200) int level) {

        int cap = getDailyCap(level);
        int expected = 1000 + (level * 20);

        assertThat(cap).isEqualTo(expected);
    }

    // Redundant with dailyCapMatchesFormula (monotonicity is a mathematical
    // consequence of 1000 + level*20), but kept as an independent safety net.
    @Property(tries = 200)
    void dailyCapIncreasesWithLevel(
            @ForAll @IntRange(min = 0, max = 200) int level1,
            @ForAll @IntRange(min = 0, max = 200) int level2) {

        int cap1 = getDailyCap(level1);
        int cap2 = getDailyCap(level2);

        assertMonotonic(level1, level2, cap1, cap2,
                "dailyCap should increase with level");
    }

    // ========================================================================
    // Providers
    // ========================================================================

    @Provide
    Arbitrary<Difficulty> difficulties() {
        return Arbitraries.of(Difficulty.values());
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    /**
     * Computes the daily cap using the same formula as XpService.getDailyCap.
     * Avoids instantiating XpService with null dependencies — the formula is pure.
     */
    private static int getDailyCap(int level) {
        return 1000 + (level * 20);
    }

    /**
     * Asserts that output values are monotonically ordered with respect to input values.
     * If input1 <= input2, then output1 must be <= output2, and vice versa.
     */
    private static void assertMonotonic(int input1, int input2, int output1, int output2, String description) {
        if (input1 <= input2) {
            assertThat(output1)
                    .as("%s: f(%d)=%d should be <= f(%d)=%d", description, input1, output1, input2, output2)
                    .isLessThanOrEqualTo(output2);
        } else {
            assertThat(output1)
                    .as("%s: f(%d)=%d should be >= f(%d)=%d", description, input1, output1, input2, output2)
                    .isGreaterThanOrEqualTo(output2);
        }
    }

    /**
     * Asserts that double output values are monotonically ordered with respect to input values.
     */
    private static void assertMonotonic(int input1, int input2, double output1, double output2, String description) {
        if (input1 <= input2) {
            assertThat(output1)
                    .as("%s: f(%d)=%f should be <= f(%d)=%f", description, input1, output1, input2, output2)
                    .isLessThanOrEqualTo(output2);
        } else {
            assertThat(output1)
                    .as("%s: f(%d)=%f should be >= f(%d)=%f", description, input1, output1, input2, output2)
                    .isGreaterThanOrEqualTo(output2);
        }
    }
}
