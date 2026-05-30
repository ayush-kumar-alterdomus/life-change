package com.ascend.analytics.service;

import com.ascend.analytics.dto.DashboardResponse;
import com.ascend.analytics.dto.HeatmapResponse;
import com.ascend.quest.entity.QuestCompletion;
import com.ascend.quest.repository.QuestCompletionRepository;
import com.ascend.quest.repository.QuestRepository;
import com.ascend.streak.repository.StreakRepository;
import com.ascend.user.repository.UserStatsRepository;
import com.ascend.xp.entity.XpHistory;
import com.ascend.xp.repository.XpHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service responsible for aggregating analytics data for the user dashboard
 * and activity heatmap visualization.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private static final int DASHBOARD_DAYS = 30;

    private final XpHistoryRepository xpHistoryRepository;
    private final QuestCompletionRepository questCompletionRepository;
    private final QuestRepository questRepository;
    private final StreakRepository streakRepository;
    private final UserStatsRepository userStatsRepository;

    /**
     * Builds the full analytics dashboard for a user.
     * Aggregates XP growth over the last 30 days, calculates quest completion rate,
     * retrieves streak history, and computes stat trends (weekly averages).
     *
     * @param userId the user's ID
     * @return populated DashboardResponse
     */
    public DashboardResponse getDashboard(UUID userId) {
        LocalDateTime thirtyDaysAgo = LocalDate.now().minusDays(DASHBOARD_DAYS).atStartOfDay();
        LocalDateTime now = LocalDate.now().atTime(LocalTime.MAX);

        List<DashboardResponse.XpGrowthEntry> xpGrowth = aggregateXpGrowth(userId, thirtyDaysAgo, now);
        double questCompletionRate = calculateQuestCompletionRate(userId, thirtyDaysAgo, now);
        List<DashboardResponse.StreakHistoryEntry> streakHistory = getStreakHistory(userId);
        Map<String, Double> statTrends = getStatTrends(userId, thirtyDaysAgo, now);

        int levelGrowth = calculateLevelGrowth(xpGrowth);

        return DashboardResponse.builder()
                .xpGrowth(xpGrowth)
                .levelGrowth(levelGrowth)
                .questCompletionRate(questCompletionRate)
                .streakHistory(streakHistory)
                .statTrends(statTrends)
                .build();
    }

    /**
     * Generates heatmap data showing quest completions per day over the specified period.
     * Returns a date → count map suitable for heatmap visualization (similar to GitHub contributions).
     *
     * @param userId the user's ID
     * @param days   number of days to include in the heatmap
     * @return populated HeatmapResponse
     */
    public HeatmapResponse getHeatmap(UUID userId, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<QuestCompletion> completions = questCompletionRepository
                .findByUserIdAndCompletedAtBetween(userId, start, end);

        // Group completions by date and count
        Map<LocalDate, Long> countsByDate = completions.stream()
                .collect(Collectors.groupingBy(
                        qc -> qc.getCompletedAt().toLocalDate(),
                        Collectors.counting()
                ));

        // Build heatmap entries for every day in the range (including zero-count days)
        List<HeatmapResponse.HeatmapEntry> entries = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            int count = countsByDate.getOrDefault(date, 0L).intValue();
            entries.add(HeatmapResponse.HeatmapEntry.builder()
                    .date(date)
                    .completionCount(count)
                    .build());
        }

        return HeatmapResponse.builder()
                .data(entries)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    /**
     * Aggregates daily XP earned over the given period, building cumulative totals.
     */
    private List<DashboardResponse.XpGrowthEntry> aggregateXpGrowth(UUID userId,
                                                                     LocalDateTime start,
                                                                     LocalDateTime end) {
        List<XpHistory> xpEntries = xpHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);

        // Filter to the date range and group by date
        Map<LocalDate, Long> dailyXp = xpEntries.stream()
                .filter(xp -> !xp.getCreatedAt().isBefore(start) && !xp.getCreatedAt().isAfter(end))
                .collect(Collectors.groupingBy(
                        xp -> xp.getCreatedAt().toLocalDate(),
                        Collectors.summingLong(XpHistory::getXpAmount)
                ));

        // Build entries for each day in the range with cumulative XP
        List<DashboardResponse.XpGrowthEntry> entries = new ArrayList<>();
        long cumulative = 0;
        LocalDate startDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            long dayXp = dailyXp.getOrDefault(date, 0L);
            cumulative += dayXp;
            entries.add(DashboardResponse.XpGrowthEntry.builder()
                    .date(date.toString())
                    .xpEarned(dayXp)
                    .cumulativeXp(cumulative)
                    .build());
        }

        return entries;
    }

    /**
     * Calculates quest completion rate as completed / assigned within the period.
     * Uses quests created by the user as "assigned" and completions as "completed".
     */
    private double calculateQuestCompletionRate(UUID userId, LocalDateTime start, LocalDateTime end) {
        long assignedCount = questRepository.countByCreatedBy_IdAndCustomTrue(userId);
        // Also count system quests assigned to the user (non-custom quests they've attempted)
        long totalAssigned = Math.max(assignedCount, 1); // Avoid division by zero

        List<QuestCompletion> completions = questCompletionRepository
                .findByUserIdAndCompletedAtBetween(userId, start, end);
        long completedCount = completions.size();

        // If no quests assigned, use completions as both numerator and denominator context
        if (totalAssigned == 0) {
            return completedCount > 0 ? 1.0 : 0.0;
        }

        return Math.min(1.0, (double) completedCount / totalAssigned);
    }

    /**
     * Retrieves streak history for the user.
     * Returns the current streak state as a single-entry history.
     * A more detailed history would require a streak_history table.
     */
    private List<DashboardResponse.StreakHistoryEntry> getStreakHistory(UUID userId) {
        List<DashboardResponse.StreakHistoryEntry> history = new ArrayList<>();

        streakRepository.findByUserId(userId).ifPresent(streak -> {
            // Build a simplified streak history based on current streak data
            if (streak.getLastCompletedAt() != null) {
                LocalDate lastDate = streak.getLastCompletedAt().toLocalDate();
                int currentStreak = streak.getCurrentStreak();

                // Reconstruct recent streak days (working backwards from last completion)
                for (int i = currentStreak - 1; i >= 0; i--) {
                    LocalDate date = lastDate.minusDays(i);
                    history.add(DashboardResponse.StreakHistoryEntry.builder()
                            .date(date.toString())
                            .streakCount(currentStreak - i)
                            .build());
                }
            }
        });

        return history;
    }

    /**
     * Computes stat trends as weekly averages of XP earned per stat type.
     * Groups XP history by stat type and calculates average weekly XP for each.
     */
    private Map<String, Double> getStatTrends(UUID userId, LocalDateTime start, LocalDateTime end) {
        List<XpHistory> xpEntries = xpHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);

        // Filter to date range and group by stat type
        Map<String, Long> totalXpByStat = xpEntries.stream()
                .filter(xp -> !xp.getCreatedAt().isBefore(start) && !xp.getCreatedAt().isAfter(end))
                .filter(xp -> xp.getStatType() != null)
                .collect(Collectors.groupingBy(
                        XpHistory::getStatType,
                        Collectors.summingLong(XpHistory::getXpAmount)
                ));

        // Calculate weekly average (divide by number of weeks in the period)
        double weeks = DASHBOARD_DAYS / 7.0;
        Map<String, Double> trends = new HashMap<>();
        totalXpByStat.forEach((stat, totalXp) ->
                trends.put(stat, totalXp / weeks)
        );

        // Include stats from UserStats that might have zero XP this period
        userStatsRepository.findByUserId(userId).ifPresent(stats -> {
            trends.putIfAbsent("DISCIPLINE", 0.0);
            trends.putIfAbsent("FOCUS", 0.0);
            trends.putIfAbsent("VITALITY", 0.0);
            trends.putIfAbsent("WISDOM", 0.0);
            trends.putIfAbsent("STRENGTH", 0.0);
            trends.putIfAbsent("CHARISMA", 0.0);
        });

        return trends;
    }

    /**
     * Estimates level growth from XP growth data.
     * Uses a simplified calculation based on total XP earned in the period.
     */
    private int calculateLevelGrowth(List<DashboardResponse.XpGrowthEntry> xpGrowth) {
        if (xpGrowth.isEmpty()) {
            return 0;
        }
        long totalXpEarned = xpGrowth.stream()
                .mapToLong(DashboardResponse.XpGrowthEntry::getXpEarned)
                .sum();
        // Approximate level growth: every 500 XP is roughly 1 level at early stages
        // This is a simplified estimate for dashboard display
        return (int) (totalXpEarned / 500);
    }
}
