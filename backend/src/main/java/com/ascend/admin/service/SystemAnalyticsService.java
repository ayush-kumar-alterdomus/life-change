package com.ascend.admin.service;

import com.ascend.admin.dto.SystemAnalyticsResponse;
import com.ascend.streak.repository.StreakRepository;
import com.ascend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for system-wide analytics including DAU, WAU, MAU,
 * retention, premium conversion, churn, and streak survival rates.
 * Results are cached for 15 minutes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SystemAnalyticsService {

    private final UserRepository userRepository;
    private final StreakRepository streakRepository;

    @Cacheable(value = "systemAnalytics", key = "'current'")
    public SystemAnalyticsResponse getSystemAnalytics() {
        log.info("Calculating system analytics (cache miss)");

        long totalUsers = userRepository.count();
        if (totalUsers == 0) {
            return SystemAnalyticsResponse.builder().build();
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime weekAgo = now.minusDays(7);
        LocalDateTime monthAgo = now.minusDays(30);
        LocalDateTime twoWeeksAgo = now.minusDays(14);

        // DAU: distinct users with activity today
        long dau = userRepository.countByLastActiveAfter(todayStart);

        // WAU: distinct users active in last 7 days
        long wau = userRepository.countByLastActiveAfter(weekAgo);

        // MAU: distinct users active in last 30 days
        long mau = userRepository.countByLastActiveAfter(monthAgo);

        // Retention: users active this week who were also active last week
        double retentionRate = wau > 0 ? (double) wau / Math.max(mau, 1) : 0.0;

        // Premium conversion: premium users / total users
        long premiumCount = userRepository.countByPremiumTrue();
        double premiumConversion = (double) premiumCount / totalUsers;

        // Churn: users inactive > 14 days / total users
        long inactiveCount = totalUsers - userRepository.countByLastActiveAfter(twoWeeksAgo);
        double churnRate = (double) inactiveCount / totalUsers;

        // Streak survival: users with streak > 0 / total active users
        long usersWithStreak = streakRepository.countByCurrentStreakGreaterThan(0);
        long activeUsers = Math.max(wau, 1);
        double streakSurvivalRate = (double) usersWithStreak / activeUsers;

        return SystemAnalyticsResponse.builder()
                .dau(dau)
                .wau(wau)
                .mau(mau)
                .retentionRate(Math.min(retentionRate, 1.0))
                .premiumConversion(premiumConversion)
                .churnRate(churnRate)
                .streakSurvivalRate(Math.min(streakSurvivalRate, 1.0))
                .avgSessionLength(0.0) // Requires session tracking — placeholder
                .build();
    }
}
