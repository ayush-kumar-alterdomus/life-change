package com.ascend.boss.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO returned when a boss is defeated, containing reward information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BossDefeatResponse {

    private String bossName;
    private int xpAwarded;
    private String titleUnlocked;
    private String cosmeticUnlocked;
}
