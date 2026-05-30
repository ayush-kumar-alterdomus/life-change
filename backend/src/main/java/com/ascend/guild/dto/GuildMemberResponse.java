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
public class GuildMemberResponse {

    private UUID userId;
    private String username;
    private String avatarUrl;
    private String role;
    private LocalDateTime joinedAt;
    private long weeklyContribution;
}
