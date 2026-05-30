package com.ascend.league.event;

import com.ascend.league.entity.LeagueTier;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Domain event published when a user is demoted to a lower league tier
 * at the end of the weekly cycle.
 */
@Getter
public class LeagueDemotionEvent extends ApplicationEvent {

    private final UUID userId;
    private final LeagueTier previousTier;
    private final LeagueTier newTier;
    private final int finalRank;

    public LeagueDemotionEvent(Object source,
                               UUID userId,
                               LeagueTier previousTier,
                               LeagueTier newTier,
                               int finalRank) {
        super(source);
        this.userId = userId;
        this.previousTier = previousTier;
        this.newTier = newTier;
        this.finalRank = finalRank;
    }
}
