package com.ascend.user.controller;

import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.service.AuthService;
import com.ascend.common.dto.ApiResponse;
import com.ascend.user.dto.IdentityTitle;
import com.ascend.user.dto.RadarChartResponse;
import com.ascend.user.dto.UserStatsResponse;
import com.ascend.user.entity.User;
import com.ascend.user.service.StatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for character stats endpoints.
 * Provides access to user RPG stats, earned identity titles, and radar chart data.
 * All endpoints require authentication via Firebase token.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatService statService;
    private final AuthService authService;

    /**
     * GET /api/v1/stats
     * Returns the authenticated user's character stats and life score.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<UserStatsResponse>> getUserStats(
            @AuthenticationPrincipal FirebasePrincipal principal) {

        User user = authService.getCurrentUser(principal.uid());
        UserStatsResponse response = statService.getUserStats(user.getId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * GET /api/v1/stats/titles
     * Returns all earned identity titles for the authenticated user.
     */
    @GetMapping("/titles")
    public ResponseEntity<ApiResponse<List<IdentityTitle>>> getEarnedTitles(
            @AuthenticationPrincipal FirebasePrincipal principal) {

        User user = authService.getCurrentUser(principal.uid());
        List<IdentityTitle> titles = statService.getEarnedTitles(user.getId());

        return ResponseEntity.ok(ApiResponse.success(titles));
    }

    /**
     * GET /api/v1/stats/radar
     * Returns stats formatted for radar chart display.
     * Each stat is represented as an entry with label, statType, and value.
     */
    @GetMapping("/radar")
    public ResponseEntity<ApiResponse<RadarChartResponse>> getRadarChartData(
            @AuthenticationPrincipal FirebasePrincipal principal) {

        User user = authService.getCurrentUser(principal.uid());
        RadarChartResponse response = statService.getRadarChartData(user.getId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
