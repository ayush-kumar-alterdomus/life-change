package com.ascend.skilltree.service;

import com.ascend.arc.entity.Arc;
import com.ascend.arc.repository.ArcRepository;
import com.ascend.common.entity.StatType;
import com.ascend.common.exception.BusinessException;
import com.ascend.skilltree.dto.SkillNodeResponse;
import com.ascend.skilltree.dto.SkillTreeResponse;
import com.ascend.skilltree.entity.SkillNode;
import com.ascend.skilltree.entity.UserSkill;
import com.ascend.skilltree.event.SkillUnlockedEvent;
import com.ascend.skilltree.exception.SkillResetCooldownException;
import com.ascend.skilltree.repository.SkillNodeRepository;
import com.ascend.skilltree.repository.UserSkillRepository;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for skill tree operations including fetching the tree structure
 * and unlocking skill nodes with prerequisite validation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SkillTreeService {

    private final SkillNodeRepository skillNodeRepository;
    private final UserSkillRepository userSkillRepository;
    private final ArcRepository arcRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Fetches the skill tree for a given arc, annotated with the user's unlock status.
     *
     * @param userId the user's ID
     * @param arcId  the arc ID to fetch the skill tree for
     * @return the skill tree response with unlock status per node
     */
    @Transactional(readOnly = true)
    public SkillTreeResponse getSkillTree(UUID userId, UUID arcId) {
        log.debug("Fetching skill tree for user={}, arc={}", userId, arcId);

        // Fetch the arc to get its name
        Arc arc = arcRepository.findById(arcId)
                .orElseThrow(() -> new BusinessException("ARC_NOT_FOUND",
                        "Arc not found with id: " + arcId));

        // Fetch all skill nodes for this arc
        List<SkillNode> nodes = skillNodeRepository.findByArcIdOrderByOrderIndex(arcId);

        // Fetch user's unlocked skills for this arc
        List<UserSkill> userSkills = userSkillRepository.findByUserIdAndArcId(userId, arcId);
        Map<UUID, UserSkill> unlockedMap = userSkills.stream()
                .filter(us -> Boolean.TRUE.equals(us.getUnlocked()))
                .collect(Collectors.toMap(UserSkill::getSkillId, us -> us));

        // Build tree structure: group nodes by parent
        Map<UUID, List<SkillNode>> childrenByParent = nodes.stream()
                .filter(n -> n.getParentNodeId() != null)
                .collect(Collectors.groupingBy(SkillNode::getParentNodeId));

        // Find root nodes (no parent)
        List<SkillNode> rootNodes = nodes.stream()
                .filter(n -> n.getParentNodeId() == null)
                .collect(Collectors.toList());

        // Build response tree recursively
        List<SkillNodeResponse> treeNodes = rootNodes.stream()
                .map(node -> buildNodeResponse(node, childrenByParent, unlockedMap))
                .collect(Collectors.toList());

        return SkillTreeResponse.builder()
                .arcId(arcId)
                .arcName(arc.getName())
                .nodes(treeNodes)
                .build();
    }

    /**
     * Unlocks a skill node for a user after validating prerequisites.
     *
     * @param userId      the user's ID
     * @param skillNodeId the skill node to unlock
     * @return the updated skill node response with buff info
     */
    @Transactional
    public SkillNodeResponse unlockNode(UUID userId, UUID skillNodeId) {
        log.info("Unlocking skill node={} for user={}", skillNodeId, userId);

        // Fetch the skill node
        SkillNode node = skillNodeRepository.findById(skillNodeId)
                .orElseThrow(() -> new BusinessException("SKILL_NODE_NOT_FOUND",
                        "Skill node not found with id: " + skillNodeId));

        // Verify prerequisite: parent node must be unlocked (or null for root)
        if (node.getParentNodeId() != null) {
            List<UserSkill> userSkills = userSkillRepository.findByUserIdAndArcId(userId, node.getArcId());
            Set<UUID> unlockedNodeIds = userSkills.stream()
                    .filter(us -> Boolean.TRUE.equals(us.getUnlocked()))
                    .map(UserSkill::getSkillId)
                    .collect(Collectors.toSet());

            if (!unlockedNodeIds.contains(node.getParentNodeId())) {
                throw new BusinessException("SKILL_PREREQUISITE_NOT_MET",
                        "Parent skill node must be unlocked before unlocking this node");
            }
        }

        // Verify user hasn't already unlocked this node
        List<UserSkill> existingSkills = userSkillRepository.findByUserIdAndArcId(userId, node.getArcId());
        boolean alreadyUnlocked = existingSkills.stream()
                .anyMatch(us -> us.getSkillId().equals(skillNodeId) && Boolean.TRUE.equals(us.getUnlocked()));

        if (alreadyUnlocked) {
            throw new BusinessException("SKILL_ALREADY_UNLOCKED",
                    "User has already unlocked this skill node");
        }

        // Create user_skills record
        LocalDateTime now = LocalDateTime.now();
        UserSkill userSkill = UserSkill.builder()
                .userId(userId)
                .skillId(skillNodeId)
                .skillName(node.getName())
                .arcId(node.getArcId())
                .unlocked(true)
                .unlockedAt(now)
                .build();

        userSkillRepository.save(userSkill);

        log.info("Skill node unlocked: nodeId={}, userId={}, buff={}% {}",
                skillNodeId, userId, node.getBuffPercent(), node.getStatType());

        // Publish SkillUnlockedEvent
        SkillUnlockedEvent event = SkillUnlockedEvent.builder()
                .userId(userId)
                .skillNodeId(skillNodeId)
                .arcId(node.getArcId())
                .skillName(node.getName())
                .statType(node.getStatType())
                .buffPercent(node.getBuffPercent().doubleValue())
                .build();

        eventPublisher.publishEvent(event);

        // Return updated node with buff info
        return SkillNodeResponse.builder()
                .id(node.getId())
                .name(node.getName())
                .description(node.getDescription())
                .statType(parseStatType(node.getStatType()))
                .buffPercent(node.getBuffPercent().doubleValue())
                .parentNodeId(node.getParentNodeId())
                .unlocked(true)
                .unlockedAt(now)
                .children(new ArrayList<>())
                .build();
    }

    /**
     * Resets the skill tree for a premium user on a specific arc.
     * Enforces a 30-day cooldown between resets.
     *
     * @param userId the user's ID
     * @param arcId  the arc ID to reset skills for
     * @throws BusinessException if user is not premium
     * @throws SkillResetCooldownException if within 30-day cooldown
     */
    @Transactional
    public void resetSkillTree(UUID userId, UUID arcId) {
        log.info("Resetting skill tree for user={}, arc={}", userId, arcId);

        // 1. Verify user is premium
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND",
                        "User not found with id: " + userId));

        if (!Boolean.TRUE.equals(user.getPremium())) {
            throw new BusinessException("PREMIUM_REQUIRED",
                    "Skill tree reset is only available for premium users");
        }

        // 2. Check cooldown: last_skill_reset_at must be > 30 days ago (or null)
        LocalDateTime lastReset = user.getLastSkillResetAt();
        if (lastReset != null) {
            LocalDateTime cooldownExpiry = lastReset.plusDays(30);
            if (LocalDateTime.now().isBefore(cooldownExpiry)) {
                throw new SkillResetCooldownException(
                        "Skill tree reset is on cooldown. Next reset available after: " + cooldownExpiry);
            }
        }

        // 3. Delete all user_skills for this arc
        userSkillRepository.deleteByUserIdAndArcId(userId, arcId);

        // 4. Update user.last_skill_reset_at = now()
        user.setLastSkillResetAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Skill tree reset completed for user={}, arc={}", userId, arcId);
    }

    /**
     * Recursively builds a SkillNodeResponse tree from a node and its children.
     */
    private SkillNodeResponse buildNodeResponse(SkillNode node,
                                                 Map<UUID, List<SkillNode>> childrenByParent,
                                                 Map<UUID, UserSkill> unlockedMap) {
        UserSkill userSkill = unlockedMap.get(node.getId());
        boolean unlocked = userSkill != null;
        LocalDateTime unlockedAt = userSkill != null ? userSkill.getUnlockedAt() : null;

        List<SkillNodeResponse> children = new ArrayList<>();
        List<SkillNode> childNodes = childrenByParent.get(node.getId());
        if (childNodes != null) {
            children = childNodes.stream()
                    .map(child -> buildNodeResponse(child, childrenByParent, unlockedMap))
                    .collect(Collectors.toList());
        }

        return SkillNodeResponse.builder()
                .id(node.getId())
                .name(node.getName())
                .description(node.getDescription())
                .statType(parseStatType(node.getStatType()))
                .buffPercent(node.getBuffPercent().doubleValue())
                .parentNodeId(node.getParentNodeId())
                .unlocked(unlocked)
                .unlockedAt(unlockedAt)
                .children(children)
                .build();
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
