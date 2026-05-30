package com.ascend.dashboard.dto;

public record DashboardArcSection(
        String id,
        String name,
        String arcType,
        int progressPercentage,
        String currentPhase
) {
}
