package com.ascend.league.event;

import com.ascend.league.entity.Leaderboard;
import com.ascend.league.repository.LeaderboardRepository;
import com.ascend.xp.event.XpAwardedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

/**
 * Listens for XP award events and updates the leaderboard accordingly.
 * Handles weekly XP accumulation, rank recalculation deferral, and Redis cache invalidation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LeagueEventListener {

    private static final String LEADERBOARD_CACHE_PREFIX = "leaderboard:";

    private final LeaderboardRepository leaderboardRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Handles XpAwardedEvent by updating the user's weekly XP in the leaderboard table,
     * recalculating their rank within the league, and invalidating the Redis leaderboard cache.
     *
     * @param event the XP awarded event containing userId and xpAmount
     */
    @EventListener
    @Transactional
    public void handleXpAwarded(XpAwardedEvent event) {
        UUID userId = event.getUserId();
        int xpAmount = event.getXpAmount();

        try {
            log.debug("Processing leaderboard update for user {} with {} XP", userId, xpAmount);

            // Update or create leaderboard entry with accumulated weekly XP
            Leaderboard entry = leaderboardRepository.findByUserId(userId)
                    .orElseGet(() -> createNewLeaderboardEntry(userId));

            entry.setWeeklyXp(entry.getWeeklyXp() + xpAmount);
            leaderboardRepository.save(entry);

            // Invalidate Redis leaderboard cache for the user's league
            invalidateLeaderboardCache(entry.getLeague());

            log.debug("Leaderboard updated for user {} — weekly XP now {}", userId, entry.getWeeklyXp());
        } catch (Exception e) {
            log.error("Failed to update leaderboard for user {} after XP award: {}",
                    userId, e.getMessage(), e);
        }
    }

    /**
     * Creates a new leaderboard entry for a user who doesn't have one yet.
     */
    private Leaderboard createNewLeaderboardEntry(UUID userId) {
        return Leaderboard.builder()
                .userId(userId)
                .weeklyXp(0L)
                .league("BRONZE")
                .build();
    }

    /**
     * Invalidates all Redis cache keys for the given league's leaderboard.
     * Uses pattern-based key deletion to clear all paginated cache entries.
     */
    private void invalidateLeaderboardCache(String league) {
        try {
            String pattern = LEADERBOARD_CACHE_PREFIX + league.toLowerCase() + ":*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("Invalidated {} leaderboard cache entries for league {}", keys.size(), league);
            }
        } catch (Exception e) {
            log.warn("Failed to invalidate leaderboard cache for league {}: {}",
                    league, e.getMessage());
        }
    }
}
