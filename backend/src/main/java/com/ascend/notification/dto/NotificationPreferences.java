package com.ascend.notification.dto;

import java.time.LocalTime;
import java.util.Map;

public record NotificationPreferences(
        boolean enabled,
        LocalTime quietHoursStart,
        LocalTime quietHoursEnd,
        Map<NotificationType, Boolean> types
) {}
