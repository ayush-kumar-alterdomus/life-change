package com.ascend.arc.service;

import com.ascend.arc.entity.ArcStatus;
import com.ascend.arc.entity.Arc;
import com.ascend.arc.entity.UserArcProgress;
import com.ascend.arc.entity.UserMilestoneCompletion;
import com.ascend.arc.event.DifficultyAdjustedEvent;
import com.ascend.arc.repository.ArcRepository;
import com.ascend.arc.repository.UserArcProgressRepository;
import com.ascend.arc.repository.UserMilestoneCompletionRepository;
import com.ascend.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for adaptive difficulty adjustment within arcs.
 * Evaluates user performance over the last 7 days and adjusts quest difficulty
 * to prevent frustration without failing the arc.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArcAdaptiveDifficultyService {

    private final ArcRepository arcRepository;
    private final UserArcProgressRepository userArcProgressRepository;
    private final UserMilestoneCompletionRepository userMilestoneCompletionRepository;
    private final ApplicationEventPublisher eventPublisher;

    private static final int EVALUATION_WINDOW_DAYS = 7;
    private static final double LOW_PERFORMANCE_THRESHOLD = 0.50;
    private static final double HIGH_PERFORMANCE_THRESHOLD = 0.90;

    /**
     * Evaluates user performance over the last 7 days and adjusts difficulty.
     * - If completion rate < 50% → reduce difficulty by one level
     * - If completion rate > 90% → increase difficulty by one level
     * - Never fails the arc due to performance decline
     *
     * Called weekly or on streak break within an arc.
     *
     * @param userId the user's ID
     * @param arcId  the arc ID
     * @return the new difficulty level, or null if no adjustment was made
     */
    @Transactional
    public String evaluatePerformance(UUID userId, UUID arcId) {
        log.info("Evaluating performance for user={} in arc={}", userId, arcId);

        // Verify active progress exists
        UserArcProgress progress = userArcProgressRepository.findByUserIdAndArcId(userId, arcId)
                .orElseThrow(() -> new BusinessException("ARC_PROGRESS_NOT_FOUND",
                        "No arc progress found for user=" + userId + " and arc=" + arcId));

        if (!ArcStatus.ACTIVE.name().equals(progress.getStatus())) {
            log.debug("Arc is not active, skipping difficulty evaluation");
            return null;
        }

        // Get the arc's current difficulty
        Arc arc = arcRepository.findById(arcId)
                .orElseThrow(() -> new BusinessException("ARC_NOT_FOUND",
                        "Arc not found with id: " + arcId));

        String currentDifficulty = arc.getDifficulty();

        // Calculate completion rate over last 7 days
        double completionRate = calculateCompletionRate(userId, arcId);

        log.debug("Completion rate for user={} in arc={}: {}%", userId, arcId, (int) (completionRate * 100));

        // Determine adjustment
        String newDifficulty = currentDifficulty;
        String reason = null;

        if (completionRate < LOW_PERFORMANCE_THRESHOLD) {
            newDifficulty = reduceDifficulty(currentDifficulty);
            reason = "Low completion rate (" + (int) (completionRate * 100) + "%) over last 7 days";
        } else if (completionRate > HIGH_PERFORMANCE_THRESHOLD) {
            newDifficulty = increaseDifficulty(currentDifficulty);
            reason = "High completion rate (" + (int) (completionRate * 100) + "%) over last 7 days";
        }

        // Apply adjustment if changed
        if (!newDifficulty.equals(currentDifficulty)) {
            arc.setDifficulty(newDifficulty);
            arcRepository.save(arc);

            // Publish DifficultyAdjustedEvent
            DifficultyAdjustedEvent event = DifficultyAdjustedEvent.builder()
                    .userId(userId)
                    .arcId(arcId)
                    .previousDifficulty(currentDifficulty)
                    .newDifficulty(newDifficulty)
                    .reason(reason)
                    .build();
            eventPublisher.publishEvent(event);

            log.info("Difficulty adjusted for user={} in arc={}: {} → {} ({})",
                    userId, arcId, currentDifficulty, newDifficulty, reason);

            return newDifficulty;
        }

        log.debug("No difficulty adjustment needed for user={} in arc={}", userId, arcId);
        return null;
    }

    /**
     * Calculates the milestone completion rate over the last 7 days.
     * Returns a value between 0.0 and 1.0.
     */
    private double calculateCompletionRate(UUID userId, UUID arcId) {
        LocalDateTime windowStart = LocalDateTime.now().minusDays(EVALUATION_WINDOW_DAYS);

        List<UserMilestoneCompletion> recentCompletions = userMilestoneCompletionRepository
                .findByUserIdAndArcId(userId, arcId)
                .stream()
                .filter(c -> c.getCompletedAt() != null && c.getCompletedAt().isAfter(windowStart))
                .toList();

        // Expected completions: roughly 1 per day for 7 days
        int expectedCompletions = EVALUATION_WINDOW_DAYS;
        int actualCompletions = recentCompletions.size();

        if (expectedCompletions == 0) return 1.0;

        return Math.min(1.0, (double) actualCompletions / expectedCompletions);
    }

    /**
     * Reduces difficulty by one level. EASY is the minimum.
     */
    private String reduceDifficulty(String current) {
        if (current == null) return "EASY";
        return switch (current.toUpperCase()) {
            case "HARD" -> "MEDIUM";
            case "MEDIUM" -> "EASY";
            default -> "EASY";
        };
    }

    /**
     * Increases difficulty by one level. HARD is the maximum.
     */
    private String increaseDifficulty(String current) {
        if (current == null) return "MEDIUM";
        return switch (current.toUpperCase()) {
            case "EASY" -> "MEDIUM";
            case "MEDIUM" -> "HARD";
            default -> "HARD";
        };
    }
}
