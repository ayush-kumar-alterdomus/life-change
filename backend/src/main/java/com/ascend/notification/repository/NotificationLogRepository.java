package com.ascend.notification.repository;

import com.ascend.notification.entity.NotificationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    List<NotificationLog> findByUserIdOrderBySentAtDesc(UUID userId);

    Page<NotificationLog> findByUserId(UUID userId, Pageable pageable);

    long countByUserIdAndSentAtBetween(UUID userId, LocalDateTime start, LocalDateTime end);

    boolean existsByUserIdAndTypeAndSentAtBetween(UUID userId, String type, LocalDateTime start, LocalDateTime end);

    long countByUserIdAndReadAtIsNull(UUID userId);

    @Modifying
    @Query("UPDATE NotificationLog n SET n.readAt = :now WHERE n.userId = :userId AND n.readAt IS NULL")
    int markAllAsRead(@Param("userId") UUID userId, @Param("now") LocalDateTime now);
}
