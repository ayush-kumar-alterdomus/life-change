package com.ascend.user.event;

import com.ascend.common.entity.StatType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain event published when a user unlocks a new identity title
 * by crossing a stat threshold.
 * Consumed by Notification and Analytics modules.
 */
@Getter
public final class AchievementUnlockedEvent extends ApplicationEvent {

    private final UUID userId;
    private final StatType statType;
    private final int threshold;
    private final String titleName;
    private final Instant occurredAt;

    public AchievementUnlockedEvent(Object source,
                                    UUID userId,
                                    StatType statType,
                                    int threshold,
                                    String titleName) {
        super(source);
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.statType = Objects.requireNonNull(statType, "statType must not be null");
        if (threshold <= 0) {
            throw new IllegalArgumentException("threshold must be positive");
        }
        this.threshold = threshold;
        this.titleName = Objects.requireNonNull(titleName, "titleName must not be null");
        this.occurredAt = Instant.now();
    }

    /**
     * Static factory for concise construction at call sites.
     */
    public static AchievementUnlockedEvent of(Object source,
                                              UUID userId,
                                              StatType statType,
                                              int threshold,
                                              String titleName) {
        return new AchievementUnlockedEvent(source, userId, statType, threshold, titleName);
    }
}
