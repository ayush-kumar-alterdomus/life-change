package com.ascend.social.controller;

import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.service.AuthService;
import com.ascend.social.dto.FriendResponse;
import com.ascend.social.service.FriendService;
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
@DisplayName("SocialController")
class SocialControllerTest {

    @Mock private FriendService friendService;
    @Mock private AuthService authService;

    private SocialController controller;
    private UUID userId;
    private User user;
    private FirebasePrincipal principal;

    @BeforeEach
    void setUp() {
        controller = new SocialController(friendService, authService);
        userId = UUID.randomUUID();
        user = User.builder().id(userId).username("alice").build();
        principal = new FirebasePrincipal("firebase-uid", "test@test.com", "password", Map.of());
        when(authService.getCurrentUser("firebase-uid")).thenReturn(user);
    }

    @Nested
    @DisplayName("GET /friends/pending")
    class GetPendingRequests {

        @Test
        @DisplayName("should return pending friend requests")
        void shouldReturnPending() {
            UUID friendId = UUID.randomUUID();
            var pending = List.of(new FriendResponse(friendId, "bob", null, 3, 5, "PENDING"));
            when(friendService.getPendingRequests(userId)).thenReturn(pending);

            var response = controller.getPendingRequests(principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getData()).hasSize(1);
            assertThat(response.getBody().getData().get(0).status()).isEqualTo("PENDING");
        }
    }

    @Nested
    @DisplayName("DELETE /friends/{friendId}")
    class RemoveFriend {

        @Test
        @DisplayName("should remove friend and return 200")
        void shouldRemoveFriend() {
            UUID friendId = UUID.randomUUID();

            var response = controller.removeFriend(principal, friendId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getMessage()).contains("removed");
            verify(friendService).removeFriend(userId, friendId);
        }
    }

    @Nested
    @DisplayName("POST /friends/block")
    class BlockUser {

        @Test
        @DisplayName("should block user and return 200")
        void shouldBlockUser() {
            UUID blockedId = UUID.randomUUID();

            var response = controller.blockUser(principal, Map.of("userId", blockedId.toString()));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getMessage()).contains("blocked");
            verify(friendService).blockUser(userId, blockedId);
        }
    }

    @Nested
    @DisplayName("GET /friends")
    class GetFriends {

        @Test
        @DisplayName("should return friends list")
        void shouldReturnFriends() {
            var friends = List.of(new FriendResponse(UUID.randomUUID(), "bob", null, 5, 10, "ACCEPTED"));
            when(friendService.getFriends(userId)).thenReturn(friends);

            var response = controller.getFriends(principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getData()).hasSize(1);
        }
    }
}
