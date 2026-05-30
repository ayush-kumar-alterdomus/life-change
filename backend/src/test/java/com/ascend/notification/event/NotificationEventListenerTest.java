package com.ascend.notification.event;

import com.ascend.notification.entity.NotificationLog;
import com.ascend.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        when(notificationService.send(any(), any(), any(), any()))
                .thenReturn(NotificationLog.builder().build());
    }

    @Nested
    @DisplayName("onQuestCompleted")
    class QuestCompleted {

        @Test
        @DisplayName("should send notification with QUEST_COMPLETED type")
        void shouldSendWithCorrectType() {
            var event = new NotificationEventListener.QuestCompletedEvent(userId, "Run 5km", 50);

            listener.onQuestCompleted(event);

            verify(notificationService).send(eq(userId), eq("QUEST_COMPLETED"), any(), any());
        }

        @Test
        @DisplayName("should include quest title and XP in message")
        void shouldIncludeDetailsInMessage() {
            var event = new NotificationEventListener.QuestCompletedEvent(userId, "Read 30 pages", 75);

            listener.onQuestCompleted(event);

            ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
            verify(notificationService).send(eq(userId), any(), any(), messageCaptor.capture());

            assertThat(messageCaptor.getValue()).contains("75 XP");
            assertThat(messageCaptor.getValue()).contains("Read 30 pages");
        }
    }

    @Nested
    @DisplayName("onStreakMilestone")
    class StreakMilestone {

        @Test
        @DisplayName("should send notification with STREAK_MILESTONE type")
        void shouldSendWithCorrectType() {
            var event = new NotificationEventListener.StreakMilestoneEvent(userId, 7);

            listener.onStreakMilestone(event);

            verify(notificationService).send(eq(userId), eq("STREAK_MILESTONE"), any(), any());
        }

        @Test
        @DisplayName("should include streak days in title")
        void shouldIncludeStreakDaysInTitle() {
            var event = new NotificationEventListener.StreakMilestoneEvent(userId, 30);

            listener.onStreakMilestone(event);

            ArgumentCaptor<String> titleCaptor = ArgumentCaptor.forClass(String.class);
            verify(notificationService).send(eq(userId), any(), titleCaptor.capture(), any());

            assertThat(titleCaptor.getValue()).contains("30");
        }

        @Test
        @DisplayName("should have specific message for 7-day milestone")
        void shouldHaveMessageFor7Days() {
            var event = new NotificationEventListener.StreakMilestoneEvent(userId, 7);

            listener.onStreakMilestone(event);

            ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
            verify(notificationService).send(eq(userId), any(), any(), messageCaptor.capture());

            assertThat(messageCaptor.getValue()).contains("One week");
        }

        @Test
        @DisplayName("should have specific message for 100-day milestone")
        void shouldHaveMessageFor100Days() {
            var event = new NotificationEventListener.StreakMilestoneEvent(userId, 100);

            listener.onStreakMilestone(event);

            ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
            verify(notificationService).send(eq(userId), any(), any(), messageCaptor.capture());

            assertThat(messageCaptor.getValue()).contains("epic");
        }

        @Test
        @DisplayName("should have fallback message for non-standard milestones")
        void shouldHaveFallbackMessage() {
            var event = new NotificationEventListener.StreakMilestoneEvent(userId, 50);

            listener.onStreakMilestone(event);

            ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
            verify(notificationService).send(eq(userId), any(), any(), messageCaptor.capture());

            assertThat(messageCaptor.getValue()).contains("50 days");
        }
    }

    @Nested
    @DisplayName("onLevelUp")
    class LevelUp {

        @Test
        @DisplayName("should send notification with LEVEL_UP type")
        void shouldSendWithCorrectType() {
            var event = new NotificationEventListener.LevelUpEvent(userId, 10);

            listener.onLevelUp(event);

            verify(notificationService).send(eq(userId), eq("LEVEL_UP"), any(), any());
        }

        @Test
        @DisplayName("should include new level in title")
        void shouldIncludeLevelInTitle() {
            var event = new NotificationEventListener.LevelUpEvent(userId, 25);

            listener.onLevelUp(event);

            ArgumentCaptor<String> titleCaptor = ArgumentCaptor.forClass(String.class);
            verify(notificationService).send(eq(userId), any(), titleCaptor.capture(), any());

            assertThat(titleCaptor.getValue()).contains("25");
        }

        @Test
        @DisplayName("should include level in message body")
        void shouldIncludeLevelInMessage() {
            var event = new NotificationEventListener.LevelUpEvent(userId, 5);

            listener.onLevelUp(event);

            ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
            verify(notificationService).send(eq(userId), any(), any(), messageCaptor.capture());

            assertThat(messageCaptor.getValue()).contains("level 5");
        }
    }
}
