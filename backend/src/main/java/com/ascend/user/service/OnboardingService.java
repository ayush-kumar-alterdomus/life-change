package com.ascend.user.service;

import com.ascend.arc.dto.ArcProgressResponse;
import com.ascend.arc.service.ArcService;
import com.ascend.common.exception.BusinessException;
import com.ascend.user.dto.OnboardingRequest;
import com.ascend.user.dto.OnboardingResponse;
import com.ascend.user.dto.OnboardingStatusResponse;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final UserRepository userRepository;
    private final ArcService arcService;
    private final ObjectMapper objectMapper;

    @Transactional
    public OnboardingResponse completeOnboarding(UUID userId, OnboardingRequest request) {
        log.info("Completing onboarding for user={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));

        // Idempotency: reject if already complete
        if (Boolean.TRUE.equals(user.getOnboardingComplete())) {
            throw new BusinessException("ONBOARDING_ALREADY_COMPLETE", "Onboarding has already been completed");
        }

        // Persist all onboarding data
        user.setAvatarUrl(request.getSelectedAvatar());
        user.setHardMode("legendary".equalsIgnoreCase(request.getDifficulty())
                || "beast_mode".equalsIgnoreCase(request.getDifficulty()));
        user.setPersonalityType(request.getPersonalityType());
        user.setDifficultyPreference(request.getDifficulty());
        user.setOnboardingComplete(true);

        try {
            String goalsJson = objectMapper.writeValueAsString(request.getSelectedGoals());
            user.setSelectedGoals(goalsJson);
        } catch (Exception e) {
            log.warn("Failed to serialize goals for user={}: {}", userId, e.getMessage());
        }

        userRepository.save(user);

        // Attempt to start the selected arc (graceful — onboarding succeeds regardless)
        boolean arcStarted = false;
        String arcId = null;
        String arcName = null;

        try {
            UUID selectedArcId = resolveArcId(request.getSelectedArc());
            if (selectedArcId != null) {
                ArcProgressResponse arcProgress = arcService.startArc(userId, selectedArcId);
                arcStarted = true;
                arcId = arcProgress.getArcId().toString();
                arcName = arcProgress.getArcName();
                log.info("Arc started for user={}: arcId={}", userId, arcId);
            }
        } catch (Exception e) {
            log.warn("Failed to start arc for user={} during onboarding: {}", userId, e.getMessage());
        }

        log.info("Onboarding completed for user={}: goals={}, personality={}, difficulty={}, arcStarted={}",
                userId, request.getSelectedGoals(), request.getPersonalityType(),
                request.getDifficulty(), arcStarted);

        return new OnboardingResponse(user.getLevel(), arcStarted, arcId, arcName);
    }

    @Transactional(readOnly = true)
    public OnboardingStatusResponse getOnboardingStatus(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));

        List<String> goals = null;
        if (user.getSelectedGoals() != null) {
            try {
                goals = objectMapper.readValue(user.getSelectedGoals(), new TypeReference<>() {});
            } catch (Exception e) {
                log.warn("Failed to deserialize goals for user={}", userId);
            }
        }

        return new OnboardingStatusResponse(
                Boolean.TRUE.equals(user.getOnboardingComplete()),
                goals,
                user.getPersonalityType(),
                user.getDifficultyPreference()
        );
    }

    /**
     * Resolves an arc identifier (could be a UUID string or a slug like "warrior")
     * to a UUID. Returns null if unresolvable.
     */
    private UUID resolveArcId(String arcIdentifier) {
        if (arcIdentifier == null || arcIdentifier.isBlank()) return null;
        try {
            return UUID.fromString(arcIdentifier);
        } catch (IllegalArgumentException e) {
            // Not a UUID — treat as a slug/name, let ArcService handle lookup
            // For now, return null (arc start will be skipped)
            log.debug("Arc identifier '{}' is not a UUID, skipping arc start", arcIdentifier);
            return null;
        }
    }
}
