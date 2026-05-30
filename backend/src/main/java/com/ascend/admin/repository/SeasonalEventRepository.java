package com.ascend.admin.repository;

import com.ascend.admin.entity.SeasonalEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SeasonalEventRepository extends JpaRepository<SeasonalEvent, UUID> {

    List<SeasonalEvent> findByActiveTrueAndEndDateAfter(LocalDateTime now);

    List<SeasonalEvent> findByActiveTrue();
}
