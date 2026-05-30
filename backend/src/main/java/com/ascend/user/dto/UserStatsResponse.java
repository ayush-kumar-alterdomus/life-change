package com.ascend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO containing a user's full character stats and earned identity titles.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsResponse {

    private int strength;
    private int wisdom;
    private int focus;
    private int discipline;
    private int vitality;
    private int charisma;
    private BigDecimal lifeScore;
    private List<IdentityTitle> titles;
}
