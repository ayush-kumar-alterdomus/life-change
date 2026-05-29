package com.ascend.xp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XpSummaryResponse {

    private long totalXp;
    private int level;
    private long xpToNextLevel;
    private long dailyXpEarned;
    private int dailyCap;
    private int prestigeLevel;
    private double comboMultiplier;
}
