package com.ascend.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserNotificationBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendXpUpdate(UUID userId, int xpGained, long newTotal, int newLevel) {
        send(userId, "/queue/xp", Map.of(
                "xpGained", xpGained,
                "newTotal", newTotal,
                "newLevel", newLevel));
    }

    public void sendLevelUp(UUID userId, int newLevel, List<String> unlocks) {
        send(userId, "/queue/level", Map.of(
                "newLevel", newLevel,
                "unlocks", unlocks));
    }

    public void sendStreakAlert(UUID userId, String message, int currentStreak) {
        send(userId, "/queue/streak", Map.of(
                "message", message,
                "currentStreak", currentStreak));
    }

    public void sendBossProgress(UUID userId, String bossName, int progress) {
        send(userId, "/queue/boss", Map.of(
                "bossName", bossName,
                "progress", progress));
    }

    public void sendNotification(UUID userId, String title, String message) {
        send(userId, "/queue/notifications", Map.of(
                "title", title,
                "message", message));
    }

    private void send(UUID userId, String destination, Object payload) {
        messagingTemplate.convertAndSendToUser(userId.toString(), destination, payload);
        log.debug("WS broadcast to user={} dest={}", userId, destination);
    }
}
