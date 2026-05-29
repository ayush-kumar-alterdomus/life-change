package com.ascend.streak.service;

import com.ascend.xp.service.ComboCalculator;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for ComboCalculator.
 * Validates that the combo multiplier stays within bounds for all valid inputs.
 *
 * **Validates: Requirements 1.2**
 */
class ComboCalculatorPropertyTest {

    // ========================================================================
    // Property 2: Combo multiplier always in [1.0, 2.0] for any non-negative streak
    // ========================================================================

    @Property(tries = 100)
    void comboMultiplierAlwaysInRange(
            @ForAll @IntRange(min = 0, max = 10000) int streakDays) {

        double multiplier = ComboCalculator.calculateComboMultiplier(streakDays);

        assertThat(multiplier)
                .as("Combo multiplier for streakDays=%d must be in [1.0, 2.0]", streakDays)
                .isGreaterThanOrEqualTo(1.0)
                .isLessThanOrEqualTo(2.0);
    }

    @Property(tries = 100)
    void comboMultiplierMatchesFormula(
            @ForAll @IntRange(min = 0, max = 10000) int streakDays) {

        double multiplier = ComboCalculator.calculateComboMultiplier(streakDays);
        double expected = Math.min(1.0 + 0.01 * streakDays, 2.0);

        assertThat(multiplier)
                .as("Combo multiplier for streakDays=%d should match formula", streakDays)
                .isEqualTo(expected);
    }

    @Property(tries = 100)
    void comboMultiplierIsMonotonic(
            @ForAll @IntRange(min = 0, max = 10000) int days1,
            @ForAll @IntRange(min = 0, max = 10000) int days2) {

        double m1 = ComboCalculator.calculateComboMultiplier(days1);
        double m2 = ComboCalculator.calculateComboMultiplier(days2);

        if (days1 <= days2) {
            assertThat(m1)
                    .as("comboMultiplier should be monotonic: f(%d)=%f <= f(%d)=%f", days1, m1, days2, m2)
                    .isLessThanOrEqualTo(m2);
        } else {
            assertThat(m1)
                    .as("comboMultiplier should be monotonic: f(%d)=%f >= f(%d)=%f", days1, m1, days2, m2)
                    .isGreaterThanOrEqualTo(m2);
        }
    }
}
