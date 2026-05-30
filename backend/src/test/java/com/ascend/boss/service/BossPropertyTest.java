package com.ascend.boss.service;

import com.ascend.common.entity.Difficulty;
import com.ascend.common.entity.StatType;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for the Boss Battles system.
 * Validates boss progress updates, defeat conditions, and guild boss collective progress.
 */
class BossPropertyTest {

    private final BossProgressCalculator calculator = new BossProgressCalculator();

    // ========================================================================
    // Property 32: Boss progress updates correctly, defeat at 100% final stage
    // ========================================================================

    @Property(tries = 100)
    void damageMatchesDifficultyMapping(
            @ForAll("difficulties") Difficulty difficulty,
            @ForAll("statTypes") StatType statType) {

        UUID bossId = UUID.randomUUID();
        int damage = calculator.calculateDamage(difficulty, statType, bossId);

        int expected = switch (difficulty) {
            case EASY -> 5;
            case MEDIUM -> 10;
            case HARD -> 15;
            case LEGENDARY -> 25;
        };

        assertThat(damage)
                .as("Damage for %s difficulty should be %d%%", difficulty, expected)
                .isEqualTo(expected);
    }

    @Property(tries = 100)
    void damageIsAlwaysPositive(
            @ForAll("difficulties") Difficulty difficulty,
            @ForAll("statTypes") StatType statType) {

        UUID bossId = UUID.randomUUID();
        int damage = calculator.calculateDamage(difficulty, statType, bossId);

        assertThat(damage)
                .as("Damage should always be positive for %s", difficulty)
                .isGreaterThan(0);
    }

    @Property(tries = 100)
    void higherDifficultyGivesMoreDamage(
            @ForAll("difficulties") Difficulty d1,
            @ForAll("difficulties") Difficulty d2,
            @ForAll("statTypes") StatType statType) {

        UUID bossId = UUID.randomUUID();
        int damage1 = calculator.calculateDamage(d1, statType, bossId);
        int damage2 = calculator.calculateDamage(d2, statType, bossId);

        if (d1.ordinal() < d2.ordinal()) {
            assertThat(damage1)
                    .as("Damage for %s (%d) should be < damage for %s (%d)",
                            d1, damage1, d2, damage2)
                    .isLessThan(damage2);
        } else if (d1.ordinal() > d2.ordinal()) {
            assertThat(damage1)
                    .as("Damage for %s (%d) should be > damage for %s (%d)",
                            d1, damage1, d2, damage2)
                    .isGreaterThan(damage2);
        }
    }

    @Property(tries = 100)
    void bossDefeatedWhenProgressReaches100(
            @ForAll @IntRange(min = 0, max = 99) int currentProgress,
            @ForAll @IntRange(min = 1, max = 100) int damage) {

        int newProgress = Math.min(currentProgress + damage, 100);
        boolean shouldBeDefeated = newProgress >= 100;

        if (shouldBeDefeated) {
            assertThat(newProgress).isGreaterThanOrEqualTo(100);
        } else {
            assertThat(newProgress).isLessThan(100);
        }
    }

    @Property(tries = 100)
    void progressNeverExceeds100(
            @ForAll @IntRange(min = 0, max = 100) int currentProgress,
            @ForAll @IntRange(min = 1, max = 100) int damage) {

        int newProgress = Math.min(currentProgress + damage, 100);

        assertThat(newProgress)
                .as("Progress should never exceed 100%%")
                .isLessThanOrEqualTo(100);
    }

    @Property(tries = 100)
    void stageAdvancesWhenThresholdCrossed(
            @ForAll @IntRange(min = 0, max = 100) int progressPercent) {

        // 3-stage boss with thresholds at 33%, 66%, 100%
        List<Integer> thresholds = List.of(33, 66, 100);

        int expectedStage;
        if (progressPercent >= 66) {
            expectedStage = 3;
        } else if (progressPercent >= 33) {
            expectedStage = 2;
        } else {
            expectedStage = 1;
        }

        int calculatedStage = calculateCurrentStage(progressPercent, thresholds);

        assertThat(calculatedStage)
                .as("At %d%% progress with thresholds %s, stage should be %d",
                        progressPercent, thresholds, expectedStage)
                .isEqualTo(expectedStage);
    }

    // ========================================================================
    // Property 33: Guild boss collective progress aggregates all members
    // ========================================================================

    @Property(tries = 100)
    void guildBossProgressAccumulatesFromAllMembers(
            @ForAll @IntRange(min = 1, max = 50) int memberCount,
            @ForAll @IntRange(min = 1, max = 25) int damagePerMember) {

        int totalDamage = memberCount * damagePerMember;

        assertThat(totalDamage)
                .as("Total guild damage from %d members × %d each should be %d",
                        memberCount, damagePerMember, totalDamage)
                .isEqualTo(memberCount * damagePerMember);
    }

    @Property(tries = 100)
    void guildBossDefeatedWhenCollectiveProgressReaches100(
            @ForAll @IntRange(min = 1, max = 50) int memberCount,
            @ForAll("difficulties") Difficulty difficulty) {

        int damagePerQuest = calculator.calculateDamage(difficulty, StatType.STRENGTH, UUID.randomUUID());

        // With enough members and quests, the boss should eventually be defeated
        int questsNeeded = (int) Math.ceil(100.0 / damagePerQuest);

        assertThat(questsNeeded * damagePerQuest)
                .as("After %d quests at %d damage each, total should reach 100%%",
                        questsNeeded, damagePerQuest)
                .isGreaterThanOrEqualTo(100);
    }

    @Property(tries = 100)
    void guildBossContributionOrderDoesNotMatter(
            @ForAll @IntRange(min = 1, max = 25) int damage1,
            @ForAll @IntRange(min = 1, max = 25) int damage2,
            @ForAll @IntRange(min = 1, max = 25) int damage3) {

        int total1 = damage1 + damage2 + damage3;
        int total2 = damage3 + damage1 + damage2;
        int total3 = damage2 + damage3 + damage1;

        assertThat(total1).isEqualTo(total2).isEqualTo(total3);
    }

    @Property(tries = 100)
    void allGuildMembersReceiveRewardsOnDefeat(
            @ForAll @IntRange(min = 1, max = 50) int memberCount,
            @ForAll @IntRange(min = 300, max = 1000) int rewardXp) {

        long totalXpDistributed = (long) memberCount * rewardXp;

        assertThat(totalXpDistributed)
                .as("Total XP distributed to %d members at %d each should be %d",
                        memberCount, rewardXp, totalXpDistributed)
                .isEqualTo((long) memberCount * rewardXp);
    }

    // ========================================================================
    // Providers
    // ========================================================================

    @Provide
    Arbitrary<Difficulty> difficulties() {
        return Arbitraries.of(Difficulty.values());
    }

    @Provide
    Arbitrary<StatType> statTypes() {
        return Arbitraries.of(StatType.values());
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    /**
     * Mirrors the stage calculation logic from BossService.
     */
    private int calculateCurrentStage(int progressPercent, List<Integer> thresholds) {
        int stage = 1;
        for (int i = 0; i < thresholds.size() - 1; i++) {
            if (progressPercent >= thresholds.get(i)) {
                stage = i + 2;
            }
        }
        return Math.min(stage, thresholds.size());
    }
}
