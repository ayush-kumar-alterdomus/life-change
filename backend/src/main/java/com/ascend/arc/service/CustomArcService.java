package com.ascend.arc.service;

import com.ascend.arc.dto.ArcDetailResponse;
import com.ascend.arc.dto.ArcType;
import com.ascend.arc.dto.CreateArcRequest;
import com.ascend.arc.entity.Arc;
import com.ascend.arc.entity.ArcMilestone;
import com.ascend.arc.repository.ArcMilestoneRepository;
import com.ascend.arc.repository.ArcRepository;
import com.ascend.arc.validator.ArcValidator;
import com.ascend.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Service for creating custom arcs.
 * Handles validation, persistence, and premium tier enforcement.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomArcService {

    private final ArcRepository arcRepository;
    private final ArcMilestoneRepository arcMilestoneRepository;
    private final ArcValidator arcValidator;

    private static final int FREE_USER_MAX_CUSTOM_ARCS = 1;
    private static final int DEFAULT_MILESTONE_XP = 50;

    /**
     * Creates a custom arc for a user.
     * Free users are limited to 1 custom arc; premium users have unlimited.
     *
     * @param userId    the user's ID
     * @param request   the create arc request
     * @param isPremium whether the user has a premium subscription
     * @return the created arc detail response
     */
    @Transactional
    public ArcDetailResponse createCustomArc(UUID userId, CreateArcRequest request, boolean isPremium) {
        log.info("Creating custom arc for user={}, title={}", userId, request.getTitle());

        // 1. Validate request
        arcValidator.validate(request);

        // 2. Check premium limits
        if (!isPremium) {
            long existingCustomArcs = arcRepository.findAll().stream()
                    .filter(arc -> !arc.getPrebuilt())
                    .count();
            if (existingCustomArcs >= FREE_USER_MAX_CUSTOM_ARCS) {
                throw new BusinessException("CUSTOM_ARC_LIMIT",
                        "Free users are limited to " + FREE_USER_MAX_CUSTOM_ARCS + " custom arc. Upgrade to premium for unlimited.");
            }
        }

        // 3. Create Arc record
        Arc arc = Arc.builder()
                .name(request.getTitle())
                .description(request.getGoal())
                .type(ArcType.CUSTOM.name())
                .difficulty("MEDIUM")
                .durationDays(request.getDurationDays())
                .prebuilt(false)
                .build();

        Arc savedArc = arcRepository.save(arc);

        // 4. Create ArcMilestone records
        List<ArcMilestone> milestones = IntStream.range(0, request.getMilestones().size())
                .mapToObj(i -> ArcMilestone.builder()
                        .arc(savedArc)
                        .title(request.getMilestones().get(i))
                        .description(null)
                        .orderIndex(i + 1)
                        .xpReward(DEFAULT_MILESTONE_XP)
                        .build())
                .collect(Collectors.toList());

        List<ArcMilestone> savedMilestones = arcMilestoneRepository.saveAll(milestones);

        log.info("Custom arc created: arcId={}, milestones={}", savedArc.getId(), savedMilestones.size());

        // 5. Return created arc
        List<ArcDetailResponse.MilestoneResponse> milestoneResponses = savedMilestones.stream()
                .map(m -> ArcDetailResponse.MilestoneResponse.builder()
                        .id(m.getId())
                        .title(m.getTitle())
                        .description(m.getDescription())
                        .orderIndex(m.getOrderIndex())
                        .xpReward(m.getXpReward())
                        .build())
                .collect(Collectors.toList());

        return ArcDetailResponse.builder()
                .id(savedArc.getId())
                .name(savedArc.getName())
                .description(savedArc.getDescription())
                .type(ArcType.CUSTOM)
                .difficulty(savedArc.getDifficulty())
                .durationDays(savedArc.getDurationDays())
                .isPrebuilt(false)
                .milestoneCount(savedMilestones.size())
                .milestones(milestoneResponses)
                .questFrequency(request.getQuestFrequency())
                .build();
    }
}
