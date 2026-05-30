package com.ascend.user.service;

import com.ascend.common.entity.StatType;
import com.ascend.quest.repository.QuestCompletionRepository;
import com.ascend.user.entity.User;
import com.ascend.user.entity.UserStats;
import com.ascend.user.repository.UserRepository;
import com.ascend.user.repository.UserStatsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Integration test: Hard Mode decay applies after 7 days inactivity.
 * Verifies that stat decay is correctly applied for Hard Mode users
 * who have not completed quests for specific stat types within 7 days.
 */
@ExtendWith(MockitoExtension.class)
class StatDecayIntegrationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserStatsRepository userStatsRepository;

    @Mock
    private QuestCompletionRepository questCompletionRepository;

    private StatDecayService statDecayService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        statDecayService = new StatDecayService(
                userRepository, userStatsRepository, questCompletionRepository);
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Hard Mode user with no activity in 7 days gets all stats decayed by 5")
    void evaluateStatDecay_noActivityIn7Days_allStatsDecayedBy5() {
        User hardModeUser = User.builder()
                .id(userId)
                .firebaseUid("firebase-123")
                .hardMode(true)
                .build();

        UserStats existingStats = UserStats.builder()
                .userId(userId)
                .strength(50)
                .wisdom(40)
                .focus(30)
                .discipline(20)
                .vitality(10)
                .charisma(60)
                .lifeScore(BigDecimal.ZERO)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(hardModeUser));
        when(userStatsRepository.findByUserId(userId)).thenReturn(Optional.of(existingStats));
        // No quests completed for any stat type in the last 7 days
        when(questCompletionRepository.findDistinctStatTypesCompletedSince(eq(userId), any(LocalDateTime.class)))
                .thenReturn(List.of());

        Set<StatType> decayedStats = statDecayService.evaluateStatDecay(userId);

        // All 6 stat types should be decayed
        assertThat(decayedStats).hasSize(6);
        assertThat(decayedStats).containsExactlyInAnyOrder(
                StatType.STRENGTH, StatType.WISDOM, StatType.FOCUS,
                StatType.DISCIPLINE, StatType.VITALITY, StatType.CHARISMA);

        // Verify stats were reduced by 5
        assertThat(existingStats.getStrength()).isEqualTo(45);
        assertThat(existingStats.getWisdom()).isEqualTo(35);
        assertThat(existingStats.getFocus()).isEqualTo(25);
        assertThat(existingStats.getDiscipline()).isEqualTo(15);
        assertThat(existingStats.getVitality()).isEqualTo(5);
        assertThat(existingStats.getCharisma()).isEqualTo(55);

        verify(userStatsRepository).save(existingStats);
    }

    @Test
    @DisplayName("Hard Mode user with partial activity only decays inactive stats")
    void evaluateStatDecay_partialActivity_onlyInactiveStatsDecayed() {
        User hardModeUser = User.builder()
                .id(userId)
                .firebaseUid("firebase-456")
                .hardMode(true)
                .build();

        UserStats existingStats = UserStats.builder()
                .userId(userId)
                .strength(50)
                .wisdom(40)
                .focus(30)
                .discipline(20)
                .vitality(10)
                .charisma(60)
                .lifeScore(BigDecimal.ZERO)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(hardModeUser));
        when(userStatsRepository.findByUserId(userId)).thenReturn(Optional.of(existingStats));
        // User completed quests for STRENGTH and FOCUS in the last 7 days
        when(questCompletionRepository.findDistinctStatTypesCompletedSince(eq(userId), any(LocalDateTime.class)))
                .thenReturn(List.of(StatType.STRENGTH, StatType.FOCUS));

        Set<StatType> decayedStats = statDecayService.evaluateStatDecay(userId);

        // Only inactive stats should be decayed
        assertThat(decayedStats).doesNotContain(StatType.STRENGTH, StatType.FOCUS);
        assertThat(decayedStats).contains(StatType.WISDOM, StatType.DISCIPLINE, StatType.VITALITY, StatType.CHARISMA);

        // Active stats remain unchanged
        assertThat(existingStats.getStrength()).isEqualTo(50);
        assertThat(existingStats.getFocus()).isEqualTo(30);

        // Inactive stats decayed by 5
        assertThat(existingStats.getWisdom()).isEqualTo(35);
        assertThat(existingStats.getDiscipline()).isEqualTo(15);
        assertThat(existingStats.getVitality()).isEqualTo(5);
        assertThat(existingStats.getCharisma()).isEqualTo(55);
    }

    @Test
    @DisplayName("Stat decay never goes below 0")
    void evaluateStatDecay_statAtLowValue_neverGoesBelowZero() {
        User hardModeUser = User.builder()
                .id(userId)
                .firebaseUid("firebase-789")
                .hardMode(true)
                .build();

        UserStats existingStats = UserStats.builder()
                .userId(userId)
                .strength(3)  // Less than decay amount (5)
                .wisdom(0)    // Already at 0
                .focus(5)     // Exactly decay amount
                .discipline(100)
                .vitality(2)
                .charisma(1)
                .lifeScore(BigDecimal.ZERO)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(hardModeUser));
        when(userStatsRepository.findByUserId(userId)).thenReturn(Optional.of(existingStats));
        when(questCompletionRepository.findDistinctStatTypesCompletedSince(eq(userId), any(LocalDateTime.class)))
                .thenReturn(List.of());

        Set<StatType> decayedStats = statDecayService.evaluateStatDecay(userId);

        // Stats should be clamped at 0
        assertThat(existingStats.getStrength()).isEqualTo(0);
        assertThat(existingStats.getWisdom()).isEqualTo(0);  // Was already 0, not decayed
        assertThat(existingStats.getFocus()).isEqualTo(0);
        assertThat(existingStats.getDiscipline()).isEqualTo(95);
        assertThat(existingStats.getVitality()).isEqualTo(0);
        assertThat(existingStats.getCharisma()).isEqualTo(0);

        // Wisdom was already at 0, so it should NOT be in the decayed set
        assertThat(decayedStats).doesNotContain(StatType.WISDOM);
        // Others that had values > 0 should be decayed
        assertThat(decayedStats).contains(StatType.STRENGTH, StatType.FOCUS,
                StatType.DISCIPLINE, StatType.VITALITY, StatType.CHARISMA);
    }

    @Test
    @DisplayName("Non-Hard Mode user does not get stat decay")
    void evaluateStatDecay_nonHardModeUser_noDecayApplied() {
        User normalUser = User.builder()
                .id(userId)
                .firebaseUid("firebase-normal")
                .hardMode(false)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(normalUser));

        Set<StatType> decayedStats = statDecayService.evaluateStatDecay(userId);

        assertThat(decayedStats).isEmpty();
        verify(userStatsRepository, never()).findByUserId(any());
        verify(userStatsRepository, never()).save(any());
    }

    @Test
    @DisplayName("User not found returns empty decay set")
    void evaluateStatDecay_userNotFound_returnsEmpty() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Set<StatType> decayedStats = statDecayService.evaluateStatDecay(userId);

        assertThat(decayedStats).isEmpty();
        verify(userStatsRepository, never()).save(any());
    }

    @Test
    @DisplayName("Hard Mode user with all stats active gets no decay")
    void evaluateStatDecay_allStatsActive_noDecay() {
        User hardModeUser = User.builder()
                .id(userId)
                .firebaseUid("firebase-active")
                .hardMode(true)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(hardModeUser));
        // All stat types have been active in the last 7 days
        when(questCompletionRepository.findDistinctStatTypesCompletedSince(eq(userId), any(LocalDateTime.class)))
                .thenReturn(List.of(
                        StatType.STRENGTH, StatType.WISDOM, StatType.FOCUS,
                        StatType.DISCIPLINE, StatType.VITALITY, StatType.CHARISMA));

        Set<StatType> decayedStats = statDecayService.evaluateStatDecay(userId);

        assertThat(decayedStats).isEmpty();
        verify(userStatsRepository, never()).save(any());
    }

    @Test
    @DisplayName("Hard Mode user with no stats record gets no decay")
    void evaluateStatDecay_noStatsRecord_noDecay() {
        User hardModeUser = User.builder()
                .id(userId)
                .firebaseUid("firebase-nostats")
                .hardMode(true)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(hardModeUser));
        when(userStatsRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(questCompletionRepository.findDistinctStatTypesCompletedSince(eq(userId), any(LocalDateTime.class)))
                .thenReturn(List.of());

        Set<StatType> decayedStats = statDecayService.evaluateStatDecay(userId);

        assertThat(decayedStats).isEmpty();
        verify(userStatsRepository, never()).save(any());
    }

    @Test
    @DisplayName("Decay amount is exactly 5 points per inactive stat")
    void evaluateStatDecay_decayAmountIsExactly5() {
        User hardModeUser = User.builder()
                .id(userId)
                .firebaseUid("firebase-exact")
                .hardMode(true)
                .build();

        UserStats existingStats = UserStats.builder()
                .userId(userId)
                .strength(100)
                .wisdom(200)
                .focus(300)
                .discipline(400)
                .vitality(500)
                .charisma(600)
                .lifeScore(BigDecimal.ZERO)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(hardModeUser));
        when(userStatsRepository.findByUserId(userId)).thenReturn(Optional.of(existingStats));
        when(questCompletionRepository.findDistinctStatTypesCompletedSince(eq(userId), any(LocalDateTime.class)))
                .thenReturn(List.of());

        statDecayService.evaluateStatDecay(userId);

        // Each stat should be reduced by exactly 5
        assertThat(existingStats.getStrength()).isEqualTo(95);
        assertThat(existingStats.getWisdom()).isEqualTo(195);
        assertThat(existingStats.getFocus()).isEqualTo(295);
        assertThat(existingStats.getDiscipline()).isEqualTo(395);
        assertThat(existingStats.getVitality()).isEqualTo(495);
        assertThat(existingStats.getCharisma()).isEqualTo(595);
    }
}
