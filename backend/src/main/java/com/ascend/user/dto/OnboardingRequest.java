package com.ascend.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class OnboardingRequest {

    @NotEmpty
    private List<String> selectedGoals;

    @NotBlank
    private String difficulty;

    @NotBlank
    private String personalityType;

    @NotBlank
    private String selectedArc;

    @NotBlank
    private String selectedAvatar;
}
