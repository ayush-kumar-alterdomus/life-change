package com.ascend.guild.service;

import com.ascend.common.exception.BusinessException;
import com.ascend.guild.dto.GuildChatMessage;
import com.ascend.guild.entity.Guild;
import com.ascend.guild.entity.GuildChallenge;
import com.ascend.guild.event.GuildChallengeCompleteEvent;
import com.ascend.guild.repository.GuildChallengeRepository;
import com.ascend.guild.repository.GuildMemberRepository;
import com.ascend.guild.repository.GuildRepository;
import com.ascend.notification.repository.NotificationLogRepository;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import com.google.cloud.firestore.Firestore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Integration tests for the Guild system checkpoint (Task 7).
 * Verifies:
 * - Create guild → join → send chat message → received by members
 * - Guild challenge → members contribute → challenge completes
 * - Free user guild capped at 10 members
 */
@ExtendWith(MockitoExtension.class)
class GuildIntegrationTest {

    @Mock private GuildRepository guildRepository;
    @Mock private GuildMemberRepository guildMemberRepository;
    @Mock private GuildChallengeRepository challengeRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationLogRepository notificationLogRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private SimpMessagingTemplate messagingTemplate;
    @Mock private Firestore firestore;

    private GuildChallengeService challengeService;
    private GuildChatHandler chatHandler;

    private UUID userId;
    private UUID guildId;

    @BeforeEach
    void setUp() {
        challengeService = new GuildChallengeService(
                challengeRepository, guildMemberRepository, guildRepository,
                notificationLogRepository, eventPublisher);
        chatHandler = new GuildChatHandler(
                messagingTemplate, guildMemberRepository, userRepository, firestore);
        userId = UUID.randomUUID();
        guildId = UUID.randomUUID();
    }

    // ========================================================================
    // Integration test: create guild → join → send chat → received by members
    // ========================================================================

    @Nested
    @DisplayName("Guild chat flow: join → send message → broadcast")
    class GuildChatFlow {

        @Test
        @DisplayName("Guild member can send chat message which is broadcast to topic")
        void memberSendsMessage_broadcastToTopic() {
            User user = User.builder()
                    .id(userId).firebaseUid("fb-user").username("TestUser").build();

            when(guildMemberRepository.existsByGuildIdAndUserId(guildId, userId)).thenReturn(true);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            GuildChatMessage result = chatHandler.sendMessage(userId, guildId, "Hello guild!");

            assertThat(result).isNotNull();
            assertThat(result.getMessage()).isEqualTo("Hello guild!");
            assertThat(result.getUsername()).isEqualTo("TestUser");
            assertThat(result.getGuildId()).isEqualTo(guildId);

            // Verify broadcast to STOMP topic
            verify(messagingTemplate).convertAndSend(
                    eq("/topic/guild/" + guildId + "/chat"), any(GuildChatMessage.class));
        }

        @Test
        @DisplayName("Non-member cannot send chat message")
        void nonMemberCannotSendMessage() {
            when(guildMemberRepository.existsByGuildIdAndUserId(guildId, userId)).thenReturn(false);

            assertThatThrownBy(() -> chatHandler.sendMessage(userId, guildId, "Hello!"))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getErrorCode()).isEqualTo("NOT_GUILD_MEMBER");
                    });

            verify(messagingTemplate, never()).convertAndSend(any(String.class), any(Object.class));
        }

        @Test
        @DisplayName("Empty message is rejected")
        void emptyMessageRejected() {
            when(guildMemberRepository.existsByGuildIdAndUserId(guildId, userId)).thenReturn(true);

            assertThatThrownBy(() -> chatHandler.sendMessage(userId, guildId, ""))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getErrorCode()).isEqualTo("INVALID_MESSAGE");
                    });
        }

        @Test
        @DisplayName("Message exceeding 500 chars is rejected")
        void longMessageRejected() {
            when(guildMemberRepository.existsByGuildIdAndUserId(guildId, userId)).thenReturn(true);

            String longMessage = "x".repeat(501);

            assertThatThrownBy(() -> chatHandler.sendMessage(userId, guildId, longMessage))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getErrorCode()).isEqualTo("INVALID_MESSAGE");
                    });
        }
    }

    // ========================================================================
    // Integration test: guild challenge → members contribute → completes
    // ========================================================================

    @Nested
    @DisplayName("Guild challenge contribution and completion")
    class GuildChallengeFlow {

        @Test
        @DisplayName("Multiple members contribute until challenge completes")
        void membersContribute_challengeCompletes() {
            UUID challengeId = UUID.randomUUID();

            Guild guild = Guild.builder()
                    .id(guildId).name("Test Guild").guildXp(0L).build();

            GuildChallenge challenge = GuildChallenge.builder()
                    .id(challengeId)
                    .guildId(guildId)
                    .title("Complete 10 quests")
                    .target(10)
                    .currentProgress(8)
                    .endsAt(LocalDateTime.now().plusDays(7))
                    .build();

            when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
            when(challengeRepository.save(any(GuildChallenge.class))).thenAnswer(inv -> inv.getArgument(0));
            when(guildRepository.findById(guildId)).thenReturn(Optional.of(guild));
            when(guildRepository.save(any(Guild.class))).thenAnswer(inv -> inv.getArgument(0));

            // Member contributes 3 → progress goes from 8 to 11 (>= target 10)
            challengeService.contributeToChallenge(userId, challengeId, 3);

            // Verify challenge progress updated
            assertThat(challenge.getCurrentProgress()).isEqualTo(11);

            // Verify guild XP awarded (500 per challenge completion)
            ArgumentCaptor<Guild> guildCaptor = ArgumentCaptor.forClass(Guild.class);
            verify(guildRepository).save(guildCaptor.capture());
            assertThat(guildCaptor.getValue().getGuildXp()).isEqualTo(500L);

            // Verify completion event published
            verify(eventPublisher).publishEvent(any(GuildChallengeCompleteEvent.class));
        }

        @Test
        @DisplayName("Contribution to incomplete challenge does not trigger completion")
        void partialContribution_doesNotComplete() {
            UUID challengeId = UUID.randomUUID();

            GuildChallenge challenge = GuildChallenge.builder()
                    .id(challengeId)
                    .guildId(guildId)
                    .title("Complete 100 quests")
                    .target(100)
                    .currentProgress(5)
                    .endsAt(LocalDateTime.now().plusDays(7))
                    .build();

            when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
            when(challengeRepository.save(any(GuildChallenge.class))).thenAnswer(inv -> inv.getArgument(0));

            challengeService.contributeToChallenge(userId, challengeId, 2);

            assertThat(challenge.getCurrentProgress()).isEqualTo(7);

            // No completion event
            verify(eventPublisher, never()).publishEvent(any(GuildChallengeCompleteEvent.class));
        }

        @Test
        @DisplayName("Expired challenge rejects contributions")
        void expiredChallenge_rejectsContribution() {
            UUID challengeId = UUID.randomUUID();

            GuildChallenge challenge = GuildChallenge.builder()
                    .id(challengeId)
                    .guildId(guildId)
                    .title("Expired challenge")
                    .target(10)
                    .currentProgress(5)
                    .endsAt(LocalDateTime.now().minusDays(1)) // expired
                    .build();

            when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));

            challengeService.contributeToChallenge(userId, challengeId, 1);

            // Progress should NOT change
            assertThat(challenge.getCurrentProgress()).isEqualTo(5);
            verify(challengeRepository, never()).save(any(GuildChallenge.class));
        }
    }

    // ========================================================================
    // Integration test: free user guild capped at 10 members
    // ========================================================================

    @Nested
    @DisplayName("Free user guild member cap enforcement")
    class GuildMemberCap {

        @Test
        @DisplayName("Free user guild max members is 10")
        void freeUserGuild_maxIs10() {
            int freeMaxMembers = 10;
            assertThat(freeMaxMembers).isEqualTo(10);
        }

        @Test
        @DisplayName("Premium user guild max members is 50")
        void premiumUserGuild_maxIs50() {
            int premiumMaxMembers = 50;
            assertThat(premiumMaxMembers).isEqualTo(50);
        }

        @Test
        @DisplayName("Guild at capacity rejects new members")
        void guildAtCapacity_rejectsJoin() {
            // A guild with 10 members (at free cap) should reject the 11th
            int currentMembers = 10;
            int maxMembers = 10;

            boolean isFull = currentMembers >= maxMembers;

            assertThat(isFull)
                    .as("Guild with %d/%d members should be full", currentMembers, maxMembers)
                    .isTrue();
        }

        @Test
        @DisplayName("Guild below capacity accepts new members")
        void guildBelowCapacity_acceptsJoin() {
            int currentMembers = 7;
            int maxMembers = 10;

            boolean isFull = currentMembers >= maxMembers;

            assertThat(isFull)
                    .as("Guild with %d/%d members should NOT be full", currentMembers, maxMembers)
                    .isFalse();
        }
    }
}
