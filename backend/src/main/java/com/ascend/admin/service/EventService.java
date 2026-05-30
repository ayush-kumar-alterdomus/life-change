package com.ascend.admin.service;

import com.ascend.admin.dto.SeasonalEventRequest;
import com.ascend.admin.entity.SeasonalEvent;
import com.ascend.admin.repository.SeasonalEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing seasonal events (creation, activation, deactivation).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final SeasonalEventRepository seasonalEventRepository;

    @Transactional
    public SeasonalEvent createEvent(SeasonalEventRequest request) {
        SeasonalEvent event = SeasonalEvent.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .active(true)
                .rewards(request.getRewards())
                .challenges(request.getThemedChallenges())
                .build();

        SeasonalEvent saved = seasonalEventRepository.save(event);
        log.info("Created seasonal event: {} ({})", saved.getTitle(), saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<SeasonalEvent> getActiveEvents() {
        return seasonalEventRepository.findByActiveTrueAndEndDateAfter(LocalDateTime.now());
    }

    @Transactional
    public void endEvent(UUID eventId) {
        SeasonalEvent event = seasonalEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));
        event.setActive(false);
        seasonalEventRepository.save(event);
        log.info("Ended seasonal event: {} ({})", event.getTitle(), event.getId());
    }
}
