package com.ascend.social.service;

import com.ascend.common.exception.BusinessException;
import com.ascend.social.dto.FriendResponse;
import com.ascend.social.model.Friendship;
import com.ascend.social.repository.FriendshipRepository;
import com.ascend.streak.entity.Streak;
import com.ascend.streak.repository.StreakRepository;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service handling friend requests, acceptance, removal, blocking,
 * and retrieval of friends and pending requests.
 */
@Slf4j
@Service
public class FriendService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_ACCEPTED = "ACCEPTED";
    private static final String STATUS_BLOCKED = "BLOCKED";

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final StreakRepository streakRepository;

    public FriendService(FriendshipRepository friendshipRepository,
                         UserRepository userRepository,
                         StreakRepository streakRepository) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
        this.streakRepository = streakRepository;
    }

    /**
     * Sends a friend request from userId to friendId.
     * Creates a PENDING friendship record.
     *
     * @param userId   the sender's user ID
     * @param friendId the target user's ID
     * @throws BusinessException if users are the same, friend not found,
     *                           or a friendship already exists
     */
    @Transactional
    public void sendFriendRequest(UUID userId, UUID friendId) {
        log.info("User {} sending friend request to {}", userId, friendId);

        if (userId.equals(friendId)) {
            throw new BusinessException("INVALID_REQUEST", "Cannot send a friend request to yourself");
        }

        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND",
                        "User not found with id: " + friendId));

        // Check if any friendship already exists between these users
        friendshipRepository.findFriendshipBetween(userId, friendId)
                .ifPresent(existing -> {
                    if (STATUS_BLOCKED.equals(existing.getStatus())) {
                        throw new BusinessException("USER_BLOCKED",
                                "Cannot send friend request — user is blocked");
                    }
                    throw new IllegalStateException(
                            "A friendship already exists between these users with status: " + existing.getStatus());
                });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND",
                        "User not found with id: " + userId));

        Friendship friendship = Friendship.builder()
                .user(user)
                .friend(friend)
                .status(STATUS_PENDING)
                .build();

        friendshipRepository.save(friendship);
        log.info("Friend request sent from {} to {}", userId, friendId);
    }

    /**
     * Accepts a pending friend request. The userId is the recipient accepting
     * the request from friendId.
     *
     * @param userId   the recipient accepting the request
     * @param friendId the sender of the original request
     * @throws BusinessException if no pending request exists
     */
    @Transactional
    public void acceptFriendRequest(UUID userId, UUID friendId) {
        log.info("User {} accepting friend request from {}", userId, friendId);

        // The pending request was created with friendId as user and userId as friend
        Friendship friendship = friendshipRepository.findByUserIdAndFriendId(friendId, userId)
                .orElseThrow(() -> new BusinessException("REQUEST_NOT_FOUND",
                        "No pending friend request found from user: " + friendId));

        if (!STATUS_PENDING.equals(friendship.getStatus())) {
            throw new IllegalStateException(
                    "Friend request is not in PENDING state, current status: " + friendship.getStatus());
        }

        friendship.setStatus(STATUS_ACCEPTED);
        friendshipRepository.save(friendship);
        log.info("Friend request accepted between {} and {}", userId, friendId);
    }

    /**
     * Removes a friendship between two users (regardless of direction).
     *
     * @param userId   one user in the friendship
     * @param friendId the other user in the friendship
     * @throws BusinessException if no friendship exists
     */
    @Transactional
    public void removeFriend(UUID userId, UUID friendId) {
        log.info("User {} removing friend {}", userId, friendId);

        Friendship friendship = friendshipRepository.findFriendshipBetween(userId, friendId)
                .orElseThrow(() -> new BusinessException("FRIENDSHIP_NOT_FOUND",
                        "No friendship found between users: " + userId + " and " + friendId));

        friendshipRepository.delete(friendship);
        log.info("Friendship removed between {} and {}", userId, friendId);
    }

    /**
     * Blocks a user. If a friendship exists, updates it to BLOCKED.
     * If no friendship exists, creates a BLOCKED record.
     *
     * @param userId    the user performing the block
     * @param blockedId the user being blocked
     * @throws BusinessException if blocking yourself or user not found
     */
    @Transactional
    public void blockUser(UUID userId, UUID blockedId) {
        log.info("User {} blocking user {}", userId, blockedId);

        if (userId.equals(blockedId)) {
            throw new BusinessException("INVALID_REQUEST", "Cannot block yourself");
        }

        User blocked = userRepository.findById(blockedId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND",
                        "User not found with id: " + blockedId));

        // Check if a friendship already exists in either direction
        Friendship friendship = friendshipRepository.findFriendshipBetween(userId, blockedId)
                .orElse(null);

        if (friendship != null) {
            friendship.setStatus(STATUS_BLOCKED);
            // Ensure the blocker is the "user" side for clarity
            if (!friendship.getUser().getId().equals(userId)) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new BusinessException("USER_NOT_FOUND",
                                "User not found with id: " + userId));
                friendshipRepository.delete(friendship);
                Friendship blockedFriendship = Friendship.builder()
                        .user(user)
                        .friend(blocked)
                        .status(STATUS_BLOCKED)
                        .build();
                friendshipRepository.save(blockedFriendship);
            } else {
                friendshipRepository.save(friendship);
            }
        } else {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException("USER_NOT_FOUND",
                            "User not found with id: " + userId));
            Friendship blockedFriendship = Friendship.builder()
                    .user(user)
                    .friend(blocked)
                    .status(STATUS_BLOCKED)
                    .build();
            friendshipRepository.save(blockedFriendship);
        }

        log.info("User {} blocked user {}", userId, blockedId);
    }

    /**
     * Returns a list of accepted friends for the given user.
     *
     * @param userId the user whose friends to retrieve
     * @return list of friend responses
     */
    @Transactional(readOnly = true)
    public List<FriendResponse> getFriends(UUID userId) {
        log.debug("Fetching friends for user={}", userId);

        List<Friendship> friendships = friendshipRepository.findAcceptedFriendships(userId);

        return friendships.stream()
                .map(friendship -> {
                    User friendUser = friendship.getUser().getId().equals(userId)
                            ? friendship.getFriend()
                            : friendship.getUser();
                    return toFriendResponse(friendUser, STATUS_ACCEPTED);
                })
                .collect(Collectors.toList());
    }

    /**
     * Returns a list of pending incoming friend requests for the given user.
     *
     * @param userId the user whose pending requests to retrieve
     * @return list of friend responses with PENDING status
     */
    @Transactional(readOnly = true)
    public List<FriendResponse> getPendingRequests(UUID userId) {
        log.debug("Fetching pending friend requests for user={}", userId);

        List<Friendship> pendingRequests = friendshipRepository.findPendingRequestsForUser(userId);

        return pendingRequests.stream()
                .map(friendship -> toFriendResponse(friendship.getUser(), STATUS_PENDING))
                .collect(Collectors.toList());
    }

    /**
     * Saves an updated user entity.
     */
    @Transactional
    public void updateUser(User user) {
        userRepository.save(user);
    }

    /**
     * Maps a User entity to a FriendResponse DTO, including streak information.
     */
    private FriendResponse toFriendResponse(User user, String status) {
        int streak = streakRepository.findByUserId(user.getId())
                .map(Streak::getCurrentStreak)
                .orElse(0);

        return new FriendResponse(
                user.getId(),
                user.getUsername(),
                user.getAvatarUrl(),
                user.getLevel(),
                streak,
                status
        );
    }
}
