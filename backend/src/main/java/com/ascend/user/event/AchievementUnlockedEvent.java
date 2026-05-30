package com.ascend.user.event;

import com.ascend.common.entity.StatType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

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

    public AchievementUnlockedEvent(Object source,
                                    UUID userId,
                                    StatType statType,
                                    int threshold,
                                    String titleName) {
        super(source);
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.statType = Objects.requireNonNull(statType, "statType must not be null");
        this.threshold = threshold;
        this.titleName = Objects.requireNonNull(titleName, "titleName must not be null");
    }
}
