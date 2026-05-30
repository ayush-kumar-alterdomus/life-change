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
public class LeagueHistoryEntry {

    private String seasonWeek;
    private LeagueTier tier;
    private double leagueScore;
    private int finalRank;
    private int groupSize;
    private String result; // "PROMOTED", "DEMOTED", "STAYED"
}
