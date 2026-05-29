package com.ascend.arc.controller;

import com.ascend.arc.dto.ArcDetailResponse;
import com.ascend.arc.dto.ArcProgressResponse;
import com.ascend.arc.dto.ArcResponse;
import com.ascend.arc.dto.CreateArcRequest;
import com.ascend.arc.dto.StartArcRequest;
import com.ascend.arc.service.ArcProgressService;
import com.ascend.arc.service.ArcRecommendationEngine;
import com.ascend.arc.service.ArcService;
import com.ascend.arc.service.CustomArcService;
import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.service.AuthService;
import com.ascend.common.dto.ApiResponse;
import com.ascend.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for arc operations.
 * All endpoints require authentication via Firebase token.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/arcs")
@RequiredArgsConstructor
public class ArcController {

    private final ArcService arcService;
    private final ArcProgressService arcProgressService;
    private final ArcRecommendationEngine arcRecommendationEngine;
    private final CustomArcService customArcService;
    private final AuthService authService;

    /**
     * GET /api/v1/arcs
     * Returns all available arcs (prebuilt + user's custom arcs).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ArcResponse>>> getAvailableArcs(
            @AuthenticationPrincipal FirebasePrincipal principal) {

        List<ArcResponse> arcs = arcService.getAvailableArcs();
        return ResponseEntity.ok(ApiResponse.success(arcs));
    }

    /**
     * GET /api/v1/arcs/{id}
     * Returns arc detail with milestones.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ArcDetailResponse>> getArcDetail(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @PathVariable UUID id) {

        ArcDetailResponse detail = arcService.getArcDetail(id);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    /**
     * POST /api/v1/arcs/start
     * Starts an arc for the authenticated user.
     */
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<ArcProgressResponse>> startArc(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @Valid @RequestBody StartArcRequest request) {

        User user = authService.getCurrentUser(principal.uid());
        ArcProgressResponse progress = arcService.startArc(user.getId(), request.getArcId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Arc started!", progress));
    }

    /**
     * GET /api/v1/arcs/progress
     * Returns current arc progress for the authenticated user.
     * Requires arcId as a query parameter.
     */
    @GetMapping("/progress")
    public ResponseEntity<ApiResponse<ArcProgressResponse>> getProgress(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @org.springframework.web.bind.annotation.RequestParam UUID arcId) {

        User user = authService.getCurrentUser(principal.uid());
        ArcProgressResponse progress = arcProgressService.getProgress(user.getId(), arcId);

        return ResponseEntity.ok(ApiResponse.success(progress));
    }

    /**
     * PATCH /api/v1/arcs/progress
     * Updates progress by completing a milestone.
     */
    @PatchMapping("/progress")
    public ResponseEntity<ApiResponse<ArcProgressResponse>> completeMilestone(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @RequestBody Map<String, String> body) {

        User user = authService.getCurrentUser(principal.uid());
        UUID arcId = UUID.fromString(body.get("arcId"));
        UUID milestoneId = UUID.fromString(body.get("milestoneId"));

        ArcProgressResponse progress = arcProgressService.completeMilestone(
                user.getId(), arcId, milestoneId);

        return ResponseEntity.ok(ApiResponse.success("Milestone completed!", progress));
    }

    /**
     * POST /api/v1/arcs
     * Creates a custom arc for the authenticated user.
     * Free users limited to 1 custom arc; premium users unlimited.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ArcDetailResponse>> createCustomArc(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @Valid @RequestBody CreateArcRequest request) {

        User user = authService.getCurrentUser(principal.uid());
        // TODO: Determine premium status from user subscription
        boolean isPremium = false;

        ArcDetailResponse response = customArcService.createCustomArc(user.getId(), request, isPremium);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Custom arc created", response));
    }

    /**
     * POST /api/v1/arcs/recommend
     * Returns arc recommendations based on user goals and assessment.
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/recommend")
    public ResponseEntity<ApiResponse<List<ArcResponse>>> recommendArc(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @RequestBody Map<String, Object> body) {

        List<String> goals = (List<String>) body.getOrDefault("goals", List.of());
        Map<String, Object> assessmentAnswers = (Map<String, Object>) body.getOrDefault("assessmentAnswers", Map.of());
        int availableMinutes = body.containsKey("availableMinutesPerDay")
                ? ((Number) body.get("availableMinutesPerDay")).intValue()
                : 30;

        List<ArcResponse> recommendations = arcRecommendationEngine.recommend(
                goals, assessmentAnswers, availableMinutes);

        return ResponseEntity.ok(ApiResponse.success(recommendations));
    }

    /**
     * POST /api/v1/arcs/{id}/abandon
     * Abandons an active arc for the authenticated user.
     */
    @PostMapping("/{id}/abandon")
    public ResponseEntity<ApiResponse<Void>> abandonArc(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @PathVariable UUID id) {

        User user = authService.getCurrentUser(principal.uid());
        arcService.abandonArc(user.getId(), id);

        return ResponseEntity.ok(ApiResponse.success("Arc abandoned", null));
    }
}
