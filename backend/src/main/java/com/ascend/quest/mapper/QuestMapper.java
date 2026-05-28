package com.ascend.quest.mapper;

import com.ascend.quest.dto.QuestResponse;
import com.ascend.quest.entity.Quest;

import java.util.Set;
import java.util.UUID;

/**
 * Maps Quest entities to response DTOs.
 */
public final class QuestMapper {

    private QuestMapper() {
        // utility class
    }

    /**
     * Converts a Quest entity to a QuestResponse DTO.
     *
     * @param quest           the quest entity
     * @param completedQuestIds set of quest IDs completed today by the user
     * @return the quest response DTO
     */
    public static QuestResponse toResponse(Quest quest, Set<UUID> completedQuestIds) {
        return QuestResponse.builder()
                .id(quest.getId())
                .title(quest.getTitle())
                .description(quest.getDescription())
                .xpReward(quest.getXpReward())
                .difficulty(quest.getDifficulty())
                .statType(quest.getStatType())
                .frequency(quest.getFrequency())
                .recurring(quest.isRecurring())
                .isCustom(quest.isCustom())
                .completed(completedQuestIds.contains(quest.getId()))
                .build();
    }

    /**
     * Converts a Quest entity to a QuestResponse DTO without completion status.
     *
     * @param quest the quest entity
     * @return the quest response DTO with completed=false
     */
    public static QuestResponse toResponse(Quest quest) {
        return QuestResponse.builder()
                .id(quest.getId())
                .title(quest.getTitle())
                .description(quest.getDescription())
                .xpReward(quest.getXpReward())
                .difficulty(quest.getDifficulty())
                .statType(quest.getStatType())
                .frequency(quest.getFrequency())
                .recurring(quest.isRecurring())
                .isCustom(quest.isCustom())
                .completed(false)
                .build();
    }
}
