package com.ascend.guild.service;

import com.ascend.guild.entity.Guild;
import com.ascend.guild.entity.GuildChallenge;
import com.ascend.guild.entity.GuildMember;
import com.ascend.guild.event.GuildChallengeCompleteEvent;
import com.ascend.guild.repository.GuildChallengeRepository;
import com.ascend.guild.repository.GuildMemberRepository;
import com.ascend.guild.repository.GuildRepository;
import com.ascend.notification.entity.NotificationLog;
import com.ascend.notification.repository.NotificationLogRepository;
import com.ascend.quest.event.QuestCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service handling guild challenge creation, contributions, and completion.
 * Listens for QuestCompletedEvent to auto-contribute to active guild challenges.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GuildChallengeService {

    private static final long CHALLENGE_COMPLETE_XP_REWARD = 500L;

    private final GuildChallengeRepository challengeRepository;
    private final GuildMemberRepository memberRepository;
    private final GuildRepository guildRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Creates a new guild challenge and notifies all guild members.
     *
     * @param guildId the guild to create the challenge for
     * @param title   the challenge title
     * @param target  the target progress value to complete the challenge
     * @param endsAt  when the challenge expires
     * @return the created GuildChallenge
     */
    @Transactional
    public GuildChallenge createChallenge(UUID guildId, String title, int target, LocalDateTime endsAt) {
        Guild guild = guildRepository.findById(guildId)
                .orElseThrow(() -> new IllegalArgumentException("Guild not found: " + guildId));

        GuildChallenge challenge = GuildChallenge.builder()
                .guildId(guildId)
                .title(title)
                .target(target)
                .currentProgress(0)
                .endsAt(endsAt)
                .build();

        GuildChallenge saved = challengeRepository.save(challenge);

        // Notify all guild members about the new challenge
        notifyGuildMembers(guildId, guild.getName(), title);

        log.info("Created guild challenge '{}' for guild {} with target {}", title, guildId, target);
        return saved;
    }

    /**
     * Contributes progress to a guild challenge. If the challenge reaches its target,
     * it is marked as complete, guild XP is awarded, and a GuildChallengeCompleteEvent is published.
     *
     * @param userId       the user making the contribution
     * @param challengeId  the challenge to contribute to
     * @param contribution the amount to contribute
     */
    @Transactional
    public void contributeToChallenge(UUID userId, UUID challengeId, int contribution) {
        GuildChallenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found: " + challengeId));

        // Verify challenge is still active
        if (challenge.getEndsAt() != null && challenge.getEndsAt().isBefore(LocalDateTime.now())) {
            log.warn("Attempted contribution to expired challenge {}", challengeId);
            return;
        }

        // Already completed
        if (challenge.getCurrentProgress() >= challenge.getTarget()) {
            log.warn("Attempted contribution to already completed challenge {}", challengeId);
            return;
        }

        // Increment progress
        int newProgress = challenge.getCurrentProgress() + contribution;
        challenge.setCurrentProgress(newProgress);
        challengeRepository.save(challenge);

        log.debug("User {} contributed {} to challenge {}. Progress: {}/{}",
                userId, contribution, challengeId, newProgress, challenge.getTarget());

        // Check if challenge is now complete
        if (newProgress >= challenge.getTarget()) {
            completeChallenge(challenge);
        }
    }

    /**
     * Listens for QuestCompletedEvent and auto-contributes to all active guild challenges
     * for guilds the user belongs to.
     */
    @EventListener
    @Transactional
    public void onQuestCompleted(QuestCompletedEvent event) {
        UUID userId = event.getUserId();

        List<GuildChallenge> activeChallenges = challengeRepository
                .findActiveChallengesForUser(userId, LocalDateTime.now());

        if (activeChallenges.isEmpty()) {
            return;
        }

        // Each quest completion contributes 1 point to all active guild challenges
        for (GuildChallenge challenge : activeChallenges) {
            contributeToChallenge(userId, challenge.getId(), 1);
        }

        log.info("User {} quest completion auto-contributed to {} active guild challenges",
                userId, activeChallenges.size());
    }

    /**
     * Completes a challenge: awards guild XP and publishes the completion event.
     */
    private void completeChallenge(GuildChallenge challenge) {
        // Award guild XP
        Guild guild = guildRepository.findById(challenge.getGuildId())
                .orElseThrow(() -> new IllegalArgumentException("Guild not found: " + challenge.getGuildId()));

        guild.setGuildXp(guild.getGuildXp() + CHALLENGE_COMPLETE_XP_REWARD);
        guildRepository.save(guild);

        // Publish completion event
        eventPublisher.publishEvent(new GuildChallengeCompleteEvent(
                this,
                challenge.getId(),
                challenge.getGuildId(),
                challenge.getTitle(),
                challenge.getTarget(),
                CHALLENGE_COMPLETE_XP_REWARD
        ));

        log.info("Guild challenge '{}' completed! Awarded {} XP to guild {}",
                challenge.getTitle(), CHALLENGE_COMPLETE_XP_REWARD, challenge.getGuildId());
    }

    /**
     * Sends a notification to all members of a guild about a new challenge.
     */
    private void notifyGuildMembers(UUID guildId, String guildName, String challengeTitle) {
        List<GuildMember> members = memberRepository.findByGuildId(guildId);

        for (GuildMember member : members) {
            NotificationLog notification = NotificationLog.builder()
                    .userId(member.getUserId())
                    .type("GUILD_CHALLENGE")
                    .title("New Guild Challenge!")
                    .message(String.format("A new challenge '%s' has been created in %s. Join the effort!",
                            challengeTitle, guildName))
                    .build();

            notificationLogRepository.save(notification);
        }

        log.debug("Notified {} guild members about new challenge '{}'", members.size(), challengeTitle);
    }
}
