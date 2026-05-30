package com.ascend.quest.service;

import com.ascend.common.entity.Difficulty;
import com.ascend.common.entity.Frequency;
import com.ascend.common.entity.StatType;
import com.ascend.common.exception.BusinessException;
import com.ascend.premium.service.PremiumService;
import com.ascend.quest.dto.UpdateQuestRequest;
import com.ascend.quest.dto.QuestResponse;
import com.ascend.quest.entity.Quest;
import com.ascend.quest.repository.QuestCompletionRepository;
import com.ascend.quest.repository.QuestRepository;
import com.ascend.quest.validator.QuestValidator;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuestService — Update & Delete")
class QuestServiceUpdateDeleteTest {

    @Mock private QuestRepository questRepository;
    @Mock private QuestCompletionRepository questCompletionRepository;
    @Mock private QuestValidator questValidator;
    @Mock private PremiumService premiumService;
    @Mock private UserRepository userRepository;

    private QuestService service;
    private UUID userId;
    private UUID questId;
    private User user;
    private Quest customQuest;

    @BeforeEach
    void setUp() {
        service = new QuestService(questRepository, questCompletionRepository, questValidator, premiumService, userRepository);
        userId = UUID.randomUUID();
        questId = UUID.randomUUID();
        user = User.builder().id(userId).username("alice").build();
        customQuest = Quest.builder()
                .id(questId)
                .title("Run 5km")
                .description("Morning run")
                .difficulty(Difficulty.MEDIUM)
                .xpReward(50)
                .statType(StatType.STRENGTH)
                .frequency(Frequency.DAILY)
                .recurring(true)
                .custom(true)
                .createdBy(user)
                .build();
    }

    @Nested
    @DisplayName("updateQuest()")
    class UpdateQuest {

        @Test
        @DisplayName("should update only provided fields")
        void shouldUpdatePartially() {
            when(questRepository.findById(questId)).thenReturn(Optional.of(customQuest));
            when(questRepository.save(any(Quest.class))).thenAnswer(inv -> inv.getArgument(0));

            var request = new UpdateQuestRequest();
            request.setTitle("Run 10km");
            request.setXpReward(100);

            QuestResponse result = service.updateQuest(userId, questId, request);

            assertThat(result.getTitle()).isEqualTo("Run 10km");
            assertThat(result.getXpReward()).isEqualTo(100);
            assertThat(result.getDifficulty()).isEqualTo(Difficulty.MEDIUM); // unchanged
            assertThat(result.getStatType()).isEqualTo(StatType.STRENGTH); // unchanged
        }

        @Test
        @DisplayName("should throw QUEST_NOT_FOUND when quest doesn't exist")
        void shouldThrowNotFound() {
            when(questRepository.findById(questId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateQuest(userId, questId, new UpdateQuestRequest()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "QUEST_NOT_FOUND");
        }

        @Test
        @DisplayName("should throw SYSTEM_QUEST when quest is not custom")
        void shouldThrowSystemQuest() {
            customQuest.setCustom(false);
            when(questRepository.findById(questId)).thenReturn(Optional.of(customQuest));

            assertThatThrownBy(() -> service.updateQuest(userId, questId, new UpdateQuestRequest()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "SYSTEM_QUEST");
        }

        @Test
        @DisplayName("should throw NOT_QUEST_OWNER when user is not the creator")
        void shouldThrowNotOwner() {
            User otherUser = User.builder().id(UUID.randomUUID()).build();
            customQuest.setCreatedBy(otherUser);
            when(questRepository.findById(questId)).thenReturn(Optional.of(customQuest));

            assertThatThrownBy(() -> service.updateQuest(userId, questId, new UpdateQuestRequest()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "NOT_QUEST_OWNER");
        }

        @Test
        @DisplayName("should throw VALIDATION_FAILED when title is blank")
        void shouldThrowValidationBlankTitle() {
            when(questRepository.findById(questId)).thenReturn(Optional.of(customQuest));

            var request = new UpdateQuestRequest();
            request.setTitle("   ");

            assertThatThrownBy(() -> service.updateQuest(userId, questId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "VALIDATION_FAILED");
        }

        @Test
        @DisplayName("should throw VALIDATION_FAILED when xpReward out of range")
        void shouldThrowValidationXp() {
            when(questRepository.findById(questId)).thenReturn(Optional.of(customQuest));

            var request = new UpdateQuestRequest();
            request.setXpReward(500);

            assertThatThrownBy(() -> service.updateQuest(userId, questId, request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "VALIDATION_FAILED");
        }

        @Test
        @DisplayName("should update frequency and recalculate recurring flag")
        void shouldUpdateFrequencyAndRecurring() {
            when(questRepository.findById(questId)).thenReturn(Optional.of(customQuest));
            when(questRepository.save(any(Quest.class))).thenAnswer(inv -> inv.getArgument(0));

            var request = new UpdateQuestRequest();
            request.setFrequency(Frequency.ONE_TIME);

            QuestResponse result = service.updateQuest(userId, questId, request);

            assertThat(result.isRecurring()).isFalse();
        }
    }

    @Nested
    @DisplayName("deleteQuest()")
    class DeleteQuest {

        @Test
        @DisplayName("should delete quest and completion history")
        void shouldDeleteSuccessfully() {
            when(questRepository.findById(questId)).thenReturn(Optional.of(customQuest));

            service.deleteQuest(userId, questId);

            verify(questCompletionRepository).deleteByQuestId(questId);
            verify(questRepository).delete(customQuest);
        }

        @Test
        @DisplayName("should throw QUEST_NOT_FOUND when quest doesn't exist")
        void shouldThrowNotFound() {
            when(questRepository.findById(questId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteQuest(userId, questId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "QUEST_NOT_FOUND");
        }

        @Test
        @DisplayName("should throw SYSTEM_QUEST when quest is not custom")
        void shouldThrowSystemQuest() {
            customQuest.setCustom(false);
            when(questRepository.findById(questId)).thenReturn(Optional.of(customQuest));

            assertThatThrownBy(() -> service.deleteQuest(userId, questId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "SYSTEM_QUEST");
        }

        @Test
        @DisplayName("should throw NOT_QUEST_OWNER when user is not the creator")
        void shouldThrowNotOwner() {
            User otherUser = User.builder().id(UUID.randomUUID()).build();
            customQuest.setCreatedBy(otherUser);
            when(questRepository.findById(questId)).thenReturn(Optional.of(customQuest));

            assertThatThrownBy(() -> service.deleteQuest(userId, questId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "NOT_QUEST_OWNER");
        }

        @Test
        @DisplayName("should throw ARC_LINKED_QUEST when quest has arcId")
        void shouldThrowArcLinked() {
            customQuest.setArcId(UUID.randomUUID());
            when(questRepository.findById(questId)).thenReturn(Optional.of(customQuest));

            assertThatThrownBy(() -> service.deleteQuest(userId, questId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "ARC_LINKED_QUEST");

            verify(questRepository, never()).delete(any());
        }
    }
}
