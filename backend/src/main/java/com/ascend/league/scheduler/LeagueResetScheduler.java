package com.ascend.league.scheduler;

import com.ascend.league.entity.LeagueGroup;
import com.ascend.league.entity.LeagueTier;
import com.ascend.league.event.LeagueDemotionEvent;
import com.ascend.league.event.LeaguePromotionEvent;
import com.ascend.league.repository.LeaderboardRepository;
import com.ascend.league.repository.LeagueGroupRepository;
import com.ascend.league.service.MatchmakingService;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Scheduler that runs every Sunday at 23:59 UTC to process the weekly league cycle.
 * <p>
 * For each league group:
 * <ol>
 *   <li>Ranks users by league score (descending)</li>
 *   <li>Top 15 users are promoted to the next tier</li>
 *   <li>Bottom 15 users are demoted to the previous tier</li>
 *   <li>Resets weekly_xp to 0 for all users</li>
 *   <li>Publishes LeaguePromotionEvent / LeagueDemotionEvent</li>
 * </ol>
 * After processing, reassigns groups for the new week.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LeagueResetScheduler {

    private static final int PROMOTION_COUNT = 15;
    private static final int DEMOTION_COUNT = 15;

    private final LeagueGroupRepository leagueGroupRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final UserRepository userRepository;
    private final MatchmakingService matchmakingService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Runs every Sunday at 23:59 UTC to process the weekly league reset.
     * This is idempotent — safe to retry if the previous run failed.
     */
    @Scheduled(cron = "0 59 23 * * SUN", zone = "UTC")
    @Transactional
    public void processWeeklyLeagueReset() {
        log.info("Weekly league reset scheduler triggered");

        String currentSeasonWeek = matchmakingService.getCurrentSeasonWeek();

        // Get all distinct group IDs for the current week
        List<UUID> groupIds = leagueGroupRepository.findDistinctGroupIdsBySeasonWeek(currentSeasonWeek);

        if (groupIds.isEmpty()) {
            log.info("No league groups found for season week {} — skipping reset", currentSeasonWeek);
            return;
        }

        log.info("Processing {} league groups for week {}", groupIds.size(), currentSeasonWeek);

        int totalPromotions = 0;
        int totalDemotions = 0;

        for (UUID groupId : groupIds) {
            try {
                int[] results = processGroup(groupId);
                totalPromotions += results[0];
                totalDemotions += results[1];
            } catch (Exception e) {
                log.error("Failed to process league group {}: {}", groupId, e.getMessage(), e);
            }
        }

        // Reset weekly XP for all leaderboard entries
        leaderboardRepository.resetAllWeeklyXp();
        log.info("Reset weekly XP for all leaderboard entries");

        // Reassign groups for the new week
        reassignGroupsForNewWeek(currentSeasonWeek);

        log.info("Weekly league reset completed: {} promotions, {} demotions across {} groups",
                totalPromotions, totalDemotions, groupIds.size());
    }

    /**
     * Processes a single league group: ranks users, promotes top 15, demotes bottom 15.
     *
     * @param groupId the UUID of the group to process
     * @return an int array where [0] = promotions count, [1] = demotions count
     */
    private int[] processGroup(UUID groupId) {
        List<LeagueGroup> members = leagueGroupRepository.findByGroupIdOrderByLeagueScoreDesc(groupId);

        if (members.isEmpty()) {
            return new int[]{0, 0};
        }

        LeagueTier groupTier = members.get(0).getTier();
        int groupSize = members.size();
        int promotions = 0;
        int demotions = 0;

        // Promote top users (only if there's a higher tier to promote to)
        LeagueTier nextTier = getNextTier(groupTier);
        if (nextTier != null) {
            int promoteCount = Math.min(PROMOTION_COUNT, groupSize);
            for (int i = 0; i < promoteCount; i++) {
                LeagueGroup member = members.get(i);
                promoteUser(member, groupTier, nextTier, i + 1);
                promotions++;
            }
        }

        // Demote bottom users (only if there's a lower tier to demote to)
        LeagueTier previousTier = getPreviousTier(groupTier);
        if (previousTier != null) {
            int demoteStartIndex = Math.max(0, groupSize - DEMOTION_COUNT);
            for (int i = demoteStartIndex; i < groupSize; i++) {
                LeagueGroup member = members.get(i);
                // Don't demote a user who was also in the promotion zone (small groups)
                if (nextTier != null && i < Math.min(PROMOTION_COUNT, groupSize)) {
                    continue;
                }
                demoteUser(member, groupTier, previousTier, i + 1);
                demotions++;
            }
        }

        log.debug("Processed group {} (tier={}): {} promotions, {} demotions out of {} members",
                groupId, groupTier, promotions, demotions, groupSize);

        return new int[]{promotions, demotions};
    }

    /**
     * Promotes a user to the next tier and publishes a LeaguePromotionEvent.
     */
    private void promoteUser(LeagueGroup member, LeagueTier currentTier, LeagueTier nextTier, int rank) {
        UUID userId = member.getUserId();

        // Update user's league field
        userRepository.findById(userId).ifPresent(user -> {
            user.setLeague(nextTier.name());
            userRepository.save(user);
        });

        // Publish promotion event
        LeaguePromotionEvent event = new LeaguePromotionEvent(
                this, userId, currentTier, nextTier, rank);
        eventPublisher.publishEvent(event);

        log.debug("Promoted user {} from {} to {} (rank {})", userId, currentTier, nextTier, rank);
    }

    /**
     * Demotes a user to the previous tier and publishes a LeagueDemotionEvent.
     */
    private void demoteUser(LeagueGroup member, LeagueTier currentTier, LeagueTier previousTier, int rank) {
        UUID userId = member.getUserId();

        // Update user's league field
        userRepository.findById(userId).ifPresent(user -> {
            user.setLeague(previousTier.name());
            userRepository.save(user);
        });

        // Publish demotion event
        LeagueDemotionEvent event = new LeagueDemotionEvent(
                this, userId, currentTier, previousTier, rank);
        eventPublisher.publishEvent(event);

        log.debug("Demoted user {} from {} to {} (rank {})", userId, currentTier, previousTier, rank);
    }

    /**
     * Reassigns all users to new groups for the upcoming week.
     * Removes old group assignments and triggers matchmaking for each user.
     */
    private void reassignGroupsForNewWeek(String completedSeasonWeek) {
        List<LeagueGroup> allMembers = leagueGroupRepository.findBySeasonWeek(completedSeasonWeek);

        // Collect user IDs before clearing old assignments
        List<UUID> userIds = allMembers.stream()
                .map(LeagueGroup::getUserId)
                .toList();

        // Delete old week's group assignments
        leagueGroupRepository.deleteBySeasonWeek(completedSeasonWeek);
        leagueGroupRepository.flush();

        // Reassign each user to a new group for the next week
        int reassigned = 0;
        for (UUID userId : userIds) {
            try {
                matchmakingService.assignToGroup(userId);
                reassigned++;
            } catch (Exception e) {
                log.error("Failed to reassign user {} to new group: {}", userId, e.getMessage(), e);
            }
        }

        log.info("Reassigned {}/{} users to new groups for the upcoming week", reassigned, userIds.size());
    }

    /**
     * Returns the next higher tier, or null if already at the highest promotable tier.
     * ASCENDANT is invite-only and cannot be promoted to via the weekly cycle.
     */
    private LeagueTier getNextTier(LeagueTier current) {
        LeagueTier[] tiers = LeagueTier.values();
        for (int i = 0; i < tiers.length - 1; i++) {
            if (tiers[i] == current) {
                LeagueTier next = tiers[i + 1];
                // Cannot promote to ASCENDANT (invite-only)
                return next == LeagueTier.ASCENDANT ? null : next;
            }
        }
        return null;
    }

    /**
     * Returns the previous lower tier, or null if already at BRONZE.
     */
    private LeagueTier getPreviousTier(LeagueTier current) {
        LeagueTier[] tiers = LeagueTier.values();
        for (int i = 1; i < tiers.length; i++) {
            if (tiers[i] == current) {
                return tiers[i - 1];
            }
        }
        return null;
    }
}
