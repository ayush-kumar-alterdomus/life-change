package com.ascend.arc.service;

import com.ascend.arc.dto.ArcDetailResponse;
import com.ascend.arc.dto.ArcPhase;
import com.ascend.arc.dto.ArcProgressResponse;
import com.ascend.arc.dto.ArcResponse;
import com.ascend.arc.entity.ArcStatus;
import com.ascend.arc.dto.ArcType;
import com.ascend.arc.entity.Arc;
import com.ascend.arc.entity.ArcMilestone;
import com.ascend.arc.entity.UserArcProgress;
import com.ascend.arc.repository.ArcMilestoneRepository;
import com.ascend.arc.repository.ArcRepository;
import com.ascend.arc.repository.UserArcProgressRepository;
import com.ascend.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for arc catalog retrieval and lifecycle management.
 * Handles listing available arcs, fetching arc details with milestones,
 * starting arcs for users, and abandoning active arcs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArcService {

    private final ArcRepository arcRepository;
    private final ArcMilestoneRepository arcMilestoneRepository;
    private final UserArcProgressRepository userArcProgressRepository;

    /**
     * Returns all available arcs (prebuilt system arcs + user's custom arcs).
     *
     * @return list of arc responses
     */
    @Transactional(readOnly = true)
    public List<ArcResponse> getAvailableArcs() {
        log.debug("Fetching all available arcs");

        List<Arc> arcs = arcRepository.findAll();

        return arcs.stream()
                .map(this::toArcResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns detailed arc information including milestones.
     *
     * @param arcId the arc ID
     * @return arc detail response with milestones
     * @throws BusinessException if arc not found
     */
    @Transactional(readOnly = true)
    public ArcDetailResponse getArcDetail(UUID arcId) {
        log.debug("Fetching arc detail for arcId={}", arcId);

        Arc arc = arcRepository.findById(arcId)
                .orElseThrow(() -> new BusinessException("ARC_NOT_FOUND",
                        "Arc not found with id: " + arcId));

        List<ArcMilestone> milestones = arcMilestoneRepository.findByArcIdOrderByOrderIndex(arcId);

        List<ArcDetailResponse.MilestoneResponse> milestoneResponses = milestones.stream()
                .map(m -> ArcDetailResponse.MilestoneResponse.builder()
                        .id(m.getId())
                        .title(m.getTitle())
                        .description(m.getDescription())
                        .orderIndex(m.getOrderIndex())
                        .xpReward(m.getXpReward())
                        .build())
                .collect(Collectors.toList());

        return ArcDetailResponse.builder()
                .id(arc.getId())
                .name(arc.getName())
                .description(arc.getDescription())
                .type(parseArcType(arc.getType()))
                .difficulty(arc.getDifficulty())
                .durationDays(arc.getDurationDays())
                .isPrebuilt(arc.getPrebuilt())
                .milestoneCount(milestones.size())
                .milestones(milestoneResponses)
                .build();
    }

    /**
     * Starts an arc for a user. Verifies the user doesn't already have an active
     * arc for the same arc ID, creates a progress record, and returns progress info.
     *
     * @param userId the user's ID
     * @param arcId  the arc to start
     * @return arc progress response
     * @throws BusinessException if arc not found or user already has an active arc
     */
    @Transactional
    public ArcProgressResponse startArc(UUID userId, UUID arcId) {
        log.info("Starting arc={} for user={}", arcId, userId);

        // Verify arc exists
        Arc arc = arcRepository.findById(arcId)
                .orElseThrow(() -> new BusinessException("ARC_NOT_FOUND",
                        "Arc not found with id: " + arcId));

        // Verify user doesn't already have an active arc
        List<UserArcProgress> activeArcs = userArcProgressRepository
                .findByUserIdAndStatus(userId, ArcStatus.ACTIVE.name());

        if (!activeArcs.isEmpty()) {
            throw new BusinessException("ARC_ALREADY_ACTIVE",
                    "User already has an active arc. Abandon or complete the current arc before starting a new one.");
        }

        // Create user_arc_progress record
        UserArcProgress progress = UserArcProgress.builder()
                .userId(userId)
                .arcId(arcId)
                .progressPercent(0)
                .currentPhase(ArcPhase.BEGINNER.ordinal() + 1)
                .status(ArcStatus.ACTIVE.name())
                .build();

        UserArcProgress savedProgress = userArcProgressRepository.save(progress);

        // Determine total milestones for the arc
        List<ArcMilestone> milestones = arcMilestoneRepository.findByArcIdOrderByOrderIndex(arcId);

        log.info("Arc started: arcId={}, userId={}, progressId={}", arcId, userId, savedProgress.getId());

        return ArcProgressResponse.builder()
                .arcId(arc.getId())
                .arcName(arc.getName())
                .progressPercent(savedProgress.getProgressPercent())
                .currentPhase(ArcPhase.BEGINNER)
                .startedAt(savedProgress.getStartedAt())
                .status(ArcStatus.ACTIVE)
                .milestonesCompleted(0)
                .totalMilestones(milestones.size())
                .build();
    }

    /**
     * Abandons an active arc for a user by setting its status to ABANDONED.
     *
     * @param userId the user's ID
     * @param arcId  the arc to abandon
     * @throws BusinessException if no active progress found for the user and arc
     */
    @Transactional
    public void abandonArc(UUID userId, UUID arcId) {
        log.info("Abandoning arc={} for user={}", arcId, userId);

        UserArcProgress progress = userArcProgressRepository.findByUserIdAndArcId(userId, arcId)
                .orElseThrow(() -> new BusinessException("ARC_PROGRESS_NOT_FOUND",
                        "No arc progress found for user=" + userId + " and arc=" + arcId));

        if (!ArcStatus.ACTIVE.name().equals(progress.getStatus())) {
            throw new BusinessException("ARC_NOT_ACTIVE",
                    "Arc is not currently active. Current status: " + progress.getStatus());
        }

        progress.setStatus(ArcStatus.ABANDONED.name());
        userArcProgressRepository.save(progress);

        log.info("Arc abandoned: arcId={}, userId={}", arcId, userId);
    }

    /**
     * Maps an Arc entity to an ArcResponse DTO.
     */
    private ArcResponse toArcResponse(Arc arc) {
        List<ArcMilestone> milestones = arcMilestoneRepository.findByArcIdOrderByOrderIndex(arc.getId());

        return ArcResponse.builder()
                .id(arc.getId())
                .name(arc.getName())
                .description(arc.getDescription())
                .type(parseArcType(arc.getType()))
                .difficulty(arc.getDifficulty())
                .durationDays(arc.getDurationDays())
                .isPrebuilt(arc.getPrebuilt())
                .milestoneCount(milestones.size())
                .build();
    }

    /**
     * Safely parses an arc type string to the ArcType enum.
     * Returns CUSTOM if the type string doesn't match any known type.
     */
    private ArcType parseArcType(String type) {
        if (type == null) {
            return ArcType.CUSTOM;
        }
        try {
            return ArcType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ArcType.CUSTOM;
        }
    }
}
