package com.ascend.quest.service;

import com.ascend.common.exception.BusinessException;
import com.ascend.quest.dto.QuestCompletionResponse;
import com.ascend.quest.entity.Quest;
import com.ascend.quest.entity.QuestCompletion;
import com.ascend.quest.event.QuestCompletedEvent;
import com.ascend.quest.exception.DuplicateCompletionException;
import com.ascend.quest.repository.QuestCompletionRepository;
import com.ascend.quest.repository.QuestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Service handling quest completion logic.
 * Validates quest existence, enforces idempotency (one completion per quest per day),
 * persists the completion record, and publishes the QuestCompletedEvent.
 */
@Slf4j
@Service
public class QuestCompletionService {

    private final QuestRepository questRepository;
    private final QuestCompletionRepository questCompletionRepository;
    private final ApplicationEventPublisher eventPublisher;

    public QuestCompletionService(QuestRepository questRepository,
                                  QuestCompletionRepository questCompletionRepository,
                                  ApplicationEventPublisher eventPublisher) {
        this.questRepository = questRepository;
        this.questCompletionRepository = questCompletionRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Completes a quest for the given user.
     *
     * @param userId  the ID of the user completing the quest
     * @param questId the ID of the quest to complete
     * @return completion confirmation with XP earned
     * @throws BusinessException             if the quest does not exist
     * @throws DuplicateCompletionException  if the quest was already completed today
     */
    @Transactional
    public QuestCompletionResponse completeQuest(UUID userId, UUID questId) {
        log.info("Processing quest completion: user={}, quest={}", userId, questId);

        // 1. Verify quest exists
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new BusinessException("QUEST_NOT_FOUND",
                        "Quest not found with id: " + questId));

        // 2. Check idempotency — reject if (user_id, quest_id, today) already exists
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        boolean alreadyCompleted = questCompletionRepository
                .existsByUserIdAndQuestIdAndCompletedAtBetween(userId, questId, startOfDay, endOfDay);

        if (alreadyCompleted) {
            throw new DuplicateCompletionException(
                    "Quest '" + quest.getTitle() + "' has already been completed today");
        }

        // 3. Create QuestCompletion record and persist it
        LocalDateTime completedAt = LocalDateTime.now();
        QuestCompletion completion = QuestCompletion.builder()
                .userId(userId)
                .questId(questId)
                .completedAt(completedAt)
                .xpEarned(quest.getXpReward())
                .multiplier(BigDecimal.ONE)
                .difficultyAtCompletion(quest.getDifficulty().name())
                .build();

        questCompletionRepository.save(completion);
        log.info("Quest completion saved: user={}, quest={}, xp={}", userId, questId, quest.getXpReward());

        // 4. Publish QuestCompletedEvent
        QuestCompletedEvent event = new QuestCompletedEvent(
                this,
                userId,
                questId,
                quest.getTitle(),
                quest.getDifficulty(),
                quest.getStatType(),
                quest.getXpReward(),
                completedAt
        );
        eventPublisher.publishEvent(event);
        log.debug("QuestCompletedEvent published: user={}, quest={}", userId, questId);

        // 5. Return completion confirmation with XP earned
        return QuestCompletionResponse.builder()
                .questId(questId)
                .questTitle(quest.getTitle())
                .xpEarned(quest.getXpReward())
                .completedAt(completedAt)
                .message("Quest completed! You earned " + quest.getXpReward() + " XP.")
                .build();
    }
}
