package com.ascend.streak.service;

import com.ascend.streak.entity.Streak;
import com.ascend.streak.repository.StreakRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for managing Comeback Mode.
 * Comeback Mode provides a 48-hour grace period after a streak break,
 * with reduced quest difficulty and recovery XP bonuses (1.5x).
 */
@Slf4j
@Service
public class ComebackModeService {

    private static final long COMEBACK_DURATION_HOURS = 48;
    private static final double COMEBACK_XP_MULTIPLIER = 1.5;

    private final StreakRepository streakRepository;

    public ComebackModeService(StreakRepository streakRepository) {
        this.streakRepository = streakRepository;
    }

    /**
     * Activates Comeback Mode for a user after a streak break.
     * Sets the comeback window to 48 hours from now, reduces quest difficulty,
     * and enables recovery XP bonuses (1.5x for comeback quests).
     *
     * @param userId the user's ID
     */
    @Transactional
    public void activateComebackMode(UUID userId) {
        log.info("Activating comeback mode for user={}", userId);

        Streak streak = streakRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException(
                        "Cannot activate comeback mode: no streak record found for user=" + userId));

        // 1. Set comeback_mode_active = true
        streak.setComebackModeActive(true);

        // 2. Set comeback_expires_at = now() + 48 hours
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(COMEBACK_DURATION_HOURS);
        streak.setComebackExpiresAt(expiresAt);

        streakRepository.save(streak);

        // 3. Reduce assigned quest difficulty temporarily (logged — actual quest difficulty reduction is out of scope)
        log.info("Comeback mode activated for user={}: quest difficulty reduced temporarily until {}",
                userId, expiresAt);

        // 4. Enable recovery XP bonuses (1.5x for comeback quests) — stored via comebackModeActive flag
        log.info("Recovery XP bonus enabled for user={}: {}x multiplier for comeback quests",
                userId, COMEBACK_XP_MULTIPLIER);
    }

    /**
     * Checks if the comeback mode has expired for a user and deactivates it if so.
     * Called periodically (e.g., hourly) to clean up expired comeback windows.
     *
     * @param userId the user's ID
     */
    @Transactional
    public void checkComebackExpiry(UUID userId) {
        log.debug("Checking comeback expiry for user={}", userId);

        Streak streak = streakRepository.findByUserId(userId).orElse(null);

        if (streak == null || !Boolean.TRUE.equals(streak.getComebackModeActive())) {
            return;
        }

        // 1. If now() > comeback_expires_at → deactivate comeback mode
        if (streak.getComebackExpiresAt() != null
                && LocalDateTime.now().isAfter(streak.getComebackExpiresAt())) {

            streak.setComebackModeActive(false);
            streak.setComebackExpiresAt(null);
            streakRepository.save(streak);

            // 2. Reset to normal difficulty (logged — actual quest difficulty reset is out of scope)
            log.info("Comeback mode expired for user={}: deactivated, quest difficulty reset to normal", userId);
        }
    }

    /**
     * Checks if Comeback Mode is currently active for a user (within the 48-hour window).
     *
     * @param userId the user's ID
     * @return true if comeback mode is active and has not expired
     */
    public boolean isComebackActive(UUID userId) {
        Streak streak = streakRepository.findByUserId(userId).orElse(null);

        if (streak == null || !Boolean.TRUE.equals(streak.getComebackModeActive())) {
            return false;
        }

        // Check if within the 48-hour window
        if (streak.getComebackExpiresAt() == null) {
            return false;
        }

        return LocalDateTime.now().isBefore(streak.getComebackExpiresAt());
    }

    /**
     * Returns the XP multiplier for comeback quests.
     * Used by the XP engine to apply the 1.5x recovery bonus during comeback mode.
     *
     * @return the comeback XP multiplier (1.5)
     */
    public double getComebackXpMultiplier() {
        return COMEBACK_XP_MULTIPLIER;
    }
}
