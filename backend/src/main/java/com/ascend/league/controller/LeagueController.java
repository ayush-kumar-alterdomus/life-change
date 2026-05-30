package com.ascend.league.controller;

import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.service.AuthService;
import com.ascend.common.dto.ApiResponse;
import com.ascend.league.dto.LeaderboardResponse;
import com.ascend.league.dto.LeagueHistoryResponse;
import com.ascend.league.dto.LeagueInfoResponse;
import com.ascend.league.service.LeagueService;
import com.ascend.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for league operations.
 * Provides endpoints for leaderboard viewing, user league info, and league history.
 * All endpoints require authentication via Firebase token.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/league")
@RequiredArgsConstructor
public class LeagueController {

    private final LeagueService leagueService;
    private final AuthService authService;

    /**
     * GET /api/v1/league/leaderboard?tier={tier}&page={page}
     * Returns a paginated leaderboard for the specified league tier.
     *
     * @param principal the authenticated Firebase user
     * @param tier      the league tier to view (e.g., "GOLD", "PLATINUM")
     * @param page      the page number (0-indexed), defaults to 0
     * @param size      the page size, defaults to 50
     * @return paginated leaderboard entries with user rank and total count
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<ApiResponse<LeaderboardResponse>> getLeaderboard(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @RequestParam String tier,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        log.debug("Leaderboard request: tier={}, page={}, size={}, user={}",
                tier, page, size, principal.uid());

        LeaderboardResponse response = leagueService.getLeaderboard(tier, page, size);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * GET /api/v1/league/info
     * Returns the current user's league information including tier, score, rank,
     * and promotion/demotion zone indicators.
     *
     * @param principal the authenticated Firebase user
     * @return the user's current league info
     */
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<LeagueInfoResponse>> getUserLeagueInfo(
            @AuthenticationPrincipal FirebasePrincipal principal) {

        User user = authService.getCurrentUser(principal.uid());
        LeagueInfoResponse response = leagueService.getUserLeagueInfo(user.getId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * GET /api/v1/league/history
     * Returns the user's past week results and promotion/demotion history.
     *
     * @param principal the authenticated Firebase user
     * @return the user's league history with weekly results
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<LeagueHistoryResponse>> getLeagueHistory(
            @AuthenticationPrincipal FirebasePrincipal principal) {

        User user = authService.getCurrentUser(principal.uid());
        LeagueHistoryResponse response = leagueService.getLeagueHistory(user.getId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
