package com.ascend.guild.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuildLeaderboardEntry {

    private int rank;
    private UUID guildId;
    private String guildName;
    private int memberCount;
    private long guildXp;
    private double avgConsistency;
    private long totalQuestsCompleted;
    private double avgStreak;
    private double score;
}
