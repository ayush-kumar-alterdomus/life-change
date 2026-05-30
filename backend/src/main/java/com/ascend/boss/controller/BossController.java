package com.ascend.boss.controller;

import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.service.AuthService;
import com.ascend.boss.dto.BossDetailResponse;
import com.ascend.boss.dto.BossResponse;
import com.ascend.boss.service.BossService;
import com.ascend.boss.service.GuildBossService;
import com.ascend.common.dto.ApiResponse;
import com.ascend.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for boss battle operations.
 * Provides endpoints for viewing boss progress (individual and guild).
 * All endpoints require authentication via Firebase token.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/boss")
@RequiredArgsConstructor
public class BossController {

    private final BossService bossService;
    private final GuildBossService guildBossService;
    private final AuthService authService;

    /**
     * GET /api/v1/boss — list user's bosses (active + defeated).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BossResponse>>> getUserBosses(
            @AuthenticationPrincipal FirebasePrincipal principal) {

        User user = authService.getCurrentUser(principal.uid());
        List<BossResponse> bosses = bossService.getUserBosses(user.getId());

        return ResponseEntity.ok(ApiResponse.success(bosses));
    }

    /**
     * GET /api/v1/boss/{id} — boss detail with progress.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BossDetailResponse>> getBossDetail(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @PathVariable UUID id) {

        User user = authService.getCurrentUser(principal.uid());
        BossDetailResponse response = bossService.getBossDetail(user.getId(), id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * GET /api/v1/boss/guild/{guildId} — guild boss progress.
     */
    @GetMapping("/guild/{guildId}")
    public ResponseEntity<ApiResponse<List<BossResponse>>> getGuildBosses(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @PathVariable UUID guildId) {

        List<BossResponse> bosses = guildBossService.getActiveGuildBosses(guildId);

        return ResponseEntity.ok(ApiResponse.success(bosses));
    }
}
