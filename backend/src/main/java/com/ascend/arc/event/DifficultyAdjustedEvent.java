package com.ascend.arc.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Domain event published when adaptive difficulty adjusts the quest difficulty for an arc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DifficultyAdjustedEvent {

    private UUID userId;
    private UUID arcId;
    private String previousDifficulty;
    private String newDifficulty;
    private String reason;
}
