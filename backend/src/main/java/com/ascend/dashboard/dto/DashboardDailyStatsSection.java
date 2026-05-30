package com.ascend.dashboard.dto;

public record DashboardDailyStatsSection(
        int questsCompleted,
        int questsTotal,
        int completionPercentage
) {
}
