package com.ascend.league.service;

import com.ascend.league.entity.Leaderboard;
import com.ascend.league.entity.LeagueGroup;
import com.ascend.league.entity.LeagueTier;
import com.ascend.league.entity.SecurityViolation;
import com.ascend.league.event.LeagueDemotionEvent;
import com.ascend.league.event.LeaguePromotionEvent;
import com.ascend.league.repository.LeaderboardRepository;
import com.ascend.league.repository.LeagueGroupRepository;
import com.ascend.league.repository.SecurityViolationRepository;
import com.ascend.league.scheduler.LeagueResetScheduler;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import com.ascend.xp.repository.XpHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Integration tests for the League system checkpoint.
 * Verifies end-to-end flows:
 * - User levels up → tier assignment updates correctly
 * - Weekly reset → promotions and demotions applied
 * - Speed violation detected → account flagged
 */
@ExtendWith(MockitoExtension.class)
class LeagueSystemIntegrationTest {

    @Mock
    private LeagueGroupRepository leagueGroupRepository;

    @Mock
    private LeaderboardRepository leaderboardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityViolationRepository securityViolationRepository;

    @Mock
    private XpHistoryRepository xpHistoryRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private MatchmakingService matchmakingService;

    private LeagueService leagueService;
    private AntiCheatService antiCheatService;
    private LeagueResetScheduler leagueResetScheduler;

    private UUID userId;

    @BeforeEach
    void setUp() {
        leagueService = new LeagueService(leagueGroupRepository, userRepository, null);
        antiCheatService = new AntiCheatService(
                xpHistoryRepository, securityViolationRepository,
                leaderboardRepository, userRepository);
        leagueResetScheduler = new LeagueResetScheduler(
                leagueGroupRepository, leaderboardRepository,
                userRepository, matchmakingService, eventPublisher);
        userId = UUID.randomUUID();
    }

    // ========================================================================
    // Integration test: user levels up → tier assignment updates
    // ========================================================================

    @Nested
    @DisplayName("User levels up → tier assignment updates correctly")
    class TierAssignmentOnLevelUp {

        @Test
        @DisplayName("User at level 5 is assigned BRONZE tier")
        void userLevel5_assignedBronze() {
            LeagueTier tier = leagueService.assignTier(5);
            assertThat(tier).isEqualTo(LeagueTier.BRONZE);
        }

        @Test
        @DisplayName("User levels up from 9 to 10 → tier changes from BRONZE to SILVER")
        void userLevelsUpTo10_tierChangesToSilver() {
            LeagueTier tierBefore = leagueService.assignTier(9);
            LeagueTier tierAfter = leagueService.assignTier(10);

            assertThat(tierBefore).isEqualTo(LeagueTier.BRONZE);
            assertThat(tierAfter).isEqualTo(LeagueTier.SILVER);
        }

        @Test
        @DisplayName("User levels up from 19 to 20 → tier changes from SILVER to GOLD")
        void userLevelsUpTo20_tierChangesToGold() {
            LeagueTier tierBefore = leagueService.assignTier(19);
            LeagueTier tierAfter = leagueService.assignTier(20);

            assertThat(tierBefore).isEqualTo(LeagueTier.SILVER);
            assertThat(tierAfter).isEqualTo(LeagueTier.GOLD);
        }

        @Test
        @DisplayName("User levels up from 34 to 35 → tier changes from GOLD to PLATINUM")
        void userLevelsUpTo35_tierChangesToPlatinum() {
            LeagueTier tierBefore = leagueService.assignTier(34);
            LeagueTier tierAfter = leagueService.assignTier(35);

            assertThat(tierBefore).isEqualTo(LeagueTier.GOLD);
            assertThat(tierAfter).isEqualTo(LeagueTier.PLATINUM);
        }

        @Test
        @DisplayName("User levels up from 49 to 50 → tier changes from PLATINUM to DIAMOND")
        void userLevelsUpTo50_tierChangesToDiamond() {
            LeagueTier tierBefore = leagueService.assignTier(49);
            LeagueTier tierAfter = leagueService.assignTier(50);

            assertThat(tierBefore).isEqualTo(LeagueTier.PLATINUM);
            assertThat(tierAfter).isEqualTo(LeagueTier.DIAMOND);
        }

        @Test
        @DisplayName("User levels up from 74 to 75 → tier changes from DIAMOND to MASTER")
        void userLevelsUpTo75_tierChangesToMaster() {
            LeagueTier tierBefore = leagueService.assignTier(74);
            LeagueTier tierAfter = leagueService.assignTier(75);

            assertThat(tierBefore).isEqualTo(LeagueTier.DIAMOND);
            assertThat(tierAfter).isEqualTo(LeagueTier.MASTER);
        }

        @Test
        @DisplayName("User at level 100 remains MASTER — ASCENDANT is invite-only")
        void userLevel100_remainsMaster_ascendantInviteOnly() {
            LeagueTier tier = leagueService.assignTier(100);
            assertThat(tier).isEqualTo(LeagueTier.MASTER);
            assertThat(tier).isNotEqualTo(LeagueTier.ASCENDANT);
        }

        @Test
        @DisplayName("Full level-up journey: user info reflects correct tier at each stage")
        void fullLevelUpJourney_tierUpdatesCorrectly() {
            // Simulate a user progressing through all tiers
            User user = User.builder()
                    .id(userId)
                    .firebaseUid("fb-test")
                    .level(5)
                    .league("BRONZE")
                    .build();

            // Level 5 → BRONZE
            assertThat(leagueService.assignTier(user.getLevel())).isEqualTo(LeagueTier.BRONZE);

            // User levels up to 15 → SILVER
            user.setLevel(15);
            LeagueTier newTier = leagueService.assignTier(user.getLevel());
            assertThat(newTier).isEqualTo(LeagueTier.SILVER);

            // User levels up to 25 → GOLD
            user.setLevel(25);
            newTier = leagueService.assignTier(user.getLevel());
            assertThat(newTier).isEqualTo(LeagueTier.GOLD);

            // User levels up to 40 → PLATINUM
            user.setLevel(40);
            newTier = leagueService.assignTier(user.getLevel());
            assertThat(newTier).isEqualTo(LeagueTier.PLATINUM);
        }
    }

    // ========================================================================
    // Integration test: weekly reset → promotions and demotions applied
    // ========================================================================

    @Nested
    @DisplayName("Weekly reset → promotions and demotions applied")
    @MockitoSettings(strictness = Strictness.LENIENT)
    class WeeklyResetPromotionsDemotions {

        @Test
        @DisplayName("Top 15 users in a SILVER group are promoted to GOLD")
        void weeklyReset_top15Promoted() {
            UUID groupId = UUID.randomUUID();
            String seasonWeek = "2024-W10";

            // Create a group of 50 SILVER users ranked by league score
            List<LeagueGroup> members = createGroupMembers(groupId, LeagueTier.SILVER, 50, seasonWeek);

            when(matchmakingService.getCurrentSeasonWeek()).thenReturn(seasonWeek);
            when(leagueGroupRepository.findDistinctGroupIdsBySeasonWeek(seasonWeek))
                    .thenReturn(List.of(groupId));
            when(leagueGroupRepository.findByGroupIdOrderByLeagueScoreDesc(groupId))
                    .thenReturn(members);
            when(leagueGroupRepository.findBySeasonWeek(seasonWeek))
                    .thenReturn(members);

            // Mock user lookups for promoted users
            for (int i = 0; i < 15; i++) {
                UUID uid = members.get(i).getUserId();
                User user = User.builder()
                        .id(uid)
                        .firebaseUid("fb-" + uid)
                        .level(15)
                        .league("SILVER")
                        .build();
                when(userRepository.findById(uid)).thenReturn(Optional.of(user));
                when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            }

            // Mock user lookups for demoted users
            for (int i = 35; i < 50; i++) {
                UUID uid = members.get(i).getUserId();
                User user = User.builder()
                        .id(uid)
                        .firebaseUid("fb-" + uid)
                        .level(15)
                        .league("SILVER")
                        .build();
                when(userRepository.findById(uid)).thenReturn(Optional.of(user));
                when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            }

            // Execute the weekly reset
            leagueResetScheduler.processWeeklyLeagueReset();

            // Verify promotions: 15 LeaguePromotionEvents published
            ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
            verify(eventPublisher, atLeast(15)).publishEvent(eventCaptor.capture());

            List<Object> allEvents = eventCaptor.getAllValues();
            long promotionEvents = allEvents.stream()
                    .filter(e -> e instanceof LeaguePromotionEvent)
                    .count();
            long demotionEvents = allEvents.stream()
                    .filter(e -> e instanceof LeagueDemotionEvent)
                    .count();

            assertThat(promotionEvents)
                    .as("Top 15 users should be promoted")
                    .isEqualTo(15);
            assertThat(demotionEvents)
                    .as("Bottom 15 users should be demoted")
                    .isEqualTo(15);

            // Verify promoted users have their league updated to GOLD
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository, atLeast(30)).save(userCaptor.capture());

            List<User> savedUsers = userCaptor.getAllValues();
            long goldUsers = savedUsers.stream()
                    .filter(u -> "GOLD".equals(u.getLeague()))
                    .count();
            long bronzeUsers = savedUsers.stream()
                    .filter(u -> "BRONZE".equals(u.getLeague()))
                    .count();

            assertThat(goldUsers)
                    .as("15 users should be promoted to GOLD")
                    .isEqualTo(15);
            assertThat(bronzeUsers)
                    .as("15 users should be demoted to BRONZE")
                    .isEqualTo(15);

            // Verify weekly XP was reset
            verify(leaderboardRepository).resetAllWeeklyXp();
        }

        @Test
        @DisplayName("BRONZE tier users cannot be demoted further (no lower tier)")
        void weeklyReset_bronzeUsersNotDemoted() {
            UUID groupId = UUID.randomUUID();
            String seasonWeek = "2024-W10";

            // Create a group of 50 BRONZE users
            List<LeagueGroup> members = createGroupMembers(groupId, LeagueTier.BRONZE, 50, seasonWeek);

            when(matchmakingService.getCurrentSeasonWeek()).thenReturn(seasonWeek);
            when(leagueGroupRepository.findDistinctGroupIdsBySeasonWeek(seasonWeek))
                    .thenReturn(List.of(groupId));
            when(leagueGroupRepository.findByGroupIdOrderByLeagueScoreDesc(groupId))
                    .thenReturn(members);
            when(leagueGroupRepository.findBySeasonWeek(seasonWeek))
                    .thenReturn(members);

            // Mock user lookups for promoted users (top 15 → SILVER)
            for (int i = 0; i < 15; i++) {
                UUID uid = members.get(i).getUserId();
                User user = User.builder()
                        .id(uid)
                        .firebaseUid("fb-" + uid)
                        .level(5)
                        .league("BRONZE")
                        .build();
                when(userRepository.findById(uid)).thenReturn(Optional.of(user));
                when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            }

            leagueResetScheduler.processWeeklyLeagueReset();

            // Verify promotions happened (BRONZE → SILVER)
            ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
            verify(eventPublisher, atLeast(1)).publishEvent(eventCaptor.capture());

            long demotionEvents = eventCaptor.getAllValues().stream()
                    .filter(e -> e instanceof LeagueDemotionEvent)
                    .count();

            // No demotions should occur for BRONZE (no lower tier)
            assertThat(demotionEvents)
                    .as("BRONZE users cannot be demoted — no lower tier exists")
                    .isEqualTo(0);
        }

        @Test
        @DisplayName("MASTER tier users cannot be promoted to ASCENDANT (invite-only)")
        void weeklyReset_masterUsersNotPromotedToAscendant() {
            UUID groupId = UUID.randomUUID();
            String seasonWeek = "2024-W10";

            // Create a group of 50 MASTER users
            List<LeagueGroup> members = createGroupMembers(groupId, LeagueTier.MASTER, 50, seasonWeek);

            when(matchmakingService.getCurrentSeasonWeek()).thenReturn(seasonWeek);
            when(leagueGroupRepository.findDistinctGroupIdsBySeasonWeek(seasonWeek))
                    .thenReturn(List.of(groupId));
            when(leagueGroupRepository.findByGroupIdOrderByLeagueScoreDesc(groupId))
                    .thenReturn(members);
            when(leagueGroupRepository.findBySeasonWeek(seasonWeek))
                    .thenReturn(members);

            // Mock user lookups for demoted users (bottom 15 → DIAMOND)
            for (int i = 35; i < 50; i++) {
                UUID uid = members.get(i).getUserId();
                User user = User.builder()
                        .id(uid)
                        .firebaseUid("fb-" + uid)
                        .level(75)
                        .league("MASTER")
                        .build();
                when(userRepository.findById(uid)).thenReturn(Optional.of(user));
                when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            }

            leagueResetScheduler.processWeeklyLeagueReset();

            // Verify no promotions to ASCENDANT
            ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
            verify(eventPublisher, atLeast(1)).publishEvent(eventCaptor.capture());

            long promotionEvents = eventCaptor.getAllValues().stream()
                    .filter(e -> e instanceof LeaguePromotionEvent)
                    .count();

            assertThat(promotionEvents)
                    .as("MASTER users cannot be promoted to ASCENDANT (invite-only)")
                    .isEqualTo(0);
        }

        @Test
        @DisplayName("Weekly reset with no groups is a no-op")
        void weeklyReset_noGroups_noOp() {
            String seasonWeek = "2024-W10";

            when(matchmakingService.getCurrentSeasonWeek()).thenReturn(seasonWeek);
            when(leagueGroupRepository.findDistinctGroupIdsBySeasonWeek(seasonWeek))
                    .thenReturn(Collections.emptyList());

            leagueResetScheduler.processWeeklyLeagueReset();

            // No events published, no XP reset
            verify(eventPublisher, never()).publishEvent(any());
            verify(leaderboardRepository, never()).resetAllWeeklyXp();
        }

        @Test
        @DisplayName("Groups are reassigned for the new week after reset")
        void weeklyReset_groupsReassignedForNewWeek() {
            UUID groupId = UUID.randomUUID();
            String seasonWeek = "2024-W10";

            List<LeagueGroup> members = createGroupMembers(groupId, LeagueTier.GOLD, 30, seasonWeek);

            when(matchmakingService.getCurrentSeasonWeek()).thenReturn(seasonWeek);
            when(leagueGroupRepository.findDistinctGroupIdsBySeasonWeek(seasonWeek))
                    .thenReturn(List.of(groupId));
            when(leagueGroupRepository.findByGroupIdOrderByLeagueScoreDesc(groupId))
                    .thenReturn(members);
            when(leagueGroupRepository.findBySeasonWeek(seasonWeek))
                    .thenReturn(members);

            // Mock user lookups for promoted users (GOLD → PLATINUM)
            for (int i = 0; i < 15; i++) {
                UUID uid = members.get(i).getUserId();
                User user = User.builder()
                        .id(uid)
                        .firebaseUid("fb-" + uid)
                        .level(25)
                        .league("GOLD")
                        .build();
                when(userRepository.findById(uid)).thenReturn(Optional.of(user));
                when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            }

            // Mock user lookups for demoted users (GOLD → SILVER)
            for (int i = 15; i < 30; i++) {
                UUID uid = members.get(i).getUserId();
                User user = User.builder()
                        .id(uid)
                        .firebaseUid("fb-" + uid)
                        .level(25)
                        .league("GOLD")
                        .build();
                when(userRepository.findById(uid)).thenReturn(Optional.of(user));
                when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            }

            leagueResetScheduler.processWeeklyLeagueReset();

            // Verify old groups are deleted
            verify(leagueGroupRepository).deleteBySeasonWeek(seasonWeek);

            // Verify matchmaking is called for each user to reassign groups
            verify(matchmakingService, times(30)).assignToGroup(any(UUID.class));
        }
    }

    // ========================================================================
    // Integration test: speed violation detected → account flagged
    // ========================================================================

    @Nested
    @DisplayName("Speed violation detected → account flagged")
    class SpeedViolationDetection {

        @Test
        @DisplayName("User with 15 completions in 5 min is flagged, XP rolled back, leaderboard banned")
        void speedViolation_userFlagged_xpRolledBack_leaderboardBanned() {
            // Setup: user has 15 completions in the last 5 minutes (exceeds threshold of 10)
            when(xpHistoryRepository.countByUserIdAndSourceTypeAndCreatedAtAfter(
                    eq(userId), eq("QUEST"), any(LocalDateTime.class)))
                    .thenReturn(15L);

            // XP earned in the violation window
            when(xpHistoryRepository.sumXpAmountByUserIdAndCreatedAtBetween(
                    eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(500L);

            // User exists with 2000 XP
            User user = User.builder()
                    .id(userId)
                    .firebaseUid("fb-cheater")
                    .xp(2000L)
                    .level(20)
                    .build();
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            // Leaderboard entry exists
            Leaderboard leaderboardEntry = Leaderboard.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .weeklyXp(500L)
                    .league("GOLD")
                    .build();
            when(leaderboardRepository.findByUserId(userId))
                    .thenReturn(Optional.of(leaderboardEntry));

            // Security violation save
            when(securityViolationRepository.save(any(SecurityViolation.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Execute
            boolean flagged = antiCheatService.detectSpeedViolation(userId);

            // Verify: user is flagged
            assertThat(flagged).isTrue();

            // Verify: XP was rolled back (2000 - 500 = 1500)
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getXp()).isEqualTo(1500L);

            // Verify: leaderboard entry was deleted (banned)
            verify(leaderboardRepository).delete(leaderboardEntry);

            // Verify: security violation was recorded
            ArgumentCaptor<SecurityViolation> violationCaptor =
                    ArgumentCaptor.forClass(SecurityViolation.class);
            verify(securityViolationRepository).save(violationCaptor.capture());

            SecurityViolation violation = violationCaptor.getValue();
            assertThat(violation.getUserId()).isEqualTo(userId);
            assertThat(violation.getViolationType())
                    .isEqualTo(com.ascend.league.entity.ViolationType.SPEED_VIOLATION);
            assertThat(violation.getCompletionsDetected()).isEqualTo(15);
            assertThat(violation.getTimeWindowMinutes()).isEqualTo(5);
            assertThat(violation.getXpRolledBack()).isEqualTo(500L);
            assertThat(violation.getLeaderboardBanned()).isTrue();
        }

        @Test
        @DisplayName("User with exactly 10 completions in 5 min is NOT flagged")
        void exactlyTenCompletions_notFlagged() {
            when(xpHistoryRepository.countByUserIdAndSourceTypeAndCreatedAtAfter(
                    eq(userId), eq("QUEST"), any(LocalDateTime.class)))
                    .thenReturn(10L);

            boolean flagged = antiCheatService.detectSpeedViolation(userId);

            assertThat(flagged).isFalse();

            // Verify no penalties applied
            verify(userRepository, never()).save(any(User.class));
            verify(leaderboardRepository, never()).delete(any(Leaderboard.class));
            verify(securityViolationRepository, never()).save(any(SecurityViolation.class));
        }

        @Test
        @DisplayName("User with 5 completions in 5 min is NOT flagged")
        void fiveCompletions_notFlagged() {
            when(xpHistoryRepository.countByUserIdAndSourceTypeAndCreatedAtAfter(
                    eq(userId), eq("QUEST"), any(LocalDateTime.class)))
                    .thenReturn(5L);

            boolean flagged = antiCheatService.detectSpeedViolation(userId);

            assertThat(flagged).isFalse();
            verify(securityViolationRepository, never()).save(any(SecurityViolation.class));
        }

        @Test
        @DisplayName("Speed violation with no leaderboard entry still flags account")
        void speedViolation_noLeaderboardEntry_stillFlagged() {
            when(xpHistoryRepository.countByUserIdAndSourceTypeAndCreatedAtAfter(
                    eq(userId), eq("QUEST"), any(LocalDateTime.class)))
                    .thenReturn(20L);

            when(xpHistoryRepository.sumXpAmountByUserIdAndCreatedAtBetween(
                    eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(300L);

            User user = User.builder()
                    .id(userId)
                    .firebaseUid("fb-cheater2")
                    .xp(1000L)
                    .level(10)
                    .build();
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            // No leaderboard entry
            when(leaderboardRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(securityViolationRepository.save(any(SecurityViolation.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            boolean flagged = antiCheatService.detectSpeedViolation(userId);

            assertThat(flagged).isTrue();

            // XP still rolled back
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getXp()).isEqualTo(700L);

            // Violation still recorded
            verify(securityViolationRepository).save(any(SecurityViolation.class));

            // No leaderboard delete (nothing to delete)
            verify(leaderboardRepository, never()).delete(any(Leaderboard.class));
        }

        @Test
        @DisplayName("XP rollback does not go below zero")
        void speedViolation_xpRollbackDoesNotGoBelowZero() {
            when(xpHistoryRepository.countByUserIdAndSourceTypeAndCreatedAtAfter(
                    eq(userId), eq("QUEST"), any(LocalDateTime.class)))
                    .thenReturn(25L);

            // XP in window exceeds user's total XP
            when(xpHistoryRepository.sumXpAmountByUserIdAndCreatedAtBetween(
                    eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(5000L);

            User user = User.builder()
                    .id(userId)
                    .firebaseUid("fb-cheater3")
                    .xp(200L)
                    .level(5)
                    .build();
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(leaderboardRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(securityViolationRepository.save(any(SecurityViolation.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            boolean flagged = antiCheatService.detectSpeedViolation(userId);

            assertThat(flagged).isTrue();

            // XP should be clamped to 0, not negative
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getXp()).isEqualTo(0L);
        }
    }

    // ========================================================================
    // Helper methods
    // ========================================================================

    /**
     * Creates a list of LeagueGroup members for testing, ordered by league score descending.
     */
    private List<LeagueGroup> createGroupMembers(UUID groupId, LeagueTier tier, int count, String seasonWeek) {
        List<LeagueGroup> members = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            LeagueGroup member = LeagueGroup.builder()
                    .id(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .groupId(groupId)
                    .tier(tier)
                    .leagueScore(100.0 - i) // Descending scores: 100, 99, 98, ...
                    .seasonWeek(seasonWeek)
                    .build();
            members.add(member);
        }
        return members;
    }
}
