package com.ascend.xp.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Domain event published when a user achieves a Perfect Day
 * (all assigned daily missions completed).
 * Consumed by Notification, Achievement, and Reward modules.
 */
@Getter
public class PerfectDayEvent extends ApplicationEvent {

    private final UUID userId;
    private final int bonusXpAwarded;
    private final int totalQuestsCompleted;
    private final LocalDate date;

    public PerfectDayEvent(Object source,
                           UUID userId,
                           int bonusXpAwarded,
                           int totalQuestsCompleted,
                           LocalDate date) {
        super(source);
        this.userId = userId;
        this.bonusXpAwarded = bonusXpAwarded;
        this.totalQuestsCompleted = totalQuestsCompleted;
        this.date = date;
    }
}
