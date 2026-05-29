package com.ascend.streak.scheduler;

import com.ascend.quest.event.DailyResetEvent;
import com.ascend.streak.entity.Streak;
import com.ascend.streak.repository.StreakRepository;
import com.ascend.streak.service.ComebackModeService;
import com.ascend.streak.service.StreakService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Scheduler responsible for streak evaluation and comeback mode expiry checks.
 *
 * Listens for DailyResetEvent (published per timezone by the Quest module)
 * to evaluate each user's daily streak. Also runs hourly to check and
 * deactivate expired comeback modes.
 */
@Slf4j
@Component
public class StreakCalculationScheduler {

    private final StreakService streakService;
    private final ComebackModeService comebackModeService;
    private final StreakRepository streakRepository;

    public StreakCalculationScheduler(StreakService streakService,
                                     ComebackModeService comebackModeService,
                                     StreakRepository streakRepository) {
        this.streakService = streakService;
        this.comebackModeService = comebackModeService;
        this.streakRepository = streakRepository;
    }

    /**
     * Handles the DailyResetEvent published by the Quest module's QuestResetScheduler.
     * For each user in the resetting timezone, evaluates their daily streak
     * (increment if >= 80% quests completed, break otherwise).
     *
     * @param event the daily reset event containing user IDs and timezone info
     */
    @EventListener
    public void onDailyReset(DailyResetEvent event) {
        log.info("Received DailyResetEvent for timezone={}, users={}, resetDate={}",
                event.getTimezone(), event.getUserIds().size(), event.getResetDate());

        for (UUID userId : event.getUserIds()) {
            try {
                streakService.evaluateDailyStreak(userId);
                log.debug("Streak evaluation completed for user={} in timezone={}",
                        userId, event.getTimezone());
            } catch (Exception e) {
                log.error("Failed to evaluate streak for user={} in timezone={}: {}",
                        userId, event.getTimezone(), e.getMessage(), e);
            }
        }

        log.info("Streak evaluation completed for timezone={}", event.getTimezone());
    }

    /**
     * Runs every hour to check and deactivate expired comeback modes.
     * Queries all users with active comeback mode and checks if their
     * 48-hour window has expired.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void checkComebackModeExpiry() {
        log.info("Hourly comeback mode expiry check triggered");

        List<Streak> activeComeback = streakRepository.findByComebackModeActiveTrue();

        if (activeComeback.isEmpty()) {
            log.debug("No users with active comeback mode");
            return;
        }

        log.info("Checking comeback expiry for {} users", activeComeback.size());

        for (Streak streak : activeComeback) {
            try {
                comebackModeService.checkComebackExpiry(streak.getUserId());
            } catch (Exception e) {
                log.error("Failed to check comeback expiry for user={}: {}",
                        streak.getUserId(), e.getMessage(), e);
            }
        }

        log.info("Comeback mode expiry check completed");
    }
}
