package com.ascend.user.service;

import com.ascend.analytics.entity.Achievement;
import com.ascend.analytics.repository.AchievementRepository;
import com.ascend.common.entity.Difficulty;
import com.ascend.common.entity.StatType;
import com.ascend.streak.repository.StreakRepository;
import com.ascend.user.dto.StatGainResponse;
import com.ascend.user.dto.StatThresholds;
import com.ascend.user.entity.UserStats;
import com.ascend.user.event.AchievementUnlockedEvent;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Integration test: stat reaches threshold → title unlocked.
 * Verifies that when a stat crosses a threshold, the identity title is
 * created as an Achievement, an event is published, and the response
 * includes the unlocked title name.
 */
@ExtendWith(MockitoExtension.class)
class TitleUnlockIntegrationTest {

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
    @DisplayName("Stat crossing 100 threshold unlocks 'The Beginner' title")
    void awardStatPoints_crossesBeginnerThreshold_unlocksTitleBeginner() {
        // Stat at 99, EASY quest gives +1 → crosses 100
        UserStats existingStats = UserStats.builder()
                .userId(userId)
                .strength(99)
                .wisdom(0)
                .focus(0)
                .discipline(0)
                .vitality(0)
                .charisma(0)
                .lifeScore(BigDecimal.ZERO)
                .build();

        when(userStatsRepository.findByUserId(userId)).thenReturn(Optional.of(existingStats));
        when(userStatsRepository.save(any(UserStats.class))).thenAnswer(inv -> inv.getArgument(0));
        when(streakRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(achievementRepository.existsByUserIdAndAchievementName(eq(userId), any()))
                .thenReturn(false);

        StatGainResponse response = statService.awardStatPoints(userId, StatType.STRENGTH, Difficulty.EASY);

        assertThat(response.getNewValue()).isEqualTo(100);
        assertThat(response.getTitleUnlocked()).isEqualTo("The Beginner");

        // Verify achievement was persisted
        ArgumentCaptor<Achievement> achievementCaptor = ArgumentCaptor.forClass(Achievement.class);
        verify(achievementRepository).save(achievementCaptor.capture());

        Achievement savedAchievement = achievementCaptor.getValue();
        assertThat(savedAchievement.getUserId()).isEqualTo(userId);
        assertThat(savedAchievement.getAchievementName()).isEqualTo("IDENTITY_TITLE_STRENGTH_100");
        assertThat(savedAchievement.getAchievementType()).isEqualTo("IDENTITY_TITLE");

        // Verify event was published
        ArgumentCaptor<AchievementUnlockedEvent> eventCaptor =
                ArgumentCaptor.forClass(AchievementUnlockedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        AchievementUnlockedEvent event = eventCaptor.getValue();
        assertThat(event.getUserId()).isEqualTo(userId);
        assertThat(event.getStatType()).isEqualTo(StatType.STRENGTH);
        assertThat(event.getThreshold()).isEqualTo(100);
        assertThat(event.getTitleName()).isEqualTo("The Beginner");
    }

    @Test
    @DisplayName("Stat crossing 250 threshold unlocks 'The Dedicated' title")
    void awardStatPoints_crossesDedicatedThreshold_unlocksTitleDedicated() {
        // Stat at 249, HARD quest gives +2 → crosses 250
        UserStats existingStats = UserStats.builder()
                .userId(userId)
                .strength(0)
                .wisdom(249)
                .focus(0)
                .discipline(0)
                .vitality(0)
                .charisma(0)
                .lifeScore(BigDecimal.ZERO)
                .build();

        when(userStatsRepository.findByUserId(userId)).thenReturn(Optional.of(existingStats));
        when(userStatsRepository.save(any(UserStats.class))).thenAnswer(inv -> inv.getArgument(0));
        when(streakRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(achievementRepository.existsByUserIdAndAchievementName(eq(userId), any()))
                .thenReturn(false);

        StatGainResponse response = statService.awardStatPoints(userId, StatType.WISDOM, Difficulty.HARD);

        assertThat(response.getNewValue()).isEqualTo(251);
        assertThat(response.getTitleUnlocked()).isEqualTo("The Dedicated");

        verify(achievementRepository).save(any(Achievement.class));
        verify(eventPublisher).publishEvent(any(AchievementUnlockedEvent.class));
    }

    @Test
    @DisplayName("Stat crossing 500 threshold unlocks stat-specific specialist title")
    void awardStatPoints_crossesSpecialistThreshold_unlocksSpecialistTitle() {
        // Focus at 498, LEGENDARY quest gives +3 → crosses 500
        UserStats existingStats = UserStats.builder()
                .userId(userId)
                .strength(0)
                .wisdom(0)
                .focus(498)
                .discipline(0)
                .vitality(0)
                .charisma(0)
                .lifeScore(BigDecimal.ZERO)
                .build();

        when(userStatsRepository.findByUserId(userId)).thenReturn(Optional.of(existingStats));
        when(userStatsRepository.save(any(UserStats.class))).thenAnswer(inv -> inv.getArgument(0));
        when(streakRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(achievementRepository.existsByUserIdAndAchievementName(eq(userId), any()))
                .thenReturn(false);

        StatGainResponse response = statService.awardStatPoints(userId, StatType.FOCUS, Difficulty.LEGENDARY);

        assertThat(response.getNewValue()).isEqualTo(501);
        assertThat(response.getTitleUnlocked()).isEqualTo("The Focused One");

        ArgumentCaptor<Achievement> achievementCaptor = ArgumentCaptor.forClass(Achievement.class);
        verify(achievementRepository).save(achievementCaptor.capture());

        Achievement savedAchievement = achievementCaptor.getValue();
        assertThat(savedAchievement.getAchievementName()).isEqualTo("IDENTITY_TITLE_FOCUS_500");
    }

    @Test
    @DisplayName("Stat crossing 1000 threshold unlocks 'The Master' title")
    void awardStatPoints_crossesMasterThreshold_unlocksTitleMaster() {
        // Discipline at 999, HARD quest gives +2 → crosses 1000
        UserStats existingStats = UserStats.builder()
                .userId(userId)
                .strength(0)
                .wisdom(0)
                .focus(0)
                .discipline(999)
                .vitality(0)
                .charisma(0)
                .lifeScore(BigDecimal.ZERO)
                .build();

        when(userStatsRepository.findByUserId(userId)).thenReturn(Optional.of(existingStats));
        when(userStatsRepository.save(any(UserStats.class))).thenAnswer(inv -> inv.getArgument(0));
        when(streakRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(achievementRepository.existsByUserIdAndAchievementName(eq(userId), any()))
                .thenReturn(false);

        StatGainResponse response = statService.awardStatPoints(userId, StatType.DISCIPLINE, Difficulty.HARD);

        assertThat(response.getNewValue()).isEqualTo(1001);
        assertThat(response.getTitleUnlocked()).isEqualTo("The Master");
    }

    @Test
    @DisplayName("Already unlocked title is not duplicated")
    void awardStatPoints_titleAlreadyUnlocked_noDuplicate() {
        // Stat at 99, will cross 100 but title already exists
        UserStats existingStats = UserStats.builder()
                .userId(userId)
                .strength(0)
                .wisdom(0)
                .focus(0)
                .discipline(0)
                .vitality(99)
                .charisma(0)
                .lifeScore(BigDecimal.ZERO)
                .build();

        when(userStatsRepository.findByUserId(userId)).thenReturn(Optional.of(existingStats));
        when(userStatsRepository.save(any(UserStats.class))).thenAnswer(inv -> inv.getArgument(0));
        when(streakRepository.findByUserId(userId)).thenReturn(Optional.empty());
        // Title already exists
        when(achievementRepository.existsByUserIdAndAchievementName(userId, "IDENTITY_TITLE_VITALITY_100"))
                .thenReturn(true);

        StatGainResponse response = statService.awardStatPoints(userId, StatType.VITALITY, Difficulty.EASY);

        assertThat(response.getNewValue()).isEqualTo(100);
        assertThat(response.getTitleUnlocked()).isNull();

        // Verify no new achievement was saved
        verify(achievementRepository, never()).save(any(Achievement.class));
        // Verify no event was published
        verify(eventPublisher, never()).publishEvent(any(AchievementUnlockedEvent.class));
    }

    @Test
    @DisplayName("No title unlocked when stat does not cross any threshold")
    void awardStatPoints_noThresholdCrossed_noTitleUnlocked() {
        UserStats existingStats = UserStats.builder()
                .userId(userId)
                .strength(0)
                .wisdom(0)
                .focus(0)
                .discipline(0)
                .vitality(0)
                .charisma(50)
                .lifeScore(BigDecimal.ZERO)
                .build();

        when(userStatsRepository.findByUserId(userId)).thenReturn(Optional.of(existingStats));
        when(userStatsRepository.save(any(UserStats.class))).thenAnswer(inv -> inv.getArgument(0));
        when(streakRepository.findByUserId(userId)).thenReturn(Optional.empty());

        StatGainResponse response = statService.awardStatPoints(userId, StatType.CHARISMA, Difficulty.EASY);

        assertThat(response.getNewValue()).isEqualTo(51);
        assertThat(response.getTitleUnlocked()).isNull();

        verify(achievementRepository, never()).save(any(Achievement.class));
        verify(eventPublisher, never()).publishEvent(any(AchievementUnlockedEvent.class));
    }

    @Test
    @DisplayName("Title unlock uses correct stat-specific name for each stat type at 500")
    void awardStatPoints_specialistThreshold_usesCorrectStatSpecificTitle() {
        // Charisma at 498, LEGENDARY gives +3 → crosses 500
        UserStats existingStats = UserStats.builder()
                .userId(userId)
                .strength(0)
                .wisdom(0)
                .focus(0)
                .discipline(0)
                .vitality(0)
                .charisma(498)
                .lifeScore(BigDecimal.ZERO)
                .build();

        when(userStatsRepository.findByUserId(userId)).thenReturn(Optional.of(existingStats));
        when(userStatsRepository.save(any(UserStats.class))).thenAnswer(inv -> inv.getArgument(0));
        when(streakRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(achievementRepository.existsByUserIdAndAchievementName(eq(userId), any()))
                .thenReturn(false);

        StatGainResponse response = statService.awardStatPoints(userId, StatType.CHARISMA, Difficulty.LEGENDARY);

        assertThat(response.getTitleUnlocked()).isEqualTo("The Charismatic One");
    }
}
