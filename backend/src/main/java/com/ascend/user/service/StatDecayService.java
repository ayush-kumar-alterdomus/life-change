package com.ascend.user.service;

import com.ascend.common.entity.StatType;
import com.ascend.quest.repository.QuestCompletionRepository;
import com.ascend.user.entity.User;
import com.ascend.user.entity.UserStats;
import com.ascend.user.repository.UserRepository;
import com.ascend.user.repository.UserStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service responsible for evaluating and applying stat decay for Hard Mode users.
 * If a user has not completed any quests for a given stat type in the last 7 days,
 * that stat decays by 5 points (minimum 0).
 * Called by the daily scheduler for all Hard Mode users.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatDecayService {

    private static final int DECAY_AMOUNT = 5;
    private static final int DECAY_WINDOW_DAYS = 7;
    private static final int MINIMUM_STAT_VALUE = 0;

    /** The amount of points deducted per stat per decay cycle. */
    public static final int DECAY_POINTS = DECAY_AMOUNT;

    private final UserRepository userRepository;
    private final UserStatsRepository userStatsRepository;
    private final QuestCompletionRepository questCompletionRepository;

    /**
     * Evaluates and applies stat decay for a specific user.
     * Only applies if the user has hard_mode enabled.
     * For each stat type with no quest completions in the last 7 days,
     * the stat is reduced by 5 points (never below 0).
     *
     * @param userId the ID of the user to evaluate decay for
     * @return the set of stat types that were decayed, empty if no decay applied
     */
    @Transactional
    public Set<StatType> evaluateStatDecay(UUID userId) {
        Set<StatType> decayedStatTypes = new HashSet<>();

        // 1. Only applies if user.hard_mode = true
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("Cannot evaluate stat decay: user {} not found", userId);
            return decayedStatTypes;
        }

        if (!Boolean.TRUE.equals(user.getHardMode())) {
            log.debug("Skipping stat decay for user {} — hard mode not enabled", userId);
            return decayedStatTypes;
        }

        // 2. Check quest completion history per stat type over last 7 days
        LocalDateTime since = LocalDateTime.now().minusDays(DECAY_WINDOW_DAYS);
        List<StatType> activeStatTypes = questCompletionRepository
                .findDistinctStatTypesCompletedSince(userId, since);

        Set<StatType> activeStats = Set.copyOf(activeStatTypes);

        // Determine which stat types have NO completions in the window
        Set<StatType> inactiveStats = Arrays.stream(StatType.values())
                .filter(statType -> !activeStats.contains(statType))
                .collect(Collectors.toSet());

        if (inactiveStats.isEmpty()) {
            log.debug("No stat decay needed for user {} — all stats active in last {} days",
                    userId, DECAY_WINDOW_DAYS);
            return decayedStatTypes;
        }

        // Fetch user stats
        UserStats stats = userStatsRepository.findByUserId(userId).orElse(null);
        if (stats == null) {
            log.debug("No stats record found for user {} — nothing to decay", userId);
            return decayedStatTypes;
        }

        // 3. Apply decay for each inactive stat type
        for (StatType statType : inactiveStats) {
            if (applyDecay(stats, statType, userId)) {
                decayedStatTypes.add(statType);
            }
        }

        // Save updated stats
        if (!decayedStatTypes.isEmpty()) {
            userStatsRepository.save(stats);
        }

        return decayedStatTypes;
    }

    /**
     * Applies decay to a single stat type, reducing it by DECAY_AMOUNT.
     * The stat value will never go below 0.
     *
     * @param stats    the user's stats entity
     * @param statType the stat type to decay
     * @param userId   the user ID (for logging)
     * @return true if decay was applied, false if stat was already at 0
     */
    private boolean applyDecay(UserStats stats, StatType statType, UUID userId) {
        int currentValue = getStatValue(stats, statType);
        int newValue = Math.max(currentValue - DECAY_AMOUNT, MINIMUM_STAT_VALUE);

        if (newValue == currentValue) {
            // Already at 0, no decay to apply
            return false;
        }

        setStatValue(stats, statType, newValue);

        // 5. Log decay event
        log.info("Stat decay applied for user {}: {} {} -> {} (-{})",
                userId, statType, currentValue, newValue, currentValue - newValue);

        return true;
    }

    private int getStatValue(UserStats stats, StatType statType) {
        return switch (statType) {
            case STRENGTH -> stats.getStrength();
            case WISDOM -> stats.getWisdom();
            case FOCUS -> stats.getFocus();
            case DISCIPLINE -> stats.getDiscipline();
            case VITALITY -> stats.getVitality();
            case CHARISMA -> stats.getCharisma();
        };
    }

    private void setStatValue(UserStats stats, StatType statType, int value) {
        switch (statType) {
            case STRENGTH -> stats.setStrength(value);
            case WISDOM -> stats.setWisdom(value);
            case FOCUS -> stats.setFocus(value);
            case DISCIPLINE -> stats.setDiscipline(value);
            case VITALITY -> stats.setVitality(value);
            case CHARISMA -> stats.setCharisma(value);
        }
    }
}
