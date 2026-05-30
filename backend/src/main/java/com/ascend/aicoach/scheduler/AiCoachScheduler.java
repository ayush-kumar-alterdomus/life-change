package com.ascend.aicoach.scheduler;

import com.ascend.aicoach.service.BurnoutDetectionService;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiCoachScheduler {

    private final UserRepository userRepository;
    private final BurnoutDetectionService burnoutDetectionService;

    @Scheduled(cron = "0 30 3 * * *")
    public void dailyEvaluation() {
        log.info("Starting AI coach daily evaluation");

        List<User> premiumUsers = userRepository.findAll().stream()
                .filter(u -> Boolean.TRUE.equals(u.getPremium()))
                .toList();

        int evaluated = 0;
        for (User user : premiumUsers) {
            try {
                burnoutDetectionService.evaluateAndAct(user.getId());
                evaluated++;
            } catch (Exception e) {
                log.error("AI coach evaluation failed for user={}: {}", user.getId(), e.getMessage());
            }
        }

        log.info("AI coach evaluation complete: evaluated={} total_premium={}", evaluated, premiumUsers.size());
    }
}
