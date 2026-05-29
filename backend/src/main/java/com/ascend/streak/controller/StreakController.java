package com.ascend.streak.controller;

import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.service.AuthService;
import com.ascend.common.dto.ApiResponse;
import com.ascend.streak.dto.StreakResponse;
import com.ascend.streak.service.StreakService;
import com.ascend.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * REST controller for streak operations.
 * All endpoints require authentication via Firebase token.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/streak")
public class StreakController {

    private final StreakService streakService;
    private final AuthService authService;

    public StreakController(StreakService streakService, AuthService authService) {
        this.streakService = streakService;
        this.authService = authService;
    }

    /**
     * GET /api/v1/streak
     * Returns current streak info for the authenticated user.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<StreakResponse>> getStreak(
            @AuthenticationPrincipal FirebasePrincipal principal) {

        User user = authService.getCurrentUser(principal.uid());
        StreakResponse response = streakService.getStreak(user.getId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * GET /api/v1/streak/history
     * Returns streak history for the past 30 days.
     * Currently returns an empty list as history tracking is not yet implemented.
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<?>>> getStreakHistory(
            @AuthenticationPrincipal FirebasePrincipal principal) {

        User user = authService.getCurrentUser(principal.uid());
        log.debug("Fetching streak history for user={}", user.getId());

        // Placeholder: history tracking not yet implemented
        return ResponseEntity.ok(ApiResponse.success(Collections.emptyList()));
    }

    /**
     * POST /api/v1/streak/shield/activate
     * Manually activates the streak shield for the authenticated user (if available).
     */
    @PostMapping("/shield/activate")
    public ResponseEntity<ApiResponse<StreakResponse>> activateShield(
            @AuthenticationPrincipal FirebasePrincipal principal) {

        User user = authService.getCurrentUser(principal.uid());
        streakService.activateShield(user.getId());
        StreakResponse response = streakService.getStreak(user.getId());

        return ResponseEntity.ok(ApiResponse.success("Shield activated", response));
    }
}
