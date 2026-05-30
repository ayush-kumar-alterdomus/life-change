package com.ascend.social.service;

import com.ascend.common.exception.BusinessException;
import com.ascend.notification.dto.NotificationType;
import com.ascend.notification.service.NotificationService;
import com.ascend.social.dto.AccountabilityPartnerResponse;
import com.ascend.social.model.AccountabilityPartner;
import com.ascend.social.repository.AccountabilityPartnerRepository;
import com.ascend.social.repository.FriendshipRepository;
import com.ascend.streak.event.StreakBrokenEvent;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountabilityService {

    private final AccountabilityPartnerRepository partnerRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public void pairPartner(UUID userId, UUID partnerId) {
        if (userId.equals(partnerId)) {
            throw new BusinessException("INVALID_REQUEST", "Cannot pair with yourself");
        }

        // Verify friendship
        friendshipRepository.findFriendshipBetween(userId, partnerId)
                .filter(f -> "ACCEPTED".equals(f.getStatus()))
                .orElseThrow(() -> new BusinessException("NOT_FRIENDS", "Must be friends to pair as accountability partners"));

        // Check existing partnership
        partnerRepository.findPartnershipBetween(userId, partnerId).ifPresent(existing -> {
            throw new BusinessException("ALREADY_PAIRED", "Partnership already exists");
        });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));
        User partner = userRepository.findById(partnerId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Partner not found"));

        AccountabilityPartner partnership = AccountabilityPartner.builder()
                .user(user)
                .partner(partner)
                .build();

        partnerRepository.save(partnership);

        notificationService.sendNotification(partnerId, NotificationType.QUEST_REMINDER,
                "New Accountability Partner! 🤝",
                String.format("%s is now your accountability partner", user.getUsername()));

        log.info("Accountability partnership created: user={} partner={}", userId, partnerId);
    }

    @Transactional(readOnly = true)
    public Optional<AccountabilityPartnerResponse> getPartner(UUID userId) {
        return partnerRepository.findActivePartnershipByUserId(userId)
                .map(partnership -> {
                    User partnerUser = partnership.getUser().getId().equals(userId)
                            ? partnership.getPartner()
                            : partnership.getUser();
                    return new AccountabilityPartnerResponse(
                            partnerUser.getId(),
                            partnerUser.getUsername(),
                            partnership.getCreatedAt());
                });
    }

    @EventListener
    public void onStreakBroken(StreakBrokenEvent event) {
        partnerRepository.findActivePartnershipByUserId(event.getUserId()).ifPresent(partnership -> {
            UUID partnerId = partnership.getUser().getId().equals(event.getUserId())
                    ? partnership.getPartner().getId()
                    : partnership.getUser().getId();

            User user = userRepository.findById(event.getUserId()).orElse(null);
            String username = user != null ? user.getUsername() : "Your partner";

            notificationService.sendNotification(partnerId, NotificationType.STREAK_WARNING,
                    "Partner Streak Broken 💔",
                    String.format("%s lost their %d-day streak. Send them encouragement!",
                            username, event.getPreviousStreak()));
        });
    }
}
