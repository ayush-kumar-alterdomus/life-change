package com.ascend.admin.controller;

import com.ascend.admin.dto.*;
import com.ascend.admin.entity.SeasonalEvent;
import com.ascend.admin.service.AdminService;
import com.ascend.admin.service.EventService;
import com.ascend.admin.service.ModerationService;
import com.ascend.admin.service.SystemAnalyticsService;
import com.ascend.arc.entity.Arc;
import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.service.AuthService;
import com.ascend.boss.entity.Boss;
import com.ascend.common.dto.ApiResponse;
import com.ascend.quest.entity.Quest;
import com.ascend.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for admin operations.
 * All endpoints require ADMIN role.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final ModerationService moderationService;
    private final SystemAnalyticsService systemAnalyticsService;
    private final EventService eventService;
    private final AuthService authService;

    // ===== Content Management =====

    @PostMapping("/arcs")
    public ResponseEntity<ApiResponse<Arc>> createArc(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @RequestBody Arc arc) {
        Arc created = adminService.createArc(arc);
        return ResponseEntity.ok(ApiResponse.success("Arc created", created));
    }

    @PutMapping("/arcs/{id}")
    public ResponseEntity<ApiResponse<Arc>> updateArc(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @PathVariable UUID id,
            @RequestBody Arc updates) {
        Arc updated = adminService.updateArc(id, updates);
        return ResponseEntity.ok(ApiResponse.success("Arc updated", updated));
    }

    @PostMapping("/quests")
    public ResponseEntity<ApiResponse<Quest>> createQuest(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @RequestBody Quest quest) {
        Quest created = adminService.createQuest(quest);
        return ResponseEntity.ok(ApiResponse.success("Quest created", created));
    }

    @PostMapping("/bosses")
    public ResponseEntity<ApiResponse<Boss>> createBoss(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @RequestBody Boss boss) {
        Boss created = adminService.createBoss(boss);
        return ResponseEntity.ok(ApiResponse.success("Boss created", created));
    }

    // ===== User Moderation =====

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<AdminUserResponse>>> getFlaggedUsers(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @RequestParam(defaultValue = "true") boolean flagged,
            @RequestParam(defaultValue = "0") int page) {
        List<AdminUserResponse> users = moderationService.getFlaggedUsers(page);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PostMapping("/moderation")
    public ResponseEntity<ApiResponse<Void>> moderateUser(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @Valid @RequestBody ModerationRequest request) {
        User admin = authService.getCurrentUser(principal.uid());
        moderationService.moderateUser(admin.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Moderation action applied"));
    }

    // ===== System Analytics =====

    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<SystemAnalyticsResponse>> getSystemAnalytics(
            @AuthenticationPrincipal FirebasePrincipal principal) {
        SystemAnalyticsResponse analytics = systemAnalyticsService.getSystemAnalytics();
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }

    // ===== Seasonal Events =====

    @PostMapping("/events")
    public ResponseEntity<ApiResponse<SeasonalEvent>> createEvent(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @Valid @RequestBody SeasonalEventRequest request) {
        SeasonalEvent event = eventService.createEvent(request);
        return ResponseEntity.ok(ApiResponse.success("Event created", event));
    }
}
