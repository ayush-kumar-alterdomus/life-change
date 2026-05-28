package com.ascend.quest.service;

import com.ascend.common.entity.Difficulty;
import com.ascend.common.entity.Frequency;
import com.ascend.common.entity.StatType;
import com.ascend.common.exception.BusinessException;
import com.ascend.quest.dto.QuestCompletionResponse;
import com.ascend.quest.entity.Quest;
import com.ascend.quest.entity.QuestCompletion;
import com.ascend.quest.event.QuestCompletedEvent;
import com.ascend.quest.exception.DuplicateCompletionException;
import com.ascend.quest.repository.QuestCompletionRepository;
import com.ascend.quest.repository.QuestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for QuestCompletionService.
 * Verifies that QuestCompletedEvent is published on successful completion,
 * and that duplicate completions are properly rejected.
 */
@ExtendWith(MockitoExtension.class)
class QuestCompletionServiceTest {

    @Mock
    private QuestRepository questRepository;

    @Mock
    private QuestCompletionRepository questCompletionRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private QuestCompletionService questCompletionService;

    private UUID userId;
    private UUID questId;
    private Quest testQuest;

    @BeforeEach
    void setUp() {
        questCompletionService = new QuestCompletionService(
                questRepository, questCompletionRepository, eventPublisher);

        userId = UUID.randomUUID();
        questId = UUID.randomUUID();
        testQuest = Quest.builder()
                .id(questId)
                .title("Morning Meditation")
                .description("Meditate for 10 minutes")
                .difficulty(Difficulty.EASY)
                .xpReward(25)
                .statType(StatType.FOCUS)
                .frequency(Frequency.DAILY)
                .recurring(true)
                .build();
    }

    @Test
    @DisplayName("QuestCompletedEvent is published on successful completion")
    void completeQuest_publishesQuestCompletedEvent() {
        when(questRepository.findById(questId)).thenReturn(Optional.of(testQuest));
        when(questCompletionRepository.existsByUserIdAndQuestIdAndCompletedAtBetween(
                eq(userId), eq(questId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(questCompletionRepository.save(any(QuestCompletion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        QuestCompletionResponse response = questCompletionService.completeQuest(userId, questId);

        // Verify event was published
        ArgumentCaptor<QuestCompletedEvent> eventCaptor = ArgumentCaptor.forClass(QuestCompletedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        QuestCompletedEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.getUserId()).isEqualTo(userId);
        assertThat(publishedEvent.getQuestId()).isEqualTo(questId);
        assertThat(publishedEvent.getQuestTitle()).isEqualTo("Morning Meditation");
        assertThat(publishedEvent.getDifficulty()).isEqualTo(Difficulty.EASY);
        assertThat(publishedEvent.getStatType()).isEqualTo(StatType.FOCUS);
        assertThat(publishedEvent.getBaseXpReward()).isEqualTo(25);
        assertThat(publishedEvent.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("Completion response contains correct XP and quest details")
    void completeQuest_returnsCorrectResponse() {
        when(questRepository.findById(questId)).thenReturn(Optional.of(testQuest));
        when(questCompletionRepository.existsByUserIdAndQuestIdAndCompletedAtBetween(
                eq(userId), eq(questId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(questCompletionRepository.save(any(QuestCompletion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        QuestCompletionResponse response = questCompletionService.completeQuest(userId, questId);

        assertThat(response.getQuestId()).isEqualTo(questId);
        assertThat(response.getQuestTitle()).isEqualTo("Morning Meditation");
        assertThat(response.getXpEarned()).isEqualTo(25);
        assertThat(response.getCompletedAt()).isNotNull();
        assertThat(response.getMessage()).contains("25 XP");
    }

    @Test
    @DisplayName("Completion record is persisted with correct data")
    void completeQuest_persistsCompletionRecord() {
        when(questRepository.findById(questId)).thenReturn(Optional.of(testQuest));
        when(questCompletionRepository.existsByUserIdAndQuestIdAndCompletedAtBetween(
                eq(userId), eq(questId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(questCompletionRepository.save(any(QuestCompletion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        questCompletionService.completeQuest(userId, questId);

        ArgumentCaptor<QuestCompletion> completionCaptor = ArgumentCaptor.forClass(QuestCompletion.class);
        verify(questCompletionRepository).save(completionCaptor.capture());

        QuestCompletion savedCompletion = completionCaptor.getValue();
        assertThat(savedCompletion.getUserId()).isEqualTo(userId);
        assertThat(savedCompletion.getQuestId()).isEqualTo(questId);
        assertThat(savedCompletion.getXpEarned()).isEqualTo(25);
        assertThat(savedCompletion.getDifficultyAtCompletion()).isEqualTo("EASY");
    }

    @Test
    @DisplayName("Duplicate completion throws DuplicateCompletionException")
    void completeQuest_duplicateCompletion_throwsException() {
        when(questRepository.findById(questId)).thenReturn(Optional.of(testQuest));
        when(questCompletionRepository.existsByUserIdAndQuestIdAndCompletedAtBetween(
                eq(userId), eq(questId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(true);

        assertThatThrownBy(() -> questCompletionService.completeQuest(userId, questId))
                .isInstanceOf(DuplicateCompletionException.class)
                .hasMessageContaining("already been completed today");

        // Verify no event was published and no record was saved
        verify(eventPublisher, never()).publishEvent(any());
        verify(questCompletionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Non-existent quest throws BusinessException")
    void completeQuest_questNotFound_throwsException() {
        when(questRepository.findById(questId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questCompletionService.completeQuest(userId, questId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Quest not found");

        verify(eventPublisher, never()).publishEvent(any());
        verify(questCompletionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Event is published with correct source reference")
    void completeQuest_eventHasCorrectSource() {
        when(questRepository.findById(questId)).thenReturn(Optional.of(testQuest));
        when(questCompletionRepository.existsByUserIdAndQuestIdAndCompletedAtBetween(
                eq(userId), eq(questId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(questCompletionRepository.save(any(QuestCompletion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        questCompletionService.completeQuest(userId, questId);

        ArgumentCaptor<QuestCompletedEvent> eventCaptor = ArgumentCaptor.forClass(QuestCompletedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        QuestCompletedEvent event = eventCaptor.getValue();
        assertThat(event.getSource()).isEqualTo(questCompletionService);
    }
}
