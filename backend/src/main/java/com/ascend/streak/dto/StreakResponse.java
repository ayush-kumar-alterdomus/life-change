package com.ascend.streak.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreakResponse {

    private int currentStreak;
    private int longestStreak;
    private BigDecimal comboMultiplier;
    private boolean shieldAvailable;
    private LocalDateTime lastCompletedAt;
    private boolean comebackModeActive;
    private LocalDateTime comebackExpiresAt;
}
