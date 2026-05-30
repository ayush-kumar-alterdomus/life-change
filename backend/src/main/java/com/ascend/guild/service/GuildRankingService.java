package com.ascend.guild.service;

import com.ascend.guild.dto.GuildResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuildRankingService {

    public List<GuildResponse> getGuildLeaderboard(int page) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
