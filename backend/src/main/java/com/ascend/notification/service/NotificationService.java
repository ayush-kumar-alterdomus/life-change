package com.ascend.notification.service;

import com.ascend.notification.dto.NotificationPreferences;
import com.ascend.notification.dto.NotificationType;
import com.ascend.notification.entity.NotificationLog;
import com.ascend.notification.repository.NotificationLogRepository;
import com.ascend.streak.entity.Streak;
import com.ascend.streak.repository.StreakRepository;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationLogRepository notificationLogRepository;
    private final UserRepository userRepository;
    private final StreakRepository streakRepository;
    private final FcmService fcmService;
    private final ObjectMapper objectMapper;

    static final int DAILY_CAP = 5;

    @Transactional
    public NotificationLog sendNotification(UUID userId, NotificationType type, String title, String message) {
        if (userId == null || type == null || title == null) {
            throw new IllegalArgumentException("userId, type, and title must not be null");
        }

        if (isDailyCapReached(userId)) {
            log.warn("Daily cap reached for user={}", userId);
            return null;
        }

        if (isInQuietHours(userId)) {
            log.debug("Quiet hours active for user={}, suppressing", userId);
            return null;
        }

        if (isRecoveryModeThrottled(userId, type)) {
            log.debug("Recovery mode throttle for user={}", userId);
            return null;
        }

        NotificationLog notification = NotificationLog.builder()
                .userId(userId)
                .type(type.name())
                .title(title)
                .message(message)
                .build();

        notification = notificationLogRepository.save(notification);

        fcmService.sendPushNotification(userId, title, message, Map.of("type", type.name()));
        log.info("Notification sent: id={} user={} type={}", notification.getId(), userId, type);
        return notification;
    }

    /**
     * Legacy send method for backward compatibility.
     */
    @Transactional
    public NotificationLog send(UUID userId, String type, String title, String message) {
        if (userId == null || type == null || title == null) {
            throw new IllegalArgumentException("userId, type, and title must not be null");
        }

        if (isDailyCapReached(userId)) {
            log.warn("Daily cap reached for user={}", userId);
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

    @Transactional(readOnly = true)
    public Page<NotificationLog> getNotifications(UUID userId, int page) {
        return notificationLogRepository.findByUserId(userId,
                PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "sentAt")));
    }

    @Transactional
    public boolean markAsRead(UUID userId, UUID notificationId) {
        return notificationLogRepository.findById(notificationId)
                .filter(n -> n.getUserId().equals(userId))
                .map(notification -> {
                    if (notification.getReadAt() != null) return false;
                    notification.setReadAt(LocalDateTime.now());
                    notificationLogRepository.save(notification);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public int markAllAsRead(UUID userId) {
        return notificationLogRepository.markAllAsRead(userId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public long countUnread(UUID userId) {
        return notificationLogRepository.countByUserIdAndReadAtIsNull(userId);
    }

    @Transactional
    public boolean delete(UUID userId, UUID notificationId) {
        return notificationLogRepository.findById(notificationId)
                .filter(n -> n.getUserId().equals(userId))
                .map(notification -> {
                    notificationLogRepository.delete(notification);
                    return true;
                })
                .orElse(false);
    }

    boolean isDailyCapReached(UUID userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        long count = notificationLogRepository.countByUserIdAndSentAtBetween(userId, startOfDay, now);
        return count >= DAILY_CAP;
    }

    private boolean isInQuietHours(UUID userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getNotificationPreferences() == null) return false;

        try {
            NotificationPreferences prefs = objectMapper.readValue(
                    user.getNotificationPreferences(), NotificationPreferences.class);
            if (prefs.quietHoursStart() == null || prefs.quietHoursEnd() == null) return false;

            LocalTime now = LocalTime.now();
            LocalTime start = prefs.quietHoursStart();
            LocalTime end = prefs.quietHoursEnd();

            if (start.isBefore(end)) {
                return !now.isBefore(start) && now.isBefore(end);
            } else {
                return !now.isBefore(start) || now.isBefore(end);
            }
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isRecoveryModeThrottled(UUID userId, NotificationType type) {
        if (type == NotificationType.STREAK_WARNING) return false; // never throttle streak warnings

        return streakRepository.findByUserId(userId)
                .map(Streak::getComebackModeActive)
                .orElse(false);
    }
}
