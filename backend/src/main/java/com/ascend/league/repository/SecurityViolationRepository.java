package com.ascend.league.repository;

import com.ascend.league.entity.SecurityViolation;
import com.ascend.league.entity.ViolationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SecurityViolationRepository extends JpaRepository<SecurityViolation, UUID> {

    List<SecurityViolation> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<SecurityViolation> findByUserIdAndResolvedFalse(UUID userId);

    long countByUserIdAndViolationTypeAndCreatedAtAfter(UUID userId, ViolationType violationType, LocalDateTime after);

    boolean existsByUserIdAndLeaderboardBannedTrue(UUID userId);
}
