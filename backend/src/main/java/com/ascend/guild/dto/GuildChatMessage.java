package com.ascend.guild.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuildChatMessage {

    private UUID id;
    private UUID guildId;
    private UUID userId;
    private String username;
    private String message;
    private LocalDateTime timestamp;
}
