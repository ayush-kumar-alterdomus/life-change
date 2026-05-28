package com.ascend.quest.event;

import com.ascend.common.entity.Difficulty;
import com.ascend.common.entity.StatType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event published when a user completes a quest.
 * Consumed by XP, Streak, Analytics, Boss, Achievement, and Notification modules.
 */
@Getter
public class QuestCompletedEvent extends ApplicationEvent {

    private final UUID userId;
    private final UUID questId;
    private final String questTitle;
    private final Difficulty difficulty;
    private final StatType statType;
    private final int baseXpReward;
    private final LocalDateTime completedAt;

    public QuestCompletedEvent(Object source,
                               UUID userId,
                               UUID questId,
                               String questTitle,
                               Difficulty difficulty,
                               StatType statType,
                               int baseXpReward,
                               LocalDateTime completedAt) {
        super(source);
        this.userId = userId;
        this.questId = questId;
        this.questTitle = questTitle;
        this.difficulty = difficulty;
        this.statType = statType;
        this.baseXpReward = baseXpReward;
        this.completedAt = completedAt;
    }
}
