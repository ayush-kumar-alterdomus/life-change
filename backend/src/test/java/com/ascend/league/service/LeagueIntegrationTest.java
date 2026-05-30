package com.ascend.league.service;

import com.ascend.league.entity.Leaderboard;
import com.ascend.league.entity.LeagueGroup;
import com.ascend.league.entity.LeagueTier;
import com.ascend.league.entity.SecurityViolation;
import com.ascend.league.entity.ViolationType;
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
 * Integration tests for the League system checkpoint (Task 7).
 * Verifies end-to-end flows using JUnit 5 with Mockito:
 * <ul>
 *   <li>User levels up → tier assignment updates correctly</li>
 *   <li>Weekly reset → promotions and demotions applied</li>
 *   <li>Speed violation detected → account flagged</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class LeagueIntegrationTest {

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
    @DisplayName("User levels up → tier assignment updates")
    class UserLevelUpTierAssignment {

        @Test
        @DisplayName("Level 0 user is assigned BRONZE tier")
        void level0_assignedBronze() {
            assertThat(leagueService.assignTier(0)).isEqualTo(LeagueTier.BRONZE);
        }

        @Test
        @DisplayName("Level 9→10 transition: BRONZE → SILVER")
        void levelUpFrom9To10_bronzeToSilver() {
            LeagueTier before = leagueService.assignTier(9);
            LeagueTier after = leagueService.assignTier(10);

            assertThat(before).isEqualTo(LeagueTier.BRONZE);
            assertThat(after).isEqualTo(LeagueTier.SILVER);
        }

        @Test
        @DisplayName("Level 19→20 transition: SILVER → GOLD")
        void levelUpFrom19To20_silverToGold() {
            LeagueTier before = leagueService.assignTier(19);
            LeagueTier after = leagueService.assignTier(20);

            assertThat(before).isEqualTo(LeagueTier.SILVER);
            assertThat(after).isEqualTo(LeagueTier.GOLD);
        }

        @Test
        @DisplayName("Level 34→35 transition: GOLD → PLATINUM")
        void levelUpFrom34To35_goldToPlatinum() {
            LeagueTier before = leagueService.assignTier(34);
            LeagueTier after = leagueService.assignTier(35);

            assertThat(before).isEqualTo(LeagueTier.GOLD);
            assertThat(after).isEqualTo(LeagueTier.PLATINUM);
        }

        @Test
        @DisplayName("Level 49→50 transition: PLATINUM → DIAMOND")
        void levelUpFrom49To50_platinumToDiamond() {
            LeagueTier before = leagueService.assignTier(49);
            LeagueTier after = leagueService.assignTier(50);

            assertThat(before).isEqualTo(LeagueTier.PLATINUM);
            assertThat(after).isEqualTo(LeagueTier.DIAMOND);
        }

        @Test
        @DisplayName("Level 74→75 transition: DIAMOND → MASTER")
        void levelUpFrom74To75_diamondToMaster() {
            LeagueTier before = leagueService.assignTier(74);
            LeagueTier after = leagueService.assignTier(75);

            assertThat(before).isEqualTo(LeagueTier.DIAMOND);
            assertThat(after).isEqualTo(LeagueTier.MASTER);
        }

        @Test
        @DisplayName("Level 100+ remains MASTER — ASCENDANT is invite-only")
        void level100Plus_remainsMaster() {
            assertThat(leagueService.assignTier(100)).isEqualTo(LeagueTier.MASTER);
            assertThat(leagueService.assignTier(200)).isEqualTo(LeagueTier.MASTER);
            assertThat(leagueService.assignTier(999)).isEqualTo(LeagueTier.MASTER);
        }

        @Test
        @DisplayName("Full user journey: tier updates at each level milestone")
        void fullUserJourney_tierUpdatesAtEachMilestone() {
            User user = User.builder()
                    .id(userId)
                    .firebaseUid("fb-journey")
                    .level(1)
                    .league("BRONZE")
                    .build();

            // Start at level 1 → BRONZE
            assertThat(leagueService.assignTier(user.getLevel())).isEqualTo(LeagueTier.BRONZE);

            // Level up to 12 → SILVER
            user.setLevel(12);
            LeagueTier tier = leagueService.assignTier(user.getLevel());
            assertThat(tier).isEqualTo(LeagueTier.SILVER);
            user.setLeague(tier.name());

            // Level up to 25 → GOLD
            user.setLevel(25);
            tier = leagueService.assignTier(user.getLevel());
            assertThat(tier).isEqualTo(LeagueTier.GOLD);
            user.setLeague(tier.name());

            // Level up to 40 → PLATINUM
            user.setLevel(40);
            tier = leagueService.assignTier(user.getLevel());
            assertThat(tier).isEqualTo(LeagueTier.PLATINUM);
            user.setLeague(tier.name());

            // Level up to 60 → DIAMOND
            user.setLevel(60);
            tier = leagueService.assignTier(user.getLevel());
            assertThat(tier).isEqualTo(LeagueTier.DIAMOND);
            user.setLeague(tier.name());

            // Level up to 80 → MASTER
            user.setLevel(80);
            tier = leagueService.assignTier(user.getLevel());
            assertThat(tier).isEqualTo(LeagueTier.MASTER);
            user.setLeague(tier.name());

            assertThat(user.getLeague()).isEqualTo("MASTER");
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
        @DisplayName("Top 15 users promoted, bottom 15 demoted in a SILVER group of 50")
        void weeklyReset_promotesTop15_demotesBottom15() {
            UUID groupId = UUID.randomUUID();
            String seasonWeek = "2025-W03";

            List<LeagueGroup> members = createGroupMembers(groupId, LeagueTier.SILVER, 50, seasonWeek);

            when(matchmakingService.getCurrentSeasonWeek()).thenReturn(seasonWeek);
            when(leagueGroupRepository.findDistinctGroupIdsBySeasonWeek(seasonWeek))
                    .thenReturn(List.of(groupId));
            when(leagueGroupRepository.findByGroupIdOrderByLeagueScoreDesc(groupId))
                    .thenReturn(members);
            when(leagueGroupRepository.findBySeasonWeek(seasonWeek))
                    .thenReturn(members);

            // Mock user lookups for promoted users (top 15 → GOLD)
            for (int i = 0; i < 15; i++) {
                UUID uid = members.get(i).getUserId();
                User user = User.builder()
                        .id(uid).firebaseUid("fb-" + uid).level(15).league("SILVER").build();
                when(userRepository.findById(uid)).thenReturn(Optional.of(user));
                when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            }

            // Mock user lookups for demoted users (bottom 15 → BRONZE)
            for (int i = 35; i < 50; i++) {
                UUID uid = members.get(i).getUserId();
                User user = User.builder()
                        .id(uid).firebaseUid("fb-" + uid).level(15).league("SILVER").build();
                when(userRepository.findById(uid)).thenReturn(Optional.of(user));
                when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            }

            // Execute
            leagueResetScheduler.processWeeklyLeagueReset();

            // Verify promotion and demotion events
            ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
            verify(eventPublisher, atLeast(30)).publishEvent(eventCaptor.capture());

            List<Object> allEvents = eventCaptor.getAllValues();
            long promotionCount = allEvents.stream()
                    .filter(e -> e instanceof LeaguePromotionEvent).count();
            long demotionCount = allEvents.stream()
                    .filter(e -> e instanceof LeagueDemotionEvent).count();

            assertThat(promotionCount).as("Top 15 should be promoted").isEqualTo(15);
            assertThat(demotionCount).as("Bottom 15 should be demoted").isEqualTo(15);

            // Verify user league field updates
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository, atLeast(30)).save(userCaptor.capture());

            long promotedToGold = userCaptor.getAllValues().stream()
                    .filter(u -> "GOLD".equals(u.getLeague())).count();
            long demotedToBronze = userCaptor.getAllValues().stream()
                    .filter(u -> "BRONZE".equals(u.getLeague())).count();

            assertThat(promotedToGold).isEqualTo(15);
            assertThat(demotedToBronze).isEqualTo(15);

            // Verify weekly XP reset
            verify(leaderboardRepository).resetAllWeeklyXp();
        }

        @Test
        @DisplayName("BRONZE users cannot be demoted (no lower tier)")
        void weeklyReset_bronzeCannotBeDemoted() {
            UUID groupId = UUID.randomUUID();
            String seasonWeek = "2025-W03";

            List<LeagueGroup> members = createGroupMembers(groupId, LeagueTier.BRONZE, 50, seasonWeek);

            when(matchmakingService.getCurrentSeasonWeek()).thenReturn(seasonWeek);
            when(leagueGroupRepository.findDistinctGroupIdsBySeasonWeek(seasonWeek))
                    .thenReturn(List.of(groupId));
            when(leagueGroupRepository.findByGroupIdOrderByLeagueScoreDesc(groupId))
                    .thenReturn(members);
            when(leagueGroupRepository.findBySeasonWeek(seasonWeek))
                    .thenReturn(members);

            // Mock promoted users (top 15 → SILVER)
            for (int i = 0; i < 15; i++) {
                UUID uid = members.get(i).getUserId();
                User user = User.builder()
                        .id(uid).firebaseUid("fb-" + uid).level(5).league("BRONZE").build();
                when(userRepository.findById(uid)).thenReturn(Optional.of(user));
                when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            }

            leagueResetScheduler.processWeeklyLeagueReset();

            // Verify no demotion events
            ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
            verify(eventPublisher, atLeast(1)).publishEvent(eventCaptor.capture());

            long demotionCount = eventCaptor.getAllValues().stream()
                    .filter(e -> e instanceof LeagueDemotionEvent).count();

            assertThat(demotionCount).as("BRONZE cannot be demoted").isEqualTo(0);
        }

        @Test
        @DisplayName("MASTER users cannot be promoted to ASCENDANT (invite-only)")
        void weeklyReset_masterCannotPromoteToAscendant() {
            UUID groupId = UUID.randomUUID();
            String seasonWeek = "2025-W03";

            List<LeagueGroup> members = createGroupMembers(groupId, LeagueTier.MASTER, 50, seasonWeek);

            when(matchmakingService.getCurrentSeasonWeek()).thenReturn(seasonWeek);
            when(leagueGroupRepository.findDistinctGroupIdsBySeasonWeek(seasonWeek))
                    .thenReturn(List.of(groupId));
            when(leagueGroupRepository.findByGroupIdOrderByLeagueScoreDesc(groupId))
                    .thenReturn(members);
            when(leagueGroupRepository.findBySeasonWeek(seasonWeek))
                    .thenReturn(members);

            // Mock demoted users (bottom 15 → DIAMOND)
            for (int i = 35; i < 50; i++) {
                UUID uid = members.get(i).getUserId();
                User user = User.builder()
                        .id(uid).firebaseUid("fb-" + uid).level(75).league("MASTER").build();
                when(userRepository.findById(uid)).thenReturn(Optional.of(user));
                when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            }

            leagueResetScheduler.processWeeklyLeagueReset();

            // Verify no promotion events (MASTER → ASCENDANT blocked)
            ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
            verify(eventPublisher, atLeast(1)).publishEvent(eventCaptor.capture());

            long promotionCount = eventCaptor.getAllValues().stream()
                    .filter(e -> e instanceof LeaguePromotionEvent).count();

            assertThat(promotionCount).as("MASTER cannot promote to ASCENDANT").isEqualTo(0);
        }

        @Test
        @DisplayName("Empty groups result in no-op reset")
        void weeklyReset_noGroups_noOp() {
            String seasonWeek = "2025-W03";

            when(matchmakingService.getCurrentSeasonWeek()).thenReturn(seasonWeek);
            when(leagueGroupRepository.findDistinctGroupIdsBySeasonWeek(seasonWeek))
                    .thenReturn(Collections.emptyList());

            leagueResetScheduler.processWeeklyLeagueReset();

            verify(eventPublisher, never()).publishEvent(any());
            verify(leaderboardRepository, never()).resetAllWeeklyXp();
        }

        @Test
        @DisplayName("Groups are reassigned for the new week after reset completes")
        void weeklyReset_reassignsGroupsForNewWeek() {
            UUID groupId = UUID.randomUUID();
            String seasonWeek = "2025-W03";

            List<LeagueGroup> members = createGroupMembers(groupId, LeagueTier.GOLD, 20, seasonWeek);

            when(matchmakingService.getCurrentSeasonWeek()).thenReturn(seasonWeek);
            when(leagueGroupRepository.findDistinctGroupIdsBySeasonWeek(seasonWeek))
                    .thenReturn(List.of(groupId));
            when(leagueGroupRepository.findByGroupIdOrderByLeagueScoreDesc(groupId))
                    .thenReturn(members);
            when(leagueGroupRepository.findBySeasonWeek(seasonWeek))
                    .thenReturn(members);

            // Mock user lookups for all users (promoted + demoted in a small group)
            for (LeagueGroup member : members) {
                UUID uid = member.getUserId();
                User user = User.builder()
                        .id(uid).firebaseUid("fb-" + uid).level(25).league("GOLD").build();
                when(userRepository.findById(uid)).thenReturn(Optional.of(user));
                when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            }

            leagueResetScheduler.processWeeklyLeagueReset();

            // Verify old groups deleted
            verify(leagueGroupRepository).deleteBySeasonWeek(seasonWeek);

            // Verify matchmaking called for each user
            verify(matchmakingService, times(20)).assignToGroup(any(UUID.class));
        }
    }

    // ========================================================================
    // Integration test: speed violation detected → account flagged
    // ========================================================================

    @Nested
    @DisplayName("Speed violation detected → account flagged")
    class SpeedViolationAccountFlagged {

        @Test
        @DisplayName("User with >10 completions in 5 min: flagged, XP rolled back, leaderboard banned")
        void speedViolation_fullPenaltyApplied() {
            // User has 15 completions in 5 minutes (exceeds threshold of 10)
            when(xpHistoryRepository.countByUserIdAndSourceTypeAndCreatedAtAfter(
                    eq(userId), eq("QUEST"), any(LocalDateTime.class)))
                    .thenReturn(15L);

            // 500 XP earned in violation window
            when(xpHistoryRepository.sumXpAmountByUserIdAndCreatedAtBetween(
                    eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(500L);

            // User has 2000 XP
            User user = User.builder()
                    .id(userId).firebaseUid("fb-cheater").xp(2000L).level(20).build();
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            // Leaderboard entry exists
            Leaderboard entry = Leaderboard.builder()
                    .id(UUID.randomUUID()).userId(userId).weeklyXp(500L).league("GOLD").build();
            when(leaderboardRepository.findByUserId(userId)).thenReturn(Optional.of(entry));

            when(securityViolationRepository.save(any(SecurityViolation.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Execute
            boolean flagged = antiCheatService.detectSpeedViolation(userId);

            // Verify flagged
            assertThat(flagged).isTrue();

            // Verify XP rolled back: 2000 - 500 = 1500
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getXp()).isEqualTo(1500L);

            // Verify leaderboard ban
            verify(leaderboardRepository).delete(entry);

            // Verify security violation recorded
            ArgumentCaptor<SecurityViolation> violationCaptor =
                    ArgumentCaptor.forClass(SecurityViolation.class);
            verify(securityViolationRepository).save(violationCaptor.capture());

            SecurityViolation violation = violationCaptor.getValue();
            assertThat(violation.getUserId()).isEqualTo(userId);
            assertThat(violation.getViolationType()).isEqualTo(ViolationType.SPEED_VIOLATION);
            assertThat(violation.getCompletionsDetected()).isEqualTo(15);
            assertThat(violation.getTimeWindowMinutes()).isEqualTo(5);
            assertThat(violation.getXpRolledBack()).isEqualTo(500L);
            assertThat(violation.getLeaderboardBanned()).isTrue();
        }

        @Test
        @DisplayName("Exactly 10 completions in 5 min: NOT flagged (threshold is >10)")
        void exactlyTenCompletions_notFlagged() {
            when(xpHistoryRepository.countByUserIdAndSourceTypeAndCreatedAtAfter(
                    eq(userId), eq("QUEST"), any(LocalDateTime.class)))
                    .thenReturn(10L);

            boolean flagged = antiCheatService.detectSpeedViolation(userId);

            assertThat(flagged).isFalse();
            verify(userRepository, never()).save(any(User.class));
            verify(leaderboardRepository, never()).delete(any(Leaderboard.class));
            verify(securityViolationRepository, never()).save(any(SecurityViolation.class));
        }

        @Test
        @DisplayName("Normal usage (5 completions) is not flagged")
        void normalUsage_notFlagged() {
            when(xpHistoryRepository.countByUserIdAndSourceTypeAndCreatedAtAfter(
                    eq(userId), eq("QUEST"), any(LocalDateTime.class)))
                    .thenReturn(5L);

            boolean flagged = antiCheatService.detectSpeedViolation(userId);

            assertThat(flagged).isFalse();
            verify(securityViolationRepository, never()).save(any(SecurityViolation.class));
        }

        @Test
        @DisplayName("Speed violation with no leaderboard entry still flags and rolls back XP")
        void speedViolation_noLeaderboardEntry_stillFlagged() {
            when(xpHistoryRepository.countByUserIdAndSourceTypeAndCreatedAtAfter(
                    eq(userId), eq("QUEST"), any(LocalDateTime.class)))
                    .thenReturn(20L);

            when(xpHistoryRepository.sumXpAmountByUserIdAndCreatedAtBetween(
                    eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(300L);

            User user = User.builder()
                    .id(userId).firebaseUid("fb-cheater2").xp(1000L).level(10).build();
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            when(leaderboardRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(securityViolationRepository.save(any(SecurityViolation.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            boolean flagged = antiCheatService.detectSpeedViolation(userId);

            assertThat(flagged).isTrue();

            // XP rolled back: 1000 - 300 = 700
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getXp()).isEqualTo(700L);

            // Violation recorded
            verify(securityViolationRepository).save(any(SecurityViolation.class));

            // No leaderboard delete (nothing to delete)
            verify(leaderboardRepository, never()).delete(any(Leaderboard.class));
        }

        @Test
        @DisplayName("XP rollback clamps to zero when violation XP exceeds user total")
        void speedViolation_xpClampsToZero() {
            when(xpHistoryRepository.countByUserIdAndSourceTypeAndCreatedAtAfter(
                    eq(userId), eq("QUEST"), any(LocalDateTime.class)))
                    .thenReturn(30L);

            // Violation XP exceeds user's total
            when(xpHistoryRepository.sumXpAmountByUserIdAndCreatedAtBetween(
                    eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(5000L);

            User user = User.builder()
                    .id(userId).firebaseUid("fb-cheater3").xp(200L).level(5).build();
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(leaderboardRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(securityViolationRepository.save(any(SecurityViolation.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            boolean flagged = antiCheatService.detectSpeedViolation(userId);

            assertThat(flagged).isTrue();

            // XP clamped to 0
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getXp()).isEqualTo(0L);
        }
    }

    // ========================================================================
    // Helpers
    // ========================================================================

    /**
     * Creates a list of LeagueGroup members ordered by league score descending.
     */
    private List<LeagueGroup> createGroupMembers(UUID groupId, LeagueTier tier, int count, String seasonWeek) {
        List<LeagueGroup> members = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            LeagueGroup member = LeagueGroup.builder()
                    .id(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .groupId(groupId)
                    .tier(tier)
                    .leagueScore(100.0 - i) // Descending: 100, 99, 98, ...
                    .seasonWeek(seasonWeek)
                    .build();
            members.add(member);
        }
        return members;
    }
}
