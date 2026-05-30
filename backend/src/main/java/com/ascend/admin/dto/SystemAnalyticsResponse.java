package com.ascend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemAnalyticsResponse {

    private long dau;
    private long wau;
    private long mau;
    private double retentionRate;
    private double premiumConversion;
    private double churnRate;
    private double streakSurvivalRate;
    private double avgSessionLength;
}
