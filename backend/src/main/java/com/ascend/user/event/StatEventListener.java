package com.ascend.user.event;

import com.ascend.quest.event.QuestCompletedEvent;
import com.ascend.user.dto.StatGainResponse;
import com.ascend.user.service.StatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listens for QuestCompletedEvent and triggers stat point awards.
 * Stat gains happen in parallel with XP awards (both listen to QuestCompletedEvent).
 * Errors are handled gracefully to avoid breaking the event chain.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatEventListener {

    private final StatService statService;

    /**
     * Handles quest completion by awarding stat points to the user.
     * Any exceptions are caught and logged to prevent disrupting other event listeners.
     *
     * @param event the quest completed event
     */
    @EventListener
    public void handleQuestCompleted(QuestCompletedEvent event) {
        try {
            log.debug("Processing stat gain for user {} on quest {} (stat: {}, difficulty: {})",
                    event.getUserId(), event.getQuestId(), event.getStatType(), event.getDifficulty());

            StatGainResponse response = statService.awardStatPoints(
                    event.getUserId(), event.getStatType(), event.getDifficulty());

            log.debug("Stat gain result for user {}: {} {} -> {} (gain: {}{})",
                    event.getUserId(), response.getStatType(),
                    response.getPreviousValue(), response.getNewValue(), response.getGain(),
                    response.getTitleUnlocked() != null ? ", title: " + response.getTitleUnlocked() : "");
        } catch (Exception e) {
            log.error("Failed to award stat points for user {} on quest {}: {}",
                    event.getUserId(), event.getQuestId(), e.getMessage(), e);
        }
    }
}
