package com.ascend.premium.controller;

import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.service.AuthService;
import com.ascend.common.dto.ApiResponse;
import com.ascend.premium.dto.PremiumFeature;
import com.ascend.premium.dto.SubscriptionStatusResponse;
import com.ascend.premium.service.FeatureGateService;
import com.ascend.premium.service.SubscriptionService;
import com.ascend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/premium")
@RequiredArgsConstructor
public class PremiumController {

    private final SubscriptionService subscriptionService;
    private final FeatureGateService featureGateService;
    private final AuthService authService;

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<SubscriptionStatusResponse>> getStatus(
            @AuthenticationPrincipal FirebasePrincipal principal) {
        User user = authService.getCurrentUser(principal.uid());
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getSubscriptionStatus(user.getId())));
    }

    @PostMapping("/trial")
    public ResponseEntity<ApiResponse<Void>> startTrial(
            @AuthenticationPrincipal FirebasePrincipal principal) {
        User user = authService.getCurrentUser(principal.uid());
        subscriptionService.startTrial(user.getId());
        return ResponseEntity.ok(ApiResponse.success("7-day trial started"));
    }

    @PostMapping("/subscribe")
    public ResponseEntity<ApiResponse<Void>> subscribe(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @RequestBody Map<String, String> body) {
        User user = authService.getCurrentUser(principal.uid());
        String provider = body.getOrDefault("provider", "UNKNOWN");
        String planType = body.getOrDefault("planType", "MONTHLY");
        subscriptionService.activatePremium(user.getId(), provider, planType);
        return ResponseEntity.ok(ApiResponse.success("Premium activated"));
    }

    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @AuthenticationPrincipal FirebasePrincipal principal) {
        User user = authService.getCurrentUser(principal.uid());
        subscriptionService.cancelSubscription(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Subscription cancelled — remains active until expiry"));
    }

    @PostMapping("/downgrade")
    public ResponseEntity<ApiResponse<Void>> downgrade(
            @AuthenticationPrincipal FirebasePrincipal principal) {
        User user = authService.getCurrentUser(principal.uid());
        subscriptionService.downgradeToFree(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Downgraded to free tier"));
    }

    @GetMapping("/feature-access/{feature}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkFeatureAccess(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @PathVariable String feature) {
        User user = authService.getCurrentUser(principal.uid());
        PremiumFeature premiumFeature;
        try {
            premiumFeature = PremiumFeature.valueOf(feature);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid feature: " + feature));
        }
        boolean accessible = featureGateService.hasAccess(user.getId(), premiumFeature);
        return ResponseEntity.ok(ApiResponse.success(Map.of("feature", feature, "accessible", accessible)));
    }

    @GetMapping("/features")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getFeatures(
            @AuthenticationPrincipal FirebasePrincipal principal) {
        User user = authService.getCurrentUser(principal.uid());
        List<Map<String, Object>> features = Arrays.stream(PremiumFeature.values())
                .map(f -> Map.<String, Object>of(
                        "feature", f.name(),
                        "accessible", featureGateService.hasAccess(user.getId(), f)))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(features));
    }
}
