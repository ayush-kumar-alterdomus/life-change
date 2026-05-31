package com.ascend.common.controller;

import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.service.AuthService;
import com.ascend.common.dto.ApiResponse;
import com.ascend.quest.dto.DailyQuestsResponse;
import com.ascend.quest.service.QuestService;
import com.ascend.streak.entity.Streak;
import com.ascend.streak.repository.StreakRepository;
import com.ascend.arc.entity.UserArcProgress;
import com.ascend.arc.repository.UserArcProgressRepository;
import com.ascend.arc.repository.ArcRepository;
import com.ascend.user.entity.User;
import com.ascend.xp.service.LevelCalculator;
import com.ascend.xp.service.XpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final AuthService authService;
    private final QuestService questService;
    private final StreakRepository streakRepository;
    private final UserArcProgressRepository userArcProgressRepository;
    private final ArcRepository arcRepository;
    private final XpService xpService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard(
            @AuthenticationPrincipal FirebasePrincipal principal) {

        User user = authService.getCurrentUser(principal.uid());

        // User summary
        long xpToNextLevel = LevelCalculator.xpRequiredForLevel(user.getLevel() + 1);
        Map<String, Object> userInfo = Map.of(
                "username", user.getUsername() != null ? user.getUsername() : "",
                "level", user.getLevel(),
                "xp", user.getXp(),
                "xpToNextLevel", xpToNextLevel
        );

        // Daily quests
        DailyQuestsResponse quests = questService.getDailyQuests(user.getId());

        // Streak
        Map<String, Object> streakInfo = streakRepository.findByUserId(user.getId())
                .map(s -> Map.<String, Object>of(
                        "days", s.getCurrentStreak(),
                        "comboMultiplier", s.getComboMultiplier()
                ))
                .orElse(Map.of("days", 0, "comboMultiplier", 1.0));

        // Current arc
        Map<String, Object> arcInfo = new HashMap<>();
        List<UserArcProgress> activeArcs = userArcProgressRepository
                .findByUserIdAndStatus(user.getId(), "ACTIVE");
        if (!activeArcs.isEmpty()) {
            UserArcProgress arc = activeArcs.get(0);
            String arcName = arcRepository.findById(arc.getArcId())
                    .map(a -> a.getName())
                    .orElse("Unknown");
            arcInfo.put("name", arcName);
            arcInfo.put("progress", arc.getProgressPercent());
        }

        // Daily XP
        long dailyXpEarned = xpService.getDailyXpEarned(user.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("user", userInfo);
        data.put("dailyQuests", quests);
        data.put("streak", streakInfo);
        data.put("currentArc", arcInfo);
        data.put("xpEarnedToday", dailyXpEarned);

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDailySummary(
            @AuthenticationPrincipal FirebasePrincipal principal) {

        User user = authService.getCurrentUser(principal.uid());
        DailyQuestsResponse quests = questService.getDailyQuests(user.getId());

        Streak streak = streakRepository.findByUserId(user.getId()).orElse(null);
        long dailyXp = xpService.getDailyXpEarned(user.getId());

        Map<String, Object> data = Map.of(
                "questsCompleted", quests.getCompletedQuests(),
                "questsTotal", quests.getTotalQuests(),
                "xpEarnedToday", dailyXp,
                "streakDays", streak != null ? streak.getCurrentStreak() : 0
        );

        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
