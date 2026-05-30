package com.ascend.xp.service;

import com.ascend.arc.entity.UserArcProgress;
import com.ascend.arc.repository.UserArcProgressRepository;
import com.ascend.common.entity.Difficulty;
import com.ascend.common.entity.StatType;
import com.ascend.quest.event.QuestCompletedEvent;
import com.ascend.skilltree.entity.UserSkill;
import com.ascend.skilltree.repository.UserSkillRepository;
import com.ascend.skilltree.service.SkillBuffCalculator;
import com.ascend.streak.entity.Streak;
import com.ascend.streak.repository.StreakRepository;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import com.ascend.xp.entity.XpHistory;
import com.ascend.xp.event.XpAwardedEvent;
import com.ascend.xp.repository.XpHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class XpServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private StreakRepository streakRepository;
    @Mock
    private UserArcProgressRepository userArcProgressRepository;
    @Mock
    private UserSkillRepository userSkillRepository;
    @Mock
    private XpHistoryRepository xpHistoryRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private SkillBuffCalculator skillBuffCalculator;

    private XpService xpService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        xpService = new XpService(
                userRepository, streakRepository, userArcProgressRepository,
                userSkillRepository, xpHistoryRepository, eventPublisher,
                skillBuffCalculator
        );
        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .level(5)
                .xp(500L)
                .firebaseUid("test-uid")
                .build();
    }

    @Test
    void getDailyCap_returnsCorrectValue() {
        assertEquals(1000, xpService.getDailyCap(0));
        assertEquals(1020, xpService.getDailyCap(1));
        assertEquals(1100, xpService.getDailyCap(5));
        assertEquals(1200, xpService.getDailyCap(10));
    }

    @Test
    void awardXp_basicQuestCompletion_awardsCorrectXp() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(streakRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userArcProgressRepository.findByUserIdAndStatus(userId, "ACTIVE"))
                .thenReturn(Collections.emptyList());
        when(userSkillRepository.findByUserIdAndUnlockedTrue(userId))
                .thenReturn(Collections.emptyList());
        when(xpHistoryRepository.sumXpAmountByUserIdAndCreatedAtBetween(eq(userId), any(), any()))
                .thenReturn(0L);

        QuestCompletedEvent event = new QuestCompletedEvent(
                this, userId, UUID.randomUUID(), "Test Quest",
                Difficulty.EASY, StatType.STRENGTH, 100, LocalDateTime.now()
        );

        // Act
        xpService.awardXp(userId, event);

        // Assert — EASY difficulty multiplier is 1.0, no streak/arc/skill bonuses
        // Final XP = floor(100 * 1.0 * 1.0 * 1.0) + 0 = 100
        ArgumentCaptor<XpHistory> historyCaptor = ArgumentCaptor.forClass(XpHistory.class);
        verify(xpHistoryRepository).save(historyCaptor.capture());
        assertEquals(100, historyCaptor.getValue().getXpAmount());

        verify(userRepository).save(user);
        assertEquals(600L, user.getXp());
    }

    @Test
    void awardXp_withStreakMultiplier_appliesCombo() {
        // Arrange
        Streak streak = Streak.builder().userId(userId).currentStreak(50).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(streakRepository.findByUserId(userId)).thenReturn(Optional.of(streak));
        when(userArcProgressRepository.findByUserIdAndStatus(userId, "ACTIVE"))
                .thenReturn(Collections.emptyList());
        when(userSkillRepository.findByUserIdAndUnlockedTrue(userId))
                .thenReturn(Collections.emptyList());
        when(xpHistoryRepository.sumXpAmountByUserIdAndCreatedAtBetween(eq(userId), any(), any()))
                .thenReturn(0L);

        QuestCompletedEvent event = new QuestCompletedEvent(
                this, userId, UUID.randomUUID(), "Test Quest",
                Difficulty.EASY, StatType.FOCUS, 100, LocalDateTime.now()
        );

        // Act
        xpService.awardXp(userId, event);

        // Assert — streak of 50 days = 1.5 multiplier
        // Final XP = floor(100 * 1.0 * 1.5 * 1.0) + 0 = 150
        ArgumentCaptor<XpHistory> historyCaptor = ArgumentCaptor.forClass(XpHistory.class);
        verify(xpHistoryRepository).save(historyCaptor.capture());
        assertEquals(150, historyCaptor.getValue().getXpAmount());
    }

    @Test
    void awardXp_withArcMultiplier_appliesArcBonus() {
        // Arrange
        UUID arcId = UUID.randomUUID();
        UserArcProgress activeArc = UserArcProgress.builder()
                .userId(userId).arcId(arcId).status("ACTIVE").progressPercent(300).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(streakRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userArcProgressRepository.findByUserIdAndStatus(userId, "ACTIVE"))
                .thenReturn(List.of(activeArc));
        when(userSkillRepository.findByUserIdAndUnlockedTrue(userId))
                .thenReturn(Collections.emptyList());
        when(xpHistoryRepository.sumXpAmountByUserIdAndCreatedAtBetween(eq(userId), any(), any()))
                .thenReturn(0L);

        QuestCompletedEvent event = new QuestCompletedEvent(
                this, userId, UUID.randomUUID(), "Test Quest",
                Difficulty.EASY, StatType.WISDOM, 100, LocalDateTime.now()
        );

        // Act
        xpService.awardXp(userId, event);

        // Assert — progressPercent=300, arcMultiplier = 1.0 + (300/1000.0) = 1.3
        // Final XP = floor(100 * 1.0 * 1.0 * 1.3) + 0 = 130
        ArgumentCaptor<XpHistory> historyCaptor = ArgumentCaptor.forClass(XpHistory.class);
        verify(xpHistoryRepository).save(historyCaptor.capture());
        assertEquals(130, historyCaptor.getValue().getXpAmount());
    }

    @Test
    void awardXp_withSkillBonuses_addsFlatBonus() {
        // Arrange
        List<UserSkill> skills = List.of(
                UserSkill.builder().userId(userId).unlocked(true).skillName("Skill1").skillId(UUID.randomUUID()).build(),
                UserSkill.builder().userId(userId).unlocked(true).skillName("Skill2").skillId(UUID.randomUUID()).build()
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(streakRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userArcProgressRepository.findByUserIdAndStatus(userId, "ACTIVE"))
                .thenReturn(Collections.emptyList());
        when(userSkillRepository.findByUserIdAndUnlockedTrue(userId)).thenReturn(skills);
        when(xpHistoryRepository.sumXpAmountByUserIdAndCreatedAtBetween(eq(userId), any(), any()))
                .thenReturn(0L);

        QuestCompletedEvent event = new QuestCompletedEvent(
                this, userId, UUID.randomUUID(), "Test Quest",
                Difficulty.EASY, StatType.DISCIPLINE, 100, LocalDateTime.now()
        );

        // Act
        xpService.awardXp(userId, event);

        // Assert — 2 unlocked skills × 5 = 10 bonus XP
        // Final XP = floor(100 * 1.0 * 1.0 * 1.0) + 10 = 110
        ArgumentCaptor<XpHistory> historyCaptor = ArgumentCaptor.forClass(XpHistory.class);
        verify(xpHistoryRepository).save(historyCaptor.capture());
        assertEquals(110, historyCaptor.getValue().getXpAmount());
    }

    @Test
    void awardXp_exceedsDailyCap_reducesAward() {
        // Arrange — user already earned 1050 of 1100 cap (level 5)
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(streakRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userArcProgressRepository.findByUserIdAndStatus(userId, "ACTIVE"))
                .thenReturn(Collections.emptyList());
        when(userSkillRepository.findByUserIdAndUnlockedTrue(userId))
                .thenReturn(Collections.emptyList());
        when(xpHistoryRepository.sumXpAmountByUserIdAndCreatedAtBetween(eq(userId), any(), any()))
                .thenReturn(1050L);

        QuestCompletedEvent event = new QuestCompletedEvent(
                this, userId, UUID.randomUUID(), "Test Quest",
                Difficulty.EASY, StatType.VITALITY, 100, LocalDateTime.now()
        );

        // Act
        xpService.awardXp(userId, event);

        // Assert — daily cap for level 5 = 1100, already earned 1050, so only 50 awarded
        ArgumentCaptor<XpHistory> historyCaptor = ArgumentCaptor.forClass(XpHistory.class);
        verify(xpHistoryRepository).save(historyCaptor.capture());
        assertEquals(50, historyCaptor.getValue().getXpAmount());
        assertEquals(550L, user.getXp());
    }

    @Test
    void awardXp_atDailyCap_awardsNothing() {
        // Arrange — user already at cap
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(streakRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userArcProgressRepository.findByUserIdAndStatus(userId, "ACTIVE"))
                .thenReturn(Collections.emptyList());
        when(userSkillRepository.findByUserIdAndUnlockedTrue(userId))
                .thenReturn(Collections.emptyList());
        when(xpHistoryRepository.sumXpAmountByUserIdAndCreatedAtBetween(eq(userId), any(), any()))
                .thenReturn(1100L);

        QuestCompletedEvent event = new QuestCompletedEvent(
                this, userId, UUID.randomUUID(), "Test Quest",
                Difficulty.EASY, StatType.CHARISMA, 100, LocalDateTime.now()
        );

        // Act
        xpService.awardXp(userId, event);

        // Assert — no XP awarded, no history saved
        verify(xpHistoryRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
        assertEquals(500L, user.getXp()); // unchanged
    }

    @Test
    void awardXp_triggersLevelUp_updatesLevel() {
        // Arrange — user at level 1 with enough XP to level up
        user.setLevel(1);
        user.setXp(0L);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(streakRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userArcProgressRepository.findByUserIdAndStatus(userId, "ACTIVE"))
                .thenReturn(Collections.emptyList());
        when(userSkillRepository.findByUserIdAndUnlockedTrue(userId))
                .thenReturn(Collections.emptyList());
        when(xpHistoryRepository.sumXpAmountByUserIdAndCreatedAtBetween(eq(userId), any(), any()))
                .thenReturn(0L);

        // LEGENDARY difficulty = 3.0 multiplier, base 100 = 300 XP
        // Level 1 requires 100 XP, Level 2 requires floor(100 * 2^1.5) = 282
        // Total for level 2 = 100 + 282 = 382, so 300 XP should be level 1
        // Let's use a higher base to trigger level-up
        QuestCompletedEvent event = new QuestCompletedEvent(
                this, userId, UUID.randomUUID(), "Epic Quest",
                Difficulty.LEGENDARY, StatType.STRENGTH, 200, LocalDateTime.now()
        );

        // Act
        xpService.awardXp(userId, event);

        // Assert — 200 * 3.0 = 600 XP, level should increase
        // Level 1 = 100, Level 2 = 282, cumulative = 382, so 600 XP = level 2
        assertTrue(user.getLevel() > 1);
        assertEquals(600L, user.getXp());
    }

    @Test
    void awardXp_publishesXpAwardedEvent() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(streakRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userArcProgressRepository.findByUserIdAndStatus(userId, "ACTIVE"))
                .thenReturn(Collections.emptyList());
        when(userSkillRepository.findByUserIdAndUnlockedTrue(userId))
                .thenReturn(Collections.emptyList());
        when(xpHistoryRepository.sumXpAmountByUserIdAndCreatedAtBetween(eq(userId), any(), any()))
                .thenReturn(0L);

        QuestCompletedEvent event = new QuestCompletedEvent(
                this, userId, UUID.randomUUID(), "Test Quest",
                Difficulty.MEDIUM, StatType.FOCUS, 50, LocalDateTime.now()
        );

        // Act
        xpService.awardXp(userId, event);

        // Assert — event published
        ArgumentCaptor<XpAwardedEvent> eventCaptor = ArgumentCaptor.forClass(XpAwardedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        XpAwardedEvent publishedEvent = eventCaptor.getValue();
        assertEquals(userId, publishedEvent.getUserId());
        assertEquals(StatType.FOCUS, publishedEvent.getStatType());
        // 50 * 1.5 (MEDIUM) * 1.0 * 1.0 + 0 = 75
        assertEquals(75, publishedEvent.getXpAmount());
    }

    @Test
    void awardXp_userNotFound_throwsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        QuestCompletedEvent event = new QuestCompletedEvent(
                this, userId, UUID.randomUUID(), "Test Quest",
                Difficulty.EASY, StatType.STRENGTH, 100, LocalDateTime.now()
        );

        assertThrows(IllegalArgumentException.class, () -> xpService.awardXp(userId, event));
    }
}
