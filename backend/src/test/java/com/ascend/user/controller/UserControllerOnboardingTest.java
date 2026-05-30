package com.ascend.user.controller;

import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.service.AuthService;
import com.ascend.user.dto.OnboardingRequest;
import com.ascend.user.dto.OnboardingResponse;
import com.ascend.user.dto.OnboardingStatusResponse;
import com.ascend.user.entity.User;
import com.ascend.user.service.OnboardingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController — Onboarding")
class UserControllerOnboardingTest {

    @Mock private AuthService authService;
    @Mock private OnboardingService onboardingService;

    private UserController controller;
    private User user;
    private FirebasePrincipal principal;

    @BeforeEach
    void setUp() {
        controller = new UserController(authService, onboardingService);
        user = User.builder().id(UUID.randomUUID()).username("alice").level(1).build();
        principal = new FirebasePrincipal("uid-123", "alice@test.com", "password", Map.of());
        when(authService.getCurrentUser("uid-123")).thenReturn(user);
    }

    @Test
    @DisplayName("PUT /onboarding should return 200 with OnboardingResponse")
    void completeOnboarding_shouldReturn200() {
        var response = new OnboardingResponse(1, true, UUID.randomUUID().toString(), "Warrior Path");
        when(onboardingService.completeOnboarding(eq(user.getId()), any())).thenReturn(response);

        var request = new OnboardingRequest();
        request.setSelectedGoals(List.of("fitness"));
        request.setDifficulty("balanced");
        request.setPersonalityType("disciplined");
        request.setSelectedArc("warrior");
        request.setSelectedAvatar("avatar");

        var result = controller.completeOnboarding(principal, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getData().arcStarted()).isTrue();
        assertThat(result.getBody().getMessage()).contains("Onboarding completed");
    }

    @Test
    @DisplayName("GET /me/onboarding-status should return status")
    void getOnboardingStatus_shouldReturnStatus() {
        var status = new OnboardingStatusResponse(true, List.of("fitness"), "disciplined", "balanced");
        when(onboardingService.getOnboardingStatus(user.getId())).thenReturn(status);

        var result = controller.getOnboardingStatus(principal);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getData().complete()).isTrue();
        assertThat(result.getBody().getData().selectedGoals()).containsExactly("fitness");
    }
}
