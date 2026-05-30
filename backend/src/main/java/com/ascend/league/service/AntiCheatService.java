package com.ascend.league.service;

import com.ascend.league.entity.SecurityViolation;
import com.ascend.league.entity.ViolationType;
import com.ascend.league.repository.LeaderboardRepository;
import com.ascend.league.repository.SecurityViolationRepository;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import com.ascend.xp.repository.XpHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Anti-cheat detection service that monitors quest completion patterns
 * for suspicious activity. Called on each QuestCompletedEvent to detect:
 * <ul>
 *   <li>Speed violations: >10 completions in 5 minutes</li>
 *   <li>Bulk spam: >50 completions in 2 minutes</li>
 * </ul>
 *
 * When a violation is detected, the service:
 * <ol>
 *   <li>Flags the account with a SecurityViolation record</li>
 *   <li>Rolls back XP earned during the violation window</li>
 *   <li>Bans the user from the leaderboard</li>
 *   <li>Logs the security event</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AntiCheatService {

    private static final int SPEED_VIOLATION_THRESHOLD = 10;
    private static final int SPEED_VIOLATION_WINDOW_MINUTES = 5;

    private static final int BULK_SPAM_THRESHOLD = 50;
    private static final int BULK_SPAM_WINDOW_MINUTES = 2;

    private final XpHistoryRepository xpHistoryRepository;
    private final SecurityViolationRepository securityViolationRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final UserRepository userRepository;

    /**
     * Detects speed violations: more than 10 quest completions in the last 5 minutes.
     * If detected, flags the account, rolls back XP, and bans from leaderboard.
     *
     * @param userId the user to check
     * @return true if a speed violation was detected
     */
    @Transactional
    public boolean detectSpeedViolation(UUID userId) {
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(SPEED_VIOLATION_WINDOW_MINUTES);

        long completionCount = countQuestCompletionsAfter(userId, windowStart);

        if (completionCount > SPEED_VIOLATION_THRESHOLD) {
            log.warn("ANTI-CHEAT: Speed violation detected for user {}. {} completions in {} minutes.",
                    userId, completionCount, SPEED_VIOLATION_WINDOW_MINUTES);

            long xpRolledBack = rollbackXp(userId, windowStart);
            banFromLeaderboard(userId);

            SecurityViolation violation = SecurityViolation.builder()
                    .userId(userId)
                    .violationType(ViolationType.SPEED_VIOLATION)
                    .description(String.format(
                            "Speed violation: %d quest completions in %d minutes (threshold: %d)",
                            completionCount, SPEED_VIOLATION_WINDOW_MINUTES, SPEED_VIOLATION_THRESHOLD))
                    .completionsDetected((int) completionCount)
                    .timeWindowMinutes(SPEED_VIOLATION_WINDOW_MINUTES)
                    .xpRolledBack(xpRolledBack)
                    .leaderboardBanned(true)
                    .build();

            securityViolationRepository.save(violation);

            log.info("SECURITY EVENT: User {} flagged for speed violation. XP rolled back: {}, leaderboard banned.",
                    userId, xpRolledBack);

            return true;
        }

        return false;
    }

    /**
     * Detects bulk spam: more than 50 quest completions in the last 2 minutes.
     * If detected, flags the account, rolls back XP, and bans from leaderboard.
     *
     * @param userId the user to check
     * @return true if bulk spam was detected
     */
    @Transactional
    public boolean detectBulkSpam(UUID userId) {
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(BULK_SPAM_WINDOW_MINUTES);

        long completionCount = countQuestCompletionsAfter(userId, windowStart);

        if (completionCount > BULK_SPAM_THRESHOLD) {
            log.warn("ANTI-CHEAT: Bulk spam detected for user {}. {} completions in {} minutes.",
                    userId, completionCount, BULK_SPAM_WINDOW_MINUTES);

            long xpRolledBack = rollbackXp(userId, windowStart);
            banFromLeaderboard(userId);

            SecurityViolation violation = SecurityViolation.builder()
                    .userId(userId)
                    .violationType(ViolationType.BULK_SPAM)
                    .description(String.format(
                            "Bulk spam: %d quest completions in %d minutes (threshold: %d)",
                            completionCount, BULK_SPAM_WINDOW_MINUTES, BULK_SPAM_THRESHOLD))
                    .completionsDetected((int) completionCount)
                    .timeWindowMinutes(BULK_SPAM_WINDOW_MINUTES)
                    .xpRolledBack(xpRolledBack)
                    .leaderboardBanned(true)
                    .build();

            securityViolationRepository.save(violation);

            log.info("SECURITY EVENT: User {} flagged for bulk spam. XP rolled back: {}, leaderboard banned.",
                    userId, xpRolledBack);

            return true;
        }

        return false;
    }

    /**
     * Checks if a user is currently banned from the leaderboard due to unresolved violations.
     *
     * @param userId the user to check
     * @return true if the user has an active leaderboard ban
     */
    public boolean isLeaderboardBanned(UUID userId) {
        return securityViolationRepository.existsByUserIdAndLeaderboardBannedTrue(userId);
    }

    /**
     * Counts quest completions (source_type = 'QUEST') for a user after a given timestamp.
     */
    private long countQuestCompletionsAfter(UUID userId, LocalDateTime after) {
        return xpHistoryRepository.countByUserIdAndSourceTypeAndCreatedAtAfter(userId, "QUEST", after);
    }

    /**
     * Rolls back XP earned by the user within the violation window.
     * Subtracts the XP from the user's total and returns the amount rolled back.
     */
    private long rollbackXp(UUID userId, LocalDateTime windowStart) {
        Long xpInWindow = xpHistoryRepository.sumXpAmountByUserIdAndCreatedAtBetween(
                userId, windowStart, LocalDateTime.now());

        if (xpInWindow == null || xpInWindow <= 0) {
            return 0L;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            long newXp = Math.max(0, user.getXp() - xpInWindow);
            user.setXp(newXp);
            userRepository.save(user);
            log.info("Rolled back {} XP from user {}. New total: {}", xpInWindow, userId, newXp);
        }

        return xpInWindow;
    }

    /**
     * Bans the user from the leaderboard by removing their leaderboard entry.
     */
    private void banFromLeaderboard(UUID userId) {
        leaderboardRepository.findByUserId(userId).ifPresent(entry -> {
            leaderboardRepository.delete(entry);
            log.info("User {} removed from leaderboard due to anti-cheat violation.", userId);
        });
    }
}
