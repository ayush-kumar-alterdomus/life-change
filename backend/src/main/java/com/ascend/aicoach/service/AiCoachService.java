package com.ascend.aicoach.service;

import com.ascend.aicoach.dto.CoachRecommendationResponse;
import com.ascend.aicoach.entity.UserBehaviorMetrics;
import com.ascend.aicoach.repository.UserBehaviorMetricsRepository;
import com.ascend.quest.entity.QuestCompletion;
import com.ascend.quest.repository.QuestCompletionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiCoachService {

    private final UserBehaviorMetricsRepository metricsRepository;
    private final QuestCompletionRepository completionRepository;
    private final AdaptiveDifficultyService difficultyService;

    public CoachRecommendationResponse getRecommendations(UUID userId) {
        UserBehaviorMetrics metrics = metricsRepository.findByUserId(userId)
                .orElse(UserBehaviorMetrics.builder().userId(userId).build());

        List<String> recommendations = new ArrayList<>();

        double risk = metrics.getBurnoutRisk().doubleValue();
        if (risk > 0.5) {
            recommendations.add("Consider taking shorter quests today — your energy levels suggest a lighter load.");
        }
        if (metrics.getMissedQuests7d() > 3) {
            recommendations.add("You've missed a few quests recently. Try setting reminders for your peak hours.");
        }
        if (metrics.getStreakBreaks30d() == 0) {
            recommendations.add("Amazing consistency! Challenge yourself with a harder quest category.");
        }

        String optimalTime = getOptimalQuestTime(userId);
        if (optimalTime != null) {
            recommendations.add("Your best completion time is around " + optimalTime + ". Schedule quests then!");
        }

        String difficulty = difficultyService.adjustDifficulty(userId);

        return new CoachRecommendationResponse(
                recommendations,
                risk,
                Boolean.TRUE.equals(metrics.getRecoveryModeActive()),
                optimalTime,
                difficulty);
    }

    public String getOptimalQuestTime(UUID userId) {
        LocalDateTime now = LocalDateTime.now();
        List<QuestCompletion> completions = completionRepository
                .findByUserIdAndCompletedAtBetween(userId, now.minusDays(30), now);

        if (completions.isEmpty()) return null;

        Map<Integer, Long> hourCounts = completions.stream()
                .collect(Collectors.groupingBy(c -> c.getCompletedAt().getHour(), Collectors.counting()));

        return hourCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> LocalTime.of(e.getKey(), 0) + " - " + LocalTime.of(e.getKey() + 1, 0))
                .orElse(null);
    }
}
