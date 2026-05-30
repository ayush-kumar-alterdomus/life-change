package com.ascend.guild.controller;

import com.ascend.guild.dto.GuildChatMessage;
import com.ascend.guild.service.GuildChatHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

/**
 * STOMP WebSocket controller for guild chat messaging.
 * Clients send messages to /app/guild/{guildId}/chat
 * and receive broadcasts on /topic/guild/{guildId}/chat.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class GuildChatController {

    private final GuildChatHandler guildChatHandler;

    /**
     * Handles incoming chat messages from WebSocket clients.
     * Message mapping: /app/guild/{guildId}/chat
     * Broadcasts to: /topic/guild/{guildId}/chat
     *
     * @param guildId        the guild ID from the destination path
     * @param message        the chat message payload
     * @param headerAccessor STOMP message headers
     */
    @MessageMapping("/guild/{guildId}/chat")
    public void sendMessage(
            @DestinationVariable String guildId,
            @Payload ChatMessageRequest message,
            SimpMessageHeaderAccessor headerAccessor) {

        Principal principal = headerAccessor.getUser();
        if (principal == null) {
            log.warn("Unauthenticated WebSocket message attempt for guild {}", guildId);
            return;
        }

        UUID userId = UUID.fromString(principal.getName());
        UUID parsedGuildId = UUID.fromString(guildId);

        log.debug("Chat message received from user {} for guild {}", userId, parsedGuildId);

        GuildChatMessage chatMessage = guildChatHandler.sendMessage(userId, parsedGuildId, message.getMessage());
        log.debug("Chat message {} processed successfully", chatMessage.getId());
    }

    /**
     * Simple request DTO for incoming chat messages via WebSocket.
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ChatMessageRequest {
        private String message;
    }
}
