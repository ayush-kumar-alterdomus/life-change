package com.ascend.arc.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateArcRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    @NotBlank(message = "Goal is required")
    @Size(max = 500, message = "Goal must not exceed 500 characters")
    private String goal;

    @NotNull(message = "Duration is required")
    @Min(value = 30, message = "Duration must be at least 30 days")
    @Max(value = 90, message = "Duration must not exceed 90 days")
    private Integer durationDays;

    @NotEmpty(message = "At least one milestone is required")
    private List<String> milestones;

    @NotNull(message = "Quest frequency is required")
    private String questFrequency;
}
