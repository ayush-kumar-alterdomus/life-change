package com.ascend.quest.controller;

import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.service.AuthService;
import com.ascend.common.entity.Difficulty;
import com.ascend.common.entity.Frequency;
import com.ascend.common.entity.StatType;
import com.ascend.quest.dto.QuestResponse;
import com.ascend.quest.dto.UpdateQuestRequest;
import com.ascend.quest.service.QuestCompletionService;
import com.ascend.quest.service.QuestService;
import com.ascend.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuestController — Update & Delete")
class QuestControllerUpdateDeleteTest {

    @Mock private QuestService questService;
    @Mock private QuestCompletionService questCompletionService;
    @Mock private AuthService authService;

    private QuestController controller;
    private User user;
    private FirebasePrincipal principal;
    private UUID questId;

    @BeforeEach
    void setUp() {
        controller = new QuestController(questService, questCompletionService, authService);
        user = User.builder().id(UUID.randomUUID()).username("alice").build();
        principal = new FirebasePrincipal("uid-123", "alice@test.com", "password", Map.of());
        questId = UUID.randomUUID();
        when(authService.getCurrentUser("uid-123")).thenReturn(user);
    }

    @Test
    @DisplayName("PUT /quests/{id} should return 200 with updated quest")
    void updateQuest_shouldReturn200() {
        var request = new UpdateQuestRequest();
        request.setTitle("Updated Title");

        var response = QuestResponse.builder()
                .id(questId).title("Updated Title").difficulty(Difficulty.MEDIUM)
                .xpReward(50).statType(StatType.STRENGTH).frequency(Frequency.DAILY)
                .recurring(true).isCustom(true).completed(false).build();

        when(questService.updateQuest(eq(user.getId()), eq(questId), any())).thenReturn(response);

        var result = controller.updateQuest(principal, questId, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getData().getTitle()).isEqualTo("Updated Title");
    }

    @Test
    @DisplayName("DELETE /quests/{id} should return 204")
    void deleteQuest_shouldReturn204() {
        var result = controller.deleteQuest(principal, questId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(questService).deleteQuest(user.getId(), questId);
    }
}
