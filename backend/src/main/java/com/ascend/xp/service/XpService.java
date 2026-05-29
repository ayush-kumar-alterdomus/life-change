package com.ascend.xp.service;

import com.ascend.arc.entity.UserArcProgress;
import com.ascend.arc.repository.UserArcProgressRepository;
import com.ascend.quest.event.QuestCompletedEvent;
import com.ascend.skilltree.repository.UserSkillRepository;
import com.ascend.streak.entity.Streak;
import com.ascend.streak.repository.StreakRepository;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import com.ascend.xp.entity.XpHistory;
import com.ascend.xp.event.XpAwardedEvent;
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
import java.util.UUID;

/**
 * Core service for awarding XP, enforcing daily caps, and triggering level-ups.
 * All XP is calculated server-side — client values are never trusted.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class XpService {

    private static final int BASE_DAILY_CAP = 1000;
    private static final int CAP_PER_LEVEL = 20;
    private static final int BONUS_XP_PER_SKILL = 5;

    private final UserRepository userRepository;
    private final StreakRepository streakRepository;
    private final UserArcProgressRepository userArcProgressRepository;
    private final UserSkillRepository userSkillRepository;
    private final XpHistoryRepository xpHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Awards XP to a user based on a quest completion event.
     * Enforces daily cap, logs history, checks for level-up, and publishes event.
     *
     * @param userId the user receiving XP
     * @param event  the quest completed event with reward details
     */
    @Transactional
    public void awardXp(UUID userId, QuestCompletedEvent event) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // 1. Fetch streak for combo multiplier
        double streakMultiplier = getStreakMultiplier(userId);

        // 2. Fetch active arc multiplier
        double arcMultiplier = getArcMultiplier(userId);

        // 3. Fetch skill buffs (bonus XP from unlocked skills)
        int bonusXp = getSkillBonusXp(userId);

        // 4. Calculate final XP
        int calculatedXp = XpCalculator.calculateFinalXp(
                event.getBaseXpReward(),
                event.getDifficulty(),
                streakMultiplier,
                arcMultiplier,
                bonusXp
        );

        // 5. Check daily cap — reduce award if it would exceed
        long dailyXpEarned = getDailyXpEarned(userId);
        int dailyCap = getDailyCap(user.getLevel());
        int remainingCap = (int) Math.max(0, dailyCap - dailyXpEarned);
        int finalXpAwarded = Math.min(calculatedXp, remainingCap);

        if (finalXpAwarded <= 0) {
            log.info("User {} has reached daily XP cap ({}/{}). No XP awarded.",
                    userId, dailyXpEarned, dailyCap);
            return;
        }

        // 6. Update user's total XP
        long newTotalXp = user.getXp() + finalXpAwarded;
        user.setXp(newTotalXp);

        // 7. Log to xp_history
        double totalMultiplier = XpCalculator.getDifficultyMultiplier(event.getDifficulty())
                * streakMultiplier * arcMultiplier;
        XpHistory history = XpHistory.builder()
                .userId(userId)
                .sourceType("QUEST")
                .sourceId(event.getQuestId())
                .xpAmount(finalXpAwarded)
                .multiplier(BigDecimal.valueOf(totalMultiplier))
                .statType(event.getStatType() != null ? event.getStatType().name() : null)
                .build();
        xpHistoryRepository.save(history);

        // 8. Check for level-up
        int previousLevel = user.getLevel();
        int newLevel = LevelCalculator.calculateLevel(newTotalXp);
        if (newLevel > previousLevel) {
            user.setLevel(newLevel);
            log.info("User {} leveled up from {} to {}!", userId, previousLevel, newLevel);
        }

        userRepository.save(user);

        // 9. Publish XpAwardedEvent
        XpAwardedEvent xpEvent = new XpAwardedEvent(
                this,
                userId,
                finalXpAwarded,
                newTotalXp,
                newLevel,
                event.getStatType(),
                "QUEST"
        );
        eventPublisher.publishEvent(xpEvent);

        log.debug("Awarded {} XP to user {} (calculated={}, cap-limited from {}). Total: {}, Level: {}",
                finalXpAwarded, userId, calculatedXp, calculatedXp, newTotalXp, newLevel);
    }

    /**
     * Returns the total XP earned by the user today (for daily cap checking).
     *
     * @param userId the user ID
     * @return sum of today's XP history entries
     */
    public long getDailyXpEarned(UUID userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        return xpHistoryRepository.sumXpAmountByUserIdAndCreatedAtBetween(userId, startOfDay, endOfDay);
    }

    /**
     * Returns the daily XP cap for a given level.
     * Formula: 1000 + (level × 20)
     *
     * @param level the user's current level
     * @return daily XP cap
     */
    public int getDailyCap(int level) {
        return BASE_DAILY_CAP + (level * CAP_PER_LEVEL);
    }

    private double getStreakMultiplier(UUID userId) {
        return streakRepository.findByUserId(userId)
                .map(Streak::getCurrentStreak)
                .map(streak -> ComboCalculator.calculateComboMultiplier(streak))
                .orElse(1.0);
    }

    private double getArcMultiplier(UUID userId) {
        List<UserArcProgress> activeArcs = userArcProgressRepository
                .findByUserIdAndStatus(userId, "ACTIVE");

        if (activeArcs.isEmpty()) {
            return 1.0;
        }

        // Use the first active arc's progress as a small multiplier boost
        // Arc multiplier: 1.0 + (progressPercent / 1000.0), capped at 1.5
        UserArcProgress activeArc = activeArcs.get(0);
        double multiplier = 1.0 + (activeArc.getProgressPercent() / 1000.0);
        return Math.min(multiplier, 1.5);
    }

    private int getSkillBonusXp(UUID userId) {
        int unlockedSkillCount = userSkillRepository.findByUserIdAndUnlockedTrue(userId).size();
        return unlockedSkillCount * BONUS_XP_PER_SKILL;
    }
}
