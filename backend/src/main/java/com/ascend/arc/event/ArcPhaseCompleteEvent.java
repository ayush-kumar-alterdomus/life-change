package com.ascend.arc.event;

import com.ascend.arc.dto.ArcPhase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Domain event published when a user transitions to a new phase within an arc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArcPhaseCompleteEvent {

    private UUID userId;
    private UUID arcId;
    private ArcPhase previousPhase;
    private ArcPhase newPhase;
    private Integer progressPercent;
}
