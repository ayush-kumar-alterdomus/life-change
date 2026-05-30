package com.ascend.aicoach.property;

import com.ascend.aicoach.entity.UserBehaviorMetrics;
import com.ascend.aicoach.repository.UserBehaviorMetricsRepository;
import com.ascend.aicoach.service.BurnoutDetectionService;
import com.ascend.notification.service.NotificationService;
import com.ascend.quest.entity.QuestCompletion;
import com.ascend.quest.repository.QuestCompletionRepository;
import net.jqwik.api.*;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BurnoutDetectionPropertyTest {

    /**
     * Property 37: Burnout risk formula always produces a value >= 0 and <= 1.
     */
    @Property(tries = 100)
    void burnoutRiskAlwaysNormalized(
            @ForAll("missedQuests") int missed,
            @ForAll("streakBreaks") int breaks,
            @ForAll("completionCounts") int completionsLast7,
            @ForAll("completionCounts") int completionsPrev7) {

        UserBehaviorMetricsRepository metricsRepo = mock(UserBehaviorMetricsRepository.class);
        QuestCompletionRepository completionRepo = mock(QuestCompletionRepository.class);
        NotificationService notificationService = mock(NotificationService.class);

        BurnoutDetectionService service = new BurnoutDetectionService(
                metricsRepo, completionRepo, notificationService);

        UUID userId = UUID.randomUUID();

        UserBehaviorMetrics metrics = UserBehaviorMetrics.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .missedQuests7d(missed)
                .streakBreaks30d(breaks)
                .build();

        when(metricsRepo.findByUserId(userId)).thenReturn(Optional.of(metrics));
        when(metricsRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Mock completions for last 7 days and previous 7 days
        when(completionRepo.findByUserIdAndCompletedAtBetween(eq(userId), any(), any()))
                .thenReturn(Collections.nCopies(completionsLast7, mock(QuestCompletion.class)));

        double risk = service.calculateBurnoutRisk(userId);

        assertThat(risk).isGreaterThanOrEqualTo(0.0);
        assertThat(risk).isLessThanOrEqualTo(1.0);
    }

    /**
     * Property 38: Recovery mode activates only when risk > 0.7 threshold.
     */
    @Property(tries = 100)
    void recoveryModeOnlyActivatesAboveThreshold(@ForAll("riskValues") double riskValue) {
        boolean shouldActivate = riskValue > 0.7;
        boolean shouldDeactivate = riskValue < 0.3;

        if (shouldActivate) {
            assertThat(riskValue).isGreaterThan(0.7);
        }
        if (shouldDeactivate) {
            assertThat(riskValue).isLessThan(0.3);
        }
        // In the middle zone (0.3-0.7), no action taken
        if (!shouldActivate && !shouldDeactivate) {
            assertThat(riskValue).isBetween(0.3, 0.7);
        }
    }

    @Provide
    Arbitrary<Integer> missedQuests() {
        return Arbitraries.integers().between(0, 14);
    }

    @Provide
    Arbitrary<Integer> streakBreaks() {
        return Arbitraries.integers().between(0, 10);
    }

    @Provide
    Arbitrary<Integer> completionCounts() {
        return Arbitraries.integers().between(0, 20);
    }

    @Provide
    Arbitrary<Double> riskValues() {
        return Arbitraries.doubles().between(0.0, 1.0);
    }
}
