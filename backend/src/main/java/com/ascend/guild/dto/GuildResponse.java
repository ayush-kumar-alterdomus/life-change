package com.ascend.guild.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class GuildResponse {

    private UUID id;
    private String name;
    private String description;
    private String type;
    private int memberCount;
    private int maxMembers;
    private long guildXp;
    private String ownerUsername;
}
