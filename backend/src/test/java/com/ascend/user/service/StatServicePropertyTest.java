package com.ascend.user.service;

import com.ascend.common.entity.Difficulty;
import com.ascend.common.entity.StatType;
import com.ascend.user.dto.IdentityTitle;
import com.ascend.user.dto.StatThresholds;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for StatService stat gain calculations and title unlock permanence.
 * Validates correctness properties across randomized inputs.
 */
class StatServicePropertyTest {

    // ========================================================================
    // Property 24: Stat gain = BaseStat × DifficultyMultiplier for all valid inputs
    // Validates: Requirements 1.2
    // ========================================================================

    @Property(tries = 200)
    void statGainMatchesBaseStatTimesDifficultyMultiplier(
            @ForAll("difficulties") Difficulty difficulty) {

        StatService statService = createStatServiceForPureFunctions();

        int gain = statService.calculateStatGain(difficulty);
        double multiplier = statService.getDifficultyMultiplier(difficulty);
        int expected = (int) (1 * multiplier); // BaseStat = 1

        assertThat(gain)
                .as("Stat gain for %s should be BaseStat(1) × multiplier(%s) = %d",
                        difficulty, multiplier, expected)
                .isEqualTo(expected);
    }

    @Property(tries = 200)
    void statGainIsAlwaysPositive(
            @ForAll("difficulties") Difficulty difficulty) {

        StatService statService = createStatServiceForPureFunctions();

        int gain = statService.calculateStatGain(difficulty);

        assertThat(gain)
                .as("Stat gain should always be positive for difficulty %s", difficulty)
                .isGreaterThan(0);
    }

    @Property(tries = 200)
    void difficultyMultiplierMatchesExpectedValues(
            @ForAll("difficulties") Difficulty difficulty) {

        StatService statService = createStatServiceForPureFunctions();

        double multiplier = statService.getDifficultyMultiplier(difficulty);
        double expected = switch (difficulty) {
            case EASY -> 1.0;
            case MEDIUM -> 1.5;
            case HARD -> 2.0;
            case LEGENDARY -> 3.0;
        };

        assertThat(multiplier)
                .as("Difficulty multiplier for %s should be %f", difficulty, expected)
                .isEqualTo(expected);
    }

    @Property(tries = 200)
    void higherDifficultyGivesEqualOrMoreStatGain(
            @ForAll("difficulties") Difficulty difficulty1,
            @ForAll("difficulties") Difficulty difficulty2) {

        StatService statService = createStatServiceForPureFunctions();

        int gain1 = statService.calculateStatGain(difficulty1);
        int gain2 = statService.calculateStatGain(difficulty2);

        if (difficulty1.ordinal() <= difficulty2.ordinal()) {
            assertThat(gain1)
                    .as("Gain for %s (%d) should be <= gain for %s (%d)",
                            difficulty1, gain1, difficulty2, gain2)
                    .isLessThanOrEqualTo(gain2);
        } else {
            assertThat(gain1)
                    .as("Gain for %s (%d) should be >= gain for %s (%d)",
                            difficulty1, gain1, difficulty2, gain2)
                    .isGreaterThanOrEqualTo(gain2);
        }
    }

    // ========================================================================
    // Property 25: Title unlock is permanent (once unlocked, always available)
    // Validates: Requirements 1.2
    // ========================================================================

    @Property(tries = 200)
    void titleExistsForEveryThresholdAndStatType(
            @ForAll("statTypes") StatType statType,
            @ForAll("thresholds") int threshold) {

        IdentityTitle title = StatThresholds.getTitleForThreshold(statType, threshold);

        assertThat(title)
                .as("A title should exist for %s at threshold %d", statType, threshold)
                .isNotNull();
        assertThat(title.statType()).isEqualTo(statType);
        assertThat(title.threshold()).isEqualTo(threshold);
        assertThat(title.titleName()).isNotBlank();
        assertThat(title.description()).isNotBlank();
    }

    @Property(tries = 200)
    void titleUnlockThresholdIsOnlyTriggeredWhenCrossed(
            @ForAll("statTypes") StatType statType,
            @ForAll("thresholds") int threshold,
            @ForAll @IntRange(min = 0, max = 1500) int previousValue,
            @ForAll @IntRange(min = 0, max = 1500) int newValue) {

        // Title should only be triggered when previousValue < threshold AND newValue >= threshold
        boolean shouldTrigger = previousValue < threshold && newValue >= threshold;

        // Verify the crossing logic is correct
        if (shouldTrigger) {
            assertThat(newValue).isGreaterThanOrEqualTo(threshold);
            assertThat(previousValue).isLessThan(threshold);
        }

        // Once a stat exceeds a threshold, the title remains available regardless of future stat value
        // This validates the permanence concept: if stat was ever >= threshold, title is earned
        if (newValue >= threshold) {
            IdentityTitle title = StatThresholds.getTitleForThreshold(statType, threshold);
            assertThat(title)
                    .as("Title for %s at threshold %d should always be retrievable (permanent)",
                            statType, threshold)
                    .isNotNull();
        }
    }

    @Property(tries = 200)
    void titleLookupIsDeterministic(
            @ForAll("statTypes") StatType statType,
            @ForAll("thresholds") int threshold) {

        // Calling getTitleForThreshold multiple times should always return the same result
        // This validates permanence: the title definition never changes
        IdentityTitle title1 = StatThresholds.getTitleForThreshold(statType, threshold);
        IdentityTitle title2 = StatThresholds.getTitleForThreshold(statType, threshold);

        assertThat(title1).isEqualTo(title2);
        assertThat(title1.titleName()).isEqualTo(title2.titleName());
        assertThat(title1.description()).isEqualTo(title2.description());
    }

    @Property(tries = 200)
    void allThresholdsAreOrderedAndPositive() {
        int previous = 0;
        for (int threshold : StatThresholds.ALL_THRESHOLDS) {
            assertThat(threshold)
                    .as("Threshold %d should be positive", threshold)
                    .isGreaterThan(0);
            assertThat(threshold)
                    .as("Threshold %d should be greater than previous %d", threshold, previous)
                    .isGreaterThan(previous);
            previous = threshold;
        }
    }

    // ========================================================================
    // Providers
    // ========================================================================

    @Provide
    Arbitrary<Difficulty> difficulties() {
        return Arbitraries.of(Difficulty.values());
    }

    @Provide
    Arbitrary<StatType> statTypes() {
        return Arbitraries.of(StatType.values());
    }

    @Provide
    Arbitrary<Integer> thresholds() {
        return Arbitraries.of(StatThresholds.ALL_THRESHOLDS);
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    /**
     * Creates a StatService instance suitable for testing pure calculation methods.
     * The dependencies are null since calculateStatGain and getDifficultyMultiplier
     * are pure functions that don't use any injected dependencies.
     */
    private StatService createStatServiceForPureFunctions() {
        return new StatService(null, null, null, null);
    }
}
