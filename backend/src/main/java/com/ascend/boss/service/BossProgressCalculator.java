package com.ascend.boss.service;

import com.ascend.common.entity.Difficulty;
import com.ascend.common.entity.StatType;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Calculates boss damage based on quest difficulty.
 * Called when a QuestCompletedEvent fires and the user has an active boss.
 */
@Component
public class BossProgressCalculator {

    /**
     * Calculate the damage dealt to a boss based on quest difficulty.
     * Damage is expressed as a percentage of a single stage's progress.
     *
     * @param difficulty the difficulty of the completed quest
     * @param statType   the stat type associated with the quest
     * @param bossId     the boss being damaged
     * @return damage as a percentage (5, 10, 15, or 25)
     */
    public int calculateDamage(Difficulty difficulty, StatType statType, UUID bossId) {
        return switch (difficulty) {
            case EASY -> 5;
            case MEDIUM -> 10;
            case HARD -> 15;
            case LEGENDARY -> 25;
        };
    }
}
