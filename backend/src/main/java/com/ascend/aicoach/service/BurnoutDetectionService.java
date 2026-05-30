package com.ascend.aicoach.service;

import com.ascend.aicoach.entity.UserBehaviorMetrics;
import com.ascend.aicoach.repository.UserBehaviorMetricsRepository;
import com.ascend.notification.dto.NotificationType;
import com.ascend.notification.service.NotificationService;
import com.ascend.quest.repository.QuestCompletionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BurnoutDetectionService {

    private final UserBehaviorMetricsRepository metricsRepository;
    private final QuestCompletionRepository completionRepository;
    private final NotificationService notificationService;

    static final double ACTIVATION_THRESHOLD = 0.7;
    static final double DEACTIVATION_THRESHOLD = 0.3;

    @Transactional
    public double calculateBurnoutRisk(UUID userId) {
        UserBehaviorMetrics metrics = getOrCreate(userId);

        LocalDateTime now = LocalDateTime.now();
        long completionsLast7 = completionRepository
                .findByUserIdAndCompletedAtBetween(userId, now.minusDays(7), now).size();
        long completionsPrev7 = completionRepository
                .findByUserIdAndCompletedAtBetween(userId, now.minusDays(14), now.minusDays(7)).size();

        double decliningActivity = completionsPrev7 > 0
                ? Math.max(0, 1.0 - ((double) completionsLast7 / completionsPrev7))
                : 0.0;

        double motivationScore = Math.max(0.1, (completionsLast7 / 7.0) * (1 + metrics.getStreakBreaks30d() * -0.1));

        double rawRisk = (metrics.getMissedQuests7d() + metrics.getStreakBreaks30d() + decliningActivity) / (motivationScore * 10);
        double normalizedRisk = Math.min(1.0, Math.max(0.0, rawRisk));

        metrics.setDecliningActivityScore(BigDecimal.valueOf(decliningActivity).setScale(4, RoundingMode.HALF_UP));
        metrics.setMotivationScore(BigDecimal.valueOf(motivationScore).setScale(4, RoundingMode.HALF_UP));
        metrics.setBurnoutRisk(BigDecimal.valueOf(normalizedRisk).setScale(4, RoundingMode.HALF_UP));
        metricsRepository.save(metrics);

        return normalizedRisk;
    }

    @Transactional
    public void evaluateAndAct(UUID userId) {
        double risk = calculateBurnoutRisk(userId);
        UserBehaviorMetrics metrics = getOrCreate(userId);

        if (risk > ACTIVATION_THRESHOLD && !Boolean.TRUE.equals(metrics.getRecoveryModeActive())) {
            activateRecoveryMode(userId);
        } else if (risk < DEACTIVATION_THRESHOLD && Boolean.TRUE.equals(metrics.getRecoveryModeActive())) {
            deactivateRecoveryMode(userId);
        }
    }

    @Transactional
    public void activateRecoveryMode(UUID userId) {
        UserBehaviorMetrics metrics = getOrCreate(userId);
        metrics.setRecoveryModeActive(true);
        metrics.setRecoveryStartedAt(LocalDateTime.now());
        metricsRepository.save(metrics);

        notificationService.sendNotification(userId, NotificationType.QUEST_REMINDER,
                "Recovery Mode Activated 🌱",
                "We noticed you might need a break. Quest load reduced by 50% with 1.5x XP bonus!");

        log.info("Recovery mode activated for user={}", userId);
    }

    @Transactional
    public void deactivateRecoveryMode(UUID userId) {
        UserBehaviorMetrics metrics = getOrCreate(userId);
        metrics.setRecoveryModeActive(false);
        metrics.setRecoveryStartedAt(null);
        metricsRepository.save(metrics);

        notificationService.sendNotification(userId, NotificationType.QUEST_REMINDER,
                "Recovery Complete! 💪",
                "You're back on track. Normal quest difficulty restored.");

        log.info("Recovery mode deactivated for user={}", userId);
    }

    private UserBehaviorMetrics getOrCreate(UUID userId) {
        return metricsRepository.findByUserId(userId).orElseGet(() -> {
            UserBehaviorMetrics m = UserBehaviorMetrics.builder().userId(userId).build();
            return metricsRepository.save(m);
        });
    }
}
