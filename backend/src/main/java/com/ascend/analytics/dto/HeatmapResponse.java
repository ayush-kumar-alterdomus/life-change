package com.ascend.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO for the activity heatmap showing quest completions per day.
 * Similar to a GitHub contribution graph.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeatmapResponse {

    private List<HeatmapEntry> data;
    private LocalDate startDate;
    private LocalDate endDate;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HeatmapEntry {
        private LocalDate date;
        private int completionCount;
    }
}
