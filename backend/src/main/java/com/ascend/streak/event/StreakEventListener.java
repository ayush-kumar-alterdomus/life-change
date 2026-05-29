package com.ascend.streak.event;

import com.ascend.quest.event.QuestCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listens for quest completion events to update real-time completion counts.
 *
 * This listener is for UI progress display only — it tracks how many quests
 * a user has completed today so the frontend can show progress toward the
 * daily streak threshold. Actual streak evaluation (increment/break) happens
 * at daily reset via {@link com.ascend.streak.scheduler.StreakCalculationScheduler}.
 */
@Slf4j
@Component
public class StreakEventListener {

    public StreakEventListener() {
        // Constructor injection ready for future dependencies (e.g., Redis counter service)
    }

    /**
     * Handles quest completion events by updating the real-time daily completion count.
     *
     * Currently logs the completion for observability. A full implementation would
     * update a real-time counter (e.g., Redis or in-memory cache) so the UI can
     * display progress toward the daily 80% streak threshold without waiting for
     * the daily reset evaluation.
     *
     * @param event the quest completed event containing user and quest details
     */
    @EventListener
    public void onQuestCompleted(QuestCompletedEvent event) {
        log.info("Quest completed for streak tracking: userId={}, questId={}, questTitle='{}', completedAt={}",
                event.getUserId(),
                event.getQuestId(),
                event.getQuestTitle(),
                event.getCompletedAt());

        // TODO: Update real-time daily completion counter (e.g., Redis INCR or in-memory cache)
        // This would allow the UI to show "3/5 quests completed today" without querying the DB.
        // Example: dailyCompletionCounter.increment(event.getUserId(), event.getCompletedAt().toLocalDate());

        log.debug("Real-time completion count updated for userId={} on date={}",
                event.getUserId(),
                event.getCompletedAt().toLocalDate());
    }
}
