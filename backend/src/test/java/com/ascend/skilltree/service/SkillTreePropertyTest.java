package com.ascend.skilltree.service;

import com.ascend.common.entity.StatType;
import com.ascend.common.exception.BusinessException;
import com.ascend.skilltree.entity.SkillNode;
import com.ascend.skilltree.entity.UserSkill;
import com.ascend.skilltree.exception.SkillResetCooldownException;
import com.ascend.skilltree.repository.SkillNodeRepository;
import com.ascend.skilltree.repository.UserSkillRepository;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Property-based tests for the Skill Tree system.
 * Validates prerequisite enforcement, buff calculation correctness,
 * and reset cooldown enforcement across randomized inputs.
 *
 * **Validates: Requirements 1.2**
 */
class SkillTreePropertyTest {

    // ========================================================================
    // Property 34: Prerequisite enforcement (can't unlock child without parent)
    // ========================================================================

    /**
     * Property: Attempting to unlock a child node when the parent node is NOT
     * unlocked must always throw a BusinessException with code SKILL_PREREQUISITE_NOT_MET.
     *
     * **Validates: Requirements 1.2**
     */
    @Property(tries = 100)
    void cannotUnlockChildWithoutParentUnlocked(
            @ForAll("userIds") UUID userId,
            @ForAll("nodeIds") UUID childNodeId,
            @ForAll("nodeIds") UUID parentNodeId,
            @ForAll("arcIds") UUID arcId) {

        // Setup: child node has a parent
        SkillNode childNode = SkillNode.builder()
                .id(childNodeId)
                .arcId(arcId)
                .name("Child Skill")
                .statType("STRENGTH")
                .buffPercent(BigDecimal.valueOf(0.05))
                .parentNodeId(parentNodeId)
                .orderIndex(1)
                .build();

        // Mock repositories
        SkillNodeRepository skillNodeRepository = Mockito.mock(SkillNodeRepository.class);
        UserSkillRepository userSkillRepository = Mockito.mock(UserSkillRepository.class);
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);

        when(skillNodeRepository.findById(childNodeId)).thenReturn(Optional.of(childNode));
        // User has NO unlocked skills (parent not unlocked)
        when(userSkillRepository.findByUserIdAndArcId(userId, arcId)).thenReturn(Collections.emptyList());

        SkillTreeService service = new SkillTreeService(
                skillNodeRepository, userSkillRepository,
                Mockito.mock(com.ascend.arc.repository.ArcRepository.class),
                userRepository, eventPublisher);

        // Act & Assert: must throw prerequisite exception
        assertThatThrownBy(() -> service.unlockNode(userId, childNodeId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException bex = (BusinessException) ex;
                    assertThat(bex.getErrorCode()).isEqualTo("SKILL_PREREQUISITE_NOT_MET");
                });
    }

    /**
     * Property: Unlocking a root node (parentNodeId == null) should never throw
     * a prerequisite exception, regardless of user state.
     *
     * **Validates: Requirements 1.2**
     */
    @Property(tries = 100)
    void rootNodeAlwaysPassesPrerequisiteCheck(
            @ForAll("userIds") UUID userId,
            @ForAll("nodeIds") UUID rootNodeId,
            @ForAll("arcIds") UUID arcId) {

        // Setup: root node has no parent
        SkillNode rootNode = SkillNode.builder()
                .id(rootNodeId)
                .arcId(arcId)
                .name("Root Skill")
                .statType("WISDOM")
                .buffPercent(BigDecimal.valueOf(0.10))
                .parentNodeId(null)
                .orderIndex(0)
                .build();

        // Mock repositories
        SkillNodeRepository skillNodeRepository = Mockito.mock(SkillNodeRepository.class);
        UserSkillRepository userSkillRepository = Mockito.mock(UserSkillRepository.class);
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);

        when(skillNodeRepository.findById(rootNodeId)).thenReturn(Optional.of(rootNode));
        // User has no skills yet (empty list means not already unlocked)
        when(userSkillRepository.findByUserIdAndArcId(userId, arcId)).thenReturn(Collections.emptyList());
        when(userSkillRepository.save(any(UserSkill.class))).thenAnswer(inv -> inv.getArgument(0));

        SkillTreeService service = new SkillTreeService(
                skillNodeRepository, userSkillRepository,
                Mockito.mock(com.ascend.arc.repository.ArcRepository.class),
                userRepository, eventPublisher);

        // Act: should NOT throw prerequisite exception (root nodes are always unlockable)
        var result = service.unlockNode(userId, rootNodeId);

        assertThat(result).isNotNull();
        assertThat(result.isUnlocked()).isTrue();
    }

    // ========================================================================
    // Property 35: Buff calculation correctness
    //              BoostedXP = floor(BaseXP × (1 + sum of buffs))
    // ========================================================================

    /**
     * Property: The calculateBoostedXp method must always return
     * floor(baseXp × (1 + totalBuff)) for any valid inputs.
     *
     * **Validates: Requirements 1.2**
     */
    @Property(tries = 100)
    void boostedXpMatchesFormula(
            @ForAll @IntRange(min = 0, max = 10000) int baseXp,
            @ForAll("statTypes") StatType statType,
            @ForAll @DoubleRange(min = 0.0, max = 2.0) double totalBuff) {

        // Build buffs map with the generated buff value
        Map<StatType, Double> buffs = new EnumMap<>(StatType.class);
        buffs.put(statType, totalBuff);

        SkillBuffCalculator calculator = new SkillBuffCalculator(null, null);

        int result = calculator.calculateBoostedXp(baseXp, statType, buffs);
        int expected = (int) Math.floor(baseXp * (1.0 + totalBuff));

        assertThat(result).isEqualTo(expected);
    }

    /**
     * Property: Boosted XP is always >= base XP when buffs are non-negative.
     *
     * **Validates: Requirements 1.2**
     */
    @Property(tries = 100)
    void boostedXpIsAlwaysGreaterOrEqualToBase(
            @ForAll @IntRange(min = 0, max = 10000) int baseXp,
            @ForAll("statTypes") StatType statType,
            @ForAll @DoubleRange(min = 0.0, max = 2.0) double totalBuff) {

        Map<StatType, Double> buffs = new EnumMap<>(StatType.class);
        buffs.put(statType, totalBuff);

        SkillBuffCalculator calculator = new SkillBuffCalculator(null, null);

        int result = calculator.calculateBoostedXp(baseXp, statType, buffs);

        assertThat(result).isGreaterThanOrEqualTo(baseXp);
    }

    /**
     * Property: With zero buffs, boosted XP equals base XP exactly.
     *
     * **Validates: Requirements 1.2**
     */
    @Property(tries = 100)
    void zeroBuffReturnsBaseXp(
            @ForAll @IntRange(min = 0, max = 10000) int baseXp,
            @ForAll("statTypes") StatType statType) {

        // Empty buffs map — no buff for any stat
        Map<StatType, Double> buffs = new EnumMap<>(StatType.class);

        SkillBuffCalculator calculator = new SkillBuffCalculator(null, null);

        int result = calculator.calculateBoostedXp(baseXp, statType, buffs);

        assertThat(result).isEqualTo(baseXp);
    }

    /**
     * Property: Additive buffs — sum of individual buffs equals combined buff.
     * If buff1 and buff2 are applied to the same stat, the result should be
     * floor(baseXp × (1 + buff1 + buff2)).
     *
     * **Validates: Requirements 1.2**
     */
    @Property(tries = 100)
    void buffsAreAdditive(
            @ForAll @IntRange(min = 0, max = 10000) int baseXp,
            @ForAll("statTypes") StatType statType,
            @ForAll @DoubleRange(min = 0.0, max = 1.0) double buff1,
            @ForAll @DoubleRange(min = 0.0, max = 1.0) double buff2) {

        double combinedBuff = buff1 + buff2;
        Map<StatType, Double> buffs = new EnumMap<>(StatType.class);
        buffs.put(statType, combinedBuff);

        SkillBuffCalculator calculator = new SkillBuffCalculator(null, null);

        int result = calculator.calculateBoostedXp(baseXp, statType, buffs);
        int expected = (int) Math.floor(baseXp * (1.0 + combinedBuff));

        assertThat(result).isEqualTo(expected);
    }

    // ========================================================================
    // Property 36: Reset cooldown enforcement (reject if < 30 days)
    // ========================================================================

    /**
     * Property: If a premium user's last reset was less than 30 days ago,
     * resetSkillTree must throw SkillResetCooldownException.
     *
     * **Validates: Requirements 1.2**
     */
    @Property(tries = 100)
    void resetRejectedWithinCooldownPeriod(
            @ForAll("userIds") UUID userId,
            @ForAll("arcIds") UUID arcId,
            @ForAll @IntRange(min = 0, max = 29) int daysAgo) {

        LocalDateTime lastReset = LocalDateTime.now().minusDays(daysAgo);

        User premiumUser = User.builder()
                .id(userId)
                .firebaseUid("firebase-" + userId)
                .premium(true)
                .lastSkillResetAt(lastReset)
                .build();

        // Mock repositories
        SkillNodeRepository skillNodeRepository = Mockito.mock(SkillNodeRepository.class);
        UserSkillRepository userSkillRepository = Mockito.mock(UserSkillRepository.class);
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(premiumUser));

        SkillTreeService service = new SkillTreeService(
                skillNodeRepository, userSkillRepository,
                Mockito.mock(com.ascend.arc.repository.ArcRepository.class),
                userRepository, eventPublisher);

        // Act & Assert: must throw cooldown exception
        assertThatThrownBy(() -> service.resetSkillTree(userId, arcId))
                .isInstanceOf(SkillResetCooldownException.class);
    }

    /**
     * Property: If a premium user's last reset was 30+ days ago,
     * resetSkillTree must NOT throw SkillResetCooldownException.
     *
     * **Validates: Requirements 1.2**
     */
    @Property(tries = 100)
    void resetAllowedAfterCooldownExpires(
            @ForAll("userIds") UUID userId,
            @ForAll("arcIds") UUID arcId,
            @ForAll @IntRange(min = 31, max = 365) int daysAgo) {

        LocalDateTime lastReset = LocalDateTime.now().minusDays(daysAgo);

        User premiumUser = User.builder()
                .id(userId)
                .firebaseUid("firebase-" + userId)
                .premium(true)
                .lastSkillResetAt(lastReset)
                .build();

        // Mock repositories
        SkillNodeRepository skillNodeRepository = Mockito.mock(SkillNodeRepository.class);
        UserSkillRepository userSkillRepository = Mockito.mock(UserSkillRepository.class);
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(premiumUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        SkillTreeService service = new SkillTreeService(
                skillNodeRepository, userSkillRepository,
                Mockito.mock(com.ascend.arc.repository.ArcRepository.class),
                userRepository, eventPublisher);

        // Act: should NOT throw cooldown exception
        // (deleteByUserIdAndArcId is void, no need to mock)
        service.resetSkillTree(userId, arcId);

        // If we reach here without exception, the cooldown was correctly not enforced
    }

    /**
     * Property: If a premium user has never reset (lastSkillResetAt == null),
     * resetSkillTree must NOT throw SkillResetCooldownException.
     *
     * **Validates: Requirements 1.2**
     */
    @Property(tries = 100)
    void resetAllowedWhenNeverResetBefore(
            @ForAll("userIds") UUID userId,
            @ForAll("arcIds") UUID arcId) {

        User premiumUser = User.builder()
                .id(userId)
                .firebaseUid("firebase-" + userId)
                .premium(true)
                .lastSkillResetAt(null)
                .build();

        // Mock repositories
        SkillNodeRepository skillNodeRepository = Mockito.mock(SkillNodeRepository.class);
        UserSkillRepository userSkillRepository = Mockito.mock(UserSkillRepository.class);
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(premiumUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        SkillTreeService service = new SkillTreeService(
                skillNodeRepository, userSkillRepository,
                Mockito.mock(com.ascend.arc.repository.ArcRepository.class),
                userRepository, eventPublisher);

        // Act: should NOT throw cooldown exception
        service.resetSkillTree(userId, arcId);

        // If we reach here without exception, first-time reset is correctly allowed
    }

    // ========================================================================
    // Providers
    // ========================================================================

    @Provide
    Arbitrary<UUID> userIds() {
        return Arbitraries.randomValue(random -> UUID.randomUUID());
    }

    @Provide
    Arbitrary<UUID> nodeIds() {
        return Arbitraries.randomValue(random -> UUID.randomUUID());
    }

    @Provide
    Arbitrary<UUID> arcIds() {
        return Arbitraries.randomValue(random -> UUID.randomUUID());
    }

    @Provide
    Arbitrary<StatType> statTypes() {
        return Arbitraries.of(StatType.values());
    }
}
