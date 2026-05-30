package com.ascend.boss.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response DTO for boss information with user's current progress.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BossResponse {

    private UUID id;
    private String name;
    private String description;
    private int totalStages;
    private int currentStage;
    private int progressPercent;
    private boolean defeated;
    private int rewardXp;
    private String rewardTitle;
}
