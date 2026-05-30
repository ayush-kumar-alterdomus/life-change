package com.ascend.guild.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Domain event published when a guild challenge reaches its target.
 * Consumed by modules that need to react to guild challenge completions
 * (e.g., achievements, analytics).
 */
@Getter
public class GuildChallengeCompleteEvent extends ApplicationEvent {

    private final UUID challengeId;
    private final UUID guildId;
    private final String challengeTitle;
    private final int target;
    private final long guildXpAwarded;

    public GuildChallengeCompleteEvent(Object source,
                                       UUID challengeId,
                                       UUID guildId,
                                       String challengeTitle,
                                       int target,
                                       long guildXpAwarded) {
        super(source);
        this.challengeId = challengeId;
        this.guildId = guildId;
        this.challengeTitle = challengeTitle;
        this.target = target;
        this.guildXpAwarded = guildXpAwarded;
    }
}
