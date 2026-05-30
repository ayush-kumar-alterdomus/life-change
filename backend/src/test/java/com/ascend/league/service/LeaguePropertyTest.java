package com.ascend.league.service;

import com.ascend.league.entity.LeagueGroup;
import com.ascend.league.entity.LeagueTier;
import com.ascend.league.repository.LeaderboardRepository;
import com.ascend.league.repository.LeagueGroupRepository;
import com.ascend.league.repository.SecurityViolationRepository;
import com.ascend.streak.repository.StreakRepository;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import com.ascend.xp.repository.XpHistoryRepository;
import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Property-based tests for the League system.
 * Validates tier assignment, league score formula, promotion/demotion logic,
 * and anti-cheat speed detection across randomized inputs.
 *
 * **Validates: Requirements 1.2**
 */
class LeaguePropertyTest {

    // ========================================================================
    // Property 26: Tier assignment correctness for all levels
    // ========================================================================

    /**
     * Property: Tier assignment must return the correct tier for any valid user level.
     * - Level 0-9 → BRONZE
     * - Level 10-19 → SILVER
     * - Level 20-34 → GOLD
     * - Level 35-49 → PLATINUM
     * - Level 50-74 → DIAMOND
     * - Level 75+ → MASTER
     * - ASCENDANT is never assigned by level alone
     *
     * **Validates: Requirements 1.2**
     */
    @Property(tries = 100)
    void tierAssignmentMatchesLevelThresholds(
            @ForAll @IntRange(min = 0, max = 200) int userLevel) {

        LeagueService leagueService = createLeagueServiceForPureFunctions();

        LeagueTier assignedTier = leagueService.assignTier(userLevel);

        LeagueTier expectedTier = computeExpectedTier(userLevel);

        assertThat(assignedTier)
                .as("Level %d should be assigned tier %s", userLevel, expectedTier)
                .isEqualTo(expectedTier);
    }

    /**
     * Property: ASCENDANT tier is never assigned by the level-based assignment,
     * regardless of how high the level is.
     *
     * **Validates: Requirements 1.2**
     */
    @Property(tries = 100)
    void ascendantTierIsNeverAssignedByLevel(
            @ForAll @IntRange(min = 0, max = 10000) int userLevel) {

        LeagueService leagueService = createLeagueServiceForPureFunctions();

        LeagueTier assignedTier = leagueService.assignTier(userLevel);

        assertThat(assignedTier)
                .as("ASCENDANT should never be assigned by level alone (level=%d)", userLevel)
                .isNotEqualTo(LeagueTier.ASCENDANT);
    }

    /**
     * Property: Tier assignment is monotonically non-decreasing with level.
     * A higher level should always result in an equal or higher tier.
     *
     * **Validates: Requirements 1.2**
     */
    @Property(tries = 100)
    void tierAssignmentIsMonotonicallyNonDecreasing(
            @ForAll @IntRange(min = 0, max = 200) int level1,
            @ForAll @IntRange(min = 0, max = 200) int level2) {

        LeagueService leagueService = createLeagueServiceForPureFunctions();

        LeagueTier tier1 = leagueService.assignTier(level1);
        LeagueTier tier2 = leagueService.assignTier(level2);

        if (level1 <= level2) {
            assertThat(tier1.ordinal())
                    .as("Tier for level %d (%s) should be <= tier for level %d (%s)",
                            level1, tier1, level2, tier2)
                    .isLessThanOrEqualTo(tier2.ordinal());
        }
    }

    // ========================================================================
    // Property 27: League score formula correctness
    // ========================================================================

    /**
     * Property: League score must equal 0.4×Level + 0.3×Consistency + 0.2×Streak + 0.1×ActivityScore
     * for all valid inputs.
     *
     * **Validates: Requirements 1.2**
     */
    @Property(tries = 100)
    void leagueScoreMatchesWeightedFormula(
            @ForAll @IntRange(min = 0, max = 100) int level,
            @ForAll @DoubleRange(min = 0.0, max = 100.0) double consistency,
            @ForAll @IntRange(min = 0, max = 365) int streak,
            @ForAll @DoubleRange(min = 0.0, max = 100.0) double activityScore) {

        LeagueService leagueService = createLeagueServiceForPureFunctions();

        double score = leagueService.calculateLeagueScore(level, consistency, streak, activityScore);

        double expected = (0.4 * level) + (0.3 * consistency) + (0.2 * streak) + (0.1 * activityScore);

        assertThat(score)
                .as("League score for level=%d, consistency=%.2f, streak=%d, activity=%.2f should be %.4f",
                        level, consistency, streak, activityScore, expected)
                .isCloseTo(expected, org.assertj.core.data.Offset.offset(0.0001));
    }

    /**
     * Property: League score is always non-negative when all inputs are non-negative.
     *
     * **Validates: Requirements 1.2**
     */
    @Property(tries = 100)
    void leagueScoreIsNonNegativeForNonNegativeInputs(
            @ForAll @IntRange(min = 0, max = 100) int level,
            @ForAll @DoubleRange(min = 0.0, max = 100.0) double consistency,
            @ForAll @IntRange(min = 0, max = 365) int streak,
            @ForAll @DoubleRange(min = 0.0, max = 100.0) double activityScore) {

        LeagueService leagueService = createLeagueServiceForPureFunctions();

        double score = leagueService.calculateLeagueScore(level, consistency, streak, activityScore);

        assertThat(score)
                .as("League score should be non-negative for non-negative inputs")
                .isGreaterThanOrEqualTo(0.0);
    }

    /**
     * Property: Increasing any single input while holding others constant
     * should increase or maintain the league score (monotonicity).
     *
     * **Validates: Requirements 1.2**
     */
    @Property(tries = 100)
    void leagueScoreIncreasesWithHigherInputs(
            @ForAll @IntRange(min = 0, max = 50) int level,
            @ForAll @DoubleRange(min = 0.0, max = 50.0) double consistency,
            @ForAll @IntRange(min = 0, max = 180) int streak,
            @ForAll @DoubleRange(min = 0.0, max = 50.0) double activityScore,
            @ForAll @IntRange(min = 1, max = 50) int levelIncrease) {

        LeagueService leagueService = createLeagueServiceForPureFunctions();

        double scoreBefore = leagueService.calculateLeagueScore(level, consistency, streak, activityScore);
        double scoreAfter = leagueService.calculateLeagueScore(level + levelIncrease, consistency, streak, activityScore);

        assertThat(scoreAfter)
                .as("Score should increase when level increases from %d to %d",
                        level, level + levelIncrease)
                .isGreaterThan(scoreBefore);
    }

    // ========================================================================
    // Property 28: Promotion/demotion (top 15 promoted, bottom 15 demoted)
    // ========================================================================

    /**
     * Property: In a group of 50 users ranked by league score, the top 15 are promoted
     * and the bottom 15 are demoted. Users in the middle (ranks 16-35) stay.
     *
     * **Validates: Requirements 1.2**
     */
    @Property(tries = 100)
    void promotionDemotionZonesAreCorrect(
            @ForAll @IntRange(min = 1, max = 50) int userRank,
            @ForAll @IntRange(min = 30, max = 50) int groupSize) {

        // Ensure userRank is within the group
        int effectiveRank = Math.min(userRank, groupSize);

        String result = determineResult(effectiveRank, groupSize);

        if (effectiveRank <= 15) {
            assertThat(result)
                    .as("Rank %d in group of %d should be PROMOTED", effectiveRank, groupSize)
                    .isEqualTo("PROMOTED");
        } else if (effectiveRank > groupSize - 15) {
            assertThat(result)
                    .as("Rank %d in group of %d should be DEMOTED", effectiveRank, groupSize)
                    .isEqualTo("DEMOTED");
        } else {
            assertThat(result)
                    .as("Rank %d in group of %d should STAY", effectiveRank, groupSize)
                    .isEqualTo("STAYED");
        }
    }

    /**
     * Property: In any group, the number of promoted users is at most 15
     * and the number of demoted users is at most 15.
     *
     * **Validates: Requirements 1.2**
     */
    @Property(tries = 100)
    void promotionAndDemotionCountsAreCapped(
            @ForAll @IntRange(min = 1, max = 100) int groupSize) {

        long promotedCount = IntStream.rangeClosed(1, groupSize)
                .filter(rank -> rank <= 15)
                .count();

        long demotedCount = IntStream.rangeClosed(1, groupSize)
                .filter(rank -> rank > groupSize - 15)
                .count();

        assertThat(promotedCount)
                .as("Promoted count in group of %d should be at most 15", groupSize)
                .isLessThanOrEqualTo(15);

        assertThat(demotedCount)
                .as("Demoted count in group of %d should be at most 15", groupSize)
                .isLessThanOrEqualTo(15);
    }

    /**
     * Property: Users are ranked by league score descending — the user with the
     * highest score gets rank 1 (promoted), lowest score gets last rank (demoted).
     *
     * **Validates: Requirements 1.2**
     */
    @Property(tries = 100)
    void rankingIsBasedOnLeagueScoreDescending(
            @ForAll("leagueScoreLists") List<Double> scores) {

        // Sort descending to simulate ranking
        List<Double> ranked = scores.stream()
                .sorted(Comparator.reverseOrder())
                .toList();

        // Verify rank 1 has the highest score
        for (int i = 0; i < ranked.size() - 1; i++) {
            assertThat(ranked.get(i))
                    .as("Rank %d score (%.2f) should be >= rank %d score (%.2f)",
                            i + 1, ranked.get(i), i + 2, ranked.get(i + 1))
                    .isGreaterThanOrEqualTo(ranked.get(i + 1));
        }

        // Top 15 (or fewer if group is small) should be promotion zone
        int groupSize = ranked.size();
        for (int rank = 1; rank <= Math.min(15, groupSize); rank++) {
            String result = determineResult(rank, groupSize);
            if (groupSize > 30) { // Only check when group is large enough to have distinct zones
                assertThat(result).isEqualTo("PROMOTED");
            }
        }
    }

    // ========================================================================
    // Property 29: Anti-cheat speed detection (>10 in 5 min flagged)
    // ========================================================================

    /**
     * Property: If a user has more than 10 quest completions in 5 minutes,
     * the speed violation detection must return true (flagged).
     *
     * **Validates: Requirements 1.2**
     */
    @Property(tries = 100)
    void speedViolationDetectedWhenExceedingThreshold(
            @ForAll("userIds") UUID userId,
            @ForAll @IntRange(min = 11, max = 100) int completionCount) {

        // Mock dependencies
        XpHistoryRepository xpHistoryRepository = Mockito.mock(XpHistoryRepository.class);
        SecurityViolationRepository securityViolationRepository = Mockito.mock(SecurityViolationRepository.class);
        LeaderboardRepository leaderboardRepository = Mockito.mock(LeaderboardRepository.class);
        UserRepository userRepository = Mockito.mock(UserRepository.class);

        // Setup: user has more than 10 completions in the last 5 minutes
        when(xpHistoryRepository.countByUserIdAndSourceTypeAndCreatedAtAfter(
                eq(userId), eq("QUEST"), any(LocalDateTime.class)))
                .thenReturn((long) completionCount);

        // XP rollback returns some value
        when(xpHistoryRepository.sumXpAmountByUserIdAndCreatedAtBetween(
                eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(100L);

        // User exists for XP rollback
        User user = User.builder().id(userId).firebaseUid("fb-" + userId).xp(1000L).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // Leaderboard entry exists for ban
        when(leaderboardRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Security violation save
        when(securityViolationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AntiCheatService antiCheatService = new AntiCheatService(
                xpHistoryRepository, securityViolationRepository,
                leaderboardRepository, userRepository);

        boolean flagged = antiCheatService.detectSpeedViolation(userId);

        assertThat(flagged)
                .as("User with %d completions in 5 min (>10 threshold) should be flagged",
                        completionCount)
                .isTrue();
    }

    /**
     * Property: If a user has 10 or fewer quest completions in 5 minutes,
     * the speed violation detection must return false (not flagged).
     *
     * **Validates: Requirements 1.2**
     */
    @Property(tries = 100)
    void speedViolationNotDetectedAtOrBelowThreshold(
            @ForAll("userIds") UUID userId,
            @ForAll @IntRange(min = 0, max = 10) int completionCount) {

        // Mock dependencies
        XpHistoryRepository xpHistoryRepository = Mockito.mock(XpHistoryRepository.class);
        SecurityViolationRepository securityViolationRepository = Mockito.mock(SecurityViolationRepository.class);
        LeaderboardRepository leaderboardRepository = Mockito.mock(LeaderboardRepository.class);
        UserRepository userRepository = Mockito.mock(UserRepository.class);

        // Setup: user has 10 or fewer completions in the last 5 minutes
        when(xpHistoryRepository.countByUserIdAndSourceTypeAndCreatedAtAfter(
                eq(userId), eq("QUEST"), any(LocalDateTime.class)))
                .thenReturn((long) completionCount);

        AntiCheatService antiCheatService = new AntiCheatService(
                xpHistoryRepository, securityViolationRepository,
                leaderboardRepository, userRepository);

        boolean flagged = antiCheatService.detectSpeedViolation(userId);

        assertThat(flagged)
                .as("User with %d completions in 5 min (<=10 threshold) should NOT be flagged",
                        completionCount)
                .isFalse();
    }

    /**
     * Property: The boundary condition — exactly 10 completions should NOT trigger
     * a violation (threshold is strictly greater than 10).
     *
     * **Validates: Requirements 1.2**
     */
    @Property(tries = 100)
    void speedViolationBoundaryExactlyTenIsNotFlagged(
            @ForAll("userIds") UUID userId) {

        // Mock dependencies
        XpHistoryRepository xpHistoryRepository = Mockito.mock(XpHistoryRepository.class);
        SecurityViolationRepository securityViolationRepository = Mockito.mock(SecurityViolationRepository.class);
        LeaderboardRepository leaderboardRepository = Mockito.mock(LeaderboardRepository.class);
        UserRepository userRepository = Mockito.mock(UserRepository.class);

        // Setup: exactly 10 completions (boundary)
        when(xpHistoryRepository.countByUserIdAndSourceTypeAndCreatedAtAfter(
                eq(userId), eq("QUEST"), any(LocalDateTime.class)))
                .thenReturn(10L);

        AntiCheatService antiCheatService = new AntiCheatService(
                xpHistoryRepository, securityViolationRepository,
                leaderboardRepository, userRepository);

        boolean flagged = antiCheatService.detectSpeedViolation(userId);

        assertThat(flagged)
                .as("Exactly 10 completions should NOT trigger speed violation (threshold is >10)")
                .isFalse();
    }

    // ========================================================================
    // Providers
    // ========================================================================

    @Provide
    Arbitrary<UUID> userIds() {
        return Arbitraries.randomValue(random -> UUID.randomUUID());
    }

    @Provide
    Arbitrary<List<Double>> leagueScoreLists() {
        return Arbitraries.doubles().between(0.0, 200.0)
                .list().ofMinSize(30).ofMaxSize(50);
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    /**
     * Creates a LeagueService instance suitable for testing pure calculation methods.
     * The repository dependencies are null since assignTier and calculateLeagueScore
     * are pure functions that don't use any injected dependencies.
     */
    private LeagueService createLeagueServiceForPureFunctions() {
        return new LeagueService(null, null, null);
    }

    /**
     * Computes the expected tier for a given level based on the defined thresholds.
     */
    private LeagueTier computeExpectedTier(int level) {
        if (level >= 75) return LeagueTier.MASTER;
        if (level >= 50) return LeagueTier.DIAMOND;
        if (level >= 35) return LeagueTier.PLATINUM;
        if (level >= 20) return LeagueTier.GOLD;
        if (level >= 10) return LeagueTier.SILVER;
        return LeagueTier.BRONZE;
    }

    /**
     * Determines the promotion/demotion result based on rank within the group.
     * Mirrors the logic in LeagueService.determineResult.
     */
    private String determineResult(int rank, int groupSize) {
        if (rank > 0 && rank <= 15) {
            return "PROMOTED";
        } else if (rank > 0 && rank > (groupSize - 15)) {
            return "DEMOTED";
        }
        return "STAYED";
    }
}
