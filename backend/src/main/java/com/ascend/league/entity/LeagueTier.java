package com.ascend.league.entity;

import lombok.Getter;

@Getter
public enum LeagueTier {

    BRONZE(0),
    SILVER(10),
    GOLD(20),
    PLATINUM(35),
    DIAMOND(50),
    MASTER(75),
    ASCENDANT(Integer.MAX_VALUE); // invite-only

    private final int minLevel;

    LeagueTier(int minLevel) {
        this.minLevel = minLevel;
    }

    /**
     * Determines the appropriate tier for a given user level.
     * ASCENDANT is invite-only and cannot be reached by level alone.
     */
    public static LeagueTier fromLevel(int level) {
        LeagueTier result = BRONZE;
        for (LeagueTier tier : values()) {
            if (tier == ASCENDANT) {
                break;
            }
            if (level >= tier.minLevel) {
                result = tier;
            }
        }
        return result;
    }
}
