package com.ascend.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for a single habit correlation insight.
 * Contains the insight message, confidence level, category, and whether it's actionable.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsightResponse {

    private String message;
    private double confidence;
    private String category;
    private boolean actionable;
}
