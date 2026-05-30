package com.ascend.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    private static final long DEBOUNCE_SECONDS = 30;
    private final ConcurrentHashMap<String, Instant> lastBroadcast = new ConcurrentHashMap<>();

    public void broadcastRankUpdate(String league, UUID userId, String username, long weeklyXp) {
        if (!shouldBroadcast(league)) {
            return;
        }

        Map<String, Object> entry = Map.of(
                "userId", userId.toString(),
                "username", username,
                "weeklyXp", weeklyXp);

        messagingTemplate.convertAndSend("/topic/leaderboard/" + league.toLowerCase(), entry);
        lastBroadcast.put(league, Instant.now());
        log.debug("Leaderboard broadcast: league={} user={}", league, userId);
    }

    private boolean shouldBroadcast(String league) {
        Instant last = lastBroadcast.get(league);
        if (last == null) return true;
        return Instant.now().isAfter(last.plusSeconds(DEBOUNCE_SECONDS));
    }
}
