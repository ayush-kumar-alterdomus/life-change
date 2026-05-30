package com.ascend.analytics.service;

import com.ascend.analytics.dto.WeeklyReportResponse;
import com.ascend.common.entity.StatType;
import com.ascend.quest.entity.Quest;
import com.ascend.quest.entity.QuestCompletion;
import com.ascend.quest.repository.QuestCompletionRepository;
import com.ascend.quest.repository.QuestRepository;
import com.ascend.streak.repository.StreakRepository;
import com.ascend.user.entity.UserStats;
import com.ascend.user.repository.UserStatsRepository;
import com.ascend.xp.repository.XpHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service responsible for generating weekly progress reports for users.
 * Aggregates quest completions, XP earned, stat gains, and produces
 * actionable recommendations based on the user's activity patterns.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeeklyReportService {

    private final QuestCompletionRepository questCompletionRepository;
    private final QuestRepository questRepository;
    private final XpHistoryRepository xpHistoryRepository;
    private final UserStatsRepository userStatsRepository;
    private final StreakRepository streakRepository;

    /**
     * Generates a weekly report for the given user covering the current week
     * (Monday to Sunday).
     *
     * @param userId the user's ID
     * @return populated WeeklyReportResponse with stats, recommendations, and Life Score
     */
    public WeeklyReportResponse generateWeeklyReport(UUID userId) {
        LocalDateTime weekStart = getWeekStart();
        LocalDateTime weekEnd = getWeekEnd();

        log.info("Generating weekly report for user={} from {} to {}", userId, weekStart, weekEnd);

        // 1. Count quests completed this week
        List<QuestCompletion> completions = questCompletionRepository
                .findByUserIdAndCompletedAtBetween(userId, weekStart, weekEnd);
        int questsCompleted = completions.size();

        // 2. Count quests missed (assigned but not completed)
        int questsMissed = calculateQuestsMissed(userId, completions, weekStart, weekEnd);

        // 3. Sum XP earned this week
        long xpEarned = xpHistoryRepository
                .sumXpAmountByUserIdAndCreatedAtBetween(userId, weekStart, weekEnd);

        // 4. Identify strongest stat (highest gain this week)
        Map<StatType, Integer> statGains = calculateStatGains(completions);
        String strongestStat = identifyStrongestStat(statGains);

        // 5. Identify weakest stat (lowest gain this week)
        String weakestStat = identifyWeakestStat(statGains);

        // 6. Generate 1-3 recommendations based on patterns
        List<String> recommendations = generateRecommendations(
                userId, questsCompleted, questsMissed, statGains, xpEarned);

        // 7. Calculate current Life Score
        BigDecimal lifeScore = calculateLifeScore(userId);

        WeeklyReportResponse report = WeeklyReportResponse.builder()
                .questsCompleted(questsCompleted)
                .questsMissed(questsMissed)
                .xpEarned(xpEarned)
                .strongestStat(strongestStat)
                .weakestStat(weakestStat)
                .recommendations(recommendations)
                .lifeScore(lifeScore)
                .build();

        log.info("Weekly report generated for user={}: completed={}, missed={}, xp={}",
                userId, questsCompleted, questsMissed, xpEarned);

        return report;
    }

    /**
     * Calculates the number of quests missed this week.
     * A quest is considered missed if it was assigned (recurring or user-created)
     * but not completed within the week.
     */
    private int calculateQuestsMissed(UUID userId, List<QuestCompletion> completions,
                                      LocalDateTime weekStart, LocalDateTime weekEnd) {
        // Get quests assigned to the user (created by them or recurring system quests)
        List<Quest> userQuests = questRepository.findByCreatedBy_Id(userId);
        List<Quest> recurringQuests = questRepository.findByRecurringTrue();

        // Combine unique quest IDs that were "assigned" this week
        List<UUID> completedQuestIds = completions.stream()
                .map(QuestCompletion::getQuestId)
                .distinct()
                .collect(Collectors.toList());

        // Count user-created quests not completed
        long missedUserQuests = userQuests.stream()
                .filter(q -> !completedQuestIds.contains(q.getId()))
                .count();

        // Count recurring quests not completed (only those created before week end)
        long missedRecurring = recurringQuests.stream()
                .filter(q -> q.getCreatedAt() != null && q.getCreatedAt().isBefore(weekEnd))
                .filter(q -> !completedQuestIds.contains(q.getId()))
                .count();

        return (int) Math.max(0, missedUserQuests + missedRecurring - userQuests.size());
    }

    /**
     * Calculates stat gains from quest completions by looking up the stat type
     * of each completed quest and summing XP earned per stat.
     */
    private Map<StatType, Integer> calculateStatGains(List<QuestCompletion> completions) {
        Map<StatType, Integer> gains = new EnumMap<>(StatType.class);

        // Initialize all stats to 0
        for (StatType stat : StatType.values()) {
            gains.put(stat, 0);
        }

        // Look up each completed quest's stat type and accumulate XP
        List<UUID> questIds = completions.stream()
                .map(QuestCompletion::getQuestId)
                .distinct()
                .collect(Collectors.toList());

        Map<UUID, Quest> questMap = questRepository.findAllById(questIds).stream()
                .collect(Collectors.toMap(Quest::getId, q -> q));

        for (QuestCompletion completion : completions) {
            Quest quest = questMap.get(completion.getQuestId());
            if (quest != null && quest.getStatType() != null) {
                int currentGain = gains.getOrDefault(quest.getStatType(), 0);
                int xpForCompletion = completion.getXpEarned() != null ? completion.getXpEarned() : 0;
                gains.put(quest.getStatType(), currentGain + xpForCompletion);
            }
        }

        return gains;
    }

    /**
     * Identifies the stat with the highest gain this week.
     */
    private String identifyStrongestStat(Map<StatType, Integer> statGains) {
        return statGains.entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(entry -> entry.getKey().name())
                .orElse("NONE");
    }

    /**
     * Identifies the stat with the lowest gain this week.
     * Only considers stats that have at least some activity to avoid always returning
     * a stat with zero gain when the user only focuses on one area.
     */
    private String identifyWeakestStat(Map<StatType, Integer> statGains) {
        // First try to find the weakest among stats with some activity
        Optional<Map.Entry<StatType, Integer>> weakestActive = statGains.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .min(Comparator.comparingInt(Map.Entry::getValue));

        if (weakestActive.isPresent()) {
            return weakestActive.get().getKey().name();
        }

        // If no stats have activity, return the first stat with zero gain
        return statGains.entrySet().stream()
                .min(Comparator.comparingInt(Map.Entry::getValue))
                .map(entry -> entry.getKey().name())
                .orElse("NONE");
    }

    /**
     * Generates 1-3 actionable recommendations based on the user's weekly patterns.
     */
    private List<String> generateRecommendations(UUID userId, int questsCompleted,
                                                  int questsMissed,
                                                  Map<StatType, Integer> statGains,
                                                  long xpEarned) {
        List<String> recommendations = new ArrayList<>();

        // Recommendation based on missed quests
        if (questsMissed > questsCompleted && questsCompleted > 0) {
            recommendations.add("You missed more quests than you completed this week. "
                    + "Consider reducing your daily quest load or setting reminders.");
        } else if (questsMissed > 0 && questsCompleted > 0) {
            recommendations.add("Try to complete all assigned quests. "
                    + "You missed " + questsMissed + " this week — consistency builds momentum.");
        }

        // Recommendation based on stat imbalance
        String strongest = identifyStrongestStat(statGains);
        String weakest = identifyWeakestStat(statGains);
        if (!strongest.equals(weakest) && !strongest.equals("NONE") && !weakest.equals("NONE")) {
            int strongestGain = statGains.values().stream().max(Integer::compareTo).orElse(0);
            int weakestGain = statGains.values().stream()
                    .filter(v -> v > 0)
                    .min(Integer::compareTo)
                    .orElse(0);

            if (strongestGain > 0 && weakestGain >= 0 && strongestGain > weakestGain * 3) {
                recommendations.add("Your " + weakest + " stat is falling behind. "
                        + "Add a quest targeting " + weakest + " to balance your growth.");
            }
        }

        // Recommendation based on overall activity
        if (questsCompleted == 0) {
            recommendations.add("You didn't complete any quests this week. "
                    + "Start small — even one quest per day builds a powerful habit.");
        } else if (xpEarned > 0) {
            // Check streak for consistency recommendation
            streakRepository.findByUserId(userId).ifPresent(streak -> {
                if (streak.getCurrentStreak() >= 7) {
                    recommendations.add("Great streak! You've been consistent for "
                            + streak.getCurrentStreak() + " days. Keep it up for bonus XP.");
                } else if (streak.getCurrentStreak() < 3) {
                    recommendations.add("Build your streak back up — "
                            + "consecutive days multiply your XP gains.");
                }
            });
        }

        // Limit to 3 recommendations max
        return recommendations.stream().limit(3).collect(Collectors.toList());
    }

    /**
     * Calculates the Life Score for a user based on the formula:
     * 0.25×Discipline + 0.2×Focus + 0.2×Vitality + 0.2×Wisdom + 0.15×Consistency
     * Normalized to a 0-100 scale.
     */
    private BigDecimal calculateLifeScore(UUID userId) {
        Optional<UserStats> statsOpt = userStatsRepository.findByUserId(userId);
        if (statsOpt.isEmpty()) {
            return BigDecimal.ZERO;
        }

        UserStats stats = statsOpt.get();

        // Get consistency metric from streak data
        int consistency = streakRepository.findByUserId(userId)
                .map(streak -> Math.min(streak.getCurrentStreak(), 100))
                .orElse(0);

        // Apply Life Score formula
        // Raw score components (stats are integer values, normalize each to 0-100 range)
        double disciplineNorm = normalizeStatValue(stats.getDiscipline());
        double focusNorm = normalizeStatValue(stats.getFocus());
        double vitalityNorm = normalizeStatValue(stats.getVitality());
        double wisdomNorm = normalizeStatValue(stats.getWisdom());
        double consistencyNorm = Math.min(consistency, 100.0);

        double rawScore = (0.25 * disciplineNorm)
                + (0.20 * focusNorm)
                + (0.20 * vitalityNorm)
                + (0.20 * wisdomNorm)
                + (0.15 * consistencyNorm);

        // Clamp to 0-100
        double clampedScore = Math.max(0.0, Math.min(100.0, rawScore));

        return BigDecimal.valueOf(clampedScore).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Normalizes a stat value to a 0-100 scale.
     * Assumes stat values grow over time; uses a soft cap at 1000 for normalization.
     */
    private double normalizeStatValue(int statValue) {
        if (statValue <= 0) {
            return 0.0;
        }
        // Soft cap: 1000 stat points = 100 normalized
        return Math.min(100.0, (statValue / 10.0));
    }

    /**
     * Returns the start of the current week (Monday at 00:00:00).
     */
    private LocalDateTime getWeekStart() {
        return LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay();
    }

    /**
     * Returns the end of the current week (Sunday at 23:59:59).
     */
    private LocalDateTime getWeekEnd() {
        return LocalDate.now()
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                .atTime(LocalTime.MAX);
    }
}
