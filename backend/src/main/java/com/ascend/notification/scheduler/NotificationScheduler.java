package com.ascend.notification.scheduler;

import com.ascend.notification.service.NotificationService;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduler that sends daily quest reminder notifications to active users.
 * Runs every day at 9:00 AM server time.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    static final String REMINDER_TYPE = "DAILY_REMINDER";
    static final String REMINDER_TITLE = "Time to level up! ⚔️";
    static final String REMINDER_MESSAGE = "Your daily quests are waiting. Complete them to maintain your streak!";

    /**
     * Sends daily reminders to all non-guest users.
     * Skips users who have notifications rate-limited.
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void sendDailyReminders() {
        log.info("Starting daily reminder notification job");

        List<User> activeUsers = userRepository.findAll().stream()
                .filter(user -> !Boolean.TRUE.equals(user.getGuest()))
                .toList();

        int sent = 0;
        int skipped = 0;

        for (User user : activeUsers) {
            var result = notificationService.send(
                    user.getId(), REMINDER_TYPE, REMINDER_TITLE, REMINDER_MESSAGE);
            if (result != null) {
                sent++;
            } else {
                skipped++;
            }
        }

        log.info("Daily reminder job complete: sent={} skipped={} total={}", sent, skipped, activeUsers.size());
    }
}
