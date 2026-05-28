package com.ascend.xp.event;

import com.ascend.quest.event.QuestCompletedEvent;
import com.ascend.xp.service.XpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listens for QuestCompletedEvent and triggers XP award processing.
 * Errors are handled gracefully to avoid breaking the event chain.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class XpEventListener {

    private final XpService xpService;

    /**
     * Handles quest completion by awarding XP to the user.
     * Any exceptions are caught and logged to prevent disrupting other event listeners.
     *
     * @param event the quest completed event
     */
    @EventListener
    public void handleQuestCompleted(QuestCompletedEvent event) {
        try {
            log.debug("Processing XP award for user {} on quest {}",
                    event.getUserId(), event.getQuestId());
            xpService.awardXp(event.getUserId(), event);
        } catch (Exception e) {
            log.error("Failed to award XP for user {} on quest {}: {}",
                    event.getUserId(), event.getQuestId(), e.getMessage(), e);
        }
    }
}
