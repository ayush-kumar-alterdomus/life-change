package com.ascend.user.service;

import com.ascend.arc.dto.ArcPhase;
import com.ascend.arc.dto.ArcProgressResponse;
import com.ascend.arc.entity.ArcStatus;
import com.ascend.arc.service.ArcService;
import com.ascend.common.exception.BusinessException;
import com.ascend.user.dto.OnboardingRequest;
import com.ascend.user.dto.OnboardingResponse;
import com.ascend.user.dto.OnboardingStatusResponse;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OnboardingService")
class OnboardingServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private ArcService arcService;

    private OnboardingService service;
    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        service = new OnboardingService(userRepository, arcService, new ObjectMapper());
        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId).username("alice").level(1).xp(0L)
                .onboardingComplete(false).build();
    }

    @Nested
    @DisplayName("completeOnboarding()")
    class CompleteOnboarding {

        @Test
        @DisplayName("should persist all fields and start arc")
        void shouldPersistAndStartArc() {
            UUID arcId = UUID.randomUUID();
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(arcService.startArc(eq(userId), eq(arcId))).thenReturn(
                    ArcProgressResponse.builder()
                            .arcId(arcId).arcName("Warrior Path")
                            .progressPercent(0).currentPhase(ArcPhase.BEGINNER)
                            .status(ArcStatus.ACTIVE).build());

            OnboardingRequest request = new OnboardingRequest();
            request.setSelectedGoals(List.of("fitness", "mindfulness"));
            request.setDifficulty("balanced");
            request.setPersonalityType("disciplined");
            request.setSelectedArc(arcId.toString());
            request.setSelectedAvatar("avatar_knight");

            OnboardingResponse result = service.completeOnboarding(userId, request);

            assertThat(result.arcStarted()).isTrue();
            assertThat(result.arcId()).isEqualTo(arcId.toString());
            assertThat(result.arcName()).isEqualTo("Warrior Path");
            assertThat(result.level()).isEqualTo(1);

            assertThat(user.getOnboardingComplete()).isTrue();
            assertThat(user.getPersonalityType()).isEqualTo("disciplined");
            assertThat(user.getDifficultyPreference()).isEqualTo("balanced");
            assertThat(user.getAvatarUrl()).isEqualTo("avatar_knight");
            assertThat(user.getSelectedGoals()).contains("fitness");
        }

        @Test
        @DisplayName("should throw 409 when already complete")
        void shouldThrowWhenAlreadyComplete() {
            user.setOnboardingComplete(true);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            OnboardingRequest request = new OnboardingRequest();
            request.setSelectedGoals(List.of("fitness"));
            request.setDifficulty("balanced");
            request.setPersonalityType("disciplined");
            request.setSelectedArc("warrior");
            request.setSelectedAvatar("avatar");

            assertThatThrownBy(() -> service.completeOnboarding(userId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "ONBOARDING_ALREADY_COMPLETE");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should succeed even when arc start fails")
        void shouldSucceedWhenArcFails() {
            UUID arcId = UUID.randomUUID();
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(arcService.startArc(eq(userId), eq(arcId)))
                    .thenThrow(new RuntimeException("Arc not found"));

            OnboardingRequest request = new OnboardingRequest();
            request.setSelectedGoals(List.of("fitness"));
            request.setDifficulty("balanced");
            request.setPersonalityType("competitive");
            request.setSelectedArc(arcId.toString());
            request.setSelectedAvatar("avatar");

            OnboardingResponse result = service.completeOnboarding(userId, request);

            assertThat(result.arcStarted()).isFalse();
            assertThat(result.arcId()).isNull();
            assertThat(user.getOnboardingComplete()).isTrue();
        }

        @Test
        @DisplayName("should succeed when arc identifier is not a UUID")
        void shouldSucceedWhenArcIsSlug() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            OnboardingRequest request = new OnboardingRequest();
            request.setSelectedGoals(List.of("learning"));
            request.setDifficulty("casual");
            request.setPersonalityType("analytical");
            request.setSelectedArc("monk");
            request.setSelectedAvatar("avatar_sage");

            OnboardingResponse result = service.completeOnboarding(userId, request);

            assertThat(result.arcStarted()).isFalse();
            assertThat(user.getOnboardingComplete()).isTrue();
            verify(arcService, never()).startArc(any(), any());
        }
    }

    @Nested
    @DisplayName("getOnboardingStatus()")
    class GetOnboardingStatus {

        @Test
        @DisplayName("should return complete status with data")
        void shouldReturnCompleteStatus() {
            user.setOnboardingComplete(true);
            user.setSelectedGoals("[\"fitness\",\"mindfulness\"]");
            user.setPersonalityType("disciplined");
            user.setDifficultyPreference("balanced");
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            OnboardingStatusResponse result = service.getOnboardingStatus(userId);

            assertThat(result.complete()).isTrue();
            assertThat(result.selectedGoals()).containsExactly("fitness", "mindfulness");
            assertThat(result.personalityType()).isEqualTo("disciplined");
            assertThat(result.difficulty()).isEqualTo("balanced");
        }

        @Test
        @DisplayName("should return incomplete status for new user")
        void shouldReturnIncompleteStatus() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            OnboardingStatusResponse result = service.getOnboardingStatus(userId);

            assertThat(result.complete()).isFalse();
            assertThat(result.selectedGoals()).isNull();
            assertThat(result.personalityType()).isNull();
        }
    }
}
