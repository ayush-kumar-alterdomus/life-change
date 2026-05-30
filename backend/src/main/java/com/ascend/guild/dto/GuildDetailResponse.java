package com.ascend.guild.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GuildDetailResponse extends GuildResponse {

    private List<GuildMemberResponse> members;
    private List<GuildChallengeResponse> activeChallenges;
    private int rank;
}
