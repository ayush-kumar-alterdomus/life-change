package com.ascend.user.event;

import com.ascend.common.entity.StatType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Domain event published when stat decay is applied to a Hard Mode user.
 * Consumed by the Notification module to inform the user about decayed stats.
 */
@Getter
public final class StatDecayEvent extends ApplicationEvent {

    private final UUID userId;
    private final Set<StatType> decayedStats;
    private final int decayAmount;

    public StatDecayEvent(Object source,
                          UUID userId,
                          Set<StatType> decayedStats,
                          int decayAmount) {
        super(source);
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.decayedStats = Objects.requireNonNull(decayedStats, "decayedStats must not be null");
        this.decayAmount = decayAmount;
    }
}
