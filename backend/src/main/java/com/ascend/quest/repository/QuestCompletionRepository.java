package com.ascend.quest.repository;

import com.ascend.quest.entity.QuestCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface QuestCompletionRepository extends JpaRepository<QuestCompletion, UUID> {

    List<QuestCompletion> findByUserIdAndCompletedAtBetween(UUID userId, LocalDateTime start, LocalDateTime end);

    boolean existsByUserIdAndQuestIdAndCompletedAtBetween(UUID userId, UUID questId, LocalDateTime start, LocalDateTime end);
}
