package com.ascend.league.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeagueHistoryResponse {

    private List<LeagueHistoryEntry> weeks;
    private int totalWeeksPlayed;
}
