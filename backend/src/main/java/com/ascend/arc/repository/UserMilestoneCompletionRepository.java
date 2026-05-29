package com.ascend.arc.repository;

import com.ascend.arc.entity.UserMilestoneCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserMilestoneCompletionRepository extends JpaRepository<UserMilestoneCompletion, UUID> {

    List<UserMilestoneCompletion> findByUserIdAndArcId(UUID userId, UUID arcId);

    Optional<UserMilestoneCompletion> findByUserIdAndMilestoneId(UUID userId, UUID milestoneId);

    int countByUserIdAndArcId(UUID userId, UUID arcId);
}
