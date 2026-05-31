package com.ascend.premium.controller;

import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.service.AuthService;
import com.ascend.common.dto.ApiResponse;
import com.ascend.premium.dto.SubscriptionStatusResponse;
import com.ascend.premium.service.SubscriptionService;
import com.ascend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/premium")
@RequiredArgsConstructor
public class PremiumController {

    private final SubscriptionService subscriptionService;
    private final AuthService authService;

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<SubscriptionStatusResponse>> getStatus(
            @AuthenticationPrincipal FirebasePrincipal principal) {

        User user = authService.getCurrentUser(principal.uid());
        SubscriptionStatusResponse status = subscriptionService.getSubscriptionStatus(user.getId());
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    @PostMapping("/trial")
    public ResponseEntity<ApiResponse<Void>> startTrial(
            @AuthenticationPrincipal FirebasePrincipal principal) {

        User user = authService.getCurrentUser(principal.uid());
        subscriptionService.startTrial(user.getId());
        return ResponseEntity.ok(ApiResponse.success("7-day free trial activated!"));
    }

    @PostMapping("/upgrade")
    public ResponseEntity<ApiResponse<Void>> upgrade(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @RequestBody Map<String, String> request) {

        User user = authService.getCurrentUser(principal.uid());
        String provider = request.getOrDefault("provider", "STRIPE");
        String planType = request.getOrDefault("planType", "MONTHLY");
        subscriptionService.activatePremium(user.getId(), provider, planType);
        return ResponseEntity.ok(ApiResponse.success("Premium activated!"));
    }

    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @AuthenticationPrincipal FirebasePrincipal principal) {

        User user = authService.getCurrentUser(principal.uid());
        subscriptionService.cancelSubscription(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Subscription cancelled. Active until expiry."));
    }

    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<Void>> handleWebhook(@RequestBody Map<String, Object> payload) {
        // Webhook handling for payment providers (Stripe/Razorpay)
        // In production, verify webhook signature before processing
        return ResponseEntity.ok(ApiResponse.success("Webhook received"));
    }
}
