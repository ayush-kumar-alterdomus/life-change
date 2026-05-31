package com.ascend.notification.controller;

import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.service.AuthService;
import com.ascend.notification.service.NotificationService;
import com.ascend.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationController")
class NotificationControllerTest {

    @Mock private NotificationService notificationService;
    @Mock private AuthService authService;

    private NotificationController controller;
    private UUID userId;
    private User user;
    private FirebasePrincipal principal;

    @BeforeEach
    void setUp() {
        controller = new NotificationController(notificationService, authService);
        userId = UUID.randomUUID();
        user = User.builder().id(userId).username("alice").build();
        principal = new FirebasePrincipal("uid-123", "alice@test.com", "password", Map.of());
        when(authService.getCurrentUser("uid-123")).thenReturn(user);
    }

    @Nested
    @DisplayName("PATCH /notifications/read")
    class MarkAsRead {

        @Test
        @DisplayName("should mark all as read when no IDs provided")
        void shouldMarkAllAsRead() {
            Map<String, List<String>> request = Map.of("notificationIds", List.of());

            var response = controller.markAsRead(principal, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(notificationService).markAllAsRead(userId);
        }

        @Test
        @DisplayName("should mark specific IDs as read")
        void shouldMarkSpecificAsRead() {
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            Map<String, List<String>> request = Map.of("notificationIds", List.of(id1.toString(), id2.toString()));

            var response = controller.markAsRead(principal, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(notificationService).markAsRead(userId, id1);
            verify(notificationService).markAsRead(userId, id2);
        }
    }

    @Nested
    @DisplayName("DELETE /notifications/{id}")
    class DeleteNotification {

        @Test
        @DisplayName("should return 200 when deleted")
        void shouldReturn200WhenDeleted() {
            UUID notifId = UUID.randomUUID();

            var response = controller.deleteNotification(principal, notifId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(notificationService).delete(userId, notifId);
        }
    }
}
