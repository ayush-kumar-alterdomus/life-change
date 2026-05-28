package com.ascend.quest.scheduler;

import com.ascend.quest.event.DailyResetEvent;
import com.ascend.quest.service.QuestAssignmentService;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Scheduler that runs every hour to handle timezone-aware daily quest resets.
 * For each timezone where it is currently midnight (00:00), it finds users in
 * that timezone, triggers quest assignment for the new day, and publishes a
 * DailyResetEvent for streak evaluation.
 */
@Slf4j
@Component
public class QuestResetScheduler {

    private final UserRepository userRepository;
    private final QuestAssignmentService questAssignmentService;
    private final ApplicationEventPublisher eventPublisher;

    public QuestResetScheduler(UserRepository userRepository,
                               QuestAssignmentService questAssignmentService,
                               ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.questAssignmentService = questAssignmentService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Runs every hour at the top of the hour.
     * Identifies timezones where it is currently midnight and processes
     * the daily reset for users in those timezones.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void performDailyReset() {
        log.info("Daily reset scheduler triggered");

        List<String> midnightTimezones = findTimezonesAtMidnight();

        if (midnightTimezones.isEmpty()) {
            log.debug("No timezones at midnight this hour");
            return;
        }

        log.info("Processing daily reset for timezones: {}", midnightTimezones);

        List<User> usersToReset = userRepository.findByTimezoneIn(midnightTimezones);

        if (usersToReset.isEmpty()) {
            log.debug("No users found in midnight timezones");
            return;
        }

        log.info("Resetting quests for {} users across {} timezones",
                usersToReset.size(), midnightTimezones.size());

        // Assign daily quests for each user
        for (User user : usersToReset) {
            try {
                questAssignmentService.assignDailyQuests(user);
            } catch (Exception e) {
                log.error("Failed to assign daily quests for user={}: {}",
                        user.getId(), e.getMessage(), e);
            }
        }

        // Publish DailyResetEvent per timezone for streak evaluation
        for (String timezone : midnightTimezones) {
            List<UUID> userIdsInTimezone = usersToReset.stream()
                    .filter(u -> timezone.equals(u.getTimezone()))
                    .map(User::getId)
                    .collect(Collectors.toList());

            if (!userIdsInTimezone.isEmpty()) {
                LocalDate resetDate = LocalDate.now(ZoneId.of(timezone));
                DailyResetEvent event = new DailyResetEvent(
                        this, userIdsInTimezone, timezone, resetDate);
                eventPublisher.publishEvent(event);
                log.info("Published DailyResetEvent for timezone={}, users={}",
                        timezone, userIdsInTimezone.size());
            }
        }

        log.info("Daily reset scheduler completed");
    }

    /**
     * Finds all standard timezone IDs where the current time is midnight (00:xx hour).
     * Uses a tolerance window of the full 00:xx hour to ensure no timezone is missed
     * between scheduler runs.
     */
    List<String> findTimezonesAtMidnight() {
        return ZoneId.getAvailableZoneIds().stream()
                .filter(this::isMidnightHour)
                .collect(Collectors.toList());
    }

    private boolean isMidnightHour(String zoneId) {
        try {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of(zoneId));
            LocalTime localTime = now.toLocalTime();
            return localTime.getHour() == 0;
        } catch (Exception e) {
            // Skip invalid timezone IDs
            return false;
        }
    }
}
