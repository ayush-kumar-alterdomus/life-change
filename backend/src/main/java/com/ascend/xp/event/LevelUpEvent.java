package com.ascend.xp.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain event published when a user levels up.
 * Consumed by Notification, Achievement, and League modules.
 */
@Getter
public final class LevelUpEvent extends ApplicationEvent {

    private final UUID userId;
    private final int previousLevel;
    private final int newLevel;
    private final List<String> unlockedFeatures;

    public LevelUpEvent(Object source,
                        UUID userId,
                        int previousLevel,
                        int newLevel,
                        List<String> unlockedFeatures) {
        super(source);
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        if (newLevel <= previousLevel) {
            throw new IllegalArgumentException("newLevel must be greater than previousLevel");
        }
        this.previousLevel = previousLevel;
        this.newLevel = newLevel;
        this.unlockedFeatures = unlockedFeatures == null
                ? List.of()
                : List.copyOf(unlockedFeatures);
    }
}
