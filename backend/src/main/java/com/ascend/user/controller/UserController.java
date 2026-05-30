package com.ascend.user.controller;

import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.service.AuthService;
import com.ascend.common.dto.ApiResponse;
import com.ascend.user.dto.OnboardingRequest;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final UserRepository userRepository;

    @PutMapping("/onboarding")
    public ResponseEntity<ApiResponse<Void>> completeOnboarding(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @Valid @RequestBody OnboardingRequest request) {

        User user = authService.getCurrentUser(principal.uid());

        user.setAvatarUrl(request.getSelectedAvatar());
        user.setHardMode("legendary".equalsIgnoreCase(request.getDifficulty()));
        userRepository.save(user);

        log.info("User {} completed onboarding: goals={}, difficulty={}, arc={}",
                user.getId(),
                request.getSelectedGoals(),
                request.getDifficulty().replaceAll("[\\r\\n]", "_"),
                request.getSelectedArc().replaceAll("[\\r\\n]", "_"));

        return ResponseEntity.ok(ApiResponse.success("Onboarding completed"));
    }
}
