package com.ascend.premium.controller;

import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.service.AuthService;
import com.ascend.premium.service.SubscriptionService;
import com.ascend.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PremiumController")
class PremiumControllerTest {

    @Mock private SubscriptionService subscriptionService;
    @Mock private AuthService authService;

    private PremiumController controller;
    private UUID userId;
    private User user;
    private FirebasePrincipal principal;

    @BeforeEach
    void setUp() {
        controller = new PremiumController(subscriptionService, authService);
        userId = UUID.randomUUID();
        user = User.builder().id(userId).username("alice").build();
        principal = new FirebasePrincipal("firebase-uid", "test@test.com", "password", Map.of());
        when(authService.getCurrentUser("firebase-uid")).thenReturn(user);
    }

    @Test
    @DisplayName("POST /trial should delegate to subscriptionService")
    void startTrial_shouldDelegate() {
        var response = controller.startTrial(principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
        verify(subscriptionService).startTrial(userId);
    }

    @Test
    @DisplayName("POST /cancel should delegate to subscriptionService")
    void cancel_shouldDelegate() {
        var response = controller.cancel(principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
        verify(subscriptionService).cancelSubscription(userId);
    }

    @Test
    @DisplayName("POST /upgrade should delegate with provider and planType")
    void upgrade_shouldDelegate() {
        var request = Map.of("provider", "STRIPE", "planType", "YEARLY");

        var response = controller.upgrade(principal, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(subscriptionService).activatePremium(userId, "STRIPE", "YEARLY");
    }
}
