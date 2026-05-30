package com.ascend.notification.controller;

import com.ascend.notification.service.NotificationService;
import com.ascend.notification.service.FcmService;
import com.ascend.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationController")
class NotificationControllerTest {

    @Mock private NotificationService notificationService;
    @Mock private FcmService fcmService;
    @Mock private UserRepository userRepository;

    private NotificationController controller;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        controller = new NotificationController(notificationService, fcmService, userRepository, new ObjectMapper());
    }

    @Nested
    @DisplayName("PATCH /notifications/read (batch)")
    class MarkAllAsRead {

        @Test
        @DisplayName("should return 200 and delegate to service")
        void shouldReturn200() {
            when(notificationService.markAllAsRead(userId)).thenReturn(5);

            var response = controller.markAllAsRead(userId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(notificationService).markAllAsRead(userId);
        }
    }

    @Nested
    @DisplayName("GET /notifications/unread-count")
    class UnreadCount {

        @Test
        @DisplayName("should return count")
        void shouldReturnCount() {
            when(notificationService.countUnread(userId)).thenReturn(7L);

            var response = controller.getUnreadCount(userId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsEntry("count", 7L);
        }
    }

    @Nested
    @DisplayName("DELETE /notifications/{id}")
    class DeleteNotification {

        @Test
        @DisplayName("should return 204 when deleted")
        void shouldReturn204WhenDeleted() {
            UUID notifId = UUID.randomUUID();
            when(notificationService.delete(userId, notifId)).thenReturn(true);

            var response = controller.deleteNotification(userId, notifId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }

        @Test
        @DisplayName("should return 404 when not found")
        void shouldReturn404WhenNotFound() {
            UUID notifId = UUID.randomUUID();
            when(notificationService.delete(userId, notifId)).thenReturn(false);

            var response = controller.deleteNotification(userId, notifId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }
}
