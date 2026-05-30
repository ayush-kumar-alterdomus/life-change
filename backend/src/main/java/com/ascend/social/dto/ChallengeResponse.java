package com.ascend.social.dto;

import java.util.UUID;

/**
 * Response DTO representing a challenge between two friends.
 */
public record ChallengeResponse(
        UUID id,
        FriendResponse opponent,
        String title,
        int target,
        int myProgress,
        int opponentProgress,
        String status,
        String winner
) {
}
