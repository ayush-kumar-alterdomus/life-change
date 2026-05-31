package com.ascend.social.controller;

import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.service.AuthService;
import com.ascend.common.dto.ApiResponse;
import com.ascend.social.dto.FriendResponse;
import com.ascend.social.service.FriendService;
import com.ascend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
public class SocialController {

    private final FriendService friendService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FriendResponse>>> getFriends(
            @AuthenticationPrincipal FirebasePrincipal principal) {

        User user = authService.getCurrentUser(principal.uid());
        List<FriendResponse> friends = friendService.getFriends(user.getId());
        return ResponseEntity.ok(ApiResponse.success(friends));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<FriendResponse>>> getPendingRequests(
            @AuthenticationPrincipal FirebasePrincipal principal) {

        User user = authService.getCurrentUser(principal.uid());
        List<FriendResponse> pending = friendService.getPendingRequests(user.getId());
        return ResponseEntity.ok(ApiResponse.success(pending));
    }

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Void>> sendFriendRequest(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @RequestBody Map<String, String> request) {

        User user = authService.getCurrentUser(principal.uid());
        UUID friendId = UUID.fromString(request.get("friendId"));
        friendService.sendFriendRequest(user.getId(), friendId);
        return ResponseEntity.ok(ApiResponse.success("Friend request sent"));
    }

    @PostMapping("/accept")
    public ResponseEntity<ApiResponse<Void>> acceptFriendRequest(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @RequestBody Map<String, String> request) {

        User user = authService.getCurrentUser(principal.uid());
        UUID friendId = UUID.fromString(request.get("friendId"));
        friendService.acceptFriendRequest(user.getId(), friendId);
        return ResponseEntity.ok(ApiResponse.success("Friend request accepted"));
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<ApiResponse<Void>> removeFriend(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @PathVariable UUID friendId) {

        User user = authService.getCurrentUser(principal.uid());
        friendService.removeFriend(user.getId(), friendId);
        return ResponseEntity.ok(ApiResponse.success("Friend removed"));
    }

    @PostMapping("/block")
    public ResponseEntity<ApiResponse<Void>> blockUser(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @RequestBody Map<String, String> request) {

        User user = authService.getCurrentUser(principal.uid());
        UUID blockedId = UUID.fromString(request.get("userId"));
        friendService.blockUser(user.getId(), blockedId);
        return ResponseEntity.ok(ApiResponse.success("User blocked"));
    }
}
