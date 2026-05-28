package com.ascend.quest.validator;

import com.ascend.common.entity.Difficulty;
import com.ascend.common.entity.Frequency;
import com.ascend.common.entity.StatType;
import com.ascend.quest.dto.CreateQuestRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for QuestValidator.
 * Verifies that invalid inputs are properly rejected with structured errors.
 */
class QuestValidatorTest {

    private QuestValidator validator;

    @BeforeEach
    void setUp() {
        validator = new QuestValidator();
    }

    @Nested
    @DisplayName("Title validation")
    class TitleValidation {

        @Test
        @DisplayName("Rejects null title")
        void rejectsNullTitle() {
            CreateQuestRequest request = new CreateQuestRequest(
                    null, "Description",
                    Difficulty.EASY, 50, StatType.FOCUS, Frequency.DAILY
            );

            List<QuestValidator.ValidationError> errors = validator.validate(request);

            assertThat(errors).anyMatch(e ->
                    e.field().equals("title") && e.message().contains("must not be blank"));
        }

        @Test
        @DisplayName("Rejects blank title")
        void rejectsBlankTitle() {
            CreateQuestRequest request = new CreateQuestRequest(
                    "   ", "Description",
                    Difficulty.EASY, 50, StatType.FOCUS, Frequency.DAILY
            );

            List<QuestValidator.ValidationError> errors = validator.validate(request);

            assertThat(errors).anyMatch(e ->
                    e.field().equals("title") && e.message().contains("must not be blank"));
        }

        @Test
        @DisplayName("Rejects empty title")
        void rejectsEmptyTitle() {
            CreateQuestRequest request = new CreateQuestRequest(
                    "", "Description",
                    Difficulty.EASY, 50, StatType.FOCUS, Frequency.DAILY
            );

            List<QuestValidator.ValidationError> errors = validator.validate(request);

            assertThat(errors).anyMatch(e ->
                    e.field().equals("title") && e.message().contains("must not be blank"));
        }

        @Test
        @DisplayName("Rejects title exceeding 100 characters")
        void rejectsTitleExceeding100Chars() {
            String longTitle = "A".repeat(101);
            CreateQuestRequest request = new CreateQuestRequest(
                    longTitle, "Description",
                    Difficulty.EASY, 50, StatType.FOCUS, Frequency.DAILY
            );

            List<QuestValidator.ValidationError> errors = validator.validate(request);

            assertThat(errors).anyMatch(e ->
                    e.field().equals("title") && e.message().contains("must not exceed 100 characters"));
        }

        @Test
        @DisplayName("Accepts valid title at boundary (100 chars)")
        void acceptsTitleAt100Chars() {
            String title = "A".repeat(100);
            CreateQuestRequest request = new CreateQuestRequest(
                    title, "Description",
                    Difficulty.EASY, 50, StatType.FOCUS, Frequency.DAILY
            );

            List<QuestValidator.ValidationError> errors = validator.validate(request);

            assertThat(errors).noneMatch(e -> e.field().equals("title"));
        }
    }

    @Nested
    @DisplayName("XP reward validation")
    class XpRewardValidation {

        @Test
        @DisplayName("Rejects null XP reward")
        void rejectsNullXpReward() {
            CreateQuestRequest request = new CreateQuestRequest(
                    "Valid Title", "Description",
                    Difficulty.EASY, null, StatType.FOCUS, Frequency.DAILY
            );

            List<QuestValidator.ValidationError> errors = validator.validate(request);

            assertThat(errors).anyMatch(e ->
                    e.field().equals("xpReward") && e.message().contains("required"));
        }

        @Test
        @DisplayName("Rejects XP reward below minimum (10)")
        void rejectsXpBelowMinimum() {
            CreateQuestRequest request = new CreateQuestRequest(
                    "Valid Title", "Description",
                    Difficulty.EASY, 9, StatType.FOCUS, Frequency.DAILY
            );

            List<QuestValidator.ValidationError> errors = validator.validate(request);

            assertThat(errors).anyMatch(e ->
                    e.field().equals("xpReward") && e.message().contains("between"));
        }

        @Test
        @DisplayName("Rejects XP reward above maximum (300)")
        void rejectsXpAboveMaximum() {
            CreateQuestRequest request = new CreateQuestRequest(
                    "Valid Title", "Description",
                    Difficulty.EASY, 301, StatType.FOCUS, Frequency.DAILY
            );

            List<QuestValidator.ValidationError> errors = validator.validate(request);

            assertThat(errors).anyMatch(e ->
                    e.field().equals("xpReward") && e.message().contains("between"));
        }

        @Test
        @DisplayName("Accepts XP reward at minimum boundary (10)")
        void acceptsXpAtMinimum() {
            CreateQuestRequest request = new CreateQuestRequest(
                    "Valid Title", "Description",
                    Difficulty.EASY, 10, StatType.FOCUS, Frequency.DAILY
            );

            List<QuestValidator.ValidationError> errors = validator.validate(request);

            assertThat(errors).noneMatch(e -> e.field().equals("xpReward"));
        }

        @Test
        @DisplayName("Accepts XP reward at maximum boundary (300)")
        void acceptsXpAtMaximum() {
            CreateQuestRequest request = new CreateQuestRequest(
                    "Valid Title", "Description",
                    Difficulty.EASY, 300, StatType.FOCUS, Frequency.DAILY
            );

            List<QuestValidator.ValidationError> errors = validator.validate(request);

            assertThat(errors).noneMatch(e -> e.field().equals("xpReward"));
        }
    }

    @Nested
    @DisplayName("Enum validation")
    class EnumValidation {

        @Test
        @DisplayName("Rejects null difficulty")
        void rejectsNullDifficulty() {
            CreateQuestRequest request = new CreateQuestRequest(
                    "Valid Title", "Description",
                    null, 50, StatType.FOCUS, Frequency.DAILY
            );

            List<QuestValidator.ValidationError> errors = validator.validate(request);

            assertThat(errors).anyMatch(e ->
                    e.field().equals("difficulty") && e.message().contains("required"));
        }

        @Test
        @DisplayName("Rejects null stat type")
        void rejectsNullStatType() {
            CreateQuestRequest request = new CreateQuestRequest(
                    "Valid Title", "Description",
                    Difficulty.EASY, 50, null, Frequency.DAILY
            );

            List<QuestValidator.ValidationError> errors = validator.validate(request);

            assertThat(errors).anyMatch(e ->
                    e.field().equals("statType") && e.message().contains("required"));
        }

        @Test
        @DisplayName("Rejects null frequency")
        void rejectsNullFrequency() {
            CreateQuestRequest request = new CreateQuestRequest(
                    "Valid Title", "Description",
                    Difficulty.EASY, 50, StatType.FOCUS, null
            );

            List<QuestValidator.ValidationError> errors = validator.validate(request);

            assertThat(errors).anyMatch(e ->
                    e.field().equals("frequency") && e.message().contains("required"));
        }
    }

    @Nested
    @DisplayName("Multiple validation errors")
    class MultipleErrors {

        @Test
        @DisplayName("Returns all errors for completely invalid request")
        void returnsAllErrorsForInvalidRequest() {
            CreateQuestRequest request = new CreateQuestRequest(
                    "", null, null, null, null, null
            );

            List<QuestValidator.ValidationError> errors = validator.validate(request);

            assertThat(errors).hasSizeGreaterThanOrEqualTo(5);
            assertThat(errors).anyMatch(e -> e.field().equals("title"));
            assertThat(errors).anyMatch(e -> e.field().equals("difficulty"));
            assertThat(errors).anyMatch(e -> e.field().equals("xpReward"));
            assertThat(errors).anyMatch(e -> e.field().equals("statType"));
            assertThat(errors).anyMatch(e -> e.field().equals("frequency"));
        }

        @Test
        @DisplayName("Rejects null request entirely")
        void rejectsNullRequest() {
            List<QuestValidator.ValidationError> errors = validator.validate(null);

            assertThat(errors).hasSize(1);
            assertThat(errors.get(0).field()).isEqualTo("request");
            assertThat(errors.get(0).message()).contains("must not be null");
        }
    }

    @Nested
    @DisplayName("Valid requests")
    class ValidRequests {

        @Test
        @DisplayName("Accepts fully valid request")
        void acceptsValidRequest() {
            CreateQuestRequest request = new CreateQuestRequest(
                    "Morning Run", "Run 5km",
                    Difficulty.MEDIUM, 50, StatType.VITALITY, Frequency.DAILY
            );

            List<QuestValidator.ValidationError> errors = validator.validate(request);

            assertThat(errors).isEmpty();
        }

        @Test
        @DisplayName("isValid returns true for valid request")
        void isValidReturnsTrueForValidRequest() {
            CreateQuestRequest request = new CreateQuestRequest(
                    "Study Session", "Study for 1 hour",
                    Difficulty.HARD, 150, StatType.WISDOM, Frequency.WEEKLY
            );

            assertThat(validator.isValid(request)).isTrue();
        }

        @Test
        @DisplayName("isValid returns false for invalid request")
        void isValidReturnsFalseForInvalidRequest() {
            CreateQuestRequest request = new CreateQuestRequest(
                    "", null, null, null, null, null
            );

            assertThat(validator.isValid(request)).isFalse();
        }
    }
}
