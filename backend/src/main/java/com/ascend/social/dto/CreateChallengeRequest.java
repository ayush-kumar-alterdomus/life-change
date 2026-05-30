package com.ascend.social.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request DTO for creating a new challenge.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateChallengeRequest {

    @NotNull(message = "Friend ID is required")
    private UUID friendId;

    @NotBlank(message = "Title is required")
    private String title;

    @Min(value = 1, message = "Target must be at least 1")
    private int target;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDateTime endsAt;
}
