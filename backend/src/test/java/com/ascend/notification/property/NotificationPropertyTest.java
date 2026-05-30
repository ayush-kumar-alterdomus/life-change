package com.ascend.notification.property;

import com.ascend.notification.dto.NotificationType;
import com.ascend.notification.entity.NotificationLog;
import com.ascend.notification.repository.NotificationLogRepository;
import com.ascend.notification.service.FcmService;
import com.ascend.notification.service.NotificationService;
import com.ascend.streak.repository.StreakRepository;
import com.ascend.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jqwik.api.*;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Property 39: Daily cap never exceeded (max 5 per user per day).
 */
class NotificationPropertyTest {

    @Property(tries = 100)
    void dailyCapNeverExceeded(@ForAll("notificationCounts") int attemptCount) {
        NotificationLogRepository repo = mock(NotificationLogRepository.class);
        UserRepository userRepo = mock(UserRepository.class);
        StreakRepository streakRepo = mock(StreakRepository.class);
        FcmService fcmService = mock(FcmService.class);
        ObjectMapper objectMapper = new ObjectMapper();

        NotificationService service = new NotificationService(
                repo, userRepo, streakRepo, fcmService, objectMapper);

        UUID userId = UUID.randomUUID();

        // Simulate current count already at or above cap
        when(repo.countByUserIdAndSentAtBetween(eq(userId), any(), any()))
                .thenReturn((long) attemptCount);
        when(repo.save(any(NotificationLog.class))).thenAnswer(inv -> {
            NotificationLog n = inv.getArgument(0);
            n.setId(UUID.randomUUID());
            return n;
        });
        when(streakRepo.findByUserId(userId)).thenReturn(Optional.empty());

        NotificationLog result = service.sendNotification(
                userId, NotificationType.QUEST_REMINDER, "Test", "Message");

        if (attemptCount >= 5) {
            assertThat(result).isNull();
            verify(repo, never()).save(any());
        } else {
            assertThat(result).isNotNull();
        }
    }

    @Provide
    Arbitrary<Integer> notificationCounts() {
        return Arbitraries.integers().between(0, 10);
    }
}
