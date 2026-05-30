package com.ascend.user.controller;

import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.service.AuthService;
import com.ascend.common.dto.ApiResponse;
import com.ascend.common.exception.BusinessException;
import com.ascend.user.dto.OnboardingRequest;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController")
class UserControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserController controller;

    private User testUser;
    private FirebasePrincipal principal;
    private final String firebaseUid = "firebase-uid-456";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .firebaseUid(firebaseUid)
                .username("newuser")
                .level(1)
                .xp(0L)
                .build();

        principal = new FirebasePrincipal(firebaseUid, "new@example.com", "password", Map.of());
        when(authService.getCurrentUser(firebaseUid)).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
    }

    @Nested
    @DisplayName("completeOnboarding")
    class CompleteOnboarding {

        @Test
        @DisplayName("should return 200 on valid onboarding request")
        void shouldReturn200OnValidRequest() {
            OnboardingRequest request = new OnboardingRequest();
            request.setSelectedGoals(List.of("fitness", "mindfulness"));
            request.setDifficulty("balanced");
            request.setPersonalityType("disciplined");
            request.setSelectedArc("monk");
            request.setSelectedAvatar("phoenix");

            ResponseEntity<ApiResponse<Void>> result = controller.completeOnboarding(principal, request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().isSuccess()).isTrue();
            assertThat(result.getBody().getMessage()).isEqualTo("Onboarding completed");
        }

        @Test
        @DisplayName("should save user with avatar URL from request")
        void shouldSaveUserWithAvatar() {
            OnboardingRequest request = new OnboardingRequest();
            request.setSelectedGoals(List.of("fitness"));
            request.setDifficulty("balanced");
            request.setPersonalityType("disciplined");
            request.setSelectedArc("monk");
            request.setSelectedAvatar("dragon");

            controller.completeOnboarding(principal, request);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getAvatarUrl()).isEqualTo("dragon");
        }

        @Test
        @DisplayName("should set hardMode true when difficulty is legendary")
        void shouldSetHardModeForLegendary() {
            OnboardingRequest request = new OnboardingRequest();
            request.setSelectedGoals(List.of("fitness"));
            request.setDifficulty("legendary");
            request.setPersonalityType("competitive");
            request.setSelectedArc("warrior");
            request.setSelectedAvatar("wolf");

            controller.completeOnboarding(principal, request);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getHardMode()).isTrue();
        }

        @Test
        @DisplayName("should set hardMode false when difficulty is not legendary")
        void shouldNotSetHardModeForNonLegendary() {
            OnboardingRequest request = new OnboardingRequest();
            request.setSelectedGoals(List.of("fitness"));
            request.setDifficulty("balanced");
            request.setPersonalityType("disciplined");
            request.setSelectedArc("monk");
            request.setSelectedAvatar("phoenix");

            controller.completeOnboarding(principal, request);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getHardMode()).isFalse();
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
}
