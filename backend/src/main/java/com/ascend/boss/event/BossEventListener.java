package com.ascend.boss.event;

import com.ascend.boss.entity.BossProgress;
import com.ascend.boss.entity.GuildBossProgress;
import com.ascend.boss.repository.BossProgressRepository;
import com.ascend.boss.repository.GuildBossProgressRepository;
import com.ascend.boss.service.BossProgressCalculator;
import com.ascend.boss.service.BossService;
import com.ascend.boss.service.GuildBossService;
import com.ascend.guild.entity.GuildMember;
import com.ascend.guild.repository.GuildMemberRepository;
import com.ascend.quest.event.QuestCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Listens for QuestCompletedEvent and applies boss damage when the user
 * has an active (non-defeated) boss. Damage is calculated based on quest
 * difficulty via BossProgressCalculator and applied through BossService.
 * Also contributes to guild bosses if the user's guild has an active boss.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BossEventListener {

    private final BossProgressRepository bossProgressRepository;
    private final GuildBossProgressRepository guildBossProgressRepository;
    private final BossProgressCalculator bossProgressCalculator;
    private final BossService bossService;
    private final GuildBossService guildBossService;
    private final GuildMemberRepository guildMemberRepository;

    /**
     * Handles quest completion by checking if the user has any active bosses
     * and applying calculated damage to each one. Also contributes to guild
     * bosses if the user belongs to a guild with an active boss.
     *
     * @param event the quest completed event
     */
    @EventListener
    public void handleQuestCompleted(QuestCompletedEvent event) {
        handleIndividualBosses(event);
        handleGuildBosses(event);
    }

    /**
     * Applies damage to the user's individual active bosses.
     */
    private void handleIndividualBosses(QuestCompletedEvent event) {
        try {
            // 1. Check if user has active (non-defeated) boss
            List<BossProgress> activeBosses = bossProgressRepository
                    .findByUserIdAndDefeatedFalse(event.getUserId());

            if (activeBosses.isEmpty()) {
                log.debug("User {} has no active bosses, skipping individual boss damage", event.getUserId());
                return;
            }

            // 2. Calculate damage from quest and apply to each active boss
            for (BossProgress bossProgress : activeBosses) {
                int damage = bossProgressCalculator.calculateDamage(
                        event.getDifficulty(),
                        event.getStatType(),
                        bossProgress.getBossId()
                );

                log.debug("Applying {} damage to boss {} for user {} (quest: {})",
                        damage, bossProgress.getBossId(), event.getUserId(), event.getQuestId());

                // 3. Apply damage to boss progress
                bossService.contributeToBoss(
                        event.getUserId(),
                        bossProgress.getBossId(),
                        damage
                );
            }
        } catch (Exception e) {
            log.error("Failed to process individual boss damage for user {} on quest {}: {}",
                    event.getUserId(), event.getQuestId(), e.getMessage(), e);
        }
    }

    /**
     * Contributes damage to guild bosses if the user belongs to a guild
     * with an active (non-defeated) guild boss.
     */
    private void handleGuildBosses(QuestCompletedEvent event) {
        try {
            // Find all guilds the user belongs to
            List<GuildMember> memberships = guildMemberRepository.findByUserId(event.getUserId());

            if (memberships.isEmpty()) {
                log.debug("User {} has no guild memberships, skipping guild boss damage", event.getUserId());
                return;
            }

            for (GuildMember membership : memberships) {
                // Check if this guild has active guild bosses
                List<GuildBossProgress> activeGuildBosses = guildBossProgressRepository
                        .findByGuildIdAndDefeatedFalse(membership.getGuildId());

                if (activeGuildBosses.isEmpty()) {
                    continue;
                }

                // Apply damage to each active guild boss
                for (GuildBossProgress guildBossProgress : activeGuildBosses) {
                    int damage = bossProgressCalculator.calculateDamage(
                            event.getDifficulty(),
                            event.getStatType(),
                            guildBossProgress.getBossId()
                    );

                    log.debug("Applying {} guild boss damage to boss {} for guild {} (user: {}, quest: {})",
                            damage, guildBossProgress.getBossId(), membership.getGuildId(),
                            event.getUserId(), event.getQuestId());

                    guildBossService.contributeToGuildBoss(
                            event.getUserId(),
                            membership.getGuildId(),
                            guildBossProgress.getBossId(),
                            damage
                    );
                }
            }
        } catch (Exception e) {
            log.error("Failed to process guild boss damage for user {} on quest {}: {}",
                    event.getUserId(), event.getQuestId(), e.getMessage(), e);
        }
    }
}
