package com.ascend.xp.event;

import com.ascend.common.entity.StatType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Domain event published after XP is awarded to a user.
 * Consumed by Leaderboard, Analytics, Notification, and Achievement modules.
 */
@Getter
public class XpAwardedEvent extends ApplicationEvent {

    private final UUID userId;
    private final int xpAmount;
    private final long newTotalXp;
    private final int newLevel;
    private final StatType statType;
    private final String source;

    public XpAwardedEvent(Object source,
                          UUID userId,
                          int xpAmount,
                          long newTotalXp,
                          int newLevel,
                          StatType statType,
                          String eventSource) {
        super(source);
        this.userId = userId;
        this.xpAmount = xpAmount;
        this.newTotalXp = newTotalXp;
        this.newLevel = newLevel;
        this.statType = statType;
        this.source = eventSource;
    }
}
