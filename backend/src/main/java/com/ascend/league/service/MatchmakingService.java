package com.ascend.league.service;

import com.ascend.league.entity.LeagueGroup;
import com.ascend.league.entity.LeagueTier;
import com.ascend.league.repository.LeagueGroupRepository;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Service responsible for matchmaking users into league groups.
 * Groups contain ~50 users with similar league scores within the same tier.
 * Groups are created at the start of each weekly cycle.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchmakingService {

    private static final int MAX_GROUP_SIZE = 50;

    private final LeagueGroupRepository leagueGroupRepository;
    private final UserRepository userRepository;
    private final LeagueService leagueService;

    /**
     * Assigns a user to a league group for the current weekly cycle.
     * Finds an existing group with available capacity in the user's tier,
     * or creates a new group if none are available.
     *
     * @param userId the UUID of the user to assign
     * @return the LeagueGroup assignment for the user
     * @throws IllegalArgumentException if the user is not found
     */
    @Transactional
    public LeagueGroup assignToGroup(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        LeagueTier tier = leagueService.assignTier(user.getLevel());
        String currentSeasonWeek = getCurrentSeasonWeek();

        // Check if user is already assigned to a group this week
        Optional<LeagueGroup> existingAssignment = leagueGroupRepository.findByUserId(userId);
        if (existingAssignment.isPresent()
                && currentSeasonWeek.equals(existingAssignment.get().getSeasonWeek())) {
            log.debug("User {} already assigned to group {} for week {}",
                    userId, existingAssignment.get().getGroupId(), currentSeasonWeek);
            return existingAssignment.get();
        }

        // Calculate the user's league score
        double leagueScore = calculateUserLeagueScore(user);

        // Find an available group for this tier and week
        UUID groupId = findOrCreateGroup(tier, currentSeasonWeek, leagueScore);

        // Create the group assignment
        LeagueGroup assignment = LeagueGroup.builder()
                .userId(userId)
                .groupId(groupId)
                .tier(tier)
                .leagueScore(leagueScore)
                .seasonWeek(currentSeasonWeek)
                .build();

        // Remove old assignment if exists (from a previous week)
        existingAssignment.ifPresent(leagueGroupRepository::delete);

        LeagueGroup saved = leagueGroupRepository.save(assignment);
        log.info("Assigned user {} to group {} in tier {} for week {}",
                userId, groupId, tier, currentSeasonWeek);

        return saved;
    }

    /**
     * Finds an existing group with capacity in the given tier for the current week,
     * preferring groups where the user's score is similar to existing members.
     * Creates a new group if no suitable group is found.
     *
     * @param tier            the league tier
     * @param seasonWeek      the current season week identifier
     * @param userLeagueScore the user's league score for similarity matching
     * @return the UUID of the group to assign the user to
     */
    private UUID findOrCreateGroup(LeagueTier tier, String seasonWeek, double userLeagueScore) {
        List<LeagueGroup> tierGroups = leagueGroupRepository.findByTierAndSeasonWeek(tier, seasonWeek);

        // Find groups with available capacity and similar scores
        UUID bestGroup = null;
        double bestScoreDiff = Double.MAX_VALUE;

        // Get distinct group IDs from the tier groups
        List<UUID> distinctGroupIds = tierGroups.stream()
                .map(LeagueGroup::getGroupId)
                .distinct()
                .toList();

        for (UUID groupId : distinctGroupIds) {
            long groupSize = leagueGroupRepository.countByGroupId(groupId);

            if (groupSize >= MAX_GROUP_SIZE) {
                continue; // Group is full
            }

            // Calculate average score of the group for similarity matching
            double avgScore = tierGroups.stream()
                    .filter(g -> g.getGroupId().equals(groupId))
                    .mapToDouble(LeagueGroup::getLeagueScore)
                    .average()
                    .orElse(0.0);

            double scoreDiff = Math.abs(avgScore - userLeagueScore);
            if (scoreDiff < bestScoreDiff) {
                bestScoreDiff = scoreDiff;
                bestGroup = groupId;
            }
        }

        if (bestGroup != null) {
            return bestGroup;
        }

        // No suitable group found — create a new one
        UUID newGroupId = UUID.randomUUID();
        log.info("Created new league group {} for tier {} in week {}", newGroupId, tier, seasonWeek);
        return newGroupId;
    }

    /**
     * Calculates the user's league score based on available metrics.
     */
    private double calculateUserLeagueScore(User user) {
        // Use level as the primary input; consistency, streak, and activity
        // default to 0 if not readily available at assignment time.
        // The score will be recalculated as the week progresses.
        return leagueService.calculateLeagueScore(
                user.getLevel(),
                0.0,  // consistency — updated during the week
                0,    // streak — updated during the week
                0.0   // activity score — updated during the week
        );
    }

    /**
     * Generates the current season week identifier (e.g., "2024-W03").
     *
     * @return the season week string
     */
    public String getCurrentSeasonWeek() {
        LocalDate now = LocalDate.now();
        int weekNumber = now.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        int year = now.getYear();
        return String.format("%d-W%02d", year, weekNumber);
    }
}
