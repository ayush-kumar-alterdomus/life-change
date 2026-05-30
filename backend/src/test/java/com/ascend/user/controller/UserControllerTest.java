package com.ascend.user.controller;

import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.service.AuthService;
import com.ascend.common.exception.BusinessException;
import com.ascend.user.dto.OnboardingRequest;
import com.ascend.user.dto.OnboardingResponse;
import com.ascend.user.entity.User;
import com.ascend.user.service.OnboardingService;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController")
class UserControllerTest {

    @Mock private AuthService authService;
    @Mock private OnboardingService onboardingService;

    private UserController controller;
    private User testUser;
    private FirebasePrincipal principal;
    private final String firebaseUid = "firebase-uid-456";

    @BeforeEach
    void setUp() {
        controller = new UserController(authService, onboardingService);
        testUser = User.builder()
                .id(UUID.randomUUID())
                .firebaseUid(firebaseUid)
                .username("newuser")
                .level(1)
                .xp(0L)
                .build();

        principal = new FirebasePrincipal(firebaseUid, "new@example.com", "password", Map.of());
        when(authService.getCurrentUser(firebaseUid)).thenReturn(testUser);
    }

    @Nested
    @DisplayName("completeOnboarding")
    class CompleteOnboarding {

        @Test
        @DisplayName("should return 200 on valid onboarding request")
        void shouldReturn200OnValidRequest() {
            var response = new OnboardingResponse(1, true, UUID.randomUUID().toString(), "Warrior");
            when(onboardingService.completeOnboarding(eq(testUser.getId()), any())).thenReturn(response);

            OnboardingRequest request = new OnboardingRequest();
            request.setSelectedGoals(List.of("fitness", "mindfulness"));
            request.setDifficulty("balanced");
            request.setPersonalityType("disciplined");
            request.setSelectedArc("monk");
            request.setSelectedAvatar("phoenix");

            var result = controller.completeOnboarding(principal, request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().isSuccess()).isTrue();
            assertThat(result.getBody().getMessage()).isEqualTo("Onboarding completed");
            assertThat(result.getBody().getData().arcStarted()).isTrue();
        }

        @Test
        @DisplayName("should throw BusinessException when user not found")
        void shouldThrowWhenUserNotFound() {
            when(authService.getCurrentUser(firebaseUid))
                    .thenThrow(new BusinessException("User not found for the authenticated account"));

            OnboardingRequest request = new OnboardingRequest();
            request.setSelectedGoals(List.of("fitness"));
            request.setDifficulty("balanced");
            request.setPersonalityType("disciplined");
            request.setSelectedArc("monk");
            request.setSelectedAvatar("phoenix");

            assertThatThrownBy(() -> controller.completeOnboarding(principal, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("User not found for the authenticated account");
        }
    }

    @Nested
    @DisplayName("getUserSummary")
    class GetUserSummary {

        @Test
        @DisplayName("should return 200 with user data")
        void shouldReturn200() {
            var result = controller.getUserSummary(principal);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody().getData().getUsername()).isEqualTo("newuser");
        }
    }
}
