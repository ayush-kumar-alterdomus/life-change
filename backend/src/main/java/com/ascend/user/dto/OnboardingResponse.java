package com.ascend.user.dto;

public record OnboardingResponse(
        int level,
        boolean arcStarted,
        String arcId,
        String arcName
) {
}
