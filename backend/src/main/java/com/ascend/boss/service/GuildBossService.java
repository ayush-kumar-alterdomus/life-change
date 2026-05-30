package com.ascend.boss.service;

import com.ascend.boss.dto.BossResponse;
import com.ascend.boss.entity.Boss;
import com.ascend.boss.entity.GuildBossProgress;
import com.ascend.boss.event.BossDefeatedEvent;
import com.ascend.boss.repository.BossRepository;
import com.ascend.boss.repository.GuildBossProgressRepository;
import com.ascend.guild.entity.GuildMember;
import com.ascend.guild.repository.GuildMemberRepository;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import com.ascend.xp.entity.XpHistory;
import com.ascend.xp.repository.XpHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for guild boss battle operations. Guild bosses aggregate damage
 * from all guild members collectively, and rewards are distributed to
 * all members upon defeat.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GuildBossService {

    private final BossRepository bossRepository;
    private final GuildBossProgressRepository guildBossProgressRepository;
    private final GuildMemberRepository guildMemberRepository;
    private final UserRepository userRepository;
    private final XpHistoryRepository xpHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Contributes damage to a guild boss. Damage is collective from all guild members.
     * Advances stage if threshold is crossed, and defeats the boss if the final stage
     * reaches 100%, awarding rewards to all guild members.
     *
     * @param userId  the contributing user's ID
     * @param guildId the guild's ID
     * @param bossId  the boss ID
     * @param damage  the damage to apply (as percentage points)
     * @return updated boss response reflecting current guild progress
     */
    @Transactional
    public BossResponse contributeToGuildBoss(UUID userId, UUID guildId, UUID bossId, int damage) {
        Boss boss = bossRepository.findById(bossId)
                .orElseThrow(() -> new IllegalArgumentException("Boss not found: " + bossId));

        if (!boss.getGuildBoss()) {
            throw new IllegalArgumentException("Boss " + bossId + " is not a guild boss");
        }

        // Verify user is a member of the guild
        if (!guildMemberRepository.existsByGuildIdAndUserId(guildId, userId)) {
            throw new IllegalArgumentException(
                    "User " + userId + " is not a member of guild " + guildId);
        }

        // 1. Fetch guild_boss_progress
        GuildBossProgress progress = guildBossProgressRepository.findByGuildIdAndBossId(guildId, bossId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No guild boss progress found for guild " + guildId + " and boss " + bossId));

        if (progress.getDefeated()) {
            log.debug("Guild boss {} already defeated by guild {}, ignoring damage", bossId, guildId);
            return mapToBossResponse(boss, progress);
        }

        // 2. Add damage (collective from all members)
        int newProgress = progress.getProgressPercent() + damage;
        progress.setProgressPercent(Math.min(newProgress, 100));

        // 3. Check stage thresholds and advance stage if threshold crossed
        List<Integer> thresholds = boss.getStageThresholds();
        if (thresholds != null && !thresholds.isEmpty()) {
            int newStage = calculateCurrentStage(progress.getProgressPercent(), thresholds);
            if (newStage > progress.getCurrentStage()) {
                log.info("Guild {} advanced boss {} from stage {} to stage {}",
                        guildId, bossId, progress.getCurrentStage(), newStage);
                progress.setCurrentStage(newStage);
            }
        }

        // 4. If defeated → award rewards to all guild members
        if (progress.getProgressPercent() >= 100
                && progress.getCurrentStage() >= boss.getTotalStages()) {
            progress.setProgressPercent(100);
            progress.setDefeated(true);
            progress.setDefeatedAt(LocalDateTime.now());
            guildBossProgressRepository.save(progress);

            awardRewardsToGuildMembers(guildId, boss);
        } else {
            guildBossProgressRepository.save(progress);
        }

        return mapToBossResponse(boss, progress);
    }

    /**
     * Gets the guild boss progress for a specific guild and boss.
     *
     * @param guildId the guild's ID
     * @param bossId  the boss ID
     * @return boss response with current guild progress
     */
    @Transactional(readOnly = true)
    public BossResponse getGuildBossProgress(UUID guildId, UUID bossId) {
        Boss boss = bossRepository.findById(bossId)
                .orElseThrow(() -> new IllegalArgumentException("Boss not found: " + bossId));

        GuildBossProgress progress = guildBossProgressRepository.findByGuildIdAndBossId(guildId, bossId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No guild boss progress found for guild " + guildId + " and boss " + bossId));

        return mapToBossResponse(boss, progress);
    }

    /**
     * Gets all active (non-defeated) guild bosses for a guild.
     *
     * @param guildId the guild's ID
     * @return list of active guild boss progress entries
     */
    @Transactional(readOnly = true)
    public List<BossResponse> getActiveGuildBosses(UUID guildId) {
        List<GuildBossProgress> activeProgress = guildBossProgressRepository
                .findByGuildIdAndDefeatedFalse(guildId);

        return activeProgress.stream()
                .map(progress -> {
                    Boss boss = bossRepository.findById(progress.getBossId()).orElse(null);
                    if (boss == null) {
                        return null;
                    }
                    return mapToBossResponse(boss, progress);
                })
                .filter(response -> response != null)
                .toList();
    }

    /**
     * Awards XP, title, and cosmetic rewards to all members of the guild
     * when a guild boss is defeated.
     */
    private void awardRewardsToGuildMembers(UUID guildId, Boss boss) {
        List<GuildMember> members = guildMemberRepository.findByGuildId(guildId);
        int xpAwarded = boss.getRewardXp();

        for (GuildMember member : members) {
            UUID memberId = member.getUserId();

            // Award XP to each guild member
            User user = userRepository.findById(memberId).orElse(null);
            if (user == null) {
                log.warn("Guild member user {} not found, skipping reward", memberId);
                continue;
            }

            user.setXp(user.getXp() + xpAwarded);
            userRepository.save(user);

            // Log XP history for each member
            XpHistory xpHistory = XpHistory.builder()
                    .userId(memberId)
                    .sourceType("GUILD_BOSS_DEFEAT")
                    .sourceId(boss.getId())
                    .xpAmount(xpAwarded)
                    .multiplier(BigDecimal.ONE)
                    .build();
            xpHistoryRepository.save(xpHistory);

            // Publish BossDefeatedEvent for each member
            BossDefeatedEvent event = new BossDefeatedEvent(
                    this,
                    memberId,
                    boss.getId(),
                    boss.getName(),
                    xpAwarded,
                    boss.getRewardTitle()
            );
            eventPublisher.publishEvent(event);

            log.info("Awarded guild boss defeat rewards to member {}: {} XP, title: {}",
                    memberId, xpAwarded, boss.getRewardTitle());
        }

        log.info("Guild {} defeated boss {} ({}). Awarded {} XP to {} members",
                guildId, boss.getId(), boss.getName(), xpAwarded, members.size());
    }

    /**
     * Determines the current stage based on overall progress and stage thresholds.
     * Thresholds represent cumulative progress percentages for each stage boundary.
     * e.g., [33, 66, 100] means stage 1 ends at 33%, stage 2 at 66%, stage 3 at 100%.
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

    private BossResponse mapToBossResponse(Boss boss, GuildBossProgress progress) {
        return BossResponse.builder()
                .id(boss.getId())
                .name(boss.getName())
                .description(boss.getDescription())
                .totalStages(boss.getTotalStages())
                .currentStage(progress.getCurrentStage())
                .progressPercent(progress.getProgressPercent())
                .defeated(progress.getDefeated())
                .rewardXp(boss.getRewardXp())
                .rewardTitle(boss.getRewardTitle())
                .build();
    }
}
