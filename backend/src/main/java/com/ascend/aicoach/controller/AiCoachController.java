package com.ascend.aicoach.controller;

import com.ascend.aicoach.dto.BurnoutRiskResponse;
import com.ascend.aicoach.dto.CoachRecommendationResponse;
import com.ascend.aicoach.repository.UserBehaviorMetricsRepository;
import com.ascend.aicoach.service.AiCoachService;
import com.ascend.aicoach.service.BurnoutDetectionService;
import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.service.AuthService;
import com.ascend.common.dto.ApiResponse;
import com.ascend.premium.service.FeatureGateService;
import com.ascend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/coach")
@RequiredArgsConstructor
public class AiCoachController {

    private final AiCoachService aiCoachService;
    private final BurnoutDetectionService burnoutDetectionService;
    private final UserBehaviorMetricsRepository metricsRepository;
    private final FeatureGateService featureGateService;
    private final AuthService authService;

    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<CoachRecommendationResponse>> getRecommendations(
            @AuthenticationPrincipal FirebasePrincipal principal) {
        User user = authService.getCurrentUser(principal.uid());
        featureGateService.requirePremium(user.getId());
        return ResponseEntity.ok(ApiResponse.success(aiCoachService.getRecommendations(user.getId())));
    }

    @GetMapping("/burnout-risk")
    public ResponseEntity<ApiResponse<BurnoutRiskResponse>> getBurnoutRisk(
            @AuthenticationPrincipal FirebasePrincipal principal) {
        User user = authService.getCurrentUser(principal.uid());
        featureGateService.requirePremium(user.getId());

        double risk = burnoutDetectionService.calculateBurnoutRisk(user.getId());
        String level = risk > 0.7 ? "CRITICAL" : risk > 0.5 ? "HIGH" : risk > 0.3 ? "MEDIUM" : "LOW";

        List<String> factors = new ArrayList<>();
        metricsRepository.findByUserId(user.getId()).ifPresent(m -> {
            if (m.getMissedQuests7d() > 3) factors.add("High missed quests");
            if (m.getStreakBreaks30d() > 2) factors.add("Multiple streak breaks");
            if (m.getDecliningActivityScore().doubleValue() > 0.5) factors.add("Declining activity");
        });

        return ResponseEntity.ok(ApiResponse.success(new BurnoutRiskResponse(risk, level, factors)));
    }

    @GetMapping("/optimal-time")
    public ResponseEntity<ApiResponse<Map<String, String>>> getOptimalTime(
            @AuthenticationPrincipal FirebasePrincipal principal) {
        User user = authService.getCurrentUser(principal.uid());
        featureGateService.requirePremium(user.getId());

        String time = aiCoachService.getOptimalQuestTime(user.getId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("optimalTime", time != null ? time : "No data yet")));
    }
}
