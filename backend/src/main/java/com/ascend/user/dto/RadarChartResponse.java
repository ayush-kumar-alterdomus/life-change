package com.ascend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO containing stats formatted for radar chart display.
 * Each entry represents one axis of the radar chart with label and value.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RadarChartResponse {

    private List<RadarChartEntry> entries;
    private int maxValue;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RadarChartEntry {
        private String label;
        private String statType;
        private int value;
    }
}
