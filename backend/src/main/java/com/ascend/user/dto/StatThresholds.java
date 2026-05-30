package com.ascend.user.dto;

import com.ascend.common.entity.StatType;

import java.util.List;
import java.util.Map;

/**
 * Constants class defining identity title unlock thresholds per stat type.
 * Each threshold awards a permanent identity title when a stat reaches that value.
 */
public final class StatThresholds {

    private StatThresholds() {
        // Utility class — no instantiation
    }

    public static final int BEGINNER_THRESHOLD = 100;
    public static final int DEDICATED_THRESHOLD = 250;
    public static final int SPECIALIST_THRESHOLD = 500;
    public static final int MASTER_THRESHOLD = 1000;

    public static final List<Integer> ALL_THRESHOLDS = List.of(
            BEGINNER_THRESHOLD,
            DEDICATED_THRESHOLD,
            SPECIALIST_THRESHOLD,
            MASTER_THRESHOLD
    );

    /**
     * Stat-specific title names at the 500 threshold.
     */
    private static final Map<StatType, String> SPECIALIST_TITLES = Map.of(
            StatType.STRENGTH, "The Strong One",
            StatType.WISDOM, "The Wise One",
            StatType.FOCUS, "The Focused One",
            StatType.DISCIPLINE, "The Disciplined One",
            StatType.VITALITY, "The Vital One",
            StatType.CHARISMA, "The Charismatic One"
    );

    /**
     * Returns the identity title for a given stat type and threshold.
     *
     * @param statType  the stat that reached the threshold
     * @param threshold the threshold value crossed
     * @return the IdentityTitle, or null if the threshold is not recognized
     */
    public static IdentityTitle getTitleForThreshold(StatType statType, int threshold) {
        return switch (threshold) {
            case BEGINNER_THRESHOLD -> new IdentityTitle(
                    statType,
                    BEGINNER_THRESHOLD,
                    "The Beginner",
                    "Reached " + BEGINNER_THRESHOLD + " " + formatStatName(statType) + " points"
            );
            case DEDICATED_THRESHOLD -> new IdentityTitle(
                    statType,
                    DEDICATED_THRESHOLD,
                    "The Dedicated",
                    "Reached " + DEDICATED_THRESHOLD + " " + formatStatName(statType) + " points"
            );
            case SPECIALIST_THRESHOLD -> new IdentityTitle(
                    statType,
                    SPECIALIST_THRESHOLD,
                    SPECIALIST_TITLES.get(statType),
                    "Reached " + SPECIALIST_THRESHOLD + " " + formatStatName(statType) + " points"
            );
            case MASTER_THRESHOLD -> new IdentityTitle(
                    statType,
                    MASTER_THRESHOLD,
                    "The Master",
                    "Reached " + MASTER_THRESHOLD + " " + formatStatName(statType) + " points"
            );
            default -> null;
        };
    }

    /**
     * Formats a StatType enum value into a human-readable name.
     */
    private static String formatStatName(StatType statType) {
        String name = statType.name();
        return name.charAt(0) + name.substring(1).toLowerCase();
    }
}
