package com.ascend.quest.controller;

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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
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
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
@AutoConfigureMockMvc(addFilters = false)
class QuestCompletionIntegrationTest {

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

    @MockBean
    private StringRedisTemplate redisTemplate;

    private static final String TEST_UID = "firebase-uid-quest-test";
    private static final String TEST_EMAIL = "questuser@example.com";

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

    private UsernamePasswordAuthenticationToken createAuth() {
        FirebasePrincipal principal = new FirebasePrincipal(
                TEST_UID, TEST_EMAIL, "google.com", Map.of());
        return new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    @DisplayName("Create quest → complete quest → verify completion record exists")
    void createQuest_completeQuest_verifyCompletionExists() throws Exception {
        // Step 1: Create a custom quest
        CreateQuestRequest createRequest = new CreateQuestRequest(
                "Morning Meditation", "Meditate for 10 minutes",
                Difficulty.EASY, 25, StatType.FOCUS, Frequency.DAILY
        );

        QuestResponse createdQuest = QuestResponse.builder()
                .id(questId)
                .title("Morning Meditation")
                .description("Meditate for 10 minutes")
                .xpReward(25)
                .difficulty(Difficulty.EASY)
                .statType(StatType.FOCUS)
                .frequency(Frequency.DAILY)
                .recurring(true)
                .isCustom(true)
                .completed(false)
                .build();

        when(questService.createCustomQuest(eq(userId), any(CreateQuestRequest.class)))
                .thenReturn(createdQuest);

        mockMvc.perform(post("/api/v1/quests")
                        .with(authentication(createAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(questId.toString()))
                .andExpect(jsonPath("$.data.title").value("Morning Meditation"));

        // Step 2: Complete the quest
        CompleteQuestRequest completeRequest = new CompleteQuestRequest(questId);
        LocalDateTime completedAt = LocalDateTime.now();

        QuestCompletionResponse completionResponse = QuestCompletionResponse.builder()
                .questId(questId)
                .questTitle("Morning Meditation")
                .xpEarned(25)
                .completedAt(completedAt)
                .message("Quest completed! You earned 25 XP.")
                .build();

        when(questCompletionService.completeQuest(userId, questId))
                .thenReturn(completionResponse);

        mockMvc.perform(post("/api/v1/quests/complete")
                        .with(authentication(createAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.questId").value(questId.toString()))
                .andExpect(jsonPath("$.data.questTitle").value("Morning Meditation"))
                .andExpect(jsonPath("$.data.xpEarned").value(25))
                .andExpect(jsonPath("$.data.message").value("Quest completed! You earned 25 XP."));

        // Step 3: Verify the quest shows as completed when fetching quest details
        QuestResponse completedQuestResponse = QuestResponse.builder()
                .id(questId)
                .title("Morning Meditation")
                .description("Meditate for 10 minutes")
                .xpReward(25)
                .difficulty(Difficulty.EASY)
                .statType(StatType.FOCUS)
                .frequency(Frequency.DAILY)
                .recurring(true)
                .isCustom(true)
                .completed(true)
                .build();

        when(questService.getQuestById(questId)).thenReturn(completedQuestResponse);

        mockMvc.perform(get("/api/v1/quests/" + questId)
                        .with(authentication(createAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(questId.toString()))
                .andExpect(jsonPath("$.data.title").value("Morning Meditation"))
                .andExpect(jsonPath("$.data.completed").value(true));
    }

    @Test
    @DisplayName("Duplicate quest completion returns 409 Conflict")
    void duplicateCompletion_returns409Conflict() throws Exception {
        // First completion succeeds
        CompleteQuestRequest completeRequest = new CompleteQuestRequest(questId);

        QuestCompletionResponse completionResponse = QuestCompletionResponse.builder()
                .questId(questId)
                .questTitle("Morning Meditation")
                .xpEarned(25)
                .completedAt(LocalDateTime.now())
                .message("Quest completed! You earned 25 XP.")
                .build();

        when(questCompletionService.completeQuest(userId, questId))
                .thenReturn(completionResponse)
                .thenThrow(new DuplicateCompletionException(
                        "Quest 'Morning Meditation' has already been completed today"));

        // First attempt succeeds
        mockMvc.perform(post("/api/v1/quests/complete")
                        .with(authentication(createAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.xpEarned").value(25));

        // Second attempt returns 409 Conflict
        mockMvc.perform(post("/api/v1/quests/complete")
                        .with(authentication(createAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(
                        "Quest 'Morning Meditation' has already been completed today"));
    }
}
