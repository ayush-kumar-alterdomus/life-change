package com.ascend.arc.service;

import com.ascend.arc.dto.ArcPhase;
import com.ascend.arc.dto.ArcProgressResponse;
import com.ascend.arc.entity.ArcStatus;
import com.ascend.arc.entity.Arc;
import com.ascend.arc.entity.ArcMilestone;
import com.ascend.arc.entity.UserArcProgress;
import com.ascend.arc.entity.UserMilestoneCompletion;
import com.ascend.arc.event.ArcCompletedEvent;
import com.ascend.arc.event.ArcPhaseCompleteEvent;
import com.ascend.arc.repository.ArcMilestoneRepository;
import com.ascend.arc.repository.ArcRepository;
import com.ascend.arc.repository.UserArcProgressRepository;
import com.ascend.arc.repository.UserMilestoneCompletionRepository;
import com.ascend.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ArcProgressService.
 * Tests milestone completion, phase transitions, and arc completion.
 */
@ExtendWith(MockitoExtension.class)
class ArcProgressServiceTest {

    @Mock
    private ArcRepository arcRepository;

    @Mock
    private ArcMilestoneRepository arcMilestoneRepository;

    @Mock
    private UserArcProgressRepository userArcProgressRepository;

    @Mock
    private UserMilestoneCompletionRepository userMilestoneCompletionRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ArcProgressService arcProgressService;

    private UUID userId;
    private UUID arcId;
    private UUID milestoneId;
    private Arc testArc;
    private ArcMilestone testMilestone;
    private UserArcProgress testProgress;

    @BeforeEach
    void setUp() {
        arcProgressService = new ArcProgressService(
                arcRepository, arcMilestoneRepository,
                userArcProgressRepository, userMilestoneCompletionRepository,
                eventPublisher);

        userId = UUID.randomUUID();
        arcId = UUID.randomUUID();
        milestoneId = UUID.randomUUID();

        testArc = Arc.builder()
                .id(arcId)
                .name("Warrior Path")
                .description("Fitness arc")
                .type("WARRIOR")
                .difficulty("MEDIUM")
                .durationDays(30)
                .prebuilt(true)
                .build();

        testMilestone = ArcMilestone.builder()
                .id(milestoneId)
                .arcId(arcId)
                .title("First Workout")
                .description("Complete your first workout")
                .orderIndex(1)
                .xpReward(50)
                .build();

        testProgress = UserArcProgress.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .arcId(arcId)
                .progressPercent(0)
                .currentPhase(1) // BEGINNER
                .status(ArcStatus.ACTIVE.name())
                .startedAt(LocalDateTime.now().minusDays(5))
                .build();
    }

    @Test
    @DisplayName("Complete milestone updates progress percentage correctly")
    void completeMilestone_updatesProgressPercent() {
        // 4 milestones total, completing 1st = 25%
        List<ArcMilestone> milestones = List.of(
                testMilestone,
                ArcMilestone.builder().id(UUID.randomUUID()).arcId(arcId).title("M2").orderIndex(2).xpReward(50).build(),
                ArcMilestone.builder().id(UUID.randomUUID()).arcId(arcId).title("M3").orderIndex(3).xpReward(50).build(),
                ArcMilestone.builder().id(UUID.randomUUID()).arcId(arcId).title("M4").orderIndex(4).xpReward(50).build()
        );

        when(arcMilestoneRepository.findById(milestoneId)).thenReturn(Optional.of(testMilestone));
        when(userArcProgressRepository.findByUserIdAndArcId(userId, arcId)).thenReturn(Optional.of(testProgress));
        when(userMilestoneCompletionRepository.findByUserIdAndMilestoneId(userId, milestoneId)).thenReturn(Optional.empty());
        when(userMilestoneCompletionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(arcMilestoneRepository.findByArcIdOrderByOrderIndex(arcId)).thenReturn(milestones);
        when(userMilestoneCompletionRepository.countByUserIdAndArcId(userId, arcId)).thenReturn(1);
        when(arcRepository.findById(arcId)).thenReturn(Optional.of(testArc));
        when(userArcProgressRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ArcProgressResponse response = arcProgressService.completeMilestone(userId, arcId, milestoneId);

        assertThat(response.getProgressPercent()).isEqualTo(25);
        assertThat(response.getMilestonesCompleted()).isEqualTo(1);
        assertThat(response.getTotalMilestones()).isEqualTo(4);
    }

    @Test
    @DisplayName("Phase transition fires ArcPhaseCompleteEvent at 25%")
    void completeMilestone_phaseTransition_publishesEvent() {
        List<ArcMilestone> milestones = List.of(
                testMilestone,
                ArcMilestone.builder().id(UUID.randomUUID()).arcId(arcId).title("M2").orderIndex(2).xpReward(50).build(),
                ArcMilestone.builder().id(UUID.randomUUID()).arcId(arcId).title("M3").orderIndex(3).xpReward(50).build(),
                ArcMilestone.builder().id(UUID.randomUUID()).arcId(arcId).title("M4").orderIndex(4).xpReward(50).build()
        );

        when(arcMilestoneRepository.findById(milestoneId)).thenReturn(Optional.of(testMilestone));
        when(userArcProgressRepository.findByUserIdAndArcId(userId, arcId)).thenReturn(Optional.of(testProgress));
        when(userMilestoneCompletionRepository.findByUserIdAndMilestoneId(userId, milestoneId)).thenReturn(Optional.empty());
        when(userMilestoneCompletionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(arcMilestoneRepository.findByArcIdOrderByOrderIndex(arcId)).thenReturn(milestones);
        when(userMilestoneCompletionRepository.countByUserIdAndArcId(userId, arcId)).thenReturn(1);
        when(arcRepository.findById(arcId)).thenReturn(Optional.of(testArc));
        when(userArcProgressRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        arcProgressService.completeMilestone(userId, arcId, milestoneId);

        ArgumentCaptor<ArcPhaseCompleteEvent> eventCaptor = ArgumentCaptor.forClass(ArcPhaseCompleteEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        ArcPhaseCompleteEvent event = eventCaptor.getValue();
        assertThat(event.getUserId()).isEqualTo(userId);
        assertThat(event.getArcId()).isEqualTo(arcId);
        assertThat(event.getPreviousPhase()).isEqualTo(ArcPhase.BEGINNER);
        assertThat(event.getNewPhase()).isEqualTo(ArcPhase.INTERMEDIATE);
        assertThat(event.getProgressPercent()).isEqualTo(25);
    }

    @Test
    @DisplayName("Arc completion at 100% fires ArcCompletedEvent")
    void completeMilestone_arcCompletion_publishesCompletedEvent() {
        // Single milestone arc, completing it = 100%
        List<ArcMilestone> milestones = List.of(testMilestone);

        when(arcMilestoneRepository.findById(milestoneId)).thenReturn(Optional.of(testMilestone));
        when(userArcProgressRepository.findByUserIdAndArcId(userId, arcId)).thenReturn(Optional.of(testProgress));
        when(userMilestoneCompletionRepository.findByUserIdAndMilestoneId(userId, milestoneId)).thenReturn(Optional.empty());
        when(userMilestoneCompletionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(arcMilestoneRepository.findByArcIdOrderByOrderIndex(arcId)).thenReturn(milestones);
        when(userMilestoneCompletionRepository.countByUserIdAndArcId(userId, arcId)).thenReturn(1);
        when(arcRepository.findById(arcId)).thenReturn(Optional.of(testArc));
        when(userArcProgressRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ArcProgressResponse response = arcProgressService.completeMilestone(userId, arcId, milestoneId);

        assertThat(response.getProgressPercent()).isEqualTo(100);
        assertThat(response.getStatus()).isEqualTo(ArcStatus.COMPLETED);

        // Verify ArcCompletedEvent was published
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, atLeast(1)).publishEvent(eventCaptor.capture());

        boolean hasCompletedEvent = eventCaptor.getAllValues().stream()
                .anyMatch(e -> e instanceof ArcCompletedEvent);
        assertThat(hasCompletedEvent).isTrue();
    }

    @Test
    @DisplayName("Milestone not belonging to arc throws exception")
    void completeMilestone_mismatchedArc_throwsException() {
        UUID otherArcId = UUID.randomUUID();
        ArcMilestone otherMilestone = ArcMilestone.builder()
                .id(milestoneId)
                .arcId(otherArcId)
                .title("Other")
                .orderIndex(1)
                .xpReward(50)
                .build();

        when(arcMilestoneRepository.findById(milestoneId)).thenReturn(Optional.of(otherMilestone));

        assertThatThrownBy(() -> arcProgressService.completeMilestone(userId, arcId, milestoneId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("does not belong");
    }

    @Test
    @DisplayName("Already completed milestone throws exception")
    void completeMilestone_alreadyCompleted_throwsException() {
        when(arcMilestoneRepository.findById(milestoneId)).thenReturn(Optional.of(testMilestone));
        when(userArcProgressRepository.findByUserIdAndArcId(userId, arcId)).thenReturn(Optional.of(testProgress));
        when(userMilestoneCompletionRepository.findByUserIdAndMilestoneId(userId, milestoneId))
                .thenReturn(Optional.of(UserMilestoneCompletion.builder().build()));

        assertThatThrownBy(() -> arcProgressService.completeMilestone(userId, arcId, milestoneId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already been completed");
    }

    @Test
    @DisplayName("getProgress returns correct progress data")
    void getProgress_returnsCorrectData() {
        testProgress.setProgressPercent(50);
        testProgress.setCurrentPhase(3); // ELITE

        List<ArcMilestone> milestones = List.of(
                testMilestone,
                ArcMilestone.builder().id(UUID.randomUUID()).arcId(arcId).title("M2").orderIndex(2).xpReward(50).build()
        );

        when(userArcProgressRepository.findByUserIdAndArcId(userId, arcId)).thenReturn(Optional.of(testProgress));
        when(arcRepository.findById(arcId)).thenReturn(Optional.of(testArc));
        when(arcMilestoneRepository.findByArcIdOrderByOrderIndex(arcId)).thenReturn(milestones);
        when(userMilestoneCompletionRepository.countByUserIdAndArcId(userId, arcId)).thenReturn(1);

        ArcProgressResponse response = arcProgressService.getProgress(userId, arcId);

        assertThat(response.getArcId()).isEqualTo(arcId);
        assertThat(response.getArcName()).isEqualTo("Warrior Path");
        assertThat(response.getProgressPercent()).isEqualTo(50);
        assertThat(response.getCurrentPhase()).isEqualTo(ArcPhase.ELITE);
        assertThat(response.getStatus()).isEqualTo(ArcStatus.ACTIVE);
        assertThat(response.getMilestonesCompleted()).isEqualTo(1);
        assertThat(response.getTotalMilestones()).isEqualTo(2);
    }
}
