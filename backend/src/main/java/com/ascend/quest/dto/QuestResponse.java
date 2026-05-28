package com.ascend.quest.dto;

import com.ascend.common.entity.Difficulty;
import com.ascend.common.entity.Frequency;
import com.ascend.common.entity.StatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestResponse {

    private UUID id;
    private String title;
    private String description;
    private int xpReward;
    private Difficulty difficulty;
    private StatType statType;
    private Frequency frequency;
    private boolean recurring;
    private boolean isCustom;
    private boolean completed;
}
