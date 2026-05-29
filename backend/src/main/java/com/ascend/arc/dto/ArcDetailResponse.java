package com.ascend.arc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ArcDetailResponse extends ArcResponse {

    private List<MilestoneResponse> milestones;
    private String questFrequency;
    private String skillTreePath;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MilestoneResponse {
        private UUID id;
        private String title;
        private String description;
        private Integer orderIndex;
        private Integer xpReward;
    }
}
