package com.ascend.notification.event;

import com.ascend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Listens for domain events and triggers appropriate notifications.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    /**
     * Triggered when a user completes a quest.
     */
    @EventListener
    public void onQuestCompleted(QuestCompletedEvent event) {
        notificationService.send(
                event.userId(),
                "QUEST_COMPLETED",
                "Quest Complete! 🎉",
                String.format("You earned %d XP for completing \"%s\"", event.xpEarned(), event.questTitle())
        );
    }

    /**
     * Triggered when a user reaches a streak milestone (7, 30, 100 days).
     */
    @EventListener
    public void onStreakMilestone(StreakMilestoneEvent event) {
        String title = String.format("%d Day Streak! 🔥", event.streakDays());
        String message = switch (event.streakDays()) {
            case 7 -> "One week strong! You're building real momentum.";
            case 30 -> "A full month! Your discipline is legendary.";
            case 100 -> "100 days! You've achieved epic status.";
            default -> String.format("Amazing! %d days in a row!", event.streakDays());
        };

        notificationService.send(event.userId(), "STREAK_MILESTONE", title, message);
    }

    /**
     * Triggered when a user levels up.
     */
    @EventListener
    public void onLevelUp(LevelUpEvent event) {
        notificationService.send(
                event.userId(),
                "LEVEL_UP",
                String.format("Level %d! ⬆️", event.newLevel()),
                String.format("Congratulations! You've reached level %d.", event.newLevel())
        );
    }

    // --- Event Records ---

    public record QuestCompletedEvent(UUID userId, String questTitle, int xpEarned) {}
    public record StreakMilestoneEvent(UUID userId, int streakDays) {}
    public record LevelUpEvent(UUID userId, int newLevel) {}
}
