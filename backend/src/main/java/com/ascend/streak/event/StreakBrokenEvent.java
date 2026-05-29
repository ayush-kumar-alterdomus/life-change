package com.ascend.streak.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Domain event published when a user's streak is broken (no shield available).
 * Consumed by Notification module for user alerts.
 */
@Getter
public class StreakBrokenEvent extends ApplicationEvent {

    private final UUID userId;
    private final int previousStreak;
    private final boolean comebackModeActivated;

    public StreakBrokenEvent(Object source,
                             UUID userId,
                             int previousStreak,
                             boolean comebackModeActivated) {
        super(source);
        this.userId = userId;
        this.previousStreak = previousStreak;
        this.comebackModeActivated = comebackModeActivated;
    }
}
