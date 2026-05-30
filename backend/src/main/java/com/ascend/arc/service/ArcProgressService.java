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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing arc progress including milestone completion,
 * phase transitions, and progress tracking.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArcProgressService {

    private final ArcRepository arcRepository;
    private final ArcMilestoneRepository arcMilestoneRepository;
    private final UserArcProgressRepository userArcProgressRepository;
    private final UserMilestoneCompletionRepository userMilestoneCompletionRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Completes a milestone for a user within an arc.
     * Recalculates progress, checks phase transitions, and handles arc completion.
     *
     * @param userId      the user's ID
     * @param arcId       the arc ID
     * @param milestoneId the milestone to complete
     * @return updated arc progress response
     */
    @Transactional
    public ArcProgressResponse completeMilestone(UUID userId, UUID arcId, UUID milestoneId) {
        log.info("Completing milestone={} for user={} in arc={}", milestoneId, userId, arcId);

        // 1. Verify milestone belongs to arc
        ArcMilestone milestone = arcMilestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new BusinessException("MILESTONE_NOT_FOUND",
                        "Milestone not found with id: " + milestoneId));

        if (!milestone.getArcId().equals(arcId)) {
            throw new BusinessException("MILESTONE_ARC_MISMATCH",
                    "Milestone does not belong to the specified arc");
        }

        // Verify user has active progress for this arc
        UserArcProgress progress = userArcProgressRepository.findByUserIdAndArcId(userId, arcId)
                .orElseThrow(() -> new BusinessException("ARC_PROGRESS_NOT_FOUND",
                        "No arc progress found for user=" + userId + " and arc=" + arcId));

        if (!ArcStatus.ACTIVE.name().equals(progress.getStatus())) {
            throw new BusinessException("ARC_NOT_ACTIVE",
                    "Arc is not currently active. Current status: " + progress.getStatus());
        }

        // Check if milestone already completed
        if (userMilestoneCompletionRepository.findByUserIdAndMilestoneId(userId, milestoneId).isPresent()) {
            throw new BusinessException("MILESTONE_ALREADY_COMPLETED",
                    "Milestone has already been completed");
        }

        // 2. Mark milestone completed
        UserMilestoneCompletion completion = UserMilestoneCompletion.builder()
                .userId(userId)
                .arcId(arcId)
                .milestoneId(milestoneId)
                .build();
        userMilestoneCompletionRepository.save(completion);

        // 3. Recalculate progress_percent
        List<ArcMilestone> allMilestones = arcMilestoneRepository.findByArcIdOrderByOrderIndex(arcId);
        int totalMilestones = allMilestones.size();
        int completedCount = userMilestoneCompletionRepository.countByUserIdAndArcId(userId, arcId);
        int progressPercent = (int) ((completedCount * 100.0) / totalMilestones);

        progress.setProgressPercent(progressPercent);

        // 4. Award milestone XP (logged; actual XP awarding delegated to XP service via event)
        log.info("Milestone completed: xpReward={} for user={}", milestone.getXpReward(), userId);

        // 5. Check phase transition
        ArcPhase previousPhase = phaseFromOrdinal(progress.getCurrentPhase());
        ArcPhase newPhase = determinePhase(progressPercent);

        if (newPhase.ordinal() > previousPhase.ordinal()) {
            progress.setCurrentPhase(newPhase.ordinal() + 1);

            // 6. Publish ArcPhaseCompleteEvent
            ArcPhaseCompleteEvent phaseEvent = ArcPhaseCompleteEvent.builder()
                    .userId(userId)
                    .arcId(arcId)
                    .previousPhase(previousPhase)
                    .newPhase(newPhase)
                    .progressPercent(progressPercent)
                    .build();
            eventPublisher.publishEvent(phaseEvent);

            log.info("Phase transition: {} → {} for user={} in arc={}", previousPhase, newPhase, userId, arcId);
        }

        // 7. If 100% → mark arc COMPLETED
        if (progressPercent >= 100) {
            progress.setStatus(ArcStatus.COMPLETED.name());
            progress.setCompletedAt(LocalDateTime.now());

            Arc arc = arcRepository.findById(arcId)
                    .orElseThrow(() -> new BusinessException("ARC_NOT_FOUND", "Arc not found"));

            ArcCompletedEvent completedEvent = ArcCompletedEvent.builder()
                    .userId(userId)
                    .arcId(arcId)
                    .arcName(arc.getName())
                    .totalDays(java.time.Duration.between(progress.getStartedAt(), LocalDateTime.now()).toDays())
                    .completionXp(calculateCompletionXp(allMilestones))
                    .build();
            eventPublisher.publishEvent(completedEvent);

            log.info("Arc completed: arcId={}, userId={}, totalDays={}", arcId, userId, completedEvent.getTotalDays());
        }

        userArcProgressRepository.save(progress);

        return ArcProgressResponse.builder()
                .arcId(arcId)
                .arcName(arcRepository.findById(arcId).map(Arc::getName).orElse("Unknown"))
                .progressPercent(progressPercent)
                .currentPhase(newPhase)
                .startedAt(progress.getStartedAt())
                .status(ArcStatus.valueOf(progress.getStatus()))
                .milestonesCompleted(completedCount)
                .totalMilestones(totalMilestones)
                .build();
    }

    /**
     * Returns the current progress for a user's arc.
     *
     * @param userId the user's ID
     * @param arcId  the arc ID
     * @return arc progress response
     */
    @Transactional(readOnly = true)
    public ArcProgressResponse getProgress(UUID userId, UUID arcId) {
        log.debug("Fetching progress for user={} in arc={}", userId, arcId);

        UserArcProgress progress = userArcProgressRepository.findByUserIdAndArcId(userId, arcId)
                .orElseThrow(() -> new BusinessException("ARC_PROGRESS_NOT_FOUND",
                        "No arc progress found for user=" + userId + " and arc=" + arcId));

        Arc arc = arcRepository.findById(arcId)
                .orElseThrow(() -> new BusinessException("ARC_NOT_FOUND",
                        "Arc not found with id: " + arcId));

        List<ArcMilestone> allMilestones = arcMilestoneRepository.findByArcIdOrderByOrderIndex(arcId);
        int completedCount = userMilestoneCompletionRepository.countByUserIdAndArcId(userId, arcId);

        return ArcProgressResponse.builder()
                .arcId(arcId)
                .arcName(arc.getName())
                .progressPercent(progress.getProgressPercent())
                .currentPhase(phaseFromOrdinal(progress.getCurrentPhase()))
                .startedAt(progress.getStartedAt())
                .status(ArcStatus.valueOf(progress.getStatus()))
                .milestonesCompleted(completedCount)
                .totalMilestones(allMilestones.size())
                .build();
    }

    /**
     * Returns the user's currently active arc progress, or null if none.
     *
     * @param userId the user's ID
     * @return arc progress response or null
     */
    @Transactional(readOnly = true)
    public ArcProgressResponse getActiveArc(UUID userId) {
        log.debug("Fetching active arc for user={}", userId);

        return userArcProgressRepository.findByUserIdAndStatus(userId, ArcStatus.ACTIVE.name())
                .stream()
                .findFirst()
                .map(progress -> {
                    Arc arc = arcRepository.findById(progress.getArcId()).orElse(null);
                    List<ArcMilestone> allMilestones = arcMilestoneRepository.findByArcIdOrderByOrderIndex(progress.getArcId());
                    int completedCount = userMilestoneCompletionRepository.countByUserIdAndArcId(userId, progress.getArcId());

                    return ArcProgressResponse.builder()
                            .arcId(progress.getArcId())
                            .arcName(arc != null ? arc.getName() : "Unknown")
                            .progressPercent(progress.getProgressPercent())
                            .currentPhase(phaseFromOrdinal(progress.getCurrentPhase()))
                            .startedAt(progress.getStartedAt())
                            .status(ArcStatus.ACTIVE)
                            .milestonesCompleted(completedCount)
                            .totalMilestones(allMilestones.size())
                            .build();
                })
                .orElse(null);
    }

    /**
     * Determines the arc phase based on progress percentage.
     * 0-24% → BEGINNER, 25-49% → INTERMEDIATE, 50-74% → ELITE, 75-100% → MASTER
     */
    private ArcPhase determinePhase(int progressPercent) {
        if (progressPercent >= 75) {
            return ArcPhase.MASTER;
        } else if (progressPercent >= 50) {
            return ArcPhase.ELITE;
        } else if (progressPercent >= 25) {
            return ArcPhase.INTERMEDIATE;
        }
        return ArcPhase.BEGINNER;
    }

    /**
     * Converts a 1-based phase ordinal back to ArcPhase enum.
     */
    private ArcPhase phaseFromOrdinal(int phaseOrdinal) {
        int index = phaseOrdinal - 1;
        ArcPhase[] phases = ArcPhase.values();
        if (index >= 0 && index < phases.length) {
            return phases[index];
        }
        return ArcPhase.BEGINNER;
    }

    /**
     * Calculates total completion XP bonus (sum of all milestone XP rewards × 0.5 bonus).
     */
    private int calculateCompletionXp(List<ArcMilestone> milestones) {
        int totalMilestoneXp = milestones.stream()
                .mapToInt(ArcMilestone::getXpReward)
                .sum();
        return (int) (totalMilestoneXp * 0.5);
    }
}
