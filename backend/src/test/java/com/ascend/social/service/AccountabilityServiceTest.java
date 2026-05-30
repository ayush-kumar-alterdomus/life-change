package com.ascend.social.service;

import com.ascend.social.dto.AccountabilityPartnerResponse;
import com.ascend.social.model.AccountabilityPartner;
import com.ascend.social.repository.AccountabilityPartnerRepository;
import com.ascend.social.repository.FriendshipRepository;
import com.ascend.notification.service.NotificationService;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountabilityService")
class AccountabilityServiceTest {

    @Mock private AccountabilityPartnerRepository partnerRepository;
    @Mock private FriendshipRepository friendshipRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;

    private AccountabilityService service;
    private UUID userId;

    @BeforeEach
    void setUp() {
        service = new AccountabilityService(partnerRepository, friendshipRepository, userRepository, notificationService);
        userId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("getPartner()")
    class GetPartner {

        @Test
        @DisplayName("should return partner when partnership exists and user is the 'user' side")
        void shouldReturnPartner_userSide() {
            UUID partnerId = UUID.randomUUID();
            User user = new User();
            user.setId(userId);
            user.setUsername("alice");
            User partner = new User();
            partner.setId(partnerId);
            partner.setUsername("bob");

            LocalDateTime createdAt = LocalDateTime.now().minusDays(5);
            AccountabilityPartner partnership = AccountabilityPartner.builder()
                    .id(UUID.randomUUID())
                    .user(user)
                    .partner(partner)
                    .active(true)
                    .createdAt(createdAt)
                    .build();

            when(partnerRepository.findActivePartnershipByUserId(userId))
                    .thenReturn(Optional.of(partnership));

            Optional<AccountabilityPartnerResponse> result = service.getPartner(userId);

            assertThat(result).isPresent();
            assertThat(result.get().partnerId()).isEqualTo(partnerId);
            assertThat(result.get().username()).isEqualTo("bob");
            assertThat(result.get().pairedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("should return partner when partnership exists and user is the 'partner' side")
        void shouldReturnPartner_partnerSide() {
            UUID otherUserId = UUID.randomUUID();
            User otherUser = new User();
            otherUser.setId(otherUserId);
            otherUser.setUsername("charlie");
            User currentUser = new User();
            currentUser.setId(userId);
            currentUser.setUsername("dave");

            LocalDateTime createdAt = LocalDateTime.now().minusDays(2);
            AccountabilityPartner partnership = AccountabilityPartner.builder()
                    .id(UUID.randomUUID())
                    .user(otherUser)
                    .partner(currentUser)
                    .active(true)
                    .createdAt(createdAt)
                    .build();

            when(partnerRepository.findActivePartnershipByUserId(userId))
                    .thenReturn(Optional.of(partnership));

            Optional<AccountabilityPartnerResponse> result = service.getPartner(userId);

            assertThat(result).isPresent();
            assertThat(result.get().partnerId()).isEqualTo(otherUserId);
            assertThat(result.get().username()).isEqualTo("charlie");
        }

        @Test
        @DisplayName("should return empty when no partnership exists")
        void shouldReturnEmpty() {
            when(partnerRepository.findActivePartnershipByUserId(userId))
                    .thenReturn(Optional.empty());

            Optional<AccountabilityPartnerResponse> result = service.getPartner(userId);

            assertThat(result).isEmpty();
        }
    }
}
