package com.ascend.user.controller;

import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.service.AuthService;
import com.ascend.common.dto.ApiResponse;
import com.ascend.user.dto.OnboardingRequest;
import com.ascend.user.dto.OnboardingResponse;
import com.ascend.user.dto.OnboardingStatusResponse;
import com.ascend.user.entity.User;
import com.ascend.user.service.OnboardingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final OnboardingService onboardingService;

    @PutMapping("/onboarding")
    public ResponseEntity<ApiResponse<OnboardingResponse>> completeOnboarding(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @Valid @RequestBody OnboardingRequest request) {

        User user = authService.getCurrentUser(principal.uid());
        OnboardingResponse response = onboardingService.completeOnboarding(user.getId(), request);

        return ResponseEntity.ok(ApiResponse.success("Onboarding completed", response));
    }

    @GetMapping("/me/onboarding-status")
    public ResponseEntity<ApiResponse<OnboardingStatusResponse>> getOnboardingStatus(
            @AuthenticationPrincipal FirebasePrincipal principal) {

        User user = authService.getCurrentUser(principal.uid());
        OnboardingStatusResponse response = onboardingService.getOnboardingStatus(user.getId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me/summary")
    public ResponseEntity<ApiResponse<User>> getUserSummary(
            @AuthenticationPrincipal FirebasePrincipal principal) {

        User user = authService.getCurrentUser(principal.uid());
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
