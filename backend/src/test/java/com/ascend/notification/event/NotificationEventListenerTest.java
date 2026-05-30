package com.ascend.notification.event;

import com.ascend.boss.event.BossDefeatedEvent;
import com.ascend.notification.dto.NotificationType;
import com.ascend.notification.entity.NotificationLog;
import com.ascend.notification.service.NotificationService;
import com.ascend.streak.dto.StreakMilestone;
import com.ascend.streak.event.StreakMilestoneEvent;
import com.ascend.xp.event.LevelUpEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationEventListener")
class NotificationEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationEventListener listener;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        when(notificationService.sendNotification(any(), any(), any(), any()))
                .thenReturn(NotificationLog.builder().build());
    }

    @Nested
    @DisplayName("onLevelUp")
    class OnLevelUp {

        @Test
        @DisplayName("should send LEVEL_UP notification")
        void shouldSendWithCorrectType() {
            var event = new LevelUpEvent(this, userId, 9, 10, List.of());

            listener.onLevelUp(event);

            verify(notificationService).sendNotification(eq(userId), eq(NotificationType.LEVEL_UP), any(), any());
        }

        @Test
        @DisplayName("should include new level in title")
        void shouldIncludeLevelInTitle() {
            var event = new LevelUpEvent(this, userId, 24, 25, List.of());

            listener.onLevelUp(event);

            ArgumentCaptor<String> titleCaptor = ArgumentCaptor.forClass(String.class);
            verify(notificationService).sendNotification(eq(userId), any(), titleCaptor.capture(), any());

            assertThat(titleCaptor.getValue()).contains("25");
        }
    }

    @Nested
    @DisplayName("onStreakMilestone")
    class OnStreakMilestone {

        @Test
        @DisplayName("should send REWARD_ALERT notification")
        void shouldSendWithCorrectType() {
            var event = new StreakMilestoneEvent(this, userId, StreakMilestone.WEEK, 7, 50);

            listener.onStreakMilestone(event);

            verify(notificationService).sendNotification(eq(userId), eq(NotificationType.REWARD_ALERT), any(), any());
        }

        @Test
        @DisplayName("should include streak days in title")
        void shouldIncludeStreakDaysInTitle() {
            var event = new StreakMilestoneEvent(this, userId, StreakMilestone.MONTH, 30, 250);

            listener.onStreakMilestone(event);

            ArgumentCaptor<String> titleCaptor = ArgumentCaptor.forClass(String.class);
            verify(notificationService).sendNotification(eq(userId), any(), titleCaptor.capture(), any());

            assertThat(titleCaptor.getValue()).contains("30");
        }
    }

    @Nested
    @DisplayName("onBossDefeated")
    class OnBossDefeated {

        @Test
        @DisplayName("should send ACHIEVEMENT notification")
        void shouldSendWithCorrectType() {
            var event = new BossDefeatedEvent(this, userId, UUID.randomUUID(), "Dragon", 500, "Dragonslayer");

            listener.onBossDefeated(event);

            verify(notificationService).sendNotification(eq(userId), eq(NotificationType.ACHIEVEMENT), any(), any());
        }

        @Test
        @DisplayName("should include boss name in message")
        void shouldIncludeBossNameInMessage() {
            var event = new BossDefeatedEvent(this, userId, UUID.randomUUID(), "Shadow King", 1000, "Shadow Conqueror");

            listener.onBossDefeated(event);

            ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
            verify(notificationService).sendNotification(eq(userId), any(), any(), messageCaptor.capture());

            assertThat(messageCaptor.getValue()).contains("Shadow King");
        }
    }
}
