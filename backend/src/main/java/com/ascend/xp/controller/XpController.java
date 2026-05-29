package com.ascend.xp.controller;

import com.ascend.auth.config.FirebasePrincipal;
import com.ascend.auth.service.AuthService;
import com.ascend.common.dto.ApiResponse;
import com.ascend.streak.entity.Streak;
import com.ascend.streak.repository.StreakRepository;
import com.ascend.user.entity.User;
import com.ascend.xp.dto.XpHistoryResponse;
import com.ascend.xp.dto.XpSummaryResponse;
import com.ascend.xp.entity.XpHistory;
import com.ascend.xp.repository.XpHistoryRepository;
import com.ascend.xp.service.ComboCalculator;
import com.ascend.xp.service.LevelCalculator;
import com.ascend.xp.service.XpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for XP-related endpoints.
 * Provides XP summary and history for the authenticated user.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/xp")
@RequiredArgsConstructor
public class XpController {

    private final AuthService authService;
    private final XpService xpService;
    private final XpHistoryRepository xpHistoryRepository;
    private final StreakRepository streakRepository;

    /**
     * GET /api/v1/xp/summary
     * Returns XP summary for the authenticated user including level, daily progress,
     * prestige info, and current combo multiplier.
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<XpSummaryResponse>> getXpSummary(
            @AuthenticationPrincipal FirebasePrincipal principal) {

        User user = authService.getCurrentUser(principal.uid());

        long dailyXpEarned = xpService.getDailyXpEarned(user.getId());
        int dailyCap = xpService.getDailyCap(user.getLevel());
        long xpToNextLevel = LevelCalculator.xpToNextLevel(user.getLevel(), user.getXp());

        double comboMultiplier = streakRepository.findByUserId(user.getId())
                .map(Streak::getCurrentStreak)
                .map(ComboCalculator::calculateComboMultiplier)
                .orElse(1.0);

        XpSummaryResponse response = XpSummaryResponse.builder()
                .totalXp(user.getXp())
                .level(user.getLevel())
                .xpToNextLevel(xpToNextLevel)
                .dailyXpEarned(dailyXpEarned)
                .dailyCap(dailyCap)
                .prestigeLevel(user.getPrestigeLevel())
                .comboMultiplier(comboMultiplier)
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * GET /api/v1/xp/history
     * Returns paginated XP history for the authenticated user.
     * Supports standard Spring Data pagination parameters (page, size, sort).
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<XpHistoryResponse>> getXpHistory(
            @AuthenticationPrincipal FirebasePrincipal principal,
            Pageable pageable) {

        User user = authService.getCurrentUser(principal.uid());

        Page<XpHistory> historyPage = xpHistoryRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);

        List<XpHistoryResponse.XpTransaction> transactions = historyPage.getContent().stream()
                .map(entry -> XpHistoryResponse.XpTransaction.builder()
                        .source(entry.getSourceType())
                        .amount(entry.getXpAmount())
                        .multiplier(entry.getMultiplier())
                        .stat(entry.getStatType())
                        .timestamp(entry.getCreatedAt())
                        .build())
                .toList();

        XpHistoryResponse response = XpHistoryResponse.builder()
                .transactions(transactions)
                .page(historyPage.getNumber())
                .size(historyPage.getSize())
                .totalElements(historyPage.getTotalElements())
                .totalPages(historyPage.getTotalPages())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
