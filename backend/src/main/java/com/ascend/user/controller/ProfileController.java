package com.ascend.user.controller;

import com.ascend.analytics.entity.Achievement;
import com.ascend.analytics.repository.AchievementRepository;
import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.service.AuthService;
import com.ascend.common.dto.ApiResponse;
import com.ascend.common.exception.BusinessException;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import com.ascend.user.repository.UserStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final UserStatsRepository userStatsRepository;
    private final AchievementRepository achievementRepository;

    /**
     * GET /api/v1/profile/{userId} — public profile view.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPublicProfile(@PathVariable UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));

        if ("PRIVATE".equals(user.getPrivacyLevel())) {
            throw new BusinessException("PROFILE_PRIVATE", "This profile is private");
        }

        Map<String, Object> profile = new HashMap<>();
        profile.put("userId", user.getId());
        profile.put("username", user.getUsername());
        profile.put("avatarUrl", user.getAvatarUrl());
        profile.put("level", user.getLevel());
        profile.put("league", user.getLeague());
        profile.put("prestigeLevel", user.getPrestigeLevel());

        userStatsRepository.findByUserId(userId).ifPresent(stats -> {
            Map<String, Integer> statMap = Map.of(
                    "strength", stats.getStrength(),
                    "wisdom", stats.getWisdom(),
                    "focus", stats.getFocus(),
                    "discipline", stats.getDiscipline(),
                    "vitality", stats.getVitality(),
                    "charisma", stats.getCharisma()
            );
            profile.put("stats", statMap);
        });

        List<Achievement> achievements = achievementRepository.findByUserId(userId);
        profile.put("achievementCount", achievements.size());

        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    /**
     * PUT /api/v1/profile — update own profile.
     */
    @PutMapping
    public ResponseEntity<ApiResponse<Void>> updateProfile(
            @AuthenticationPrincipal FirebasePrincipal principal,
            @RequestBody Map<String, String> request) {

        User user = authService.getCurrentUser(principal.uid());

        if (request.containsKey("username")) {
            String username = request.get("username");
            if (username == null || username.isBlank() || username.length() > 50) {
                throw new BusinessException("VALIDATION_FAILED", "Username must be 1-50 characters");
            }
            user.setUsername(username);
        }
        if (request.containsKey("avatarUrl")) {
            user.setAvatarUrl(request.get("avatarUrl"));
        }
        if (request.containsKey("timezone")) {
            user.setTimezone(request.get("timezone"));
        }
        if (request.containsKey("privacyLevel")) {
            String privacy = request.get("privacyLevel");
            if (List.of("PUBLIC", "FRIENDS_ONLY", "PRIVATE").contains(privacy)) {
                user.setPrivacyLevel(privacy);
            }
        }

        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Profile updated"));
    }

    /**
     * GET /api/v1/profile/achievements — own achievements.
     */
    @GetMapping("/achievements")
    public ResponseEntity<ApiResponse<List<Achievement>>> getAchievements(
            @AuthenticationPrincipal FirebasePrincipal principal) {

        User user = authService.getCurrentUser(principal.uid());
        List<Achievement> achievements = achievementRepository.findByUserId(user.getId());
        return ResponseEntity.ok(ApiResponse.success(achievements));
    }
}
