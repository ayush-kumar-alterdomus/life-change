package com.ascend.xp.service;

import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import com.ascend.xp.event.LevelUpEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service responsible for detecting level-ups, updating user records,
 * determining feature unlocks, and publishing LevelUpEvents.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LevelService {

    private static final int LEAGUES_UNLOCK_LEVEL = 10;
    private static final int GUILDS_UNLOCK_LEVEL = 25;
    private static final int PRESTIGE_UNLOCK_LEVEL = 100;

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Checks whether the user has leveled up based on new total XP and processes
     * all level-ups that occurred (possibly multiple levels at once).
     *
     * @param userId      the user's ID
     * @param newTotalXp  the user's updated total XP
     * @param currentLevel the user's level before this XP award
     * @return a {@link LevelUpResult} with the new level and any unlocked features,
     *         or {@code null} if no level-up occurred
     */
    @Transactional
    public LevelUpResult checkAndProcessLevelUp(UUID userId, long newTotalXp, int currentLevel) {
        int newLevel = LevelCalculator.calculateLevel(newTotalXp);

        if (newLevel <= currentLevel) {
            return null;
        }

        // Update user record
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setLevel(newLevel);
        userRepository.save(user);

        // Determine all features unlocked across the gained levels
        List<String> unlockedFeatures = determineUnlocks(currentLevel, newLevel);

        // Publish a LevelUpEvent for each level gained
        for (int level = currentLevel + 1; level <= newLevel; level++) {
            List<String> levelUnlocks = determineUnlocksForLevel(level);
            LevelUpEvent event = new LevelUpEvent(this, userId, level - 1, level, levelUnlocks);
            eventPublisher.publishEvent(event);
            log.info("User {} leveled up: {} → {} | Unlocks: {}", userId, level - 1, level, levelUnlocks);
        }

        log.info("User {} level-up complete: {} → {} | Total unlocks: {}",
                userId, currentLevel, newLevel, unlockedFeatures);

        return new LevelUpResult(newLevel, unlockedFeatures);
    }

    /**
     * Determines all features unlocked between two levels (exclusive of currentLevel,
     * inclusive of newLevel).
     */
    private List<String> determineUnlocks(int currentLevel, int newLevel) {
        List<String> unlocks = new ArrayList<>();
        for (int level = currentLevel + 1; level <= newLevel; level++) {
            unlocks.addAll(determineUnlocksForLevel(level));
        }
        return unlocks;
    }

    /**
     * Determines features unlocked at a specific level.
     */
    private List<String> determineUnlocksForLevel(int level) {
        List<String> unlocks = new ArrayList<>();
        if (level == LEAGUES_UNLOCK_LEVEL) {
            unlocks.add("LEAGUES");
        }
        if (level == GUILDS_UNLOCK_LEVEL) {
            unlocks.add("GUILDS");
        }
        if (level == PRESTIGE_UNLOCK_LEVEL) {
            unlocks.add("PRESTIGE");
        }
        return unlocks;
    }

    /**
     * Holds the result of a level-up check.
     */
    public record LevelUpResult(int newLevel, List<String> unlockedFeatures) {
    }
}
