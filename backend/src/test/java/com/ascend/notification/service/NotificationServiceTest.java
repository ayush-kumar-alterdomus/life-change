package com.ascend.notification.service;

import com.ascend.notification.dto.NotificationType;
import com.ascend.notification.entity.NotificationLog;
import com.ascend.notification.repository.NotificationLogRepository;
import com.ascend.streak.repository.StreakRepository;
import com.ascend.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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
    @Mock
    private UserRepository userRepository;
    @Mock
    private StreakRepository streakRepository;
    @Mock
    private FcmService fcmService;

    private NotificationService service;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        service = new NotificationService(repository, userRepository, streakRepository, fcmService, new ObjectMapper());
    }

    @Nested
    @DisplayName("sendNotification()")
    class SendNotification {

        @Test
        @DisplayName("should save and return notification")
        void shouldSaveAndReturn() {
            when(repository.countByUserIdAndSentAtBetween(eq(userId), any(), any())).thenReturn(0L);
            when(streakRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(repository.save(any(NotificationLog.class))).thenAnswer(inv -> {
                NotificationLog n = inv.getArgument(0);
                n.setId(UUID.randomUUID());
                return n;
            });

            NotificationLog result = service.sendNotification(userId, NotificationType.LEVEL_UP, "Level 5!", "Congrats");

            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getType()).isEqualTo("LEVEL_UP");
        }

        @Test
        @DisplayName("should return null when daily cap reached")
        void shouldReturnNullWhenCapped() {
            when(repository.countByUserIdAndSentAtBetween(eq(userId), any(), any()))
                    .thenReturn((long) NotificationService.DAILY_CAP);

            NotificationLog result = service.sendNotification(userId, NotificationType.QUEST_REMINDER, "Title", "msg");

            assertThat(result).isNull();
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("should allow sending when below daily cap")
        void shouldAllowBelowCap() {
            when(repository.countByUserIdAndSentAtBetween(eq(userId), any(), any()))
                    .thenReturn((long) NotificationService.DAILY_CAP - 1);
            when(streakRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            NotificationLog result = service.sendNotification(userId, NotificationType.ACHIEVEMENT, "Done", null);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("should throw when userId is null")
        void shouldThrowWhenUserIdNull() {
            assertThatThrownBy(() -> service.sendNotification(null, NotificationType.LEVEL_UP, "T", "m"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw when type is null")
        void shouldThrowWhenTypeNull() {
            assertThatThrownBy(() -> service.sendNotification(userId, null, "T", "m"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw when title is null")
        void shouldThrowWhenTitleNull() {
            assertThatThrownBy(() -> service.sendNotification(userId, NotificationType.LEVEL_UP, null, "m"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("markAsRead()")
    class MarkAsRead {

        @Test
        @DisplayName("should mark unread notification as read")
        void shouldMarkUnread() {
            UUID notifId = UUID.randomUUID();
            var notification = NotificationLog.builder()
                    .id(notifId).userId(userId).type("T").title("X").readAt(null).build();
            when(repository.findById(notifId)).thenReturn(Optional.of(notification));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            boolean result = service.markAsRead(userId, notifId);

            assertThat(result).isTrue();
            assertThat(notification.getReadAt()).isNotNull();
        }

        @Test
        @DisplayName("should return false for already-read notification")
        void shouldReturnFalseForAlreadyRead() {
            UUID notifId = UUID.randomUUID();
            var notification = NotificationLog.builder()
                    .id(notifId).userId(userId).type("T").title("X")
                    .readAt(LocalDateTime.now().minusHours(1)).build();
            when(repository.findById(notifId)).thenReturn(Optional.of(notification));

            boolean result = service.markAsRead(userId, notifId);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should return false when not found")
        void shouldReturnFalseWhenNotFound() {
            UUID notifId = UUID.randomUUID();
            when(repository.findById(notifId)).thenReturn(Optional.empty());

            boolean result = service.markAsRead(userId, notifId);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("markAllAsRead()")
    class MarkAllAsRead {

        @Test
        @DisplayName("should delegate to repository")
        void shouldDelegateToRepository() {
            when(repository.markAllAsRead(eq(userId), any(LocalDateTime.class))).thenReturn(3);

            int result = service.markAllAsRead(userId);

            assertThat(result).isEqualTo(3);
            verify(repository).markAllAsRead(eq(userId), any(LocalDateTime.class));
        }
    }

    @Nested
    @DisplayName("countUnread()")
    class CountUnread {

        @Test
        @DisplayName("should return unread count")
        void shouldReturnCount() {
            when(repository.countByUserIdAndReadAtIsNull(userId)).thenReturn(5L);

            long result = service.countUnread(userId);

            assertThat(result).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("should delete owned notification")
        void shouldDeleteOwned() {
            UUID notifId = UUID.randomUUID();
            var notification = NotificationLog.builder()
                    .id(notifId).userId(userId).type("T").title("X").build();
            when(repository.findById(notifId)).thenReturn(Optional.of(notification));

            boolean result = service.delete(userId, notifId);

            assertThat(result).isTrue();
            verify(repository).delete(notification);
        }

        @Test
        @DisplayName("should return false for non-owned notification")
        void shouldReturnFalseForNonOwned() {
            UUID notifId = UUID.randomUUID();
            var notification = NotificationLog.builder()
                    .id(notifId).userId(UUID.randomUUID()).type("T").title("X").build();
            when(repository.findById(notifId)).thenReturn(Optional.of(notification));

            boolean result = service.delete(userId, notifId);

            assertThat(result).isFalse();
            verify(repository, never()).delete(any());
        }

        @Test
        @DisplayName("should return false when not found")
        void shouldReturnFalseWhenNotFound() {
            UUID notifId = UUID.randomUUID();
            when(repository.findById(notifId)).thenReturn(Optional.empty());

            boolean result = service.delete(userId, notifId);

            assertThat(result).isFalse();
        }
    }
}
