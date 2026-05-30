package com.ascend.aicoach.dto;

import java.util.List;

public record BurnoutRiskResponse(
        double riskScore,
        String riskLevel,
        List<String> factors
) {}
