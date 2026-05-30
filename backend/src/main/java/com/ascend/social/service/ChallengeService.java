package com.ascend.social.service;

import com.ascend.common.exception.BusinessException;
import com.ascend.notification.dto.NotificationType;
import com.ascend.notification.service.NotificationService;
import com.ascend.quest.event.QuestCompletedEvent;
import com.ascend.social.dto.ChallengeResponse;
import com.ascend.social.dto.CreateChallengeRequest;
import com.ascend.social.dto.FriendResponse;
import com.ascend.social.model.Challenge;
import com.ascend.social.repository.ChallengeRepository;
import com.ascend.social.repository.FriendshipRepository;
import com.ascend.streak.entity.Streak;
import com.ascend.streak.repository.StreakRepository;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final StreakRepository streakRepository;
    private final NotificationService notificationService;

    @Transactional
    public ChallengeResponse createChallenge(UUID challengerId, CreateChallengeRequest request) {
        // Verify friendship
        friendshipRepository.findFriendshipBetween(challengerId, request.getFriendId())
                .filter(f -> "ACCEPTED".equals(f.getStatus()))
                .orElseThrow(() -> new BusinessException("NOT_FRIENDS", "You must be friends to create a challenge"));

        User challenger = userRepository.findById(challengerId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));
        User challenged = userRepository.findById(request.getFriendId())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Friend not found"));

        Challenge challenge = Challenge.builder()
                .challenger(challenger)
                .challenged(challenged)
                .title(request.getTitle())
                .target(request.getTarget())
                .endsAt(request.getEndsAt())
                .build();

        challenge = challengeRepository.save(challenge);

        notificationService.sendNotification(request.getFriendId(), NotificationType.QUEST_REMINDER,
                "New Challenge! ⚔️",
                String.format("%s challenged you: %s", challenger.getUsername(), request.getTitle()));

        log.info("Challenge created: id={} challenger={} challenged={}", challenge.getId(), challengerId, request.getFriendId());
        return toResponse(challenge, challengerId);
    }

    @Transactional
    public void contributeToChallenge(UUID userId, UUID challengeId, int progress) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new BusinessException("CHALLENGE_NOT_FOUND", "Challenge not found"));

        if (!"ACTIVE".equals(challenge.getStatus())) return;

        if (challenge.getChallenger().getId().equals(userId)) {
            challenge.setChallengerProgress(challenge.getChallengerProgress() + progress);
            if (challenge.getChallengerProgress() >= challenge.getTarget()) {
                challenge.setStatus("COMPLETED");
                challenge.setWinner(challenge.getChallenger());
            }
        } else if (challenge.getChallenged().getId().equals(userId)) {
            challenge.setChallengedProgress(challenge.getChallengedProgress() + progress);
            if (challenge.getChallengedProgress() >= challenge.getTarget()) {
                challenge.setStatus("COMPLETED");
                challenge.setWinner(challenge.getChallenged());
            }
        }

        challengeRepository.save(challenge);
    }

    @Transactional(readOnly = true)
    public List<ChallengeResponse> getActiveChallenges(UUID userId) {
        return challengeRepository.findActiveChallenges(userId).stream()
                .map(c -> toResponse(c, userId))
                .toList();
    }

    @EventListener
    public void onQuestCompleted(QuestCompletedEvent event) {
        List<Challenge> active = challengeRepository.findActiveChallenges(event.getUserId());
        for (Challenge challenge : active) {
            contributeToChallenge(event.getUserId(), challenge.getId(), 1);
        }
    }

    private ChallengeResponse toResponse(Challenge c, UUID currentUserId) {
        boolean isChallenger = c.getChallenger().getId().equals(currentUserId);
        User opponent = isChallenger ? c.getChallenged() : c.getChallenger();
        int myProgress = isChallenger ? c.getChallengerProgress() : c.getChallengedProgress();
        int opponentProgress = isChallenger ? c.getChallengedProgress() : c.getChallengerProgress();

        int streak = streakRepository.findByUserId(opponent.getId())
                .map(Streak::getCurrentStreak).orElse(0);

        FriendResponse opponentResponse = new FriendResponse(
                opponent.getId(), opponent.getUsername(), opponent.getAvatarUrl(),
                opponent.getLevel(), streak, "ACCEPTED");

        String winner = c.getWinner() != null ? c.getWinner().getUsername() : null;

        return new ChallengeResponse(c.getId(), opponentResponse, c.getTitle(),
                c.getTarget(), myProgress, opponentProgress, c.getStatus(), winner);
    }
}
