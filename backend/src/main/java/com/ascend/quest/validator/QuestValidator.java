package com.ascend.quest.validator;

import com.ascend.common.entity.Difficulty;
import com.ascend.common.entity.Frequency;
import com.ascend.common.entity.StatType;
import com.ascend.quest.dto.CreateQuestRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Validates custom quest creation requests beyond Jakarta Bean Validation.
 * Provides structured validation errors for business rule enforcement.
 */
@Component
public class QuestValidator {

    private static final int TITLE_MAX_LENGTH = 100;
    private static final int XP_MIN = 10;
    private static final int XP_MAX = 300;

    /**
     * Validates a CreateQuestRequest and returns a list of validation errors.
     * Returns an empty list if the request is valid.
     *
     * @param request the quest creation request to validate
     * @return list of validation error messages (empty if valid)
     */
    public List<ValidationError> validate(CreateQuestRequest request) {
        if (request == null) {
            return Collections.singletonList(
                    new ValidationError("request", "Quest request must not be null"));
        }

        List<ValidationError> errors = new ArrayList<>();

        validateTitle(request.getTitle(), errors);
        validateDifficulty(request.getDifficulty(), errors);
        validateXpReward(request.getXpReward(), errors);
        validateStatType(request.getStatType(), errors);
        validateFrequency(request.getFrequency(), errors);

        return errors;
    }

    /**
     * Checks if the request is valid (no validation errors).
     */
    public boolean isValid(CreateQuestRequest request) {
        return validate(request).isEmpty();
    }

    private void validateTitle(String title, List<ValidationError> errors) {
        if (title == null || title.isBlank()) {
            errors.add(new ValidationError("title", "Title must not be blank"));
        } else if (title.length() > TITLE_MAX_LENGTH) {
            errors.add(new ValidationError("title",
                    "Title must not exceed " + TITLE_MAX_LENGTH + " characters"));
        }
    }

    private void validateDifficulty(Difficulty difficulty, List<ValidationError> errors) {
        if (difficulty == null) {
            errors.add(new ValidationError("difficulty",
                    "Difficulty is required and must be one of: EASY, MEDIUM, HARD, LEGENDARY"));
        }
    }

    private void validateXpReward(Integer xpReward, List<ValidationError> errors) {
        if (xpReward == null) {
            errors.add(new ValidationError("xpReward", "XP reward is required"));
        } else if (xpReward < XP_MIN || xpReward > XP_MAX) {
            errors.add(new ValidationError("xpReward",
                    "XP reward must be between " + XP_MIN + " and " + XP_MAX));
        }
    }

    private void validateStatType(StatType statType, List<ValidationError> errors) {
        if (statType == null) {
            errors.add(new ValidationError("statType",
                    "Stat type is required and must be one of: STRENGTH, WISDOM, FOCUS, DISCIPLINE, VITALITY, CHARISMA"));
        }
    }

    private void validateFrequency(Frequency frequency, List<ValidationError> errors) {
        if (frequency == null) {
            errors.add(new ValidationError("frequency",
                    "Frequency is required and must be one of: DAILY, WEEKLY, MONTHLY, ONE_TIME"));
        }
    }

    /**
     * Structured validation error with field name and message.
     */
    public record ValidationError(String field, String message) {
    }
}
