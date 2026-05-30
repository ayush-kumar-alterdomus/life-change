package com.ascend.user.service;

import com.ascend.analytics.entity.Achievement;
import com.ascend.analytics.repository.AchievementRepository;
import com.ascend.common.entity.Difficulty;
import com.ascend.common.entity.StatType;
import com.ascend.streak.entity.Streak;
import com.ascend.streak.repository.StreakRepository;
import com.ascend.user.dto.IdentityTitle;
import com.ascend.user.dto.RadarChartResponse;
import com.ascend.user.dto.StatGainResponse;
import com.ascend.user.dto.StatThresholds;
import com.ascend.user.dto.UserStatsResponse;
import com.ascend.user.entity.UserStats;
import com.ascend.user.event.AchievementUnlockedEvent;
import com.ascend.user.repository.UserStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for awarding stat points on quest completion,
 * recalculating life_score, and checking identity title unlocks.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatService {

    private static final int BASE_STAT = 1;
    private static final double EASY_MULTIPLIER = 1.0;
    private static final double MEDIUM_MULTIPLIER = 1.5;
    private static final double HARD_MULTIPLIER = 2.0;
    private static final double LEGENDARY_MULTIPLIER = 3.0;

    private static final double DISCIPLINE_WEIGHT = 0.25;
    private static final double FOCUS_WEIGHT = 0.20;
    private static final double VITALITY_WEIGHT = 0.20;
    private static final double WISDOM_WEIGHT = 0.20;
    private static final double CONSISTENCY_WEIGHT = 0.15;
    private static final double CONSISTENCY_MAX_STREAK_DAYS = 30.0;

    private static final String ACHIEVEMENT_TYPE_IDENTITY_TITLE = "IDENTITY_TITLE";

    private final UserStatsRepository userStatsRepository;
    private final StreakRepository streakRepository;
    private final AchievementRepository achievementRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Awards stat points to a user based on quest difficulty and stat type.
     * Steps:
     * 1. Calculate gain: BaseStat(1) × DifficultyMultiplier
     * 2. Fetch current user_stats
     * 3. Increment the relevant stat
     * 4. Check title unlock thresholds
     * 5. Recalculate life_score
     * 6. Save updated stats
     * 7. Return StatGainResponse
     *
     * @param userId     the user receiving stat points
     * @param statType   the stat category to increase
     * @param difficulty the quest difficulty determining point gain
     * @return StatGainResponse with previous/new values and any title unlocked
     */
    @Transactional
    public StatGainResponse awardStatPoints(UUID userId, StatType statType, Difficulty difficulty) {
        // 1. Calculate gain
        int statGain = calculateStatGain(difficulty);

        // 2. Fetch current user_stats
        UserStats stats = userStatsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultStats(userId));

        // Record previous value
        int previousValue = getStatValue(stats, statType);

        // 3. Increment the relevant stat
        applyStatGain(stats, statType, statGain);
        int newValue = getStatValue(stats, statType);

        // 4. Check title unlock thresholds
        String titleUnlocked = checkTitleUnlocks(userId, statType, newValue, previousValue);

        // 5. Recalculate life_score
        BigDecimal newLifeScore = calculateLifeScore(stats, userId);
        stats.setLifeScore(newLifeScore);

        // 6. Save updated stats
        userStatsRepository.save(stats);

        log.debug("Awarded {} {} points to user {}. Previous: {}, New: {}, Life score: {}",
                statGain, statType, userId, previousValue, newValue, newLifeScore);

        // 7. Return StatGainResponse
        return StatGainResponse.builder()
                .statType(statType)
                .previousValue(previousValue)
                .newValue(newValue)
                .gain(statGain)
                .titleUnlocked(titleUnlocked)
                .build();
    }

    /**
     * Returns the full user stats with earned identity titles.
     *
     * @param userId the user ID
     * @return UserStatsResponse with all stats, life score, and earned titles
     */
    @Transactional(readOnly = true)
    public UserStatsResponse getUserStats(UUID userId) {
        UserStats stats = userStatsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultStats(userId));

        List<IdentityTitle> earnedTitles = getEarnedTitles(userId);

        return UserStatsResponse.builder()
                .strength(stats.getStrength())
                .wisdom(stats.getWisdom())
                .focus(stats.getFocus())
                .discipline(stats.getDiscipline())
                .vitality(stats.getVitality())
                .charisma(stats.getCharisma())
                .lifeScore(stats.getLifeScore())
                .titles(earnedTitles)
                .build();
    }

    /**
     * Calculates stat gain from quest difficulty.
     * Formula: BaseStat(1) × DifficultyMultiplier (EASY=1, MEDIUM=1.5, HARD=2, LEGENDARY=3).
     *
     * @param difficulty the quest difficulty
     * @return integer stat points to award (rounded down)
     */
    int calculateStatGain(Difficulty difficulty) {
        double multiplier = getDifficultyMultiplier(difficulty);
        return (int) (BASE_STAT * multiplier);
    }

    /**
     * Returns the difficulty multiplier for stat gain calculation.
     *
     * @param difficulty the quest difficulty
     * @return the multiplier value
     */
    double getDifficultyMultiplier(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> EASY_MULTIPLIER;
            case MEDIUM -> MEDIUM_MULTIPLIER;
            case HARD -> HARD_MULTIPLIER;
            case LEGENDARY -> LEGENDARY_MULTIPLIER;
        };
    }

    /**
     * Calculates the life_score based on weighted stat values and streak-based consistency.
     * Formula: 0.25×Discipline + 0.2×Focus + 0.2×Vitality + 0.2×Wisdom + 0.15×Consistency
     * Consistency is derived from the user's current streak (capped at 30 days → 100%).
     *
     * @param stats  the user's current stats
     * @param userId the user ID (for streak lookup)
     * @return the calculated life_score normalized to 0-100 scale
     */
    public BigDecimal calculateLifeScore(UserStats stats, UUID userId) {
        double consistency = getConsistencyScore(userId);

        double score = (DISCIPLINE_WEIGHT * stats.getDiscipline())
                + (FOCUS_WEIGHT * stats.getFocus())
                + (VITALITY_WEIGHT * stats.getVitality())
                + (WISDOM_WEIGHT * stats.getWisdom())
                + (CONSISTENCY_WEIGHT * consistency);

        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Checks if the user has crossed any stat thresholds for identity title unlocks.
     * Titles are permanent — once unlocked, they are never revoked even if stat decreases.
     *
     * @param userId        the user ID
     * @param statType      the stat type that was incremented
     * @param newValue      the new stat value after increment
     * @param previousValue the stat value before increment
     * @return the title name if a new title was unlocked, null otherwise
     */
    String checkTitleUnlocks(UUID userId, StatType statType, int newValue, int previousValue) {
        String unlockedTitle = null;

        for (int threshold : StatThresholds.ALL_THRESHOLDS) {
            // Check if this increment crossed the threshold
            if (newValue >= threshold && previousValue < threshold) {
                IdentityTitle title = StatThresholds.getTitleForThreshold(statType, threshold);
                if (title != null) {
                    String achievementName = buildAchievementName(statType, threshold);

                    // Check if title already unlocked (idempotency)
                    if (!achievementRepository.existsByUserIdAndAchievementName(userId, achievementName)) {
                        // Create Achievement record
                        Achievement achievement = Achievement.builder()
                                .userId(userId)
                                .achievementName(achievementName)
                                .achievementType(ACHIEVEMENT_TYPE_IDENTITY_TITLE)
                                .description(title.description())
                                .build();
                        achievementRepository.save(achievement);

                        // Publish AchievementUnlockedEvent for notification/analytics modules
                        AchievementUnlockedEvent event = new AchievementUnlockedEvent(
                                this, userId, statType, threshold, title.titleName());
                        eventPublisher.publishEvent(event);

                        unlockedTitle = title.titleName();

                        log.info("User {} unlocked identity title '{}' for {} at threshold {}",
                                userId, title.titleName(), statType, threshold);
                    }
                }
            }
        }

        return unlockedTitle;
    }

    private void applyStatGain(UserStats stats, StatType statType, int gain) {
        switch (statType) {
            case STRENGTH -> stats.setStrength(stats.getStrength() + gain);
            case WISDOM -> stats.setWisdom(stats.getWisdom() + gain);
            case FOCUS -> stats.setFocus(stats.getFocus() + gain);
            case DISCIPLINE -> stats.setDiscipline(stats.getDiscipline() + gain);
            case VITALITY -> stats.setVitality(stats.getVitality() + gain);
            case CHARISMA -> stats.setCharisma(stats.getCharisma() + gain);
        }
    }

    int getStatValue(UserStats stats, StatType statType) {
        return switch (statType) {
            case STRENGTH -> stats.getStrength();
            case WISDOM -> stats.getWisdom();
            case FOCUS -> stats.getFocus();
            case DISCIPLINE -> stats.getDiscipline();
            case VITALITY -> stats.getVitality();
            case CHARISMA -> stats.getCharisma();
        };
    }

    private double getConsistencyScore(UUID userId) {
        int currentStreak = streakRepository.findByUserId(userId)
                .map(Streak::getCurrentStreak)
                .orElse(0);

        return Math.min(currentStreak / CONSISTENCY_MAX_STREAK_DAYS, 1.0) * 100.0;
    }

    private UserStats createDefaultStats(UUID userId) {
        UserStats stats = UserStats.builder()
                .userId(userId)
                .build();
        return userStatsRepository.save(stats);
    }

    /**
     * Returns stats formatted for radar chart display.
     * Each stat becomes an entry with label, statType, and value.
     *
     * @param userId the user ID
     * @return RadarChartResponse with entries and max value
     */
    @Transactional(readOnly = true)
    public RadarChartResponse getRadarChartData(UUID userId) {
        UserStats stats = userStatsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultStats(userId));

        List<RadarChartResponse.RadarChartEntry> entries = List.of(
                RadarChartResponse.RadarChartEntry.builder()
                        .label("Strength").statType(StatType.STRENGTH.name()).value(stats.getStrength()).build(),
                RadarChartResponse.RadarChartEntry.builder()
                        .label("Wisdom").statType(StatType.WISDOM.name()).value(stats.getWisdom()).build(),
                RadarChartResponse.RadarChartEntry.builder()
                        .label("Focus").statType(StatType.FOCUS.name()).value(stats.getFocus()).build(),
                RadarChartResponse.RadarChartEntry.builder()
                        .label("Discipline").statType(StatType.DISCIPLINE.name()).value(stats.getDiscipline()).build(),
                RadarChartResponse.RadarChartEntry.builder()
                        .label("Vitality").statType(StatType.VITALITY.name()).value(stats.getVitality()).build(),
                RadarChartResponse.RadarChartEntry.builder()
                        .label("Charisma").statType(StatType.CHARISMA.name()).value(stats.getCharisma()).build()
        );

        int maxValue = entries.stream()
                .mapToInt(RadarChartResponse.RadarChartEntry::getValue)
                .max()
                .orElse(100);

        // Ensure maxValue is at least 100 for meaningful chart display
        maxValue = Math.max(maxValue, 100);

        return RadarChartResponse.builder()
                .entries(entries)
                .maxValue(maxValue)
                .build();
    }

    /**
     * Retrieves all earned identity titles for a user from the achievements table.
     *
     * @param userId the user ID
     * @return list of earned IdentityTitle records
     */
    @Transactional(readOnly = true)
    public List<IdentityTitle> getEarnedTitles(UUID userId) {
        List<Achievement> titleAchievements = achievementRepository.findByUserId(userId)
                .stream()
                .filter(a -> ACHIEVEMENT_TYPE_IDENTITY_TITLE.equals(a.getAchievementType()))
                .toList();

        List<IdentityTitle> titles = new ArrayList<>();
        for (Achievement achievement : titleAchievements) {
            IdentityTitle title = parseAchievementToTitle(achievement);
            if (title != null) {
                titles.add(title);
            }
        }
        return titles;
    }

    /**
     * Builds a unique achievement name for a stat/threshold combination.
     * Format: "IDENTITY_TITLE_{STAT_TYPE}_{THRESHOLD}"
     */
    private String buildAchievementName(StatType statType, int threshold) {
        return "IDENTITY_TITLE_" + statType.name() + "_" + threshold;
    }

    /**
     * Parses an Achievement entity back into an IdentityTitle DTO.
     */
    private IdentityTitle parseAchievementToTitle(Achievement achievement) {
        String name = achievement.getAchievementName();
        if (!name.startsWith("IDENTITY_TITLE_")) {
            return null;
        }

        // Parse format: IDENTITY_TITLE_{STAT_TYPE}_{THRESHOLD}
        String remainder = name.substring("IDENTITY_TITLE_".length());
        int lastUnderscore = remainder.lastIndexOf('_');
        if (lastUnderscore < 0) {
            return null;
        }

        try {
            String statTypeName = remainder.substring(0, lastUnderscore);
            int threshold = Integer.parseInt(remainder.substring(lastUnderscore + 1));
            StatType statType = StatType.valueOf(statTypeName);
            return StatThresholds.getTitleForThreshold(statType, threshold);
        } catch (IllegalArgumentException e) {
            log.warn("Could not parse achievement name to IdentityTitle: {}", name);
            return null;
        }
    }
}
