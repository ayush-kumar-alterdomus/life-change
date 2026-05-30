package com.ascend.common.service;

import com.ascend.boss.event.BossDefeatedEvent;
import com.ascend.reward.event.AchievementUnlockedEvent;
import com.ascend.streak.event.StreakBrokenEvent;
import com.ascend.xp.event.LevelUpEvent;
import com.ascend.xp.event.XpAwardedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RealtimeEventListener {

    private final UserNotificationBroadcaster broadcaster;

    @EventListener
    public void onXpAwarded(XpAwardedEvent event) {
        broadcaster.sendXpUpdate(event.getUserId(), event.getXpAmount(),
                event.getNewTotalXp(), event.getNewLevel());
    }

    @EventListener
    public void onLevelUp(LevelUpEvent event) {
        broadcaster.sendLevelUp(event.getUserId(), event.getNewLevel(), event.getUnlockedFeatures());
    }

    @EventListener
    public void onStreakBroken(StreakBrokenEvent event) {
        String message = event.isComebackModeActivated()
                ? "Streak lost, but Comeback Mode activated!"
                : "Your streak was broken. Start fresh!";
        broadcaster.sendStreakAlert(event.getUserId(), message, 0);
    }

    @EventListener
    public void onBossDefeated(BossDefeatedEvent event) {
        broadcaster.sendBossProgress(event.getUserId(), event.getBossName(), 100);
    }

    @EventListener
    public void onAchievementUnlocked(AchievementUnlockedEvent event) {
        broadcaster.sendNotification(event.getUserId(),
                "Achievement Unlocked! 🏅",
                event.getAchievementName());
    }
}
