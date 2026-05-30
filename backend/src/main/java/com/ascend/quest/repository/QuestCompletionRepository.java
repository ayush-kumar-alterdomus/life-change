package com.ascend.quest.repository;

import com.ascend.common.entity.StatType;
import com.ascend.quest.entity.QuestCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface QuestCompletionRepository extends JpaRepository<QuestCompletion, UUID> {

    List<QuestCompletion> findByUserIdAndCompletedAtBetween(UUID userId, LocalDateTime start, LocalDateTime end);

    boolean existsByUserIdAndQuestIdAndCompletedAtBetween(UUID userId, UUID questId, LocalDateTime start, LocalDateTime end);

    /**
     * Finds distinct stat types for which the user has completed quests within the given time range.
     * Used by stat decay logic to determine which stats are still active.
     */
    @Query("SELECT DISTINCT q.statType FROM QuestCompletion qc JOIN Quest q ON qc.questId = q.id " +
            "WHERE qc.userId = :userId AND qc.completedAt >= :since")
    List<StatType> findDistinctStatTypesCompletedSince(@Param("userId") UUID userId,
                                                       @Param("since") LocalDateTime since);
}
