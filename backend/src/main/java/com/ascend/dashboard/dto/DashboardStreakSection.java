package com.ascend.dashboard.dto;

public record DashboardStreakSection(
        int currentStreak,
        int longestStreak,
        boolean shieldAvailable,
        boolean comebackModeActive
) {
}
