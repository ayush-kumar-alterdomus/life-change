package com.ascend.notification.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationType type,
        String title,
        String message,
        LocalDateTime sentAt,
        LocalDateTime readAt,
        boolean read
) {}
