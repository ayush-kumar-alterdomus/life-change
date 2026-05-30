package com.ascend.boss.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Detailed boss response including stage thresholds and contributors (for guild bosses).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BossDetailResponse {

    private UUID id;
    private String name;
    private String description;
    private int totalStages;
    private int currentStage;
    private int progressPercent;
    private boolean defeated;
    private int rewardXp;
    private String rewardTitle;
    private String rewardCosmetic;
    private List<Integer> stageThresholds;
    private List<BossContributor> contributors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BossContributor {
        private UUID userId;
        private String username;
        private int damageContributed;
    }
}
