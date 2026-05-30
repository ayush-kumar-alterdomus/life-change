package com.ascend.notification.controller;

import com.ascend.notification.dto.NotificationPreferences;
import com.ascend.notification.dto.NotificationResponse;
import com.ascend.notification.dto.NotificationType;
import com.ascend.notification.entity.NotificationLog;
import com.ascend.notification.service.FcmService;
import com.ascend.notification.service.NotificationService;
import com.ascend.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final FcmService fcmService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(defaultValue = "0") int page) {
        Page<NotificationLog> notifications = notificationService.getNotifications(userId, page);
        return ResponseEntity.ok(notifications.map(this::toResponse));
    }

    @PostMapping("/register-token")
    public ResponseEntity<Void> registerToken(
            @AuthenticationPrincipal UUID userId,
            @RequestBody Map<String, String> body) {
        fcmService.registerToken(userId, body.get("token"));
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        boolean marked = notificationService.markAsRead(userId, id);
        return marked ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PatchMapping("/read")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal UUID userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal UUID userId) {
        long count = notificationService.countUnread(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        boolean deleted = notificationService.delete(userId, id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PutMapping("/preferences")
    public ResponseEntity<Void> updatePreferences(
            @AuthenticationPrincipal UUID userId,
            @RequestBody NotificationPreferences preferences) {
        try {
            String json = objectMapper.writeValueAsString(preferences);
            userRepository.findById(userId).ifPresent(user -> {
                user.setNotificationPreferences(json);
                userRepository.save(user);
            });
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private NotificationResponse toResponse(NotificationLog log) {
        NotificationType type;
        try {
            type = NotificationType.valueOf(log.getType());
        } catch (IllegalArgumentException e) {
            type = NotificationType.QUEST_REMINDER;
        }
        return new NotificationResponse(
                log.getId(), type, log.getTitle(), log.getMessage(),
                log.getSentAt(), log.getReadAt(), log.getReadAt() != null
        );
    }
}
