package com.ascend.social.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO representing an item in the activity feed.
 */
public record ActivityFeedItem(
        UUID userId,
        String username,
        String eventType,
        String title,
        String description,
        LocalDateTime timestamp
) {
}
