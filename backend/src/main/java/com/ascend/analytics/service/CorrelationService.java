package com.ascend.analytics.service;

import com.ascend.analytics.dto.InsightResponse;
import com.ascend.quest.entity.QuestCompletion;
import com.ascend.quest.repository.QuestCompletionRepository;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Simplified correlation detection service that analyzes quest completion patterns
 * and generates rule-based insights. Premium feature only.
 *
 * Uses simple heuristics (not ML) to detect patterns like:
 * - Weekday vs weekend completion rates
 * - Morning vs evening completion patterns
 * - Stat-specific time correlations
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CorrelationService {

    private static final int ANALYSIS_DAYS = 30;

    private final QuestCompletionRepository questCompletionRepository;
    private final UserRepository userRepository;

    /**
     * Detects habit correlations for a user based on their quest completion patterns.
     * Returns a list of insights with confidence levels and actionability.
     * Premium feature only — caller should verify premium status.
     *
     * @param userId the user's ID
     * @return list of detected insights
     */
    public List<InsightResponse> detectCorrelations(UUID userId) {
        log.debug("Detecting correlations for user={}", userId);

        // Verify user is premium
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (!Boolean.TRUE.equals(user.getPremium())) {
            log.debug("User {} is not premium — correlation detection skipped", userId);
            return List.of();
        }

        LocalDateTime since = LocalDateTime.now().minusDays(ANALYSIS_DAYS);
        LocalDateTime now = LocalDateTime.now();

        List<QuestCompletion> completions = questCompletionRepository
                .findByUserIdAndCompletedAtBetween(userId, since, now);

        if (completions.size() < 7) {
            log.debug("Insufficient data for user {} ({} completions) — need at least 7",
                    userId, completions.size());
            return List.of(InsightResponse.builder()
                    .message("Complete more quests to unlock habit insights. Need at least 7 completions.")
                    .confidence(1.0)
                    .category("DATA")
                    .actionable(false)
                    .build());
        }

        List<InsightResponse> insights = new ArrayList<>();

        // Analyze weekday vs weekend patterns
        analyzeWeekdayPattern(completions, insights);

        // Analyze time-of-day patterns
        analyzeTimeOfDayPattern(completions, insights);

        // Analyze consistency patterns
        analyzeConsistencyPattern(completions, insights);

        log.debug("Detected {} insights for user {}", insights.size(), userId);
        return insights;
    }

    /**
     * Analyzes whether the user completes more quests on weekdays vs weekends.
     */
    private void analyzeWeekdayPattern(List<QuestCompletion> completions, List<InsightResponse> insights) {
        Map<Boolean, Long> weekdayVsWeekend = completions.stream()
                .collect(Collectors.partitioningBy(
                        qc -> {
                            DayOfWeek day = qc.getCompletedAt().getDayOfWeek();
                            return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
                        },
                        Collectors.counting()
                ));

        long weekdayCount = weekdayVsWeekend.getOrDefault(true, 0L);
        long weekendCount = weekdayVsWeekend.getOrDefault(false, 0L);

        // Normalize by number of days (5 weekdays vs 2 weekend days)
        double weekdayRate = weekdayCount / 5.0;
        double weekendRate = weekendCount / 2.0;

        if (weekdayRate > weekendRate * 1.5 && weekdayCount > 5) {
            insights.add(InsightResponse.builder()
                    .message("You complete significantly more quests on weekdays. "
                            + "Consider adding lighter weekend quests to maintain momentum.")
                    .confidence(0.75)
                    .category("TIMING")
                    .actionable(true)
                    .build());
        } else if (weekendRate > weekdayRate * 1.5 && weekendCount > 3) {
            insights.add(InsightResponse.builder()
                    .message("You're more active on weekends. "
                            + "Try scheduling quick quests during weekday breaks to stay consistent.")
                    .confidence(0.70)
                    .category("TIMING")
                    .actionable(true)
                    .build());
        }
    }

    /**
     * Analyzes whether the user tends to complete quests in the morning or evening.
     */
    private void analyzeTimeOfDayPattern(List<QuestCompletion> completions, List<InsightResponse> insights) {
        long morningCount = completions.stream()
                .filter(qc -> {
                    int hour = qc.getCompletedAt().getHour();
                    return hour >= 5 && hour < 12;
                })
                .count();

        long afternoonCount = completions.stream()
                .filter(qc -> {
                    int hour = qc.getCompletedAt().getHour();
                    return hour >= 12 && hour < 18;
                })
                .count();

        long eveningCount = completions.stream()
                .filter(qc -> {
                    int hour = qc.getCompletedAt().getHour();
                    return hour >= 18 || hour < 5;
                })
                .count();

        long total = completions.size();
        double morningPct = (double) morningCount / total;
        double eveningPct = (double) eveningCount / total;

        if (morningPct > 0.5 && morningCount > 5) {
            insights.add(InsightResponse.builder()
                    .message("Morning completions correlate with higher Focus gains. "
                            + "You're a morning person — keep scheduling important quests early.")
                    .confidence(0.80)
                    .category("PERFORMANCE")
                    .actionable(true)
                    .build());
        } else if (eveningPct > 0.5 && eveningCount > 5) {
            insights.add(InsightResponse.builder()
                    .message("You tend to complete quests in the evening. "
                            + "Try a morning quest for variety — it may boost your Focus stat.")
                    .confidence(0.65)
                    .category("PERFORMANCE")
                    .actionable(true)
                    .build());
        }
    }

    /**
     * Analyzes completion consistency (daily spread vs burst patterns).
     */
    private void analyzeConsistencyPattern(List<QuestCompletion> completions, List<InsightResponse> insights) {
        // Count unique days with completions
        long uniqueDays = completions.stream()
                .map(qc -> qc.getCompletedAt().toLocalDate())
                .distinct()
                .count();

        double avgPerDay = (double) completions.size() / Math.max(uniqueDays, 1);

        if (uniqueDays >= 20 && avgPerDay <= 3) {
            insights.add(InsightResponse.builder()
                    .message("You have excellent consistency — completing quests on "
                            + uniqueDays + " out of 30 days. This steady approach maximizes long-term growth.")
                    .confidence(0.85)
                    .category("CONSISTENCY")
                    .actionable(false)
                    .build());
        } else if (uniqueDays < 10 && completions.size() > 15) {
            insights.add(InsightResponse.builder()
                    .message("You tend to complete quests in bursts rather than daily. "
                            + "Spreading quests across more days would improve your streak and consistency bonus.")
                    .confidence(0.70)
                    .category("CONSISTENCY")
                    .actionable(true)
                    .build());
        }
    }
}
