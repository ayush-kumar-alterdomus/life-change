package com.ascend.user.dto;

import com.ascend.common.entity.StatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO returned after stat points are awarded from a quest completion.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatGainResponse {

    private StatType statType;
    private int previousValue;
    private int newValue;
    private int gain;
    private String titleUnlocked;
}
