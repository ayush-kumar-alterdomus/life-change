package com.ascend.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModerationRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Action is required")
    private ModerationAction action;

    private String reason;

    /** Duration in hours for SUSPEND action. */
    private Integer duration;
}
