package com.ascend.admin.service;

import com.ascend.admin.dto.AdminUserResponse;
import com.ascend.admin.dto.ModerationRequest;
import com.ascend.league.repository.LeaderboardRepository;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for user moderation actions including warn, suspend, ban,
 * leaderboard restriction, and unflagging.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModerationService {

    private final UserRepository userRepository;
    private final LeaderboardRepository leaderboardRepository;

    @Transactional
    public void moderateUser(UUID adminId, ModerationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getUserId()));

        switch (request.getAction()) {
            case WARN -> {
                log.info("Admin {} warned user {} — reason: {}", adminId, user.getId(), request.getReason());
                // Notification would be sent here
            }
            case SUSPEND -> {
                user.setSuspended(true);
                user.setSuspendedUntil(LocalDateTime.now().plusHours(
                        request.getDuration() != null ? request.getDuration() : 24));
                userRepository.save(user);
                log.info("Admin {} suspended user {} for {} hours", adminId, user.getId(), request.getDuration());
            }
            case BAN -> {
                user.setBanned(true);
                userRepository.save(user);
                log.info("Admin {} banned user {}", adminId, user.getId());
            }
            case LEADERBOARD_RESTRICT -> {
                leaderboardRepository.findByUserId(user.getId())
                        .ifPresent(leaderboardRepository::delete);
                log.info("Admin {} restricted user {} from leaderboard", adminId, user.getId());
            }
            case UNFLAG -> {
                user.setFlagged(false);
                userRepository.save(user);
                log.info("Admin {} unflagged user {}", adminId, user.getId());
            }
        }
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> getFlaggedUsers(int page) {
        Page<User> flaggedUsers = userRepository.findByFlaggedTrue(PageRequest.of(page, 20));
        return flaggedUsers.stream()
                .map(this::mapToAdminUserResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getUserDetail(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return mapToAdminUserResponse(user);
    }

    private AdminUserResponse mapToAdminUserResponse(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .level(user.getLevel())
                .premium(Boolean.TRUE.equals(user.getPremium()))
                .role(user.getRole() != null ? user.getRole() : "USER")
                .createdAt(user.getCreatedAt())
                .lastActive(user.getLastActive())
                .flagged(Boolean.TRUE.equals(user.getFlagged()))
                .banned(Boolean.TRUE.equals(user.getBanned()))
                .build();
    }
}
