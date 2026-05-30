package com.ascend.league.event;

import com.ascend.league.service.AntiCheatService;
import com.ascend.quest.event.QuestCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listens for QuestCompletedEvent and triggers anti-cheat detection.
 * Runs asynchronously to avoid blocking the quest completion flow.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AntiCheatEventListener {

    private final AntiCheatService antiCheatService;

    /**
     * Called on each QuestCompletedEvent to check for suspicious activity.
     * Runs both speed violation and bulk spam detection.
     *
     * @param event the quest completed event
     */
    @Async
    @EventListener
    public void onQuestCompleted(QuestCompletedEvent event) {
        log.debug("Anti-cheat check triggered for user {} on quest completion.", event.getUserId());

        try {
            // Check for bulk spam first (stricter threshold, shorter window)
            boolean bulkSpamDetected = antiCheatService.detectBulkSpam(event.getUserId());

            if (!bulkSpamDetected) {
                // Only check speed violation if bulk spam wasn't already detected
                antiCheatService.detectSpeedViolation(event.getUserId());
            }
        } catch (Exception e) {
            // Anti-cheat should never block quest completion — log and continue
            log.error("Anti-cheat check failed for user {}: {}", event.getUserId(), e.getMessage(), e);
        }
    }
}
