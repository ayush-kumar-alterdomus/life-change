package com.ascend.xp.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Domain event published when a user completes a prestige reset.
 * Consumed by Notification, Analytics, and Leaderboard modules.
 */
@Getter
public class PrestigeEvent extends ApplicationEvent {

    private final UUID userId;
    private final int newPrestigeLevel;

    public PrestigeEvent(Object source, UUID userId, int newPrestigeLevel) {
        super(source);
        this.userId = userId;
        this.newPrestigeLevel = newPrestigeLevel;
    }
}
