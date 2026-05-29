package com.ascend.xp.repository;

import com.ascend.xp.entity.XpHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface XpHistoryRepository extends JpaRepository<XpHistory, UUID> {

    List<XpHistory> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Page<XpHistory> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(x.xpAmount), 0) FROM XpHistory x WHERE x.userId = :userId AND x.createdAt BETWEEN :start AND :end")
    Long sumXpAmountByUserIdAndCreatedAtBetween(@Param("userId") UUID userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
