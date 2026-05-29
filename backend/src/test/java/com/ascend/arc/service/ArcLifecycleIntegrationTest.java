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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration-style test for the full arc lifecycle:
 * start arc → complete milestones → phase transitions → arc completion.
 * Uses mocks but tests the full service interaction flow.
 */
@ExtendWith(MockitoExtension.class)
class ArcLifecycleIntegrationTest {

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

    private ArcService arcService;
    private ArcProgressService arcProgressService;

    private UUID userId;
    private UUID arcId;
    private Arc testArc;
    private List<ArcMilestone> milestones;

    @BeforeEach
    void setUp() {
        arcService = new ArcService(arcRepository, arcMilestoneRepository, userArcProgressRepository);
        arcProgressService = new ArcProgressService(
                arcRepository, arcMilestoneRepository,
                userArcProgressRepository, userMilestoneCompletionRepository,
                eventPublisher);

        userId = UUID.randomUUID();
        arcId = UUID.randomUUID();

        testArc = Arc.builder()
                .id(arcId)
                .name("Warrior Path")
                .description("30-day fitness journey")
                .type("WARRIOR")
                .difficulty("MEDIUM")
                .durationDays(30)
                .prebuilt(true)
                .build();

        // Create 4 milestones for clean 25% increments
        milestones = List.of(
                ArcMilestone.builder().id(UUID.randomUUID()).arcId(arcId).title("M1").orderIndex(1).xpReward(50).build(),
                ArcMilestone.builder().id(UUID.randomUUID()).arcId(arcId).title("M2").orderIndex(2).xpReward(50).build(),
                ArcMilestone.builder().id(UUID.randomUUID()).arcId(arcId).title("M3").orderIndex(3).xpReward(50).build(),
                ArcMilestone.builder().id(UUID.randomUUID()).arcId(arcId).title("M4").orderIndex(4).xpReward(50).build()
        );
    }

    @Test
    @DisplayName("Full lifecycle: start → milestones → phases → completion")
    void fullArcLifecycle() {
        // --- Step 1: Start Arc ---
        when(arcRepository.findById(arcId)).thenReturn(Optional.of(testArc));
        when(userArcProgressRepository.findByUserIdAndStatus(userId, "ACTIVE")).thenReturn(List.of());
        when(arcMilestoneRepository.findByArcIdOrderByOrderIndex(arcId)).thenReturn(milestones);

        UserArcProgress savedProgress = UserArcProgress.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .arcId(arcId)
                .progressPercent(0)
                .currentPhase(1)
                .status("ACTIVE")
                .startedAt(LocalDateTime.now())
                .build();
        when(userArcProgressRepository.save(any())).thenReturn(savedProgress);

        ArcProgressResponse startResponse = arcService.startArc(userId, arcId);

        assertThat(startResponse.getArcId()).isEqualTo(arcId);
        assertThat(startResponse.getProgressPercent()).isEqualTo(0);
        assertThat(startResponse.getCurrentPhase()).isEqualTo(ArcPhase.BEGINNER);
        assertThat(startResponse.getStatus()).isEqualTo(ArcStatus.ACTIVE);

        // --- Step 2: Complete milestone 1 (25% → INTERMEDIATE) ---
        UUID m1Id = milestones.get(0).getId();
        when(arcMilestoneRepository.findById(m1Id)).thenReturn(Optional.of(milestones.get(0)));
        when(userArcProgressRepository.findByUserIdAndArcId(userId, arcId)).thenReturn(Optional.of(savedProgress));
        when(userMilestoneCompletionRepository.findByUserIdAndMilestoneId(userId, m1Id)).thenReturn(Optional.empty());
        when(userMilestoneCompletionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(userMilestoneCompletionRepository.countByUserIdAndArcId(userId, arcId)).thenReturn(1);
        when(userArcProgressRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ArcProgressResponse m1Response = arcProgressService.completeMilestone(userId, arcId, m1Id);

        assertThat(m1Response.getProgressPercent()).isEqualTo(25);
        assertThat(m1Response.getCurrentPhase()).isEqualTo(ArcPhase.INTERMEDIATE);

        // Verify phase transition event
        ArgumentCaptor<ArcPhaseCompleteEvent> phaseCaptor = ArgumentCaptor.forClass(ArcPhaseCompleteEvent.class);
        verify(eventPublisher).publishEvent(phaseCaptor.capture());
        assertThat(phaseCaptor.getValue().getNewPhase()).isEqualTo(ArcPhase.INTERMEDIATE);

        // --- Step 3: Complete all remaining milestones (100% → COMPLETED) ---
        reset(eventPublisher);

        UUID m4Id = milestones.get(3).getId();
        savedProgress.setProgressPercent(75);
        savedProgress.setCurrentPhase(4); // MASTER

        when(arcMilestoneRepository.findById(m4Id)).thenReturn(Optional.of(milestones.get(3)));
        when(userArcProgressRepository.findByUserIdAndArcId(userId, arcId)).thenReturn(Optional.of(savedProgress));
        when(userMilestoneCompletionRepository.findByUserIdAndMilestoneId(userId, m4Id)).thenReturn(Optional.empty());
        when(userMilestoneCompletionRepository.countByUserIdAndArcId(userId, arcId)).thenReturn(4);

        ArcProgressResponse finalResponse = arcProgressService.completeMilestone(userId, arcId, m4Id);

        assertThat(finalResponse.getProgressPercent()).isEqualTo(100);
        assertThat(finalResponse.getStatus()).isEqualTo(ArcStatus.COMPLETED);

        // Verify arc completed event
        ArgumentCaptor<Object> completedCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, atLeast(1)).publishEvent(completedCaptor.capture());

        boolean hasCompletedEvent = completedCaptor.getAllValues().stream()
                .anyMatch(e -> e instanceof ArcCompletedEvent);
        assertThat(hasCompletedEvent).isTrue();
    }
}
