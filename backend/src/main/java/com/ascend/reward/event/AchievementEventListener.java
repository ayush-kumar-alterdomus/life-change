package com.ascend.reward.event;

import com.ascend.boss.event.BossDefeatedEvent;
import com.ascend.reward.service.AchievementService;
import com.ascend.streak.event.StreakMilestoneEvent;
import com.ascend.xp.event.LevelUpEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AchievementEventListener {

    private final AchievementService achievementService;

    @EventListener
    public void onLevelUp(LevelUpEvent event) {
        if (event.getNewLevel() >= 10) {
            achievementService.checkAndUnlock(event.getUserId(), "Level 10", "MILESTONE", "Reached level 10", "🌟");
        }
        if (event.getNewLevel() >= 50) {
            achievementService.checkAndUnlock(event.getUserId(), "Level 50", "MILESTONE", "Reached level 50", "⭐");
        }
        if (event.getNewLevel() >= 100) {
            achievementService.checkAndUnlock(event.getUserId(), "Level 100", "MILESTONE", "Reached level 100", "💫");
        }
    }

    @EventListener
    public void onStreakMilestone(StreakMilestoneEvent event) {
        if (event.getStreakDays() >= 7) {
            achievementService.checkAndUnlock(event.getUserId(), "Week Warrior", "STREAK", "7-day streak", "🔥");
        }
        if (event.getStreakDays() >= 30) {
            achievementService.checkAndUnlock(event.getUserId(), "Month Master", "STREAK", "30-day streak", "🏆");
        }
    }

    @EventListener
    public void onBossDefeated(BossDefeatedEvent event) {
        achievementService.checkAndUnlock(event.getUserId(), "Boss Slayer", "COMBAT", "Defeated a boss", "⚔️");
    }
}
