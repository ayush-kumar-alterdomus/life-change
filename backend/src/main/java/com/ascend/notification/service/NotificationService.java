package com.ascend.notification.service;

import com.ascend.notification.entity.NotificationLog;
import com.ascend.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationLogRepository notificationLogRepository;

    /** Maximum notifications per user per hour to prevent spam. */
    static final int MAX_NOTIFICATIONS_PER_HOUR = 10;

    /**
     * Sends a notification to a user. Rate-limited to MAX_NOTIFICATIONS_PER_HOUR.
     *
     * @return the saved NotificationLog, or null if rate-limited
     */
    @Transactional
    public NotificationLog send(UUID userId, String type, String title, String message) {
        if (userId == null || type == null || title == null) {
            throw new IllegalArgumentException("userId, type, and title must not be null");
        }

        if (isRateLimited(userId)) {
            log.warn("Notification rate-limited for user={}", userId);
            return null;
        }

        NotificationLog notification = NotificationLog.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .build();

        notification = notificationLogRepository.save(notification);
        log.info("Notification sent: id={} user={} type={}", notification.getId(), userId, type);
        return notification;
    }

    /**
     * Retrieves all notifications for a user, ordered by most recent first.
     */
    @Transactional(readOnly = true)
    public List<NotificationLog> getNotificationsForUser(UUID userId) {
        return notificationLogRepository.findByUserIdOrderBySentAtDesc(userId);
    }

    /**
     * Marks a notification as read by setting readAt timestamp.
     *
     * @return true if marked successfully, false if not found or already read
     */
    @Transactional
    public boolean markAsRead(UUID notificationId) {
        return notificationLogRepository.findById(notificationId)
                .map(notification -> {
                    if (notification.getReadAt() != null) {
                        return false; // Already read
                    }
                    notification.setReadAt(LocalDateTime.now());
                    notificationLogRepository.save(notification);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Checks if a user has exceeded the hourly notification rate limit.
     */
    boolean isRateLimited(UUID userId) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        LocalDateTime now = LocalDateTime.now();
        long count = notificationLogRepository.countByUserIdAndSentAtBetween(userId, oneHourAgo, now);
        return count >= MAX_NOTIFICATIONS_PER_HOUR;
    }
}
