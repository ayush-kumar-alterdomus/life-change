package com.ascend.common.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ApiResponse")
class ApiResponseTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Nested
    @DisplayName("success(T data)")
    class SuccessWithData {

        @Test
        @DisplayName("should set success to true")
        void shouldSetSuccessTrue() {
            ApiResponse<String> response = ApiResponse.success("hello");
            assertThat(response.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("should include data in response")
        void shouldIncludeData() {
            ApiResponse<Integer> response = ApiResponse.success(42);
            assertThat(response.getData()).isEqualTo(42);
        }

        @Test
        @DisplayName("should have null message when not provided")
        void shouldHaveNullMessage() {
            ApiResponse<String> response = ApiResponse.success("data");
            assertThat(response.getMessage()).isNull();
        }

        @Test
        @DisplayName("should set timestamp automatically")
        void shouldSetTimestamp() {
            ApiResponse<String> response = ApiResponse.success("data");
            assertThat(response.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("should handle null data")
        void shouldHandleNullData() {
            ApiResponse<Object> response = ApiResponse.success((Object) null);
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getData()).isNull();
        }
    }

    @Nested
    @DisplayName("success(String message)")
    class SuccessWithMessage {

        @Test
        @DisplayName("should set success to true with message only")
        void shouldSetSuccessTrue() {
            ApiResponse<Void> response = ApiResponse.success("Guild created!");
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("Guild created!");
        }

        @Test
        @DisplayName("should have null data")
        void shouldHaveNullData() {
            ApiResponse<Void> response = ApiResponse.success("Done");
            assertThat(response.getData()).isNull();
        }
    }

    @Nested
    @DisplayName("success(String message, T data)")
    class SuccessWithMessageAndData {

        @Test
        @DisplayName("should include both message and data")
        void shouldIncludeBoth() {
            ApiResponse<String> response = ApiResponse.success("Created", "item-123");
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("Created");
            assertThat(response.getData()).isEqualTo("item-123");
        }
    }

    @Nested
    @DisplayName("error(String message)")
    class Error {

        @Test
        @DisplayName("should set success to false")
        void shouldSetSuccessFalse() {
            ApiResponse<Object> response = ApiResponse.error("Something went wrong");
            assertThat(response.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("should include error message")
        void shouldIncludeMessage() {
            ApiResponse<Object> response = ApiResponse.error("Not found");
            assertThat(response.getMessage()).isEqualTo("Not found");
        }

        @Test
        @DisplayName("should have null data")
        void shouldHaveNullData() {
            ApiResponse<Object> response = ApiResponse.error("Error");
            assertThat(response.getData()).isNull();
        }

        @Test
        @DisplayName("should set timestamp automatically")
        void shouldSetTimestamp() {
            ApiResponse<Object> response = ApiResponse.error("Error");
            assertThat(response.getTimestamp()).isNotNull();
        }
    }

    @Nested
    @DisplayName("JSON serialization")
    class JsonSerialization {

        @Test
        @DisplayName("should exclude null fields from JSON output")
        void shouldExcludeNullFields() throws Exception {
            ApiResponse<Void> response = ApiResponse.success("OK");
            String json = objectMapper.writeValueAsString(response);

            assertThat(json).contains("\"success\":true");
            assertThat(json).contains("\"message\":\"OK\"");
            assertThat(json).doesNotContain("\"data\"");
        }

        @Test
        @DisplayName("should maintain field order: success, message, data, timestamp")
        void shouldMaintainFieldOrder() throws Exception {
            ApiResponse<String> response = ApiResponse.success("msg", "payload");
            String json = objectMapper.writeValueAsString(response);

            int successIdx = json.indexOf("\"success\"");
            int messageIdx = json.indexOf("\"message\"");
            int dataIdx = json.indexOf("\"data\"");
            int timestampIdx = json.indexOf("\"timestamp\"");

            assertThat(successIdx).isLessThan(messageIdx);
            assertThat(messageIdx).isLessThan(dataIdx);
            assertThat(dataIdx).isLessThan(timestampIdx);
        }
    }
}
