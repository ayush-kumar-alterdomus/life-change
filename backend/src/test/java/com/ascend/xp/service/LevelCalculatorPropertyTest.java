package com.ascend.xp.service;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.LongRange;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for level progression and prestige calculations.
 * Validates correctness properties across randomized inputs.
 */
class LevelCalculatorPropertyTest {

    // ========================================================================
    // Property 7: Level formula correctness
    // ========================================================================

    @Property(tries = 200)
    void xpRequiredForLevelMatchesFormula(
            @ForAll @IntRange(min = 1, max = 200) int level) {

        long result = LevelCalculator.xpRequiredForLevel(level);
        long expected = (long) Math.floor(100.0 * Math.pow(level, 1.5));

        assertThat(result).isEqualTo(expected);
    }

    @Property(tries = 200)
    void xpRequiredForLevelIsAlwaysPositive(
            @ForAll @IntRange(min = 1, max = 200) int level) {

        long result = LevelCalculator.xpRequiredForLevel(level);

        assertThat(result).isGreaterThan(0);
    }

    @Property(tries = 200)
    void xpRequiredForLevelIsMonotonic(
            @ForAll @IntRange(min = 1, max = 100) int level1,
            @ForAll @IntRange(min = 1, max = 100) int level2) {

        long xp1 = LevelCalculator.xpRequiredForLevel(level1);
        long xp2 = LevelCalculator.xpRequiredForLevel(level2);

        if (level1 <= level2) {
            assertThat(xp1).isLessThanOrEqualTo(xp2);
        } else {
            assertThat(xp1).isGreaterThanOrEqualTo(xp2);
        }
    }

    @Property(tries = 200)
    void calculateLevelIsConsistentWithXpRequired(
            @ForAll @IntRange(min = 1, max = 50) int targetLevel) {

        // Calculate cumulative XP needed to reach targetLevel
        long cumulativeXp = 0;
        for (int i = 1; i <= targetLevel; i++) {
            cumulativeXp += LevelCalculator.xpRequiredForLevel(i);
        }

        // With exactly the cumulative XP, user should be at targetLevel
        int calculatedLevel = LevelCalculator.calculateLevel(cumulativeXp);
        assertThat(calculatedLevel).isEqualTo(targetLevel);

        // With one less XP, user should be at targetLevel - 1
        if (cumulativeXp > 0) {
            int levelBelow = LevelCalculator.calculateLevel(cumulativeXp - 1);
            assertThat(levelBelow).isEqualTo(targetLevel - 1);
        }
    }

    @Property(tries = 200)
    void calculateLevelIsMonotonic(
            @ForAll @LongRange(min = 0, max = 1000000) long xp1,
            @ForAll @LongRange(min = 0, max = 1000000) long xp2) {

        int level1 = LevelCalculator.calculateLevel(xp1);
        int level2 = LevelCalculator.calculateLevel(xp2);

        if (xp1 <= xp2) {
            assertThat(level1).isLessThanOrEqualTo(level2);
        } else {
            assertThat(level1).isGreaterThanOrEqualTo(level2);
        }
    }

    @Property(tries = 200)
    void calculateLevelIsNonNegative(
            @ForAll @LongRange(min = 0, max = 1000000) long totalXp) {

        int level = LevelCalculator.calculateLevel(totalXp);

        assertThat(level).isGreaterThanOrEqualTo(0);
    }

    @Property(tries = 200)
    void xpToNextLevelIsAlwaysPositive(
            @ForAll @IntRange(min = 0, max = 50) int currentLevel,
            @ForAll @LongRange(min = 0, max = 500000) long totalXp) {

        long xpNeeded = LevelCalculator.xpToNextLevel(currentLevel, totalXp);

        assertThat(xpNeeded).isGreaterThanOrEqualTo(0);
    }

    // ========================================================================
    // Property 8: Prestige multiplier correctness
    // ========================================================================

    @Property(tries = 200)
    void prestigeMultiplierMatchesFormula(
            @ForAll @IntRange(min = 0, max = 100) int prestigeLevel) {

        double result = PrestigeService.getPrestigeMultiplier(prestigeLevel);
        double expected = 1.0 + (0.1 * prestigeLevel);

        assertThat(result).isEqualTo(expected);
    }

    @Property(tries = 200)
    void prestigeMultiplierIsAtLeastOne(
            @ForAll @IntRange(min = 0, max = 100) int prestigeLevel) {

        double result = PrestigeService.getPrestigeMultiplier(prestigeLevel);

        assertThat(result).isGreaterThanOrEqualTo(1.0);
    }

    @Property(tries = 200)
    void prestigeMultiplierIsMonotonic(
            @ForAll @IntRange(min = 0, max = 50) int prestige1,
            @ForAll @IntRange(min = 0, max = 50) int prestige2) {

        double m1 = PrestigeService.getPrestigeMultiplier(prestige1);
        double m2 = PrestigeService.getPrestigeMultiplier(prestige2);

        if (prestige1 <= prestige2) {
            assertThat(m1).isLessThanOrEqualTo(m2);
        } else {
            assertThat(m1).isGreaterThanOrEqualTo(m2);
        }
    }

    @Property(tries = 200)
    void prestigeZeroGivesNoBonus() {
        double result = PrestigeService.getPrestigeMultiplier(0);
        assertThat(result).isEqualTo(1.0);
    }
}
