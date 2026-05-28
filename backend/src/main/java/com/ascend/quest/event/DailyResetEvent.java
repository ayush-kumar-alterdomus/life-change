package com.ascend.quest.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Domain event published when the daily reset occurs for a group of users.
 * Consumed by the Streak module to evaluate streak continuity.
 */
@Getter
public class DailyResetEvent extends ApplicationEvent {

    private final List<UUID> userIds;
    private final String timezone;
    private final LocalDate resetDate;

    public DailyResetEvent(Object source, List<UUID> userIds, String timezone, LocalDate resetDate) {
        super(source);
        this.userIds = userIds;
        this.timezone = timezone;
        this.resetDate = resetDate;
    }
}
