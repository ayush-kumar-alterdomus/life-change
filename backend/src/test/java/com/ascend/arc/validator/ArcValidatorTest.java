package com.ascend.arc.validator;

import com.ascend.arc.dto.CreateArcRequest;
import com.ascend.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Unit tests for ArcValidator.
 * Verifies that invalid custom arc requests are properly rejected.
 */
class ArcValidatorTest {

    private ArcValidator arcValidator;

    @BeforeEach
    void setUp() {
        arcValidator = new ArcValidator();
    }

    @Test
    @DisplayName("Valid request passes validation")
    void validate_validRequest_passes() {
        CreateArcRequest request = new CreateArcRequest(
                "My Custom Arc",
                "Become more disciplined",
                30,
                List.of("Week 1 goal", "Week 2 goal"),
                "DAILY"
        );

        assertThatCode(() -> arcValidator.validate(request))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Null request throws validation error")
    void validate_nullRequest_throwsException() {
        assertThatThrownBy(() -> arcValidator.validate(null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("must not be null");
    }

    @Test
    @DisplayName("Blank title throws validation error")
    void validate_blankTitle_throwsException() {
        CreateArcRequest request = new CreateArcRequest(
                "", "Goal", 30, List.of("Milestone"), "DAILY");

        assertThatThrownBy(() -> arcValidator.validate(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Title is required");
    }

    @Test
    @DisplayName("Title exceeding 100 chars throws validation error")
    void validate_longTitle_throwsException() {
        String longTitle = "A".repeat(101);
        CreateArcRequest request = new CreateArcRequest(
                longTitle, "Goal", 30, List.of("Milestone"), "DAILY");

        assertThatThrownBy(() -> arcValidator.validate(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("100 characters");
    }

    @Test
    @DisplayName("Blank goal throws validation error")
    void validate_blankGoal_throwsException() {
        CreateArcRequest request = new CreateArcRequest(
                "Title", "", 30, List.of("Milestone"), "DAILY");

        assertThatThrownBy(() -> arcValidator.validate(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Goal is required");
    }

    @Test
    @DisplayName("Duration below 30 days throws validation error")
    void validate_durationTooShort_throwsException() {
        CreateArcRequest request = new CreateArcRequest(
                "Title", "Goal", 29, List.of("Milestone"), "DAILY");

        assertThatThrownBy(() -> arcValidator.validate(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("between 30 and 90");
    }

    @Test
    @DisplayName("Duration above 90 days throws validation error")
    void validate_durationTooLong_throwsException() {
        CreateArcRequest request = new CreateArcRequest(
                "Title", "Goal", 91, List.of("Milestone"), "DAILY");

        assertThatThrownBy(() -> arcValidator.validate(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("between 30 and 90");
    }

    @Test
    @DisplayName("Empty milestones list throws validation error")
    void validate_emptyMilestones_throwsException() {
        CreateArcRequest request = new CreateArcRequest(
                "Title", "Goal", 30, Collections.emptyList(), "DAILY");

        assertThatThrownBy(() -> arcValidator.validate(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("At least one milestone");
    }

    @Test
    @DisplayName("Blank milestone entry throws validation error")
    void validate_blankMilestone_throwsException() {
        CreateArcRequest request = new CreateArcRequest(
                "Title", "Goal", 30, List.of("Valid", ""), "DAILY");

        assertThatThrownBy(() -> arcValidator.validate(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("must not be blank");
    }

    @Test
    @DisplayName("Invalid quest frequency throws validation error")
    void validate_invalidFrequency_throwsException() {
        CreateArcRequest request = new CreateArcRequest(
                "Title", "Goal", 30, List.of("Milestone"), "HOURLY");

        assertThatThrownBy(() -> arcValidator.validate(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("DAILY, EVERY_OTHER_DAY, WEEKLY");
    }

    @Test
    @DisplayName("Null quest frequency throws validation error")
    void validate_nullFrequency_throwsException() {
        CreateArcRequest request = new CreateArcRequest(
                "Title", "Goal", 30, List.of("Milestone"), null);

        assertThatThrownBy(() -> arcValidator.validate(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Quest frequency is required");
    }
}
