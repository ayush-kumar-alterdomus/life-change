package com.ascend.skilltree.dto;

import com.ascend.common.entity.StatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillNodeResponse {

    private UUID id;
    private String name;
    private String description;
    private StatType statType;
    private double buffPercent;
    private UUID parentNodeId;
    private boolean unlocked;
    private LocalDateTime unlockedAt;

    @Builder.Default
    private List<SkillNodeResponse> children = new ArrayList<>();
}
