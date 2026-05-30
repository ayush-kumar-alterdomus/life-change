package com.ascend.guild.service;

import com.ascend.guild.dto.CreateGuildRequest;
import com.ascend.guild.dto.GuildChatMessage;
import com.ascend.guild.dto.GuildDetailResponse;
import com.ascend.guild.dto.GuildResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuildService {

    public GuildResponse createGuild(UUID userId, CreateGuildRequest request) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public List<GuildResponse> listGuilds(String type, int page) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public GuildDetailResponse getGuildDetail(UUID guildId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void joinGuild(UUID userId, UUID guildId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void leaveGuild(UUID userId, UUID guildId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public List<GuildChatMessage> getChatHistory(UUID guildId, int page, int size) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
