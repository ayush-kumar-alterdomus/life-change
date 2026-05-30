package com.ascend.skilltree.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Domain event published when a user unlocks a skill node.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillUnlockedEvent {

    private UUID userId;
    private UUID skillNodeId;
    private UUID arcId;
    private String skillName;
    private String statType;
    private double buffPercent;
}
