package com.ascend.league.service;

import com.ascend.league.dto.LeaderboardEntry;
import com.ascend.league.dto.LeaderboardResponse;
import com.ascend.league.dto.LeagueHistoryEntry;
import com.ascend.league.dto.LeagueHistoryResponse;
import com.ascend.league.dto.LeagueInfoResponse;
import com.ascend.league.entity.LeagueGroup;
import com.ascend.league.entity.LeagueTier;
import com.ascend.league.repository.LeagueGroupRepository;
import com.ascend.streak.repository.StreakRepository;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * Core service for league tier assignment, score calculation, leaderboard retrieval,
 * and user league info.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeagueService {

    private static final double LEVEL_WEIGHT = 0.4;
    private static final double CONSISTENCY_WEIGHT = 0.3;
    private static final double STREAK_WEIGHT = 0.2;
    private static final double ACTIVITY_WEIGHT = 0.1;

    private static final int PROMOTION_ZONE_SIZE = 15;
    private static final int DEMOTION_ZONE_SIZE = 15;

    private final LeagueGroupRepository leagueGroupRepository;
    private final UserRepository userRepository;
    private final StreakRepository streakRepository;

    /**
     * Determines the appropriate league tier based on user level.
     * ASCENDANT tier is invite-only and cannot be reached by level alone.
     *
     * @param userLevel the user's current level
     * @return the assigned LeagueTier
     */
    public LeagueTier assignTier(int userLevel) {
        return LeagueTier.fromLevel(userLevel);
    }

    /**
     * Calculates the league score using the weighted formula:
     * 0.4×Level + 0.3×Consistency + 0.2×Streak + 0.1×ActivityScore
     *
     * @param level         the user's current level
     * @param consistency   the user's consistency score (0-100)
     * @param streak        the user's current streak count
     * @param activityScore the user's activity score (0-100)
     * @return the calculated league score
     */
    public double calculateLeagueScore(int level, double consistency, int streak, double activityScore) {
        return (LEVEL_WEIGHT * level)
                + (CONSISTENCY_WEIGHT * consistency)
                + (STREAK_WEIGHT * streak)
                + (ACTIVITY_WEIGHT * activityScore);
    }

    /**
     * Retrieves a paginated leaderboard for a specific league tier.
     *
     * @param league the league tier name (e.g., "GOLD")
     * @param page   the page number (0-indexed)
     * @param size   the page size
     * @return LeaderboardResponse with entries, user rank, and total count
     */
    @Transactional(readOnly = true)
    public LeaderboardResponse getLeaderboard(String league, int page, int size) {
        LeagueTier tier = LeagueTier.valueOf(league.toUpperCase());
        Pageable pageable = PageRequest.of(page, size);

        Page<LeagueGroup> groupPage = leagueGroupRepository.findByTierOrderByLeagueScoreDesc(tier, pageable);

        List<LeaderboardEntry> entries = IntStream.range(0, groupPage.getContent().size())
                .mapToObj(i -> {
                    LeagueGroup group = groupPage.getContent().get(i);
                    int rank = (page * size) + i + 1;
                    return buildLeaderboardEntry(group, rank);
                })
                .toList();

        return LeaderboardResponse.builder()
                .entries(entries)
                .userRank(0) // caller should set this based on authenticated user
                .totalUsers((int) groupPage.getTotalElements())
                .league(tier)
                .build();
    }

    /**
     * Retrieves the current league info for a specific user, including tier, score, rank,
     * and promotion/demotion zone indicators.
     *
     * @param userId the user's UUID
     * @return LeagueInfoResponse with current tier, score, rank, and zone info
     */
    @Transactional(readOnly = true)
    public LeagueInfoResponse getUserLeagueInfo(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        LeagueTier currentTier = assignTier(user.getLevel());

        // Get user's league group entry
        LeagueGroup userGroup = leagueGroupRepository.findByUserId(userId)
                .orElse(null);

        double leagueScore = 0.0;
        int weeklyRank = 0;
        int groupSize = 0;

        if (userGroup != null) {
            leagueScore = userGroup.getLeagueScore();

            // Calculate rank within the user's group
            List<LeagueGroup> groupMembers = leagueGroupRepository
                    .findByGroupIdOrderByLeagueScoreDesc(userGroup.getGroupId());
            groupSize = groupMembers.size();

            for (int i = 0; i < groupMembers.size(); i++) {
                if (groupMembers.get(i).getUserId().equals(userId)) {
                    weeklyRank = i + 1;
                    break;
                }
            }
        }

        return LeagueInfoResponse.builder()
                .currentTier(currentTier)
                .leagueScore(leagueScore)
                .weeklyRank(weeklyRank)
                .promotionZone(PROMOTION_ZONE_SIZE)
                .demotionZone(DEMOTION_ZONE_SIZE)
                .groupSize(groupSize)
                .build();
    }

    /**
     * Retrieves the league history for a user, showing past week results and promotions/demotions.
     * Returns entries for previous season weeks, ordered by most recent first.
     *
     * @param userId the user's UUID
     * @return LeagueHistoryResponse with past week entries
     */
    @Transactional(readOnly = true)
    public LeagueHistoryResponse getLeagueHistory(UUID userId) {
        List<LeagueHistoryEntry> historyEntries = new ArrayList<>();

        // Get the user's current league group entry
        LeagueGroup currentEntry = leagueGroupRepository.findByUserId(userId).orElse(null);

        if (currentEntry != null) {
            historyEntries.add(buildHistoryEntry(currentEntry, userId));
        }

        // Since the scheduler deletes old week data on reset, history is limited
        // to the current week's entry. In a production system, a dedicated history
        // table would persist past results across weekly resets.

        return LeagueHistoryResponse.builder()
                .weeks(historyEntries)
                .totalWeeksPlayed(historyEntries.size())
                .build();
    }

    /**
     * Builds a history entry from a LeagueGroup record.
     */
    private LeagueHistoryEntry buildHistoryEntry(LeagueGroup group, UUID userId) {
        List<LeagueGroup> groupMembers = leagueGroupRepository
                .findByGroupIdOrderByLeagueScoreDesc(group.getGroupId());

        int groupSize = groupMembers.size();
        int finalRank = 0;
        for (int i = 0; i < groupMembers.size(); i++) {
            if (groupMembers.get(i).getUserId().equals(userId)) {
                finalRank = i + 1;
                break;
            }
        }

        String result = determineResult(finalRank, groupSize);

        return LeagueHistoryEntry.builder()
                .seasonWeek(group.getSeasonWeek())
                .tier(group.getTier())
                .leagueScore(group.getLeagueScore())
                .finalRank(finalRank)
                .groupSize(groupSize)
                .result(result)
                .build();
    }

    /**
     * Determines the promotion/demotion result based on rank within the group.
     */
    private String determineResult(int rank, int groupSize) {
        if (rank > 0 && rank <= PROMOTION_ZONE_SIZE) {
            return "PROMOTED";
        } else if (rank > 0 && rank > (groupSize - DEMOTION_ZONE_SIZE)) {
            return "DEMOTED";
        }
        return "STAYED";
    }

    private LeaderboardEntry buildLeaderboardEntry(LeagueGroup group, int rank) {
        User user = userRepository.findById(group.getUserId()).orElse(null);
        int streak = streakRepository.findByUserId(group.getUserId())
                .map(s -> s.getCurrentStreak())
                .orElse(0);

        return LeaderboardEntry.builder()
                .rank(rank)
                .userId(group.getUserId())
                .username(user != null ? user.getUsername() : "Unknown")
                .avatarUrl(user != null ? user.getAvatarUrl() : null)
                .level(user != null ? user.getLevel() : 0)
                .weeklyXp(0L) // populated from leaderboard table if needed
                .leagueScore(group.getLeagueScore())
                .streak(streak)
                .build();
    }
}
