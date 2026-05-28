package com.ascend.xp.service;

/**
 * Pure utility class for streak combo multiplier calculation.
 * All methods are static and side-effect free.
 */
public final class ComboCalculator {

    /** Maximum combo multiplier cap. */
    private static final double MAX_MULTIPLIER = 2.0;

    /** Multiplier increment per streak day. */
    private static final double INCREMENT_PER_DAY = 0.01;

    private ComboCalculator() {
        // Utility class — no instantiation
    }

    /**
     * Calculates the combo multiplier based on the user's current streak days.
     * <p>
     * Formula: min(1 + 0.01 × streakDays, 2.0)
     *
     * @param streakDays number of consecutive streak days (must be >= 0)
     * @return combo multiplier in the range [1.0, 2.0]
     * @throws IllegalArgumentException if streakDays is negative
     */
    public static double calculateComboMultiplier(int streakDays) {
        if (streakDays < 0) {
            throw new IllegalArgumentException("streakDays must be non-negative, got: " + streakDays);
        }
        return Math.min(1.0 + INCREMENT_PER_DAY * streakDays, MAX_MULTIPLIER);
    }
}
