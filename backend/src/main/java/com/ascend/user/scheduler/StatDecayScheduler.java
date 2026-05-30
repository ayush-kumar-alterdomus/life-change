package com.ascend.user.scheduler;

import com.ascend.common.entity.StatType;
import com.ascend.user.entity.User;
import com.ascend.user.event.StatDecayEvent;
import com.ascend.user.repository.UserRepository;
import com.ascend.user.service.StatDecayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Scheduler that runs daily to evaluate stat decay for all Hard Mode users.
 * For each user with hard_mode enabled, calls {@link StatDecayService#evaluateStatDecay}
 * and publishes a {@link StatDecayEvent} if any stats were decayed.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatDecayScheduler {

    private final UserRepository userRepository;
    private final StatDecayService statDecayService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Runs daily at 2:00 AM UTC to evaluate stat decay for all Hard Mode users.
     * For each user, evaluates which stats have been inactive for 7+ days
     * and applies decay. Publishes a StatDecayEvent for notification purposes.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void runDailyStatDecay() {
        log.info("Daily stat decay scheduler triggered");

        List<User> hardModeUsers = userRepository.findByHardModeTrue();

        if (hardModeUsers.isEmpty()) {
            log.debug("No Hard Mode users found — skipping stat decay");
            return;
        }

        log.info("Evaluating stat decay for {} Hard Mode users", hardModeUsers.size());

        int decayedCount = 0;

        for (User user : hardModeUsers) {
            try {
                Set<StatType> decayedStats = statDecayService.evaluateStatDecay(user.getId());

                if (!decayedStats.isEmpty()) {
                    decayedCount++;
                    publishStatDecayEvent(user, decayedStats);
                }
            } catch (Exception e) {
                log.error("Failed to evaluate stat decay for user={}: {}",
                        user.getId(), e.getMessage(), e);
            }
        }

        log.info("Daily stat decay completed: {}/{} users had stats decayed",
                decayedCount, hardModeUsers.size());
    }

    /**
     * Publishes a StatDecayEvent for notification purposes.
     *
     * @param user         the user whose stats decayed
     * @param decayedStats the set of stat types that were decayed
     */
    private void publishStatDecayEvent(User user, Set<StatType> decayedStats) {
        StatDecayEvent event = new StatDecayEvent(
                this,
                user.getId(),
                decayedStats,
                StatDecayService.DECAY_POINTS
        );
        eventPublisher.publishEvent(event);
        log.debug("Published StatDecayEvent for user={}, decayedStats={}",
                user.getId(), decayedStats);
    }
}
