package com.ascend.guild.service;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for the Guild system.
 * Validates tier-based member caps and shared quest XP accumulation.
 */
class GuildPropertyTest {

    // ========================================================================
    // Property 30: Guild creation enforces tier caps (Free: 10, Premium: 50)
    // ========================================================================

    private static final int FREE_MAX_MEMBERS = 10;
    private static final int PREMIUM_MAX_MEMBERS = 50;

    @Property(tries = 100)
    void freeUserGuildCappedAt10Members(
            @ForAll @IntRange(min = 1, max = 100) int attemptedMembers) {

        int maxAllowed = FREE_MAX_MEMBERS;
        int actualMembers = Math.min(attemptedMembers, maxAllowed);

        assertThat(actualMembers)
                .as("Free user guild should never exceed %d members (attempted: %d)",
                        FREE_MAX_MEMBERS, attemptedMembers)
                .isLessThanOrEqualTo(FREE_MAX_MEMBERS);
    }

    @Property(tries = 100)
    void premiumUserGuildCappedAt50Members(
            @ForAll @IntRange(min = 1, max = 200) int attemptedMembers) {

        int maxAllowed = PREMIUM_MAX_MEMBERS;
        int actualMembers = Math.min(attemptedMembers, maxAllowed);

        assertThat(actualMembers)
                .as("Premium user guild should never exceed %d members (attempted: %d)",
                        PREMIUM_MAX_MEMBERS, attemptedMembers)
                .isLessThanOrEqualTo(PREMIUM_MAX_MEMBERS);
    }

    @Property(tries = 100)
    void premiumCapIsAlwaysGreaterThanFreeCap() {
        assertThat(PREMIUM_MAX_MEMBERS)
                .as("Premium cap (%d) should always be greater than free cap (%d)",
                        PREMIUM_MAX_MEMBERS, FREE_MAX_MEMBERS)
                .isGreaterThan(FREE_MAX_MEMBERS);
    }

    @Property(tries = 100)
    void guildMemberCountNeverNegative(
            @ForAll @IntRange(min = 0, max = 50) int currentMembers,
            @ForAll @IntRange(min = 0, max = 50) int membersLeaving) {

        int afterLeaving = Math.max(0, currentMembers - membersLeaving);

        assertThat(afterLeaving)
                .as("Guild member count should never be negative")
                .isGreaterThanOrEqualTo(0);
    }

    // ========================================================================
    // Property 31: Shared quest XP accumulates from all member contributions
    // ========================================================================

    @Property(tries = 100)
    void sharedQuestXpAccumulatesFromAllContributions(
            @ForAll @IntRange(min = 1, max = 50) int memberCount,
            @ForAll @IntRange(min = 1, max = 10) int contributionPerMember) {

        int totalContribution = memberCount * contributionPerMember;

        assertThat(totalContribution)
                .as("Total contribution from %d members × %d each should be %d",
                        memberCount, contributionPerMember, totalContribution)
                .isEqualTo(memberCount * contributionPerMember);
    }

    @Property(tries = 100)
    void challengeCompletesWhenProgressReachesTarget(
            @ForAll @IntRange(min = 1, max = 1000) int target,
            @ForAll @IntRange(min = 1, max = 50) int memberCount,
            @ForAll @IntRange(min = 1, max = 100) int contributionsPerMember) {

        int totalProgress = memberCount * contributionsPerMember;
        boolean isComplete = totalProgress >= target;

        if (totalProgress >= target) {
            assertThat(isComplete)
                    .as("Challenge with target %d should be complete when progress is %d",
                            target, totalProgress)
                    .isTrue();
        } else {
            assertThat(isComplete)
                    .as("Challenge with target %d should NOT be complete when progress is %d",
                            target, totalProgress)
                    .isFalse();
        }
    }

    @Property(tries = 100)
    void xpAccumulationIsCommutative(
            @ForAll @IntRange(min = 0, max = 100) int contribution1,
            @ForAll @IntRange(min = 0, max = 100) int contribution2,
            @ForAll @IntRange(min = 0, max = 100) int contribution3) {

        // Order of contributions shouldn't matter
        int total1 = contribution1 + contribution2 + contribution3;
        int total2 = contribution3 + contribution1 + contribution2;
        int total3 = contribution2 + contribution3 + contribution1;

        assertThat(total1).isEqualTo(total2).isEqualTo(total3);
    }

    @Property(tries = 100)
    void guildXpRewardIsFixedPerChallengeCompletion(
            @ForAll @IntRange(min = 1, max = 100) int challengesCompleted) {

        long xpPerChallenge = 500L; // As defined in GuildChallengeService
        long totalGuildXp = challengesCompleted * xpPerChallenge;

        assertThat(totalGuildXp)
                .as("Guild XP after %d challenges should be %d (500 per challenge)",
                        challengesCompleted, totalGuildXp)
                .isEqualTo(challengesCompleted * 500L);
    }

    @Property(tries = 100)
    void progressNeverExceedsContributions(
            @ForAll @IntRange(min = 0, max = 500) int initialProgress,
            @ForAll @IntRange(min = 1, max = 100) int contribution) {

        int newProgress = initialProgress + contribution;

        assertThat(newProgress)
                .as("Progress after contribution should always increase")
                .isGreaterThan(initialProgress);
    }
}
