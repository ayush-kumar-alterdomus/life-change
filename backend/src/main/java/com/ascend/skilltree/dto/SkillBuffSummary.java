package com.ascend.skilltree.dto;

import com.ascend.common.entity.StatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillBuffSummary {

    private StatType statType;
    private double totalBuffPercent;
}
