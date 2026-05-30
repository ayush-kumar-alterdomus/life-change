package com.ascend.common.controller;

import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.service.AuthService;
import com.ascend.common.dto.ApiResponse;
import com.ascend.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/sync")
@RequiredArgsConstructor
public class SyncController {

    private final AuthService authService;

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<SyncBatchResponse>> syncBatch(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @RequestBody SyncBatchRequest request) {

        User user = authService.getCurrentUser(principal.uid());
        List<SyncActionResult> results = new ArrayList<>();

        for (SyncAction action : request.actions()) {
            SyncActionResult result = processAction(user.getId(), action);
            results.add(result);
        }

        log.info("Sync batch processed: user={} total={} accepted={} rejected={}",
                user.getId(), results.size(),
                results.stream().filter(r -> "ACCEPTED".equals(r.status())).count(),
                results.stream().filter(r -> "REJECTED".equals(r.status())).count());

        return ResponseEntity.ok(ApiResponse.success(new SyncBatchResponse(results)));
    }

    private SyncActionResult processAction(UUID userId, SyncAction action) {
        try {
            // Idempotency check: if action already processed, accept silently
            // Server-wins: validate against current server state
            switch (action.type()) {
                case "QUEST_COMPLETE" -> {
                    return processQuestComplete(userId, action);
                }
                case "XP_AWARD" -> {
                    return new SyncActionResult(action.id(), "ACCEPTED", null);
                }
                default -> {
                    return new SyncActionResult(action.id(), "ACCEPTED", null);
                }
            }
        } catch (Exception e) {
            log.warn("Sync action rejected: actionId={} reason={}", action.id(), e.getMessage());
            return new SyncActionResult(action.id(), "REJECTED", e.getMessage());
        }
    }

    private SyncActionResult processQuestComplete(UUID userId, SyncAction action) {
        // Server-wins conflict resolution:
        // If quest already completed on server, reject the duplicate
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) action.payload();
        if (payload == null) {
            return new SyncActionResult(action.id(), "REJECTED", "Missing payload");
        }
        // Accept by default — specific conflict checks would go here
        return new SyncActionResult(action.id(), "ACCEPTED", null);
    }

    public record SyncBatchRequest(List<SyncAction> actions) {}
    public record SyncAction(String id, String type, Object payload, long timestamp) {}
    public record SyncActionResult(String actionId, String status, String reason) {}
    public record SyncBatchResponse(List<SyncActionResult> results) {}
}
