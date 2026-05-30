package com.ascend.notification.service;

import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final UserRepository userRepository;

    public void sendPushNotification(UUID userId, String title, String body, Map<String, String> data) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getFcmToken() == null) {
            log.debug("No FCM token for user={}, skipping push", userId);
            return;
        }

        Message.Builder builder = Message.builder()
                .setToken(user.getFcmToken())
                .setNotification(Notification.builder().setTitle(title).setBody(body).build());

        if (data != null && !data.isEmpty()) {
            builder.putAllData(data);
        }

        try {
            FirebaseMessaging.getInstance().send(builder.build());
            log.debug("Push sent to user={}", userId);
        } catch (FirebaseMessagingException e) {
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                log.warn("FCM token expired for user={}, clearing", userId);
                user.setFcmToken(null);
                userRepository.save(user);
            } else {
                log.error("FCM send failed for user={}: {}", userId, e.getMessage());
            }
        }
    }

    @Transactional
    public void registerToken(UUID userId, String fcmToken) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setFcmToken(fcmToken);
            userRepository.save(user);
        });
    }

    @Transactional
    public void removeToken(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setFcmToken(null);
            userRepository.save(user);
        });
    }
}
