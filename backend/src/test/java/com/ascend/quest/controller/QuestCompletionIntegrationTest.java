package com.ascend.quest.controller;

import com.ascend.auth.config.DevAuthFilter;
import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.config.SecurityConfig;
import com.ascend.auth.service.AuthService;
import com.ascend.common.entity.Difficulty;
import com.ascend.common.entity.Frequency;
import com.ascend.common.entity.StatType;
import com.ascend.common.exception.GlobalExceptionHandler;
import com.ascend.quest.dto.CompleteQuestRequest;
import com.ascend.quest.dto.CreateQuestRequest;
import com.ascend.quest.dto.QuestCompletionResponse;
import com.ascend.quest.dto.QuestResponse;
import com.ascend.quest.exception.DuplicateCompletionException;
import com.ascend.quest.service.QuestCompletionService;
import com.ascend.quest.service.QuestService;
import com.ascend.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the quest completion flow via the REST API.
 * Verifies: create quest → complete quest → verify completion record exists,
 * and duplicate completion returns 409 Conflict.
 */
@WebMvcTest(
        controllers = QuestController.class,
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class),
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class, DevAuthFilter.class})
)
@AutoConfigureMockMvc(addFilters = false)
class QuestCompletionIntegrationTest {

    private static final String TEST_UID = "firebase-uid-quest-test";
    private static final String TEST_EMAIL = "questuser@example.com";
    private static final String QUEST_TITLE = "Morning Meditation";
    private static final String QUEST_DESCRIPTION = "Meditate for 10 minutes";
    private static final int QUEST_XP_REWARD = 25;
    private static final String COMPLETION_MESSAGE = "Quest completed! You earned 25 XP.";
    private static final String DUPLICATE_MESSAGE = "Quest 'Morning Meditation' has already been completed today";
    private static final LocalDateTime FIXED_COMPLETED_AT = LocalDateTime.of(2026, 5, 29, 10, 0, 0);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QuestService questService;

    @MockBean
    private QuestCompletionService questCompletionService;

    @MockBean
    private AuthService authService;

    /** Mocked to satisfy application context — rate limiting is not under test here. */
    @MockBean
    private StringRedisTemplate redisTemplate;

    private User testUser;
    private UUID userId;
    private UUID questId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        questId = UUID.randomUUID();
        testUser = User.builder()
                .id(userId)
                .firebaseUid(TEST_UID)
                .email(TEST_EMAIL)
                .username("questtester")
                .level(1)
                .xp(0L)
                .league("BRONZE")
                .premium(false)
                .hardMode(false)
                .guest(false)
                .timezone("UTC")
                .build();

        when(authService.getCurrentUser(TEST_UID)).thenReturn(testUser);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helper methods
    // ──────────────────────────────────────────────────────────────────────────

    private UsernamePasswordAuthenticationToken createAuth() {
        FirebasePrincipal principal = new FirebasePrincipal(
                TEST_UID, TEST_EMAIL, "google.com", Map.of());
        return new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor withPrincipal() {
        return request -> {
            org.springframework.security.core.context.SecurityContextHolder.getContext()
                    .setAuthentication(createAuth());
            return request;
        };
    }

    private QuestResponse buildQuestResponse(boolean completed) {
        return QuestResponse.builder()
                .id(questId)
                .title(QUEST_TITLE)
                .description(QUEST_DESCRIPTION)
                .xpReward(QUEST_XP_REWARD)
                .difficulty(Difficulty.EASY)
                .statType(StatType.FOCUS)
                .frequency(Frequency.DAILY)
                .recurring(true)
                .isCustom(true)
                .completed(completed)
                .build();
    }

    private QuestCompletionResponse buildCompletionResponse() {
        return QuestCompletionResponse.builder()
                .questId(questId)
                .questTitle(QUEST_TITLE)
                .xpEarned(QUEST_XP_REWARD)
                .completedAt(FIXED_COMPLETED_AT)
                .message(COMPLETION_MESSAGE)
                .build();
    }

    private CreateQuestRequest buildCreateQuestRequest() {
        return new CreateQuestRequest(
                QUEST_TITLE, QUEST_DESCRIPTION,
                Difficulty.EASY, QUEST_XP_REWARD, StatType.FOCUS, Frequency.DAILY
        );
    }

    private CompleteQuestRequest buildCompleteQuestRequest() {
        return new CompleteQuestRequest(questId);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Scenario tests
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Quest completion flow")
    class QuestCompletionFlow {

        @Test
        @DisplayName("Create quest → complete quest → verify completion record exists")
        void createQuest_completeQuest_verifyCompletionExists() throws Exception {
            // Step 1: Create a custom quest
            when(questService.createCustomQuest(eq(userId), any(CreateQuestRequest.class)))
                    .thenReturn(buildQuestResponse(false));

            mockMvc.perform(post("/api/v1/quests")
                            .with(withPrincipal())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildCreateQuestRequest())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(questId.toString()))
                    .andExpect(jsonPath("$.data.title").value(QUEST_TITLE));

            // Step 2: Complete the quest
            when(questCompletionService.completeQuest(userId, questId))
                    .thenReturn(buildCompletionResponse());

            mockMvc.perform(post("/api/v1/quests/complete")
                            .with(withPrincipal())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildCompleteQuestRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.questId").value(questId.toString()))
                    .andExpect(jsonPath("$.data.questTitle").value(QUEST_TITLE))
                    .andExpect(jsonPath("$.data.xpEarned").value(QUEST_XP_REWARD))
                    .andExpect(jsonPath("$.data.message").value(COMPLETION_MESSAGE));

            // Step 3: Verify the quest shows as completed
            when(questService.getQuestById(questId)).thenReturn(buildQuestResponse(true));

            mockMvc.perform(get("/api/v1/quests/" + questId)
                            .with(withPrincipal()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(questId.toString()))
                    .andExpect(jsonPath("$.data.title").value(QUEST_TITLE))
                    .andExpect(jsonPath("$.data.completed").value(true));
        }
    }

    @Nested
    @DisplayName("Idempotency enforcement")
    class IdempotencyEnforcement {

        @Test
        @DisplayName("Duplicate quest completion returns 409 Conflict")
        void duplicateCompletion_returns409Conflict() throws Exception {
            when(questCompletionService.completeQuest(userId, questId))
                    .thenReturn(buildCompletionResponse())
                    .thenThrow(new DuplicateCompletionException(DUPLICATE_MESSAGE));

            // First attempt succeeds
            mockMvc.perform(post("/api/v1/quests/complete")
                            .with(withPrincipal())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildCompleteQuestRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.xpEarned").value(QUEST_XP_REWARD));

            // Second attempt returns 409 Conflict
            mockMvc.perform(post("/api/v1/quests/complete")
                            .with(withPrincipal())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildCompleteQuestRequest())))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(DUPLICATE_MESSAGE));
        }
    }

    @Nested
    @DisplayName("Validation and error handling")
    class ValidationAndErrorHandling {

        @Test
        @DisplayName("Complete quest with null questId returns 400 Bad Request")
        void completeQuest_nullQuestId_returns400() throws Exception {
            mockMvc.perform(post("/api/v1/quests/complete")
                            .with(withPrincipal())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Create quest with blank title returns 400 Bad Request")
        void createQuest_blankTitle_returns400() throws Exception {
            CreateQuestRequest invalidRequest = new CreateQuestRequest(
                    "", QUEST_DESCRIPTION,
                    Difficulty.EASY, QUEST_XP_REWARD, StatType.FOCUS, Frequency.DAILY
            );

            mockMvc.perform(post("/api/v1/quests")
                            .with(withPrincipal())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}
