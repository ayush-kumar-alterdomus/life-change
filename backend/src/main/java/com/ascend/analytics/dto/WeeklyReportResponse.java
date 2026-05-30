package com.ascend.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for the weekly progress report containing quest stats,
 * XP earned, strongest/weakest stats, recommendations, and Life Score.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyReportResponse {

    private int questsCompleted;
    private int questsMissed;
    private long xpEarned;
    private String strongestStat;
    private String weakestStat;
    private List<String> recommendations;
    private BigDecimal lifeScore;
}
