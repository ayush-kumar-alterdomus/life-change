package com.ascend.notification.controller;

import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.service.AuthService;
import com.ascend.common.dto.ApiResponse;
import com.ascend.notification.dto.NotificationResponse;
import com.ascend.notification.dto.NotificationType;
import com.ascend.notification.entity.NotificationLog;
import com.ascend.notification.service.NotificationService;
import com.ascend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getNotifications(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {

        User user = authService.getCurrentUser(principal.uid());
        Page<NotificationLog> notifications = notificationService.getNotifications(user.getId(), page);

        List<NotificationResponse> items = notifications.getContent().stream()
                .filter(n -> !unreadOnly || n.getReadAt() == null)
                .map(this::toResponse)
                .toList();

        long unreadCount = notificationService.countUnread(user.getId());

        Map<String, Object> data = Map.of(
                "notifications", items,
                "unreadCount", unreadCount,
                "totalPages", notifications.getTotalPages()
        );

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PatchMapping("/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @RequestBody Map<String, List<String>> request) {

        User user = authService.getCurrentUser(principal.uid());
        List<String> ids = request.get("notificationIds");

        if (ids == null || ids.isEmpty()) {
            notificationService.markAllAsRead(user.getId());
        } else {
            ids.forEach(id -> notificationService.markAsRead(user.getId(), UUID.fromString(id)));
        }

        return ResponseEntity.ok(ApiResponse.success("Notifications marked as read"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @PathVariable UUID id) {

        User user = authService.getCurrentUser(principal.uid());
        notificationService.delete(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted"));
    }

    private NotificationResponse toResponse(NotificationLog log) {
        NotificationType type;
        try {
            type = NotificationType.valueOf(log.getType());
        } catch (IllegalArgumentException e) {
            type = NotificationType.GENERAL;
        }
        return new NotificationResponse(
                log.getId(), type, log.getTitle(), log.getMessage(),
                log.getSentAt(), log.getReadAt(), log.getReadAt() != null
        );
    }
}
