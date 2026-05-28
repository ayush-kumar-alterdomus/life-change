package com.ascend.xp.service;

import com.ascend.common.entity.Difficulty;

/**
 * Pure utility class for XP calculation.
 * All methods are static and side-effect free.
 */
public final class XpCalculator {

    private XpCalculator() {
        // Utility class — no instantiation
    }

    /**
     * Returns the XP multiplier for a given difficulty level.
     *
     * @param difficulty the quest difficulty
     * @return multiplier value (EASY=1.0, MEDIUM=1.5, HARD=2.0, LEGENDARY=3.0)
     */
    public static double getDifficultyMultiplier(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 1.0;
            case MEDIUM -> 1.5;
            case HARD -> 2.0;
            case LEGENDARY -> 3.0;
        };
    }

    /**
     * Calculates the final XP awarded for a quest completion.
     * <p>
     * Formula: FinalXP = floor(BaseXP × DifficultyMultiplier × StreakMultiplier × ArcMultiplier) + BonusXP
     *
     * @param baseXp           base XP reward (must be >= 0)
     * @param difficulty       quest difficulty level (non-null)
     * @param streakMultiplier multiplier from combo/streak (must be >= 1.0)
     * @param arcMultiplier    multiplier from active arc (must be >= 1.0)
     * @param bonusXp          flat bonus XP added after multiplication (must be >= 0)
     * @return calculated final XP as integer (floor)
     * @throws IllegalArgumentException if any input is invalid
     */
    public static int calculateFinalXp(int baseXp, Difficulty difficulty,
                                       double streakMultiplier, double arcMultiplier,
                                       int bonusXp) {
        if (baseXp < 0) {
            throw new IllegalArgumentException("baseXp must be non-negative, got: " + baseXp);
        }
        if (difficulty == null) {
            throw new IllegalArgumentException("difficulty must not be null");
        }
        if (streakMultiplier < 1.0) {
            throw new IllegalArgumentException("streakMultiplier must be >= 1.0, got: " + streakMultiplier);
        }
        if (arcMultiplier < 1.0) {
            throw new IllegalArgumentException("arcMultiplier must be >= 1.0, got: " + arcMultiplier);
        }
        if (bonusXp < 0) {
            throw new IllegalArgumentException("bonusXp must be non-negative, got: " + bonusXp);
        }

        double difficultyMultiplier = getDifficultyMultiplier(difficulty);
        double raw = baseXp * difficultyMultiplier * streakMultiplier * arcMultiplier;
        return (int) Math.floor(raw) + bonusXp;
    }
}
