package com.ascend.analytics.service;

import com.ascend.analytics.dto.DashboardResponse;
import com.ascend.analytics.dto.HeatmapResponse;
import com.ascend.analytics.dto.WeeklyReportResponse;
import com.ascend.quest.entity.QuestCompletion;
import com.ascend.quest.repository.QuestCompletionRepository;
import com.ascend.quest.repository.QuestRepository;
import com.ascend.streak.entity.Streak;
import com.ascend.streak.repository.StreakRepository;
import com.ascend.user.entity.UserStats;
import com.ascend.user.repository.UserStatsRepository;
import com.ascend.user.service.LifeScoreCalculator;
import com.ascend.xp.entity.XpHistory;
import com.ascend.xp.repository.XpHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsIntegrationTest {

    @Mock private XpHistoryRepository xpHistoryRepository;
    @Mock private QuestCompletionRepository questCompletionRepository;
    @Mock private QuestRepository questRepository;
    @Mock private StreakRepository streakRepository;
    @Mock private UserStatsRepository userStatsRepository;

    private AnalyticsService analyticsService;
    private LifeScoreService lifeScoreService;
    private LifeScoreCalculator lifeScoreCalculator;

    private UUID userId;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsService(
                xpHistoryRepository, questCompletionRepository,
                questRepository, streakRepository, userStatsRepository);
        lifeScoreCalculator = new LifeScoreCalculator();
        lifeScoreService = new LifeScoreService(
                userStatsRepository, streakRepository, lifeScoreCalculator);
        userId = UUID.randomUUID();
    }

    // ========================================================================
    // Integration test: complete quests over multiple days → dashboard shows trends
    // ========================================================================

    @Nested
    @DisplayName("Dashboard shows trends from quest completions")
    class DashboardTrends {

        @Test
        @DisplayName("Dashboard aggregates XP growth over 30 days")
        void dashboard_showsXpGrowth() {
            List<XpHistory> xpEntries = List.of(
                    XpHistory.builder().userId(userId).xpAmount(100)
                            .createdAt(LocalDateTime.now().minusDays(5)).statType("STRENGTH").build(),
                    XpHistory.builder().userId(userId).xpAmount(150)
                            .createdAt(LocalDateTime.now().minusDays(3)).statType("FOCUS").build(),
                    XpHistory.builder().userId(userId).xpAmount(200)
                            .createdAt(LocalDateTime.now().minusDays(1)).statType("WISDOM").build()
            );

            when(xpHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(xpEntries);
            when(questCompletionRepository.findByUserIdAndCompletedAtBetween(eq(userId), any(), any()))
                    .thenReturn(List.of());
            when(questRepository.countByCreatedBy_IdAndCustomTrue(userId)).thenReturn(0L);
            when(streakRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(userStatsRepository.findByUserId(userId)).thenReturn(Optional.empty());

            DashboardResponse dashboard = analyticsService.getDashboard(userId);

            assertThat(dashboard).isNotNull();
            assertThat(dashboard.getXpGrowth()).isNotEmpty();
            assertThat(dashboard.getStatTrends()).isNotNull();
        }

        @Test
        @DisplayName("Heatmap shows completions per day")
        void heatmap_showsCompletionsPerDay() {
            List<QuestCompletion> completions = List.of(
                    createCompletion(LocalDateTime.now().minusDays(2)),
                    createCompletion(LocalDateTime.now().minusDays(2)),
                    createCompletion(LocalDateTime.now().minusDays(1))
            );

            when(questCompletionRepository.findByUserIdAndCompletedAtBetween(eq(userId), any(), any()))
                    .thenReturn(completions);

            HeatmapResponse heatmap = analyticsService.getHeatmap(userId, 7);

            assertThat(heatmap).isNotNull();
            assertThat(heatmap.getData()).isNotEmpty();
            assertThat(heatmap.getStartDate()).isNotNull();
            assertThat(heatmap.getEndDate()).isNotNull();

            // Should have entries for all 7 days + today
            assertThat(heatmap.getData().size()).isGreaterThanOrEqualTo(7);
        }
    }

    // ========================================================================
    // Integration test: Sunday → weekly report generated with correct stats
    // ========================================================================

    @Nested
    @DisplayName("Weekly report generation")
    class WeeklyReportGeneration {

        @Test
        @DisplayName("Weekly report contains all required fields")
        void weeklyReport_containsAllFields() {
            WeeklyReportService reportService = new WeeklyReportService(
                    questCompletionRepository, questRepository,
                    xpHistoryRepository, userStatsRepository, streakRepository);

            when(questCompletionRepository.findByUserIdAndCompletedAtBetween(eq(userId), any(), any()))
                    .thenReturn(List.of());
            when(questRepository.findByCreatedBy_Id(userId)).thenReturn(List.of());
            when(questRepository.findByRecurringTrue()).thenReturn(List.of());
            when(xpHistoryRepository.sumXpAmountByUserIdAndCreatedAtBetween(eq(userId), any(), any()))
                    .thenReturn(0L);
            when(userStatsRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(streakRepository.findByUserId(userId)).thenReturn(Optional.empty());

            WeeklyReportResponse report = reportService.generateWeeklyReport(userId);

            assertThat(report).isNotNull();
            assertThat(report.getQuestsCompleted()).isGreaterThanOrEqualTo(0);
            assertThat(report.getQuestsMissed()).isGreaterThanOrEqualTo(0);
            assertThat(report.getXpEarned()).isGreaterThanOrEqualTo(0);
            assertThat(report.getStrongestStat()).isNotNull();
            assertThat(report.getWeakestStat()).isNotNull();
            assertThat(report.getRecommendations()).isNotNull();
            assertThat(report.getLifeScore()).isNotNull();
        }
    }

    // ========================================================================
    // Unit test: Life Score calculation for various stat combinations
    // ========================================================================

    @Nested
    @DisplayName("Life Score calculation")
    class LifeScoreCalculation {

        @Test
        @DisplayName("Life Score is 0 when all stats are 0 and no streak")
        void lifeScore_allZeros_returnsZero() {
            UserStats stats = UserStats.builder()
                    .userId(userId).discipline(0).focus(0).vitality(0)
                    .wisdom(0).strength(0).charisma(0).lifeScore(BigDecimal.ZERO).build();

            when(userStatsRepository.findByUserId(userId)).thenReturn(Optional.of(stats));
            when(userStatsRepository.save(any(UserStats.class))).thenAnswer(inv -> inv.getArgument(0));
            when(streakRepository.findByUserId(userId)).thenReturn(Optional.empty());

            BigDecimal score = lifeScoreService.calculateLifeScore(userId);

            assertThat(score).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Life Score is 100 when all stats are max and full streak")
        void lifeScore_allMax_returns100() {
            UserStats stats = UserStats.builder()
                    .userId(userId).discipline(1000).focus(1000).vitality(1000)
                    .wisdom(1000).strength(1000).charisma(1000).lifeScore(BigDecimal.ZERO).build();

            Streak streak = Streak.builder().userId(userId).currentStreak(30).build();

            when(userStatsRepository.findByUserId(userId)).thenReturn(Optional.of(stats));
            when(userStatsRepository.save(any(UserStats.class))).thenAnswer(inv -> inv.getArgument(0));
            when(streakRepository.findByUserId(userId)).thenReturn(Optional.of(streak));

            BigDecimal score = lifeScoreService.calculateLifeScore(userId);

            assertThat(score).isEqualByComparingTo(new BigDecimal("100.00"));
        }

        @Test
        @DisplayName("Life Score increases with higher stats")
        void lifeScore_higherStats_higherScore() {
            UserStats lowStats = UserStats.builder()
                    .userId(userId).discipline(100).focus(100).vitality(100)
                    .wisdom(100).strength(100).charisma(100).lifeScore(BigDecimal.ZERO).build();

            UserStats highStats = UserStats.builder()
                    .userId(userId).discipline(500).focus(500).vitality(500)
                    .wisdom(500).strength(500).charisma(500).lifeScore(BigDecimal.ZERO).build();

            when(streakRepository.findByUserId(userId)).thenReturn(Optional.empty());

            when(userStatsRepository.findByUserId(userId)).thenReturn(Optional.of(lowStats));
            when(userStatsRepository.save(any(UserStats.class))).thenAnswer(inv -> inv.getArgument(0));
            BigDecimal lowScore = lifeScoreService.calculateLifeScore(userId);

            when(userStatsRepository.findByUserId(userId)).thenReturn(Optional.of(highStats));
            BigDecimal highScore = lifeScoreService.calculateLifeScore(userId);

            assertThat(highScore.compareTo(lowScore)).isGreaterThan(0);
        }

        @Test
        @DisplayName("Life Score accounts for streak-based consistency")
        void lifeScore_streakIncreasesScore() {
            UserStats stats = UserStats.builder()
                    .userId(userId).discipline(200).focus(200).vitality(200)
                    .wisdom(200).strength(200).charisma(200).lifeScore(BigDecimal.ZERO).build();

            when(userStatsRepository.findByUserId(userId)).thenReturn(Optional.of(stats));
            when(userStatsRepository.save(any(UserStats.class))).thenAnswer(inv -> inv.getArgument(0));

            // No streak
            when(streakRepository.findByUserId(userId)).thenReturn(Optional.empty());
            BigDecimal scoreNoStreak = lifeScoreService.calculateLifeScore(userId);

            // 30-day streak
            Streak streak = Streak.builder().userId(userId).currentStreak(30).build();
            when(streakRepository.findByUserId(userId)).thenReturn(Optional.of(streak));
            BigDecimal scoreWithStreak = lifeScoreService.calculateLifeScore(userId);

            assertThat(scoreWithStreak.compareTo(scoreNoStreak)).isGreaterThan(0);
        }
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    private QuestCompletion createCompletion(LocalDateTime completedAt) {
        return QuestCompletion.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .questId(UUID.randomUUID())
                .completedAt(completedAt)
                .build();
    }
}
