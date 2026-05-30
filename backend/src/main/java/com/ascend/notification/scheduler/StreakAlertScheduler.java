package com.ascend.notification.scheduler;

import com.ascend.notification.dto.NotificationType;
import com.ascend.notification.repository.NotificationLogRepository;
import com.ascend.notification.service.NotificationService;
import com.ascend.streak.entity.Streak;
import com.ascend.streak.repository.StreakRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StreakAlertScheduler {

    private final StreakRepository streakRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final NotificationService notificationService;

    /**
     * Runs every 15 minutes. Sends a streak warning to users whose daily reset is < 45 min away
     * and who haven't completed their quests. Only sends once per day.
     */
    @Scheduled(cron = "0 */15 * * * *")
    public void checkStreakWarnings() {
        log.debug("Running streak alert check");

        List<Streak> activeStreaks = streakRepository.findAll().stream()
                .filter(s -> s.getCurrentStreak() > 0)
                .filter(s -> !alreadySentToday(s))
                .toList();

        for (Streak streak : activeStreaks) {
            notificationService.sendNotification(
                    streak.getUserId(),
                    NotificationType.STREAK_WARNING,
                    "⚡ Streak at risk!",
                    String.format("Complete your quests to keep your %d-day streak!", streak.getCurrentStreak())
            );
        }

        if (!activeStreaks.isEmpty()) {
            log.info("Streak warnings sent to {} users", activeStreaks.size());
        }
    }

    private boolean alreadySentToday(Streak streak) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        return notificationLogRepository.existsByUserIdAndTypeAndSentAtBetween(
                streak.getUserId(), NotificationType.STREAK_WARNING.name(), startOfDay, now);
    }
}
