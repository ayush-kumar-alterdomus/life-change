package com.ascend.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BusinessException")
class BusinessExceptionTest {

    @Test
    @DisplayName("should default errorCode to BUSINESS_ERROR when only message provided")
    void shouldDefaultErrorCode() {
        var ex = new BusinessException("Something failed");

        assertThat(ex.getErrorCode()).isEqualTo("BUSINESS_ERROR");
        assertThat(ex.getMessage()).isEqualTo("Something failed");
    }

    @Test
    @DisplayName("should use custom errorCode when provided")
    void shouldUseCustomErrorCode() {
        var ex = new BusinessException("QUEST_NOT_FOUND", "Quest does not exist");

        assertThat(ex.getErrorCode()).isEqualTo("QUEST_NOT_FOUND");
        assertThat(ex.getMessage()).isEqualTo("Quest does not exist");
    }

    @Test
    @DisplayName("should preserve cause when provided")
    void shouldPreserveCause() {
        var cause = new IllegalStateException("root cause");
        var ex = new BusinessException("DB_ERROR", "Database failure", cause);

        assertThat(ex.getErrorCode()).isEqualTo("DB_ERROR");
        assertThat(ex.getMessage()).isEqualTo("Database failure");
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("should be a RuntimeException (unchecked)")
    void shouldBeRuntimeException() {
        var ex = new BusinessException("test");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("should be throwable and catchable")
    void shouldBeThrowable() {
        try {
            throw new BusinessException("XP_OVERFLOW", "XP exceeds maximum");
        } catch (BusinessException ex) {
            assertThat(ex.getErrorCode()).isEqualTo("XP_OVERFLOW");
            assertThat(ex.getMessage()).isEqualTo("XP exceeds maximum");
        }
    }
}
