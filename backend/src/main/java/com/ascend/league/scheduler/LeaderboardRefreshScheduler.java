package com.ascend.league.scheduler;

import com.ascend.league.entity.LeagueTier;
import com.ascend.league.entity.Leaderboard;
import com.ascend.league.repository.LeaderboardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Refreshes leaderboard data every 5 minutes and broadcasts updates
 * to WebSocket subscribers on /topic/leaderboard/{league}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LeaderboardRefreshScheduler {

    private final LeaderboardRepository leaderboardRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Scheduled(fixedRate = 300_000) // every 5 minutes
    public void refreshLeaderboards() {
        log.debug("Leaderboard refresh triggered");

        for (LeagueTier tier : LeagueTier.values()) {
            if (tier == LeagueTier.ASCENDANT) continue;

            try {
                List<Leaderboard> topUsers = leaderboardRepository
                        .findByLeagueOrderByWeeklyXpDesc(tier.name(), PageRequest.of(0, 10))
                        .getContent();

                if (topUsers.isEmpty()) continue;

                List<Map<String, Object>> entries = topUsers.stream()
                        .map(entry -> Map.<String, Object>of(
                                "userId", entry.getUserId().toString(),
                                "weeklyXp", entry.getWeeklyXp(),
                                "league", tier.name()
                        ))
                        .toList();

                Map<String, Object> payload = Map.of(
                        "type", "LEADERBOARD_UPDATE",
                        "data", Map.of(
                                "league", tier.name(),
                                "topUsers", entries,
                                "updatedAt", LocalDateTime.now().toString()
                        )
                );

                messagingTemplate.convertAndSend("/topic/leaderboard/" + tier.name(), payload);
            } catch (Exception e) {
                log.error("Failed to refresh leaderboard for tier {}: {}", tier, e.getMessage());
            }
        }

        log.debug("Leaderboard refresh completed");
    }
}
