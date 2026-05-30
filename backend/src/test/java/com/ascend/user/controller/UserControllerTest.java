package com.ascend.user.controller;

import com.ascend.auth.service.AuthService;
import com.ascend.common.exception.BusinessException;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class, properties = "spring.security.enabled=false")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UserController")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .firebaseUid("firebase-uid-456")
                .username("newuser")
                .level(1)
                .xp(0L)
                .build();

        when(authService.getCurrentUser(any())).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
    }

    @Nested
    @DisplayName("PUT /api/v1/users/onboarding")
    class CompleteOnboarding {

        @Test
        @DisplayName("should return 200 on valid onboarding request")
        void shouldReturn200OnValidRequest() throws Exception {
            String body = """
                    {
                        "selectedGoals": ["fitness", "mindfulness"],
                        "difficulty": "balanced",
                        "personalityType": "disciplined",
                        "selectedArc": "monk",
                        "selectedAvatar": "phoenix"
                    }
                    """;

            mockMvc.perform(put("/api/v1/users/onboarding")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Onboarding completed"));
        }

        @Test
        @DisplayName("should save user with avatar and hardMode flag")
        void shouldSaveUserWithCorrectFields() throws Exception {
            String body = """
                    {
                        "selectedGoals": ["fitness"],
                        "difficulty": "legendary",
                        "personalityType": "competitive",
                        "selectedArc": "warrior",
                        "selectedAvatar": "dragon"
                    }
                    """;

            mockMvc.perform(put("/api/v1/users/onboarding")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk());

            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("should return 400 when selectedGoals is empty")
        void shouldReturn400WhenGoalsEmpty() throws Exception {
            String body = """
                    {
                        "selectedGoals": [],
                        "difficulty": "balanced",
                        "personalityType": "disciplined",
                        "selectedArc": "monk",
                        "selectedAvatar": "phoenix"
                    }
                    """;

            mockMvc.perform(put("/api/v1/users/onboarding")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when difficulty is blank")
        void shouldReturn400WhenDifficultyBlank() throws Exception {
            String body = """
                    {
                        "selectedGoals": ["fitness"],
                        "difficulty": "",
                        "personalityType": "disciplined",
                        "selectedArc": "monk",
                        "selectedAvatar": "phoenix"
                    }
                    """;

            mockMvc.perform(put("/api/v1/users/onboarding")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when selectedArc is missing")
        void shouldReturn400WhenArcMissing() throws Exception {
            String body = """
                    {
                        "selectedGoals": ["fitness"],
                        "difficulty": "balanced",
                        "personalityType": "disciplined",
                        "selectedAvatar": "phoenix"
                    }
                    """;

            mockMvc.perform(put("/api/v1/users/onboarding")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when selectedAvatar is blank")
        void shouldReturn400WhenAvatarBlank() throws Exception {
            String body = """
                    {
                        "selectedGoals": ["fitness"],
                        "difficulty": "balanced",
                        "personalityType": "disciplined",
                        "selectedArc": "monk",
                        "selectedAvatar": ""
                    }
                    """;

            mockMvc.perform(put("/api/v1/users/onboarding")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when user not found for firebase uid")
        void shouldReturn400WhenUserNotFound() throws Exception {
            when(authService.getCurrentUser("firebase-uid-456"))
                    .thenThrow(new BusinessException("User not found for the authenticated account"));

            String body = """
                    {
                        "selectedGoals": ["fitness"],
                        "difficulty": "balanced",
                        "personalityType": "disciplined",
                        "selectedArc": "monk",
                        "selectedAvatar": "phoenix"
                    }
                    """;

            mockMvc.perform(put("/api/v1/users/onboarding")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("User not found for the authenticated account"));
        }
    }
}
