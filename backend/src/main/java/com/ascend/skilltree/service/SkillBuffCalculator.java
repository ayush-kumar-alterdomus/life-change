package com.ascend.skilltree.service;

import com.ascend.common.entity.StatType;
import com.ascend.skilltree.entity.SkillNode;
import com.ascend.skilltree.entity.UserSkill;
import com.ascend.skilltree.repository.SkillNodeRepository;
import com.ascend.skilltree.repository.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Calculates passive XP buffs granted by unlocked skill nodes.
 * Buffs are additive — multiple nodes for the same stat type stack.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SkillBuffCalculator {

    private final UserSkillRepository userSkillRepository;
    private final SkillNodeRepository skillNodeRepository;

    /**
     * Fetches all active buffs for a user by aggregating unlocked skill node buff percentages,
     * grouped by stat type.
     *
     * @param userId the user's ID
     * @return map of StatType to total buff percent (e.g., STRENGTH → 0.15 means +15%)
     */
    @Transactional(readOnly = true)
    public Map<StatType, Double> getActiveBuffs(UUID userId) {
        log.debug("Calculating active buffs for user={}", userId);

        // 1. Fetch all unlocked skills for user
        List<UserSkill> unlockedSkills = userSkillRepository.findByUserIdAndUnlockedTrue(userId);

        if (unlockedSkills.isEmpty()) {
            log.debug("No unlocked skills found for user={}", userId);
            return new EnumMap<>(StatType.class);
        }

        // 2. Fetch the corresponding skill nodes to get stat_type and buff_percent
        List<UUID> skillNodeIds = unlockedSkills.stream()
                .map(UserSkill::getSkillId)
                .toList();

        List<SkillNode> skillNodes = skillNodeRepository.findAllById(skillNodeIds);

        // 3. Group by stat_type and sum buff_percent per stat type
        Map<StatType, Double> buffs = new EnumMap<>(StatType.class);

        for (SkillNode node : skillNodes) {
            if (node.getStatType() == null || node.getBuffPercent() == null) {
                continue;
            }

            StatType statType = parseStatType(node.getStatType());
            if (statType == null) {
                log.warn("Unknown stat type '{}' on skill node={}", node.getStatType(), node.getId());
                continue;
            }

            double buffValue = node.getBuffPercent().doubleValue();
            buffs.merge(statType, buffValue, Double::sum);
        }

        log.debug("Active buffs for user={}: {}", userId, buffs);
        return buffs;
    }

    /**
     * Calculates the boosted XP after applying the skill buff for a given stat type.
     * Formula: floor(baseXp × (1 + totalBuff))
     *
     * @param baseXp   the base XP before buff application (must be >= 0)
     * @param statType the stat type of the XP being awarded
     * @param buffs    map of active buffs (StatType → totalBuffPercent)
     * @return the boosted XP value (floor of the calculation)
     */
    public int calculateBoostedXp(int baseXp, StatType statType, Map<StatType, Double> buffs) {
        if (baseXp < 0) {
            throw new IllegalArgumentException("baseXp must be non-negative, got: " + baseXp);
        }

        double totalBuff = buffs.getOrDefault(statType, 0.0);
        double boosted = baseXp * (1.0 + totalBuff);
        return (int) Math.floor(boosted);
    }

    /**
     * Safely parses a stat type string to the StatType enum.
     * Returns null if the string doesn't match any known stat type.
     */
    private StatType parseStatType(String statType) {
        if (statType == null) {
            return null;
        }
        try {
            return StatType.valueOf(statType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
