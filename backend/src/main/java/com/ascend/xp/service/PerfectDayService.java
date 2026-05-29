package com.ascend.xp.service;

import com.ascend.common.entity.Frequency;
import com.ascend.quest.entity.Quest;
import com.ascend.quest.entity.QuestCompletion;
import com.ascend.quest.repository.QuestCompletionRepository;
import com.ascend.quest.repository.QuestRepository;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import com.ascend.xp.entity.XpHistory;
import com.ascend.xp.event.PerfectDayEvent;
import com.ascend.xp.repository.XpHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service responsible for detecting and rewarding Perfect Day achievements.
 * A Perfect Day occurs when a user completes all their assigned daily missions for the day.
 * Reward: 100 bonus XP + chest unlock trigger (via PerfectDayEvent).
 *
 * Called after each quest completion to check if it was the last one needed.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PerfectDayService {

    private static final int PERFECT_DAY_BONUS_XP = 100;
    private static final String PERFECT_DAY_SOURCE_TYPE = "PERFECT_DAY";

    private final QuestRepository questRepository;
    private final QuestCompletionRepository questCompletionRepository;
    private final UserRepository userRepository;
    private final XpHistoryRepository xpHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Checks if the user has completed all assigned daily missions for today.
     * If all are completed, awards 100 bonus XP and publishes a PerfectDayEvent
     * to trigger chest unlock and other downstream effects.
     *
     * @param userId the user to check for Perfect Day achievement
     */
    @Transactional
    public void checkPerfectDay(UUID userId) {
        log.debug("Checking Perfect Day status for user={}", userId);

        // 1. Count user's assigned daily missions for today
        int totalDailyQuests = countAssignedDailyQuests(userId);

        if (totalDailyQuests == 0) {
            log.debug("User {} has no assigned daily quests, skipping Perfect Day check", userId);
            return;
        }

        // 2. Count user's completed daily missions for today
        int completedDailyQuests = countCompletedDailyQuests(userId);

        log.debug("User {} Perfect Day progress: {}/{}", userId, completedDailyQuests, totalDailyQuests);

        // 3. If all completed → award bonus XP + trigger chest unlock
        if (completedDailyQuests >= totalDailyQuests) {
            // Check if bonus was already awarded today (idempotency guard)
            if (hasAlreadyReceivedPerfectDayBonus(userId)) {
                log.debug("User {} already received Perfect Day bonus today, skipping", userId);
                return;
            }

            awardPerfectDayBonus(userId, totalDailyQuests);
        }
    }

    /**
     * Counts the total number of daily quests assigned to the user.
     * Includes both user-created custom quests and system recurring quests.
     */
    private int countAssignedDailyQuests(UUID userId) {
        // User's custom quests (daily frequency)
        List<Quest> userQuests = questRepository.findByCreatedBy_Id(userId);
        long userDailyCount = userQuests.stream()
                .filter(q -> q.getFrequency() == Frequency.DAILY)
                .count();

        // System recurring quests assigned to all users
        List<Quest> recurringQuests = questRepository.findByRecurringTrue();
        Set<UUID> userQuestIds = userQuests.stream()
                .map(Quest::getId)
                .collect(Collectors.toSet());

        long recurringDailyCount = recurringQuests.stream()
                .filter(q -> !userQuestIds.contains(q.getId()))
                .filter(q -> q.getFrequency() == Frequency.DAILY)
                .count();

        return (int) (userDailyCount + recurringDailyCount);
    }

    /**
     * Counts the number of daily quests the user has completed today.
     */
    private int countCompletedDailyQuests(UUID userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        List<QuestCompletion> completions = questCompletionRepository
                .findByUserIdAndCompletedAtBetween(userId, startOfDay, endOfDay);

        return completions.size();
    }

    /**
     * Checks if the user has already received the Perfect Day bonus today.
     * Prevents duplicate awards on repeated quest completions after achieving Perfect Day.
     */
    private boolean hasAlreadyReceivedPerfectDayBonus(UUID userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        List<XpHistory> todayHistory = xpHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return todayHistory.stream()
                .filter(h -> h.getCreatedAt() != null
                        && !h.getCreatedAt().isBefore(startOfDay)
                        && !h.getCreatedAt().isAfter(endOfDay))
                .anyMatch(h -> PERFECT_DAY_SOURCE_TYPE.equals(h.getSourceType()));
    }

    /**
     * Awards the Perfect Day bonus XP and publishes the PerfectDayEvent.
     */
    private void awardPerfectDayBonus(UUID userId, int totalQuestsCompleted) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Award bonus XP
        long newTotalXp = user.getXp() + PERFECT_DAY_BONUS_XP;
        user.setXp(newTotalXp);
        userRepository.save(user);

        // Log to xp_history
        XpHistory history = XpHistory.builder()
                .userId(userId)
                .sourceType(PERFECT_DAY_SOURCE_TYPE)
                .xpAmount(PERFECT_DAY_BONUS_XP)
                .multiplier(BigDecimal.ONE)
                .build();
        xpHistoryRepository.save(history);

        // Check for level-up after bonus
        int previousLevel = user.getLevel();
        int newLevel = LevelCalculator.calculateLevel(newTotalXp);
        if (newLevel > previousLevel) {
            user.setLevel(newLevel);
            userRepository.save(user);
            log.info("User {} leveled up from {} to {} via Perfect Day bonus!", userId, previousLevel, newLevel);
        }

        // 4. Publish PerfectDayEvent (triggers chest unlock + notifications)
        PerfectDayEvent event = new PerfectDayEvent(
                this,
                userId,
                PERFECT_DAY_BONUS_XP,
                totalQuestsCompleted,
                LocalDate.now()
        );
        eventPublisher.publishEvent(event);

        log.info("Perfect Day achieved for user {}! Awarded {} bonus XP. Quests completed: {}",
                userId, PERFECT_DAY_BONUS_XP, totalQuestsCompleted);
    }
}
