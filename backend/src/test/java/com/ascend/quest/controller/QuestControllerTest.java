package com.ascend.quest.controller;

import com.ascend.auth.service.AuthService;
import com.ascend.common.dto.ApiResponse;
import com.ascend.common.entity.Difficulty;
import com.ascend.common.entity.Frequency;
import com.ascend.common.entity.StatType;
import com.ascend.quest.dto.CompleteQuestRequest;
import com.ascend.quest.dto.CreateQuestRequest;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuestController")
class QuestControllerTest {

    @Mock
    private QuestService questService;

    @Mock
    private QuestCompletionService questCompletionService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private QuestController controller;

    private User testUser;
    private final String firebaseUid = "firebase-uid-123";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .firebaseUid(firebaseUid)
                .username("testuser")
                .level(5)
                .xp(1200L)
                .build();

        when(authService.getCurrentUser(any())).thenReturn(testUser);
    }

    @Nested
    @DisplayName("getDailyQuests")
    class GetDailyQuests {

        @Test
        @DisplayName("should return 200 with daily quests")
        void shouldReturn200WithDailyQuests() {
            var response = DailyQuestsResponse.builder()
                    .date(LocalDate.of(2024, 1, 15))
                    .quests(Collections.emptyList())
                    .totalQuests(5)
                    .completedQuests(2)
                    .build();

            when(questService.getDailyQuests(testUser.getId())).thenReturn(response);

            var principal = new com.ascend.auth.config.FirebasePrincipal(firebaseUid, "test@example.com", "password", java.util.Map.of());
            ResponseEntity<ApiResponse<DailyQuestsResponse>> result = controller.getDailyQuests(principal);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().isSuccess()).isTrue();
            assertThat(result.getBody().getData().getTotalQuests()).isEqualTo(5);
            assertThat(result.getBody().getData().getCompletedQuests()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("completeQuest")
    class CompleteQuest {

        @Test
        @DisplayName("should return 200 on successful completion")
        void shouldReturn200OnSuccess() {
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

            var principal = new com.ascend.auth.config.FirebasePrincipal(firebaseUid, "test@example.com", "password", java.util.Map.of());
            var request = new CompleteQuestRequest(questId);
            ResponseEntity<ApiResponse<QuestCompletionResponse>> result = controller.completeQuest(principal, request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody().isSuccess()).isTrue();
            assertThat(result.getBody().getData().getXpEarned()).isEqualTo(50);
        }

        @Test
        @DisplayName("should propagate DuplicateCompletionException")
        void shouldThrowOnDuplicate() {
            UUID questId = UUID.randomUUID();

            when(questCompletionService.completeQuest(eq(testUser.getId()), eq(questId)))
                    .thenThrow(new DuplicateCompletionException("Quest already completed today"));

            var principal = new com.ascend.auth.config.FirebasePrincipal(firebaseUid, "test@example.com", "password", java.util.Map.of());
            var request = new CompleteQuestRequest(questId);

            assertThatThrownBy(() -> controller.completeQuest(principal, request))
                    .isInstanceOf(DuplicateCompletionException.class)
                    .hasMessage("Quest already completed today");
        }
    }

    @Nested
    @DisplayName("createCustomQuest")
    class CreateCustomQuest {

        @Test
        @DisplayName("should return 201 on successful creation")
        void shouldReturn201OnSuccess() {
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

            var principal = new com.ascend.auth.config.FirebasePrincipal(firebaseUid, "test@example.com", "password", java.util.Map.of());
            var request = new CreateQuestRequest("Meditate 10 min", null, Difficulty.EASY, 25, StatType.FOCUS, Frequency.DAILY);
            ResponseEntity<ApiResponse<QuestResponse>> result = controller.createCustomQuest(principal, request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(result.getBody().isSuccess()).isTrue();
            assertThat(result.getBody().getData().getTitle()).isEqualTo("Meditate 10 min");
            assertThat(result.getBody().getData().getXpReward()).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("getQuestById")
    class GetQuestById {

        @Test
        @DisplayName("should return 200 with quest details")
        void shouldReturn200WithQuest() {
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

            var principal = new com.ascend.auth.config.FirebasePrincipal(firebaseUid, "test@example.com", "password", java.util.Map.of());
            ResponseEntity<ApiResponse<QuestResponse>> result = controller.getQuestById(principal, questId);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody().isSuccess()).isTrue();
            assertThat(result.getBody().getData().getTitle()).isEqualTo("Read 30 pages");
            assertThat(result.getBody().getData().getXpReward()).isEqualTo(75);
        }
    }
}
