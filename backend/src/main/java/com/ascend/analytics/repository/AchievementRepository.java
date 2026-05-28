package com.ascend.analytics.repository;

import com.ascend.analytics.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, UUID> {

    List<Achievement> findByUserId(UUID userId);

    boolean existsByUserIdAndAchievementName(UUID userId, String achievementName);
}
