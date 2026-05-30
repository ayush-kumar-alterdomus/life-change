package com.ascend.analytics.controller;

import com.ascend.analytics.dto.DashboardResponse;
import com.ascend.analytics.dto.HeatmapResponse;
import com.ascend.analytics.dto.InsightResponse;
import com.ascend.analytics.dto.WeeklyReportResponse;
import com.ascend.analytics.service.AnalyticsService;
import com.ascend.analytics.service.CorrelationService;
import com.ascend.analytics.service.LifeScoreService;
import com.ascend.analytics.service.WeeklyReportService;
import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.service.AuthService;
import com.ascend.common.dto.ApiResponse;
import com.ascend.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for analytics and insights endpoints.
 * Provides dashboard data, weekly reports, heatmaps, Life Score, and habit insights.
 * All endpoints require authentication via Firebase token.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final WeeklyReportService weeklyReportService;
    private final LifeScoreService lifeScoreService;
    private final CorrelationService correlationService;
    private final AuthService authService;

    /**
     * GET /api/v1/analytics/dashboard — full dashboard data.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @AuthenticationPrincipal FirebasePrincipal principal) {

        User user = authService.getCurrentUser(principal.uid());
        DashboardResponse response = analyticsService.getDashboard(user.getId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * GET /api/v1/analytics/weekly — latest weekly report.
     */
    @GetMapping("/weekly")
    public ResponseEntity<ApiResponse<WeeklyReportResponse>> getWeeklyReport(
            @AuthenticationPrincipal FirebasePrincipal principal) {

        User user = authService.getCurrentUser(principal.uid());
        WeeklyReportResponse response = weeklyReportService.generateWeeklyReport(user.getId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * GET /api/v1/analytics/heatmap?days={days} — activity heatmap.
     */
    @GetMapping("/heatmap")
    public ResponseEntity<ApiResponse<HeatmapResponse>> getHeatmap(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @RequestParam(defaultValue = "90") int days) {

        User user = authService.getCurrentUser(principal.uid());
        HeatmapResponse response = analyticsService.getHeatmap(user.getId(), days);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * GET /api/v1/analytics/life-score — current life score.
     */
    @GetMapping("/life-score")
    public ResponseEntity<ApiResponse<BigDecimal>> getLifeScore(
            @AuthenticationPrincipal FirebasePrincipal principal) {

        User user = authService.getCurrentUser(principal.uid());
        BigDecimal lifeScore = lifeScoreService.calculateLifeScore(user.getId());

        return ResponseEntity.ok(ApiResponse.success(lifeScore));
    }

    /**
     * GET /api/v1/analytics/insights — habit correlations (premium only).
     */
    @GetMapping("/insights")
    public ResponseEntity<ApiResponse<List<InsightResponse>>> getInsights(
            @AuthenticationPrincipal FirebasePrincipal principal) {

        User user = authService.getCurrentUser(principal.uid());
        List<InsightResponse> insights = correlationService.detectCorrelations(user.getId());

        return ResponseEntity.ok(ApiResponse.success(insights));
    }
}
