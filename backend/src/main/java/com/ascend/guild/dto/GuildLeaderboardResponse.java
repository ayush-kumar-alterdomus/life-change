package com.ascend.guild.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuildLeaderboardResponse {

    private List<GuildLeaderboardEntry> entries;
    private int totalGuilds;
    private int currentPage;
    private int totalPages;
}
