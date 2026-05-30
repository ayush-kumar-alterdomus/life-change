package com.ascend.user.dto;

import java.util.List;

public record OnboardingStatusResponse(
        boolean complete,
        List<String> selectedGoals,
        String personalityType,
        String difficulty
) {
}
