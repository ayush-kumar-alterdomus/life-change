package com.ascend.arc.validator;

import com.ascend.arc.dto.CreateArcRequest;
import com.ascend.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Validates custom arc creation requests beyond Jakarta Bean Validation annotations.
 * Enforces business rules such as milestone content validation and quest frequency values.
 */
@Slf4j
@Component
public class ArcValidator {

    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MIN_DURATION_DAYS = 30;
    private static final int MAX_DURATION_DAYS = 90;
    private static final int MAX_MILESTONES = 20;

    /**
     * Validates a CreateArcRequest for custom arc creation.
     * Throws BusinessException if validation fails.
     *
     * @param request the create arc request to validate
     */
    public void validate(CreateArcRequest request) {
        if (request == null) {
            throw new BusinessException("VALIDATION_ERROR", "Request must not be null");
        }

        validateTitle(request.getTitle());
        validateGoal(request.getGoal());
        validateDuration(request.getDurationDays());
        validateMilestones(request.getMilestones());
        validateQuestFrequency(request.getQuestFrequency());
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new BusinessException("VALIDATION_ERROR", "Title is required");
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new BusinessException("VALIDATION_ERROR",
                    "Title must not exceed " + MAX_TITLE_LENGTH + " characters");
        }
    }

    private void validateGoal(String goal) {
        if (goal == null || goal.isBlank()) {
            throw new BusinessException("VALIDATION_ERROR", "Goal is required");
        }
    }

    private void validateDuration(Integer durationDays) {
        if (durationDays == null) {
            throw new BusinessException("VALIDATION_ERROR", "Duration is required");
        }
        if (durationDays < MIN_DURATION_DAYS || durationDays > MAX_DURATION_DAYS) {
            throw new BusinessException("VALIDATION_ERROR",
                    "Duration must be between " + MIN_DURATION_DAYS + " and " + MAX_DURATION_DAYS + " days");
        }
    }

    private void validateMilestones(java.util.List<String> milestones) {
        if (milestones == null || milestones.isEmpty()) {
            throw new BusinessException("VALIDATION_ERROR", "At least one milestone is required");
        }
        if (milestones.size() > MAX_MILESTONES) {
            throw new BusinessException("VALIDATION_ERROR",
                    "Maximum " + MAX_MILESTONES + " milestones allowed");
        }
        for (int i = 0; i < milestones.size(); i++) {
            if (milestones.get(i) == null || milestones.get(i).isBlank()) {
                throw new BusinessException("VALIDATION_ERROR",
                        "Milestone at position " + (i + 1) + " must not be blank");
            }
        }
    }

    private void validateQuestFrequency(String questFrequency) {
        if (questFrequency == null || questFrequency.isBlank()) {
            throw new BusinessException("VALIDATION_ERROR", "Quest frequency is required");
        }
        // Valid frequencies: DAILY, EVERY_OTHER_DAY, WEEKLY
        String upper = questFrequency.toUpperCase();
        if (!upper.equals("DAILY") && !upper.equals("EVERY_OTHER_DAY") && !upper.equals("WEEKLY")) {
            throw new BusinessException("VALIDATION_ERROR",
                    "Quest frequency must be one of: DAILY, EVERY_OTHER_DAY, WEEKLY");
        }
    }
}
