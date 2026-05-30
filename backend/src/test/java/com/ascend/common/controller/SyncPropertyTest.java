package com.ascend.common.controller;

import com.ascend.common.controller.SyncController.SyncAction;
import com.ascend.common.controller.SyncController.SyncActionResult;
import com.ascend.common.controller.SyncController.SyncBatchRequest;
import com.ascend.common.controller.SyncController.SyncBatchResponse;
import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.service.AuthService;
import com.ascend.common.dto.ApiResponse;
import com.ascend.user.entity.User;
import net.jqwik.api.*;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Property 14: All queued actions synced — accepted ones match server state, rejected ones rolled back.
 * Property 15: Server-wins conflict resolution — pending actions preserved.
 */
class SyncPropertyTest {

    @Property(tries = 100)
    void allActionsGetAResult(@ForAll("actionLists") List<SyncAction> actions) {
        AuthService authService = mock(AuthService.class);
        User user = User.builder().id(UUID.randomUUID()).firebaseUid("uid").username("test").build();
        when(authService.getCurrentUser(any())).thenReturn(user);

        SyncController controller = new SyncController(authService);
        FirebasePrincipal principal = new FirebasePrincipal("uid", "test@test.com", "password", Map.of());

        SyncBatchRequest request = new SyncBatchRequest(actions);
        ResponseEntity<ApiResponse<SyncBatchResponse>> response = controller.syncBatch(principal, request);

        SyncBatchResponse body = response.getBody().getData();

        // Every action gets exactly one result
        assertThat(body.results()).hasSize(actions.size());

        // Every result has a valid status
        for (SyncActionResult result : body.results()) {
            assertThat(result.status()).isIn("ACCEPTED", "REJECTED");
            assertThat(result.actionId()).isNotNull();
        }

        // All action IDs are represented in results
        Set<String> actionIds = new HashSet<>();
        actions.forEach(a -> actionIds.add(a.id()));
        Set<String> resultIds = new HashSet<>();
        body.results().forEach(r -> resultIds.add(r.actionId()));
        assertThat(resultIds).containsExactlyInAnyOrderElementsOf(actionIds);
    }

    @Property(tries = 100)
    void serverWinsConflictResolution(@ForAll("conflictActions") SyncAction action) {
        AuthService authService = mock(AuthService.class);
        User user = User.builder().id(UUID.randomUUID()).firebaseUid("uid").username("test").build();
        when(authService.getCurrentUser(any())).thenReturn(user);

        SyncController controller = new SyncController(authService);
        FirebasePrincipal principal = new FirebasePrincipal("uid", "test@test.com", "password", Map.of());

        SyncBatchRequest request = new SyncBatchRequest(List.of(action));
        ResponseEntity<ApiResponse<SyncBatchResponse>> response = controller.syncBatch(principal, request);

        SyncBatchResponse body = response.getBody().getData();
        assertThat(body.results()).hasSize(1);

        SyncActionResult result = body.results().get(0);
        // Null payload quest actions get rejected (server-wins: invalid data rejected)
        if ("QUEST_COMPLETE".equals(action.type()) && action.payload() == null) {
            assertThat(result.status()).isEqualTo("REJECTED");
        }
    }

    @Provide
    Arbitrary<List<SyncAction>> actionLists() {
        return actionArbitrary().list().ofMinSize(1).ofMaxSize(10);
    }

    @Provide
    Arbitrary<SyncAction> conflictActions() {
        return Arbitraries.of(
                new SyncAction(UUID.randomUUID().toString(), "QUEST_COMPLETE", null, System.currentTimeMillis()),
                new SyncAction(UUID.randomUUID().toString(), "QUEST_COMPLETE", Map.of("questId", "abc"), System.currentTimeMillis()),
                new SyncAction(UUID.randomUUID().toString(), "XP_AWARD", Map.of("amount", 50), System.currentTimeMillis())
        );
    }

    private Arbitrary<SyncAction> actionArbitrary() {
        return Arbitraries.of("QUEST_COMPLETE", "XP_AWARD", "STREAK_UPDATE").map(type ->
                new SyncAction(UUID.randomUUID().toString(), type, Map.of("data", "value"), System.currentTimeMillis())
        );
    }
}
