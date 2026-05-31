package com.ascend.dashboard.dto;

public record DashboardUserSection(
        String displayName,
        int level,
        String avatarUrl,
        boolean premium,
        String league
) {
}
