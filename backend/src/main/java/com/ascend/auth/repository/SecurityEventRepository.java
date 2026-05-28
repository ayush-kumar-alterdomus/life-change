package com.ascend.auth.repository;

import com.ascend.auth.entity.SecurityEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SecurityEventRepository extends JpaRepository<SecurityEvent, UUID> {

    List<SecurityEvent> findByUserId(UUID userId);

    List<SecurityEvent> findByEventType(String eventType);

    List<SecurityEvent> findByUserIdAndCreatedAtAfter(UUID userId, LocalDateTime after);

    List<SecurityEvent> findByEventTypeAndCreatedAtAfter(String eventType, LocalDateTime after);
}
