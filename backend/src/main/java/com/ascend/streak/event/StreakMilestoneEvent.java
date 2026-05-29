package com.ascend.streak.event;

import com.ascend.streak.dto.StreakMilestone;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Domain event published when a user reaches a streak milestone.
 * Consumed by Notification module for user alerts and XP module for bonus XP.
 */
@Getter
public class StreakMilestoneEvent extends ApplicationEvent {

    private final UUID userId;
    private final StreakMilestone milestoneType;
    private final int streakDays;
    private final int bonusXp;

    public StreakMilestoneEvent(Object source,
                                UUID userId,
                                StreakMilestone milestoneType,
                                int streakDays,
                                int bonusXp) {
        super(source);
        this.userId = userId;
        this.milestoneType = milestoneType;
        this.streakDays = streakDays;
        this.bonusXp = bonusXp;
    }
}
