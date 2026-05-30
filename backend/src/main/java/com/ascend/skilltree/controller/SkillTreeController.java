package com.ascend.skilltree.controller;

import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.service.AuthService;
import com.ascend.common.dto.ApiResponse;
import com.ascend.common.entity.StatType;
import com.ascend.skilltree.dto.SkillBuffSummary;
import com.ascend.skilltree.dto.SkillNodeResponse;
import com.ascend.skilltree.dto.SkillTreeResponse;
import com.ascend.skilltree.dto.UnlockSkillRequest;
import com.ascend.skilltree.service.SkillBuffCalculator;
import com.ascend.skilltree.service.SkillTreeService;
import com.ascend.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for skill tree operations.
 * All endpoints require authentication via Firebase token.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/skills")
@RequiredArgsConstructor
public class SkillTreeController {

    private final SkillTreeService skillTreeService;
    private final SkillBuffCalculator skillBuffCalculator;
    private final AuthService authService;

    /**
     * GET /api/v1/skills/tree?arcId={id}
     * Returns the skill tree for a specific arc, annotated with the user's unlock status.
     */
    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<SkillTreeResponse>> getSkillTree(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @RequestParam UUID arcId) {

        User user = authService.getCurrentUser(principal.uid());
        SkillTreeResponse response = skillTreeService.getSkillTree(user.getId(), arcId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * POST /api/v1/skills/unlock
     * Unlocks a skill node for the authenticated user after validating prerequisites.
     */
    @PostMapping("/unlock")
    public ResponseEntity<ApiResponse<SkillNodeResponse>> unlockSkill(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @Valid @RequestBody UnlockSkillRequest request) {

        User user = authService.getCurrentUser(principal.uid());
        SkillNodeResponse response = skillTreeService.unlockNode(user.getId(), request.getSkillNodeId());

        return ResponseEntity.ok(ApiResponse.success("Skill unlocked!", response));
    }

    /**
     * GET /api/v1/skills/buffs
     * Returns the active buff summary for the authenticated user,
     * aggregated by stat type across all unlocked skill nodes.
     */
    @GetMapping("/buffs")
    public ResponseEntity<ApiResponse<List<SkillBuffSummary>>> getActiveBuffs(
            @AuthenticationPrincipal FirebasePrincipal principal) {

        User user = authService.getCurrentUser(principal.uid());
        Map<StatType, Double> buffs = skillBuffCalculator.getActiveBuffs(user.getId());

        List<SkillBuffSummary> summaries = buffs.entrySet().stream()
                .map(entry -> SkillBuffSummary.builder()
                        .statType(entry.getKey())
                        .totalBuffPercent(entry.getValue())
                        .build())
                .toList();

        return ResponseEntity.ok(ApiResponse.success(summaries));
    }

    /**
     * POST /api/v1/skills/reset?arcId={id}
     * Resets the skill tree for a specific arc. Premium users only, with 30-day cooldown.
     */
    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<Void>> resetSkillTree(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @RequestParam UUID arcId) {

        User user = authService.getCurrentUser(principal.uid());
        skillTreeService.resetSkillTree(user.getId(), arcId);

        return ResponseEntity.ok(ApiResponse.success("Skill tree reset successfully"));
    }
}
