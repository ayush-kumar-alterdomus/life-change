package com.ascend.guild.controller;

import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.service.AuthService;
import com.ascend.common.dto.ApiResponse;
import com.ascend.guild.dto.CreateGuildRequest;
import com.ascend.guild.dto.GuildChatMessage;
import com.ascend.guild.dto.GuildDetailResponse;
import com.ascend.guild.dto.GuildResponse;
import com.ascend.guild.service.GuildChatHandler;
import com.ascend.guild.service.GuildRankingService;
import com.ascend.guild.service.GuildService;
import com.ascend.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for guild operations.
 * Provides endpoints for guild CRUD, membership, chat history, and rankings.
 * All endpoints require authentication via Firebase token.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/guilds")
@RequiredArgsConstructor
public class GuildController {

    private final GuildService guildService;
    private final GuildRankingService guildRankingService;
    private final GuildChatHandler guildChatHandler;
    private final AuthService authService;

    /**
     * POST /api/v1/guilds — create a new guild.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<GuildResponse>> createGuild(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @Valid @RequestBody CreateGuildRequest request) {

        User user = authService.getCurrentUser(principal.uid());
        GuildResponse response = guildService.createGuild(user.getId(), request);

        return ResponseEntity.ok(ApiResponse.success("Guild created!", response));
    }

    /**
     * GET /api/v1/guilds — list/search guilds (paginated).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<GuildResponse>>> listGuilds(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page) {

        List<GuildResponse> guilds = guildService.listGuilds(type, page);

        return ResponseEntity.ok(ApiResponse.success(guilds));
    }

    /**
     * GET /api/v1/guilds/{id} — guild detail with members and challenges.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GuildDetailResponse>> getGuildDetail(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @PathVariable UUID id) {

        GuildDetailResponse response = guildService.getGuildDetail(id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * POST /api/v1/guilds/{id}/join — join a guild.
     */
    @PostMapping("/{id}/join")
    public ResponseEntity<ApiResponse<Void>> joinGuild(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @PathVariable UUID id) {

        User user = authService.getCurrentUser(principal.uid());
        guildService.joinGuild(user.getId(), id);

        return ResponseEntity.ok(ApiResponse.success("Joined guild successfully"));
    }

    /**
     * POST /api/v1/guilds/{id}/leave — leave a guild.
     */
    @PostMapping("/{id}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveGuild(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @PathVariable UUID id) {

        User user = authService.getCurrentUser(principal.uid());
        guildService.leaveGuild(user.getId(), id);

        return ResponseEntity.ok(ApiResponse.success("Left guild successfully"));
    }

    /**
     * GET /api/v1/guilds/{id}/chat/history — paginated chat history.
     */
    @GetMapping("/{id}/chat/history")
    public ResponseEntity<ApiResponse<List<GuildChatMessage>>> getChatHistory(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        List<GuildChatMessage> history = guildService.getChatHistory(id, page, size);

        return ResponseEntity.ok(ApiResponse.success(history));
    }

    /**
     * GET /api/v1/guilds/leaderboard — guild rankings (paginated).
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<ApiResponse<List<GuildResponse>>> getGuildLeaderboard(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @RequestParam(defaultValue = "0") int page) {

        List<GuildResponse> leaderboard = guildRankingService.getGuildLeaderboard(page);

        return ResponseEntity.ok(ApiResponse.success(leaderboard));
    }
}
