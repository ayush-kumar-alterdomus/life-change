package com.ascend.social.service;

import com.ascend.social.dto.ActivityFeedItem;
import com.ascend.social.model.ActivityFeed;
import com.ascend.social.model.PrivacyLevel;
import com.ascend.social.repository.ActivityFeedRepository;
import com.ascend.social.repository.FriendshipRepository;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import com.ascend.xp.event.LevelUpEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityFeedService {

    private final ActivityFeedRepository activityFeedRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    @Transactional
    public void publishActivity(UUID userId, String eventType, String title, String description) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        // Don't publish for PRIVATE users
        if (PrivacyLevel.PRIVATE.name().equals(user.getPrivacyLevel())) {
            log.debug("Skipping activity publish for PRIVATE user={}", userId);
            return;
        }

        ActivityFeed activity = ActivityFeed.builder()
                .userId(userId)
                .eventType(eventType)
                .title(title)
                .description(description)
                .build();

        activityFeedRepository.save(activity);
    }

    @Transactional(readOnly = true)
    public List<ActivityFeedItem> getFeed(UUID userId, int page) {
        // Get friend IDs
        List<UUID> friendIds = friendshipRepository.findAcceptedFriendships(userId).stream()
                .map(f -> f.getUser().getId().equals(userId) ? f.getFriend().getId() : f.getUser().getId())
                .toList();

        if (friendIds.isEmpty()) return List.of();

        // Filter out PRIVATE users
        List<UUID> visibleFriendIds = friendIds.stream()
                .filter(fid -> {
                    User friend = userRepository.findById(fid).orElse(null);
                    return friend != null && !PrivacyLevel.PRIVATE.name().equals(friend.getPrivacyLevel());
                })
                .toList();

        if (visibleFriendIds.isEmpty()) return List.of();

        return activityFeedRepository.findByUserIdInOrderByCreatedAtDesc(visibleFriendIds, PageRequest.of(page, 20))
                .map(a -> {
                    User user = userRepository.findById(a.getUserId()).orElse(null);
                    String username = user != null ? user.getUsername() : "Unknown";
                    return new ActivityFeedItem(a.getUserId(), username, a.getEventType(),
                            a.getTitle(), a.getDescription(), a.getCreatedAt());
                })
                .getContent();
    }

    @EventListener
    public void onLevelUp(LevelUpEvent event) {
        publishActivity(event.getUserId(), "LEVEL_UP",
                String.format("Reached Level %d!", event.getNewLevel()),
                String.format("Leveled up from %d to %d", event.getPreviousLevel(), event.getNewLevel()));
    }
}
