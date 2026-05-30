package com.ascend.boss.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Domain event published when a user defeats a boss.
 * Consumed by Notification, Analytics, and Achievement modules.
 */
@Getter
public class BossDefeatedEvent extends ApplicationEvent {

    private final UUID userId;
    private final UUID bossId;
    private final String bossName;
    private final int xpAwarded;
    private final String titleUnlocked;

    public BossDefeatedEvent(Object source,
                             UUID userId,
                             UUID bossId,
                             String bossName,
                             int xpAwarded,
                             String titleUnlocked) {
        super(source);
        this.userId = userId;
        this.bossId = bossId;
        this.bossName = bossName;
        this.xpAwarded = xpAwarded;
        this.titleUnlocked = titleUnlocked;
    }
}
