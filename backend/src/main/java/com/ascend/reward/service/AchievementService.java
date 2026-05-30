package com.ascend.reward.service;

import com.ascend.reward.dto.AchievementResponse;
import com.ascend.reward.entity.Achievement;
import com.ascend.reward.event.AchievementUnlockedEvent;
import com.ascend.reward.repository.AchievementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public boolean checkAndUnlock(UUID userId, String name, String type, String description, String badge) {
        if (achievementRepository.existsByUserIdAndName(userId, name)) {
            return false; // already unlocked
        }

        Achievement achievement = Achievement.builder()
                .userId(userId)
                .name(name)
                .type(type)
                .description(description)
                .badge(badge)
                .build();

        achievementRepository.save(achievement);
        eventPublisher.publishEvent(new AchievementUnlockedEvent(this, userId, name, type));
        log.info("Achievement unlocked: user={} name={}", userId, name);
        return true;
    }

    @Transactional(readOnly = true)
    public List<AchievementResponse> getAchievements(UUID userId) {
        return achievementRepository.findByUserId(userId).stream()
                .map(a -> new AchievementResponse(a.getName(), a.getType(), a.getDescription(), a.getUnlockedAt(), a.getBadge()))
                .toList();
    }
}
