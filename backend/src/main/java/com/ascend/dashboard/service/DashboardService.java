package com.ascend.dashboard.service;

import com.ascend.arc.dto.ArcProgressResponse;
import com.ascend.arc.service.ArcProgressService;
import com.ascend.dashboard.dto.*;
import com.ascend.notification.service.NotificationService;
import com.ascend.quest.dto.DailyQuestsResponse;
import com.ascend.quest.dto.QuestResponse;
import com.ascend.quest.service.QuestService;
import com.ascend.streak.dto.StreakResponse;
import com.ascend.streak.service.StreakService;
import com.ascend.user.entity.User;
import com.ascend.xp.service.ComboCalculator;
import com.ascend.xp.service.LevelCalculator;
import com.ascend.xp.service.XpService;
import com.ascend.streak.repository.StreakRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final XpService xpService;
    private final StreakService streakService;
    private final QuestService questService;
    private final ArcProgressService arcProgressService;
    private final NotificationService notificationService;
    private final StreakRepository streakRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(User user) {
        UUID userId = user.getId();

        return DashboardResponse.builder()
                .user(buildUserSection(user))
                .xp(buildXpSection(user))
                .streak(buildStreakSection(userId))
                .dailyStats(buildDailyStatsSection(userId))
                .quests(buildQuestsSection(userId))
                .activeArc(buildArcSection(userId))
                .notifications(buildNotificationSection(userId))
                .build();
    }

    private DashboardUserSection buildUserSection(User user) {
        return new DashboardUserSection(
                user.getUsername(),
                user.getLevel(),
                user.getAvatarUrl(),
                Boolean.TRUE.equals(user.getPremium())
        );
    }

    private DashboardXpSection buildXpSection(User user) {
        try {
            long dailyXpEarned = xpService.getDailyXpEarned(user.getId());
            int dailyCap = xpService.getDailyCap(user.getLevel());
            long xpToNextLevel = LevelCalculator.xpToNextLevel(user.getLevel(), user.getXp());
            double comboMultiplier = streakRepository.findByUserId(user.getId())
                    .map(s -> ComboCalculator.calculateComboMultiplier(s.getCurrentStreak()))
                    .orElse(1.0);

            return new DashboardXpSection(
                    user.getXp(), user.getLevel(), xpToNextLevel,
                    dailyXpEarned, dailyCap, comboMultiplier
            );
        } catch (Exception e) {
            log.warn("Failed to build XP section for user={}: {}", user.getId(), e.getMessage());
            return null;
        }
    }

    private DashboardStreakSection buildStreakSection(UUID userId) {
        try {
            StreakResponse streak = streakService.getStreak(userId);
            return new DashboardStreakSection(
                    streak.getCurrentStreak(),
                    streak.getLongestStreak(),
                    streak.isShieldAvailable(),
                    streak.isComebackModeActive()
            );
        } catch (Exception e) {
            log.warn("Failed to build streak section for user={}: {}", userId, e.getMessage());
            return null;
        }
    }

    private DashboardDailyStatsSection buildDailyStatsSection(UUID userId) {
        try {
            DailyQuestsResponse daily = questService.getDailyQuests(userId);
            int total = daily.getTotalQuests();
            int completed = daily.getCompletedQuests();
            int percentage = total > 0 ? (completed * 100) / total : 0;
            return new DashboardDailyStatsSection(completed, total, percentage);
        } catch (Exception e) {
            log.warn("Failed to build daily stats section for user={}: {}", userId, e.getMessage());
            return null;
        }
    }

    private List<QuestResponse> buildQuestsSection(UUID userId) {
        try {
            DailyQuestsResponse daily = questService.getDailyQuests(userId);
            return daily.getQuests();
        } catch (Exception e) {
            log.warn("Failed to build quests section for user={}: {}", userId, e.getMessage());
            return null;
        }
    }

    private DashboardArcSection buildArcSection(UUID userId) {
        try {
            ArcProgressResponse arc = arcProgressService.getActiveArc(userId);
            if (arc == null) return null;
            return new DashboardArcSection(
                    arc.getArcId().toString(),
                    arc.getArcName(),
                    arc.getCurrentPhase() != null ? arc.getCurrentPhase().name() : null,
                    arc.getProgressPercent() != null ? arc.getProgressPercent() : 0,
                    arc.getCurrentPhase() != null ? arc.getCurrentPhase().name() : null
            );
        } catch (Exception e) {
            log.warn("Failed to build arc section for user={}: {}", userId, e.getMessage());
            return null;
        }
    }

    private DashboardNotificationSection buildNotificationSection(UUID userId) {
        try {
            long unread = notificationService.countUnread(userId);
            return new DashboardNotificationSection(unread);
        } catch (Exception e) {
            log.warn("Failed to build notification section for user={}: {}", userId, e.getMessage());
            return new DashboardNotificationSection(0);
        }
    }
}
