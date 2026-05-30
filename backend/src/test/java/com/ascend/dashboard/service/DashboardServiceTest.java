package com.ascend.dashboard.service;

import com.ascend.arc.dto.ArcPhase;
import com.ascend.arc.dto.ArcProgressResponse;
import com.ascend.arc.entity.ArcStatus;
import com.ascend.arc.service.ArcProgressService;
import com.ascend.dashboard.dto.DashboardResponse;
import com.ascend.notification.service.NotificationService;
import com.ascend.quest.dto.DailyQuestsResponse;
import com.ascend.quest.dto.QuestResponse;
import com.ascend.quest.service.QuestService;
import com.ascend.streak.dto.StreakResponse;
import com.ascend.streak.entity.Streak;
import com.ascend.streak.repository.StreakRepository;
import com.ascend.streak.service.StreakService;
import com.ascend.user.entity.User;
import com.ascend.xp.service.XpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService")
class DashboardServiceTest {

    @Mock private XpService xpService;
    @Mock private StreakService streakService;
    @Mock private QuestService questService;
    @Mock private ArcProgressService arcProgressService;
    @Mock private NotificationService notificationService;
    @Mock private StreakRepository streakRepository;

    private DashboardService service;
    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        service = new DashboardService(xpService, streakService, questService,
                arcProgressService, notificationService, streakRepository);
        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .username("alice")
                .level(10)
                .xp(2500L)
                .avatarUrl("https://avatar.url")
                .premium(false)
                .build();
    }

    @Nested
    @DisplayName("getDashboard() — happy path")
    class HappyPath {

        @Test
        @DisplayName("should return all sections populated")
        void shouldReturnAllSections() {
            // XP
            when(xpService.getDailyXpEarned(userId)).thenReturn(150L);
            when(xpService.getDailyCap(10)).thenReturn(1200);
            Streak streak = Streak.builder().userId(userId).currentStreak(7).build();
            when(streakRepository.findByUserId(userId)).thenReturn(Optional.of(streak));

            // Streak
            when(streakService.getStreak(userId)).thenReturn(StreakResponse.builder()
                    .currentStreak(7).longestStreak(14)
                    .shieldAvailable(true).comebackModeActive(false)
                    .comboMultiplier(BigDecimal.valueOf(1.07))
                    .build());

            // Quests
            var questResponse = QuestResponse.builder().id(UUID.randomUUID()).title("Run").build();
            when(questService.getDailyQuests(userId)).thenReturn(DailyQuestsResponse.builder()
                    .date(LocalDate.now()).quests(List.of(questResponse))
                    .totalQuests(5).completedQuests(2).build());

            // Arc
            when(arcProgressService.getActiveArc(userId)).thenReturn(ArcProgressResponse.builder()
                    .arcId(UUID.randomUUID()).arcName("Fitness")
                    .progressPercent(45).currentPhase(ArcPhase.INTERMEDIATE)
                    .status(ArcStatus.ACTIVE).build());

            // Notifications
            when(notificationService.countUnread(userId)).thenReturn(3L);

            DashboardResponse result = service.getDashboard(user);

            assertThat(result.user()).isNotNull();
            assertThat(result.user().displayName()).isEqualTo("alice");
            assertThat(result.user().level()).isEqualTo(10);

            assertThat(result.xp()).isNotNull();
            assertThat(result.xp().dailyXpEarned()).isEqualTo(150L);
            assertThat(result.xp().dailyCap()).isEqualTo(1200);

            assertThat(result.streak()).isNotNull();
            assertThat(result.streak().currentStreak()).isEqualTo(7);
            assertThat(result.streak().shieldAvailable()).isTrue();

            assertThat(result.dailyStats()).isNotNull();
            assertThat(result.dailyStats().questsCompleted()).isEqualTo(2);
            assertThat(result.dailyStats().questsTotal()).isEqualTo(5);
            assertThat(result.dailyStats().completionPercentage()).isEqualTo(40);

            assertThat(result.quests()).hasSize(1);

            assertThat(result.activeArc()).isNotNull();
            assertThat(result.activeArc().name()).isEqualTo("Fitness");
            assertThat(result.activeArc().progressPercentage()).isEqualTo(45);

            assertThat(result.notifications().unreadCount()).isEqualTo(3L);
        }
    }

    @Nested
    @DisplayName("getDashboard() — graceful degradation")
    class GracefulDegradation {

        @Test
        @DisplayName("should return null xp section when XpService throws")
        void shouldReturnNullXpOnFailure() {
            when(xpService.getDailyXpEarned(userId)).thenThrow(new RuntimeException("DB down"));
            when(streakService.getStreak(userId)).thenReturn(StreakResponse.builder()
                    .currentStreak(0).longestStreak(0).shieldAvailable(false)
                    .comebackModeActive(false).comboMultiplier(BigDecimal.ONE).build());
            when(questService.getDailyQuests(userId)).thenReturn(DailyQuestsResponse.builder()
                    .date(LocalDate.now()).quests(List.of())
                    .totalQuests(0).completedQuests(0).build());
            when(arcProgressService.getActiveArc(userId)).thenReturn(null);
            when(notificationService.countUnread(userId)).thenReturn(0L);

            DashboardResponse result = service.getDashboard(user);

            assertThat(result.user()).isNotNull();
            assertThat(result.xp()).isNull();
            assertThat(result.streak()).isNotNull();
        }

        @Test
        @DisplayName("should return null activeArc when user has no active arc")
        void shouldReturnNullArcWhenNone() {
            when(xpService.getDailyXpEarned(userId)).thenReturn(0L);
            when(xpService.getDailyCap(10)).thenReturn(1200);
            when(streakRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(streakService.getStreak(userId)).thenReturn(StreakResponse.builder()
                    .currentStreak(0).longestStreak(0).shieldAvailable(false)
                    .comebackModeActive(false).comboMultiplier(BigDecimal.ONE).build());
            when(questService.getDailyQuests(userId)).thenReturn(DailyQuestsResponse.builder()
                    .date(LocalDate.now()).quests(List.of())
                    .totalQuests(3).completedQuests(0).build());
            when(arcProgressService.getActiveArc(userId)).thenReturn(null);
            when(notificationService.countUnread(userId)).thenReturn(0L);

            DashboardResponse result = service.getDashboard(user);

            assertThat(result.activeArc()).isNull();
            assertThat(result.user()).isNotNull();
        }

        @Test
        @DisplayName("should return 0 unread when notification service throws")
        void shouldReturn0UnreadOnFailure() {
            when(xpService.getDailyXpEarned(userId)).thenReturn(0L);
            when(xpService.getDailyCap(10)).thenReturn(1200);
            when(streakRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(streakService.getStreak(userId)).thenReturn(StreakResponse.builder()
                    .currentStreak(0).longestStreak(0).shieldAvailable(false)
                    .comebackModeActive(false).comboMultiplier(BigDecimal.ONE).build());
            when(questService.getDailyQuests(userId)).thenReturn(DailyQuestsResponse.builder()
                    .date(LocalDate.now()).quests(List.of())
                    .totalQuests(0).completedQuests(0).build());
            when(arcProgressService.getActiveArc(userId)).thenReturn(null);
            when(notificationService.countUnread(userId)).thenThrow(new RuntimeException("Redis down"));

            DashboardResponse result = service.getDashboard(user);

            assertThat(result.notifications().unreadCount()).isEqualTo(0L);
        }
    }
}
