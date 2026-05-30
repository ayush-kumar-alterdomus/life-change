package com.ascend.reward.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class AchievementUnlockedEvent extends ApplicationEvent {

    private final UUID userId;
    private final String achievementName;
    private final String type;

    public AchievementUnlockedEvent(Object source, UUID userId, String achievementName, String type) {
        super(source);
        this.userId = userId;
        this.achievementName = achievementName;
        this.type = type;
    }
}
