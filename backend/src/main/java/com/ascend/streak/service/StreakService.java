package com.ascend.streak.service;

import com.ascend.common.entity.Frequency;
import com.ascend.quest.entity.Quest;
import com.ascend.quest.entity.QuestCompletion;
import com.ascend.quest.repository.QuestCompletionRepository;
import com.ascend.quest.repository.QuestRepository;
import com.ascend.streak.dto.StreakMilestone;
import com.ascend.streak.dto.StreakResponse;
import com.ascend.streak.entity.Streak;
import com.ascend.streak.event.StreakBrokenEvent;
import com.ascend.streak.event.StreakMilestoneEvent;
import com.ascend.streak.event.StreakShieldedEvent;
import com.ascend.streak.repository.StreakRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Core service for managing user streaks.
 * Handles streak evaluation, incrementing, breaking, and milestone detection.
 */
@Slf4j
@Service
public class StreakService {

    private static final double STREAK_COMPLETION_THRESHOLD = 0.8;
    private static final double COMBO_INCREMENT_PER_DAY = 0.01;
    private static final double MAX_COMBO_MULTIPLIER = 2.0;

    private final StreakRepository streakRepository;
    private final QuestRepository questRepository;
    private final QuestCompletionRepository questCompletionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ComebackModeService comebackModeService;

    public StreakService(StreakRepository streakRepository,
                         QuestRepository questRepository,
                         QuestCompletionRepository questCompletionRepository,
                         ApplicationEventPublisher eventPublisher,
                         ComebackModeService comebackModeService) {
        this.streakRepository = streakRepository;
        this.questRepository = questRepository;
        this.questCompletionRepository = questCompletionRepository;
        this.eventPublisher = eventPublisher;
        this.comebackModeService = comebackModeService;
    }

    /**
     * Fetches the current streak data for a user.
     *
     * @param userId the user's ID
     * @return StreakResponse with current streak information
     */
    public StreakResponse getStreak(UUID userId) {
        log.debug("Fetching streak for user={}", userId);

        Streak streak = streakRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultStreak(userId));

        return StreakResponse.builder()
                .currentStreak(streak.getCurrentStreak())
                .longestStreak(streak.getLongestStreak())
                .comboMultiplier(streak.getComboMultiplier())
                .shieldAvailable(streak.getShieldAvailable())
                .lastCompletedAt(streak.getLastCompletedAt())
                .comebackModeActive(Boolean.TRUE.equals(streak.getComebackModeActive()))
                .comebackExpiresAt(streak.getComebackExpiresAt())
                .build();
    }

    /**
     * Evaluates the daily streak for a user based on quest completion percentage.
     * Called during the daily reset to determine if the streak should be incremented or broken.
     *
     * @param userId the user's ID
     */
    @Transactional
    public void evaluateDailyStreak(UUID userId) {
        log.info("Evaluating daily streak for user={}", userId);

        // 1. Count user's assigned daily quests
        long assignedDailyQuests = countAssignedDailyQuests(userId);

        if (assignedDailyQuests == 0) {
            log.debug("No daily quests assigned for user={}, skipping streak evaluation", userId);
            return;
        }

        // 2. Count user's completed daily quests for today
        long completedDailyQuests = countCompletedDailyQuests(userId);

        // 3. Calculate completion percentage
        double completionPercentage = (double) completedDailyQuests / assignedDailyQuests;
        log.debug("Streak evaluation for user={}: completed={}/{}, percentage={}",
                userId, completedDailyQuests, assignedDailyQuests, completionPercentage);

        // 4. Evaluate based on 80% threshold
        if (completionPercentage >= STREAK_COMPLETION_THRESHOLD) {
            incrementStreak(userId);
        } else {
            breakStreak(userId);
        }
    }

    /**
     * Increments the user's streak, recalculates combo multiplier,
     * updates longest streak, and checks for milestones.
     *
     * @param userId the user's ID
     */
    @Transactional
    public void incrementStreak(UUID userId) {
        log.info("Incrementing streak for user={}", userId);

        Streak streak = streakRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultStreak(userId));

        // 1. Increment current_streak
        int newStreak = streak.getCurrentStreak() + 1;
        streak.setCurrentStreak(newStreak);

        // 2. Recalculate combo_multiplier: min(1 + 0.01 × currentStreak, 2.0)
        BigDecimal newMultiplier = BigDecimal.valueOf(
                Math.min(1.0 + COMBO_INCREMENT_PER_DAY * newStreak, MAX_COMBO_MULTIPLIER));
        streak.setComboMultiplier(newMultiplier);

        // 3. Update last_completed_at
        streak.setLastCompletedAt(LocalDateTime.now());

        // 4. Update longest_streak if current > longest
        if (newStreak > streak.getLongestStreak()) {
            streak.setLongestStreak(newStreak);
        }

        streakRepository.save(streak);
        log.info("Streak incremented for user={}: streak={}, multiplier={}", userId, newStreak, newMultiplier);

        // 5. Check for streak milestones and publish event if reached
        checkAndPublishMilestone(userId, newStreak);
    }

    /**
     * Handles a streak break for a user.
     * If a shield is available, it is activated to preserve the streak.
     * Otherwise, the streak is reset to 0 and Comeback Mode is activated.
     *
     * @param userId the user's ID
     */
    @Transactional
    public void breakStreak(UUID userId) {
        log.info("Evaluating streak break for user={}", userId);

        Streak streak = streakRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultStreak(userId));

        // 1. Check if shield is available
        if (Boolean.TRUE.equals(streak.getShieldAvailable())) {
            // 2. Shield available → activate shield, preserve streak
            activateShield(userId);

            log.info("Streak shielded for user={}: streak preserved at {}", userId, streak.getCurrentStreak());

            // Publish StreakShieldedEvent
            StreakShieldedEvent shieldedEvent = new StreakShieldedEvent(
                    this,
                    userId,
                    streak.getCurrentStreak(),
                    0 // shieldsRemaining after use
            );
            eventPublisher.publishEvent(shieldedEvent);
        } else {
            // 3. No shield → reset current_streak to 0, activate Comeback Mode
            int previousStreak = streak.getCurrentStreak();

            streak.setCurrentStreak(0);
            streak.setComboMultiplier(BigDecimal.ONE);
            streakRepository.save(streak);

            // Activate Comeback Mode
            comebackModeService.activateComebackMode(userId);

            log.info("Streak broken for user={}: reset from {} to 0, comeback mode activated", userId, previousStreak);

            // 4. Publish StreakBrokenEvent
            StreakBrokenEvent brokenEvent = new StreakBrokenEvent(
                    this,
                    userId,
                    previousStreak,
                    true // comebackModeActivated
            );
            eventPublisher.publishEvent(brokenEvent);
        }
    }

    /**
     * Activates the streak shield for a user, consuming the shield to preserve the streak.
     * Sets shield_available to false and records the time the shield was used.
     *
     * @param userId the user's ID
     */
    @Transactional
    public void activateShield(UUID userId) {
        log.info("Activating streak shield for user={}", userId);

        Streak streak = streakRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultStreak(userId));

        // 1. Set shield_available = false
        streak.setShieldAvailable(false);

        // 2. Set shield_used_at = now()
        streak.setShieldUsedAt(LocalDateTime.now());

        // 3. Streak remains intact (no changes to currentStreak or comboMultiplier)
        streakRepository.save(streak);

        log.info("Shield activated for user={}: shield consumed, streak preserved at {}", userId, streak.getCurrentStreak());
    }

    /**
     * Counts the number of daily quests assigned to a user.
     * Uses the quest frequency to identify daily quests created by or assigned to the user.
     */
    private long countAssignedDailyQuests(UUID userId) {
        List<Quest> dailyQuests = questRepository.findByFrequencyAndCustomFalse(Frequency.DAILY);
        List<Quest> customDailyQuests = questRepository.findByCreatedBy_Id(userId).stream()
                .filter(q -> q.getFrequency() == Frequency.DAILY)
                .toList();

        return dailyQuests.size() + customDailyQuests.size();
    }

    /**
     * Counts the number of daily quests completed by the user today.
     */
    private long countCompletedDailyQuests(UUID userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        List<QuestCompletion> completions = questCompletionRepository
                .findByUserIdAndCompletedAtBetween(userId, startOfDay, endOfDay);

        return completions.size();
    }

    /**
     * Checks if the new streak count matches any milestone threshold
     * and publishes a StreakMilestoneEvent if so.
     */
    private void checkAndPublishMilestone(UUID userId, int currentStreak) {
        Arrays.stream(StreakMilestone.values())
                .filter(milestone -> milestone.getDays() == currentStreak)
                .findFirst()
                .ifPresent(milestone -> {
                    log.info("Streak milestone reached for user={}: {} ({} days, {} XP bonus)",
                            userId, milestone.name(), milestone.getDays(), milestone.getXpBonus());

                    StreakMilestoneEvent event = new StreakMilestoneEvent(
                            this,
                            userId,
                            milestone,
                            currentStreak,
                            milestone.getXpBonus()
                    );
                    eventPublisher.publishEvent(event);
                });
    }

    /**
     * Creates a default streak record for a new user.
     */
    private Streak createDefaultStreak(UUID userId) {
        Streak streak = Streak.builder()
                .userId(userId)
                .currentStreak(0)
                .longestStreak(0)
                .comboMultiplier(BigDecimal.ONE)
                .shieldAvailable(false)
                .build();

        return streakRepository.save(streak);
    }
}
