package com.ascend.dashboard.dto;

public record DashboardXpSection(
        long totalXp,
        int level,
        long xpToNextLevel,
        long dailyXpEarned,
        int dailyCap,
        double comboMultiplier
) {
}
