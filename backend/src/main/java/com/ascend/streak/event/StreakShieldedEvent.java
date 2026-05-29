package com.ascend.streak.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Domain event published when a user's streak is preserved by a shield.
 * Consumed by Notification module for user alerts.
 */
@Getter
public class StreakShieldedEvent extends ApplicationEvent {

    private final UUID userId;
    private final int streakPreserved;
    private final int shieldsRemaining;

    public StreakShieldedEvent(Object source,
                               UUID userId,
                               int streakPreserved,
                               int shieldsRemaining) {
        super(source);
        this.userId = userId;
        this.streakPreserved = streakPreserved;
        this.shieldsRemaining = shieldsRemaining;
    }
}
