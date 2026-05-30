package com.ascend.social.property;

import com.ascend.social.dto.ActivityFeedItem;
import com.ascend.social.model.ActivityFeed;
import com.ascend.social.model.Friendship;
import com.ascend.social.repository.ActivityFeedRepository;
import com.ascend.social.repository.FriendshipRepository;
import com.ascend.social.service.ActivityFeedService;
import com.ascend.user.entity.User;
import com.ascend.user.repository.UserRepository;
import net.jqwik.api.*;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Property 52: Privacy enforcement — PRIVATE users never appear in public feeds.
 */
class PrivacyPropertyTest {

    @Property(tries = 100)
    void privateUsersNeverInFeed(@ForAll("privacyLevels") String friendPrivacy) {
        ActivityFeedRepository feedRepo = mock(ActivityFeedRepository.class);
        FriendshipRepository friendshipRepo = mock(FriendshipRepository.class);
        UserRepository userRepo = mock(UserRepository.class);

        ActivityFeedService service = new ActivityFeedService(feedRepo, friendshipRepo, userRepo);

        UUID currentUserId = UUID.randomUUID();
        UUID friendId = UUID.randomUUID();

        // Setup: currentUser has one friend
        User currentUser = User.builder().id(currentUserId).firebaseUid("uid1")
                .username("me").privacyLevel("PUBLIC").build();
        User friendUser = User.builder().id(friendId).firebaseUid("uid2")
                .username("friend").privacyLevel(friendPrivacy).build();

        Friendship friendship = Friendship.builder()
                .id(UUID.randomUUID())
                .user(currentUser)
                .friend(friendUser)
                .status("ACCEPTED")
                .build();

        when(friendshipRepo.findAcceptedFriendships(currentUserId)).thenReturn(List.of(friendship));
        when(userRepo.findById(friendId)).thenReturn(Optional.of(friendUser));

        // If friend is not PRIVATE, they have feed activity
        ActivityFeed activity = ActivityFeed.builder()
                .id(UUID.randomUUID())
                .userId(friendId)
                .eventType("LEVEL_UP")
                .title("Level 5!")
                .description("Reached level 5")
                .createdAt(LocalDateTime.now())
                .build();

        when(feedRepo.findByUserIdInOrderByCreatedAtDesc(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(activity)));

        List<ActivityFeedItem> feed = service.getFeed(currentUserId, 0);

        if ("PRIVATE".equals(friendPrivacy)) {
            assertThat(feed).isEmpty();
        }
        // PUBLIC and FRIENDS_ONLY users should be visible to friends
        if ("PUBLIC".equals(friendPrivacy) || "FRIENDS_ONLY".equals(friendPrivacy)) {
            assertThat(feed).isNotEmpty();
        }
    }

    @Provide
    Arbitrary<String> privacyLevels() {
        return Arbitraries.of("PUBLIC", "FRIENDS_ONLY", "PRIVATE");
    }
}
