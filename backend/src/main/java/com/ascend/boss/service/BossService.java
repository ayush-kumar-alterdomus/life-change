package com.ascend.boss.service;

import com.ascend.boss.dto.BossDefeatResponse;
import com.ascend.boss.dto.BossDetailResponse;
import com.ascend.boss.dto.BossResponse;
import com.ascend.boss.entity.Boss;
import com.ascend.boss.entity.BossProgress;
import com.ascend.boss.event.BossDefeatedEvent;
import com.ascend.boss.repository.BossProgressRepository;
import com.ascend.boss.repository.BossRepository;
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
 * Core service for boss battle operations including progress tracking,
 * damage contribution, stage advancement, and defeat handling.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BossService {

    private final BossRepository bossRepository;
    private final BossProgressRepository bossProgressRepository;
    private final UserRepository userRepository;
    private final XpHistoryRepository xpHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Lists all bosses for a user, including active (in-progress) and defeated ones.
     *
     * @param userId the user's ID
     * @return list of boss responses with progress info
     */
    @Transactional(readOnly = true)
    public List<BossResponse> getUserBosses(UUID userId) {
        List<BossProgress> progressList = bossProgressRepository.findByUserId(userId);

        return progressList.stream()
                .map(progress -> {
                    Boss boss = bossRepository.findById(progress.getBossId())
                            .orElse(null);
                    if (boss == null) {
                        return null;
                    }
                    return mapToBossResponse(boss, progress);
                })
                .filter(response -> response != null)
                .toList();
    }

    /**
     * Gets detailed boss information with progress for a specific user and boss.
     *
     * @param userId the user's ID
     * @param bossId the boss ID
     * @return detailed boss response with stage thresholds
     */
    @Transactional(readOnly = true)
    public BossDetailResponse getBossDetail(UUID userId, UUID bossId) {
        Boss boss = bossRepository.findById(bossId)
                .orElseThrow(() -> new IllegalArgumentException("Boss not found: " + bossId));

        BossProgress progress = bossProgressRepository.findByUserIdAndBossId(userId, bossId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No boss progress found for user " + userId + " and boss " + bossId));

        return BossDetailResponse.builder()
                .id(boss.getId())
                .name(boss.getName())
                .description(boss.getDescription())
                .totalStages(boss.getTotalStages())
                .currentStage(progress.getCurrentStage())
                .progressPercent(progress.getProgressPercent())
                .defeated(progress.getDefeated())
                .rewardXp(boss.getRewardXp())
                .rewardTitle(boss.getRewardTitle())
                .rewardCosmetic(boss.getRewardCosmetic())
                .stageThresholds(boss.getStageThresholds())
                .build();
    }

    /**
     * Contributes damage to a boss for a user. Advances stage if threshold is crossed,
     * and defeats the boss if the final stage reaches 100%.
     *
     * @param userId the user's ID
     * @param bossId the boss ID
     * @param damage the damage to apply (as percentage points)
     * @return updated boss response, or defeat response if boss is defeated
     */
    @Transactional
    public BossResponse contributeToBoss(UUID userId, UUID bossId, int damage) {
        Boss boss = bossRepository.findById(bossId)
                .orElseThrow(() -> new IllegalArgumentException("Boss not found: " + bossId));

        BossProgress progress = bossProgressRepository.findByUserIdAndBossId(userId, bossId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No boss progress found for user " + userId + " and boss " + bossId));

        if (progress.getDefeated()) {
            log.debug("Boss {} already defeated by user {}, ignoring damage", bossId, userId);
            return mapToBossResponse(boss, progress);
        }

        // Add damage to progress
        int newProgress = progress.getProgressPercent() + damage;
        progress.setProgressPercent(Math.min(newProgress, 100));

        // Check stage thresholds and advance stage if threshold crossed
        List<Integer> thresholds = boss.getStageThresholds();
        if (thresholds != null && !thresholds.isEmpty()) {
            int newStage = calculateCurrentStage(progress.getProgressPercent(), thresholds);
            if (newStage > progress.getCurrentStage()) {
                log.info("User {} advanced boss {} from stage {} to stage {}",
                        userId, bossId, progress.getCurrentStage(), newStage);
                progress.setCurrentStage(newStage);
            }
        }

        // Check if final stage at 100% → defeat boss
        if (progress.getProgressPercent() >= 100
                && progress.getCurrentStage() >= boss.getTotalStages()) {
            progress.setProgressPercent(100);
            defeatBoss(userId, bossId);
            // Re-fetch after defeat to get updated state
            progress = bossProgressRepository.findByUserIdAndBossId(userId, bossId)
                    .orElse(progress);
        } else {
            bossProgressRepository.save(progress);
        }

        return mapToBossResponse(boss, progress);
    }

    /**
     * Defeats a boss for a user: marks as defeated, awards legendary XP,
     * unlocks exclusive title and cosmetic, and publishes BossDefeatedEvent.
     *
     * @param userId the user's ID
     * @param bossId the boss ID
     * @return defeat response with reward details
     */
    @Transactional
    public BossDefeatResponse defeatBoss(UUID userId, UUID bossId) {
        Boss boss = bossRepository.findById(bossId)
                .orElseThrow(() -> new IllegalArgumentException("Boss not found: " + bossId));

        BossProgress progress = bossProgressRepository.findByUserIdAndBossId(userId, bossId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No boss progress found for user " + userId + " and boss " + bossId));

        // 1. Mark defeated
        progress.setDefeated(true);
        progress.setDefeatedAt(LocalDateTime.now());
        progress.setProgressPercent(100);
        bossProgressRepository.save(progress);

        // 2. Award legendary XP (300-1000 based on boss reward_xp)
        int xpAwarded = boss.getRewardXp();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setXp(user.getXp() + xpAwarded);
        userRepository.save(user);

        // Log XP history
        XpHistory xpHistory = XpHistory.builder()
                .userId(userId)
                .sourceType("BOSS_DEFEAT")
                .sourceId(bossId)
                .xpAmount(xpAwarded)
                .multiplier(BigDecimal.ONE)
                .build();
        xpHistoryRepository.save(xpHistory);

        log.info("User {} defeated boss {} ({}). Awarded {} XP, title: {}, cosmetic: {}",
                userId, bossId, boss.getName(), xpAwarded, boss.getRewardTitle(), boss.getRewardCosmetic());

        // 3. Unlock exclusive title and cosmetic (stored in defeat response)
        // Title and cosmetic are defined on the boss entity itself

        // 4. Publish BossDefeatedEvent
        BossDefeatedEvent event = new BossDefeatedEvent(
                this,
                userId,
                bossId,
                boss.getName(),
                xpAwarded,
                boss.getRewardTitle()
        );
        eventPublisher.publishEvent(event);

        return BossDefeatResponse.builder()
                .bossName(boss.getName())
                .xpAwarded(xpAwarded)
                .titleUnlocked(boss.getRewardTitle())
                .cosmeticUnlocked(boss.getRewardCosmetic())
                .build();
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

    private BossResponse mapToBossResponse(Boss boss, BossProgress progress) {
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
