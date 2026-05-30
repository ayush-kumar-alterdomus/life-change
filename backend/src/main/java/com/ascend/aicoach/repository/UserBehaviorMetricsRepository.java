package com.ascend.aicoach.repository;

import com.ascend.aicoach.entity.UserBehaviorMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserBehaviorMetricsRepository extends JpaRepository<UserBehaviorMetrics, UUID> {
    Optional<UserBehaviorMetrics> findByUserId(UUID userId);
}
