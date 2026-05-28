package com.ascend.xp.service;

/**
 * Pure utility class for level progression calculations.
 * All methods are static and side-effect free.
 */
public final class LevelCalculator {

    private LevelCalculator() {
        // Utility class — no instantiation
    }

    /**
     * Returns the XP required to reach a specific level (not cumulative).
     * <p>
     * Formula: floor(100 × level^1.5)
     *
     * @param level the target level (must be >= 1)
     * @return XP required for that level
     * @throws IllegalArgumentException if level is less than 1
     */
    public static long xpRequiredForLevel(int level) {
        if (level < 1) {
            throw new IllegalArgumentException("level must be >= 1, got: " + level);
        }
        return (long) Math.floor(100.0 * Math.pow(level, 1.5));
    }

    /**
     * Calculates the cumulative XP required to reach a given level
     * (sum of xpRequiredForLevel from 1 to level).
     *
     * @param level the target level (must be >= 1)
     * @return total cumulative XP needed to reach this level
     */
    private static long cumulativeXpForLevel(int level) {
        long total = 0;
        for (int i = 1; i <= level; i++) {
            total += xpRequiredForLevel(i);
        }
        return total;
    }

    /**
     * Calculates the highest level achievable with the given total XP.
     * <p>
     * Returns the highest level where cumulative XP requirement <= totalXp.
     *
     * @param totalXp the user's total accumulated XP (must be >= 0)
     * @return the calculated level (minimum 0 if totalXp is less than level 1 requirement)
     * @throws IllegalArgumentException if totalXp is negative
     */
    public static int calculateLevel(long totalXp) {
        if (totalXp < 0) {
            throw new IllegalArgumentException("totalXp must be non-negative, got: " + totalXp);
        }

        int level = 0;
        long cumulativeXp = 0;

        while (true) {
            long nextLevelXp = xpRequiredForLevel(level + 1);
            if (cumulativeXp + nextLevelXp > totalXp) {
                break;
            }
            cumulativeXp += nextLevelXp;
            level++;
        }

        return level;
    }

    /**
     * Calculates the remaining XP needed to reach the next level.
     *
     * @param currentLevel the user's current level (must be >= 0)
     * @param totalXp      the user's total accumulated XP (must be >= 0)
     * @return XP remaining until the next level-up
     * @throws IllegalArgumentException if inputs are invalid
     */
    public static long xpToNextLevel(int currentLevel, long totalXp) {
        if (currentLevel < 0) {
            throw new IllegalArgumentException("currentLevel must be non-negative, got: " + currentLevel);
        }
        if (totalXp < 0) {
            throw new IllegalArgumentException("totalXp must be non-negative, got: " + totalXp);
        }

        // Calculate cumulative XP needed to reach current level
        long cumulativeForCurrent = cumulativeXpForLevel(currentLevel);
        // XP needed for the next level
        long nextLevelRequirement = xpRequiredForLevel(currentLevel + 1);
        // XP progress within current level
        long xpIntoCurrentLevel = totalXp - cumulativeForCurrent;

        return Math.max(0, nextLevelRequirement - xpIntoCurrentLevel);
    }
}
