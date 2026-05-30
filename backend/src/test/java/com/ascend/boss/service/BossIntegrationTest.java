package com.ascend.boss.service;

import com.ascend.boss.dto.BossDefeatResponse;
import com.ascend.boss.dto.BossResponse;
import com.ascend.boss.entity.Boss;
import com.ascend.boss.entity.BossProgress;
import com.ascend.boss.entity.GuildBossProgress;
import com.ascend.boss.event.BossDefeatedEvent;
import com.ascend.boss.repository.BossProgressRepository;
import com.ascend.boss.repository.BossRepository;
import com.ascend.boss.repository.GuildBossProgressRepository;
import com.ascend.guild.entity.GuildMember;
import com.ascend.guild.repository.GuildMemberRepository;
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
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BossIntegrationTest {

    @Mock private BossRepository bossRepository;
    @Mock private BossProgressRepository bossProgressRepository;
    @Mock private GuildBossProgressRepository guildBossProgressRepository;
    @Mock private GuildMemberRepository guildMemberRepository;
    @Mock private UserRepository userRepository;
    @Mock private XpHistoryRepository xpHistoryRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    private BossService bossService;
    private GuildBossService guildBossService;

    private UUID userId;
    private UUID bossId;
    private UUID guildId;

    @BeforeEach
    void setUp() {
        bossService = new BossService(bossRepository, bossProgressRepository,
                userRepository, xpHistoryRepository, eventPublisher);
        guildBossService = new GuildBossService(bossRepository, guildBossProgressRepository,
                guildMemberRepository, userRepository, xpHistoryRepository, eventPublisher);
        userId = UUID.randomUUID();
        bossId = UUID.randomUUID();
        guildId = UUID.randomUUID();
    }

    // ========================================================================
    // Integration test: quest completions → boss damage → stage advance → defeat → rewards
    // ========================================================================

    @Nested
    @DisplayName("Quest completions → boss damage → stage advance → defeat → rewards")
    class BossDamageAndDefeat {

        @Test
        @DisplayName("Damage advances boss stage when threshold crossed")
        void damage_advancesStage() {
            Boss boss = Boss.builder()
                    .id(bossId).name("Shadow Dragon").totalStages(3)
                    .stageThresholds(List.of(33, 66, 100))
                    .rewardXp(500).rewardTitle("Dragon Slayer").rewardCosmetic("Dragon Wings")
                    .guildBoss(false).build();

            BossProgress progress = BossProgress.builder()
                    .id(UUID.randomUUID()).userId(userId).bossId(bossId)
                    .currentStage(1).progressPercent(30).defeated(false).build();

            when(bossRepository.findById(bossId)).thenReturn(Optional.of(boss));
            when(bossProgressRepository.findByUserIdAndBossId(userId, bossId))
                    .thenReturn(Optional.of(progress));
            when(bossProgressRepository.save(any(BossProgress.class))).thenAnswer(inv -> inv.getArgument(0));

            // Apply 10% damage → 30 + 10 = 40% → crosses 33% threshold → stage 2
            BossResponse result = bossService.contributeToBoss(userId, bossId, 10);

            assertThat(progress.getProgressPercent()).isEqualTo(40);
            assertThat(progress.getCurrentStage()).isEqualTo(2);
            assertThat(result.isDefeated()).isFalse();
        }

        @Test
        @DisplayName("Boss defeated at 100% final stage — rewards awarded")
        void bossDefeated_rewardsAwarded() {
            Boss boss = Boss.builder()
                    .id(bossId).name("Shadow Dragon").totalStages(3)
                    .stageThresholds(List.of(33, 66, 100))
                    .rewardXp(500).rewardTitle("Dragon Slayer").rewardCosmetic("Dragon Wings")
                    .guildBoss(false).build();

            BossProgress progress = BossProgress.builder()
                    .id(UUID.randomUUID()).userId(userId).bossId(bossId)
                    .currentStage(3).progressPercent(90).defeated(false).build();

            User user = User.builder().id(userId).firebaseUid("fb-user").xp(1000L).level(20).build();

            when(bossRepository.findById(bossId)).thenReturn(Optional.of(boss));
            when(bossProgressRepository.findByUserIdAndBossId(userId, bossId))
                    .thenReturn(Optional.of(progress));
            when(bossProgressRepository.save(any(BossProgress.class))).thenAnswer(inv -> inv.getArgument(0));
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(xpHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // Apply 25% damage → 90 + 25 = 115 → capped at 100 → defeated
            bossService.contributeToBoss(userId, bossId, 25);

            // Verify defeated
            assertThat(progress.getDefeated()).isTrue();
            assertThat(progress.getDefeatedAt()).isNotNull();

            // Verify XP awarded
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getXp()).isEqualTo(1500L); // 1000 + 500

            // Verify event published
            verify(eventPublisher).publishEvent(any(BossDefeatedEvent.class));
        }

        @Test
        @DisplayName("Already defeated boss ignores further damage")
        void alreadyDefeated_ignoresDamage() {
            Boss boss = Boss.builder()
                    .id(bossId).name("Shadow Dragon").totalStages(3)
                    .stageThresholds(List.of(33, 66, 100))
                    .rewardXp(500).guildBoss(false).build();

            BossProgress progress = BossProgress.builder()
                    .id(UUID.randomUUID()).userId(userId).bossId(bossId)
                    .currentStage(3).progressPercent(100).defeated(true)
                    .defeatedAt(LocalDateTime.now()).build();

            when(bossRepository.findById(bossId)).thenReturn(Optional.of(boss));
            when(bossProgressRepository.findByUserIdAndBossId(userId, bossId))
                    .thenReturn(Optional.of(progress));

            BossResponse result = bossService.contributeToBoss(userId, bossId, 10);

            assertThat(result.isDefeated()).isTrue();
            verify(bossProgressRepository, never()).save(any());
        }
    }

    // ========================================================================
    // Integration test: guild members contribute → collective progress → guild boss defeated
    // ========================================================================

    @Nested
    @DisplayName("Guild members contribute → collective progress → guild boss defeated")
    class GuildBossDefeat {

        @Test
        @DisplayName("Multiple guild members contribute collectively to defeat boss")
        void guildMembersContribute_bossDefeated() {
            UUID member1 = UUID.randomUUID();
            UUID member2 = UUID.randomUUID();

            Boss boss = Boss.builder()
                    .id(bossId).name("World Serpent").totalStages(2)
                    .stageThresholds(List.of(50, 100))
                    .rewardXp(1000).rewardTitle("Serpent Slayer").rewardCosmetic("Serpent Scale")
                    .guildBoss(true).build();

            GuildBossProgress progress = GuildBossProgress.builder()
                    .id(UUID.randomUUID()).guildId(guildId).bossId(bossId)
                    .currentStage(2).progressPercent(85).defeated(false).build();

            List<GuildMember> members = List.of(
                    GuildMember.builder().userId(member1).guildId(guildId).build(),
                    GuildMember.builder().userId(member2).guildId(guildId).build()
            );

            User user1 = User.builder().id(member1).firebaseUid("fb-1").xp(500L).build();
            User user2 = User.builder().id(member2).firebaseUid("fb-2").xp(800L).build();

            when(bossRepository.findById(bossId)).thenReturn(Optional.of(boss));
            when(guildBossProgressRepository.findByGuildIdAndBossId(guildId, bossId))
                    .thenReturn(Optional.of(progress));
            when(guildBossProgressRepository.save(any(GuildBossProgress.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(guildMemberRepository.existsByGuildIdAndUserId(guildId, member1)).thenReturn(true);
            when(guildMemberRepository.findByGuildId(guildId)).thenReturn(members);
            when(userRepository.findById(member1)).thenReturn(Optional.of(user1));
            when(userRepository.findById(member2)).thenReturn(Optional.of(user2));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(xpHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // Member 1 contributes 25% → 85 + 25 = 110 → capped at 100 → defeated
            guildBossService.contributeToGuildBoss(member1, guildId, bossId, 25);

            // Verify defeated
            assertThat(progress.getDefeated()).isTrue();
            assertThat(progress.getDefeatedAt()).isNotNull();

            // Verify rewards distributed to ALL guild members
            verify(userRepository, times(2)).save(any(User.class));
            verify(eventPublisher, times(2)).publishEvent(any(BossDefeatedEvent.class));
        }

        @Test
        @DisplayName("Partial guild contribution does not defeat boss")
        void partialContribution_doesNotDefeat() {
            Boss boss = Boss.builder()
                    .id(bossId).name("World Serpent").totalStages(2)
                    .stageThresholds(List.of(50, 100))
                    .rewardXp(1000).guildBoss(true).build();

            GuildBossProgress progress = GuildBossProgress.builder()
                    .id(UUID.randomUUID()).guildId(guildId).bossId(bossId)
                    .currentStage(1).progressPercent(20).defeated(false).build();

            when(bossRepository.findById(bossId)).thenReturn(Optional.of(boss));
            when(guildBossProgressRepository.findByGuildIdAndBossId(guildId, bossId))
                    .thenReturn(Optional.of(progress));
            when(guildBossProgressRepository.save(any(GuildBossProgress.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(guildMemberRepository.existsByGuildIdAndUserId(guildId, userId)).thenReturn(true);

            guildBossService.contributeToGuildBoss(userId, guildId, bossId, 10);

            assertThat(progress.getProgressPercent()).isEqualTo(30);
            assertThat(progress.getDefeated()).isFalse();
            verify(eventPublisher, never()).publishEvent(any(BossDefeatedEvent.class));
        }
    }
}
