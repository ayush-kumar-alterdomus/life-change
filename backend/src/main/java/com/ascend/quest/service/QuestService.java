package com.ascend.quest.service;

import com.ascend.common.entity.Frequency;
import com.ascend.common.exception.BusinessException;
import com.ascend.premium.service.PremiumService;
import com.ascend.quest.dto.CreateQuestRequest;
import com.ascend.quest.dto.DailyQuestsResponse;
import com.ascend.quest.dto.QuestResponse;
import com.ascend.quest.dto.UpdateQuestRequest;
import com.ascend.quest.entity.Quest;
import com.ascend.quest.entity.QuestCompletion;
import com.ascend.quest.exception.CustomQuestLimitException;
import com.ascend.quest.mapper.QuestMapper;
import com.ascend.quest.repository.QuestCompletionRepository;
import com.ascend.quest.repository.QuestRepository;
import com.ascend.quest.validator.QuestValidator;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for quest retrieval and custom quest creation operations.
 * Handles fetching daily quests, individual quests, arc-based quests
 * with completion status resolution, and custom quest creation with limits.
 */
@Slf4j
@Service
public class QuestService {

    private static final int FREE_USER_MAX_CUSTOM_QUESTS = 5;
    private static final Frequency DEFAULT_FREQUENCY = Frequency.DAILY;

    private final QuestRepository questRepository;
    private final QuestCompletionRepository questCompletionRepository;
    private final QuestValidator questValidator;
    private final PremiumService premiumService;
    private final UserRepository userRepository;

    public QuestService(QuestRepository questRepository,
                        QuestCompletionRepository questCompletionRepository,
                        QuestValidator questValidator,
                        PremiumService premiumService,
                        UserRepository userRepository) {
        this.questRepository = questRepository;
        this.questCompletionRepository = questCompletionRepository;
        this.questValidator = questValidator;
        this.premiumService = premiumService;
        this.userRepository = userRepository;
    }

    /**
     * Returns the user's assigned quests for today with completion status.
     * Joins with quest_completion to determine if each quest is completed today.
     *
     * @param userId the authenticated user's ID
     * @return daily quests response with completion stats
     */
    @Transactional(readOnly = true)
    public DailyQuestsResponse getDailyQuests(UUID userId) {
        log.debug("Fetching daily quests for user={}", userId);

        // Get all quests assigned to this user (recurring + custom)
        List<Quest> userQuests = questRepository.findByCreatedBy_Id(userId);
        List<Quest> recurringQuests = questRepository.findByRecurringTrue();

        // Merge: user's custom quests + system recurring quests (avoid duplicates)
        Set<UUID> userQuestIds = userQuests.stream()
                .map(Quest::getId)
                .collect(Collectors.toSet());

        List<Quest> allDailyQuests = new java.util.ArrayList<>(userQuests);
        recurringQuests.stream()
                .filter(q -> !userQuestIds.contains(q.getId()))
                .forEach(allDailyQuests::add);

        // Determine which quests are completed today
        Set<UUID> completedToday = getCompletedQuestIdsForToday(userId);

        List<QuestResponse> questResponses = allDailyQuests.stream()
                .map(quest -> QuestMapper.toResponse(quest, completedToday))
                .collect(Collectors.toList());

        int completedCount = (int) questResponses.stream()
                .filter(QuestResponse::isCompleted)
                .count();

        return DailyQuestsResponse.builder()
                .date(LocalDate.now())
                .quests(questResponses)
                .totalQuests(questResponses.size())
                .completedQuests(completedCount)
                .build();
    }

    /**
     * Fetches a single quest by ID.
     *
     * @param questId the quest ID
     * @return the quest response
     * @throws BusinessException if quest not found
     */
    @Transactional(readOnly = true)
    public QuestResponse getQuestById(UUID questId) {
        log.debug("Fetching quest id={}", questId);

        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new BusinessException("QUEST_NOT_FOUND",
                        "Quest not found with id: " + questId));

        return QuestMapper.toResponse(quest);
    }

    /**
     * Fetches all quests belonging to a specific arc.
     *
     * @param arcId the arc ID
     * @return list of quest responses
     */
    @Transactional(readOnly = true)
    public List<QuestResponse> getQuestsByArc(UUID arcId) {
        log.debug("Fetching quests for arc={}", arcId);

        List<Quest> quests = questRepository.findByArcId(arcId);

        return quests.stream()
                .map(QuestMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Creates a custom quest for the given user.
     * Validates the request, enforces quest limits for free users,
     * and persists the quest with custom flags set.
     *
     * @param userId  the ID of the user creating the quest
     * @param request the quest creation request
     * @return the created quest response
     * @throws BusinessException           if validation fails or user not found
     * @throws CustomQuestLimitException   if free user exceeds max custom quests
     */
    @Transactional
    public QuestResponse createCustomQuest(UUID userId, CreateQuestRequest request) {
        log.info("Creating custom quest for user={}", userId);

        // Validate request via QuestValidator
        List<QuestValidator.ValidationError> errors = questValidator.validate(request);
        if (!errors.isEmpty()) {
            String errorMessage = errors.stream()
                    .map(e -> e.field() + ": " + e.message())
                    .collect(Collectors.joining("; "));
            throw new BusinessException("VALIDATION_FAILED", errorMessage);
        }

        // Check quest limits for free users
        if (!premiumService.isPremiumUser(userId)) {
            long currentCount = questRepository.countByCreatedBy_IdAndCustomTrue(userId);
            if (currentCount >= FREE_USER_MAX_CUSTOM_QUESTS) {
                throw new CustomQuestLimitException(
                        "Free users can create a maximum of " + FREE_USER_MAX_CUSTOM_QUESTS
                                + " custom quests. Upgrade to premium for unlimited quests.");
            }
        }

        // Load user reference
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND",
                        "User not found with id: " + userId));

        // Set default frequency if not provided
        Frequency frequency = request.getFrequency() != null
                ? request.getFrequency()
                : DEFAULT_FREQUENCY;

        // Build and persist the quest
        Quest quest = Quest.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .difficulty(request.getDifficulty())
                .xpReward(request.getXpReward())
                .statType(request.getStatType())
                .frequency(frequency)
                .recurring(frequency == Frequency.DAILY || frequency == Frequency.WEEKLY)
                .custom(true)
                .createdBy(user)
                .build();

        Quest savedQuest = questRepository.save(quest);
        log.info("Custom quest created: id={}, user={}, title='{}'",
                savedQuest.getId(), userId, savedQuest.getTitle());

        return QuestMapper.toResponse(savedQuest);
    }

    /**
     * Resolves the set of quest IDs that the user has completed today.
     * Uses the start and end of the current day as the time window.
     */
    private Set<UUID> getCompletedQuestIdsForToday(UUID userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        List<QuestCompletion> completions = questCompletionRepository
                .findByUserIdAndCompletedAtBetween(userId, startOfDay, endOfDay);

        return completions.stream()
                .map(QuestCompletion::getQuestId)
                .collect(Collectors.toSet());
    }

    /**
     * Updates a custom quest owned by the user.
     * Only non-null fields in the request are applied (partial update).
     *
     * @param userId  the authenticated user's ID
     * @param questId the quest to update
     * @param request the update request with optional fields
     * @return the updated quest response
     */
    @Transactional
    public QuestResponse updateQuest(UUID userId, UUID questId, UpdateQuestRequest request) {
        log.info("Updating quest={} for user={}", questId, userId);

        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new BusinessException("QUEST_NOT_FOUND",
                        "Quest not found with id: " + questId));

        if (!quest.isCustom()) {
            throw new BusinessException("SYSTEM_QUEST", "System quests cannot be modified");
        }

        if (quest.getCreatedBy() == null || !quest.getCreatedBy().getId().equals(userId)) {
            throw new BusinessException("NOT_QUEST_OWNER", "You can only update your own quests");
        }

        if (request.getTitle() != null) {
            if (request.getTitle().isBlank() || request.getTitle().length() > 100) {
                throw new BusinessException("VALIDATION_FAILED", "Title must be 1-100 characters");
            }
            quest.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            quest.setDescription(request.getDescription());
        }
        if (request.getDifficulty() != null) {
            quest.setDifficulty(request.getDifficulty());
        }
        if (request.getXpReward() != null) {
            if (request.getXpReward() < 10 || request.getXpReward() > 300) {
                throw new BusinessException("VALIDATION_FAILED", "XP reward must be between 10 and 300");
            }
            quest.setXpReward(request.getXpReward());
        }
        if (request.getStatType() != null) {
            quest.setStatType(request.getStatType());
        }
        if (request.getFrequency() != null) {
            quest.setFrequency(request.getFrequency());
            quest.setRecurring(request.getFrequency() == Frequency.DAILY || request.getFrequency() == Frequency.WEEKLY);
        }

        Quest saved = questRepository.save(quest);
        log.info("Quest updated: id={}, user={}", questId, userId);
        return QuestMapper.toResponse(saved);
    }

    /**
     * Deletes a custom quest owned by the user.
     * Also removes associated completion history.
     *
     * @param userId  the authenticated user's ID
     * @param questId the quest to delete
     */
    @Transactional
    public void deleteQuest(UUID userId, UUID questId) {
        log.info("Deleting quest={} for user={}", questId, userId);

        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new BusinessException("QUEST_NOT_FOUND",
                        "Quest not found with id: " + questId));

        if (!quest.isCustom()) {
            throw new BusinessException("SYSTEM_QUEST", "System quests cannot be deleted");
        }

        if (quest.getCreatedBy() == null || !quest.getCreatedBy().getId().equals(userId)) {
            throw new BusinessException("NOT_QUEST_OWNER", "You can only delete your own quests");
        }

        if (quest.getArcId() != null) {
            throw new BusinessException("ARC_LINKED_QUEST",
                    "Cannot delete a quest linked to an arc. Abandon the arc first.");
        }

        questCompletionRepository.deleteByQuestId(questId);
        questRepository.delete(quest);
        log.info("Quest deleted: id={}, user={}", questId, userId);
    }
}
