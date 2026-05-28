package com.ascend.notification.repository;

import com.ascend.notification.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    List<NotificationLog> findByUserIdOrderBySentAtDesc(UUID userId);

    long countByUserIdAndSentAtBetween(UUID userId, LocalDateTime start, LocalDateTime end);
}
