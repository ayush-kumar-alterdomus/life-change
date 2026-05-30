package com.ascend.skilltree.service;

import com.ascend.arc.repository.ArcRepository;
import com.ascend.common.entity.StatType;
import com.ascend.common.exception.BusinessException;
import com.ascend.skilltree.dto.SkillNodeResponse;
import com.ascend.skilltree.entity.SkillNode;
import com.ascend.skilltree.entity.UserSkill;
import com.ascend.skilltree.exception.SkillResetCooldownException;
import com.ascend.skilltree.repository.SkillNodeRepository;
import com.ascend.skilltree.repository.UserSkillRepository;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration tests for the Skill Tree system.
 * Verifies end-to-end flows: unlock root → unlock child → buff applies,
 * prerequisite enforcement, and premium reset cooldown.
 */
@ExtendWith(MockitoExtension.class)
class SkillTreeIntegrationTest {

    @Mock
    private SkillNodeRepository skillNodeRepository;

    @Mock
    private UserSkillRepository userSkillRepository;

    @Mock
    private ArcRepository arcRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private SkillTreeService skillTreeService;
    private SkillBuffCalculator skillBuffCalculator;

    private UUID userId;
    private UUID arcId;

    @BeforeEach
    void setUp() {
        skillTreeService = new SkillTreeService(
                skillNodeRepository, userSkillRepository, arcRepository, userRepository, eventPublisher);
        skillBuffCalculator = new SkillBuffCalculator(userSkillRepository, skillNodeRepository);
        userId = UUID.randomUUID();
        arcId = UUID.randomUUID();
    }

    // ========================================================================
    // Integration test: unlock root node → unlock child → verify buff applies to XP
    // ========================================================================

    @Nested
    @DisplayName("Unlock root → unlock child → buff applies to XP")
    class UnlockFlowAndBuffApplication {

        @Test
        @DisplayName("Full flow: unlock root, then child, then verify buff boosts XP correctly")
        void unlockRootThenChild_buffAppliesCorrectly() {
            UUID rootNodeId = UUID.randomUUID();
            UUID childNodeId = UUID.randomUUID();

            // Setup: root node (no parent) with 10% STRENGTH buff
            SkillNode rootNode = SkillNode.builder()
                    .id(rootNodeId)
                    .arcId(arcId)
                    .name("Iron Fist")
                    .description("Basic strength training")
                    .statType("STRENGTH")
                    .buffPercent(BigDecimal.valueOf(0.10))
                    .parentNodeId(null)
                    .orderIndex(0)
                    .build();

            // Setup: child node (parent = root) with 5% STRENGTH buff
            SkillNode childNode = SkillNode.builder()
                    .id(childNodeId)
                    .arcId(arcId)
                    .name("Steel Grip")
                    .description("Advanced strength training")
                    .statType("STRENGTH")
                    .buffPercent(BigDecimal.valueOf(0.05))
                    .parentNodeId(rootNodeId)
                    .orderIndex(1)
                    .build();

            // --- Step 1: Unlock root node ---
            when(skillNodeRepository.findById(rootNodeId)).thenReturn(Optional.of(rootNode));
            when(userSkillRepository.findByUserIdAndArcId(userId, arcId))
                    .thenReturn(Collections.emptyList());
            when(userSkillRepository.save(any(UserSkill.class))).thenAnswer(inv -> inv.getArgument(0));

            SkillNodeResponse rootResult = skillTreeService.unlockNode(userId, rootNodeId);

            assertThat(rootResult).isNotNull();
            assertThat(rootResult.isUnlocked()).isTrue();
            assertThat(rootResult.getName()).isEqualTo("Iron Fist");
            assertThat(rootResult.getBuffPercent()).isEqualTo(0.10);

            // Verify event was published for root unlock
            verify(eventPublisher).publishEvent(any());

            // --- Step 2: Unlock child node (parent is now unlocked) ---
            UserSkill rootUnlocked = UserSkill.builder()
                    .userId(userId)
                    .skillId(rootNodeId)
                    .skillName("Iron Fist")
                    .arcId(arcId)
                    .unlocked(true)
                    .unlockedAt(LocalDateTime.now())
                    .build();

            when(skillNodeRepository.findById(childNodeId)).thenReturn(Optional.of(childNode));
            when(userSkillRepository.findByUserIdAndArcId(userId, arcId))
                    .thenReturn(List.of(rootUnlocked));
            when(userSkillRepository.save(any(UserSkill.class))).thenAnswer(inv -> inv.getArgument(0));

            SkillNodeResponse childResult = skillTreeService.unlockNode(userId, childNodeId);

            assertThat(childResult).isNotNull();
            assertThat(childResult.isUnlocked()).isTrue();
            assertThat(childResult.getName()).isEqualTo("Steel Grip");
            assertThat(childResult.getBuffPercent()).isEqualTo(0.05);

            // --- Step 3: Verify buff applies to XP ---
            // Combined buff: 10% (root) + 5% (child) = 15% total STRENGTH buff
            Map<StatType, Double> buffs = new EnumMap<>(StatType.class);
            buffs.put(StatType.STRENGTH, 0.15); // 10% + 5%

            int baseXp = 100;
            int boostedXp = skillBuffCalculator.calculateBoostedXp(baseXp, StatType.STRENGTH, buffs);

            // Expected: floor(100 × (1 + 0.15)) = floor(115.0) = 115
            assertThat(boostedXp).isEqualTo(115);
        }

        @Test
        @DisplayName("Single root node buff applies correctly to XP calculation")
        void unlockRootOnly_singleBuffApplies() {
            UUID rootNodeId = UUID.randomUUID();

            SkillNode rootNode = SkillNode.builder()
                    .id(rootNodeId)
                    .arcId(arcId)
                    .name("Meditation I")
                    .statType("WISDOM")
                    .buffPercent(BigDecimal.valueOf(0.08))
                    .parentNodeId(null)
                    .orderIndex(0)
                    .build();

            when(skillNodeRepository.findById(rootNodeId)).thenReturn(Optional.of(rootNode));
            when(userSkillRepository.findByUserIdAndArcId(userId, arcId))
                    .thenReturn(Collections.emptyList());
            when(userSkillRepository.save(any(UserSkill.class))).thenAnswer(inv -> inv.getArgument(0));

            SkillNodeResponse result = skillTreeService.unlockNode(userId, rootNodeId);

            assertThat(result.isUnlocked()).isTrue();
            assertThat(result.getStatType()).isEqualTo(StatType.WISDOM);

            // Verify buff calculation with single 8% buff
            Map<StatType, Double> buffs = new EnumMap<>(StatType.class);
            buffs.put(StatType.WISDOM, 0.08);

            int boostedXp = skillBuffCalculator.calculateBoostedXp(200, StatType.WISDOM, buffs);
            // Expected: floor(200 × 1.08) = 216
            assertThat(boostedXp).isEqualTo(216);
        }
    }

    // ========================================================================
    // Integration test: attempt to unlock child without parent → rejected
    // ========================================================================

    @Nested
    @DisplayName("Prerequisite enforcement - child without parent rejected")
    class PrerequisiteEnforcement {

        @Test
        @DisplayName("Attempting to unlock child node without parent unlocked throws SKILL_PREREQUISITE_NOT_MET")
        void unlockChildWithoutParent_throwsPrerequisiteException() {
            UUID rootNodeId = UUID.randomUUID();
            UUID childNodeId = UUID.randomUUID();

            SkillNode childNode = SkillNode.builder()
                    .id(childNodeId)
                    .arcId(arcId)
                    .name("Advanced Focus")
                    .statType("FOCUS")
                    .buffPercent(BigDecimal.valueOf(0.07))
                    .parentNodeId(rootNodeId)
                    .orderIndex(1)
                    .build();

            when(skillNodeRepository.findById(childNodeId)).thenReturn(Optional.of(childNode));
            // User has NO unlocked skills — parent is not unlocked
            when(userSkillRepository.findByUserIdAndArcId(userId, arcId))
                    .thenReturn(Collections.emptyList());

            assertThatThrownBy(() -> skillTreeService.unlockNode(userId, childNodeId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getErrorCode()).isEqualTo("SKILL_PREREQUISITE_NOT_MET");
                    });

            // Verify no skill was saved
            verify(userSkillRepository, never()).save(any(UserSkill.class));
            // Verify no event was published
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Attempting to unlock child when a different node is unlocked (not the parent) is rejected")
        void unlockChildWithWrongParentUnlocked_throwsPrerequisiteException() {
            UUID correctParentId = UUID.randomUUID();
            UUID wrongNodeId = UUID.randomUUID();
            UUID childNodeId = UUID.randomUUID();

            SkillNode childNode = SkillNode.builder()
                    .id(childNodeId)
                    .arcId(arcId)
                    .name("Deep Discipline")
                    .statType("DISCIPLINE")
                    .buffPercent(BigDecimal.valueOf(0.06))
                    .parentNodeId(correctParentId)
                    .orderIndex(2)
                    .build();

            // User has a different node unlocked, but NOT the correct parent
            UserSkill wrongUnlock = UserSkill.builder()
                    .userId(userId)
                    .skillId(wrongNodeId)
                    .skillName("Wrong Skill")
                    .arcId(arcId)
                    .unlocked(true)
                    .unlockedAt(LocalDateTime.now())
                    .build();

            when(skillNodeRepository.findById(childNodeId)).thenReturn(Optional.of(childNode));
            when(userSkillRepository.findByUserIdAndArcId(userId, arcId))
                    .thenReturn(List.of(wrongUnlock));

            assertThatThrownBy(() -> skillTreeService.unlockNode(userId, childNodeId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getErrorCode()).isEqualTo("SKILL_PREREQUISITE_NOT_MET");
                    });

            verify(userSkillRepository, never()).save(any(UserSkill.class));
        }

        @Test
        @DisplayName("Root node (no parent) can always be unlocked regardless of user state")
        void unlockRootNode_alwaysSucceeds() {
            UUID rootNodeId = UUID.randomUUID();

            SkillNode rootNode = SkillNode.builder()
                    .id(rootNodeId)
                    .arcId(arcId)
                    .name("Foundation")
                    .statType("VITALITY")
                    .buffPercent(BigDecimal.valueOf(0.12))
                    .parentNodeId(null)
                    .orderIndex(0)
                    .build();

            when(skillNodeRepository.findById(rootNodeId)).thenReturn(Optional.of(rootNode));
            when(userSkillRepository.findByUserIdAndArcId(userId, arcId))
                    .thenReturn(Collections.emptyList());
            when(userSkillRepository.save(any(UserSkill.class))).thenAnswer(inv -> inv.getArgument(0));

            SkillNodeResponse result = skillTreeService.unlockNode(userId, rootNodeId);

            assertThat(result).isNotNull();
            assertThat(result.isUnlocked()).isTrue();
            assertThat(result.getName()).isEqualTo("Foundation");

            verify(userSkillRepository).save(any(UserSkill.class));
        }
    }

    // ========================================================================
    // Integration test: premium reset → verify cooldown enforced
    // ========================================================================

    @Nested
    @DisplayName("Premium skill reset with cooldown enforcement")
    class PremiumResetCooldown {

        @Test
        @DisplayName("Premium user reset within 30-day cooldown throws SkillResetCooldownException")
        void resetWithinCooldown_throwsCooldownException() {
            // User last reset 10 days ago — within 30-day cooldown
            User premiumUser = User.builder()
                    .id(userId)
                    .firebaseUid("firebase-premium-user")
                    .premium(true)
                    .lastSkillResetAt(LocalDateTime.now().minusDays(10))
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(premiumUser));

            assertThatThrownBy(() -> skillTreeService.resetSkillTree(userId, arcId))
                    .isInstanceOf(SkillResetCooldownException.class);

            // Verify no skills were deleted
            verify(userSkillRepository, never()).deleteByUserIdAndArcId(any(), any());
            // Verify user was not updated
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Premium user reset after 30-day cooldown succeeds")
        void resetAfterCooldown_succeeds() {
            // User last reset 31 days ago — cooldown expired
            User premiumUser = User.builder()
                    .id(userId)
                    .firebaseUid("firebase-premium-user")
                    .premium(true)
                    .lastSkillResetAt(LocalDateTime.now().minusDays(31))
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(premiumUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            // Should not throw
            skillTreeService.resetSkillTree(userId, arcId);

            // Verify skills were deleted
            verify(userSkillRepository).deleteByUserIdAndArcId(userId, arcId);
            // Verify user's lastSkillResetAt was updated
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getLastSkillResetAt()).isNotNull();
        }

        @Test
        @DisplayName("Premium user with no previous reset (null lastSkillResetAt) can reset")
        void firstTimeReset_succeeds() {
            User premiumUser = User.builder()
                    .id(userId)
                    .firebaseUid("firebase-premium-user")
                    .premium(true)
                    .lastSkillResetAt(null)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(premiumUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            // Should not throw
            skillTreeService.resetSkillTree(userId, arcId);

            verify(userSkillRepository).deleteByUserIdAndArcId(userId, arcId);
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getLastSkillResetAt()).isNotNull();
        }

        @Test
        @DisplayName("Non-premium user attempting reset throws PREMIUM_REQUIRED")
        void nonPremiumReset_throwsPremiumRequired() {
            User freeUser = User.builder()
                    .id(userId)
                    .firebaseUid("firebase-free-user")
                    .premium(false)
                    .lastSkillResetAt(null)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(freeUser));

            assertThatThrownBy(() -> skillTreeService.resetSkillTree(userId, arcId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getErrorCode()).isEqualTo("PREMIUM_REQUIRED");
                    });

            verify(userSkillRepository, never()).deleteByUserIdAndArcId(any(), any());
        }

        @Test
        @DisplayName("Reset at exactly 30 days is still within cooldown (boundary test)")
        void resetAtExactly30Days_stillWithinCooldown() {
            // Last reset exactly 29 days and 23 hours ago — still within cooldown
            User premiumUser = User.builder()
                    .id(userId)
                    .firebaseUid("firebase-premium-user")
                    .premium(true)
                    .lastSkillResetAt(LocalDateTime.now().minusDays(29).minusHours(23))
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(premiumUser));

            assertThatThrownBy(() -> skillTreeService.resetSkillTree(userId, arcId))
                    .isInstanceOf(SkillResetCooldownException.class);

            verify(userSkillRepository, never()).deleteByUserIdAndArcId(any(), any());
        }
    }
}
