package com.ascend.streak.dto;

/**
 * Streak milestone thresholds with associated XP bonus values.
 */
public enum StreakMilestone {

    WEEK(7, 50),
    TWO_WEEKS(14, 100),
    MONTH(30, 250),
    QUARTER(90, 500),
    YEAR(365, 2000);

    private final int days;
    private final int xpBonus;

    StreakMilestone(int days, int xpBonus) {
        this.days = days;
        this.xpBonus = xpBonus;
    }

    public int getDays() {
        return days;
    }

    public int getXpBonus() {
        return xpBonus;
    }
}
