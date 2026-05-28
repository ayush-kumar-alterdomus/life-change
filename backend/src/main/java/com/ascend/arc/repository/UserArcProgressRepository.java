package com.ascend.arc.repository;

import com.ascend.arc.entity.UserArcProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserArcProgressRepository extends JpaRepository<UserArcProgress, UUID> {

    List<UserArcProgress> findByUserIdAndStatus(UUID userId, String status);

    Optional<UserArcProgress> findByUserIdAndArcId(UUID userId, UUID arcId);
}
