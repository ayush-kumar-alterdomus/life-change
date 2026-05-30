package com.ascend.premium.scheduler;

import com.ascend.notification.dto.NotificationType;
import com.ascend.notification.service.NotificationService;
import com.ascend.premium.entity.Subscription;
import com.ascend.premium.repository.SubscriptionRepository;
import com.ascend.premium.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionExpiryScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionService subscriptionService;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 * * * *")
    public void checkExpiredSubscriptions() {
        log.info("Checking for expired subscriptions");

        List<Subscription> expired = subscriptionRepository.findAll().stream()
                .filter(s -> Boolean.TRUE.equals(s.getPremium()))
                .filter(s -> !Boolean.TRUE.equals(s.getAutoRenew()))
                .filter(s -> s.getExpiresAt() != null && s.getExpiresAt().isBefore(LocalDateTime.now()))
                .toList();

        for (Subscription sub : expired) {
            subscriptionService.downgradeToFree(sub.getUserId());
            notificationService.sendNotification(sub.getUserId(), NotificationType.REWARD_ALERT,
                    "Subscription Expired",
                    "Your premium subscription has ended. All your progress is preserved!");
        }

        if (!expired.isEmpty()) {
            log.info("Downgraded {} expired subscriptions", expired.size());
        }
    }
}
