package com.ascend.premium.controller;

import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.service.AuthService;
import com.ascend.premium.dto.PremiumFeature;
import com.ascend.premium.service.FeatureGateService;
import com.ascend.premium.service.SubscriptionService;
import com.ascend.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PremiumController")
class PremiumControllerTest {

    @Mock private SubscriptionService subscriptionService;
    @Mock private FeatureGateService featureGateService;
    @Mock private AuthService authService;

    private PremiumController controller;
    private UUID userId;
    private User user;
    private FirebasePrincipal principal;

    @BeforeEach
    void setUp() {
        controller = new PremiumController(subscriptionService, featureGateService, authService);
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        principal = new FirebasePrincipal("firebase-uid", "test@test.com", "password", java.util.Map.of());
        when(authService.getCurrentUser("firebase-uid")).thenReturn(user);
    }

    @Test
    @DisplayName("POST /downgrade should delegate to subscriptionService")
    void downgrade_shouldDelegate() {
        var response = controller.downgrade(principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).contains("free tier");
        verify(subscriptionService).downgradeToFree(userId);
    }

    @Test
    @DisplayName("GET /feature-access/{feature} should return accessible=true for premium user")
    void featureAccess_shouldReturnTrue() {
        when(featureGateService.hasAccess(userId, PremiumFeature.AI_COACH)).thenReturn(true);

        var response = controller.checkFeatureAccess(principal, "AI_COACH");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).containsEntry("accessible", true);
    }

    @Test
    @DisplayName("GET /feature-access/{feature} should return 400 for invalid feature")
    void featureAccess_shouldReturn400ForInvalid() {
        var response = controller.checkFeatureAccess(principal, "INVALID_FEATURE");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
