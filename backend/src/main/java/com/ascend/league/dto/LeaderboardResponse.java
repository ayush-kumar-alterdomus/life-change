package com.ascend.league.dto;

import com.ascend.league.entity.LeagueTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardResponse {

    private List<LeaderboardEntry> entries;
    private int userRank;
    private int totalUsers;
    private LeagueTier league;
}
