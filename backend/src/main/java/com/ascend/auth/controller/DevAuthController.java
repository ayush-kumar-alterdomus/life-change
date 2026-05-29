package com.ascend.auth.controller;

import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.dto.LoginResponse;
import com.ascend.auth.dto.UserProfileResponse;
import com.ascend.auth.service.AuthService;
import com.ascend.common.dto.ApiResponse;
import com.ascend.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Dev-only auth controller active when firebase.enabled=false.
 * Returns the auto-authenticated dev user without token verification.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "false", matchIfMissing = true)
public class DevAuthController {

    private final AuthService authService;

    public DevAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login() {
        User user = authService.getCurrentUser("dev-user-uid");
        return ResponseEntity.ok(ApiResponse.success(buildLoginResponse(user)));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register() {
        User user = authService.getCurrentUser("dev-user-uid");
        return ResponseEntity.ok(ApiResponse.success(buildLoginResponse(user)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser(
            @AuthenticationPrincipal FirebasePrincipal principal) {
        User user = authService.getCurrentUser(principal.uid());
        UserProfileResponse profile = UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .level(user.getLevel())
                .xp(user.getXp())
                .league(user.getLeague())
                .premium(user.getPremium())
                .hardMode(user.getHardMode())
                .timezone(user.getTimezone())
                .createdAt(user.getCreatedAt())
                .build();
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    private LoginResponse buildLoginResponse(User user) {
        return LoginResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .level(user.getLevel())
                .xp(user.getXp())
                .league(user.getLeague())
                .premium(user.getPremium())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
