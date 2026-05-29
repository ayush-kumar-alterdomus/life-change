package com.ascend.arc.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Domain event published when a user completes an entire arc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArcCompletedEvent {

    private UUID userId;
    private UUID arcId;
    private String arcName;
    private Long totalDays;
    private Integer completionXp;
}
