package com.ascend.notification.service;

import com.ascend.notification.entity.NotificationLog;
import com.ascend.notification.repository.NotificationLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService")
class NotificationServiceTest {

    @Mock
    private NotificationLogRepository repository;

    @InjectMocks
    private NotificationService service;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("send()")
    class Send {

        @Test
        @DisplayName("should save notification and return it")
        void shouldSaveAndReturn() {
            when(repository.countByUserIdAndSentAtBetween(eq(userId), any(), any())).thenReturn(0L);
            when(repository.save(any(NotificationLog.class))).thenAnswer(inv -> {
                NotificationLog n = inv.getArgument(0);
                n.setId(UUID.randomUUID());
                return n;
            });

            NotificationLog result = service.send(userId, "QUEST_COMPLETED", "Quest Done!", "You earned 50 XP");

            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getType()).isEqualTo("QUEST_COMPLETED");
            assertThat(result.getTitle()).isEqualTo("Quest Done!");
            assertThat(result.getMessage()).isEqualTo("You earned 50 XP");
        }

        @Test
        @DisplayName("should persist notification to repository")
        void shouldPersist() {
            when(repository.countByUserIdAndSentAtBetween(eq(userId), any(), any())).thenReturn(0L);
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.send(userId, "LEVEL_UP", "Level 5!", null);

            ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
            verify(repository).save(captor.capture());

            NotificationLog saved = captor.getValue();
            assertThat(saved.getUserId()).isEqualTo(userId);
            assertThat(saved.getType()).isEqualTo("LEVEL_UP");
            assertThat(saved.getTitle()).isEqualTo("Level 5!");
            assertThat(saved.getMessage()).isNull();
        }

        @Test
        @DisplayName("should return null when rate-limited")
        void shouldReturnNullWhenRateLimited() {
            when(repository.countByUserIdAndSentAtBetween(eq(userId), any(), any()))
                    .thenReturn((long) NotificationService.MAX_NOTIFICATIONS_PER_HOUR);

            NotificationLog result = service.send(userId, "SPAM", "Too many", "msg");

            assertThat(result).isNull();
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("should allow sending when just below rate limit")
        void shouldAllowJustBelowLimit() {
            when(repository.countByUserIdAndSentAtBetween(eq(userId), any(), any()))
                    .thenReturn((long) NotificationService.MAX_NOTIFICATIONS_PER_HOUR - 1);
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            NotificationLog result = service.send(userId, "OK", "Still allowed", null);

            assertThat(result).isNotNull();
            verify(repository).save(any());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when userId is null")
        void shouldThrowWhenUserIdNull() {
            assertThatThrownBy(() -> service.send(null, "TYPE", "Title", "msg"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("must not be null");
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when type is null")
        void shouldThrowWhenTypeNull() {
            assertThatThrownBy(() -> service.send(userId, null, "Title", "msg"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when title is null")
        void shouldThrowWhenTitleNull() {
            assertThatThrownBy(() -> service.send(userId, "TYPE", null, "msg"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should allow null message (optional)")
        void shouldAllowNullMessage() {
            when(repository.countByUserIdAndSentAtBetween(eq(userId), any(), any())).thenReturn(0L);
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            NotificationLog result = service.send(userId, "TYPE", "Title", null);

            assertThat(result).isNotNull();
            assertThat(result.getMessage()).isNull();
        }
    }

    @Nested
    @DisplayName("getNotificationsForUser()")
    class GetNotifications {

        @Test
        @DisplayName("should return notifications ordered by most recent")
        void shouldReturnOrdered() {
            var n1 = NotificationLog.builder().userId(userId).type("A").title("First").build();
            var n2 = NotificationLog.builder().userId(userId).type("B").title("Second").build();
            when(repository.findByUserIdOrderBySentAtDesc(userId)).thenReturn(List.of(n2, n1));

            List<NotificationLog> result = service.getNotificationsForUser(userId);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getTitle()).isEqualTo("Second");
        }

        @Test
        @DisplayName("should return empty list when user has no notifications")
        void shouldReturnEmptyList() {
            when(repository.findByUserIdOrderBySentAtDesc(userId)).thenReturn(Collections.emptyList());

            List<NotificationLog> result = service.getNotificationsForUser(userId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("markAsRead()")
    class MarkAsRead {

        @Test
        @DisplayName("should set readAt and return true for unread notification")
        void shouldMarkUnread() {
            UUID notifId = UUID.randomUUID();
            var notification = NotificationLog.builder()
                    .id(notifId).userId(userId).type("T").title("X").readAt(null).build();

            when(repository.findById(notifId)).thenReturn(Optional.of(notification));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            boolean result = service.markAsRead(notifId);

            assertThat(result).isTrue();
            assertThat(notification.getReadAt()).isNotNull();
            verify(repository).save(notification);
        }

        @Test
        @DisplayName("should return false for already-read notification")
        void shouldReturnFalseForAlreadyRead() {
            UUID notifId = UUID.randomUUID();
            var notification = NotificationLog.builder()
                    .id(notifId).userId(userId).type("T").title("X")
                    .readAt(LocalDateTime.now().minusHours(1)).build();

            when(repository.findById(notifId)).thenReturn(Optional.of(notification));

            boolean result = service.markAsRead(notifId);

            assertThat(result).isFalse();
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("should return false when notification not found")
        void shouldReturnFalseWhenNotFound() {
            UUID notifId = UUID.randomUUID();
            when(repository.findById(notifId)).thenReturn(Optional.empty());

            boolean result = service.markAsRead(notifId);

            assertThat(result).isFalse();
        }
    }
}
