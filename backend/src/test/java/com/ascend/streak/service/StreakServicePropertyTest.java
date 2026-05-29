package com.ascend.streak.service;

import com.ascend.common.entity.Frequency;
import com.ascend.quest.entity.Quest;
import com.ascend.quest.entity.QuestCompletion;
import com.ascend.quest.repository.QuestCompletionRepository;
import com.ascend.quest.repository.QuestRepository;
import com.ascend.streak.entity.Streak;
import com.ascend.streak.repository.StreakRepository;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.lifecycle.BeforeProperty;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for StreakService.
 * Validates streak tracking invariants, reset behavior, and shield auto-activation.
 *
 * **Validates: Requirements 1.2**
 */
class StreakServicePropertyTest {

    @Mock
    private StreakRepository streakRepository;

    @Mock
    private QuestRepository questRepository;

    @Mock
    private QuestCompletionRepository questCompletionRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ComebackModeService comebackModeService;

    private StreakService streakService;

    @BeforeProperty
    void setUp() {
        MockitoAnnotations.openMocks(this);
        streakService = new StreakService(
                streakRepository,
                questRepository,
                questCompletionRepository,
                eventPublisher,
                comebackModeService
        );
    }

    // ========================================================================
    // Property 9: Streak tracking invariant (consecutive days >= 80%)
    // If completed/assigned >= 0.8, then streak should increment.
    // ========================================================================

    @Property(tries = 100)
    void streakIncrementsWhenCompletionAtOrAboveThreshold(
            @ForAll @IntRange(min = 1, max = 20) int assignedQuests,
            @ForAll @IntRange(min = 0, max = 100) int currentStreak) {

        // Calculate minimum completions needed to meet 80% threshold
        int minCompletions = (int) Math.ceil(assignedQuests * 0.8);

        // Use exactly the minimum completions needed (always >= 80%)
        int completedQuests = minCompletions;

        UUID userId = UUID.randomUUID();

        // Set up streak entity
        Streak streak = Streak.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .currentStreak(currentStreak)
                .longestStreak(Math.max(currentStreak, 10))
                .comboMultiplier(BigDecimal.valueOf(Math.min(1.0 + 0.01 * currentStreak, 2.0)))
                .shieldAvailable(false)
                .build();

        // Mock repository: return the streak
        when(streakRepository.findByUserId(userId)).thenReturn(Optional.of(streak));
        when(streakRepository.save(any(Streak.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock quest repositories: assigned daily quests
        List<Quest> dailyQuests = createDailyQuests(assignedQuests);
        when(questRepository.findByFrequencyAndCustomFalse(Frequency.DAILY)).thenReturn(dailyQuests);
        when(questRepository.findByCreatedBy_Id(userId)).thenReturn(Collections.emptyList());

        // Mock completions
        List<QuestCompletion> completions = createCompletions(completedQuests, userId);
        when(questCompletionRepository.findByUserIdAndCompletedAtBetween(eq(userId), any(), any()))
                .thenReturn(completions);

        // Execute
        streakService.evaluateDailyStreak(userId);

        // Verify: streak should have been incremented
        assertThat(streak.getCurrentStreak())
                .as("Streak should increment when completion (%d/%d = %.2f) >= 80%%",
                        completedQuests, assignedQuests, (double) completedQuests / assignedQuests)
                .isEqualTo(currentStreak + 1);
    }

    // ========================================================================
    // Property 10: Streak reset without shield
    // If completion < 80% and shield is NOT available, streak resets to 0.
    // ========================================================================

    @Property(tries = 100)
    void streakResetsToZeroWhenBelowThresholdWithoutShield(
            @ForAll @IntRange(min = 1, max = 20) int assignedQuests,
            @ForAll @IntRange(min = 1, max = 100) int currentStreak) {

        // Calculate completions that are strictly below 80% threshold
        int maxCompletionsBelow = (int) Math.ceil(assignedQuests * 0.8) - 1;
        int completedQuests = Math.max(0, maxCompletionsBelow);

        // Verify we're actually below threshold
        double completionRate = (double) completedQuests / assignedQuests;
        if (completionRate >= 0.8) {
            // Edge case: if assignedQuests is 1, ceil(0.8) - 1 = 0, which is 0/1 = 0.0 < 0.8
            // This should still be fine, but let's force 0 completions
            completedQuests = 0;
        }

        UUID userId = UUID.randomUUID();

        // Set up streak entity with NO shield
        Streak streak = Streak.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .currentStreak(currentStreak)
                .longestStreak(Math.max(currentStreak, 10))
                .comboMultiplier(BigDecimal.valueOf(Math.min(1.0 + 0.01 * currentStreak, 2.0)))
                .shieldAvailable(false)
                .build();

        // Mock repository
        when(streakRepository.findByUserId(userId)).thenReturn(Optional.of(streak));
        when(streakRepository.save(any(Streak.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock quest repositories
        List<Quest> dailyQuests = createDailyQuests(assignedQuests);
        when(questRepository.findByFrequencyAndCustomFalse(Frequency.DAILY)).thenReturn(dailyQuests);
        when(questRepository.findByCreatedBy_Id(userId)).thenReturn(Collections.emptyList());

        // Mock completions (below threshold)
        List<QuestCompletion> completions = createCompletions(completedQuests, userId);
        when(questCompletionRepository.findByUserIdAndCompletedAtBetween(eq(userId), any(), any()))
                .thenReturn(completions);

        // Execute
        streakService.evaluateDailyStreak(userId);

        // Verify: streak should be reset to 0
        assertThat(streak.getCurrentStreak())
                .as("Streak should reset to 0 when completion (%d/%d = %.2f) < 80%% and no shield",
                        completedQuests, assignedQuests, (double) completedQuests / assignedQuests)
                .isEqualTo(0);

        // Verify: combo multiplier should be reset to 1.0
        assertThat(streak.getComboMultiplier())
                .as("Combo multiplier should reset to 1.0 when streak breaks")
                .isEqualByComparingTo(BigDecimal.ONE);

        // Verify: comeback mode should be activated
        verify(comebackModeService).activateComebackMode(userId);
    }

    // ========================================================================
    // Property 12: Shield auto-activation preserves streak
    // If completion < 80% and shield IS available, streak is preserved.
    // ========================================================================

    @Property(tries = 100)
    void shieldAutoActivationPreservesStreak(
            @ForAll @IntRange(min = 1, max = 20) int assignedQuests,
            @ForAll @IntRange(min = 1, max = 100) int currentStreak) {

        // Calculate completions that are strictly below 80% threshold
        int maxCompletionsBelow = (int) Math.ceil(assignedQuests * 0.8) - 1;
        int completedQuests = Math.max(0, maxCompletionsBelow);

        // Verify we're actually below threshold
        double completionRate = (double) completedQuests / assignedQuests;
        if (completionRate >= 0.8) {
            completedQuests = 0;
        }

        UUID userId = UUID.randomUUID();

        // Set up streak entity WITH shield available
        Streak streak = Streak.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .currentStreak(currentStreak)
                .longestStreak(Math.max(currentStreak, 10))
                .comboMultiplier(BigDecimal.valueOf(Math.min(1.0 + 0.01 * currentStreak, 2.0)))
                .shieldAvailable(true)
                .build();

        // Mock repository
        when(streakRepository.findByUserId(userId)).thenReturn(Optional.of(streak));
        when(streakRepository.save(any(Streak.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock quest repositories
        List<Quest> dailyQuests = createDailyQuests(assignedQuests);
        when(questRepository.findByFrequencyAndCustomFalse(Frequency.DAILY)).thenReturn(dailyQuests);
        when(questRepository.findByCreatedBy_Id(userId)).thenReturn(Collections.emptyList());

        // Mock completions (below threshold)
        List<QuestCompletion> completions = createCompletions(completedQuests, userId);
        when(questCompletionRepository.findByUserIdAndCompletedAtBetween(eq(userId), any(), any()))
                .thenReturn(completions);

        // Execute
        streakService.evaluateDailyStreak(userId);

        // Verify: streak should be preserved (NOT reset to 0)
        assertThat(streak.getCurrentStreak())
                .as("Streak should be preserved at %d when shield is available", currentStreak)
                .isEqualTo(currentStreak);

        // Verify: shield should be consumed (set to false)
        assertThat(streak.getShieldAvailable())
                .as("Shield should be consumed after auto-activation")
                .isFalse();

        // Verify: comeback mode should NOT be activated
        verify(comebackModeService, never()).activateComebackMode(userId);
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    /**
     * Creates a list of daily Quest objects for mocking.
     */
    private List<Quest> createDailyQuests(int count) {
        List<Quest> quests = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Quest quest = Quest.builder()
                    .id(UUID.randomUUID())
                    .title("Daily Quest " + i)
                    .frequency(Frequency.DAILY)
                    .custom(false)
                    .build();
            quests.add(quest);
        }
        return quests;
    }

    /**
     * Creates a list of QuestCompletion objects for mocking.
     */
    private List<QuestCompletion> createCompletions(int count, UUID userId) {
        List<QuestCompletion> completions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            QuestCompletion completion = QuestCompletion.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .questId(UUID.randomUUID())
                    .completedAt(LocalDateTime.now())
                    .xpEarned(50)
                    .build();
            completions.add(completion);
        }
        return completions;
    }
}
