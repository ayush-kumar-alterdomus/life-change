package com.ascend.notification.event;

import com.ascend.boss.event.BossDefeatedEvent;
import com.ascend.guild.event.GuildChallengeCompleteEvent;
import com.ascend.notification.dto.NotificationType;
import com.ascend.notification.service.NotificationService;
import com.ascend.streak.event.StreakMilestoneEvent;
import com.ascend.xp.event.LevelUpEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @EventListener
    public void onLevelUp(LevelUpEvent event) {
        notificationService.sendNotification(
                event.getUserId(),
                NotificationType.LEVEL_UP,
                String.format("Level %d! ⬆️", event.getNewLevel()),
                String.format("Congratulations! You've reached level %d.", event.getNewLevel())
        );
    }

    @EventListener
    public void onStreakMilestone(StreakMilestoneEvent event) {
        notificationService.sendNotification(
                event.getUserId(),
                NotificationType.REWARD_ALERT,
                String.format("%d Day Streak! 🔥", event.getStreakDays()),
                String.format("Amazing! You earned %d bonus XP for your %d-day streak!",
                        event.getBonusXp(), event.getStreakDays())
        );
    }

    @EventListener
    public void onBossDefeated(BossDefeatedEvent event) {
        notificationService.sendNotification(
                event.getUserId(),
                NotificationType.ACHIEVEMENT,
                "Boss Defeated! 🏆",
                String.format("You defeated %s and earned %d XP!", event.getBossName(), event.getXpAwarded())
        );
    }

    @EventListener
    public void onGuildChallengeComplete(GuildChallengeCompleteEvent event) {
        // Guild events don't have a single userId; log at guild level
        log.info("Guild challenge complete: guild={} challenge={}", event.getGuildId(), event.getChallengeTitle());
    }
}
