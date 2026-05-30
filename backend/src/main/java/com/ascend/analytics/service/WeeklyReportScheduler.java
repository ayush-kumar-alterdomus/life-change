package com.ascend.analytics.service;

import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Scheduler that generates weekly reports for all active users every Sunday.
 * Runs at 23:00 in each timezone to ensure the full week's data is captured
 * before the week resets on Monday.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyReportScheduler {

    private final WeeklyReportService weeklyReportService;
    private final UserRepository userRepository;

    /**
     * Runs every hour on Sundays to handle timezone-aware weekly report generation.
     * For each timezone where it is currently 23:00 on Sunday, generates weekly
     * reports for all users in that timezone.
     *
     * Cron: every hour, but only processes when it's Sunday 23:xx in a timezone.
     */
    @Scheduled(cron = "0 0 * * * SUN")
    public void generateWeeklyReports() {
        log.info("Weekly report scheduler triggered");

        List<String> sundayEveningTimezones = findTimezonesAtSundayEvening();

        if (sundayEveningTimezones.isEmpty()) {
            log.debug("No timezones at Sunday evening this hour");
            return;
        }

        log.info("Generating weekly reports for timezones: {}", sundayEveningTimezones);

        List<User> users = userRepository.findByTimezoneIn(sundayEveningTimezones);

        if (users.isEmpty()) {
            log.debug("No users found in Sunday evening timezones");
            return;
        }

        log.info("Generating weekly reports for {} users", users.size());

        int successCount = 0;
        int failureCount = 0;

        for (User user : users) {
            try {
                weeklyReportService.generateWeeklyReport(user.getId());
                successCount++;
            } catch (Exception e) {
                failureCount++;
                log.error("Failed to generate weekly report for user={}: {}",
                        user.getId(), e.getMessage(), e);
            }
        }

        log.info("Weekly report generation completed: success={}, failures={}",
                successCount, failureCount);
    }

    /**
     * Finds all timezone IDs where it is currently Sunday at 23:xx hour.
     * This ensures reports are generated at the end of Sunday, capturing
     * the full week's activity.
     */
    List<String> findTimezonesAtSundayEvening() {
        return ZoneId.getAvailableZoneIds().stream()
                .filter(this::isSundayEvening)
                .collect(Collectors.toList());
    }

    private boolean isSundayEvening(String zoneId) {
        try {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of(zoneId));
            DayOfWeek dayOfWeek = now.getDayOfWeek();
            LocalTime localTime = now.toLocalTime();
            return dayOfWeek == DayOfWeek.SUNDAY && localTime.getHour() == 23;
        } catch (Exception e) {
            // Skip invalid timezone IDs
            return false;
        }
    }
}
