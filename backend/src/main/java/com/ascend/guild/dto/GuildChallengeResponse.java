package com.ascend.guild.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuildChallengeResponse {

    private UUID id;
    private String title;
    private int target;
    private int currentProgress;
    private LocalDateTime endsAt;
    private List<String> contributors;
}
