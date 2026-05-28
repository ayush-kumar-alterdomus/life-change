package com.ascend.quest.controller;

import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.service.AuthService;
import com.ascend.common.dto.ApiResponse;
import com.ascend.quest.dto.CompleteQuestRequest;
import com.ascend.quest.dto.CreateQuestRequest;
import com.ascend.quest.dto.DailyQuestsResponse;
import com.ascend.quest.dto.QuestCompletionResponse;
import com.ascend.quest.dto.QuestResponse;
import com.ascend.quest.service.QuestCompletionService;
import com.ascend.quest.service.QuestService;
import com.ascend.user.entity.User;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for quest operations.
 * All endpoints require authentication via Firebase token.
 * Rate limiting is applied at the filter level (20/min for quest completion).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/quests")
public class QuestController {

    private final QuestService questService;
    private final QuestCompletionService questCompletionService;
    private final AuthService authService;

    public QuestController(QuestService questService,
                           QuestCompletionService questCompletionService,
                           AuthService authService) {
        this.questService = questService;
        this.questCompletionService = questCompletionService;
        this.authService = authService;
    }

    /**
     * GET /api/v1/quests/daily
     * Returns today's quests for the authenticated user with completion status.
     */
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<DailyQuestsResponse>> getDailyQuests(
            @AuthenticationPrincipal FirebasePrincipal principal) {

        User user = authService.getCurrentUser(principal.uid());
        DailyQuestsResponse response = questService.getDailyQuests(user.getId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * POST /api/v1/quests/complete
     * Completes a quest for the authenticated user.
     * Returns 409 Conflict if the quest was already completed today.
     */
    @PostMapping("/complete")
    public ResponseEntity<ApiResponse<QuestCompletionResponse>> completeQuest(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @Valid @RequestBody CompleteQuestRequest request) {

        User user = authService.getCurrentUser(principal.uid());
        QuestCompletionResponse response = questCompletionService.completeQuest(
                user.getId(), request.getQuestId());

        return ResponseEntity.ok(ApiResponse.success("Quest completed!", response));
    }

    /**
     * POST /api/v1/quests
     * Creates a custom quest for the authenticated user.
     * Free users are limited to 5 custom quests; premium users have no limit.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<QuestResponse>> createCustomQuest(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @Valid @RequestBody CreateQuestRequest request) {

        User user = authService.getCurrentUser(principal.uid());
        QuestResponse response = questService.createCustomQuest(user.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Custom quest created", response));
    }

    /**
     * GET /api/v1/quests/{id}
     * Returns details for a specific quest.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuestResponse>> getQuestById(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @PathVariable UUID id) {

        QuestResponse response = questService.getQuestById(id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
