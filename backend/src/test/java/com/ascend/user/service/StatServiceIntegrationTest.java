package com.ascend.user.service;

import com.ascend.analytics.entity.Achievement;
import com.ascend.analytics.repository.AchievementRepository;
import com.ascend.common.entity.Difficulty;
import com.ascend.common.entity.StatType;
import com.ascend.streak.entity.Streak;
import com.ascend.streak.repository.StreakRepository;
import com.ascend.user.dto.StatGainResponse;
import com.ascend.user.dto.StatThresholds;
import com.ascend.user.entity.UserStats;
import com.ascend.user.repository.UserStatsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration test: complete quest → stat increases by correct amount.
 * Verifies the full flow from awardStatPoints through stat increment,
 * life score recalculation, and persistence.
 */
@ExtendWith(MockitoExtension.class)
class StatServiceIntegrationTest {

    @Mock
    private UserStatsRepository userStatsRepository;

    @Mock
    private StreakRepository streakRepository;

    @Mock
    private AchievementRepository achievementRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private StatService statService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        statService = new StatService(
                userStatsRepository, streakRepository, achievementRepository, eventPublisher);
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Completing an EASY quest increases the stat by 1 point")
    void awardStatPoints_easyQuest_increasesByOne() {
        UserStats existingStats = UserStats.builder()
                .userId(userId)
                .strength(10)
                .wisdom(5)
                .focus(8)
                .discipline(12)
                .vitality(3)
                .charisma(7)
                .lifeScore(BigDecimal.ZERO)
                .build();

        when(userStatsRepository.findByUserId(userId)).thenReturn(Optional.of(existingStats));
        when(userStatsRepository.save(any(UserStats.class))).thenAnswer(inv -> inv.getArgument(0));
        when(streakRepository.findByUserId(userId)).thenReturn(Optional.empty());

        StatGainResponse response = statService.awardStatPoints(userId, StatType.STRENGTH, Difficulty.EASY);

        assertThat(response.getStatType()).isEqualTo(StatType.STRENGTH);
        assertThat(response.getPreviousValue()).isEqualTo(10);
        assertThat(response.getNewValue()).isEqualTo(11);
        assertThat(response.getGain()).isEqualTo(1);
    }

    @Test
    @DisplayName("Completing a MEDIUM quest increases the stat by 1 point (floor of 1.5)")
    void awardStatPoints_mediumQuest_increasesByOne() {
        UserStats existingStats = UserStats.builder()
                .userId(userId)
                .strength(20)
                .wisdom(5)
                .focus(8)
                .discipline(12)
                .vitality(3)
                .charisma(7)
                .lifeScore(BigDecimal.ZERO)
                .build();

        when(userStatsRepository.findByUserId(userId)).thenReturn(Optional.of(existingStats));
        when(userStatsRepository.save(any(UserStats.class))).thenAnswer(inv -> inv.getArgument(0));
        when(streakRepository.findByUserId(userId)).thenReturn(Optional.empty());

        StatGainResponse response = statService.awardStatPoints(userId, StatType.STRENGTH, Difficulty.MEDIUM);

        assertThat(response.getStatType()).isEqualTo(StatType.STRENGTH);
        assertThat(response.getPreviousValue()).isEqualTo(20);
        assertThat(response.getNewValue()).isEqualTo(21);
        assertThat(response.getGain()).isEqualTo(1);
    }

    @Test
    @DisplayName("Completing a HARD quest increases the stat by 2 points")
    void awardStatPoints_hardQuest_increasesByTwo() {
        UserStats existingStats = UserStats.builder()
                .userId(userId)
                .strength(0)
                .wisdom(5)
                .focus(30)
                .discipline(12)
                .vitality(3)
                .charisma(7)
                .lifeScore(BigDecimal.ZERO)
                .build();

        when(userStatsRepository.findByUserId(userId)).thenReturn(Optional.of(existingStats));
        when(userStatsRepository.save(any(UserStats.class))).thenAnswer(inv -> inv.getArgument(0));
        when(streakRepository.findByUserId(userId)).thenReturn(Optional.empty());

        StatGainResponse response = statService.awardStatPoints(userId, StatType.FOCUS, Difficulty.HARD);

        assertThat(response.getStatType()).isEqualTo(StatType.FOCUS);
        assertThat(response.getPreviousValue()).isEqualTo(30);
        assertThat(response.getNewValue()).isEqualTo(32);
        assertThat(response.getGain()).isEqualTo(2);
    }

    @Test
    @DisplayName("Completing a LEGENDARY quest increases the stat by 3 points")
    void awardStatPoints_legendaryQuest_increasesByThree() {
        UserStats existingStats = UserStats.builder()
                .userId(userId)
                .strength(0)
                .wisdom(50)
                .focus(8)
                .discipline(12)
                .vitality(3)
                .charisma(7)
                .lifeScore(BigDecimal.ZERO)
                .build();

        when(userStatsRepository.findByUserId(userId)).thenReturn(Optional.of(existingStats));
        when(userStatsRepository.save(any(UserStats.class))).thenAnswer(inv -> inv.getArgument(0));
        when(streakRepository.findByUserId(userId)).thenReturn(Optional.empty());

        StatGainResponse response = statService.awardStatPoints(userId, StatType.WISDOM, Difficulty.LEGENDARY);

        assertThat(response.getStatType()).isEqualTo(StatType.WISDOM);
        assertThat(response.getPreviousValue()).isEqualTo(50);
        assertThat(response.getNewValue()).isEqualTo(53);
        assertThat(response.getGain()).isEqualTo(3);
    }

    @Test
    @DisplayName("Stat gain persists updated stats to repository")
    void awardStatPoints_persistsUpdatedStats() {
        UserStats existingStats = UserStats.builder()
                .userId(userId)
                .strength(10)
                .wisdom(5)
                .focus(8)
                .discipline(12)
                .vitality(3)
                .charisma(7)
                .lifeScore(BigDecimal.ZERO)
                .build();

        when(userStatsRepository.findByUserId(userId)).thenReturn(Optional.of(existingStats));
        when(userStatsRepository.save(any(UserStats.class))).thenAnswer(inv -> inv.getArgument(0));
        when(streakRepository.findByUserId(userId)).thenReturn(Optional.empty());

        statService.awardStatPoints(userId, StatType.DISCIPLINE, Difficulty.HARD);

        ArgumentCaptor<UserStats> statsCaptor = ArgumentCaptor.forClass(UserStats.class);
        verify(userStatsRepository).save(statsCaptor.capture());

        UserStats savedStats = statsCaptor.getValue();
        assertThat(savedStats.getDiscipline()).isEqualTo(14); // 12 + 2
        assertThat(savedStats.getLifeScore()).isNotNull();
    }

    @Test
    @DisplayName("Life score is recalculated after stat gain")
    void awardStatPoints_recalculatesLifeScore() {
        Streak streak = Streak.builder()
                .userId(userId)
                .currentStreak(15)
                .build();

        UserStats existingStats = UserStats.builder()
                .userId(userId)
                .strength(100)
                .wisdom(80)
                .focus(60)
                .discipline(90)
                .vitality(70)
                .charisma(50)
                .lifeScore(BigDecimal.ZERO)
                .build();

        when(userStatsRepository.findByUserId(userId)).thenReturn(Optional.of(existingStats));
        when(userStatsRepository.save(any(UserStats.class))).thenAnswer(inv -> inv.getArgument(0));
        when(streakRepository.findByUserId(userId)).thenReturn(Optional.of(streak));

        statService.awardStatPoints(userId, StatType.VITALITY, Difficulty.EASY);

        ArgumentCaptor<UserStats> statsCaptor = ArgumentCaptor.forClass(UserStats.class);
        verify(userStatsRepository).save(statsCaptor.capture());

        UserStats savedStats = statsCaptor.getValue();
        assertThat(savedStats.getLifeScore()).isNotNull();
        assertThat(savedStats.getLifeScore().compareTo(BigDecimal.ZERO)).isGreaterThan(0);
    }

    @Test
    @DisplayName("New user gets default stats created when awarding points")
    void awardStatPoints_newUser_createsDefaultStats() {
        when(userStatsRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userStatsRepository.save(any(UserStats.class))).thenAnswer(inv -> inv.getArgument(0));
        when(streakRepository.findByUserId(userId)).thenReturn(Optional.empty());

        StatGainResponse response = statService.awardStatPoints(userId, StatType.CHARISMA, Difficulty.EASY);

        assertThat(response.getPreviousValue()).isEqualTo(0);
        assertThat(response.getNewValue()).isEqualTo(1);
        assertThat(response.getGain()).isEqualTo(1);
    }

    @Test
    @DisplayName("Each stat type is independently incremented")
    void awardStatPoints_differentStatTypes_incrementIndependently() {
        UserStats existingStats = UserStats.builder()
                .userId(userId)
                .strength(10)
                .wisdom(20)
                .focus(30)
                .discipline(40)
                .vitality(50)
                .charisma(60)
                .lifeScore(BigDecimal.ZERO)
                .build();

        when(userStatsRepository.findByUserId(userId)).thenReturn(Optional.of(existingStats));
        when(userStatsRepository.save(any(UserStats.class))).thenAnswer(inv -> inv.getArgument(0));
        when(streakRepository.findByUserId(userId)).thenReturn(Optional.empty());

        StatGainResponse response = statService.awardStatPoints(userId, StatType.CHARISMA, Difficulty.LEGENDARY);

        assertThat(response.getStatType()).isEqualTo(StatType.CHARISMA);
        assertThat(response.getPreviousValue()).isEqualTo(60);
        assertThat(response.getNewValue()).isEqualTo(63);
        // Other stats remain unchanged
        assertThat(existingStats.getStrength()).isEqualTo(10);
        assertThat(existingStats.getWisdom()).isEqualTo(20);
        assertThat(existingStats.getFocus()).isEqualTo(30);
        assertThat(existingStats.getDiscipline()).isEqualTo(40);
        assertThat(existingStats.getVitality()).isEqualTo(50);
    }
}
