package com.ascend.reward.repository;

import com.ascend.reward.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, UUID> {
    List<Achievement> findByUserId(UUID userId);
    boolean existsByUserIdAndName(UUID userId, String name);
}
