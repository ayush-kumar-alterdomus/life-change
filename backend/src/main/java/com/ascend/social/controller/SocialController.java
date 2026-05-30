package com.ascend.social.controller;

import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.service.AuthService;
import com.ascend.common.dto.ApiResponse;
import com.ascend.social.dto.ActivityFeedItem;
import com.ascend.social.dto.AccountabilityPartnerResponse;
import com.ascend.social.dto.ChallengeResponse;
import com.ascend.social.dto.CreateChallengeRequest;
import com.ascend.social.dto.FriendResponse;
import com.ascend.social.model.PrivacyLevel;
import com.ascend.social.service.AccountabilityService;
import com.ascend.social.service.ActivityFeedService;
import com.ascend.social.service.ChallengeService;
import com.ascend.social.service.FriendService;
import com.ascend.user.entity.User;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for social features including friends, challenges,
 * activity feed, accountability partners, and privacy settings.
 * All endpoints require authentication via Firebase token.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/social")
public class SocialController {

    private final FriendService friendService;
    private final ChallengeService challengeService;
    private final ActivityFeedService activityFeedService;
    private final AccountabilityService accountabilityService;
    private final AuthService authService;

    public SocialController(FriendService friendService,
                            ChallengeService challengeService,
                            ActivityFeedService activityFeedService,
                            AccountabilityService accountabilityService,
                            AuthService authService) {
        this.friendService = friendService;
        this.challengeService = challengeService;
        this.activityFeedService = activityFeedService;
        this.accountabilityService = accountabilityService;
        this.authService = authService;
    }

    /**
     * GET /api/v1/social/friends
     * Returns the list of accepted friends for the authenticated user.
     */
    @GetMapping("/friends")
    public ResponseEntity<ApiResponse<List<FriendResponse>>> getFriends(
            @AuthenticationPrincipal FirebasePrincipal principal) {

        User user = authService.getCurrentUser(principal.uid());
        List<FriendResponse> friends = friendService.getFriends(user.getId());

        return ResponseEntity.ok(ApiResponse.success(friends));
    }

    /**
     * GET /api/v1/social/friends/pending
     * Returns the list of pending incoming friend requests for the authenticated user.
     */
    @GetMapping("/friends/pending")
    public ResponseEntity<ApiResponse<List<FriendResponse>>> getPendingRequests(
            @AuthenticationPrincipal FirebasePrincipal principal) {

        User user = authService.getCurrentUser(principal.uid());
        List<FriendResponse> pending = friendService.getPendingRequests(user.getId());

        return ResponseEntity.ok(ApiResponse.success(pending));
    }

    /**
     * POST /api/v1/social/friends/request
     * Sends a friend request to the specified user.
     */
    @PostMapping("/friends/request")
    public ResponseEntity<ApiResponse<Void>> sendFriendRequest(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @Valid @RequestBody Map<String, UUID> body) {

        User user = authService.getCurrentUser(principal.uid());
        UUID friendId = body.get("friendId");
        friendService.sendFriendRequest(user.getId(), friendId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Friend request sent"));
    }

    /**
     * POST /api/v1/social/friends/accept
     * Accepts a pending friend request from the specified user.
     */
    @PostMapping("/friends/accept")
    public ResponseEntity<ApiResponse<Void>> acceptFriendRequest(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @Valid @RequestBody Map<String, UUID> body) {

        User user = authService.getCurrentUser(principal.uid());
        UUID friendId = body.get("friendId");
        friendService.acceptFriendRequest(user.getId(), friendId);

        return ResponseEntity.ok(ApiResponse.success("Friend request accepted"));
    }

    /**
     * DELETE /api/v1/social/friends/{friendId}
     * Removes an existing friendship.
     */
    @DeleteMapping("/friends/{friendId}")
    public ResponseEntity<ApiResponse<Void>> removeFriend(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @PathVariable UUID friendId) {

        User user = authService.getCurrentUser(principal.uid());
        friendService.removeFriend(user.getId(), friendId);

        return ResponseEntity.ok(ApiResponse.success("Friend removed"));
    }

    /**
     * POST /api/v1/social/friends/block
     * Blocks a user.
     */
    @PostMapping("/friends/block")
    public ResponseEntity<ApiResponse<Void>> blockUser(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @Valid @RequestBody Map<String, UUID> body) {

        User user = authService.getCurrentUser(principal.uid());
        UUID blockedId = body.get("userId");
        friendService.blockUser(user.getId(), blockedId);

        return ResponseEntity.ok(ApiResponse.success("User blocked"));
    }

    /**
     * POST /api/v1/social/challenges
     * Creates a new challenge between the authenticated user and a friend.
     */
    @PostMapping("/challenges")
    public ResponseEntity<ApiResponse<ChallengeResponse>> createChallenge(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @Valid @RequestBody CreateChallengeRequest request) {

        User user = authService.getCurrentUser(principal.uid());
        ChallengeResponse response = challengeService.createChallenge(user.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Challenge created", response));
    }

    /**
     * GET /api/v1/social/challenges
     * Returns the list of active challenges for the authenticated user.
     */
    @GetMapping("/challenges")
    public ResponseEntity<ApiResponse<List<ChallengeResponse>>> getActiveChallenges(
            @AuthenticationPrincipal FirebasePrincipal principal) {

        User user = authService.getCurrentUser(principal.uid());
        List<ChallengeResponse> challenges = challengeService.getActiveChallenges(user.getId());

        return ResponseEntity.ok(ApiResponse.success(challenges));
    }

    /**
     * GET /api/v1/social/feed
     * Returns the paginated activity feed for the authenticated user's friends.
     */
    @GetMapping("/feed")
    public ResponseEntity<ApiResponse<List<ActivityFeedItem>>> getActivityFeed(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @RequestParam(defaultValue = "0") int page) {

        User user = authService.getCurrentUser(principal.uid());
        List<ActivityFeedItem> feed = activityFeedService.getFeed(user.getId(), page);

        return ResponseEntity.ok(ApiResponse.success(feed));
    }

    /**
     * POST /api/v1/social/accountability/pair
     * Pairs the authenticated user with an accountability partner.
     */
    @PostMapping("/accountability/pair")
    public ResponseEntity<ApiResponse<Void>> pairAccountabilityPartner(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @Valid @RequestBody Map<String, UUID> body) {

        User user = authService.getCurrentUser(principal.uid());
        UUID partnerId = body.get("partnerId");
        accountabilityService.pairPartner(user.getId(), partnerId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Accountability partner paired"));
    }

    /**
     * GET /api/v1/social/accountability
     * Returns the current accountability partner for the authenticated user.
     */
    @GetMapping("/accountability")
    public ResponseEntity<ApiResponse<AccountabilityPartnerResponse>> getAccountabilityPartner(
            @AuthenticationPrincipal FirebasePrincipal principal) {

        User user = authService.getCurrentUser(principal.uid());
        return accountabilityService.getPartner(user.getId())
                .map(partner -> ResponseEntity.ok(ApiResponse.success(partner)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/v1/social/privacy
     * Updates the privacy level for the authenticated user.
     */
    @PutMapping("/privacy")
    public ResponseEntity<ApiResponse<Void>> updatePrivacyLevel(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @Valid @RequestBody Map<String, PrivacyLevel> body) {

        User user = authService.getCurrentUser(principal.uid());
        PrivacyLevel level = body.get("level");
        user.setPrivacyLevel(level.name());
        friendService.updateUser(user);

        return ResponseEntity.ok(ApiResponse.success("Privacy level updated"));
    }
}
