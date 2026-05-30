package com.ascend.guild.service;

import com.ascend.common.exception.BusinessException;
import com.ascend.guild.dto.GuildChatMessage;
import com.ascend.guild.repository.GuildMemberRepository;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handles guild chat operations including message validation, rate limiting,
 * broadcasting via STOMP, and persistence to Firestore.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GuildChatHandler {

    private static final int MAX_MESSAGE_LENGTH = 500;
    private static final int RATE_LIMIT_PER_MINUTE = 30;

    private final SimpMessagingTemplate messagingTemplate;
    private final GuildMemberRepository guildMemberRepository;
    private final UserRepository userRepository;
    private final Firestore firestore;

    // Rate limiting: userId -> RateLimitEntry
    private final ConcurrentHashMap<UUID, RateLimitEntry> rateLimitMap = new ConcurrentHashMap<>();

    /**
     * Sends a chat message to a guild channel.
     * Verifies membership, validates the message, enforces rate limits,
     * broadcasts to subscribers, and persists to Firestore.
     *
     * @param userId  the ID of the user sending the message
     * @param guildId the ID of the guild to send the message to
     * @param message the message content
     * @return the constructed GuildChatMessage that was broadcast
     */
    public GuildChatMessage sendMessage(UUID userId, UUID guildId, String message) {
        // 1. Verify user is guild member
        verifyMembership(userId, guildId);

        // 2. Validate message (max 500 chars, no empty)
        validateMessage(message);

        // 3. Enforce rate limit: 30 messages/minute per user
        enforceRateLimit(userId);

        // Resolve username
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));

        // Build chat message
        GuildChatMessage chatMessage = GuildChatMessage.builder()
                .id(UUID.randomUUID())
                .guildId(guildId)
                .userId(userId)
                .username(user.getUsername())
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        // 4. Broadcast to /topic/guild/{guildId}/chat
        String destination = "/topic/guild/" + guildId + "/chat";
        messagingTemplate.convertAndSend(destination, chatMessage);
        log.debug("Chat message broadcast to {} by user {}", destination, userId);

        // 5. Persist to guild_chat collection in Firestore (for history)
        persistToFirestore(chatMessage);

        return chatMessage;
    }

    private void verifyMembership(UUID userId, UUID guildId) {
        boolean isMember = guildMemberRepository.existsByGuildIdAndUserId(guildId, userId);
        if (!isMember) {
            throw new BusinessException("NOT_GUILD_MEMBER",
                    "User is not a member of this guild");
        }
    }

    private void validateMessage(String message) {
        if (message == null || message.isBlank()) {
            throw new BusinessException("INVALID_MESSAGE",
                    "Message cannot be empty");
        }
        if (message.length() > MAX_MESSAGE_LENGTH) {
            throw new BusinessException("INVALID_MESSAGE",
                    "Message exceeds maximum length of " + MAX_MESSAGE_LENGTH + " characters");
        }
    }

    private void enforceRateLimit(UUID userId) {
        long now = System.currentTimeMillis();
        RateLimitEntry entry = rateLimitMap.compute(userId, (key, existing) -> {
            if (existing == null || now - existing.windowStart > 60_000) {
                // Start a new window
                return new RateLimitEntry(now, new AtomicInteger(0));
            }
            return existing;
        });

        int count = entry.count.incrementAndGet();
        if (count > RATE_LIMIT_PER_MINUTE) {
            throw new BusinessException("RATE_LIMIT_EXCEEDED",
                    "Rate limit exceeded. Maximum " + RATE_LIMIT_PER_MINUTE + " messages per minute.");
        }
    }

    @SuppressWarnings("null")
    private void persistToFirestore(GuildChatMessage chatMessage) {
        try {
            Map<String, Object> document = Map.of(
                    "id", chatMessage.getId().toString(),
                    "guildId", chatMessage.getGuildId().toString(),
                    "userId", chatMessage.getUserId().toString(),
                    "username", chatMessage.getUsername(),
                    "message", chatMessage.getMessage(),
                    "timestamp", chatMessage.getTimestamp().toString()
            );

            String documentId = chatMessage.getId().toString();

            firestore.collection("guild_chat")
                    .document(documentId)
                    .set(document);

            log.debug("Chat message {} persisted to Firestore", chatMessage.getId());
        } catch (Exception e) {
            // Log but don't fail the message send if persistence fails
            log.error("Failed to persist chat message {} to Firestore: {}",
                    chatMessage.getId(), e.getMessage(), e);
        }
    }

    /**
     * Internal record for tracking rate limit windows.
     */
    private record RateLimitEntry(long windowStart, AtomicInteger count) {
    }
}
