package com.ascend.auth.service;

import com.ascend.streak.entity.Streak;
import com.ascend.streak.repository.StreakRepository;
import com.ascend.user.entity.User;
import com.ascend.user.entity.UserStats;
import com.ascend.user.repository.UserRepository;
import com.ascend.user.repository.UserStatsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service handling authentication-related user operations:
 * login/register flow, user lookup, and new user creation with defaults.
 */
@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserStatsRepository userStatsRepository;
    private final StreakRepository streakRepository;

    public AuthService(UserRepository userRepository,
                       UserStatsRepository userStatsRepository,
                       StreakRepository streakRepository) {
        this.userRepository = userRepository;
        this.userStatsRepository = userStatsRepository;
        this.streakRepository = streakRepository;
    }

    /**
     * Finds an existing user by Firebase UID, or creates a new one if not found.
     * Used during the login flow after Firebase token verification.
     */
    @Transactional
    public User loginOrRegister(String firebaseUid, String email, String provider) {
        Optional<User> existingUser = userRepository.findByFirebaseUid(firebaseUid);

        if (existingUser.isPresent()) {
            log.debug("Existing user found for uid={}", firebaseUid);
            return existingUser.get();
        }

        log.info("Creating new user for uid={} provider={}", firebaseUid, provider);
        return createUser(firebaseUid, email, generateUsername(email));
    }

    /**
     * Fetches a user by their Firebase UID.
     *
     * @throws com.ascend.common.exception.BusinessException if user not found
     */
    @Transactional(readOnly = true)
    public User getCurrentUser(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new com.ascend.common.exception.BusinessException(
                        "User not found for the authenticated account"));
    }

    /**
     * Creates a new user record with default values (level 1, xp 0, Bronze league),
     * along with initial Streak and UserStats records.
     */
    @Transactional
    public User createUser(String firebaseUid, String email, String username) {
        User user = User.builder()
                .firebaseUid(firebaseUid)
                .email(email)
                .username(username)
                .level(1)
                .xp(0L)
                .league("BRONZE")
                .premium(false)
                .hardMode(false)
                .timezone("UTC")
                .build();

        user = userRepository.save(user);
        log.info("Created user id={} username={}", user.getId(), username);

        // Create initial UserStats
        UserStats stats = UserStats.builder()
                .userId(user.getId())
                .build();
        userStatsRepository.save(stats);

        // Create initial Streak
        Streak streak = Streak.builder()
                .userId(user.getId())
                .build();
        streakRepository.save(streak);

        return user;
    }

    /**
     * Generates a username from email, or a random fallback if email is null.
     */
    private String generateUsername(String email) {
        if (email != null && email.contains("@")) {
            String base = email.substring(0, email.indexOf('@'));
            // Truncate to fit within 50 char limit, leaving room for suffix
            if (base.length() > 40) {
                base = base.substring(0, 40);
            }
            // Check uniqueness and append suffix if needed
            if (userRepository.findByUsername(base).isEmpty()) {
                return base;
            }
            return base + "_" + System.currentTimeMillis() % 10000;
        }
        return "user_" + System.currentTimeMillis() % 100000;
    }
}
