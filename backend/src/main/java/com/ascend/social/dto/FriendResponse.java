package com.ascend.social.dto;

import java.util.UUID;

/**
 * Response DTO representing a friend in the social system.
 */
public record FriendResponse(
        UUID userId,
        String username,
        String avatarUrl,
        int level,
        int streak,
        String status
) {
}
