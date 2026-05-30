package com.ascend.quest.controller;

import com.ascend.auth.service.AuthService;
import com.ascend.common.entity.Difficulty;
import com.ascend.common.entity.Frequency;
import com.ascend.common.entity.StatType;
import com.ascend.quest.dto.DailyQuestsResponse;
import com.ascend.quest.dto.QuestCompletionResponse;
import com.ascend.quest.dto.QuestResponse;
import com.ascend.quest.exception.DuplicateCompletionException;
import com.ascend.quest.service.QuestCompletionService;
import com.ascend.quest.service.QuestService;
import com.ascend.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = QuestController.class, properties = "spring.security.enabled=false")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("QuestController")
class QuestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuestService questService;

    @MockBean
    private QuestCompletionService questCompletionService;

    @MockBean
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .firebaseUid("firebase-uid-123")
                .username("testuser")
                .level(5)
                .xp(1200L)
                .build();

        when(authService.getCurrentUser(any())).thenReturn(testUser);
    }

    @Nested
    @DisplayName("GET /api/v1/quests/daily")
    class GetDailyQuests {

        @Test
        @DisplayName("should return 200 with daily quests")
        void shouldReturn200WithDailyQuests() throws Exception {
            var response = DailyQuestsResponse.builder()
                    .date(LocalDate.of(2024, 1, 15))
                    .quests(Collections.emptyList())
                    .totalQuests(5)
                    .completedQuests(2)
                    .build();

            when(questService.getDailyQuests(testUser.getId())).thenReturn(response);

            mockMvc.perform(get("/api/v1/quests/daily"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalQuests").value(5))
                    .andExpect(jsonPath("$.data.completedQuests").value(2));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/quests/complete")
    class CompleteQuest {

        @Test
        @DisplayName("should return 200 on successful completion")
        void shouldReturn200OnSuccess() throws Exception {
            UUID questId = UUID.randomUUID();
            var completionResponse = QuestCompletionResponse.builder()
                    .questId(questId)
                    .questTitle("Run 5km")
                    .xpEarned(50)
                    .completedAt(LocalDateTime.now())
                    .message("Quest completed!")
                    .build();

            when(questCompletionService.completeQuest(eq(testUser.getId()), eq(questId)))
                    .thenReturn(completionResponse);

            String body = """
                    {"questId": "%s"}
                    """.formatted(questId);

            mockMvc.perform(post("/api/v1/quests/complete")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.xpEarned").value(50))
                    .andExpect(jsonPath("$.message").value("Quest completed!"));
        }

        @Test
        @DisplayName("should return 409 when quest already completed today")
        void shouldReturn409OnDuplicate() throws Exception {
            UUID questId = UUID.randomUUID();

            when(questCompletionService.completeQuest(eq(testUser.getId()), eq(questId)))
                    .thenThrow(new DuplicateCompletionException("Quest already completed today"));

            String body = """
                    {"questId": "%s"}
                    """.formatted(questId);

            mockMvc.perform(post("/api/v1/quests/complete")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Quest already completed today"));
        }

        @Test
        @DisplayName("should return 400 when questId is missing")
        void shouldReturn400WhenQuestIdMissing() throws Exception {
            mockMvc.perform(post("/api/v1/quests/complete")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/quests")
    class CreateCustomQuest {

        @Test
        @DisplayName("should return 201 on successful creation")
        void shouldReturn201OnSuccess() throws Exception {
            var questResponse = QuestResponse.builder()
                    .id(UUID.randomUUID())
                    .title("Meditate 10 min")
                    .difficulty(Difficulty.EASY)
                    .xpReward(25)
                    .statType(StatType.FOCUS)
                    .frequency(Frequency.DAILY)
                    .build();

            when(questService.createCustomQuest(eq(testUser.getId()), any()))
                    .thenReturn(questResponse);

            String body = """
                    {
                        "title": "Meditate 10 min",
                        "difficulty": "EASY",
                        "xpReward": 25,
                        "statType": "FOCUS",
                        "frequency": "DAILY"
                    }
                    """;

            mockMvc.perform(post("/api/v1/quests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.title").value("Meditate 10 min"))
                    .andExpect(jsonPath("$.data.xpReward").value(25));
        }

        @Test
        @DisplayName("should return 400 when title is blank")
        void shouldReturn400WhenTitleBlank() throws Exception {
            String body = """
                    {
                        "title": "",
                        "difficulty": "EASY",
                        "xpReward": 25,
                        "statType": "FOCUS",
                        "frequency": "DAILY"
                    }
                    """;

            mockMvc.perform(post("/api/v1/quests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when xpReward is below minimum (10)")
        void shouldReturn400WhenXpTooLow() throws Exception {
            String body = """
                    {
                        "title": "Easy quest",
                        "difficulty": "EASY",
                        "xpReward": 5,
                        "statType": "FOCUS",
                        "frequency": "DAILY"
                    }
                    """;

            mockMvc.perform(post("/api/v1/quests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when xpReward exceeds maximum (300)")
        void shouldReturn400WhenXpTooHigh() throws Exception {
            String body = """
                    {
                        "title": "OP quest",
                        "difficulty": "LEGENDARY",
                        "xpReward": 500,
                        "statType": "STRENGTH",
                        "frequency": "ONE_TIME"
                    }
                    """;

            mockMvc.perform(post("/api/v1/quests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when difficulty is missing")
        void shouldReturn400WhenDifficultyMissing() throws Exception {
            String body = """
                    {
                        "title": "No difficulty",
                        "xpReward": 50,
                        "statType": "WISDOM",
                        "frequency": "WEEKLY"
                    }
                    """;

            mockMvc.perform(post("/api/v1/quests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/quests/{id}")
    class GetQuestById {

        @Test
        @DisplayName("should return 200 with quest details")
        void shouldReturn200WithQuest() throws Exception {
            UUID questId = UUID.randomUUID();
            var questResponse = QuestResponse.builder()
                    .id(questId)
                    .title("Read 30 pages")
                    .difficulty(Difficulty.MEDIUM)
                    .xpReward(75)
                    .statType(StatType.WISDOM)
                    .frequency(Frequency.DAILY)
                    .build();

            when(questService.getQuestById(questId)).thenReturn(questResponse);

            mockMvc.perform(get("/api/v1/quests/{id}", questId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.title").value("Read 30 pages"))
                    .andExpect(jsonPath("$.data.xpReward").value(75));
        }
    }
}
