package com.ascend.aicoach.dto;

import java.util.List;

public record CoachRecommendationResponse(
        List<String> recommendations,
        double burnoutRisk,
        boolean recoveryModeActive,
        String optimalQuestTime,
        String difficultyAdjustment
) {}
