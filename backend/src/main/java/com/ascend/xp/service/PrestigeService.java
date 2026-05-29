package com.ascend.xp.service;

import com.ascend.analytics.entity.Achievement;
import com.ascend.analytics.repository.AchievementRepository;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import com.ascend.xp.event.PrestigeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service responsible for the Prestige system.
 * <p>
 * Prestige allows users who reach Level 100+ to reset their level and XP
 * in exchange for a permanent XP multiplier bonus and a prestige badge.
 * <p>
 * Future XP formula incorporates prestige: BaseXP × (1 + 0.1 × PrestigeLevel)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrestigeService {

    private static final int PRESTIGE_MINIMUM_LEVEL = 100;
    private static final double PRESTIGE_MULTIPLIER_INCREMENT = 0.1;
    private static final String PRESTIGE_BADGE_TYPE = "PRESTIGE";

    private final UserRepository userRepository;
    private final AchievementRepository achievementRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Executes a prestige reset for the given user.
     * <p>
     * Requirements:
     * <ul>
     *   <li>User must be Level 100 or higher</li>
     *   <li>Resets level to 1 and XP to 0</li>
     *   <li>Increments prestige_level</li>
     *   <li>Awards a prestige badge (achievement)</li>
     *   <li>Publishes a {@link PrestigeEvent}</li>
     * </ul>
     *
     * @param userId the ID of the user requesting prestige
     * @return the new prestige level after the reset
     * @throws IllegalArgumentException if user is not found
     * @throws IllegalStateException    if user has not reached the minimum level
     */
    @Transactional
    public int prestige(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // 1. Verify user is Level 100+
        if (user.getLevel() < PRESTIGE_MINIMUM_LEVEL) {
            throw new IllegalStateException(
                    String.format("User %s must be at least level %d to prestige (current: %d)",
                            userId, PRESTIGE_MINIMUM_LEVEL, user.getLevel()));
        }

        int previousPrestigeLevel = user.getPrestigeLevel();
        int newPrestigeLevel = previousPrestigeLevel + 1;

        // 2. Reset level to 1, reset XP to 0
        user.setLevel(1);
        user.setXp(0L);

        // 3. Increment prestige_level
        user.setPrestigeLevel(newPrestigeLevel);
        userRepository.save(user);

        // 4. Award prestige badge
        awardPrestigeBadge(userId, newPrestigeLevel);

        // 5. Publish PrestigeEvent
        PrestigeEvent event = new PrestigeEvent(this, userId, newPrestigeLevel);
        eventPublisher.publishEvent(event);

        log.info("User {} prestiged! New prestige level: {}. Level and XP reset.",
                userId, newPrestigeLevel);

        return newPrestigeLevel;
    }

    /**
     * Returns the prestige XP multiplier for a given prestige level.
     * <p>
     * Formula: 1 + 0.1 × prestigeLevel
     * <p>
     * Examples:
     * <ul>
     *   <li>Prestige 0 → 1.0 (no bonus)</li>
     *   <li>Prestige 1 → 1.1</li>
     *   <li>Prestige 5 → 1.5</li>
     *   <li>Prestige 10 → 2.0</li>
     * </ul>
     *
     * @param prestigeLevel the user's prestige level (must be >= 0)
     * @return the prestige multiplier
     * @throws IllegalArgumentException if prestigeLevel is negative
     */
    public static double getPrestigeMultiplier(int prestigeLevel) {
        if (prestigeLevel < 0) {
            throw new IllegalArgumentException("prestigeLevel must be non-negative, got: " + prestigeLevel);
        }
        return 1.0 + (PRESTIGE_MULTIPLIER_INCREMENT * prestigeLevel);
    }

    private void awardPrestigeBadge(UUID userId, int prestigeLevel) {
        Achievement badge = Achievement.builder()
                .userId(userId)
                .achievementName("PRESTIGE_" + prestigeLevel)
                .achievementType(PRESTIGE_BADGE_TYPE)
                .description("Reached Prestige Level " + prestigeLevel)
                .build();
        achievementRepository.save(badge);
        log.debug("Awarded prestige badge to user {}: PRESTIGE_{}", userId, prestigeLevel);
    }
}
