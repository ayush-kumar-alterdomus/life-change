package com.ascend.league.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntry {

    private int rank;
    private UUID userId;
    private String username;
    private String avatarUrl;
    private int level;
    private long weeklyXp;
    private double leagueScore;
    private int streak;
}
