package com.ascend.common.exception;

import com.ascend.auth.service.FirebaseAuthenticationException;
import com.ascend.common.dto.ApiResponse;
import com.ascend.quest.exception.CustomQuestLimitException;
import com.ascend.quest.exception.DuplicateCompletionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Nested
    @DisplayName("handleFirebaseAuthException")
    class FirebaseAuth {

        @Test
        @DisplayName("should return 401 UNAUTHORIZED")
        void shouldReturn401() {
            var ex = new FirebaseAuthenticationException("Invalid token");
            ResponseEntity<ApiResponse<Void>> response = handler.handleFirebaseAuthException(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("should include exception message in response body")
        void shouldIncludeMessage() {
            var ex = new FirebaseAuthenticationException("Token expired");
            ResponseEntity<ApiResponse<Void>> response = handler.handleFirebaseAuthException(ex);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isFalse();
            assertThat(response.getBody().getMessage()).isEqualTo("Token expired");
        }
    }

    @Nested
    @DisplayName("handleDuplicateCompletionException")
    class DuplicateCompletion {

        @Test
        @DisplayName("should return 409 CONFLICT")
        void shouldReturn409() {
            var ex = new DuplicateCompletionException("Quest already completed today");
            ResponseEntity<ApiResponse<Void>> response = handler.handleDuplicateCompletionException(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }

        @Test
        @DisplayName("should include exception message")
        void shouldIncludeMessage() {
            var ex = new DuplicateCompletionException("Already done");
            ResponseEntity<ApiResponse<Void>> response = handler.handleDuplicateCompletionException(ex);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isFalse();
            assertThat(response.getBody().getMessage()).isEqualTo("Already done");
        }
    }

    @Nested
    @DisplayName("handleCustomQuestLimitException")
    class CustomQuestLimit {

        @Test
        @DisplayName("should return 403 FORBIDDEN")
        void shouldReturn403() {
            var ex = new CustomQuestLimitException("Max 10 custom quests allowed");
            ResponseEntity<ApiResponse<Void>> response = handler.handleCustomQuestLimitException(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("should include exception message")
        void shouldIncludeMessage() {
            var ex = new CustomQuestLimitException("Limit reached");
            ResponseEntity<ApiResponse<Void>> response = handler.handleCustomQuestLimitException(ex);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("Limit reached");
        }
    }

    @Nested
    @DisplayName("handleBusinessException")
    class Business {

        @Test
        @DisplayName("should return 400 BAD_REQUEST")
        void shouldReturn400() {
            var ex = new BusinessException("Invalid operation");
            ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessException(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("should include exception message")
        void shouldIncludeMessage() {
            var ex = new BusinessException("INSUFFICIENT_XP", "Not enough XP to level up");
            ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessException(ex);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("Not enough XP to level up");
        }
    }

    @Nested
    @DisplayName("handleValidationException")
    class Validation {

        @Test
        @DisplayName("should return 400 BAD_REQUEST with field errors")
        void shouldReturn400WithFieldErrors() {
            var bindingResult = new BeanPropertyBindingResult(new Object(), "request");
            bindingResult.addError(new FieldError("request", "title", "must not be blank"));
            bindingResult.addError(new FieldError("request", "xpReward", "must be at least 10"));

            var ex = new MethodArgumentNotValidException(
                    null, bindingResult);

            ResponseEntity<ApiResponse<Void>> response = handler.handleValidationException(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().isSuccess()).isFalse();
            assertThat(response.getBody().getMessage()).contains("title: must not be blank");
            assertThat(response.getBody().getMessage()).contains("xpReward: must be at least 10");
        }
    }

    @Nested
    @DisplayName("handleNotFoundException")
    class NotFound {

        @Test
        @DisplayName("should return 404 NOT_FOUND")
        void shouldReturn404() throws Exception {
            var ex = new NoResourceFoundException(org.springframework.http.HttpMethod.GET, "/api/v1/quests/999");
            ResponseEntity<ApiResponse<Void>> response = handler.handleNotFoundException(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("Resource not found");
        }
    }

    @Nested
    @DisplayName("handleGenericException")
    class Generic {

        @Test
        @DisplayName("should return 500 INTERNAL_SERVER_ERROR")
        void shouldReturn500() {
            var ex = new RuntimeException("Unexpected NPE");
            ResponseEntity<ApiResponse<Void>> response = handler.handleGenericException(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Test
        @DisplayName("should NOT leak internal exception message to client")
        void shouldNotLeakInternalMessage() {
            var ex = new RuntimeException("SQL connection pool exhausted at jdbc:postgresql://...");
            ResponseEntity<ApiResponse<Void>> response = handler.handleGenericException(ex);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
            assertThat(response.getBody().getMessage()).doesNotContain("SQL");
            assertThat(response.getBody().getMessage()).doesNotContain("postgresql");
        }

        @Test
        @DisplayName("should set success to false")
        void shouldSetSuccessFalse() {
            var ex = new Exception("anything");
            ResponseEntity<ApiResponse<Void>> response = handler.handleGenericException(ex);

            assertThat(response.getBody().isSuccess()).isFalse();
        }
    }
}
