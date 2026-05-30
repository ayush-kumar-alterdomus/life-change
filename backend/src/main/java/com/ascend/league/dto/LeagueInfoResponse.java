package com.ascend.league.dto;

import com.ascend.league.entity.LeagueTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeagueInfoResponse {

    private LeagueTier currentTier;
    private double leagueScore;
    private int weeklyRank;
    private int promotionZone;
    private int demotionZone;
    private int groupSize;
}
