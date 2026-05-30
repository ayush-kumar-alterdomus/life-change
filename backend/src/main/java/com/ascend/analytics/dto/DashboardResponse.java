package com.ascend.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for the analytics dashboard containing XP growth,
 * level progress, quest completion rates, streak history, and stat trends.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private List<XpGrowthEntry> xpGrowth;
    private int levelGrowth;
    private double questCompletionRate;
    private List<StreakHistoryEntry> streakHistory;
    private Map<String, Double> statTrends;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class XpGrowthEntry {
        private String date;
        private long xpEarned;
        private long cumulativeXp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreakHistoryEntry {
        private String date;
        private int streakCount;
    }
}
